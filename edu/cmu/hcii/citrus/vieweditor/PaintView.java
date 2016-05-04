// Created on Feb 22, 2005

package edu.cmu.hcii.citrus.vieweditor;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;
import edu.cmu.hcii.citrus.views.paints.*;

//
// @author Andrew J. Ko
//
public class PaintView extends ElementView {

	public static final Dec<Paint> model = new Dec<Paint>();

	public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, App.getGlobalStyle().get(Style.horizontalSpacing).value));

	public static final Dec<Real> width = new Dec<Real>(View.<Real>parseExpression("(this rightmostChildsRight)"));
	public static final Dec<Real> height = new Dec<Real>("(this tallestChildsHeight)");

	public static final Dec<Real> scale = new Dec<Real>(new Real(0.75));
	
	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>(
		new FilledRectanglePaint(Color.lightGray, 0.5, 0.0, 0.0, 0.0, 0.0, 5, 5)));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
		"[" +
		"(a Preview)" + 
		"]"
	));
	
	public static final Dec<Real> vPad = new Dec<Real>(new Real(5));
	public static final Dec<Real> hPad = new Dec<Real>(new Real(5));

	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(true));

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>("[(a Draggable)]");
	
	public PaintView(ArgumentList arguments) { super(arguments); }

	public static class Preview extends View {

		public static final Dec<Real> width = new Dec<Real>(new Real(48));
		public static final Dec<Real> height = new Dec<Real>(new Real(48));

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[model]"));

		public Preview(ArgumentList arguments) { super(arguments); }

	}
	
}
