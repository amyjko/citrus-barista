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

public class Draggable extends Behavior {

	public static final Dec<Element<?>> onFailure = new Dec<Element<?>>();
	
	public Draggable() { super(); }
	public Draggable(Namespace t, ArgumentList args) { super(t, args); }
	
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
	
		new Behavior(App.focusReceived, App.keyboard.addFocusPaint),
		new Behavior(App.focusLost, App.keyboard.removeFocusPaint),	
		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
//			if(t.bool(View.focusable)) {
//				t.requestKeyboardFocus("next");
				App.mouse.pointer.pickAndHoist.evaluate(t);
				return true;
//			} else return false;
		}}),
		new Behavior(App.mouse.pointer.dragged, new Action() { public boolean evaluate(View t) {
			if(App.mouse.pointer.isPicked(t)) {
				t.addBackgroundPaint(App.getGlobalStyle().getDragShadow());
				t.makeVisible();
				t.getWindow().setCursorTo(Mouse.MOVE_CURSOR);
				t.set(View.transparency, new Real(.6));
				java.awt.geom.Point2D p = App.mouse.pointer.positionRelativeToPointPicked();
				t.set(View.xOrigin, new Real(-p.getX() + t.get(View.left).value));
				t.set(View.yOrigin, new Real(-p.getY() + t.get(View.top).value));
				return true;
			}
			else return false;
		}}),
		new Behavior(App.mouse.leftButton.released, new Action() { public boolean evaluate(View t) {
				if(App.mouse.pointer.getViewPicked() == t) {
					App.mouse.pointer.release.evaluate(t);
					return true;
				} else return false;
		}}),
		new Behavior(App.mouse.pointer.picked, new Action() { public boolean evaluate(View t) {
				return true;
			}}
		),
		new Behavior(App.mouse.pointer.released, new Action() { public boolean evaluate(View t) {
				t.removeBackgroundPaint(App.getGlobalStyle().getDragShadow());
				return true;
			}}		
		),
		new Behavior(App.mouse.pointer.dropSucceeded, new Action() { public boolean evaluate(View t) {
			dropAnimation.evaluate(t).evaluate(t);
			return false;
		}}),
		new Behavior(App.mouse.pointer.dropFailed, new Action() { public boolean evaluate(View t) {
//			Element<?> responseToFailure = Draggable.this.get(onFailure);
//			if(responseToFailure == null)
				dropAnimation.evaluate(t).evaluate(t);
//			else responseToFailure.evaluate(null);
			return false;
		}})

	));
	// Set the origins and transparencies back, then unhoist.
	private static Element dropAnimation = parseExpression(
		"(an AnimateInOrder [" +
			"(an AnimateTogether [" + 
				"(an AnimateSetProperty property=@xOrigin value=0.0)" +
				"(an AnimateSetProperty property=@transparency value=1.0)" +
				"(an AnimateSetProperty property=@yOrigin value=0.0)" +
			"])" +
			"(an AnimateSetProperty property=@hoisted value=false)" +
		"])"
	);
		
}