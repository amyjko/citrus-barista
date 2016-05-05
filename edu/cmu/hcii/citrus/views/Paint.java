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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.cmu.hcii.citrus.*;

// Paint can paint, given a AWT graphics context, and can return the boundaries in which it paints.
// It's also required to manually register a property listener as listener to all of its properties.
public abstract class Paint extends Expression<Paint> {
	
	public static final Dec<Real> alpha = new Dec<Real>(new Real(1.0));
	static {
		alpha.is(new PropertyRestriction("(alpha >= 0.0)", "0.0"));
		alpha.is(new PropertyRestriction("(alpha <= 1.0)", "1.0"));
	}
	
	public static final Dec<Real> left = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> right = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> top = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> bottom = new Dec<Real>(new Real(0.0));

	public static final Dec<Color> primaryColor = new Dec<Color>(Color.black);
	public static final Dec<Color> secondaryColor = new Dec<Color>((Element)null);

	public Paint() { this(null, null); }
	public Paint(ArgumentList arguments) { this(null, arguments); }
	public Paint(Color newColor, Color newSecondColor,
			double newAlpha, 
			double leftOffset, double topOffset, 
			double rightOffset, double bottomOffset) {

		this(null, null);
		set(primaryColor, newColor);
		set(secondaryColor, newSecondColor);
		set(alpha, new Real(newAlpha));
		set(left, new Real(leftOffset));
		set(right, new Real(rightOffset));
		set(top, new Real(topOffset));
		set(bottom, new Real(bottomOffset));

	}
	public Paint(Namespace type, ArgumentList arguments) { super(type, arguments); 
	
			for(DecInterface dec : getType().getDeclarationsToInstantiate())
			getPropertyByDeclaration(dec).addListener(notifyApp);

	}

	protected static Listener notifyApp = new ListenerAdapter() {
		public void outOfDate(Property p, Transition t, Element oldValue) { App.propertyIsOutOfDate(p); }
		public void changed(Property p, Transition t, Element oldValue, Element newValue) { 
			App.propertyChanged(p); }	
	};
	
	public double getVisible(Dec<Real> declaration) { 
		
		return getPropertyByDeclaration(declaration).getVisible().value;
		
	}

	public Paint evaluate(Element<?> el) { return this; }

	public abstract void paint(Graphics2D g, View v, double l, double t, double r, double b);
	
	/////////////////////////////////////////////////////////////////////
	//
	// Answers the question, "If you were to paint this Tile, where 
	// would you paint it?" This is largely used for reporting damage.
	// Should be returned in terms of the tile's local coordinate system.
	//
	/////////////////////////////////////////////////////////////////////    
	public abstract Rectangle2D getPaintBounds(double l, double t, double r, double b);
	
	// Should sum all of the relevant properties, defining something like a hash code to define
	// the unique appearance of the paint.
	public int hashCode(View v, double l, double t, double r, double b) {
		
		int code = 1;
		code = code * 31 + get(primaryColor).hashCode();
		code = code * 31 + (new Double(getVisible(alpha))).hashCode();
		code = code * 31 + (new Double(getVisible(left))).hashCode();
		code = code * 31 + (new Double(getVisible(right))).hashCode();
		code = code * 31 + (new Double(getVisible(top))).hashCode();
		code = code * 31 + (new Double(getVisible(bottom))).hashCode();
		return code;
		
	}
	
}
