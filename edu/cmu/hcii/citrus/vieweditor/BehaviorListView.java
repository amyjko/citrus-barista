// Created on Feb 22, 2005

package edu.cmu.hcii.citrus.vieweditor;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;

//
// @author Andrew J. Ko
//
public class BehaviorListView extends ListView {
		
	public static final Dec<List<Behavior>> model = new Dec<List<Behavior>>();

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 2));

	public static final Dec<Real> width = new Dec<Real>(true, "(if (model isEmpty) 150.0 (this rightmostChildsRight))");
	public static final Dec<Real> height = new Dec<Real>(true, "(if (model isEmpty) 150.0 (this lastChildsBottom))"); 

	public static final Dec<Real> hPad = new Dec<Real>(new Real(3));
	public static final Dec<Real> vPad = new Dec<Real>(new Real(3));

	public BehaviorListView(ArgumentList arguments) { super(arguments); 
	
		get(property).addView(this);
	
	}

}
