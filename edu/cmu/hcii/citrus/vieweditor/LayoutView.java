// Created on Feb 22, 2005

package edu.cmu.hcii.citrus.vieweditor;

import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;


//
// @author Andrew J. Ko
//
public class LayoutView extends ElementView {

	public static final Dec<Layout> model = new Dec<Layout>();

	public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, App.getGlobalStyle().get(Style.horizontalSpacing).value));

	public static final Dec<Real> width = new Dec<Real>(View.<Real>parseExpression("(this rightmostChildsRight)"));
	public static final Dec<Real> height = new Dec<Real>(View.<Real>parseExpression("(this tallestChildsHeight)"));
	
	public static final Dec<List<Paint>> background = new Dec<List<Paint>>("[(this getStyle).lighterBackgroundPaint]");
	
	public static final Dec<Real> vPad = new Dec<Real>(new Real(5));
	public static final Dec<Real> hPad = new Dec<Real>(new Real(5));

	// These were for Vertical Layout
	/*
	  	public static final Dec<List<View>> children = new Dec<List<View>>(View.children,
		make(Label.class, arg(Label.text, string("Vertical\nLayout")), arg(Label.font, getStyleProperty(Style.plainFont))),
		make(PropertyPopupMenu.class, arg(PropertyPopupMenu.property, dot(model, getProp(VerticalLayout.alignment)))),
		make(ParsedTextField.class, arg(ParsedTextField.model, dot(model, getProp(VerticalLayout.indentation)))),
		make(ParsedTextField.class, arg(ParsedTextField.model, dot(model, getProp(VerticalLayout.spacing)))));

//Horizontal
 * 	
 	public static final Dec<List<View>> children = new Dec<List<View>>(View.children,
		make(Label.class, arg(Label.text, string("Horizontal\nlayout")), arg(Label.font, getStyleProperty(Style.plainFont))),
		make(PropertyPopupMenu.class, arg(PropertyPopupMenu.property, dot(model, getProp(HorizontalLayout.alignment)))),
		make(ParsedTextField.class, arg(ParsedTextField.model, dot(model, getProp(HorizontalLayout.spacing)))));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.children,
		make(Label.class, arg(Label.text, string("Horizontal\ntabbed layout")), arg(Label.font, getStyleProperty(Style.plainFont))),
		make(ParsedTextField.class, arg(ParsedTextField.model, dot(model, getProp(HorizontalTabbedLayout.spacing)))));

	 */
	
	public LayoutView(ArgumentList arguments) { super(arguments); }

}
