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

import java.awt.geom.Point2D;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

// May only be added to list views
public class Insertable extends Behavior {
		
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
	
		new Behavior(App.mouse.pointer.draggedOver, new Action() { public boolean evaluate(View t) {

//			debug("Dragged over " + t);
			removeFeedbackFromChildren(t);
			return check(t) != null;
		
		}}),
		new Behavior(App.mouse.pointer.draggedIn, new Action() { public boolean evaluate(View t) {

//			debug("Dragged into " + t);
			removeFeedbackFromChildren(t);
			return check(t) != null;
		
		}}),
		new Behavior(App.mouse.pointer.draggedOut, new Action() { public boolean evaluate(View t) {

//			debug("Dragged out of " + t);
			removeFeedbackFromChildren(t);
			return true;

		}}),
		// Assume that the element is not in a list.
		new Behavior(App.mouse.pointer.droppedOver, new Action() { public boolean evaluate(View t) {

			ElementView viewOver = check(t);

			removeFeedbackFromChildren(t);

			List list = ((ListView)t).get(ListView.model);

			boolean empty = list.isEmpty().value;
			
			if(!empty && viewOver == null) return false;
			
			Element<?> itemDropped = ((ElementView)App.mouse.pointer.getViewPicked()).getModel();

			if(!areCompatible(itemDropped, list)) return false;
			
			itemDropped.emancipate();

			if(empty) {
				list.appendOverTime(itemDropped, App.getGlobalStyle().getQuickTransition());
			}
			else {

				Point2D local = viewOver.globalToLocal(App.mouse.pointer.getPosition());
				boolean before = local.getY() < viewOver.get(View.top).value + viewOver.get(View.height).value / 2;

				if(before) list.insertBeforeOverTime(viewOver.getModel(), itemDropped, App.getGlobalStyle().getQuickTransition());
				else list.insertAfterOverTime(viewOver.getModel(), itemDropped, App.getGlobalStyle().getQuickTransition());

			}				
			return true;

		}})
	));

	public static ElementView check(View t) {
		
		if(!(t instanceof ListView)) {
			System.err.println("Insertable: Can only use Insertable insertable in ListViews; view is a " + t);
			return null;
		}
		
		// Make sure the view we're over holds a list.
		List<?> listOver = ((ListView)t).get(ListView.model);

		// Make sure the element picked is an element view.
		View pick = App.mouse.pointer.getViewPicked();
		if(!(pick instanceof ElementView)) return null;
		Element elementPicked = ((ElementView)pick).getModel();
		if(elementPicked == null) return null;

		// Which particular item is being dragged over? Iterate through the list view's children
		// until we find one that contains the mouse pointer's position.
		View itemOver = null;
		for(View view : t.get(View.children)) {
			if(view != pick && view.contains(App.mouse.pointer.getPosition()).value) {
				itemOver = view;
				break;
			}				
		}
		if(itemOver == null && pick != t.get(View.children).last()) itemOver = t.get(View.children).last();

		// If the list is empty, we provide special feedback
		boolean empty = listOver.isEmpty().value;
		
		if(!empty && itemOver == null) return null;

		// Verify the type
		boolean compatible = areCompatible(elementPicked, listOver);
		
		if(!compatible) return null;
		
		if(empty) {
			if(compatible) t.addForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
//			else t.addForegroundPaint(App.getGlobalStyle().getBadReplaceFeedback());
			return (ListView)t;
		}
		else {
			// If the mouse is above the center, we'd insert before this list.
			// Otherwise, we'd insert after this list.
			Point2D local = itemOver.globalToLocal(App.mouse.pointer.getPosition());
			boolean before = local.getY() < itemOver.get(View.top).value + itemOver.get(View.height).value / 2;
	
			if(before) {
				if(compatible) itemOver.addForegroundPaint(App.getGlobalStyle().getInsertBeforeFeedback());
//				else itemOver.addForegroundPaint(App.getGlobalStyle().getBadInsertBeforeFeedback());
			}
			else {
				if(compatible) itemOver.addForegroundPaint(App.getGlobalStyle().getInsertAfterFeedback());
//				else itemOver.addForegroundPaint(App.getGlobalStyle().getBadInsertAfterFeedback());
			}

			return (ElementView)itemOver;
			
		}
			
	}
	
	public static void removeFeedbackFromChildren(View t) {
		
		if(t != null) {
			for(View child : t.get(View.children)) {
				child.removeForegroundPaint(App.getGlobalStyle().getInsertBeforeFeedback());
				child.removeForegroundPaint(App.getGlobalStyle().getInsertAfterFeedback());
				child.removeForegroundPaint(App.getGlobalStyle().getBadInsertBeforeFeedback());
				child.removeForegroundPaint(App.getGlobalStyle().getBadInsertAfterFeedback());
			}
			t.removeForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
			t.removeForegroundPaint(App.getGlobalStyle().getBadReplaceFeedback());
		}
		
	}
	
	// The type of the element being dragged must be a subtype of the the first type of the list's type expression.
	public static boolean areCompatible(Element elementPicked, List listOver) {

		TypeExpression listTypeExpression = listOver.getPropertyOwner().getTypeExpression();

		// If it's a generic list, return true.
		if(listTypeExpression.getTypeArguments().isEmpty().value) return true;

		Type listType = listTypeExpression.getTypeArguments().first().getBaseType();
		
		return elementPicked.getType().isTypeOf(listType).value;

	}

}