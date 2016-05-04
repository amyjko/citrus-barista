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

public class Replaceable extends Behavior {
	
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
	
		new Behavior(App.mouse.pointer.draggedOver, new Action() { public boolean evaluate(View t) {
			return true;
		}}		
		),
		new Behavior(App.mouse.pointer.draggedIn, new Action() { public boolean evaluate(View t) {

			if(!(t instanceof ElementView)) throw new ElementError("Only ElementViews can be replaceable", this);
			Element<?> elementOver = ((ElementView)t).getModel();
			View pick = App.mouse.pointer.getViewPicked();
			if(!(pick instanceof ElementView)) return false;
			Element<?> elementPicked = ((ElementView)pick).getModel();
			if(isCompatible(elementPicked, elementOver).value) {
				t.addForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
				return true;

			}
			else return false;
		}}),
		new Behavior(App.mouse.pointer.draggedOut, new Action() { public boolean evaluate(View t) {
			t.removeForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
			return true;
		}}),
		new Behavior(App.mouse.pointer.droppedOver, new Action() { public boolean evaluate(View t) {

				Window window = t.getWindow();
				if(!(t instanceof ElementView)) throw new ElementError("Only ElementViews can be replaceable", this);
				Element<?> elementOver = ((ElementView)t).getModel();
				View pick = App.mouse.pointer.getViewPicked();
				if(!(pick instanceof ElementView)) return false;
				Element<?> elementPicked = ((ElementView)pick).getModel();
				if(isCompatible(elementPicked, elementOver).value) {
					
					t.removeForegroundPaint(App.getGlobalStyle().getReplaceFeedback());		

					Property p = ((ElementView)t).getModel().getPropertyOwner();

					// Free the element picked (implicitly freeing its view)
					elementPicked.replaceWith(null, new Nothing(), App.getGlobalStyle().getQuickTransition());

					// Set the property to the element picked, now that it's not owned.
					p.set(elementPicked);

					return true;
				}
				else return false;
			}}
		)
	));

	public static Bool isCompatible(Element<?> elementPicked, Element<?> elementOver) {
		
		return new Bool(elementPicked != elementOver && 
						elementOver.getPropertyOwner() != null && 
						elementOver.getPropertyOwner().getTypeExpression().canBeAssignedA(elementPicked.getType()).value);
		
	}

}