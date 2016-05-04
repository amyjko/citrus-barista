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

import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.util.Iterator;

import edu.cmu.hcii.citrus.*;

public class TextLayout extends BaseElement<TextLayout> implements Iterable<TextLayout.Line> {

    public static final Text LEFT_JUSTIFIED = new Text("left-justified");
    public static final Text CENTERED = new Text("centered");
    public static final Text RIGHT_JUSTIFIED = new Text("right-justified");

	public static final Dec<Bool> fitToWidth = new Dec<Bool>(new Bool(false));
	public static final Dec<Text> horizontalAlignment = new Dec<Text>(LEFT_JUSTIFIED);
	public static final Dec<Real> hangingIndentation = new Dec<Real>(new Real(0.0));
	// Store the right and bottom edges of the text layout, so that tiles can constrain to the text
	// layout if they wish.
	public static final Dec<Real> rightExtent = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> bottomExtent = new Dec<Real>(new Real(0.0));

	public static final Dec<FontFace> font = new Dec<FontFace>(new Parameter());
	public static final Dec<Text> text = new Dec<Text>(new Text(""));
	
	private double leftEdgeToStartAt, rightEdgeToWrapAt;
	private boolean wrappedToRightEdge;
	
	// This is a linked list of lines, which store a glyph vector for each line, based on a substring
	// of the text property, and the index of the end of the line.
	private Line lines;
	private int lineCount = 0;

	private static Listener<?> updateLayout = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {
			
			((TextLayout)((Property<?>)p).getElementOwner()).doLayout();
			
		}
	};

	public TextLayout(Namespace type, ArgumentList args) { 
		
		super(type, args); 

		getPropertyByDeclaration(font).addListener(updateLayout);
		getPropertyByDeclaration(text).addListener(updateLayout);
		
		leftEdgeToStartAt = 0;
		rightEdgeToWrapAt = 0;

		// Compute the initial layout.
		layoutCharactersUsing(get(text)); 

	}

	// When a label's primitive string changes, iterates through the string buffer, tracking the 
	// width and height of the text, and marking the character indices to wrap at.
	// TODO: Since layout progresses from left to right, this should take a start index, so that 
	// there's some idea of what part of the layout doesn't need to be redone.
	public Nothing updateRightEdge(Real edge) {
		rightEdgeToWrapAt = edge.value;
		doLayout();
		return null;
	}
	public Nothing doLayout() { layoutCharactersUsing(get(text)); return null; }
	public void layoutCharactersUsing(Text characters) {
		
		set(text, characters);
		
		wrappedToRightEdge = bool(fitToWidth);

		FontFace font = get(TextLayout.font);
		
		// Erase the old line list so the vectors can be garbage collected.
		for(Line line = lines; line != null; line = line.nextLine) line.glyphVector = null;

		lines = null;
		Line lastLine = null;
		lineCount = 0;
		
		// Reset the right and bottom edges.
		double rightEdge = 0, bottomEdge = 0;
		
		// Iterate through each character in the buffer, tracking the index of any spaces and
		// the width of the current line.
		char c;
		int indexOfLastDelimiter = -1;
		double rightEdgeOfCurrentLine = leftEdgeToStartAt;

		// Loop until we reach the last character...
		for(int charIndex = 0; charIndex < characters.length().value; charIndex++) {
			
			c = characters.charAt(charIndex);
			
			// Advance the width of the line by the width of the character, unless its a carriage return.
			if(c != '\n') rightEdgeOfCurrentLine += font.getCharWidth(c);
			
			// Is the width of this line greater than the current right edge?
			if(rightEdgeOfCurrentLine > rightEdge) rightEdge = rightEdgeOfCurrentLine;

			// If this is a space, remember its index
			if(c == ' ') indexOfLastDelimiter = charIndex;

			// A flag to check if a line needs to be added.
			boolean addALine = false;

			// If this character is a new line, terminate this line at this character.
			if(c == '\n') addALine = true;
			// We're fitting to the tile's right edge and we're past it, then terminate this
			// line at the last ' ' character.
			else if(wrappedToRightEdge && rightEdgeOfCurrentLine > rightEdgeToWrapAt && indexOfLastDelimiter != -1) {
				
				addALine = true;
				charIndex = indexOfLastDelimiter;
			
			}

			// Add a line, if asked to.
			if(addALine) {
				
				// Add a line ending at this newline.
				lastLine = addLine(characters, charIndex, lastLine, font);

				// Reset the width of the line to 0.
				rightEdgeOfCurrentLine = leftEdgeToStartAt;
				
				// Increment the bottom by a line height.
				bottomEdge += font.getAscent() + font.getDescent();
				
				// Unset the flag
				addALine = false;

				// There is no last space anymore
				indexOfLastDelimiter = -1;
				
			}
			
		}

		// Add the last line and increment the bottom one last time.
		addLine(characters, characters.length().value - 1, lastLine, font);
		bottomEdge += font.getAscent() + font.getDescent();
		
		// Update the right and bottom edges
		set(rightExtent, new Real(rightEdge), App.getGlobalStyle().getQuickestTransition());
		if(bottomEdge == 0) set(bottomExtent, new Real(font.getAscent() + font.getDescent()));
		else set(bottomExtent, new Real(bottomEdge));

		applyAlignments();
		
	}
		
	// Adds a line to the line list and returns the new last line.
	private Line addLine(Text characters, int endIndex, Line lastLine, FontFace font) {
	
		lineCount++;
		
		// If there are no lines yet, add a new line starting at zero
		if(lastLine == null) {

			String text = characters.value.substring(0, endIndex + 1);
			lines = new Line(text, 0, endIndex, font);
			return lines;
		
		}
		// Otherwise, add the next line to the previous line
		else {

			String text = characters.value.substring(lastLine.endIndex + 1, endIndex + 1);
			lastLine.nextLine = new Line(text, lastLine.endIndex + 1, endIndex, font); 
			return lastLine.nextLine;
			
		}
		
	}

	private void applyAlignments() {
		
		FontFace f = get(font);

		double ascent = f.getVisible(FontFace.ascent);
		double descent = f.getVisible(FontFace.descent);

		float left = 0;
		float top = 0;
		
		// Start at the leading minus the decent.
		float baseline = (float)(top + ascent - 1);

		// Get the alignment state.
		Text hAlignString = get(horizontalAlignment);
		int hAlign = hAlignString.equals(LEFT_JUSTIFIED) ? -1 : hAlignString.equals(CENTERED) ? 0 : 1;

		double hanging = real(hangingIndentation);
		
		double maxRight = real(rightExtent);
		
		int lineStartIndex = 0;
		for(Line line = lines; line != null; lineStartIndex = line.endIndex + 1, line = line.nextLine) {

			if(hAlign < 0) {
				line.left = left;
				line.baseline = baseline;
			}
			else if(hAlign > 0) {
				line.left = (float)(real(rightExtent) - line.getWidth());
				line.baseline = baseline;
			}
			else {
				line.left = (float)(left + ((real(rightExtent) - left) - line.getWidth()) / 2);
				line.baseline = baseline;
			}

			// If this isn't the first line, add the hanging indentation
			if(line != lines) {
				line.left += hanging;
			}

			// Increment the baseline by the line height.
			baseline += ascent + descent;
			
			double right = line.left + line.getWidth();
			if(right > maxRight) maxRight = right;

		}
		
		set(rightExtent, new Real(maxRight));
		
	}
	
	private Line getLineThatContainsIndex(Int index) {
		
		Line currentLine = lines;
		Line lastLine = null;
		while(currentLine != null && !currentLine.contains(index.value)) {
			lastLine = currentLine;
			currentLine = currentLine.nextLine;
		}
		if(currentLine == null) return lastLine;
		else return currentLine;
		
	}
	
	public Int getIndexAboveOrBelowIndex(Int index, Bool above) {
		
		Line currentLine = lines;
		Line lineAbove = null;
		Line lineBelow = null;
		while(currentLine != null && !currentLine.contains(index.value)) {
			lineAbove = currentLine;
			currentLine = currentLine.nextLine;
		}
		if(currentLine == null) return null;
		else lineBelow = currentLine.nextLine;

		if(above.value) {
			if(lineAbove == null) return null;
			else return lineAbove.getIndexClosestTo(currentLine.getPositionOf(index));
		}
		else {
			if(lineBelow == null) return null;
			else return lineBelow.getIndexClosestTo(currentLine.getPositionOf(index));
		}
		
	}
	
	// Return the character closest to the given local point. Continues until glyph distances worsen.
	public int getCharacterIndexClosestTo(double x, double y) {
		
		FontFace f = get(font);

		if(lines == null) return 0;
		
		double closestDistance = Double.POSITIVE_INFINITY;
		int bestIndex = 0;

		// Find the closest line
		Line previousLine = lines;
		Line bestLine = lines;
		for(; bestLine != null; 
			previousLine = bestLine, 
			bestLine = bestLine.nextLine) {
			
			double testDistance = Math.abs(bestLine.baseline - y);
			if(testDistance > closestDistance) break;
			else closestDistance = testDistance;

		}
		
		bestLine = previousLine;

		// Iterate through the glyphs until we find the best character index.
		// Note that the closest horizontal distance is set to the x position passed in.
		closestDistance = x;
		GlyphVector gv = bestLine.glyphVector;
		double lastX = 0;
		for(int i = 0; i <= gv.getNumGlyphs(); i++) {
		
			Point2D p = gv.getGlyphPosition(i);
			double testDistance = Math.abs(lastX - x);			
			if(testDistance > closestDistance) return bestLine.endIndex - gv.getNumGlyphs() + i - 1;
			else closestDistance = testDistance;
			lastX = p.getX();
			
		}

		// If the last one is closest, return the last index;
		if(bestLine.nextLine == null && Math.abs(gv.getGlyphPosition(gv.getNumGlyphs()).getX() - x) < closestDistance) 
			return bestLine.endIndex + 1;
		else return bestLine.endIndex;
		
	}	

	public Real getBaselineOfIndex(Int index) {
		
		double ascent = get(font).getVisible(FontFace.ascent);
		double descent = get(font).getVisible(FontFace.descent);
		double baseline = ascent - 1;
		Line currentLine = lines;
		while(currentLine != null && !currentLine.contains(index.value)) {
			currentLine = currentLine.nextLine;
			baseline += ascent + descent;
		}
		if(currentLine == null) baseline -= ascent + descent;
		return new Real(baseline);
		
	}
	
	public Real getLeftOf(Int index) {

		Line line = getLineThatContainsIndex(index);
		return line.getPositionOf(index);

	}

	public Real getTopOfLineAt(Int index) {

		return new Real(getBaselineOfIndex(index).value - get(font).getVisible(FontFace.ascent)); 

	}


	public Real getBottomOfLineAt(Int index) {
		
		return new Real(getBaselineOfIndex(index).value + get(font).getVisible(FontFace.ascent));
		
	}

	public double getVisible(Dec<Real> declaration) { 
		
		return getPropertyByDeclaration(declaration).getVisible().value;
		
	}

	public Iterator<Line> iterator() {
		
		return new LineIterator(lines);
	}
	
	private static class LineIterator implements Iterator<Line> {
		
		private Line line;
		
		public LineIterator(Line newLines) { line = newLines; }

		public boolean hasNext() { return line != null; }
		public Line next() { 

			Line temp = line;
			line = line.nextLine;
			return temp;
			
		}
		public void remove() { return; }
		
	}

	// Represents a glyph vector based a substring of the layout's text.
	public static class Line {

		public int startIndex;
		public int endIndex;
		public GlyphVector glyphVector;
		public Line nextLine;
		public float left;
		public float baseline;
		
		public Line(String text, int newStartIndex, int newEndIndex, FontFace f) {

			this.startIndex = newStartIndex;
			this.endIndex = newEndIndex;			
			glyphVector = f.getFont().createGlyphVector(App.getGraphics().getFontRenderContext(), text);
			nextLine = null;
			
		}
		
		public double getWidth() {
			
			return glyphVector.getGlyphPosition(glyphVector.getNumGlyphs()).getX();
			
		}
		
		public boolean contains(int index) {
			
			return index >= startIndex && index <= endIndex;
			
		}
		
		public Real getPositionOf(Int index) {
			
			try {
				return new Real(glyphVector.getGlyphPosition(index.value - startIndex).getX() + left);
			} catch(ArrayIndexOutOfBoundsException e) { return new Real(0.0); }

		}

		public Int getIndexClosestTo(Real position) {
			
			double smallestDistance = Double.MAX_VALUE;
			for(int i = 0; i <= glyphVector.getNumGlyphs(); i++) {
				
				Point2D p = glyphVector.getGlyphPosition(i);
				double testDistance = Math.abs(position.value - (p.getX() + left));			
				if(testDistance < smallestDistance) smallestDistance = testDistance;
				else return new Int(startIndex + i - 1);
				
			}
			return new Int(endIndex);
			
		}
		
		public String toString() { return "Line(" + startIndex + "->" + endIndex; }

	}

}