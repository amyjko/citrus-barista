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

// Represents an instance of an "On" declaration, with
// a property pointing to the subject and a 
public class Observer extends BaseElement<Observer> implements Listener,Namespace<Observer> {
	
	// Reference to its context
	private final Element context;

	// Reference to the On declaration
	private final When when;	
	
	// The subject we're observing, constrained to the ons expression. A reference, not an owner.
	public static final Dec<Element> subject = new Dec<Element>(true, new BaseElement<Element>() {
		public Element evaluate(Element<?> env) {
			return ((Observer)env).when.peek(When.subjectExpression).evaluate(((Observer)env).context);
		}
	}, true);
	
	// The subject we're observing, constrained to the ons expression.
	public static final Dec<Type> event = new Dec<Type>(true, new BaseElement<Type>() {
		public Type evaluate(Element<?> env) {
			return (Type)((Observer)env).when.peek(When.eventExpression).evaluate(((Observer)env).peek(subject));
		}
	}, true);

	public static class SubjectListener implements Listener {
		
		// When the subject changes, add and remove this as a listener.
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {

			oldValue.removeListener((Observer)p.getElementOwner());
			newValue.addListener((Observer)p.getElementOwner());
			
		}

		public void outOfDate(Property p, Transition t, Element oldValue) {}
		public void validityChanged(Property p, Transition t, boolean isValid) {}
		public void cycle(Property p, List cycle) {}
		
	}
	public static final SubjectListener subjectListener = new SubjectListener();
	
	public Observer(Element newContext, When newWhen) {
		
		context = newContext;
		when = newWhen;
		
		// Get the subject property
		Property p = getPropertyByDeclaration(subject);

		// Listen to changes in the subject, so we can add and remove this as a listener
		p.addListener(subjectListener);

		get(subject).addListener(this);
		
	}
	
	public void changed(Property p, Transition t, Element oldValue, Element newValue) {}
	public void outOfDate(Property p, Transition t, Element oldValue) {}
	public void validityChanged(Property p, Transition t, boolean isValid) {}
	public void cycle(Property p, List cycle) {}	

	public String toString() { return "" + context + " " + when; }

	public Element instantiate(ArgumentList args) {

		// Create an instance of this namespace using the given arguments, and
		// return the evaluation of this function's expression in this namespace.
		Element<?> expr = when.peek(When.response);
		args.enclosingEnvironment = context;
		Element<?> env = new BaseElement(this, args);
		Element<?> result = when.peek(When.response).evaluate(env);
		return result;

	}
	
	public Element<?> getExpression() { return when.peek(When.response); }
	
	public Iterable<DecInterface> getDeclarationsToInstantiate() { return when.declarations; }
	public Iterable<DecInterface> getDeclarationsDeclared() { return when.declarations; }
	public String getName() { return when.peek(When.name).value; }
	public Bool isTypeOf(Namespace t) { return new Bool(t == this); }
	public Language getLanguage() { return (Language)when.ownerOfType(Boot.LANGUAGE); }
	public int getNumberOfDeclarations() { return 1; }
	public DecInterface<?> getDeclarationOf(Text name) { 
	
		DecInterface dec = when.declarations.first();
		return dec.getName().value.equals(name.value) ? dec : null;
		
	}
	public Type getTypeNamed(String name) { return when.getTypeNamed(name); }
	public Function getFunctionNamed(Text name, Bool isStatic) { return when.getFunctionNamed(name, isStatic); }
	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) { return when.getDeclarationsInContext(names); }
	public Set<Type> getTypesInContext(Set<Type> names) { return when.getTypesInContext(names); }
	public Set<Function> getFunctionsInContext(Set<Function> functions) { return when.getFunctionsInContext(functions); }
	public boolean isStatic() { return false; }
	
}
