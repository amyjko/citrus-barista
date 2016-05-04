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
import edu.cmu.hcii.citrus.views.devices.Mouse;

// Splits a fixed amount of space between two tiles and a divider.
public class SplitView extends View {

	public static final Dec<Real> split = new Dec<Real>(new Real(.5));
	static {		
		split.is(new PropertyRestriction("(split >= 0.0)", "0.0"));
		split.is(new PropertyRestriction("(split <= 1.0)", "1.0"));
	}

	public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
	public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));	
	
	// The two tiles that are resized when the proportion changes.
	public static final Dec<View> one = new Dec<View>(new Parameter<View>(), true);
	public static final Dec<View> two = new Dec<View>(new Parameter<View>(), true);

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
			"[" +
			"(a One) (a Divider) (a Two)" +
			"]"
	));

	// Whether the divider is vertical or horizontal.
	public static final Dec<Bool> vertical = new Dec<Bool>(new Bool(true));

	public SplitView(ArgumentList arguments) { super(arguments); }
	public SplitView(Namespace subType, ArgumentList arguments) { super(subType, arguments); }
	
	public static class Divider extends View {
		
		public static final Dec<Real> left = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return sv.get(children).nth(new Int(1)).get(right);
			else return new Real(0.0); }});

		public static final Dec<Real> top = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return new Real(0.0);
			else return sv.get(SplitView.one).get(bottom); }});

		public static final Dec<Real> width = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return new Real(5.0);
			else return sv.parentsWidth();
		}});

		public static final Dec<Real> height = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return sv.parentsHeight();
			else return new Real(5.0);
		}});

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression(
				"[(this getStyle).splitPaneVerticalDividerPaint]"
		));
		
		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
			// When the divider moves, set the proportion to one relative to the mouse focus point.	
			new Behavior(
				App.mouse.pointer.dragged, new Action() { 
					public boolean evaluate(View t) {
						SplitView sv = (SplitView)t.getParent();
						if(sv.get(SplitView.vertical).value) 
							sv.set(SplitView.split, new Real(Math.max(0.0, Math.min(1.0, App.mouse.pointer.positionRelativeToPointPicked().getX() / (sv.get(width).value - t.get(width).value)))));
						else 
							sv.set(SplitView.split, new Real(Math.max(0.0, Math.min(1.0, App.mouse.pointer.positionRelativeToPointPicked().getY() / (sv.get(height).value - t.get(height).value))))); 
						return true;
				}}),
			new Behavior(App.mouse.pointer.entered, new Action() { public boolean evaluate(View t) {
				SplitView sv = (SplitView)t.getParent();
				if(sv.get(SplitView.vertical).value) t.getWindow().setCursorTo(Mouse.RESIZE_EW_CURSOR);
				else t.getWindow().setCursorTo(Mouse.RESIZE_NS_CURSOR);
				return true;
			}}),
			new Behavior(App.mouse.pointer.exited, App.mouse.setCursorToDefault),
			new Behavior(App.mouse.leftButton.pressed, App.mouse.pointer.pick),
			new Behavior(
				App.mouse.leftButton.released, new Action() {
					public boolean evaluate(View t) {
						App.mouse.pointer.release.evaluate(t);
						App.mouse.setCursorToDefault.evaluate(t);
						return true;
					}
				})));

		public Divider() { super(); }
		public Divider(ArgumentList arguments) { super(arguments); }

	}
	
	public static class One extends View {
		
		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(true));
		public static final Dec<Real> left = new Dec<Real>(true, new Real(0.0));
		public static final Dec<Real> top = new Dec<Real>(true, new Real(0.0));

		// The first tile is a proportion of the parent based on the divider's position.			
		public static final Dec<Real> width = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) 
				return new Real(sv.get(width).value * sv.get(SplitView.split).value - sv.get(children).nth(new Int(2)).paddedWidth().value);
			else return sv.parentsWidth(); }});

		// The first tile is a proportion of the parent based on the divider's position.			
		public static final Dec<Real> height = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return sv.parentsHeight();
			else return new Real(sv.get(height).value * sv.get(SplitView.split).value - sv.get(children).nth(new Int(2)).paddedHeight().value); }});

		public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression("[one]"));

		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
			// When the divider moves, set the proportion to one relative to the mouse focus point.	
			new Behavior(
				App.mouse.leftButton.clicked, new Action() { public boolean evaluate(View t) {			
					if(App.keyboard.COMMAND.isDown()) {
						SplitView sv = (SplitView)t.getParent();
						if(sv.get(SplitView.split).value == 0.0) sv.set(SplitView.split, new Real(1.0), App.getGlobalStyle().getQuickTransition());
						else sv.set(SplitView.split, new Real(0.0), App.getGlobalStyle().getQuickTransition());
						return true;
					}
					else return false;			
				}})));
		
		public One(ArgumentList arguments) { super(arguments); }

	}

	public static class Two extends View {
		
		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(true));

		public static final Dec<Real> left = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return sv.get(children).nth(new Int(2)).get(right);
			else return new Real(0.0); }});

		public static final Dec<Real> top = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return new Real(0.0);
			else return sv.get(children).nth(new Int(2)).get(bottom); }});
		
		// The second tile is the remaining proportion of the parent.						
		public static final Dec<Real> width = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return new Real(sv.get(width).value - sv.get(children).nth(new Int(2)).get(width).value - sv.get(children).nth(new Int(1)).get(width).value);
			else return sv.parentsWidth(); }});

		// The second tile is the remaining proportion of the parent.						
		public static final Dec<Real> height = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			SplitView sv = (SplitView)((View)env).getParent();
			if(sv == null) return new Real(0.0);
			if(sv.get(SplitView.vertical).value) return sv.parentsHeight();
			else return new Real(sv.get(height).value - sv.get(children).nth(new Int(2)).get(height).value - sv.get(children).nth(new Int(1)).get(height).value);
		}});

		public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression("[two]"));

		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
			// When the divider moves, set the proportion to one relative to the mouse focus point.	
			new Behavior(
				App.mouse.leftButton.clicked, new Action() { public boolean evaluate(View t) {			
					if(App.keyboard.COMMAND.isDown()) {
						SplitView sv = (SplitView)t.getParent();
						if(sv.get(SplitView.split).value == 0.0) sv.set(SplitView.split, new Real(1.0), App.getGlobalStyle().getQuickTransition());
						else sv.set(SplitView.split, new Real(0.0), App.getGlobalStyle().getQuickTransition());
						return true;
					} else return false;
				}})));

		public Two(ArgumentList arguments) { super(arguments); }

	}
	
}