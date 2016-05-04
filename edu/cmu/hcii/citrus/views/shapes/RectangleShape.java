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
package edu.cmu.hcii.citrus.views.shapes;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

public class RectangleShape extends Shape {

	public RectangleShape() { super(); }
	public RectangleShape(ArgumentList arguments) { super(arguments); }
	
	public boolean contains(double l, double t, double r, double b, double x, double y) {

		double minX, maxX, minY, maxY;
		if(l < r) { minX = l; maxX= r; } else { minX = r; maxX = l; }
		if(t < b) { minY = t; maxY= b; } else { minY = b; maxY = t; }
		
		return minX <= x && maxX >= x && minY <= y && maxY >= y;		
	
	}
	
}
