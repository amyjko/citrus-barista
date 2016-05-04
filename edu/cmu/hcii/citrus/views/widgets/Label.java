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
package edu.cmu.hcii.citrus.views.widgets;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.paints.TextPaint;

public class Label extends ElementView {

	public static final Dec<Text> model = new Dec<Text>();
	public static final Dec<Property> property = new Dec<Property>((Element)null, true);
	public static final Dec<Text> text = new Dec<Text>(new Text(""));
	public static final Dec<TextLayout> textLayout = new Dec<TextLayout>("(a LabelTextLayout)");
	
	public static final Dec<List<TextPaint>> content = new Dec<List<TextPaint>>("[(a TextPaint primaryColor=color layout=textLayout)]");
	
	public static final Dec<Real> width = new Dec<Real>(true, "textLayout.rightExtent");
	public static final Dec<Real> height = new Dec<Real>(true, "(textLayout.bottomExtent minus textLayout.font.descent)");

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> focusable = new Dec<Bool>("false");

	public static final Dec<FontFace> font = new Dec<FontFace>("(this getStyle).plainFont");
	public static final Dec<Color> color = new Dec<Color>(Color.black);

	public static class LabelTextLayout extends TextLayout {

		public LabelTextLayout(Namespace type, ArgumentList args) { super(type, args); }
		public static final Dec<FontFace> font = new Dec<FontFace>(true, "enclosing.font");
		public static final Dec<Text> text = new Dec<Text>(true, "enclosing.text");
		
	}

	private Listener computeLayoutOnChange = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {
			if(newValue instanceof Text)
				get(textLayout).layoutCharactersUsing((Text)newValue);
		}
	};

	public Label(ArgumentList arguments) { this(null, arguments); }
	public Label(Namespace subType, ArgumentList arguments) {
	
		super(subType, arguments);	
		getPropertyByDeclaration(text).addListener(computeLayoutOnChange);
		get(textLayout).layoutCharactersUsing(get(text));
		Property p = get(property);
		if(p != null) {
			p.addListener(computeLayoutOnChange);
			if(p.get() != null)
				get(textLayout).layoutCharactersUsing(p.get().toCitrus());
		}
	
	}

	public ElementView updatedViewFor(Property p, Element newValue, Transition t) { return this; }	

	public Real descent() { return get(font).peek(FontFace.descent); }

	public String toString() { return super.toString() + " \"" + get(text) + "\""; }
	
}