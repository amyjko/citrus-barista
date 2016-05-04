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

import java.awt.geom.Point2D;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.devices.*;
import edu.cmu.hcii.citrus.views.paints.*;

public class TextField extends ElementView {

	public static class TextFieldTextLayout extends TextLayout {

		public TextFieldTextLayout(Namespace type, ArgumentList args) { super(type, args); }
		public static final Dec<FontFace> font = new Dec<FontFace>(true, "enclosing.font");
		public static final Dec<Text> text = new Dec<Text>(true, "enclosing.text");
		public static final Dec<Bool> fitToWidth = new Dec<Bool>(true, "enclosing.fitToWidth");		
	}

	public static final Dec<Text> text = new Dec<Text>(new Text(""));
	public static final Dec<TextLayout> textLayout = new Dec<TextLayout>("(a TextFieldTextLayout)");

	public static final Dec<Property> property = new Dec<Property>(new Parameter<Property>());
	public static final Dec<Text> model = new Dec<Text>();

	// The index that the cursor is before. 0 indicates before the first character, 
	// text.length() indicates after the last character.
	public static final Dec<Int> caretIndex = new Dec<Int>(new Int(0));
	public static final Dec<Int> endIndex = new Dec<Int>(new Int(0));
	static {

		caretIndex.set(Dec.isUndoable, Bool.TRUE);
		caretIndex.is(new PropertyRestriction("(caretIndex >= 0)", "0"));
		caretIndex.is(new PropertyRestriction("(caretIndex <= (text length))", "(text length)"));
		endIndex.is(new PropertyRestriction("(endIndex >= 0)", "0"));
		endIndex.is(new PropertyRestriction("(endIndex <= (text length))", "(text length)"));

	}

	// Whether the label has a fixed width or height
	public static final Dec<Bool> fitToWidth = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> fitToHeight = new Dec<Bool>(new Bool(true));
	
	public static final Dec<Color> color = new Dec<Color>(Color.black);

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(
		"[(this getStyle).textBackgroundPaint]");

	public static final Dec<List<TextPaint>> content = new Dec<List<TextPaint>>(
		"[ (a TextPaint layout=textLayout primaryColor=color) ]");

	public static final Dec<FontFace> font = new Dec<FontFace>("(this getStyle).plainFont");

	public static final Dec<Real> width = new Dec<Real>(true, "(80.0 max textLayout.rightExtent)");
	public static final Dec<Real> height = new Dec<Real>(true, "(textLayout.bottomExtent minus font.descent)");

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
	
	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(true));

	public static final Dec<Real> hPad = new Dec<Real>("(this getStyle).textFieldPadding");
	public static final Dec<Real> vPad = new Dec<Real>("(this getStyle).textFieldPadding");
	
	public ElementView updatedViewFor(Property p, Element newValue, Transition t) { return this; }

	// When the model property changes, update the text.
	private Listener<?> modelChanged = new ListenerAdapter() {
		public void outOfDate(Property p, Transition t, Element oldValue) {
			
			set(text, (Text)p.get());
		}
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {
			
			Text newText = new Text("" + newValue);
			set(text, newText);
			
		}
	};

	// When the text property changes, recompute the layout.
	private Listener<Element> computeLayoutOnChange = new ListenerAdapter<Element>() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {
			
			getTextLayout().layoutCharactersUsing((Text)newValue);
			
			// Parse the new text value into an appropriate element.
			Namespace type = get(property).getTypeExpression().getBaseType();

			Element valueToSet = null;

			if(type == Reflection.getJavaType(Text.class)) valueToSet = newValue;
			else if(type == Reflection.getJavaType(Real.class)) valueToSet = new Real(Double.parseDouble(((Text)newValue).value));
			else if(type == Reflection.getJavaType(Char.class)) valueToSet = new Char(((Text)newValue).value.charAt(0));
			else if(type == Reflection.getJavaType(Bool.class)) {
				valueToSet = ((Text)newValue).value.equals("true") ? 
						new Bool(true) : ((Text)newValue).value.equals("false") ? 
								new Bool(false) : new Nothing();
			}

			// Set the property to the parsed value.
			if(valueToSet != null) {
				boolean changed = get(property).set(valueToSet, getStyle().getQuickerTransition());
			}
			
		}
	};

	private static Action hideAction = new Action() { 
		public boolean evaluate(View t) {
			if(App.mouse.pointer.isPicked(t)) App.mouse.pointer.release();
			t.set(hoisted, new Bool(false));
			t.set(hidden, new Bool(true));
			return true; 
		}
	};

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(

		new Behavior(App.mouse.pointer.entered, App.mouse.setCursorTo(Mouse.TEXT_CURSOR)),
		new Behavior(App.mouse.pointer.exited, App.mouse.setCursorTo(Mouse.DEFAULT_CURSOR)),
		new Behavior(App.mouse.leftButton.doubleClicked, false, new Action() { public boolean evaluate(View t) {
			
			t.set(endIndex, new Int(0));
			t.set(caretIndex, t.get(text).length());
			return true;
			
		}}),
		// When this is clicked, focus, and place the cursor in the appropriate position.
		new Behavior(App.mouse.leftButton.pressed, false, new Action() { public boolean evaluate(View t) {

			TextField tf = (TextField)t;
			TextLayout tp = tf.getTextLayout();
			Point2D p = tf.globalToContent(App.mouse.pointer.getPosition());
			Int index = new Int(tp.getCharacterIndexClosestTo(p.getX(), p.getY()));

			if(!tf.getPropertyByDeclaration(caretIndex).willBeSet(index)) {
				return false;
			}

			// Focus on this text field.
			if(!t.getWindow().focusOn(t, "mouse").value)
				return false;

			// Map the x coordinate to the closest caret position.
			t.set(caretIndex, index);
			t.set(endIndex, t.get(caretIndex));

			App.mouse.pointer.pick(t);
			
			return true;

		}}),
		new Behavior(new MousePointer.Dragged(), new Action() { public boolean evaluate(View t) {
			
			Point2D p = t.globalToContent(App.mouse.pointer.getPosition());
			TextLayout tp = t.get(textLayout);
			t.set(caretIndex, new Int(tp.getCharacterIndexClosestTo(p.getX(), p.getY())));
			return true;
			
		}}),		
		new Behavior(new MouseButton.Released(), new Action() { public boolean evaluate(View t) {
			
			App.mouse.pointer.release();
			return true;
			
		}}),
		// When focus is received, check the last focus and position the cursor
		// so that it's optimally placed for navigation.
		new Behavior(App.focusReceived, new Action() { public boolean evaluate(View t) {

			TextField tf = (TextField)t;
			TextLayout tp = tf.get(textLayout);

			String direction = t.getWindow().getDirectionOfLastFocusMovement();
			if(direction.equals("next")) {
				t.set(caretIndex, new Int(0));
				t.set(endIndex, t.get(caretIndex));
			}
			else if(direction.equals("previous")) {
				t.set(caretIndex, tf.get(text).length());
				t.set(endIndex, t.get(caretIndex));
			}
			else if(direction.equals("mouse")) {
				
				Point2D p = tf.globalToContent(App.mouse.pointer.getPosition());	
				t.set(caretIndex, 
						new Int(tp.getCharacterIndexClosestTo(p.getX(), p.getY())));
				t.set(endIndex, t.get(caretIndex));				
				
			}
			else if(direction.equals("above")) {
				
				
			}
			else if(direction.equals("below")) {
				
				
			}
			else if(direction.length() > 0 && Character.isDigit(direction.charAt(0))) {

				int i = 0;
				try {
					i = Integer.parseInt(direction);
				} catch(NumberFormatException e) {
					System.err.println("Warning: direction received in text field for focus was not an integer " + direction);					
				}
				t.set(caretIndex, new Int(i));
				t.set(endIndex, t.get(caretIndex));
				
			}
			else {
				View lastFocus = t.getWindow().getLastFocus();
				if(lastFocus != null && lastFocus.getWindow() != null) {
	
					// Get the local position of the last focus' center.
					Point2D p = tf.globalToContent(lastFocus.getGlobalCenter());	
					
					t.set(caretIndex, new Int(tp.getCharacterIndexClosestTo(p.getX(), p.getY())));
					t.set(endIndex, t.get(caretIndex));
					
				}
			}
			return false;
		
		}}),
		
		new Behavior(App.focusLost, new Action() { public boolean evaluate(View t) {
			View setView = t.get(children).nth(new Int(2));
			if(setView != null) {
				setView.set(hoisted, new Bool(false));
				setView.set(hidden, new Bool(true));
			}
			return false;
		}}),
		
		new Behavior(App.keyboard.typed, true, new Action() { public boolean evaluate(View t) {
			
			TextField tf = (TextField)t;
			if(App.keyboard.COMMAND.isDown() || App.keyboard.CONTROL.isDown() || App.keyboard.ALT.isDown()) return false;
			if(App.keyboard.getLastCharacterTyped().value == '\b')
				return tf.backspaceIfValid().value;
			else return tf.insertIfValid(App.keyboard.getLastCharacterTyped()).value;			

		}}),

		// Handles forward delete
		new Behavior(new Keyboard.Pressed("delete"), new Action() { public boolean evaluate(View t) {
			return ((TextField)t).delete().value;
		}}),

		// Handles the event if the cursor changes.
		new Behavior(new Keyboard.Pressed("right"), new Action() { public boolean evaluate(View t) {
			TextField tf = (TextField)t;
			if(App.keyboard.COMMAND.isDown())
				return t.set(caretIndex, tf.getText().length());
			else return ((TextField)t).moveCursorBy(1);
		}}),

		// Handles the event if the cursor changes.
		new Behavior(new Keyboard.Pressed("left"), new Action() { public boolean evaluate(View t) {			
			if(App.keyboard.COMMAND.isDown())
				return t.set(caretIndex, new Int(0));
			else return ((TextField)t).moveCursorBy(-1);
		}}),

		new Behavior(new Keyboard.Pressed("up"), new Action() { public boolean evaluate(View t) {			
			return ((TextField)t).moveCursorUp().value;
		}}),

		new Behavior(new Keyboard.Pressed("down"), new Action() { public boolean evaluate(View t) {			
			return ((TextField)t).moveCursorDown().value;
		}}),
		
		new Behavior(new App.BlinkCaret(), new Action() { public boolean evaluate(View t) {
			t.postDamage();
			return true;
		}})
		
	));

	private Listener makeCaretVisible = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element o, Element n) {
			((TextField)(p.getElementOwner())).makeCaretVisible();
		}
	};
	
	public void makeCaretVisible() {
		
		TextPaint tp = (TextPaint)get(content).first();
		Point2D caretPosition = getGlobalCenter();
		makeRectangleVisible(caretPosition, caretPosition);
		
	}

	public TextField(ArgumentList arguments) { this(null, arguments); }
	public TextField(Namespace type, ArgumentList arguments) {

		super(type, arguments);
		
		set(text, new Text("" + get(property).get()));

		// Add the text paint to the content
		getPropertyByDeclaration(text).addListener(computeLayoutOnChange);
		get(property).addListener(modelChanged);

		TextLayout tp = get(textLayout);

		// The cursor index must be within the text's boundaries
		set(caretIndex, getText().length());
		getPropertyByDeclaration(text).addListener(notifyApp);		
		getPropertyByDeclaration(caretIndex).addListener(notifyApp);		
		getPropertyByDeclaration(caretIndex).addListener(makeCaretVisible);
		
		tp.layoutCharactersUsing(getText());
		
		// TODO Total hack. This should happen automatically, right?
		get(property).addView(this);
		
	}
	
	public Text getText() { return get(text); }
	public int getCaretIndex() { return get(caretIndex).value; }
	
	public Bool backspaceIfValid() {

		if(integer(caretIndex) == 0) return Bool.FALSE;
		Text newText = get(text).withoutCharacterAt(get(caretIndex));		
		if(!valueWillBeSet(newText)) return Bool.FALSE;		
		else return backspace();
		
	}
	
	// Removes the character just before the cursor index, or the selected text.
	public Bool backspace() {
		
		int index = getCaretIndex();
		if(getText().length().value > 0) {
			// If there's a selection, delete it
			if(get(caretIndex) != get(endIndex)) {
				String txt = get(text).value;
				int leftIndex = Math.min(get(caretIndex).value, get(endIndex).value);
				int rightIndex = Math.max(get(caretIndex).value, get(endIndex).value);
				Text newText = new Text(txt.substring(0, leftIndex) + txt.substring(rightIndex, txt.length()));
				boolean result = set(text, newText, getStyle().getQuickestTransition());
				set(caretIndex, new Int(leftIndex));
				set(endIndex, new Int(leftIndex));
				return new Bool(result);
			}
			else if(index > 0) {
				Text newText = get(text).withoutCharacterAt(get(caretIndex));
				boolean result = set(text, newText, getStyle().getQuickestTransition());
				if(result) {
					set(caretIndex, new Int(index - 1));
					set(endIndex, get(caretIndex));
				}
				return new Bool(result);
			} 
			else return Bool.FALSE;
		} else return Bool.FALSE;
		
	}
	
	public Bool delete() {
		
		int index = getCaretIndex();
		if(getText().length().value > 0) {
			if(index < getText().length().value) {
				Text newText = get(text).withoutCharacterAt(new Int(get(caretIndex).value + 1));		
				if(!valueWillBeSet(newText)) return new Bool(false);		
				if(set(text, newText, getStyle().getQuickestTransition())) {
					return Bool.TRUE;
				} else return Bool.FALSE;
			} else return Bool.FALSE;
		} else return Bool.FALSE;
		
	}
	
	// Inserts the character at the cursor index. For example, if the caret is at 0,
	// a character is insterted at 0.
	public Bool insertIfValid(Char c) {
		
		if(!valueWillBeSet(get(text).withCharacterAt(get(caretIndex), c))) 
			return new Bool(false);
		else return insert(c);
		
	}
	public Bool insert(Char c) {

		if(get(caretIndex) != get(endIndex))
			backspace();
		
		Text newText = get(text).withCharacterAt(get(caretIndex), c);
		boolean success = set(text, newText, getStyle().getQuickestTransition());
		if(success) {
			set(caretIndex, new Int(getCaretIndex() + 1));
			set(endIndex, get(caretIndex));
		}
		return new Bool(success);

	}
	
	public boolean valueWillBeSet(Text newValue) {

		// Parse the new text value into an appropriate element.
		Namespace type = get(property).getTypeExpression().getBaseType();

		Element valueToSet = null;
		if(type == Reflection.getJavaType(Text.class)) valueToSet = newValue;
		else if(type == Reflection.getJavaType(Real.class)) valueToSet = new Real(Double.parseDouble(newValue.value));
		else if(type == Reflection.getJavaType(Bool.class)) 
			valueToSet = newValue.value.equals("true") ? 
					new Bool(true) : newValue.value.equals("false") ? 
							new Bool(false) : new Nothing();

		// Set the property to the parsed value.
		if(valueToSet != null) {
			
			return get(property).willBeSet(valueToSet);
		}
		else return true;
		
	}
	
	public Text textIfCharacterWereInserted(Char c) {
	
		int index = get(caretIndex).value;
		String txt = get(text).value;
		String modifiedText = "";
		if(c.value == '\b') {
			if(index == 0) return null;
			else modifiedText = txt.substring(0, index - 1) + txt.substring(index);
		}
		else modifiedText = txt.substring(0, index) + c + txt.substring(index);
		return new Text(modifiedText);

	}
	
	// Moves the cursor by the specified amount.
	public boolean moveCursorBy(int delta) { 
		
		boolean changed = set(caretIndex, new Int(getCaretIndex() + delta));
		set(endIndex, get(caretIndex));
		return changed;
		
	}

	public Bool moveCursorDown() {

		TextLayout tp = getTextLayout();
		Int index = tp.getIndexAboveOrBelowIndex(get(caretIndex), Bool.FALSE);
		if(index == null) return Bool.FALSE;
		else {
			set(endIndex, index);
			return Bool.valueOf(set(caretIndex, index));
		}
		
	}

	public Bool moveCursorUp() {
		
		TextLayout tp = getTextLayout();
		Int index = tp.getIndexAboveOrBelowIndex(get(caretIndex), Bool.TRUE);
		if(index == null) return Bool.FALSE;
		else {
			set(endIndex, index);
			return Bool.valueOf(set(caretIndex, index));
		}
		
	}

	public Real descent() { return get(font).peek(FontFace.descent); }
	
	public TextLayout getTextLayout() { return get(textLayout); }

	public Real getCaretLeft() { return peek(textLayout).getLeftOf(get(caretIndex)); }
	public Real getCaretBottom() { return peek(textLayout).getBottomOfLineAt(get(caretIndex)); }
	
	// Return the "center" of this tile, as defined by the tile. Text fields, for example,
	// might define their center by their text caret's position.
	public Point2D getGlobalCenter() {
		
		// Where is the text caret?
		TextLayout tp = getTextLayout();
		return localToGlobal(new Point2D.Double(	tp.getLeftOf(get(caretIndex)).value + get(left).value, 
												tp.getLeftOf(get(caretIndex)).value + get(top).value));	
		
	}

	public Real getMaxChildAscent() {
		
		return get(font).peek(FontFace.ascent);
		
	}
	
}