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
package edu.cmu.hcii.citrus.views.layouts;


import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.Layout;
import edu.cmu.hcii.citrus.views.View;

public class CenteredLayout extends Layout {

	public static final Dec<Real> spacing = new Dec<Real>(new Real(0.0));

	public CenteredLayout(ArgumentList args) { super(args); }

    public CenteredLayout(double newSpacing) { 
    	
    		super(null);
    		set(spacing, new Real(newSpacing));
    		
    }
        
	public Real getLeft(View child) {
		
		return child.getParent().get(View.width).minus(child.paddedWidth().divide(new Real(2)));
		
	}

	public Real getTop(View child) {

		return child.getPreviousSibling() == null ? 
					child.getParent().get(View.height).minus(child.getParent().totalHeightOfChildren().divide(new Real(2))) : 
					child.getPreviousSibling().get(View.bottom).plus(get(spacing));
	
	}

	public String toString() {
		
		return "Centered Layout";

	}
    
}