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
import edu.cmu.hcii.citrus.views.widgets.TextField;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

// Always wraps on '\n'.
// "fitToWidth" determines if wrapping occurs at the right edge of the given boundaries
// "bottomEdge" determines the very bottom pixel painted, so that tiles can optionally wrap to the height of the paint.
public class TextPaint extends Paint {

	public static final Dec<TextLayout> layout = new Dec<TextLayout>();

	public TextPaint() {}
	public TextPaint(ArgumentList arguments) { super(arguments); }

	// Text paint ensures that the paint is within the tile's boundaries.
	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		TextLayout lay = get(layout);
		double hanging = lay.real(TextLayout.hangingIndentation);
		FontFace font = lay.get(TextLayout.font);
		
		return new Rectangle(
			(int)l - 1 + (int)(hanging < 0 ? hanging : 0), (int)t - 1, 
			(int)lay.getVisible(TextLayout.rightExtent) + 1,
			(int)lay.getVisible(TextLayout.bottomExtent) + 1); 
		
	}

	// Paint one or more horizontal glyph vectors
	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {
		
		TextLayout textLayout = get(layout);
		
		// Save the context state
		java.awt.Color oldColor = g.getColor();
		AlphaComposite oldComposite = (AlphaComposite)g.getComposite();
		
		// Set the color of the text and the composite.
		java.awt.Color textColor = get(primaryColor).getVisibleColor();
		g.setColor(textColor);
		g.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, oldComposite.getAlpha() * (float)getVisible(alpha)));

		float left = (float)l;
		float top = (float)t;

		boolean drawSelection = false;
		if(v instanceof TextField) {
			Window w = v.getWindow();
			if(w != null && w.isFocusedOn(v))
				drawSelection = true;
		}
		int selectionStart = drawSelection ? v.get(TextField.caretIndex).value : -1;
		int selectionEnd = drawSelection ? v.get(TextField.endIndex).value : -1;
		if(selectionEnd < selectionStart) {
			int temp = selectionStart;
			selectionStart = selectionEnd;
			selectionEnd = temp;
		}
		
		// Paint each glyph vector, left justified.
		int lineStartIndex = 0;
		for(TextLayout.Line line : textLayout) {

			if(drawSelection) {
				boolean containsSelection = true;
				java.awt.Color selectionColor = v.getStyle().get(Style.selectionColor).getVisibleColor();
				int leftIndex, rightIndex;
				// If all of the indices in this line are outside the line, don't paint selection.
				if(line.endIndex < selectionStart || line.startIndex > selectionEnd)
					containsSelection = false;
				// If the selection within a single line, draw it.
				if(line.startIndex <= selectionStart && line.endIndex >= selectionEnd) {
					drawSelection = false;
					leftIndex = selectionStart;
					rightIndex = selectionEnd;
				}
				// If this line is within the boundaries of the selection, select the whole line.
				else if(selectionStart <= line.startIndex && selectionEnd >= line.endIndex) {
					leftIndex = line.startIndex;
					rightIndex = line.endIndex;
					if(selectionEnd == textLayout.get(TextLayout.text).length().value) rightIndex++;
				}
				// If this is the beginning of a line, draw to the end of the line
				else if(line.startIndex <= selectionStart && selectionEnd >= line.endIndex) {				
					leftIndex = selectionStart;
					rightIndex = line.endIndex;
					if(selectionEnd == textLayout.get(TextLayout.text).length().value) rightIndex++;
				}
				// If this is the end of a line, draw from the beginning of the line.
				else if(selectionStart <= line.startIndex && line.endIndex >= selectionEnd) {
					leftIndex = line.startIndex;
					rightIndex = selectionEnd;
					drawSelection = false;
				}
				else {
					leftIndex = 0;
					rightIndex = 0;
					containsSelection = false;
				}
				
				if(containsSelection) {
					int selectionLeft = (int)(left + line.left + line.getPositionOf(new Int(leftIndex)).value);
					int selectionWidth = (int)(left + line.left + line.getPositionOf(new Int(rightIndex)).value - selectionLeft);
					int selectionTop = (int)(top + line.baseline - textLayout.get(TextLayout.font).get(FontFace.ascent).value);
					int selectionHeight = (int)textLayout.get(TextLayout.font).get(FontFace.lineHeight).value;
					g.setColor(selectionColor);
					g.fillRect(selectionLeft, selectionTop, selectionWidth, selectionHeight);
					g.setColor(textColor);
				}
			}

			g.drawGlyphVector(line.glyphVector, left + line.left, top + line.baseline);
			
		}

		// HACK for painting caret and selection paint for text fields.
		if(v instanceof TextField) {
			Window w = v.getWindow();
			if(w != null && w.isFocusedOn(v) && w.bool(Window.paintCaret)) {
				Int index = ((TextField)v).get(TextField.caretIndex);
				paintCaret(g, (int)(left + getCaretLeft(index).value), (int)(top + textLayout.getBaselineOfIndex(index).value));					
			}
		}
		
		// Restore the context state
		g.setComposite(oldComposite);   
		g.setColor(oldColor);
		
	}
	
	private void paintCaret(Graphics2D g, int x, int baseline) {
		
		FontFace f = get(layout).get(TextLayout.font);
		java.awt.Color c = g.getColor();
		g.setColor(java.awt.Color.black);
		g.drawLine(x, (int)(baseline + f.getVisible(FontFace.descent)), x, (int)(baseline - f.getVisible(FontFace.ascent)));
		g.setColor(c);
		
	}
	
	public Real getCaretLeft(Int index) {
		
		return get(layout).getLeftOf(index);
		
	}

	public Real getCaretTop(Int index) { return get(layout).getTopOfLineAt(index); }
	public Real getCaretBottom(Int index) { return get(layout).getBottomOfLineAt(index); }
		
	public int hashCode(View v, double l, double t, double r, double b) {

		TextLayout textLayout = get(layout);
		int code = super.hashCode(v, l, t, r, b);
		code = code * 31 + (new Double(textLayout.getVisible(TextLayout.rightExtent))).hashCode();
		code = code * 31 + (new Double(textLayout.getVisible(TextLayout.bottomExtent))).hashCode();
		code = code * 31 + textLayout.get(TextLayout.text).value.hashCode();
		code = code * 31 + textLayout.get(TextLayout.font).getFont().hashCode();

		if(v instanceof TextField) {
			Window w = v.getWindow();
			code = code * 31 + (new Boolean(w.isFocusedOn(v))).hashCode();
			code = code * 31 + (new Boolean(w.bool(Window.paintCaret))).hashCode();
			code = code * 31 + new Integer(v.integer(TextField.caretIndex)).hashCode();
		}
		
		return code;
	
	}

}