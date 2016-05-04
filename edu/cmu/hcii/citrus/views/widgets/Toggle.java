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
import edu.cmu.hcii.citrus.views.devices.*;

////////////////////////////////////
//
// A view of a Boolean property.
//
////////////////////////////////////
public class Toggle extends ElementView {

	public static final Dec<Property<Bool>> property = new Dec<Property<Bool>>((Element)null, true);

	public static final Dec<View> trueView = new Dec<View>(View.<View>parseExpression("(a Label text=\"true\")"), true);
	public static final Dec<View> falseView = new Dec<View>(View.<View>parseExpression("(a Label text=\"false\")"), true);

	public static final Dec<Real> vPad = new Dec<Real>(new Real(3));
	public static final Dec<Real> hPad = new Dec<Real>(new Real(3));

//	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(true));

	public static final BaseElement<Real> toggleWidth = new BaseElement<Real>() { 
		public Real evaluate(Element<?> env) {
			if(env.get(property).get().value) return env.get(children).first().paddedWidth();
			else return env.get(children).last().paddedWidth(); }};

	public static final BaseElement<Real> toggleHeight = new BaseElement<Real>() { 
		public Real evaluate(Element<?> env) {
			if(env.get(property).get().value) return env.get(children).first().paddedHeight();
			else return env.get(children).last().paddedHeight(); }};
	
	public static final Dec<Real> width = new Dec<Real>(true, toggleWidth);
	public static final Dec<Real> height = new Dec<Real>(true, toggleHeight);

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.mouse.pointer.entered, new Action() { 
			public boolean evaluate(View t) {
				t.addBackgroundPaint(t.getStyle().get(Style.textBackgroundPaint));
				return true;
			}}),
		new Behavior(App.mouse.pointer.exited, new Action() { 
			public boolean evaluate(View t) {
				t.removeBackgroundPaint(t.getStyle().get(Style.textBackgroundPaint));
				return true;
			}}),
		new Behavior(App.focusReceived, App.keyboard.addFocusPaint),
		new Behavior(App.focusLost, App.keyboard.removeFocusPaint),
		new Behavior(new Keyboard.Typed(' '), new Action() { 
			public boolean evaluate(View t) {
				t.get(property).set(t.get(property).get().not(), t.getStyle().getQuickerTransition());
				return true;
			}}),
		new Behavior(App.mouse.leftButton.pressed, App.mouse.pointer.pick),
		new Behavior(
			App.mouse.leftButton.released, new Action() { 
				public boolean evaluate(View t) {
					if(App.mouse.pointer.isPicked(t)) {
						Property<Bool> p = t.get(property);
						if(t.contains(App.mouse.pointer.getPosition()).value) p.set(p.get().not(), t.getStyle().getQuickestTransition());
						App.mouse.pointer.release();
						return true;
					} return false;
				}})
	));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression("[(a True) (a False)]"));

	public Toggle(Namespace subType, ArgumentList arguments) { super(subType, arguments); }

	public ElementView updatedViewFor(Property p, Element newValue, Transition t) { return this; }
	
	public static class False extends View {

		public static final Dec<Bool> hidden = new Dec<Bool>(true, new Expression<Bool>() { 
			public Bool evaluate(Element<?> env) {
				return env.get(property).get();
			}});

		public static final Dec<Real> width = new Dec<Real>(true, "(this widestChildsWidth)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");

		public static final Dec<List<View>> children = new Dec<List<View>>("[falseView]");

		public False(ArgumentList arguments) { super(arguments); }

	}
	
	public static class True extends View {

		public static final Dec<Bool> hidden = new Dec<Bool>(true, new Expression<Bool>() { 
			public Bool evaluate(Element<?> env) {
				return env.get(property).get().not();
			}});
		
		public static final Dec<Real> width = new Dec<Real>(true, "(this widestChildsWidth)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");

		public static final Dec<List<View>> children = new Dec<List<View>>("[trueView]");

		public True(ArgumentList arguments) { super(arguments); }

	}


}