// Created on Feb 22, 2005

package edu.cmu.hcii.citrus.vieweditor;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;

//
// @author Andrew J. Ko
//
public class ViewDecListView extends ListView {
		
	public static final Dec<List<Dec>> model = new Dec<List<Dec>>();

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 2));
	
	public static final Dec<Real> hPad = new Dec<Real>(new Real(3));
	public static final Dec<Real> vPad = new Dec<Real>(new Real(3));

	public ViewDecListView(ArgumentList arguments) { super(arguments); 
	
		// Hack to get property to update this view
		get(model).getPropertyOwner().addView(this);
	}

	public Bool include(Element<?> item) { 
		
		return item.get(Dec.typeExpression).getBaseType().isTypeOf(Boot.LIST).not(); 
		
	}

}
