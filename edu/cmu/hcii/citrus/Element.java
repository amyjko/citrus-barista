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

import edu.cmu.hcii.citrus.views.ElementView;
import edu.cmu.hcii.citrus.views.Transition;

public interface Element<T extends Element> {

	// TYPE
	public Namespace<?> getType();
	public Bool isa(Type t);

	// PROPERTIES
	public Set<Property<?>> getProperties();
	public Property getProperty(Text name);
	public Bool hasa(Text name);
	public Element<?> getEnclosingInstance();
	
	// GETTING and SETTING
	public Element get(Text name);
	public <ValueType extends Element<?>> ValueType get(DecInterface<ValueType> declaration);
	public <ValueType extends Element<?>> boolean set(DecInterface<ValueType> name, ValueType value);
	public Bool set(Text propertyName, Element value);

	// EQUALITY
	public Bool isEquivalentTo(Element<?> o);		// Some notion of structural equivalence
	public Bool is(Element e);				// Same actual object (like lisp's eq)

	// PREDICATES
	public boolean isValid();
	public Bool isNothing();
	public Bool isSomething();

	// OWNERS AND USERS
	public void registerProperty(Property<?> p);	
	public void unregisterProperty(Property<?> p);
	
	public Property getPropertyOwner();
	public Element<?> getElementOwner();

	public Iterable<Property> getUsers();

	// OWNERSHIP CHANGES
	public Element replaceWith(Element oldElement, Element newElement, Transition t);
	public Bool disown(Element<?> e, Transition t);
	public Bool emancipate();
	public Bool emancipateOverTime(Transition t);

	// VIEWS
	public void addViewState(ViewState viewState);

	public void addView(ElementView newView);
	public void removeView(ElementView oldView);

	public ElementView getFirstView();
	public Iterable<ElementView> getViews();
	
	// DUPLICATION
	public Element duplicate();

	// TRANSLATION
	
	// Should return Citrus source could that, when evaluated, would result in something equivalent to this instance.
	public Text toCitrus();
	
	public Text toText();

	// Should return a text string that, when resolved, would result in an equivalent reference.
	public Text toCitrusReference();
	
	public Text toXML();
	
	public String toString();
	
	
	// EVALUATION
	public T evaluate(Element<?> env);
	public Type resultingType();
	public Context contextFor(Element e);
	
	// TRANSITIONS
	public T getTransitionalValue(Transition t, T start, T end, long time);
	
	// SEARCHING
	public Element ownerOfType(Namespace type);
	public Element getFirstChildOfType(Namespace type);

	// LISTENING
	public void propogateListener(Listener listener, boolean add);
	
    public boolean addListener(Listener newListener);
    public boolean removeListener(Listener listener);
    public boolean notifyListenersOf(Type eventType, Element ... arguments);

	
}