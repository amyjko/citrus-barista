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
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EllipsePaint extends Paint {

	public EllipsePaint(ArgumentList args) { super(args); }
	public EllipsePaint(Color newColor, 
						double newAlpha, 
						double l, double t, 
						double r, double b) {
		
		super(newColor, newColor, newAlpha, l, t, r, b);
		
	}
	
	// Paints the tile's padded bounds
	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		return new Rectangle(getLeft(l, r), getTop(t, b), getWidth(l, r) + 1, getHeight(t, b) + 1);

	}
	
	private int getLeft(double l, double r) { return (int)(l + getVisible(left) * (r - l)); }
	private int getTop(double t, double b) { return (int)(t + getVisible(top) * (b - t)); }
	private int getWidth(double l, double r) { return (int)(((r - l) - 1) * (1.0 - Math.abs(getVisible(right) + getVisible(left)))); }
	private int getHeight(double t, double b) { return (int)(((b - t) - 1) * (1.0 - Math.abs(getVisible(top) + getVisible(bottom)))); }
	
	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {

		java.awt.Color oldColor = g.getColor();
		AlphaComposite oldComposite = (AlphaComposite)g.getComposite();

		int left, top, width, height;
		
		left = getLeft(l, r);
		top = getTop(t, b);
		width = getWidth(l, r);
		height = getHeight(t, b);

		g.setColor(get(primaryColor).getVisibleColor());
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)getVisible(alpha) * oldComposite.getAlpha()));

		g.fillOval(left, top, width, height);

		g.setComposite(oldComposite);
		g.setColor(oldColor);
		
	}
	
}