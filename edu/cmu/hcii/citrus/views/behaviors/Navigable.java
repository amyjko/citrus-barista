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
package edu.cmu.hcii.citrus.views.behaviors;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.devices.*;

public class Navigable extends Behavior {
	
	public Navigable() { super(); }
	public Navigable(Namespace t, ArgumentList args) { super(t, args); }
		
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
	
		new Behavior(App.mouse.leftButton.pressed, false, new BaseElement<Bool>() { public Bool evaluate(Element<?> t) {
			View v = ((View)t).focusableVisibleViewClosestTo(App.mouse.pointer.get(MousePointer.left), App.mouse.pointer.get(MousePointer.top));
			((View)t).getWindow().focusOn(v, "mouse");
			return Bool.TRUE;
		}}),
		new Behavior(new Keyboard.Pressed("left"), false, new BaseElement<Bool>() { public Bool evaluate(Element<?> t) {			
			return focus((View)t, Bool.FALSE);
		}}),
		new Behavior(new Keyboard.Pressed("right"), false, new BaseElement<Bool>() { public Bool evaluate(Element<?> t) {
			return focus((View)t, Bool.TRUE);			
		}}),
		new Behavior(new Keyboard.Typed('\t'), false,new BaseElement<Bool>() { public Bool evaluate(Element<?> t) {
			return focus((View)t, Bool.TRUE);
		}}),
		new Behavior(new Keyboard.Typed(null, 0, null, new Char('\t'), new Bool(true), null, null, null), false,new BaseElement<Bool>() { public Bool evaluate(Element<?> t) {
			return focus((View)t, Bool.FALSE);
		}}),
		new Behavior(new Keyboard.Typed(' '), false,new BaseElement<Bool>() { public Bool evaluate(Element<?> t) {
			return focus((View)t, Bool.TRUE);			
		}}),
		new Behavior(App.keyboard.DOWN.pressed, false, App.keyboard.focusBelow),
		new Behavior(App.keyboard.UP.pressed, false, App.keyboard.focusAbove)

	));

	public static Bool focus(View view, Bool direction) {
		
		// Find the child of this view whose descendant has focus.
		View childWithFocus = view.childWithFocus();
		
		// If none of this view's children have the focus (how is this possible?), don't handle it.
		if(childWithFocus == null) {
			return new Bool(false);
		}

		// Get the next focusable sibling of the child with the focus.
		View newFocus = childWithFocus.focusableSibling(direction);
		
		// If there isn't one, don't handle it.
		if(newFocus == null) return Bool.FALSE;
		// Otherwise, focus on it.
		else {
			
			view.getWindow().focusOn(newFocus, direction.value ? "next" : "previous");
			return Bool.TRUE;

		}
		
	}
			
}