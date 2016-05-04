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

import java.awt.geom.Rectangle2D;

import edu.cmu.hcii.citrus.*;

public class Rectangle extends BootElement<Rectangle> {

	public final Rectangle2D value;
	
	public Rectangle(double x, double y, double w, double h) { value = new Rectangle2D.Double(x, y, w, h); }
	
	public Rectangle evaluate(Element<?> env) { return this; }
	
	public Rectangle duplicate() { return new Rectangle(value.getX(), value.getY(), value.getWidth(), value.getHeight()); }

	public Text toCitrus() { return new Text(value.toString()); }
	public String toString() { return value.toString(); }

	// Returns a linear interpolation between the start and end transforms.
	public Rectangle getTransitionalValue(Transition t, Rectangle start, Rectangle end, long currentTime) {

		// Recover the attributes of the start transform.
		double startX = start.value.getX();
		double startY = start.value.getY();
		double startW = start.value.getWidth();
		double startH = start.value.getHeight();

		// Recover the attributes of the end transform.
		double endX = end.value.getX();
		double endY = end.value.getY();
		double endW = end.value.getWidth();
		double endH = end.value.getHeight();
		
		// Find the transitional values using the transition.
		double x = t.value(currentTime, startX, endX);
		double y = t.value(currentTime, startY, endY);
		double w = t.value(currentTime, startW, endW);
		double h = t.value(currentTime, startH, endH);
		
		return new Rectangle(x, y, w, h);
		
	}
	
	public Bool isEquivalentTo(Element<?> o) { return new Bool(o instanceof Rectangle && ((Rectangle)o).value.equals(value)); }


}
