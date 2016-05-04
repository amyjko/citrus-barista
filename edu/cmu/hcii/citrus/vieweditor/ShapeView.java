// Created on Feb 21, 2005

package edu.cmu.hcii.citrus.vieweditor;

import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.paints.*;

//
// @author Andrew J. Ko
//
public class ShapeView extends ElementView {

	public static final Dec<Shape> model = new Dec<Shape>();

	public static final Dec<Real> width = new Dec<Real>(new Real(48));
	public static final Dec<Real> height = new Dec<Real>(new Real(48));
	
	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>(
		new RectanglePaint(Color.black, 1.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0, 0)));

	public ShapeView(ArgumentList arguments) { super(arguments); }

}
