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
package edu.cmu.hcii.citrus.views.widgets;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

////////////////////////////////////////////
//
// A widget with an arbitrary label and an
// associated action. The label is the only child.
// Performs an action when clicked.
//
////////////////////////////////////////////
public class MenuItem extends View {

	public static final Dec<Real> width = new Dec<Real>("(this parentsWidth)");
	public static final Dec<Real> height = new Dec<Real>("(this tallestChildsHeight)");
	public static final Dec<Real> vPad = new Dec<Real>("5.0");
	public static final Dec<Real> hPad = new Dec<Real>("5.0");
	public static final Dec<List<Paint>> background = new Dec<List<Paint>>("(this getStyle).menuItemPaint)");
	public static final Dec<Expression> action = new Dec<Expression>(new Parameter<Expression>());
	
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.mouse.pointer.entered, new Action() { public boolean evaluate(View t) {
			t.removeBackgroundPaint(App.getGlobalStyle().getMenuItemPaint());
			t.addBackgroundPaint(App.getGlobalStyle().getOverMenuItemPaint()); 
			return true;
		}}),
		new Behavior(App.mouse.pointer.exited, new Action() { public boolean evaluate(View t) {
			t.removeBackgroundPaint(App.getGlobalStyle().getOverMenuItemPaint());
			t.addBackgroundPaint(App.getGlobalStyle().getMenuItemPaint()); 
			return true;
		}}),
		new Behavior(App.mouse.leftButton.released, new Action() { public boolean evaluate(View t) {
			if(t.get(action) != null) t.get(action).evaluate(t);
			return false; 
		}})
	));
	
	private Object value;

	public MenuItem(View newLabel, Object value, Action newAction) {
		
		super();

		this.value = value;

		addChild(newLabel);
		newLabel.moveTo(0, 0);
		
	}
	
	public Object getValue() { return value; }

}