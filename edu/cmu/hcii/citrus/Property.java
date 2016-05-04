/*
 * 
 * Citrus - A object-oriented, interpreted language that is designed to simplify 
 * the creation of dynamic, immediate feedback graphical desktop applications.
 * 
 * Copyright (c) 2005 Andrew Jensen Ko
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package edu.cmu.hcii.citrus;

import java.lang.ref.WeakReference;

import java.util.Stack;
import java.util.Vector;

import edu.cmu.hcii.citrus.views.App;
import edu.cmu.hcii.citrus.views.Debug;
import edu.cmu.hcii.citrus.views.Transition;
import edu.cmu.hcii.citrus.views.Translator;
import edu.cmu.hcii.citrus.views.ElementView;

// A pointer to an element. Manages owners and users of elements by adding this as a user
// when it is set to an element, and removing itself as a user when it is set to null.
// A Property is always a user of something, including instances of nothing.
//
// Because Properties can be changed amongst all kinds of threads, this class NEEDS to be thread
// safe with regard to its shared data (the evaluation stack and pointer, for example). Currently, it is NOT!
public class Property<ValueType extends Element> implements Element<Property> {

	/////////////////////////////////////////////////////////////////////////
	// Global Bookkeeping
	/////////////////////////////////////////////////////////////////////////

	private static final Stack<ElementChangeAccumulator> eventAccumulators = new Stack<ElementChangeAccumulator>();
	public static void pushAccumulator(ElementChangeAccumulator accumulator) { eventAccumulators.push(accumulator); }
	public static ElementChangeAccumulator popAccumulator() { return eventAccumulators.pop(); }
	public static ElementChangeAccumulator peekAccumulator() { 
		if(eventAccumulators.empty()) return null;
		else return eventAccumulators.peek(); 
	}

	public static class PropertySetEvent extends ElementChangeEvent {

		private final Element oldValue;
		private final String newValue = "";
		private final Element cause;
		
		public PropertySetEvent(Property property, Element oldValue, Element newValue, Element cause) {
			
			super(property);
			this.oldValue = oldValue;
			//this.newValue = "" + newValue;
			this.cause = cause;

		}
		
		public void undo() {
			
			// Already undone if its a constraint!
			if(((Property)elementChanged).getValueFunction() != null) return;
			
			((Property)elementChanged).set(oldValue);
			
		}
		
		public String toString() { 
			return "" + 
				elementChanged.getElementOwner() + "'s " + 
				((Property)elementChanged).getName() + 
				" was changed from " + 
				oldValue + 
				" to " + 
				newValue +
				" by " + cause; 
		}
		
	}
	
	// A stack of all of the properties currently being evaluated. The 
	// last in is the property currently being evaluated. The "topOfEvaluationStack"
	// integer refers to the index into the stack of the current property being
	// evaluated.
	private static Property<?>[] propertiesBeingEvaluated = new Property[1024];
	private static int topOfEvaluationStack = -1;

	// A stack of the properties and listeners being evaluated, and the index
	// of the current property or listener being evaluated.
	private static enum Action { evaluate, notify, initialize };
	private static Action[] actionStack = new Action[1024];
	private static int topOfActionStack = -1;
	
	public static boolean initializing() { return topOfActionStack >= 0 && actionStack[topOfActionStack].equals(Action.initialize); }

	public static class ValueWillChange extends BootElement {
		public ValueWillChange(ArgumentList args) { super(args); }
	}
	public static class ValueChanged extends BootElement {
		public ValueChanged(ArgumentList args) { super(args); }
	}
	public static class ValidityWillChange extends BootElement {
		public ValidityWillChange(ArgumentList args) { super(args); }
	}
	public static class ValidityChanged extends BootElement {
		public ValidityChanged(ArgumentList args) { super(args); }
	}
	public static class OutOfDate extends BootElement {
		public OutOfDate(ArgumentList args) { super(args); }
	}
	public static class CycleDetected extends BootElement {
		public CycleDetected(ArgumentList args) { super(args); }
	}
	public static class ChangedStructurally extends BootElement {
		public ChangedStructurally(ArgumentList args) { super(args); }
	}
	public static Type VALUE_WILL_CHANGE = null;
	public static Type VALUE_CHANGED = null;
	public static Type VALIDITY_WILL_CHANGE = null;
	public static Type VALIDITY_CHANGED = null;
	public static Type MARKED_OUT_OF_DATE = null;
	public static Type CYCLE_DETECTED = null;


	/////////////////////////////////////////////////////////////////////////
	// Per Instance Bookkeeping
	/////////////////////////////////////////////////////////////////////////

	// The Element that owns this property
	protected final Element owner;

	// The value of this property
	private ValueType value = null;

	// The value function on this property
	private Element<ValueType> function = null;

	// Indicates whether this property's value is valid, according to it's declaration's value sets.
	protected boolean valid = true;

    	// Indicates whether this property's value may be out of date 
	// based on its constraint function.
    private boolean outOfDate;

	// Whether or not the constraint is currently being evaluated. 
	// This is used to detect cycles in the constraint graph.
	private boolean evaluating;
	
	// The number of times that this function has been evaluated.
	private short numberOfEvaluations;

	// The properties that the function depends on.
	private IncomingEdge incoming;

	// The property's outgoing edge bookkeeping
	private OutgoingEdge outgoing;
	private int out = 0;
    
	// Who to tell about property events.
    protected WeakSet<Listener<ValueType>> listeners;

    // The declaration of this property, which determines its default value, value sets, and other info.
    protected DecInterface<ValueType> declaration;

	// The set of views that represent this property's value.
	private WeakSet<ElementView> views;

	// The current transition being used on this property
	private Transition transition = null;

	// The property's previous value
	private ValueType transitionStartValue;
	    
    /////////////////////////////////////////////////////////////////////
    // Constructor.
    /////////////////////////////////////////////////////////////////////
    
	public Property(Element newOwner, DecInterface<ValueType> newDeclaration) {

		if(newDeclaration == null)
			throw new ElementError("This property received a null declaration.", null);
		
		owner = newOwner;
		declaration = newDeclaration;
		numberOfEvaluations = 0;		// Not evaluated yet
	    listeners = null;				// No listeners yet
		evaluating = false;
		outOfDate = true;
		incoming = null;
		outgoing = null;

		// We give value the canonical value and leave it up to Element() to initialize 
		// this property to the appropriate default value according to the declaration
		// by calling initialize();
		Type baseType = declaration.getTypeExpression().getBaseType();
		if(baseType == Boot.INT) value = (ValueType)new Int(0);
		else if(baseType == Boot.REAL) value = (ValueType)new Real(0.0);
		else if(baseType == Boot.TEXT) value = (ValueType)new Text("");
		else if(baseType == Boot.CHAR) value = (ValueType)new Char(' ');
//		else if(baseType == Boot.LIST) value = (ValueType)new List();
		else value = (ValueType)new Nothing();
		
	}
	
	private static Property mostRecentInit = null;
	public Property<ValueType> initialize(Element parameterizedValue, boolean valueIsFunction, Transition t) {

		synchronized(propertiesBeingEvaluated) {
		
		// Push the initalize action on the action stack to prevent dependencies
		// from being created while we evaluate this propertie's default value and value
		// function expressions.
		pushAction(Action.initialize);
		mostRecentInit = this;
		
		try {

			// Is the default value a parameter? If so, we expect a parameterized value here.
			ValueType initialValue = null;
			
			// If a parameterized value was passed in, assign it as the function or value
			if(parameterizedValue != null) {
				if(valueIsFunction) {
					// If there's a function, evaluate it, making it up to date.
					// Evaluate it now to set up dependencies.
					function = parameterizedValue;
					numberOfEvaluations = 0;		// Not evaluated yet
					outOfDate = false;
					markOutOfDateAndPending(t, value, value);
					evaluate();

				}
				else {
					// Set this property to the new initial value.
					// We start the value at the default value so that if this is later
					// undone, it still has a legal value.
					if(declaration.isUndoable() && !declaration.isParameterized()) {
						value = declaration.getDefaultValue(owner);
						if(value == null) value = (ValueType)new Nothing();
					}
					set((ValueType)parameterizedValue, t);
				}
			}
			// If there is no parameterized value, but we expect one, complain.
			else if(declaration.isParameterized())
				throw new ElementError("" + getName() + " expected a value, but none was provided.", owner);
			// If the value is just a default, evaluate it.
			else if(!declaration.functionIsConstraint()) {

				// Set this property to the new initial value.
				set(declaration.getDefaultValue(owner), t);
				// It has its default value now, so its not out of date.
				outOfDate = false;
	
			}
			// If the declaration has a function, we assign the function as this property's value function.
			else if(declaration.functionIsConstraint()) {

				// If there's a function, evaluate it, making it up to date.
				// Evaluate it now to set up dependencies.
				function = declaration.getValueFunction();
				numberOfEvaluations = 0;		// Not evaluated yet
				outOfDate = false;
				markOutOfDateAndPending(t, value, value);
				evaluate();
	
			}

		} 
		catch(Exception e) {
			e.printStackTrace();
			throw new ElementError("Exception while evaluating " + owner + "'s property " + declaration + "'s default value expression: " + e, declaration);
		}
		
		// Pop off the initialize action, now that we're done initializing.
		popAction();

		// Return this initialized property, for convenience in expressions.
		return this;

		}
		
	}

	// Need to actually set the value to test it.
    public boolean willBeSet(ValueType newValue) {

		boolean willBeSet = true;
		ValueType oldValue = value;
		value = newValue;
		try {
			newValue = (ValueType)declaration.validate(this, newValue);
			willBeSet = !(newValue instanceof PropertyRestriction);
		} catch(Exception e) {
			e.printStackTrace();
			willBeSet = false;
		}
		value = oldValue;
		return willBeSet;

    }

	// TODO: Warning: this has only been used to override Declaration's and ListDec's default value's
	// declarations, for the Type and TileType editors. It hasn't been approved for other use.
	public void updateDeclaration(DecInterface<ValueType> newDeclaration) { declaration = newDeclaration; }
	
    	// Returns the number of times the constraint has been evaluated.
    public int getNumberOfEvaluations() { return numberOfEvaluations; }

    // Add a property listener, if it's not already added.
    public boolean addListener(Listener newListener) {

    		boolean changed = false;
    		if(listeners == null) { 
    			changed = true;
    			listeners = new WeakSet<Listener<ValueType>>(newListener);
    		}
    		else changed = listeners.include(newListener);

    		// If this listener is an implicit listener and this isn't a reference, propogate it.
    		if(	changed && 
    			newListener instanceof Observer && 
    			((Observer)newListener).peek(Observer.event) instanceof ChangedStructurally &&
    			!isReference()) {
    			value.propogateListener(newListener, true);
    		}
    		
    		return changed;
    		
    }
    
    // Remove a property listener, if there's a list at all.
    public boolean removeListener(Listener listener) {
    	
    		boolean changed = false;
    	
    		// If there are no listeners, return.
    		if(listeners == null) changed = false;
    		// If the first element is it, set the new first node to the next node.
    		else if(listeners.getElement() == listener) { listeners = listeners.getNext(); changed = true; }
    		// Otherwise, search for the element to remove.
    		else changed = listeners.exclude(listener);
    		
    		// If this listener is a deep listener, propogate it.
    		// We don't unpropogate the listener if this is a reference.
    		if(	changed && 
       		listener instanceof Observer && 
			((Observer)listener).peek(Observer.event) instanceof ChangedStructurally &&
    			!isReference())
    			value.propogateListener(listener, false);
    		
    		return changed;
    		
    }

    public Iterable<Listener<ValueType>> getPropertyListeners() { 
		if(listeners == null) return new Vector<Listener<ValueType>>(0);
		else return listeners;
    }
	
    public DecInterface<ValueType> getDeclaration() { return declaration; }
    
    /////////////////////////////////////////////////////////////////////
    // Constraint handling
    /////////////////////////////////////////////////////////////////////    
	
	// Add an incoming edge to the given property.
	private void addIncomingEdgeFrom(Property p, OutgoingEdge outgoingEdge) { 
		
		incoming = new IncomingEdge(p, incoming, outgoingEdge);
		
	}
	
	// Remove the incoming edge from the given property.
	private void removeIncomingEdgeFrom(Property p) { 

		// Find the incoming edge from the given property
		IncomingEdge previous = null;
		IncomingEdge edge = incoming;
		while(edge != null) { if(edge.property == p) break; previous = edge; edge = edge.next; }

		// If we found it, and its the first edge, set the first edge to the next edge.
	 	// Otherwise, have the previous point to the current's next.
		if(edge != null) {
	 		if(previous == null) incoming = edge.next;
		 	else previous.next = edge.next;
		}
		
	}

	// Remove the outgoing edge to the given property
	private void removeOutgoingEdgeTo(Property p) {
		
		// Find the incoming edge from the given property
		OutgoingEdge previous = null;
		OutgoingEdge edge = outgoing;
		while(edge != null) { if(edge.property.get() == p) break; previous = edge; edge = edge.next; }

		// If we found it, and its the first edge, set the first edge to the next edge.
	 	// Otherwise, have the previous point to the current's next.
		if(edge != null) {
	 		if(previous == null) outgoing = edge.next;
		 	else previous.next = edge.next;
		}
		
		out--;
		
	}
	
    /////////////////////////////////////////////////////////////////////
    // Getters
    /////////////////////////////////////////////////////////////////////    
    	
	public boolean isOutOfDate() { return outOfDate; }
	
	// Returns the property's value. Before returning the value, we first
	// check if any properties depend on this property's value. We then evaluate
	// the property's constraint before returning it. This won't incur any
	// performance penalty if the value is not out of date.
    public ValueType get() {

		synchronized(propertiesBeingEvaluated) {

    		// If we're currently getting this property in the context of a property evaluation 
    		// then add this dependency.
    		if(topOfActionStack >= 0 && actionStack[topOfActionStack].equals(Action.evaluate) && !declaration.isConstant())
    			updateDependency();

    		// Ensure that this property's value is up to date.
    		evaluate();
    		
    		// Return the up to date value.
    		return value instanceof Nothing ? null : value;
    		
		}
    	
   	}
    
    // Returns this property's value without recording dependencies or getting the
    // value up to date. This should only be used if this property should never be part
    // of the constraint graph, and the properties that depend on its values should not be updated
    // when this property's value changes.
    public ValueType peek() { 

		// Ensure that this property's value is up to date.
		evaluate();
		
		// Return the up to date value.
		return value instanceof Nothing ? null : value;

    }

    public boolean owns(Element<?> e) { return e.getPropertyOwner() == this; }
    public boolean isReference() { return declaration.isReferenceOnly() || value.getPropertyOwner() != this; }
    public boolean valueIsNothing() { 
    	
    		peek();
    		return value.isNothing().value; 
    		
    }

    // Make sure to capture the dependency by calling get.
    public Nothing getNothing() { 
    	
    		if(value.isNothing().value) {
    			get();
    			return (Nothing)value;
    		} else return null;
    		
    }
    
    public Element<ValueType> getValueFunction() { return function; }
    public void setValueFunction(Expression<ValueType> newFunction) {
    	
    		function = newFunction;
    		
    		// TODO: I'm not confident that this is all that needs to be done
    		// to have the function update the value properly. We'll see.
    		numberOfEvaluations = 0;
    		outOfDate = true;
    		evaluate();
    	
    }

    // Records a dependency between this and the property currently being evaluated.
    private void updateDependency() {

		synchronized(propertiesBeingEvaluated) {

	    	if(topOfEvaluationStack < 0) return;
	    	
			// Get the property that's currently being evaluated.
			Property propertyEvaluating = propertiesBeingEvaluated[topOfEvaluationStack];
			
			// Allow dependencies to self. Why?
			// (1) Change listeners may want to use the new value, and technically,
			// just after setting it as part of a constraint, it hasn't finished evaluating
			// yet. 
			// (2), it may be convenience to have something depend on itself (for example, something
			// that's conditionally constrained to some expression, and otherwise its current value.
			// (3) property restrictions need to reference the property they restrict 
	//		if(propertyEvaluating == this)
	//			return;
			
			// Do we have an outgoing edge to the property being evaluated?
			// We have two options when it comes to searching for it:
			// (1) Search through the outgoing edges
			// (2) Search through the incoming edges of the property evaluating.
			// The fastest would be to search the smaller of the two lists,
			// but that would require storing the length of both.
			// We determined empirically that the incoming edge lists are typically shorter.
	
			// Is this an incoming property of the property being evaluated?
			OutgoingEdge edgeToPropertyEvaluating = null;
			IncomingEdge previous = null;
			for(IncomingEdge incomingEdge = propertyEvaluating.incoming; incomingEdge != null; incomingEdge = incomingEdge.next) {
				if(incomingEdge.property == this) {
					edgeToPropertyEvaluating = incomingEdge.outgoingEdge;
					break;
				}
			}
			
			// If there's already an edge to the property, equate the edge's number of uses 
			// to the number of times the property being evaluated has been evaluated.
			// When a property is marked out of date, if these numbers don't match, this dependency will be removed,
			// since we'll know they haven't been evaluated the same number of times.
			if(edgeToPropertyEvaluating != null) 
				edgeToPropertyEvaluating.numberOfUses = propertyEvaluating.getNumberOfEvaluations();
			// Otherwise, add the outgoing edge to the property being evaluated and
			// an incoming edge from this to the property being evaluated.
			else {
	
				// Insert a new outgoing edge in front.
				outgoing = new OutgoingEdge(propertyEvaluating, outgoing); 
		 		propertyEvaluating.addIncomingEdgeFrom(this, outgoing);
		 		out++;
	 			if(out > 100 /*out % 100 == 0*/) {
	 				System.err.println("Look! " + owner + "'s " + getName() + " has " + out + 
	 						" outgoing edges, most recently to " + propertyEvaluating.getName() + 
	 						" as part of " + propertiesBeingEvaluated[topOfEvaluationStack].function);
	 			}
			}

		}
		
    }
    
    public void resetConstraintBookkeeping() { numberOfEvaluations = 0; }
    
    public boolean isEvaluating() { return evaluating; }
    
	// This method updates this property's value if any of the property's it depends on have changed.
    // Dependencies include properties referenced in constraints or restrictions.
	private boolean evaluate() {

		synchronized(propertiesBeingEvaluated) {

			// We only evaluate this property if it is out of date.
			if(!outOfDate) return false;
			
			// If this property's evaluating flag is set, then we must have already
			// called evaluate on this property, and thus a cycle exists in the 
			// dependency graph. We avoid evaluating to ensure that this method halts.
			// The one exception is if this is the property currently being evaluated.
			// This is not a cycle: evaluate A -> evaluate B -> set B -> notify for B -> get A
			if(evaluating) {
				
				if(propertiesBeingEvaluated[topOfEvaluationStack] != this) cycle(); 
				return false; 
	
			}
			
			// Mark this property as up to date, since we're about to make it up to date.
			outOfDate = false;
	
			// Push this property onto the stack of currently evaluating properties.
			++topOfEvaluationStack;
			propertiesBeingEvaluated[topOfEvaluationStack] = this;
			pushAction(Action.evaluate);
			
			// Set the property's evaluating flag so that we can detect cyclic dependencies.
			evaluating = true;
			
			// Whether any incoming edges' values are pending. Starts as false, unless
			// a new constraint has just been applied that has yet to be evaluated.
			boolean hasPendingValue = (numberOfEvaluations == 0);
	
			// Do any of the incoming edges have values pending?
			IncomingEdge previous = null;
			for(IncomingEdge incomingEdge = incoming; incomingEdge != null; incomingEdge = incomingEdge.next) {
	
				// If the edge coming into this property has been used fewer 
				// number of times than this property has been evaluated, then remove it.
				// It must not have been used in the last evaluation.
				if(incomingEdge.outgoingEdge.numberOfUses < numberOfEvaluations) {
	
					// If the edge is the first edge, set the first edge to the next edge.
			 		if(previous == null) incoming = incomingEdge.next;
				 	// Otherwise, have the previous point to the current's next.
				 	else previous.next = incomingEdge.next;
	
			 		// Remove this from the property's incoming edges
					incomingEdge.property.removeOutgoingEdgeTo(this);
	
					// We don't advance the previous (since it hasn't changed)
	
				}
				// Otherwise, advance the previous and evaluate the incoming property.
				else {
					
					// Evaluate the property, to determine it has a pending value.
					incomingEdge.property.evaluate();
					// If the edge is pending, set the flag to true;
					hasPendingValue = hasPendingValue || incomingEdge.outgoingEdge.pending;
					// Set the pending flag of the edge to this property to false.
					incomingEdge.outgoingEdge.pending = false;
	
					previous = incomingEdge;
					
				}
	
			}
	
			// If there's a value pending, then execute the constraint to get the new value.
			if(hasPendingValue) {
	
				// Increment the number of evaluations by one. All of the current dependencies
				// in the function will be matched to this value (via updateDependency()), 
				// while the stale dependencies will have the old value, and be removed
				// the next time this is evaluated.
				numberOfEvaluations++;
	
				// Try evaluating the function. By default, we'll use set to the current value. If there's a 
				// null pointer exception, we leave the value alone.
				ValueType newValue = value;
				if(function != null) {
					try {
						if(function instanceof Closure) {
							newValue = (ValueType)((Closure)function).instantiate(new ArgumentList());
						}
						else 
							newValue = function.evaluate(owner); 
					}
					catch(Exception e) { e.printStackTrace(); }
				}
	
				// After evaluating the constraint function, are any of the incoming edges 
				// stale? Note that instead of doing this eagerly, we could wait for the incoming
				// properties to change, which would also remove stale edges. However, if they
				// never change, then references to this property would never be garbage collected.
	
				// Pop it off the stack before we set the value, to avoid creating
				// unwanted dependencies.
				popAction();
				propertiesBeingEvaluated[topOfEvaluationStack] = null;
				--topOfEvaluationStack;
				evaluating = false;
	
				// Now validate, set the new value, mark out of date, notify of validity changes, etc.
				setHelper(newValue, null);
	
				// Mark this property as up to date AGAIN, just in case setting it marked it out of date.
				outOfDate = false;
				
				return true;
	
			} else {
	
				// Pop it off the stack, and unset the flag.
				popAction();
				propertiesBeingEvaluated[topOfEvaluationStack] = null;
				--topOfEvaluationStack;
				evaluating = false;
	
				// Mark this property as up to date AGAIN, just in case setting it marked it out of date.
				outOfDate = false;
				
				return false;
	
			}
			
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Setters
	//////////////////////////////////////////////////////////////////////////////////////////

	// Gets ownership of the value that this property points to to this property,
	// taking it away from its current owner.
	public Bool requestOwnership() {

		Property oldOwner = value.getPropertyOwner();
		if(oldOwner == this) return Bool.TRUE;

		if(oldOwner != null) value.unregisterProperty(oldOwner);
		value.unregisterProperty(this);
		value.registerProperty(this);
		if(oldOwner != null) value.registerProperty(oldOwner);
		
		return Bool.TRUE;
		
	}

	// Sets the canonical value, transitioning the interim value between the 
    // old canonical and the new canonical over the transition time, which is in milliseconds. 
    // Remembers the time of the set. Returns true if the value changed, false if not.
    // NOTE: THIS IS CALLED BY BOTH THE USER AND BY evaluate().
    public boolean set(ValueType value) { return set(value, null); }
	public boolean set(ValueType newValue, Transition newTransition) {

		boolean success = setHelper(newValue, newTransition);
		// Update the constrained value
		if(function != null) {
			outOfDate = true;
			numberOfEvaluations = 0;
			evaluate();
		}
		return success;
		
	}
	
	private boolean setHelper(ValueType newValue, Transition newTransition) {

		synchronized(propertiesBeingEvaluated) {

			// If the new element is null, convert it to a unique nothing.
			if(newValue == null) newValue = (ValueType)new Nothing();
	
			/////////////////////////////
			// VALIDATION
			//
			// The next step is to validate the new value.
			//
			//
			
			// Remember the old value
			ValueType oldValue = value;
	
			// Have the declaration validate this new value.    		
			// Note that we have to catch potential null pointer exceptions here, otherwise, 
			// setting will not succeed, and thus if this is being 
			// called by Property.evaluate(), this property's evaluate flag will not be unset,
			// and we would get weird notifications about cycles that don't exist.
			Element validatedValue = null;
			boolean newValueIsValid = true;
	
			// We push the evaluate action and push this property on the evaluation stack in order 
			// to catch any dependencies in the validation.
			++topOfEvaluationStack;
			propertiesBeingEvaluated[topOfEvaluationStack] = this;
			pushAction(Action.evaluate);
			value = newValue;
			try {
				validatedValue = (ValueType)declaration.validate(this, newValue);
			    newValueIsValid = !(validatedValue instanceof PropertyRestriction);
			} catch(Exception e) {
				System.err.println("EXCEPTION DURING VALIDATION OF " + newValue);
				e.printStackTrace();
				newValueIsValid = false;
			}
			value = oldValue;
			popAction();
			propertiesBeingEvaluated[topOfEvaluationStack] = null;
			--topOfEvaluationStack;
	
		    // Did validity change?
		    boolean validityChanged = (newValueIsValid != valid);
	
		    // Update the validity flag.
		    valid = newValueIsValid;
	
		    if(valid)
		    		newValue = (ValueType)validatedValue;
			
			/////////////////////////////
			// UPDATE AND NOTIFICATION
			//
			// Next we assign the new value, and if it's not equivalent to the old value, we
			// mark dependent properties out of date and notify listeners about the change.
			//
			
		    // The value changed if: (1) the old value is null and the new value is not; 
			// (2) the new value is null and the old value is not; or (3) neither are null and they are not equal.
		    boolean changed = 
		    		oldValue != newValue && 
		    		oldValue == null ? 
		    				newValue != null : 
		    				!oldValue.isEquivalentTo(newValue).value;
	
			// If the value changed...
			if(changed) {
	
				// Notify listeners that the value will change
				notifyListenersOf(VALUE_WILL_CHANGE, newTransition, oldValue, newValue);
	
				// Unpropogate listeners from the old value
				if(listeners != null)
					for(Listener listener : listeners)
						if(listener instanceof Observer && 
							((Observer)listener).peek(Observer.event) instanceof ChangedStructurally)
							oldValue.propogateListener(listener, false);
				
				// Set the new value...
				value = newValue;
				
				/////////////////////////////
				// OWNERSHIP
				//
				// Our first responsibility is to maintain the property owner pointers,
				// so that every element has backpointers to the things that point to it.
				// We unregister this property with the old value, and register it with the new value.
				//
	
				oldValue.unregisterProperty(this);
				newValue.registerProperty(this);
	
				// Mark this out of date and its outgoing edges pending.
				markOutOfDateAndPending(newTransition, oldValue, newValue);
				
				// Out of date if this is constrained. This way, next time its
				// accessed it will be updated.
				if(function != null) outOfDate = true;
	
			}
			
		    // If validity changed, notify this property's listeners.
			if(validityChanged) {
				boolean handled = notifyListenersOf(VALIDITY_CHANGED, newTransition, Bool.valueOf(valid));
				if(!valid && !handled)
					System.err.println(
							"" + newValue + 
							" is invalid for " + getDeclaration() + " of " + 
							getDeclaration().ownerOfType(Boot.TYPE) + 
							" because " + validatedValue + "; not setting.");
				
			}
	
			// Record the set event in the accumulator at the top
			if(!eventAccumulators.empty()) {
				// Only if we're not initializing
				if(getDeclaration().isUndoable()) {
					ElementChangeEvent event = new PropertySetEvent(this, oldValue, newValue, null);
					eventAccumulators.peek().recordEvent(event);
				}
				
			}
			
			// Return to the user if the value changed.
		    return changed;
	 
		}
		
	}
	
	private void pushAction(Action action) {

		topOfActionStack++;
		actionStack[topOfActionStack] = action;

	}
	
	private void popAction() {
	
		actionStack[topOfActionStack] = null;
		topOfActionStack--;
		
	}
    
	// A convenience way of getting this property's valid values.
    public Set<ValueType> getValidValues(Set<ValueType> values) { return declaration.getValidValues(this, values); }

    /////////////////////////////////////////////////////////////////////////////////
    // Methods for invalidating property's values.
    /////////////////////////////////////////////////////////////////////////////////
    
    // Mark each property depending on this as out of date. This is called automatically.
    // The only circumstances where something else might want to call this is to force
    // an update (for example, if the screen needed to be repainted, and the updater didn't do it).
    public void touch(Transition t) { markOutOfDateAndPending(t, value, value); }
	public void markOutOfDateAndPending(Transition newTransition, ValueType oldValue, ValueType newValue) {
		
		// Propogate implicit listeners to the new value
		 if(listeners != null)
			for(Listener listener : listeners)
				if(listener instanceof Closure && 
					((Observer)listener).peek(Observer.event) instanceof ChangedStructurally) {
					newValue.propogateListener(listener, true);
				}

		// Notify the change handlers of the change to this property's value. We set a flag
		// to prevent dependencies from being created while listeners execute.
		notifyListenersOf(VALUE_CHANGED, newTransition, oldValue, newValue);
		
		if(views != null) updateViews(newValue, newTransition);
		
		if(newTransition != null) prepareNewTransition(newTransition, oldValue);
				
		// Mark each variable depending on property out of date
		markOutOfDate(newTransition);

		// Mark each outgoing edge as pending, telling the property that a value is pending.
		for(OutgoingEdge edge = outgoing; edge != null; edge = edge.next) {
			edge.pending = true;
		}
	
	}

	private void updateViews(ValueType newValue, Transition t) {

		java.util.Iterator<ElementView> oldViews = views.iterator();
		java.util.LinkedList<ElementView> newViews = new java.util.LinkedList<ElementView>();
		if(Translator.debug) System.err.println("\nUpdating views for " + this);
		// For each of the property's views
		while(oldViews.hasNext()) {

			// Remove the old view from the property's view list.
			ElementView oldView = oldViews.next();

			if(oldView.getWindow() == null)
				oldViews.remove();
			else {

				if(Translator.debug) System.err.println("\tUpdating " + oldView + " with window " + oldView.getWindow());

				// Ask the view for a suitable replacement for itself.
				ElementView newView = oldView.updatedViewFor(this, newValue, t);
				if(newView != oldView) {
					oldViews.remove();
					newViews.add(newView);
					oldView.replaceWith(newView, t);
				}
				if(Translator.debug) System.err.println("\tAsked " + oldView + " for new view and it gave me " + newView);

			}

		}
		if(Translator.debug) System.err.println("Done updating views for " + this);
		
		// Add the new views to the property's view list.
		for(ElementView newView : newViews) addView(newView);
		
	}

	// Mark each outgoing edge and its outgoing edges' as out of date.
	public void markOutOfDate(Transition newTransition) {

		// If the property isn't out of date, mark it and all of its dependents out of date
		if(!outOfDate) {

			outOfDate = true;

			// Mark all upstream dependents as out of date
 			OutgoingEdge previous = null;
 			OutgoingEdge edge = outgoing;
 			while(edge != null) {

 				// If this edge has no property, then the edge is stale, and should
 				// be removed. Note that we do not remove the incoming edge, because
 				// it has already been garbage collected.
 				if(edge.property.get() == null) {
 				
 					// If the edge is the first edge, set the first edge to the next edge.
			 		if(previous == null) outgoing = edge.next;
				 	// Otherwise, have the previous point to the current's next.
				 	else previous.next = edge.next;

 				}
 				// If the edge has been used fewer times than the property has been evaluated,
 				// remove the edge.
 				else if(edge.numberOfUses < (edge.property.get()).getNumberOfEvaluations()) {
 					
 					// If the edge is the first edge, set the first edge to the next edge.
			 		if(previous == null) outgoing = edge.next;
				 	// Otherwise, have the previous point to the current's next.
				 	else previous.next = edge.next;

			 		// Remove this from the property's incoming edges
					(edge.property.get()).removeIncomingEdgeFrom(this);
					
					// We don't advance the previous (since it hasn't changed)

 				}
 				// Otherwise, mark the dependent out of date. Note that we don't notify listeners
 				// before this loop because then we'd send out of date events to properties that
 				// simply changed.
				else {

					(edge.property.get()).markOutOfDate(newTransition);
					(edge.property.get()).notifyListenersOfOutOfDate(newTransition);

	 				// Advance the previous edge.
	 				previous = edge;

				}

 				// Advance the edge
 				edge = edge.next;
				 				
			}

		}

	}
	
	private void notifyListenersOfOutOfDate(Transition newTransition) {

		// Tell all of the change handlers that this property is out of date
		notifyListenersOf(MARKED_OUT_OF_DATE, newTransition, value);

		if(newTransition != null) prepareNewTransition(newTransition, value);
		
	}
	
	private void prepareNewTransition(Transition t, ValueType oldValue) {

		// If there's current transition, set the start value to the old value.
		if(transition == null) transitionStartValue = oldValue;
		// Otherwise, start at the old transition's intermediate value.
		else transitionStartValue = (ValueType)value.getTransitionalValue(transition, transitionStartValue, oldValue, App.getUpdateTime());

		// Set the new transition and reset its start time.
		transition = t;
		transition.reset();

	}

    public ValueType getVisible() {

		// If there is no transition, return the canonical value.
		if(transition == null) return get();
		// If the transition is done, nullify and return.
		else if(isDoneTransitioning()) { transition = null; return get(); }
		// Otherwise, get the transitional value and set it.
		else return (ValueType)value.getTransitionalValue(transition, transitionStartValue, get(), App.getUpdateTime());
		
	}
    
    public boolean isDoneTransitioning() { return transition == null || transition.isComplete(App.getUpdateTime()); }

	public boolean valueIsRecoverable() { return getDeclaration().valueIsRecoverable(value); }
	
	public boolean valueIsEqualToDefault() {
		
		ValueType defaultValue = getDeclaration().getDefaultValue(getElementOwner());
		if(value == null) return false;
		else if(value instanceof Nothing && defaultValue instanceof Nothing) return false;
		else if(getDeclaration().getValueFunction() != null) return false;
		else if(value.isEquivalentTo(defaultValue).value) return false;
		else return true;
		
	}
	
	private void cycle() {
		
		// Construct the list of dependencies that make the cycle.
		// Stop including properties when we reach this property.
		List<Property> cycle = new List<Property>();
		
		// Find the first occurence of this property on the stack.
		int i = 0;
		Property p = null;
		while(i <= topOfEvaluationStack && p != this) { p = propertiesBeingEvaluated[i++]; }
		i--;
		// Add all subsequent properties that it depends upon.
		while(i <= topOfEvaluationStack) cycle.append(propertiesBeingEvaluated[i++]);

		notifyListenersOf(CYCLE_DETECTED, cycle);
		
	}
		
	private String stackToString() {

		String s = "When evaluating " + propertiesBeingEvaluated[0].getElementOwner() + "'s " + propertiesBeingEvaluated[0].getName() + ", determined that ...\n";	
		s = s + "" + getElementOwner() + "'s " + getName() + " depends on\n";
		int i = topOfEvaluationStack;
		while(i >= 0/* && propertiesBeingEvaluated[i + 1] != this*/) {
			Property<?> p = propertiesBeingEvaluated[i];
			s = s + "" + p.getElementOwner() + "'s " + 
					p.getName();
			if(function != null) 
				s = s + "(" + p.getValueFunction().getClass() + 
						"), which depends on\n";
			--i;
		}
		return s;
		
	}
	
	// Returns true of a listener handled the event.
	public boolean notifyListenersOf(Type eventType, Element ... arguments) {
			
			if(listeners == null) return false; 
			if(eventType == null) return false;
	
			boolean handled = false;
			// Push the notify action on the stack
			pushAction(Action.notify);
			
			// Evaluate each listener of the appropriate type
			for(Listener listener : listeners) {
	
				if(listener instanceof Observer && ((Observer)listener).get(Observer.event) == eventType) {
	
					handled = true;
					ArgumentList eventArgs = new ArgumentList();
					eventArgs.enclosingEnvironment = this;
					Element event = eventType.instantiate(eventArgs);
					
					ArgumentList args = new ArgumentList();
					args.add(((Observer)listener).getName(), event);
					((Observer)listener).instantiate(args);
					
				}
				// Backwards compatibility for old notification mechanism
				else {
	
					handled = true;
					if(eventType == MARKED_OUT_OF_DATE) listener.outOfDate(this, (Transition)arguments[0], arguments[1]);
					else if(eventType == VALUE_CHANGED) listener.changed(this, (Transition)arguments[0], arguments[1], arguments[2]);
					else if(eventType == VALIDITY_CHANGED) listener.validityChanged(this, (Transition)arguments[0], ((Bool)arguments[1]).value);
					else if(eventType == CYCLE_DETECTED) listener.cycle(this, (List<Property>)arguments[0]);
				
				}
	
			}
	
			// Now that we're done with the action, pop it off the stack.
			popAction();
	
			return handled;

	}
		
	/////////////////////////////////////////////////////////////
	//
	// Translation
	//
	/////////////////////////////////////////////////////////////
		
	public TypeExpression getTypeExpression() { return declaration.getTypeExpression(); }

	public Text toXML() {

		String s = "";
		s = s + "<" + getName() + ">";
		Text valueText = (isReference() ? value.toCitrusReference() : value.toXML());
		boolean containsNewline = valueText.value.indexOf('\n') >= 0;
		if(containsNewline) s = s + "\n";
		s = s + valueText;
		if(!valueText.value.endsWith("\n") && containsNewline) s = s + "\n";
		s = s + "</" + getName() + ">";
		return new Text(s);

	}

	//////////////////////////////////////////////////////////////////////////////////
	//
	// ElementInterface
	//
	//////////////////////////////////////////////////////////////////////////////////	

	// TYPE
	public Namespace<?> getType() { return Boot.PROPERTY; }
	public Bool isa(Type t) { return new Bool(t == Boot.PROPERTY); }

	// PROPERTIES
	public Set<Property<?>> getProperties() { throw new ElementError("Properties have no properties.", null); }
	public Property getProperty(Text name) { return null; }
	public Bool hasa(Text name) { return new Bool(false); }
	public Element<?> getEnclosingInstance() { return null; }

	// GETTING
	public Element get(Text name) { return null; }
	public <ValueType extends Element<?>> ValueType get(DecInterface<ValueType> declaration) { return null; }

	// SETTING
	public <ValueType extends Element<?>> boolean set(DecInterface<ValueType> dec, ValueType value) {
		throw new ElementError("Nothing to set on a Property.", this);		
	}
	public Bool set(Text propertyName, Element value) {
		throw new ElementError("Nothing to set on a Property.", this);		
	}

	// EQUALITY
	public Bool isEquivalentTo(Element<?> o) { return new Bool(this.equals(o)); }
	public Bool is(Element e) { return new Bool(this == e); }
	
	// PREDICATES
	public boolean isValid() { 
		
		// Update the value and validity, if necessary
		get();
		return valid; 
		
	}
	public Bool isSomething() { return Bool.TRUE; }
	public Bool isNothing() { return Bool.FALSE; }

	// OWNERS
	
	// TODO This doesn't really make sense, so we'll ignore it for now.
	public final void registerProperty(Property<?> p) {}
	public final void unregisterProperty(Property<?> p) {}
	public Property getPropertyOwner() { return this; }
	public Element<?> getElementOwner() { return owner; }
	
	// USERS
	public Element replaceWith(Element oldElement, Element newElement, Transition t) { 
		
		return value.replaceWith(oldElement, newElement, t);		
				
	}
	
	// Ask the value to disown this
	public Bool disown(Element e, Transition t) { return value.disown(e, t); }
	public Bool emancipateOverTime(Transition t) { return new Bool(false); }
	public Bool emancipate() { return new Bool(false); }

	// DUPLICATION
	// If this is a reference, return the element referenced. Otherwise, duplicate.
	public ValueType duplicate() {

		if(isReference()) return value.isNothing().value ? value : get(); 
		else return (ValueType)value.duplicate(); 
		
	}

	// TRANSLATION
	public Text toCitrus() { return new Text(CitrusParser.PROPERTY + getName().value); }
	public Text toCitrusReference() { return toCitrus(); }
	public Text toText() { return new Text(toString()); }
	
    public String toString() { 

		String s = super.toString();
		String hex = s.substring(s.lastIndexOf('@'));
		return "@" + getName() + " = " + value + "(" + hex + ")"; 
		
    }


	//////////////////////////
	//
	// EVALUATION
	//
	public Property evaluate(Element<?> env) { return this; /*get();*/ }
	public Type resultingType() { return declaration.getTypeExpression().getBaseType(); }
	public Context contextFor(Element e) { return null; }

	// VIEW STATE
	public void addViewState(ViewState viewState) {}

	public Iterable<Property> getUsers() { return null; }
	public void addView(ElementView newView) {

		if(views == null) views = new WeakSet<ElementView>(newView);
		else views.include(newView);
		
	}
	public void removeView(ElementView oldView) { if(views != null) views.exclude(oldView); }
	public final ElementView getFirstView() { 
		if(views == null) return null;
		else {

			for(ElementView view : views) 
				if(view.getWindow() != null) return view;
			return null;
		}
	}
	public final Iterable<ElementView> getViews() {
		if(views == null) return new java.util.Vector<ElementView>(0);
		else return views;
	}
	public Property getTransitionalValue(Transition t, Property start, Property end, long time) { return end; }
	public Element ownerOfType(Namespace type) { return owner.ownerOfType(type); }
	public Element getFirstChildOfType(Namespace type) { return value.getFirstChildOfType(type); }

	public void propogateListener(Listener listener, boolean add) {
				
		// Add this listener. If it was already included, we stop here.
		// Otherwise, we propogate the listener to this property's value.
		if(add) addListener(listener);
		else removeListener(listener);
		
	}

	//////////////////////////////////////////////////////////////////////////////////
	//
	// String mappings.
	//
	//////////////////////////////////////////////////////////////////////////////////	
	
	public Text getName() { return declaration.getName(); }

    public String outgoingEdgesToString() {
    	
    		String s = "" + getElementOwner() + "'s " + getName() + " affects:\n";
		OutgoingEdge edge = outgoing;
 		while(edge != null) { 
 			s = s + "\t" + (edge.property.get()).getElementOwner() + "'s " + 
				(edge.property.get()).getName() + "\n"; 
 			edge = edge.next; 
 		}
 		return s;
    	
    }

    // This does NOT invoke o(), and create dependencies. It only peeks.
    public String incomingEdgesToString() {
    	
		String s = "" + owner + "'s " + getName() + " depends on:\n";
		IncomingEdge edge = incoming;
 		while(edge != null) { s = s + "\t" + edge.property.getElementOwner().getType().getName() + "'s " + edge.property.getName() + "\n"; edge = edge.next; }
 		return s;
    	
    }

    public void finalize() {

    	// This was causing thread issues with the finalization thread
		if(Debug.propertyFinalization())
			Debug.print("Finalizing " + getElementOwner() + "'s " + getName());
    	
    }
    
	protected void debug(String s) { System.err.println("" + owner + "'s " + getName() + ": " + s); }
	
}

// An edge holds a property and points to the next in the list and
// supports insertion to the front and removal from anywhere.
final class IncomingEdge {
	
	public Property property;			// The property that this property depends on.
	public IncomingEdge next;			// The next incoming edge in the list.
	public OutgoingEdge outgoingEdge;	// The corresponding outgoing edge equivalent to this edge.
	
	public IncomingEdge(Property newProperty, IncomingEdge newNext, OutgoingEdge newOutgoingEdge) { 
		
		property = newProperty;
		next = newNext;
		outgoingEdge = newOutgoingEdge;
	
	}
			
}

// An outgoing edge is annotated with a pending flag, which indicates that its value 
// has yet to propogate, and a number of uses counter, for comparison with the dependents' 
// number of evaluations.
final class OutgoingEdge {
	
	public WeakReference<Property> property;	// The property that depends on this property.
	public OutgoingEdge next;			// The next outgoing edge in this list.
	public int numberOfUses;			// The number of times this outgoing edge has been used.
	public boolean pending;			// Whether or not this property's value is pending.
	
	public OutgoingEdge(Property newDependentProperty, OutgoingEdge newNext) { 
		
		// Set the property and make the number of uses match the number of evaluations.
		property = new WeakReference<Property>(newDependentProperty);
		next = newNext;
		numberOfUses = newDependentProperty.getNumberOfEvaluations(); 
		pending = true;
		
	}
	
}