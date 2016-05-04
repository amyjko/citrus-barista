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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class FilledRectanglePaint extends Paint {

	public static final Dec<Real> cornerWidth = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> cornerHeight = new Dec<Real>(new Real(0.0));

	public FilledRectanglePaint() {}
	public FilledRectanglePaint(Namespace type, ArgumentList arguments) { super(type, arguments); }
	public FilledRectanglePaint(ArgumentList arguments) { super(arguments); }
	public FilledRectanglePaint(Color newColor, double newAlpha, 
								double leftOffset, double topOffset, double rightOffset, double bottomOffset, 
								double newCornerWidth, double newCornerHeight) {

		super(newColor, newColor, newAlpha, leftOffset, rightOffset, topOffset, bottomOffset);
		set(cornerWidth, new Real(newCornerWidth));
		set(cornerHeight, new Real(newCornerHeight));
		
	}
	public FilledRectanglePaint(Color newColor, Color newSecondaryColor, double newAlpha, double leftOffset, 
							  double topOffset, double rightOffset, double bottomOffset, 
							  double newCornerWidth, double newCornerHeight) {

		super(newColor, newSecondaryColor, newAlpha, leftOffset, rightOffset, topOffset, bottomOffset);
		set(cornerWidth, new Real(newCornerWidth));
		set(cornerHeight, new Real(newCornerHeight));
		
		}
	
	// Paints the tile's padded bounds
	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		return new Rectangle(getLeft(l), getTop(t), getWidth(l, r) + 2, getHeight(t, b) + 2);

	}
	
	private int getLeft(double l) { return (int)Math.round(l + getVisible(left)); }
	private int getTop(double t) { return (int)Math.round(t + getVisible(top)); }
	private int getWidth(double l, double r) { return (int)Math.floor(r - getLeft(l) - getVisible(right)); }
	private int getHeight(double t, double b) { return (int)Math.floor(b - getTop(t) - getVisible(bottom)); }
	
	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {
		
		java.awt.Color oldColor = g.getColor();
		java.awt.AlphaComposite oldComposite = (AlphaComposite)g.getComposite();
		java.awt.Paint oldPaint = g.getPaint();

		int left, top, width, height;
		
		left = getLeft(l);
		top = getTop(t);
		width = getWidth(l, r);
		height = getHeight(t, b);

		g.setColor(get(primaryColor).getVisibleColor());
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)getVisible(alpha) * oldComposite.getAlpha()));

		java.awt.Color primary = get(primaryColor).getVisibleColor();
		java.awt.Color secondary = get(secondaryColor) == null ? null : get(secondaryColor).getVisibleColor();
		if(secondary != null)
			g.setPaint(new GradientPaint((float)(l + (r - l) / 2), (float)t, primary, 
										(float)(l + (r - l) / 2), (float)b, secondary));

		double cw = getVisible(cornerWidth);
		double ch = getVisible(cornerHeight);

		if(width < 0) { left += width; width = -width; }
		if(height < 0) { top += height; height= -height; }
		
		if(cw == 0 || ch == 0) g.fillRect(left, top, width, height);
		else g.fillRoundRect(left, top, width, height, (int)cw, (int)ch);

		g.setPaint(oldPaint);
		g.setComposite(oldComposite);
		g.setColor(oldColor);
		
	}
	
	public int hashCode(View v, double l, double t, double r, double b) {

		int code = super.hashCode(v, l, t, r, b);
		code = code * 31 + (new Double(getVisible(cornerWidth))).hashCode();
		code = code * 31 + (new Double(getVisible(cornerHeight))).hashCode();
		code = code * 31 + (new Double(l)).hashCode();
		code = code * 31 + (new Double(t)).hashCode();
		code = code * 31 + (new Double(r)).hashCode();
		code = code * 31 + (new Double(b)).hashCode();
		
		return code;
		
	}

	
}