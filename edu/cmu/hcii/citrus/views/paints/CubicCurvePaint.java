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
package edu.cmu.hcii.citrus.views.paints;

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

public class CubicCurvePaint extends Paint {
	
	public static final Dec<Real> stroke = new Dec<Real>(new Real(1.0));
	public static final Dec<Point> start = new Dec<Point>(new Point(0.0, 0.0));
	public static final Dec<Point> control1 = new Dec<Point>(new Point(1.0, 0.25));
	public static final Dec<Point> control2 = new Dec<Point>(new Point(1.0, 0.75));
	public static final Dec<Point> end = new Dec<Point>(new Point(0.0, 1.0));
	
	public CubicCurvePaint(ArgumentList arguments) { super(arguments); }
	
	// Paints the tile's padded bounds
	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		int visibleStroke = (int)getVisible(stroke);
		return new Rectangle((int)l - visibleStroke, (int)t - visibleStroke, (int)(r - l) + 1 + visibleStroke * 2, (int)(b - t) + 1 + visibleStroke * 2);
		
	}

	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {

		float strokeWidth = (float)getVisible(stroke);
		int width = (int)(r - l);
		int height = (int)(b - t);
		
		// Save the render state.
		java.awt.Color oldColor = g.getColor();
		AlphaComposite oldComposite = (AlphaComposite)g.getComposite();
		Stroke oldStroke = g.getStroke();
		java.awt.Paint oldPaint = g.getPaint();

		// Set the color to the arrow color and make a 2 pixel butt stroke
		g.setColor(get(primaryColor).getVisibleColor());

		// Set the stroke
		g.setStroke(new BasicStroke(strokeWidth, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_BEVEL));

		// Set the composite
		g.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, oldComposite.getAlpha() * (float)getVisible(alpha)));

		java.awt.geom.CubicCurve2D curve = 
			new java.awt.geom.CubicCurve2D.Double(
					l + width * get(start).getX(), t + height * get(start).getY(),
					l + width * get(control1).getX(), t + height * get(control1).getY(),
					l + width * get(control2).getX(), t + height * get(control2).getY(),
					l + width * get(end).getX(), t + height * get(end).getY());

		g.draw(curve);
		
		// Restore the render state
		g.setPaint(oldPaint);
		g.setComposite(oldComposite);   
		g.setStroke(oldStroke);
		g.setColor(oldColor);

	}
	
	public int hashCode(View v, double l, double t, double r, double b) {

		int code = super.hashCode(v, l, t, r, b);
		code = code * 31 + get(start).hashCode();
		code = code * 31 + get(control1).hashCode();
		code = code * 31 + get(control2).hashCode();
		code = code * 31 + get(end).hashCode();
		code = code * 31 + (new Double(getVisible(stroke))).hashCode();
		code = code * 31 + (new Double(l)).hashCode();
		code = code * 31 + (new Double(t)).hashCode();
		code = code * 31 + (new Double(r)).hashCode();
		code = code * 31 + (new Double(b)).hashCode();
		return code;
		
	}
	
}