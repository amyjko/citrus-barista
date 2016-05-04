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

import edu.cmu.hcii.citrus.views.Transition;

public abstract class BootElement<T extends Element> extends AbstractElement<T> {

	protected Property<?> propertyOwner = null;
	
	public BootElement() { super(); }
	public BootElement(ArgumentList arguments) {}

	// TYPE
	public Namespace<?> getType() { return Reflection.getJavaType(getClass()); }

	// PROPERTIES
	public Set<Property<?>> getProperties() { return new Set<Property<?>>(); }
	public Property getProperty(Text name) { return null; }
	public Bool hasa(Text name) { System.err.println("Haven't actually implemented hasPropertyNamed() for BootElement."); return new Bool(false); }
	public Element<?> getEnclosingInstance() { return null; }

	// GETTING
	public Element get(Text name) { return null; }
	public <ValueType extends Element<?>> ValueType get(DecInterface<ValueType> declaration) { return null; }

	// SETTING
	public <ValueType extends Element<?>> boolean set(DecInterface<ValueType> dec, ValueType value) {

		throw new ElementError("Nothing to set on a Nothing.", this);		
		
	}

	// EQUALITY
	public Bool isEquivalentTo(Element<?> e) { return this == e ? new Bool(true) : new Bool(false); }
	
	// PREDICATES
	public boolean isValid() { return true; }

	// USERS
	public Element replaceWith(Element oldElement, Element newElement, Transition t) { 
		
		throw new ElementError("BootElement.replaceWith() isn't implemented repaired", null);
				
	}

	public Property getPropertyOwner() { return propertyOwner; }
	protected void setPropertyOwner(Property p) { propertyOwner = p; }
	
	// DUPLICATION
	public Element duplicate() { throw new ElementError("Can't duplicate " + this, null); }

	// TRANSLATION
	public Text toCitrus() { return new Text("No syntax for BootElement"); }
	public Text toText() { return toCitrusReference(); }
	public Text toCitrusReference() { return new Text("No syntax for a reference to " + getType()); }
	public String toString() { return getType().getName(); }

	// EVALUATION
	public T evaluate(Element<?> env) { return (T)this; }

	// VIEWS
	public void addViewState(ViewState viewState) {}

	// TRANSITIONS
	public T getTransitionalValue(Transition t, T start, T end, long time) { return end; }

	// SEARCHING
	public final Element ownerOfType(Namespace t) { 
		
		Element owner = getElementOwner();
		while(owner != null && !(owner.getType().isTypeOf(t).value))
			owner = owner.getElementOwner();
		return owner;
		
	}

	// No properties, no propogation. The only time this is incorrect is when we're
	// bootstrapping things like types; then, we'd like to be able to listen to changes on
	// them, but they have no properties to do so.
	public void propogateListener(Listener listener, boolean add) {}
	
}