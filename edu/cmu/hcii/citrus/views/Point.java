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
package edu.cmu.hcii.citrus.views;

import edu.cmu.hcii.citrus.*;

public class Point extends BaseElement<Point> {

	public static final Dec<Real> x = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> y = new Dec<Real>(new Real(0.0));

	public Point(Namespace subtype, ArgumentList arguments) { super(subtype, arguments); }
	public Point(double newX, double newY) { super(null); set(x, new Real(newX)); set(y, new Real(newY)); }

	public double getX() { return real(x); }
	public double getY() { return real(y); }
	
	public String toString() { return "(" + getX() + ", " +  getY() + ")"; }
	
	public int hashCode() { 
		
		int code = 1;
		code = code * 31 + (new Double(getPropertyByDeclaration(x).getVisible().value)).hashCode();
		code = code * 31 + (new Double(getPropertyByDeclaration(y).getVisible().value)).hashCode(); 
		return code;
		
	}
	
}