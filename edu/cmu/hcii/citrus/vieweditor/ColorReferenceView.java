// Created on Feb 25, 2005

package edu.cmu.hcii.citrus.vieweditor;

import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

//
// @author Andrew J. Ko
//
public class ColorReferenceView extends ElementView {

	public static final Dec<Color> model = new Dec<Color>();

	public static final Dec<Real> width = new Dec<Real>(new Real(40));
	public static final Dec<Real> height = new Dec<Real>(new Real(20));
	
	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression(
		"[" +
		"(a FilledRectanglePaint primaryColor=model secondaryColor=model)" +
		"]"
	));

	public ColorReferenceView(ArgumentList arguments) { super(arguments); }
	
}
