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

public class HorizontalLayout extends Layout {
    
    public static final Text TOP = new Text("top-aligned");
    public static final Text CENTERED = new Text("vertically-centered");
    public static final Text BOTTOM = new Text("bottom-aligned");
    
    	public static final Dec<Text> alignment = new Dec<Text>(CENTERED);
    	public static final Dec<Real> spacing = new Dec<Real>(new Real(0.0));

    	static {
    		alignment.is(new PropertyRestriction("(alignment matches \"top-aligned|vertically-centered|bottom-aligned\")", null));
    	}
    	
	public HorizontalLayout(Namespace type, ArgumentList args) { super(type, args); }
    	public HorizontalLayout(int newAlignment, double newSpacing) {

    		super(null);
    		set(alignment, newAlignment == -1 ? TOP : newAlignment == 0 ? CENTERED : BOTTOM);
    		set(spacing, new Real(newSpacing));
        
    	}
    
    // TODO: This should constrain the spacing, not set it.
    public HorizontalLayout(int newAlignment, Expression<Real> newSpacing) {

    		super(null);
		set(alignment, newAlignment == -1 ? TOP : newAlignment == 0 ? CENTERED : BOTTOM);
		set(spacing, newSpacing.evaluate(null));
        
    }
    
    // Return 0 if this is the first child, and the previous child's right + spacing for the rest.
	public Real getLeft(View child) {
		
		return child.getPreviousSibling() == null ? new Real(0.0) : 
				child.getPreviousSibling().get(View.right).plus(peek(spacing));
	
	}

	// Depends on the alignment.
	public Real getTop(View child) {
		
		Text align = peek(alignment);
		if(align.equals(TOP)) return new Real(0.0);
		else if(align.equals(BOTTOM)) return child.getParent().get(View.height).minus(child.paddedHeight());     
		else {
			View par = child.getParent();
			if(par == null) return new Real(0.0);
			else return par.get(View.height).minus(child.paddedHeight()).divide(new Real(2.0));
		}
		
	}
	
}