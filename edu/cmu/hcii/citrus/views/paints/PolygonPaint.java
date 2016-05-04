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

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

public class PolygonPaint extends Paint {

	public static final Dec<List<Point>> points = new Dec<List<Point>>(new NewList<Point>());
	public static final Dec<Bool> fill = new Dec<Bool>(new Bool(true));
	public static final Dec<Real> stroke = new Dec<Real>(new Real(1.0));
	
	public PolygonPaint(Namespace subtype, ArgumentList arguments) { super(subtype, arguments); }

	// Paints the tile's padded bounds
	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		// Compute points
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for(Point point : get(points)) {
			double x= point.getX() * (r - l) + l;
			double y = point.getY() * (b - t) + t;
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
		}

		return new Rectangle((int)minX, (int)minY, (int)(maxX - minX), (int)(maxY - minY));
		
	}
	
	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {
		
		java.awt.Color oldColor = g.getColor();
		java.awt.AlphaComposite oldComposite = (AlphaComposite)g.getComposite();
		java.awt.Paint oldPaint = g.getPaint();
		java.awt.Stroke oldStroke = g.getStroke();

		int left, top, width, height;

		g.setColor(get(primaryColor).getVisibleColor());
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)getVisible(alpha) * oldComposite.getAlpha()));

		java.awt.Color primary = get(primaryColor).getVisibleColor();
		if(get(secondaryColor) != null) {
			java.awt.Color secondary = get(secondaryColor).getVisibleColor();
			g.setPaint(new GradientPaint((float)(l + (r - l) / 2), (float)t, primary, 
										(float)(l + (r - l) / 2), (float)b, secondary));
		}

		// Compute points
		Polygon p = new Polygon();
		for(Point point : get(points))			
			p.addPoint((int)(point.getX() * (r - l) + l), (int)(point.getY() * (b - t) + t));

		g.setStroke(new BasicStroke((float)getVisible(stroke), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));		

		if(bool(fill)) g.fillPolygon(p);
		else g.drawPolygon(p);

		g.setStroke(oldStroke);
		g.setPaint(oldPaint);
		g.setComposite(oldComposite);
		g.setColor(oldColor);
		
	}
	
	public int hashCode(View v, double l, double t, double r, double b) {

		int code = super.hashCode(v, l, t, r, b);
		code = code * 31 + (new Double(l)).hashCode();
		code = code * 31 + (new Double(t)).hashCode();
		code = code * 31 + (new Double(r)).hashCode();
		code = code * 31 + (new Double(b)).hashCode();
		return code;
		
	}
	
}