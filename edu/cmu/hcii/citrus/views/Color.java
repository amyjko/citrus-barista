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

public class Color extends BaseElement<Color> {

	public static final Dec<Real> r = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> g = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> b = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> alpha = new Dec<Real>(new Real(255.0));

	public static final Color black = new Color(0, 0, 0, 255);
	public static final Color white = new Color(255, 255, 255, 255);
	public static final Color red = new Color(255, 0, 0, 255);
	public static final Color green = new Color(0, 255, 0, 255);
	public static final Color blue = new Color(0, 0, 255, 255);
	public static final Color cyan = new Color(255, 0, 255, 255);
	public static final Color yellow = new Color(255, 255, 0, 255);
	public static final Color magenta = new Color(255, 0, 255, 255);
	public static final Color gray = new Color(100, 100, 100, 255);
	public static final Color grey = new Color(100, 100, 100, 255);
	public static final Color lightGrey = new Color(200, 200, 200, 255);
	public static final Color lightGray = new Color(200, 200, 200, 255);	
	public static final Color transparent = new Color(0, 0, 0, 0);
	
	public Color(Namespace type, ArgumentList arguments) { 
		
		super(type, arguments); 
	
		for(DecInterface dec : getType().getDeclarationsToInstantiate())
		getPropertyByDeclaration(dec).addListener(notifyApp);

	}

	protected static Listener notifyApp = new ListenerAdapter() {
		public void outOfDate(Property p, Transition t, Element oldValue) { App.propertyIsOutOfDate(p); }
		public void changed(Property p, Transition t, Element oldValue, Element newValue) { App.propertyChanged(p); }	
	};

	public Color(int newRed, int newGreen, int newBlue, int newAlpha) { 
		
		set(r, new Real(newRed));		
		set(g, new Real(newGreen));
		set(b, new Real(newBlue));
		set(alpha, new Real(newAlpha));
		
	}
	
	public java.awt.Color getColor() {
		return new java.awt.Color((int)(getPropertyByDeclaration(r).get().value), 
				(int)(getPropertyByDeclaration(g).get().value),
				(int)(getPropertyByDeclaration(b).get().value), 
				(int)(getPropertyByDeclaration(alpha).get().value));		
	}
	public java.awt.Color getVisibleColor() {
		
		return new java.awt.Color((int)(getPropertyByDeclaration(r).getVisible().value), 
								(int)(getPropertyByDeclaration(g).getVisible().value),
								(int)(getPropertyByDeclaration(b).getVisible().value), 
								(int)(getPropertyByDeclaration(alpha).getVisible().value));
		
	}

}