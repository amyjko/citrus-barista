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
package edu.cmu.hcii.citrus.views;

import edu.cmu.hcii.citrus.*;

//
// @author Andrew J. Ko
//
public class AnimateSetProperty extends AnimationStatement {

	public static final Dec<Property> property = new Dec<Property>();
	public static final Dec<Element> value = new Dec<Element>();
	public static final Dec<Transition> transition = new Dec<Transition>();

	// Runtime state
	private Property<?> propertyAnimating = null;
	
	public AnimateSetProperty() {}
	public AnimateSetProperty(Namespace subType, ArgumentList args) { super(subType, args); }

	public boolean startAnimating(Element context) {

		// Get the property, value, and transition
		Property p = get(property);
		Element v = get(value);
		Transition t = get(transition);
		if(t == null) t = App.getGlobalStyle().getQuickerTransition();
		t.reset();
		p.set(v, t);
		
		// If the property animating is visible, set it.
		propertyAnimating = p;

		return false;
		
	}

	public boolean doneAnimating(Element context) {

		// Is the property done transitioning?
		return propertyAnimating == null || propertyAnimating.isDoneTransitioning();

	}

	public String toString() { 

		return "set " + propertyAnimating.getName();
		
	}
	
}