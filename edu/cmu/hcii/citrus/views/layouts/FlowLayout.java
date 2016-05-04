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

public class FlowLayout extends Layout {
    
    	public static final Dec<Real> hangingIndent = new Dec<Real>(new Real(0.0));
    	public static final Dec<Real> hSpacing = new Dec<Real>(new Real(0.0));
    	public static final Dec<Real> vSpacing = new Dec<Real>(new Real(0.0));
    	public static final Dec<View> viewToWrapInside = new Dec<View>();

    	Real right = new Real(200);
    	
    	public FlowLayout(ArgumentList args) { super(args); }
    	public FlowLayout(double newHSpacing, double newVSpacing, double newHangingIndent, View newView) {

    		super(null);
    		set(hSpacing, new Real(newHSpacing));
    		set(vSpacing, new Real(newVSpacing));
    		set(hangingIndent, new Real(newHangingIndent));
    		set(viewToWrapInside, newView);
    		
    	}

	public Real getLeft(View child) {
		
		// If previous sibling is null, return 0
		// If this would be past parent's right (previous sibling's right + width > parent's right), return 0
		// Otherwise, return previous sibling's right.

		if(child.getPreviousSibling() == null) return new Real(0);

		View view = get(viewToWrapInside);
		if(view == null) return new Real(child.getPreviousSibling().real(View.right) + peek(hSpacing).value);
		else {
			Real localRight = right;//child.localRightOf(view);
			if(child.getPreviousSibling().real(View.right) + peek(hSpacing).value + child.paddedWidth().value > localRight.value)
				return peek(hangingIndent);
			else return child.getPreviousSibling().get(View.right).plus(get(hSpacing));
		}
		
	}

	public Real getTop(View child) {

		// If previous sibling is null, return 0
		// If this would be past parent's right (previous sibling's right + width > parent's right)
		//		return previous sibling's bottom + spacing
		// Otherwise, return previous sibling's top.
		if(child.getPreviousSibling() == null) return new Real(0);
		
		View view = get(viewToWrapInside);
		if(view == null) {
			return child.getPreviousSibling().get(View.top);
		}
		else {
			Real localRight = right;//child.localRightOf(view);
			if(child.getPreviousSibling().real(View.right) + peek(hSpacing).value + child.paddedWidth().value > localRight.value) {
				return child.getPreviousSibling().get(View.bottom).plus(get(vSpacing));
			}
			else {
				return child.getPreviousSibling().get(View.top);
			}
		}
		
	}

}