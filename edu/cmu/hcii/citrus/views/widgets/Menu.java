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
import edu.cmu.hcii.citrus.views.layouts.*;
import edu.cmu.hcii.citrus.views.devices.*;

public class Menu extends SetView {

	// Reference only, so we only generate reference views for each.
	public static final Dec<Set> model = new Dec<Set>(new Parameter<Set>(), true);

	// The current selection
	public static final Dec<View> selection = new Dec<View>((Element)null, true);

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 3));

	public static final Dec<Real> width = new Dec<Real>(true, "(if (model isEmpty) 25.0 (this rightmostChildsRight))");
	public static final Dec<Real> height = new Dec<Real>(true, "(if (model isEmpty) 25.0 (this lastChildsBottom))"); 

	public static final Dec<Real> hPad = new Dec<Real>("5.0");
	public static final Dec<Real> vPad = new Dec<Real>("5.0");
	
	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(
		"[" +
		"(this getStyle).menuPaint " +
		"(a FilledRectanglePaint primaryColor=(a Color r=255.0 g=243.0 b=0.0) alpha<-'(if (selection is nothing) 0.0 1.0) top<-'(if (selection is nothing) 0.0 selection.top) bottom<-'(if (selection is nothing) 0.0 (height minus selection.bottom)))" +
		"]"
	);

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(new MousePointer.Entered(),new Action() { public boolean evaluate(View t) {
			((Menu)t).chooseSelection();
			return true;
		}}),
		new Behavior(new MousePointer.Moved(),new Action() { public boolean evaluate(View t) {
			((Menu)t).chooseSelection();
			return true;
		}}),
		new Behavior(new MousePointer.Exited(), new Action() { public boolean evaluate(View t) {
			((Menu)t).setSelection(null);
			return true;
		}}),
		new Behavior(App.mouse.leftButton.released, new Action() { public boolean evaluate(View t) {
			System.err.println("Menu was clicked");
//			if(t.get(action) != null) t.get(action).evaluate(t);
			return true;
		}})
	));
	
	public void moveSelectionUp() {

		if(get(selection) == null) setSelection(get(children).first());
		else if(get(selection).getPreviousSibling() != null) setSelection(get(selection).getPreviousSibling());
		
	}
	
	public void moveSelectionDown() {
		
		if(get(selection) == null) setSelection(get(children).first());
		else if(get(selection).getNextSibling() != null) setSelection(get(selection).getNextSibling());
		
	}
	
	public void chooseSelection() {

		View newSelection = null;
		View oldSelection = get(selection);
		double mouseY = globalToContent(App.mouse.pointer.getPosition()).getY();
		for(View v : get(children))
			if(v.real(top) <= mouseY) newSelection = v;
		setSelection(newSelection);
		
	}
	
	public void setSelection(View newSelection) {
		
		set(selection, newSelection);
		
	}

	public Menu(ArgumentList arguments) { 
		
		super(arguments); 
		getPropertyByDeclaration(selection).addListener(notifyApp);
		
	}

}