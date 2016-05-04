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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

// Paints a list of paints.
public class PaintPaint extends Paint {

	public static final Dec<List<Paint>> paint = new Dec<List<Paint>>(new NewList<Paint>());
	
	public PaintPaint() {}
	public PaintPaint(Namespace type, ArgumentList args) { super(type, args); }
	public PaintPaint(Paint ... paints) { for(Paint p : paints) get(paint).append(p); }
	
	public PaintPaint addPaint(Paint p) {
	
		if(p == this) throw new ViewError("Can't add a PaintPaint to itself");
		else if (p == null) throw new ViewError("Can't add null to a PaintPaint");
		get(paint).append(p);
		return this;
		
	}

	// Add the paint of all of the paints.
	public Rectangle2D getPaintBounds(double l, double t, double r, double b) {

		Rectangle2D area = null;
		Rectangle2D temp = null;
		for(Paint p : get(paint)) {
			temp = p.getPaintBounds(l, t, r, b);
			if(area == null) area = temp;
			else area.add(temp);
		}
		if(area == null) return new Rectangle2D.Double();
		else return area;
		
	}

	// Paint the paints in order
	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {

		for(Paint p : get(paint))
			p.paint(g, v, l, t, r, b);
		
	}
	
	public int hashCode(View v, double l, double t, double r, double b) {

		int code = super.hashCode(v, l, t, r, b);
		for(Paint p : get(paint)) code = code * 31 + p.hashCode(v, l, t, r, b);
		return code;
		
	}

}