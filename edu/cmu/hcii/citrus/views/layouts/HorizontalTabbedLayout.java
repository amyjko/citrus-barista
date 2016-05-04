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

public class HorizontalTabbedLayout extends Layout {
    
    public static final Text TOP = new Text("top-aligned");
    public static final Text CENTERED = new Text("vertically-centered");
    public static final Text BOTTOM = new Text("bottom-aligned");

	public static final Dec<Real> tabLength = new Dec<Real>(new Real(0.0));
    	public static final Dec<Real> spacing = new Dec<Real>(new Real(0.0));
    	public static final Dec<Text> alignment = new Dec<Text>(TOP);
        
    	static {
    		alignment.is(new PropertyRestriction("(alignment matches \"top-aligned|vertically-centered|bottom-aligned\")", null));
    	}

    	public HorizontalTabbedLayout(ArgumentList args) { super(args); }
    	public HorizontalTabbedLayout(int newAlignment, double newTabLength, double newSpacing) {

    		super(null);
    		set(alignment, newAlignment == -1 ? TOP : newAlignment == 0 ? CENTERED : BOTTOM);
    		set(spacing, new Real(newSpacing));
    		set(tabLength, new Real(newTabLength));
        
    	}
    
    // Return 0 if this is the first child, and the previous child's right + spacing for the rest.
	public Real getLeft(View child) {

		double sp = peek(tabLength).value;
		
		// The first child is at 0.
		// Subsequent siblings are at the next closest interval defined by the spacing, right of the previous sibling.
		if(child.getPreviousSibling() == null) return new Real(0.0);
		else if(child.getPreviousSibling().getPreviousSibling() == null && 
				child.getPreviousSibling().real(View.right) < sp)
			return new Real(sp);
		else return child.getPreviousSibling().get(View.right).plus(peek(spacing));
	
	}

	// Depends on the alignment.
	public Real getTop(View child) {
		
		Text align = peek(alignment);
		if(align.equals(TOP)) return 	child.getPreviousSibling() == null ? new Real(0.0) : child.getPreviousSibling().get(View.top);
		else if(align.equals(BOTTOM)) return child.getParent().get(View.height).minus(child.paddedHeight());     
		else return child.getParent().get(View.height).minus(child.paddedHeight()).divide(new Real(2));
		
	}

	public String toString() { return "Row layout"; }
	
}