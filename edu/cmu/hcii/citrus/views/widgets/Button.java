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

public class Button extends View {

	public static final Dec<Expression<?>> action = new Dec<Expression<?>>();
	public static final Dec<View> label = new Dec<View>(View.<View>parseExpression("(a Label text=\"Label\")"));

	private static final Action setDownInPaint = new Action() { public boolean evaluate(View t) {
		if(App.mouse.pointer.isPicked(t)) {
			t.removeBackgroundPaint(App.getGlobalStyle().get(Style.buttonUpPaint));
			t.removeBackgroundPaint(App.getGlobalStyle().get(Style.buttonDownOutPaint));
			t.addBackgroundPaint(App.getGlobalStyle().get(Style.buttonDownInPaint));
			return true;
		} else return false;
	}};
	private static final Action setDownOutPaint = new Action() { public boolean evaluate(View t) {
		if(App.mouse.pointer.isPicked(t)) {
			t.removeBackgroundPaint(App.getGlobalStyle().get(Style.buttonUpPaint));
			t.removeBackgroundPaint(App.getGlobalStyle().get(Style.buttonDownInPaint));
			t.addBackgroundPaint(App.getGlobalStyle().get(Style.buttonDownOutPaint));		
			return true;
		} else return false;
	}};
	private static final Action setUpPaint = new Action() { public boolean evaluate(View t) {
		t.removeBackgroundPaint(App.getGlobalStyle().get(Style.buttonDownInPaint));
		t.removeBackgroundPaint(App.getGlobalStyle().get(Style.buttonDownOutPaint));
		t.addBackgroundPaint(App.getGlobalStyle().get(Style.buttonUpPaint));		
		return true;
	}};

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[(this getStyle).buttonUpPaint]"));
	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(true));

	public static final Dec<Real> width = new Dec<Real>("(this widestChildsWidth)");
	public static final Dec<Real> height = new Dec<Real>("(this firstChildsBottom)");
	public static final Dec<Real> vPad = new Dec<Real>(new Real(5));
	public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
	public static final Dec<Bool> enabled = new Dec<Bool>(new Bool(true));
	public static final Dec<Real> transparency = new Dec<Real>(true, "(if enabled 1.0 0.5)");

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(0, 0, 0));

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.focusReceived, App.keyboard.addFocusPaint),
		new Behavior(App.focusLost, App.keyboard.removeFocusPaint),
		new Behavior(App.keyboard.SPACE.pressed, setDownInPaint),
		new Behavior(App.keyboard.typed, new Expression<Bool>() { public Bool evaluate(Element<?> t) {
			if(App.keyboard.getLastCharacterTyped().value == ' ') {
				setUpPaint.evaluate(t);
				((Button)t).executeAction((View)t);
				return new Bool(true);
			}
			else return new Bool(false);
		}}),
		new Behavior(App.mouse.pointer.entered, setDownInPaint),
		new Behavior(App.mouse.pointer.exited, setDownOutPaint),
		new Behavior(App.mouse.leftButton.pressed, new Expression<Bool>() { public Bool evaluate(Element<?> t) {
			if(!t.get(enabled).value) return Bool.TRUE;
			App.mouse.pointer.pick((View)t);
			setDownInPaint.evaluate(t);
			return new Bool(true); 
		}}),
		new Behavior(App.mouse.leftButton.released, new Expression<Bool>() { public Bool evaluate(Element<?> t) {
			if(App.mouse.pointer.isPicked((View)t)) {
				setUpPaint.evaluate(t);
				if(((View)t).contains(App.mouse.pointer.getPosition()).value)
					((Button)t).executeAction((View)t);
				App.mouse.pointer.release();
				return new Bool(true);
			} else return new Bool(false);
		}})
	));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression("[ label ]"));

	public Button(BaseType type, ArgumentList arguments) { super(type, arguments); }
	public Button(ArgumentList arguments) { super(arguments); }
	
	public Nothing executeAction(View view) {
		
		Element act = get(action);
		if(act != null) {
			if(act instanceof Closure)
				((Closure)act).instantiate(new ArgumentList());
			else act.evaluate(view);
		}
		return null;
		
	}
	
}