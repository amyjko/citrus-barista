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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import edu.cmu.hcii.citrus.views.ElementView;
import edu.cmu.hcii.citrus.views.Transition;
import edu.cmu.hcii.citrus.views.Translator;

// Implements basic element methods that use the property owner, views, users, and searching.
public abstract class AbstractElement<T extends Element> implements Element<T> {

	protected WeakSet<ElementView> views = null;
	protected WeakSet<Property> users = null;	// Properties that refer to this element, but do not own it.

	// Observers to notify about events
    protected WeakSet<Listener> listenersListening;

    // Observers that this element has declared
    protected LinkedList<Listener> listenersDeclared;
    
	// TYPE
	public abstract Namespace<?> getType();
	public Bool isa(Type t) { return getType().isTypeOf(t); }

	// PROPERTIES
	public abstract Set<Property<?>> getProperties();
	public abstract Property getProperty(Text name);
	public abstract Bool hasa(Text name);


	public Element get(String name) { return get(new Text(name)); }
	public Element get(Text name) {

		Property p = getProperty(name);
		if(p != null) {
			if(p.valueIsNothing()) return p.getNothing();
			else return p.get();
		}
		else return null;

	}
	
	// GETTING and SETTING
	public abstract <ValueType extends Element<?>> ValueType get(DecInterface<ValueType> declaration);
	public abstract <ValueType extends Element<?>> boolean set(DecInterface<ValueType> name, ValueType value);

	public Bool set(Text propertyName, Element value) {
		
		return getProperty(propertyName).set(value) ? Bool.TRUE : Bool.FALSE;
		
	}
	
	// EQUALITY
	public abstract Bool isEquivalentTo(Element<?> o);
	public Bool is(Element o) { return new Bool(this == o); }

	// PREDICATES
	public abstract boolean isValid();
	public Bool isNothing() { return Bool.FALSE; }
	public Bool isSomething() { return Bool.TRUE; }

	// OWNERS AND USERS
	public void registerProperty(Property<?> p) {

		// If the property may own this and we don't have an owner, then make this the owner.
		if(!p.getDeclaration().isReferenceOnly() && getPropertyOwner() == null) {
			if(p.owner == this) throw new ElementError("Creating ownership cycle!", null);// " + this + " owns " + p + "!", this);
			setPropertyOwner(p);
		}
		// Otherwise, add it as a user.
		else {
			if(users == null) users = new WeakSet<Property>(p); 
			else users.include(p);
		}

	}
	
	public void unregisterProperty(Property<?> p) {
		
		// If this property was the owner, nullify the owner.
		if(getPropertyOwner() == p) {
			setPropertyOwner(null);
		}
		// Otherwise, remove it as a user.
		else if(users != null) users.exclude(p);

	}

	protected abstract void setPropertyOwner(Property p);

	public Element<?> getElementOwner() { 
		
		Property p = getPropertyOwner();
		return p == null ? null : p.owner;

	}

	public final Iterable<Property> getUsers() { 
		if(users == null) return new java.util.Vector<Property>(0);
		else return users; 
	}

	public abstract Element replaceWith(Element oldElement, Element newElement, Transition t);

	// The normal way to disown is to set the property owner of e to nothing.
	public Bool disown(Element<?> e, Transition t) {

		// We only disown ourselves
		if(this == e) {
			return new Bool(e.getPropertyOwner().set(new Nothing(), t));
		}
		else return new Bool(false);
		
	}
	// Ask property owner to disown
	public Bool emancipate() { 
	
		Property<?> propertyOwner = getPropertyOwner();
		if(propertyOwner == null) return new Bool(false);
		else return propertyOwner.disown(this, null);
		
	}
	public Bool emancipateOverTime(Transition t) {
		
		return getPropertyOwner().disown(this, t);
		
	}
	
	// VIEWS
	public abstract void addViewState(ViewState viewState);

	public void addView(ElementView newView) { 
		
		if(views == null) views = new WeakSet<ElementView>(newView);
		else views.include(newView);
		
	}	

	public void removeView(ElementView oldView) { if(views != null) views.exclude(oldView); }

	public final ElementView getFirstView() { 
		if(views == null) return null;
		else return views.getElement();
	}
	public final Iterable<ElementView> getViews() {
		if(views == null) return new java.util.Vector<ElementView>(0);
		else return views;		
	}
	
	public ElementView toView() { return Translator.toView(null, false, null, this); }


    // Add a property listener, if it's not already added.
    public boolean addListener(Listener newListener) {

		boolean changed = false;
    		if(listenersListening == null) { 
    			listenersListening = new WeakSet<Listener>(newListener);
    		}
    		else changed = listenersListening.include(newListener);
    		return changed;
    		
    }
    
    // Remove a property listener, if there's a list at all.
    public boolean removeListener(Listener listener) {
    	
    		boolean changed = false;
    		// If there are no listeners, return.
    		if(listenersListening == null) changed = false;
    		// If the first element is it, set the new first node to the next node.
    		else if(listenersListening.getElement() == listener) { listenersListening = listenersListening.getNext(); changed = true; }
    		// Otherwise, search for the element to remove.
    		else changed = listenersListening.exclude(listener);    		
    		return changed;
    		
    }
    
    public boolean notifyListenersOf(Type eventType, Element ... arguments) {
    	
    		if(listenersListening == null) return false;
    		boolean handled = false;
    		for(Listener listener : listenersListening) {

    			if(listener instanceof Observer && ((Observer)listener).get(Observer.event).isTypeOf(eventType).value) {
    				handled = true;
    				ArgumentList eventArgs = new ArgumentList();
    				eventArgs.enclosingEnvironment = this;
    				int i = 0;
    				for(DecInterface dec : eventType.getDeclarationsDeclared()) {
    					eventArgs.add(dec.getName().value, arguments[i]);
    					i++;
    				}
    				Element event = eventType.instantiate(eventArgs);    				
    				
    				ArgumentList args = new ArgumentList();
    				args.add(((Observer)listener).getName(), event);
    				((Observer)listener).instantiate(args);
    				
    			}

    		}
    		return handled;
    	
    }

	//////////////////////////
	//
	// Duplication
	//
	public abstract Element duplicate();

	//////////////////////////
	//
	// EVALUATION
	//
	public abstract T evaluate(Element<?> env);
	
	// By default, an element evaluates to itself, so the resulting type is its type.
	public Type resultingType() { return (Type)getType(); }
	
	// By default, an element doesn't evaluate anything, so it shouldn't be providing any context to anything.
	public Context contextFor(Element e) { return null; }	

	//////////////////////////
	//
	// Transitions
	//
	public T getTransitionalValue(Transition t, T start, T end, long time) { return end; }
	
	//////////////////////////
	//
	// Searching
	//
	public Element ownerOfType(Namespace type) {

		Element owner = getElementOwner();
		while(owner != null && !(owner.getType().isTypeOf(type).value)) {
			owner = owner.getElementOwner();
			if(owner == this) {
				String str = "" + this + " owns\n";
				owner = this.getElementOwner();
				while(owner != this) { str = str + "" + owner + " owns\n"; owner = owner.getElementOwner();  }
				throw new ElementError("Cycle in ownership!\n", null);
			}
		}
		return owner;

	}
	
	// Returns the lowest common ancestor for this view and the given view v,
	// if one exists.
	public Element commonAncestorWith(Element e) {
		
		// Is this an ancestor of e? If so, return this.
		if(this.owns(e).value) return this;
		
		// Is e an ancestor of this? If so, return v.
		if(((AbstractElement)e).owns(this).value) return e;
		
		// Otherwise, find the common ancestor.
		Element o = getElementOwner();
		while(o != null) {
			// Is this ancestor of this view also an ancestor of v?
			if(((AbstractElement)o).owns(e).value) return o;
			o = o.getElementOwner();
		}
		return null;
		
	}
	
	public Bool owns(Element e) {

		Element o = e.getElementOwner();
		while(o != null) {
			if(o == this) return Bool.TRUE;
			o = o.getElementOwner();
		}
		return Bool.FALSE;

	}

	
	// A breadth-first search for an element of type t;
	public Element getFirstChildOfType(Namespace t) {

		// Go through all of the element pointer properties to find elements of the given type.
		Element child = null;
		for(DecInterface<?> pd : getType().getDeclarationsToInstantiate()) {

			Property<?> p = getProperty(pd.getName());
			if(p != null && !p.isReference()) {
				Element value = p.get();
				if(value != null) {
					if(value.getType().isTypeOf(t).value) child = value;
					else child = value.getFirstChildOfType(t);
				}
			}

			if(child != null) return child;
			
		}
		return null;
		
	}

	//////////////////////////
	//
	// Translation
	//
	public abstract Text toCitrus();

	public Text toXML() {

		Text typeReference = getType().toCitrusReference();
		
		// Write type tag
		String s = "<" + typeReference + ">\n";

		// Write all of the properties, ignoring those that haven't been requested yet.
		for(Property<?> p : getProperties()) {
			if(p.getDeclaration() != BaseElement.environment &&
			   p.getValueFunction() == null && !p.getDeclaration().isReferenceOnly())
			s = "" + s + p.toXML() + "\n";
		}
		
		// Close type tag
		s = s + "</" + typeReference + ">";
		
		return new Text(s);
		
	}
	
	private Set<Text> gatherLanguagesUsed(Set<Text> languagesUsed) {

		// What language does this use?
		languagesUsed.add(((Unit)getType().ownerOfType(Boot.LANGUAGE)).get(Unit.language));
		System.err.println("Languages used is " + languagesUsed);
		for(Property<?> p : getProperties())
			if(!p.isReference())
				((AbstractElement<?>)p.get()).gatherLanguagesUsed(languagesUsed);
		return languagesUsed;
		
	}

	public Bool writeXMLTo(Text path) {
		
		Set<Text> languagesUsed = gatherLanguagesUsed(new Set<Text>());

		Text xml = toXML();
		String fileWrapper = "<File>\n";
		for(Text language : languagesUsed)
			fileWrapper = fileWrapper + "<uses>" + language + "</uses>\n";
		fileWrapper = fileWrapper + "</File>\n";

		xml = new Text(fileWrapper + xml);

		try {
			FileWriter writer = new FileWriter(new File(path.value));
			writer.write(xml.value);
			writer.close();
			return new Bool(true);
		}
		catch(IOException e) {
			System.err.println("Couldn't write to " + path + ": " + e);
			return new Bool(false);
		}
		
	}
	
}