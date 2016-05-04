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

public class VerticalLayout extends Layout {

    public static final Text LEFT = new Text("left-aligned");
    public static final Text CENTERED = new Text("centered");
    public static final Text RIGHT = new Text("right-aligned");
	
    public static final Dec<Text> alignment = new Dec<Text>(LEFT);
	public static final Dec<Real> indentation = new Dec<Real>(new Real(0.0));    
	public static final Dec<Real> spacing = new Dec<Real>(new Real(0.0));

	static {

    		alignment.is(new PropertyRestriction("(alignment matches \"left-aligned|centered|right-aligned\")", null));
		
	}

	public VerticalLayout(Namespace type, ArgumentList args) { super(type, args); }

    	public VerticalLayout(int newAlignment, double newIndentation, double newSpacing) { 
    	
    		super(null);
    		set(alignment, newAlignment == -1 ? LEFT : newAlignment == 0 ? CENTERED : RIGHT);
    		set(indentation, new Real(newIndentation));
    		set(spacing, new Real(newSpacing));
    		
    }
    
    // TODO: This should constrain the spacing and indentation, not set them.
    public VerticalLayout(int newAlignment, Expression<Real> newIndentation, Expression<Real> newSpacing) { 
    	
    		super(null);
		set(alignment, newAlignment == -1 ? LEFT : newAlignment == 0 ? CENTERED : RIGHT);
    		set(indentation, newIndentation.evaluate(null));
    		set(spacing, newSpacing.evaluate(null));
    		
    }
    
	public Real getLeft(View child) {
		
		Text al = peek(alignment);
		if(al.equals(LEFT)) return peek(indentation);
		else if(alignment.equals(RIGHT)) return child.getParent().get(View.width).minus(child.paddedWidth());
		else return child.getParent().get(View.width).minus(child.paddedWidth()).divide(new Real(2));
		
	}

	public Real getTop(View child) {

		return 	child.getPreviousSibling() == null ? new Real(0.0) : 
				child.getPreviousSibling().get(View.bottom).plus(peek(spacing));
		
	}

	public String toString() {
		
		Text al = peek(alignment);
		if(al.equals(LEFT)) return LEFT.value;
		else if(al.equals(RIGHT)) return RIGHT.value;
		else return CENTERED.value;

	}
    
}