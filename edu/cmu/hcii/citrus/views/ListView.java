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

public class ListView extends GroupView {
	
	public static final boolean debugNavigation = false;
	public static void debugNavigation(String message) { System.err.println(message); }

	public static final Dec<List<?>> model = new Dec<List<?>>((Element)null, true);

	public ListView(ArgumentList arguments) { this(null, arguments); }
	public ListView(Namespace subtype, ArgumentList arguments) { super(subtype, arguments); updatedViewFor(null, get(model), null); }

	public Bool insertAndFocusOn(Int index, Element elementToInsert) {

		return insertAndFocusOnOverTime(index, elementToInsert, null);		
	
	}
	
	public Bool insertAndFocusOnOverTime(Int index, Element elementToInsert, Transition t) {

		((List)get(model)).insertAfterIndexOverTime(index, elementToInsert, t);
		View firstView = elementToInsert.getFirstView();
		if(firstView == null) return new Bool(true);
		View firstFocusable = firstView.getDeepestFocusableAncestor(true);
		if(firstFocusable == null) return new Bool(true);
		firstFocusable.requestKeyboardFocus("previous");
		return new Bool(true);
		
	}

	public Bool insertAndFocusOnProperty(Int index, Element elementToInsert, Text propertyName) {

		// Before or after is determined by the the bilateral view's state
		((List)get(model)).insertAfterIndex(index, elementToInsert);
		Property p = elementToInsert.getProperty(propertyName);
		if(p == null) return new Bool(true);
		View firstView = p.getFirstView();
		if(firstView == null) return new Bool(true);
		View firstFocusable = firstView.getDeepestFocusableAncestor(true);
		if(firstFocusable == null) return new Bool(true);
		firstFocusable.requestKeyboardFocus("previous");
		return new Bool(true);
		
	}

	public Bool removeItemAtAndFocusOnPrevious(Int index) {
		
//		set(caretIndex, get(caretIndex).minus(new Int(1)));
		View viewToFocusOn = get(children).itemAt(index.minus(new Int(1)));
		if(viewToFocusOn == null) get(children).first();
		if(viewToFocusOn != null) {
			View firstFocusable = viewToFocusOn.getDeepestFocusableAncestor(false);
			if(firstFocusable != null)
				firstFocusable.requestKeyboardFocus("previous");
		}
		Bool result = ((List)get(model)).removeItemAt(index);
		if(viewToFocusOn == null) requestKeyboardFocus("previous");
		return result;

	}
	
//	// The meaning of the index = the index of the item that new items are inserted after.
//	// 0 indicates inserting before the first item.
//	// 1 indicates inserting after the first item.
//	// length indicates inserting after the last item.
//	public static final Dec<Int> caretIndex = new Dec<Int>(new Int(1));
//	static {
//		caretIndex.is(new Boundary(CitrusParser.parse("(model length)"), true));
//		caretIndex.is(new Boundary(new Int(0), false));
//	}
//	
//	public static final Dec<Bool> vertical = new Dec<Bool>(new Bool(true));
//
//	public static final Dec<Paint> caretPaint = new Dec<Paint>(
//			"(a LinePaint " +
//				"stroke=1.0" +
//				"primaryColor=(a Color r=0.0 g=0.0 b=0.0) " +
//				"x1<-'(enclosing caretX)" +
//				"y1<-'(enclosing caretY1) " +
//				"x2<-'x1 " +
//				"y2<-'(enclosing caretY2)" +
//			")"
//		);
//
//	public Real caretX() {
//
//		List<?> list = get(model);
//		Int index = get(caretIndex);
//		Real xPosition = null;
//		if(list.isEmpty().value) return new Real(0.0);
//		else if(index.value == 0) xPosition = get(children).first().get(left);
//		else { 
//			if(get(vertical).value)
//				xPosition = get(children).itemAt(index).get(right);
//			else
//				xPosition = get(children).itemAt(index).get(left).plus(get(children).itemAt(index.plus(new Int(1))).get(right)).divide(new Real(2.0));
//		}
//		
//		return xPosition.divide(get(width));
//		
//	}
//	
//	public Real caretY1() {
//		
//		List<?> list = get(model);
//		Int index = get(caretIndex);
//		Real yPosition = null;
//		if(get(vertical).value) {
//			if(list.isEmpty().value) yPosition = new Real(0.0);
//			else if(index.value == 0) yPosition = get(children).first().get(top);
//			else yPosition = get(children).itemAt(index).get(top);
//		}
//		else yPosition = new Real(0.0);
//
//		return yPosition.minus(new Real(2.0)).divide(get(height));
//		
//	}
//	
//	public Real caretY2() {
//		
//		List<?> list = get(model);
//		Int index = get(caretIndex);
//		Real yPosition = null;
//		if(get(vertical).value) {			
//			if(list.isEmpty().value) yPosition = get(height);
//			else if(index.value == 0) yPosition = get(children).first().get(bottom);
//			else yPosition = get(children).itemAt(index).get(bottom);
//		}
//		else yPosition = get(height);
//		
//		return yPosition.plus(new Real(2.0)).divide(get(height));
//
//	}
//
//	public Bool focusOnFocusableItem(Bool direction) {
//
//		// If this has focus, let someone else get focus.
//		if(hasKeyboardFocus().value) return new Bool(false);
//		
//		// Otherwise, use the current index and the list to determine what to focus on.
//		View childWithFocus = childWithFocus();
//		if(childWithFocus == null) return Bool.FALSE;
//		Int indexWithFocus = get(children).indexOf(childWithFocus);
//		if(indexWithFocus.value > 0) set(caretIndex, indexWithFocus); 
//		else set(caretIndex, new Int(0));
//		
//		View focusableView = null;
//		Int newIndex = null;
//		
//		// If there are no children, give this focus so some can be added
//		if(get(children).isEmpty().value) {
//			focusableView = this;
//			newIndex = new Int(0);			
//		}
//		// If we're in the middle somewhere, find the focusable sibling of the current index.
//		else {
//			focusableView = childWithFocus.focusableSibling(direction);
//			newIndex = get(children).indexOf(focusableView);
//		}
//		
//		// If there is no focusable view, don't handle this.
//		if(focusableView == null) return new Bool(false);
//		// Focus on the view!
//		getWindow().focusOn(focusableView, direction.value ? "next" : "previous");
//		set(caretIndex, newIndex);		
//		return new Bool(true);
//				
//	}
//
//	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
//		new Behavior(App.keyboard.DOWN.pressed, App.keyboard.focusBelow),
//		new Behavior(App.keyboard.UP.pressed, App.keyboard.focusAbove),
//		new Insertable(),
//		// When focus is received, check the last focus and position the cursor
//		// so that it's optimally placed for navigation.
//		new Behavior(new App.ChildReceivedFocus(), new Action() { public boolean evaluate(View t) {
//			
//			View focus = t.getWindow().getFocus();
//			while(focus.getParent() != t) focus = focus.getParent();
//			Int newIndex = t.get(children).indexOf(focus);
//			t.set(caretIndex, newIndex);
//			return true;
//		
//		}}),
//		new Behavior(App.focusReceived, new Action() { public boolean evaluate(View t) {
//			
//			ListView tf = (ListView)t;
//
//			String direction = t.getWindow().getDirectionOfLastFocusMovement();
//			View newFocus = null;
//			Int newIndex = null;
//
//			if(t.get(children).isEmpty().value) {
//				if(!tf.get(foreground).contains(t.get(caretPaint)).value)
//					tf.get(foreground).append(t.get(caretPaint));
//				t.set(caretIndex, new Int(0));
//				return true;
//			}
//			else if(direction.equals("mouse")) {
//
//				return true;
//				
//			}
//			if(direction.equals("next")) {
//				newFocus = t.get(children).first().thisOrFirstFocusableChild(Bool.TRUE);
//				newIndex = new Int(1);
//			}
//			else if(direction.equals("previous")) {
//				newFocus = t.get(children).last().thisOrFirstFocusableChild(Bool.FALSE);
//				newIndex = t.get(children).length().minus(new Int(1));				
//			}
//			if(newFocus == null) return false;
//			newFocus.requestKeyboardFocus(direction);
//			t.set(caretIndex, newIndex);
//			return true;
//			
//		}}),
//		new Behavior(App.focusLost, new Action() { public boolean evaluate(View t) {
//			t.get(foreground).remove(t.get(caretPaint));
//			t.get(background).remove(t.get(focusPaint));
//			return true;
//		}}),
//		new Behavior(new App.BlinkCaret(), "(caretPaint.@alpha set (if (this getWindow).paintCaret 1.0 0.0))"),
//		// When this is clicked, focus, and place the cursor in the appropriate position.
//		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
//
//			View v = t.focusableVisibleViewClosestTo(App.mouse.pointer.get(MousePointer.left), App.mouse.pointer.get(MousePointer.top));
//			t.getWindow().focusOn(v, "mouse");
//			return true;
//		}}),
//		
//		new Behavior(new App.BlinkCaret(), "(caretPaint.@alpha set (if (this getWindow).paintCaret 1.0 0.0))"),
//		new Behavior(App.keyboard.RIGHT.pressed, "(this focusOnFocusableItem true)"),
//		new Behavior(new Keyboard.Typed('\t'), "(this focusOnFocusableItem true)"),
//		new Behavior(new Keyboard.Typed(' '), "(this focusOnFocusableItem true)"),
//		new Behavior(App.keyboard.LEFT.pressed, "(this focusOnFocusableItem false)")
//		
//	));
	
}