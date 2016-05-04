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

public class Moveable extends Behavior {
	
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
	
		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
			App.mouse.pointer.pick.evaluate(t);
			return true;
		}}),

		new Behavior(App.mouse.pointer.dragged, new Action() { public boolean evaluate(View t) {
			if(App.mouse.pointer.isPicked(t)) {
				java.awt.geom.Point2D p = App.mouse.pointer.positionRelativeToPointPicked();
				t.set(View.left, new Real(p.getX()));
				t.set(View.top, new Real(p.getY()));
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
			t.addBackgroundPaint(App.getGlobalStyle().getDragShadow());
			return true;
		}}),

		new Behavior(App.mouse.pointer.released, new Action() { public boolean evaluate(View t) {
			t.set(View.hoisted, new Bool(false));
			t.removeBackgroundPaint(App.getGlobalStyle().getDragShadow());
			return true;
		}})

	));

}