// Created on Feb 15, 2005

package edu.cmu.hcii.citrus.vieweditor;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.behaviors.*;
import edu.cmu.hcii.citrus.views.layouts.*;
import edu.cmu.hcii.citrus.views.paints.*;

//
// @author Andrew J. Ko
//
public class BehaviorView extends ElementView {

	public static final Dec<Behavior> model = new Dec<Behavior>();

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> collapse = new Dec<Bool>(new Bool(false));

	public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
	public static final Dec<Real> vPad = new Dec<Real>(new Real(2));

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));

	public static final Dec<Real> width = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
		return env.get(collapse).value ? 
				env.get(children).first().rightmostChildsRight() : 
				((View)env).rightmostChildsRight(); }});

	public static final Dec<Real> height = new Dec<Real>(true, "(this lastChildsBottom)");

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>(
		new RectanglePaint(Color.lightGrey, 0.3, 1.0, 0.0, 0.0, 0.0, 0.0, 5, 5)));

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Draggable(),
		new Behavior(App.keyboard.DOWN.pressed, App.keyboard.focusBelow),
		new Behavior(App.keyboard.UP.pressed, App.keyboard.focusAbove),
		new Navigable()));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
		"[" +
		"(a Header)" + 
		"(a Body)" +
		"]"
	));

	public BehaviorView(ArgumentList arguments) { super(arguments); }

	public static class Header extends View {

		public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, 5));

		public static final Dec<Real> width = new Dec<Real>(true, "(this lastChildsRight)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");

		public static final Dec<List<View>> children = new Dec<List<View>>(
			"[" +
			"(a Label text=\"when\" font=(this getStyle).italicFont)" +
			"(model.@event toView)" +
			"]"
		);

		public Header(ArgumentList arguments) { super(arguments); }
		
	}

	public static class Body extends View {

		public static final Dec<Bool> hidden = new Dec<Bool>("collapse");

		public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 5));

		public static final Dec<Real> width = new Dec<Real>(true, "(this widestChildsWidth)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this lastChildsBottom)");

		public static final Dec<List<View>> children = new Dec<List<View>>("[(model.@action toView)]");

		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public Body(ArgumentList arguments) { super(arguments); }
		
	}
	
}