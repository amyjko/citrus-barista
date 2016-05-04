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
import edu.cmu.hcii.citrus.views.widgets.TextField;

public class TextFlowLayout extends Layout {
    
    	public static final Dec<Real> linespacing = new Dec<Real>(new Real(0.0));
    	
	public TextFlowLayout(Namespace type, ArgumentList args) { super(type, args); }
    
    // Return 0 if this is the first child, and the previous child's right + spacing for the rest.
	public Real getLeft(View child) {
		
		View sib = child.getPreviousSibling();
		if(sib == null) return new Real(0.0);
		View second = sib.get(View.children).second();
		if(!(second instanceof TextField)) return sib.get(View.right);
		return new Real(second.get(View.right).value + sib.get(View.left).value);
	
	}

	// Depends on the alignment.
	public Real getTop(View child) {
		
		View sib = child.getPreviousSibling();
		if(sib == null) return new Real(0.0);
		View second = sib.get(View.children).second();
		if(!(second instanceof TextField)) return sib.get(View.bottom).minus(child.paddedHeight());
		return second.get(View.bottom).minus(child.paddedHeight());
		
	}
	
}