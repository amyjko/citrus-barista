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

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

public class RectanglePaint extends Paint {

	public static final Dec<Real> cornerWidth = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> cornerHeight = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> stroke = new Dec<Real>(new Real(1.0));

	public Stroke strokeCache = null;

	public RectanglePaint() {}
	public RectanglePaint(Namespace subtype, ArgumentList arguments) { super(subtype, arguments); }
	public RectanglePaint(ArgumentList args) { super(args); }
	public RectanglePaint(Color newColor, double newAlpha, 
						double newStrokeWidth, double l,
						double t, double r, 
						double b, double newCornerWidth,
						double newCornerHeight) {

		super(newColor, newColor, newAlpha, l, t, r, b);
		set(stroke, new Real(newStrokeWidth));
		set(cornerWidth, new Real(newCornerWidth));
		set(cornerHeight, new Real(newCornerHeight));
		
	}

	// Paints the tile's padded bounds
	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		double str = getVisible(stroke);
		return new Rectangle(getLeft(l) - (int)(str), 
							getTop(t) - (int)(str), 
							getWidth(l, r) + (int)str * 2 + 1, 
							getHeight(t, b) + (int)str * 2 + 1);

	}
	
	private int getLeft(double l) { return (int)Math.round(l + getVisible(left)); }
	private int getTop(double t) { return (int)Math.round(t + getVisible(top)); }
	private int getWidth(double l, double r) { return (int)Math.floor((r - getLeft(l) - getVisible(right))); }
	private int getHeight(double t, double b) { return (int)Math.floor((b - getTop(t) - getVisible(bottom))); }
	
	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {

		if(l == r || t == b) return;
		
		// Save the old state
		java.awt.Color oldColor = g.getColor();
		AlphaComposite oldComposite = (AlphaComposite)g.getComposite();
		Stroke oldStroke = g.getStroke();

		// Get the boundaries
		int left, top, width, height;
		
		left = getLeft(l);
		top = getTop(t);
		width = getWidth(l, r);
		height = getHeight(t, b);

		// Set the context
		g.setColor(get(primaryColor).getVisibleColor());
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)getVisible(alpha) * oldComposite.getAlpha()));
		g.setStroke(new BasicStroke((float)getVisible(stroke), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));		

		if(strokeCache != null) g.setStroke(strokeCache);

		if(width < 0) { left += width; width = -width; }
		if(height < 0) { top += height; height= -height; }
		
		// Draw the rectangle
		int cw = (int)getVisible(cornerWidth);
		int ch = (int)getVisible(cornerHeight);
		if(cw != 0 || ch != 0) g.drawRoundRect(left, top, width, height, cw, ch);
		else g.drawRect(left, top, width, height);

		// Restore the state
		g.setStroke(oldStroke);
		g.setComposite(oldComposite);
		g.setColor(oldColor);
		
	}
	
	public int hashCode(View v, double l, double t, double r, double b) {

		int code = super.hashCode(v, l, t, r, b);
		code = code * 31 + (new Double(getVisible(cornerWidth))).hashCode(); 
		code = code * 31 + (new Double(getVisible(cornerHeight))).hashCode(); 
		code = code * 31 + (new Double(getVisible(stroke))).hashCode(); 
		code = code * 31 + (new Double(l)).hashCode();
		code = code * 31 + (new Double(t)).hashCode();
		code = code * 31 + (new Double(r)).hashCode();
		code = code * 31 + (new Double(b)).hashCode();
		return code;
		
	}

	
}