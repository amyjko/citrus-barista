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

public class Checkbox extends ElementView {

	public static final Dec<Property<Bool>> property = new Dec<Property<Bool>>((Element)null, true);
	public static final Dec<Bool> model = new Dec<Bool>();

	public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, 5));

	public static final Dec<Real> width = new Dec<Real>(true, "(this lastChildsRight)");
	public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");
	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.mouse.leftButton.pressed, App.mouse.pointer.pick),
		new Behavior(App.mouse.leftButton.released, new Action() { 
			public boolean evaluate(View t) {
				Property<Bool> flag = t.get(property);
				if(flag != null && App.mouse.pointer.isPicked(t)) {
					if(t.contains(App.mouse.pointer.getPosition()).value) flag.set(flag.get().not(), t.getStyle().getQuickTransition());
					App.mouse.pointer.release();
					return true;
				} else return false;
			}})
	));
	
	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression("[(a Box)]"));

	public Checkbox(ArgumentList arguments) { super(arguments); }
	public Checkbox(Namespace type, ArgumentList arguments) { super(type, arguments); }
	
	public ElementView updatedViewFor(Property p, Element newValue, Transition t) { return this; }	

	public static class Box extends View {
		
		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
		public static final Dec<Real> width = new Dec<Real>(new Real(13));
		public static final Dec<Real> height = new Dec<Real>(new Real(13));

		public static final Dec<List<Paint>> content = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[ (this getStyle).checkboxPaint]"));

		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
			new Behavior(App.focusReceived, App.keyboard.addFocusPaint),
			new Behavior(App.focusLost, App.keyboard.removeFocusPaint),
			new Behavior(App.keyboard.typed, new Action() { public boolean evaluate(View t) {
				Property<Bool> flag = t.get(property);
				if(flag != null) {
					flag.set(flag.get().not(), t.getStyle().getQuickTransition());
					return true;
				} else return false;
			}})
		));

		public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression("[(a Checkmark)]"));
		
		public Box() { super(); }
		public Box(ArgumentList args) { super(args); }
		
	}
	
	public static class Checkmark extends View {
		
		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
		public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
		public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));
		public static final Dec<List<Paint>> content = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[(this getStyle).checkmarkPaint]"));

		public static final Dec<Bool> hidden = new Dec<Bool>(true, new Expression<Bool>() { public Bool evaluate(Element<?> env) {
			if(env.get(property) == null) return new Bool(true);
			else return env.get(property).get().not(); }});

		public Checkmark() { super(); }
		public Checkmark(ArgumentList args) { super(args); }

	}


}