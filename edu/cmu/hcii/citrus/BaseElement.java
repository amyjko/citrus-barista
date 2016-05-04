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

import java.util.LinkedList;
import java.util.Hashtable;

import edu.cmu.hcii.citrus.views.Debug;
import edu.cmu.hcii.citrus.views.ElementView;
import edu.cmu.hcii.citrus.views.Transition;

public class BaseElement<T extends Element> extends AbstractElement<T> {

	// Bootstrapped types for the elements language.
	// NOTE: We do declare the element owner and enclosing instance declarations, but
	// we don't add them to the type, since we handle them different during element instantiation.
	public static final BootDec<Element> environment = new BootDec<Element>(new Nothing(), true);
	public static final BootDec<Property<Element>> owner = new BootDec<Property<Element>>(new Nothing(), true);

	// Make sure that we've bootstrapped Citrus
	static { Boot.init(); }

	/////////////////////////////////////////////////
	//
	// Members
	//
	/////////////////////////////////////////////////

	// Points to the owner of the property that current owns this element.
	protected final Property<Property<Element>> propertyOwner;

	// This is the table of properties
	private final Hashtable<String, Property<?>> properties;
	
	private LinkedList<ViewState> viewStateList = null;
	
	// This is the element's type object, which defines the element's semantics.
	// Note that it is NOT a Property, since it will never change.
	// TODO: This should be final, since it never changes, but it in fact does change because of the kludge
	// we have for creating Java elements.
	protected Namespace<?> namespace;

	/////////////////////////////////////////////////
	//
	// Events
	//
	/////////////////////////////////////////////////
	
	/////////////////////////////////////////////////
	//
	// Constructors
	//
	/////////////////////////////////////////////////

	public BaseElement() { this(null, null); }
	public BaseElement(ArgumentList arguments) { this(null, arguments); }
	public BaseElement(Namespace type, ArgumentList arguments) {

		// Make a new element owner property to point to the owner.
		propertyOwner = new Property<Property<Element>>(this, owner);
		propertyOwner.initialize(null, false, null);

		// If no type was provided, get one from this element's class.
		if(type == null) namespace = Reflection.getJavaType(getClass());
		// Otherwise, use the type provided.
		else namespace = type;

		// Make the table of an appropriate size. One extra for the element owner
		// and one extra for the enclosing instance.
		properties = new Hashtable<String,Property<?>>(namespace.getNumberOfDeclarations() + 2);
		Property<?>[] propertyList = new Property[namespace.getNumberOfDeclarations()];

		// Is there an inclosing instance in the arguments?
		if(arguments != null) {
			Element instance = arguments.enclosingEnvironment;
			if(instance != null) {

				// Create the enclsing instance property, set it, and add it to the properties.
				Property<Element> enclosingInstanceProperty = environment.make(this);
				enclosingInstanceProperty.set(instance);
				addProperty(enclosingInstanceProperty);

			}
		}

		// Instantiate all of the properties for whom arguments were passed.
		if(arguments != null) {
			for(ArgumentList.Argument arg : arguments.arguments) {

				DecInterface<?> declarationToInstantiate = null;

				// Find the particular declaration in the declarations to instantiate that has the same name
				declarationToInstantiate = namespace.getDeclarationOf(arg.name);
				if(declarationToInstantiate == null) 
					throw new ElementError("This element was passed an argument for \"" + arg.name +
							"\" (with value " + arg.value + "), but I couldn't find the corresponding declaration " + 
							"in the the namespace " + namespace, null);

				// Make the property, add it, and initialize it with the value from the argument table.
				// TODO: But this means we'll be ignoring parameterized values if they've already been initialized!
				if(properties.containsKey(declarationToInstantiate.getName().value))
					getProperty(declarationToInstantiate.getName()).initialize(arg.value, arg.isConstraint, null);
				else addProperty(declarationToInstantiate.make(this)).initialize(arg.value, arg.isConstraint, null);

			}
		}

		// Initialize all of the whens
		if(namespace instanceof BaseType) {
			Type proto = (BaseType)namespace;
			while(proto != null && proto instanceof BaseType) {
				for(When handler : ((BaseType)proto).peek(BaseType.handlers)) {
					if(listenersDeclared == null) listenersDeclared = new LinkedList<Listener>();
					listenersDeclared.add(new Observer(this, handler));	
				}
				proto = proto.getPrototype();
			}
		}		
		
	}

	/////////////////////////////////////////////////
	//
	// Methods
	//
	/////////////////////////////////////////////////

	// TYPE
	public Namespace<?> getType() { return namespace; }

	
	// Adds a new property to this element's properties. Throws an error if the property already exists.
	public synchronized Property addProperty(Property<?> newProperty) {

		// Is there a relation with this name already in this element?
		if(properties.containsKey(newProperty.getName().value)) {
			System.err.println("Properties are " + properties);
			throw new ElementError("" + this + " already has a property named " + 
								  newProperty.getName() + ", with declaration " + 
								  properties.get(newProperty.getName().value).getDeclaration(), this);
		}
		
		// Put the relation in the properties table
		properties.put(newProperty.getName().value, newProperty);
		
		return newProperty;
		
	}

	// Contructs a set view of this Element's property table's values.
	public Set<Property<?>> getProperties() { return new Set<Property<?>>(properties.values()); }

	// A typesafe, but slower way to access a property. This uses the declaration's type variables to return a type
	// of the appropriate type.
	public <ValueType extends Element> Property<ValueType> getPropertyByDeclaration(DecInterface<ValueType> declaration) {

		// We can't be sure that the property of the given name is of the appropriate type.
		return (Property<ValueType>)getProperty(declaration.getName());
		
	}

	// A typesafe, but slower way to access a property's value, using the declaration's type variables.
	public String text(DecInterface<Text> declaration) { return ((Text)(getProperty(declaration.getName()).get())).value; }
	public char character(DecInterface<Char> declaration) { return ((Char)(getProperty(declaration.getName()).get())).value; }
	public int integer(DecInterface<Int> declaration) { return ((Int)(getProperty(declaration.getName()).get())).value; }
	public boolean bool(DecInterface<Bool> declaration) { return ((Bool)(getProperty(declaration.getName()).get())).value; }
	public double real(DecInterface<Real> declaration) { return ((Real)(getProperty(declaration.getName()).get())).value; }

	public <ValueType extends Element<?>> ValueType get(DecInterface<ValueType> declaration) {
		return (ValueType)getProperty(declaration.getName()).get();		
	}

	public <ValueType extends Element<?>> ValueType peek(DecInterface<ValueType> declaration) {
		return (ValueType)getProperty(declaration.getName()).peek();
		
	}

	// We can't be sure that the property of the given name is of the appropriate type.
	public <ValueType extends Element<?>> boolean set(DecInterface<ValueType> property, ValueType value) { return getPropertyByDeclaration(property).set(value); }
	public <ValueType extends Element<?>> boolean set(DecInterface<ValueType> property, ValueType value, Transition t) { return getPropertyByDeclaration(property).set(value, t); }
	public boolean set(String name, boolean value) { return ((Property)getProperty(new Text(name))).set(new Bool(value)); }
	public boolean set(String name, String value) { return ((Property)getProperty(new Text(name))).set(new Text(value)); }
	
	// Searches the hashtable for a property of the given name.
	public synchronized Property<?> getProperty(Text name) { 
		
		// Does the property table have the property?
		Property p = properties.get(name.value);
		
		// If we found one, return it.
		if(p != null) return p;

		// If we didn't find one, does this element's type declare a property by this name?
		// If so, create, add, and initialize it (not passing a parameterized value).
		DecInterface<?> declaration = namespace.getDeclarationOf(name);
		if(declaration != null)
			return addProperty(declaration.make(this)).initialize(null, false, null);
		
		// Does this element have an enclosing instance? If so, return its property of the given name.
		Element enclosing = getEnclosingInstance();
		if(enclosing != null) return enclosing.getProperty(name);

		// Does this element's type have a static property by this name?
		p = getType().getProperty(name);
		if(p != null) return p;
		
		// Return empty handed.
		return null;

	}

	// Returns true if this property has a property with the given name
	public Bool hasa(Text name) { 
	
		for(DecInterface<?> pd : namespace.getDeclarationsToInstantiate())
			if(pd.getName().equals(name)) return new Bool(true);
		return new Bool(false);
		
	}
	
	public Element<?> getEnclosingInstance() {

		Property<Element> enclosing = (Property<Element>)properties.get(environment.getName().value);
		if(enclosing == null) return null;
		else return enclosing.peek();

	}

	public Bool isEquivalentTo(Element<?> e) { return new Bool(this.equals(e)); }

	public <ElementType extends Element> ElementType getOwnerOfType(Class<ElementType> c) {
		
		return (ElementType)ownerOfType(Reflection.getJavaType(c));
		
	}
	
	public <ElementType extends Element> ElementType getFirstChildOfType(Class<ElementType> c) {
		return (ElementType)getFirstChildOfType(Reflection.getJavaType(c));
	}
	
	public void addView(ElementView newView) {

		/*
		// If there are currently no views for this, make sure to use any residing
		// view state read in from the serialization of this element.
		ViewState viewState = null;
		// If we have view state, find the first view state of the matching type.
		if(viewStateList != null) {

			Iterator<ViewState> viewStates = viewStateList.iterator();
			while(viewStates.hasNext()) {
				ViewState vs = viewStates.next();
				// If this view state is for a view of the appropriate type, remove it 
				// and leave the loop.
				if(vs.getViewTypeName().equals(newView.getType().getName())) {
					viewState = vs;
					viewStates.remove();
					break;
				}
			}
			
			// If we found a view state, use it to update the view.
			if(viewState != null) {

				Hashtable<String,Object> propertiesValues = viewState.getPropertyValueTable();
				for(String propertyName : propertiesValues.keySet()) {

					String valueString = (String)propertiesValues.get(propertyName);
					Property viewProperty = newView.getProperty(propertyName);
					// If we found a property by the given name, convert the value string to a value and set the property.
					if(viewProperty != null) {
						viewProperty.set(viewProperty.valueFromString(valueString));
					} 
					else debug("Warning: " + newView + " didn't have property named " + 
								propertyName + "; this state must be out of date");
					
				}
				
			}
			
			// If there are no more view states, remove the view state property from the table.
			if(viewStateList.isEmpty()) properties.remove("privateViewState");
			
		}
		*/
		
		super.addView(newView);

	}
	
	public void addViewState(ViewState newViewState) { 

		// Does the hash have a view state entry? If not, add one.
		if(viewStateList == null) viewStateList = new LinkedList<ViewState>();
		viewStateList.add(newViewState);
		
	}

	// Set the property that owns this element to the given replacement.
	public final Element replaceWith(Element oldElement, Element newElement, Transition t) {

		if(oldElement == this) {

			Property pOwner = propertyOwner.get();
			if(pOwner == null) System.err.println("Nothing owns " + this);
			else pOwner.set(newElement, t);
			return newElement;		

		}
		else throw new ElementError("" + this + " doesn't know how to replace " + oldElement + " with " + newElement, null);
		
	}	

	// Duplicate this element, using the given arguments.
	public Element duplicate() {

		ArgumentList args = new ArgumentList();
		for(DecInterface<?> p : namespace.getDeclarationsToInstantiate()) {
			if(!p.functionIsConstraint())  {
				args.add(p.getName().value, getProperty(p.getName()).duplicate());
			}
		}
		return namespace.instantiate(args);
		
	}

	public Property getPropertyOwner() { return propertyOwner.get(); }
	protected void setPropertyOwner(Property p) { propertyOwner.set(p); }
	
	// An element is valid if all of its properties are valid.
	public boolean isValid() {
		
		// Validate all of the properties, noting that we don't validate the owner
		// since its not in the properties list.
		for(Property<?> p : properties.values())
			if(!p.isValid()) return false;
		return true;
		
	}
	
	public T evaluate(Element<?> env) { return (T)this; }
	
	//////////////////////////////////////
	//
	// These are convenience methods for getting a property's value. Note that
	// many will throw class cast exceptions if the value isn't of the expected type.
	//
	//////////////////////////////////////

	public static <ValueType extends Element<?>> Element<ValueType> parseExpression(String code) { return CitrusParser.code(code); }
	
	public Element peekAtOwnerOfType(Type t) {
		
		Element owner = this;
		while(owner != null && !owner.getType().isTypeOf(t).value) {
			if(owner instanceof BaseElement) {
				Property pOwner = (Property)((BaseElement)owner).propertyOwner.peek();
				if(pOwner == null) return null;
				owner = pOwner.getElementOwner();
			}
			else if(owner instanceof BootElement)
				owner = ((BootElement)owner).propertyOwner.peek().getElementOwner();
			else throw new ElementError("Can't peek at property owner of " + owner, null);
		}
		return owner;

	}
	
	// Just for debugging purposes
	public final void finalize() {

		if(Debug.elementFinalization()) Debug.print("Finalizing " + this);
		
	}

	public String toString() {
		
		String s = super.toString();
		String hex = s.substring(s.lastIndexOf('@'));
		return "" + namespace.getLanguage() + 
				"." + 
				namespace.getName() + 
				hex;
		
	}
	
	public Text toText() { return new Text(toString()); }

	public Text toCitrus() { return new Text("no syntax for " + getType()); }
	public Text toCitrusReference() { return new Text("No reference syntax for " + getType()); }
	
	public String propertiesToString() {
		
		return properties.toString();
		
	}

	public void propogateListener(Listener listener, boolean add) {

		// Iterate through all properties, propogating the listener
		for(DecInterface dec : namespace.getDeclarationsToInstantiate())
			getProperty(dec.getName()).propogateListener(listener, add);
		
	}
	
	protected void debug(String s) { System.err.println("" + getType() + ": " + s); }
	
}