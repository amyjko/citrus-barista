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

import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;

public class CollapsableListView extends ListView {

	public static final Dec<List> property = new Dec<List>();

	public static final Dec<Bool> collapsed = new Dec<Bool>(new Bool(false));
	public static final Dec<View> label = new Dec<View>(true, new Parameter<View>());

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));

	public static final Dec<Real> height = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element<?> env) {
		CollapsableListView v = (CollapsableListView)env;
		if(v.get(collapsed).value) return v.getFirstChild().paddedHeight();
		else return v.bottommostChildsBottom(); }});

	public static final Dec<Real> width = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
		return ((CollapsableListView)env).rightmostChildsRight(); }});

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(true, new BaseElement<Bool>() { public Bool evaluate(Element<?> env) {
		return env.get(collapsed);
	}});

	public static final Dec<List<View>> children = new Dec<List<View>>("[(a Header) (an ItemList)]");
	
	public CollapsableListView(ArgumentList args) { super(args); }
	
	public static class Header extends View {
		
		public static final Dec<List<Behavior>> behavior = new Dec<List<Behavior>>(new List<Behavior>(
			new Behavior(
				App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
					Property<Bool> collapsed = t.getParent().getPropertyByDeclaration(CollapsableListView.collapsed);
					return collapsed.set(collapsed.get().not(), App.getGlobalStyle().getQuickerTransition()); }})));
		
		public static final Dec<Real> width = new Dec<Real>(View.<Real>parseExpression("(this rightmostChildsRight)"));
		public static final Dec<Real> height = new Dec<Real>(View.<Real>parseExpression("(this lastChildsBottom)"));

		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>("[(this getStyle).lighterBackgroundPaint]");
		
		public static final Dec<List<View>> children = new Dec<List<View>>("[label]");

		public Header(ArgumentList arguments) { super(arguments); }
		
	}
	
	public static class ItemList extends View {

		public static final Dec<Real> width = new Dec<Real>("(this rightmostChildsRight)");
		public static final Dec<Real> height = new Dec<Real>("(this lastChildsBottom)");

		public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 5));

		public static final Dec<List<View>> chlidren = new Dec<List<View>>("[(property toView)]");

		public ItemList(ArgumentList arguments) {
			super(arguments);
		}
		
	}

}