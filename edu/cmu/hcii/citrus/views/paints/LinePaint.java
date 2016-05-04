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
import java.awt.GradientPaint;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

public class LinePaint extends Paint {
	
	public static final Dec<Bool> solid = new Dec<Bool>(new Bool(true));
	public static final Dec<Bool> arrow = new Dec<Bool>(new Bool(false));
	public static final Dec<Real> stroke = new Dec<Real>(new Real(1.0));
	public static final Dec<Real> x1 = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> y1 = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> x2 = new Dec<Real>(new Real(1.0));
	public static final Dec<Real> y2 = new Dec<Real>(new Real(1.0));
	
	public LinePaint() {}
	public LinePaint(Namespace type, ArgumentList arguments) { super(type, arguments); }
	public LinePaint(ArgumentList arguments) { super(arguments); }
	public LinePaint(Color newColor, double newAlpha, 
					double newStrokeWidth, boolean newSolid, 
					double newX1Offset, double newY1Offset, double newX2Offset,
					double newY2Offset) {

		super(newColor, newColor, newAlpha, 0, 0, 0, 0);
		set(stroke, new Real(newStrokeWidth));
		set(solid, new Bool(newSolid));
		set(x1, new Real(newX1Offset));
		set(x2, new Real(newX2Offset));
		set(y1, new Real(newY1Offset));
		set(y2, new Real(newY2Offset));
	
	}

	// Paints the tile's padded bounds
	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		double x1 = getLeft(l, r), y1 = getTop(t, b), x2 = getRight(l, r), y2 = getBottom(t, b);
		int str = (int)getVisible(stroke);
		boolean paintArrow = bool(arrow);
		if(paintArrow) str = str * 3;		
		return new Rectangle((int)Math.min(x1, x2) - str, (int)Math.min(y1, y2) - str, 
							(int)Math.abs(x1 - x2) + str * 2 + 1, (int)Math.abs(y1 - y2) + str * 2 + 1);
		
	}
	
	private double getLeft(double l, double r) { return l + real(left) + getVisible(x1) * ((r - l) - 1); }
	private double getTop(double t, double b) { return t + real(top) + getVisible(y1) * ((b - t) - 1); }
	private double getRight(double l, double r) { return l + real(right) + getVisible(x2) * ((r - l) - 1); }
	private double getBottom(double t, double b) { return t + real(bottom) + getVisible(y2) * ((b - t) - 1); }

	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {
		
		boolean paintArrow = bool(arrow);
		float strokeWidth = (float)getVisible(stroke);
		
		// Save the render state.
		java.awt.Color oldColor = g.getColor();
		AlphaComposite oldComposite = (AlphaComposite)g.getComposite();
		Stroke oldStroke = g.getStroke();
		java.awt.Paint oldPaint = g.getPaint();

		// Compute the 2 points
		int x1 = (int)getLeft(l, r), y1 = (int)getTop(t, b), x2 = (int)getRight(l, r), y2 = (int)getBottom(t, b);
		
		// Set the color to the arrow color and make a 2 pixel butt stroke
		g.setColor(get(primaryColor).getVisibleColor());

		// Set the stroke
		g.setStroke(new BasicStroke(strokeWidth, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER));

		// If not solid, we fade away if one of the views is invisible, away from the visible view.
		// If this isn't solid, we set the paint to grey out the middle of the line (cyclic)
		if(!bool(solid)) 
			g.setPaint(new GradientPaint(x1, y1, get(primaryColor).getVisibleColor(), x1 + (x2 - x1) / 2, y1 + (y2 - y1) / 2, Color.transparent.getColor(), true));

		// Set the composite
		g.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, oldComposite.getAlpha() * (float)getVisible(alpha)));

		if(paintArrow) {
            
			// This is the angle near the head of the arrow; we'll set it depending on the type of arrow
			double theta;
			float radius = strokeWidth * 3;
	        	// Calculate and draw the arrow head using the theta we calculated
            	theta = Math.atan2(y2 - y1, x2 - x1);
	        	int clockX = (int)(x2 - radius * Math.cos(Math.toRadians(Math.toDegrees(theta) + 30)));
	        	int clockY = (int)(y2 - radius * Math.sin(Math.toRadians(Math.toDegrees(theta) + 30)));
	        	int counterX = (int)(x2 - radius * Math.cos(Math.toRadians(Math.toDegrees(theta) - 30)));
	        	int counterY = (int)(y2 - radius * Math.sin(Math.toRadians(Math.toDegrees(theta) - 30)));
	        	java.awt.Polygon p = new java.awt.Polygon();
	        	p.addPoint(clockX, clockY);
	        	p.addPoint(counterX, counterY);
	        	p.addPoint(x2, y2);
	        	g.fillPolygon(p);
	        
	        g.drawLine(x1, y1, x2 - (int)(.8 * radius * Math.cos(theta)), y2 - (int)(.8 * radius * Math.sin(theta)));
			
		} else {

	        g.drawLine(x1, y1, x2, y2);

		}
		
		// Restore the render state
		g.setPaint(oldPaint);
		g.setComposite(oldComposite);   
		g.setStroke(oldStroke);
		g.setColor(oldColor);

	}

	public int hashCode(View v, double l, double t, double r, double b) {

		int code = super.hashCode(v, l, t, r, b);
		code = code * 31 + (new Double(getVisible(x1))).hashCode(); 
		code = code * 31 + (new Double(getVisible(x2))).hashCode(); 
		code = code * 31 + (new Double(getVisible(y1))).hashCode(); 
		code = code * 31 + (new Double(getVisible(y2))).hashCode(); 
		code = code * 31 + (new Double(getVisible(stroke))).hashCode(); 
		code = code * 31 + (new Double(l)).hashCode();
		code = code * 31 + (new Double(t)).hashCode();
		code = code * 31 + (new Double(r)).hashCode();
		code = code * 31 + (new Double(b)).hashCode();
		return code;

	}
}