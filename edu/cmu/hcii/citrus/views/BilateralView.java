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
import edu.cmu.hcii.citrus.views.devices.Keyboard;
import edu.cmu.hcii.citrus.views.devices.MousePointer;

public class BilateralView extends ElementView {

	public static final boolean debugNavigation = false;
	public static void debugNavigation(String message) { if(debugNavigation) System.err.println(message); }
	
	public BilateralView(Namespace subtype, ArgumentList arguments) { super(subtype, arguments); }
	
	public Bool replaceWithPropertyNamed(Text propertyName) {

		return new Bool(true);
		
	}	
	
	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(true));

	public static final Dec<Bool> onTheLeft = new Dec<Bool>(new Bool(true));
	static { onTheLeft.set(Dec.isUndoable, Bool.TRUE); }

	public static final Dec<Paint> caretPaint = new Dec<Paint>(
		"(a LinePaint " +
			"stroke=1.0" +
			"primaryColor=(a Color r=0.0 g=0.0 b=0.0) " +
			"x1<-'(if onTheLeft 0.0 1.0) " +
			"y1=0.0 " +
			"x2<-'(if onTheLeft 0.0 1.0) " +
			"y2=1.0" +
		")"
	);

	public static final Dec<Paint> focusPaint = new Dec<Paint>("(this getStyle).listFocusPaint");

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.keyboard.DOWN.pressed, false, App.keyboard.focusBelow),
		new Behavior(App.keyboard.UP.pressed, false, App.keyboard.focusAbove),
		// When focus is received, check the last focus and position the cursor
		// so that it's optimally placed for navigation.
		new Behavior(App.focusReceived, false, new Action() { public boolean evaluate(View t) {

			BilateralView tf = (BilateralView)t;

			if(!tf.get(View.foreground).contains(t.get(caretPaint)).value)
				tf.get(View.foreground).append(t.get(caretPaint));

			if(!tf.get(View.background).contains(t.get(focusPaint)).value) {
				tf.get(View.background).append(t.get(focusPaint));
			}

			String direction = t.getWindow().getDirectionOfLastFocusMovement();
			if(direction.equals("next")) t.set(onTheLeft, new Bool(true));
			else if(direction.equals("previous")) t.set(onTheLeft, new Bool(false));
			else if(direction.equals("mouse")) {
				return true;				
			}

			return true;
		
		}}),
		new Behavior(App.focusLost, false, new Action() { public boolean evaluate(View t) {
			t.get(t.foreground).remove(t.get(caretPaint));
			t.get(t.background).remove(t.get(focusPaint));
			return true;
		}}),
		// When this is clicked, focus, and place the cursor in the appropriate position.
		new Behavior(App.mouse.leftButton.pressed, false, new Action() { public boolean evaluate(View t) {

			View v = t.focusableVisibleViewClosestTo(App.mouse.pointer.get(MousePointer.left), App.mouse.pointer.get(MousePointer.top));
			t.getWindow().focusOn(v, "mouse");
			return true;
			
		}}),
		new Behavior(new Keyboard.Typed('\t'), "(this focusOnFocusableItem true)"),
		new Behavior(new Keyboard.Typed(' '), "(this focusOnFocusableItem true)"),
		new Behavior(new App.ChildReceivedFocus(), "(@onTheLeft set false)"),
		new Behavior(App.keyboard.RIGHT.pressed, "(this focusOnFocusableItem true)"),
		new Behavior(App.keyboard.LEFT.pressed, "(this focusOnFocusableItem false)"),
		new Behavior(new App.BlinkCaret(), "(caretPaint.@alpha set (if (this getWindow).paintCaret 1.0 0.0))")
		
	));

	// True = right, false=left.
	public Bool focusOnFocusableItem(Bool direction) {

		// Get the child with focus to help us determine what to focus on
		View childWithFocus = childWithFocus();

		// If this has focus...
		if(hasKeyboardFocus().value) {
			// On the left
			if(bool(onTheLeft)) {
				// And we're moving to the right (next)
				if(direction.value) {
					debugNavigation("Moving from the left to the next focusable child");
					View focusableView = firstFocusableChild(Bool.TRUE);
					if(focusableView == null) return new Bool(false);
					getWindow().focusOn(focusableView, direction.value ? "next" : "previous");
					return new Bool(true);
				}
				// If we're moving to the left, don't handle this
				return new Bool(false);				
			}
			// On the right
			else {
				// And we're moving to the right
				if(direction.value) return new Bool(false);
				// And we're moving to the left
				else {
					debugNavigation("Moving from the right to the previous focusable child");
					View focusableView = firstFocusableChild(Bool.FALSE);
					if(focusableView == null) return new Bool(false);
					getWindow().focusOn(focusableView, direction.value ? "next" : "previous");
					return new Bool(true);	
				}
			}
		}		
		// If this doesn't have focus...
		else {
			// Moving to the right, give this focus on the left
			if(direction.value) {
				if(childWithFocus == null) {
					getWindow().focusOn(this, "internal");
					debugNavigation("Moving right from outside to the left");
					set(onTheLeft, new Bool(true));
				}
				else {
					// Does the child with focus have a focusable sibling?
					View focusableSibling = childWithFocus.focusableSibling(Bool.TRUE);
					if(focusableSibling != null) {
						getWindow().focusOn(focusableSibling, "next");
						debugNavigation("Moving to the next focusable sibling");						
					}
					else {
						getWindow().focusOn(this, "internal");
						debugNavigation("Moving right from inside to the right");
						set(onTheLeft, new Bool(false));
					}
				}
			}
			// Moving to the right, give this focus on the right
			else {
				if(childWithFocus == null) {
					getWindow().focusOn(this, "internal");
					debugNavigation("Moving left from outside to right.");
					set(onTheLeft, new Bool(false));
				}
				else {
					// Does the child with focus have a focusable sibling?
					View focusableSibling = childWithFocus.focusableSibling(Bool.FALSE);
					if(focusableSibling != null) {
						getWindow().focusOn(focusableSibling, "previous");
						debugNavigation("Moving to the previous focusable sibling");
					}
					else {
						getWindow().focusOn(this, "internal");
						debugNavigation("Moving left from inside to left.");
						set(onTheLeft, new Bool(true));
					}					
				}
			}
			return new Bool(true);
		}
		
	}

}