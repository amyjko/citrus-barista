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
import edu.cmu.hcii.citrus.views.paints.FilledRectanglePaint;

// Given a Type, creates an instance when clicked and prepares for dragging.
public class Duplicator extends View {

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

	public static final Dec<Element> elementToDuplicate = new Dec<Element>(false, new Parameter());
	
	public static final Dec<View> label = new Dec<View>("(a Label text=(elementToDuplicate getType).name)", true);
	
	public static final Dec<Element> elementToDrag = new Dec<Element>((Element)null, false);

	public static final Dec<Real> width = new Dec<Real>(true, "(this firstChildsRight)");
	public static final Dec<Real> height = new Dec<Real>(true, "(this firstChildsBottom)");
	
	public static final Dec<Real> vPad = new Dec<Real>(new Real(5));
	public static final Dec<Real> hPad = new Dec<Real>(new Real(5));

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new List<Paint>(
			new FilledRectanglePaint(new Color(0, 0, 0, 255), 0.1, 0, 0, 0, 0, 5, 5)));

	public static final Dec<List<View>> children = new Dec<List<View>>("[label]");

	public static final When elementToDragChanges = CitrusParser.when(CitrusParser.tokenize(
			"when event (@elementToDrag ValueChanged) " +
				"(this removeIfNull elementToDrag)"));
	
	public Nothing removeIfNull(Element newValue) {
		
		if(newValue.isNothing().value) get(children).nth(new Int(2)).remove();
		return null;
		
	}
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(

		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {

			// If we haven't created one yet, create one without any arguments
			// and set the element to drag to the new instance.
			if(t.get(elementToDrag) == null)
				t.set(elementToDrag, t.get(elementToDuplicate).duplicate());

			Element instance = t.get(elementToDrag);

			// Create a view of the instance and add it to this.
			View newView = Translator.toView(t.getPropertyByDeclaration(elementToDrag));

			if(newView.reactsTo(App.mouse.pointer.dragged).value) {
			
				t.get(children).append(newView);
				
				// Set the transparency of the tile to .5 and pick it up.
				App.mouse.pointer.pickAndHoist.evaluate(newView);
				newView.set(transparency, new Real(1.0), App.getGlobalStyle().getQuickTransition());

				// Leave the rest to the element view, given that it is a draggable element view.					
				return true;
				
			} else {
				debug("The view created for this duplicate of " + t.get(elementToDuplicate) + " doesn't react to dragging.");
				return false;
			}
			
		}}),

		// If the drop fails, then disable it, and reuse it later.
		new Behavior(App.mouse.pointer.dropFailed, new Action() { public boolean evaluate(View t) {
			t.get(children).nth(new Int(2)).remove();
			return true;
		}})

	));
	
	public Duplicator(ArgumentList arguments) { super(arguments); }
	
}