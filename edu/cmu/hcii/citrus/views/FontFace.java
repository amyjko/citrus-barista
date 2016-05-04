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

import java.awt.Font;
import java.awt.FontMetrics;

import edu.cmu.hcii.citrus.*;

public class FontFace extends BaseElement<FontFace> {

	// When any of these change...
	public static final Dec<Text> family = new Dec<Text>(new Text("Times New Roman"));
	public static final Dec<Text> style = new Dec<Text>(new Text("plain"));
	public static final Dec<Real> size = new Dec<Real>(new Real(12));

	// ...these font metrics should be updated.
	public static final Dec<Real> descent = new Dec<Real>(new Real(0));
	public static final Dec<Real> ascent = new Dec<Real>(new Real(12));
	public static final Dec<Real> lineHeight = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> spaceWidth = new Dec<Real>(new Real(0));
	
	static {

		descent.set(Dec.isConstant, new Bool(true));
		ascent.set(Dec.isConstant, new Bool(true));
		spaceWidth.set(Dec.isConstant, new Bool(true));
		
//		String[] fontFamilyNames = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//		for(int i = 0; i < fontFamilyNames.length; i++) fontFamilies.mayBe(new Text(fontFamilyNames[i]));

//		family.is(fontFamilies);

	}

	private Font font;
	private FontMetrics metrics = null;

	private void updateFont() {

		font = constructFont();
		if(App.getGraphics() == null) return;
		metrics = App.getGraphics().getFontMetrics(constructFont());
		set(ascent, new Real(metrics.getMaxAscent()));
		set(descent, new Real(metrics.getMaxDescent()));
		set(spaceWidth, new Real(metrics.charWidth(' ')));
		set(lineHeight, new Real(metrics.getHeight()));
		
	}
	
	// Called when any of the family, style or size changes.
	private Listener updateFont = new ListenerAdapter() {
		public void changed(Property p, Transition t, Object oldValue, Object newValue) {
			updateFont();
		}
	};

	public FontFace(ArgumentList args) { 
		
		super(args); 
	
		getPropertyByDeclaration(family).addListener(updateFont);
		getPropertyByDeclaration(style).addListener(updateFont);
		getPropertyByDeclaration(size).addListener(updateFont);
		
		updateFont();

	}
	public FontFace() { this(null); }

	public FontFace(String newFamily, String newStyle, int newSize) { 
		
		set(family,  new Text(newFamily));
		set(style, new Text(newStyle));
		set(size, new Real(newSize));		

		updateFont();
		
	}
	
	public java.awt.Font constructFont() {

		String sty = text(style);
		return new Font(text(family), 
						sty.equalsIgnoreCase("bold") ? Font.BOLD : sty.equalsIgnoreCase("italic") ? Font.ITALIC: Font.PLAIN,
						(int)getPropertyByDeclaration(size).getVisible().value);
		
	}
	
	public Font getFont() { 
		if(metrics == null) updateFont();
		return font; 
	}
	public double getAscent() { return real(ascent); }
	public double getDescent() { return real(descent); }
	public double getSpaceWidth() { return real(spaceWidth); }
	public int getCharWidth(char c) { 
		if(metrics == null) updateFont(); 
		if(metrics == null) return 0;
		else return metrics.charWidth(c); 
	}
	public double getVisible(Dec<Real> propertyName) { return getPropertyByDeclaration(propertyName).getVisible().value; }

	public FontFace withSize(Real newSize) {
		
		FontFace newFont = (FontFace)this.duplicate();
		newFont.set(size, newSize);
		return newFont;
		
	}
	
	public String toString() { return "" + get(size) + " pt " + get(family); }
	
}