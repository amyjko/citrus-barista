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
package edu.cmu.hcii.citrus.views.devices;


import java.util.*;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

public class Keyboard extends Device {

	// Characters
	public final KeyboardKey A = new KeyboardKey(this, "A");
	public final KeyboardKey B = new KeyboardKey(this, "B");
	public final KeyboardKey C = new KeyboardKey(this, "C");
	public final KeyboardKey D = new KeyboardKey(this, "D");
	public final KeyboardKey E = new KeyboardKey(this, "E");
	public final KeyboardKey F = new KeyboardKey(this, "F");
	public final KeyboardKey G = new KeyboardKey(this, "G");
	public final KeyboardKey H = new KeyboardKey(this, "H");
	public final KeyboardKey I = new KeyboardKey(this, "I");
	public final KeyboardKey J = new KeyboardKey(this, "J");
	public final KeyboardKey K = new KeyboardKey(this, "K");
	public final KeyboardKey L = new KeyboardKey(this, "L");
	public final KeyboardKey M = new KeyboardKey(this, "M");
	public final KeyboardKey N = new KeyboardKey(this, "N");
	public final KeyboardKey O = new KeyboardKey(this, "O");
	public final KeyboardKey P = new KeyboardKey(this, "P");
	public final KeyboardKey Q = new KeyboardKey(this, "O");
	public final KeyboardKey R = new KeyboardKey(this, "R");
	public final KeyboardKey S = new KeyboardKey(this, "S");
	public final KeyboardKey T = new KeyboardKey(this, "T");
	public final KeyboardKey U = new KeyboardKey(this, "U");
	public final KeyboardKey V = new KeyboardKey(this, "V");
	public final KeyboardKey W = new KeyboardKey(this, "W");
	public final KeyboardKey X = new KeyboardKey(this, "X");
	public final KeyboardKey Y = new KeyboardKey(this, "Y");
	public final KeyboardKey Z = new KeyboardKey(this, "Z");
	
	// Numbers
	public final KeyboardKey ZERO = new KeyboardKey(this, "0");
	public final KeyboardKey ONE = new KeyboardKey(this, "1");
	public final KeyboardKey TWO = new KeyboardKey(this, "2");
	public final KeyboardKey THREE = new KeyboardKey(this, "3");
	public final KeyboardKey FOUR = new KeyboardKey(this, "4");
	public final KeyboardKey FIVE = new KeyboardKey(this, "5");
	public final KeyboardKey SIX = new KeyboardKey(this, "6");
	public final KeyboardKey SEVEN = new KeyboardKey(this, "7");
	public final KeyboardKey EIGHT = new KeyboardKey(this, "8");
	public final KeyboardKey NINE = new KeyboardKey(this, "9");

	// Numerical operators
	public final KeyboardKey MINUS = new KeyboardKey(this, "-");
	public final KeyboardKey EQUALS = new KeyboardKey(this, "+");
	
	// Function keys
	public final KeyboardKey ESCAPE = new KeyboardKey(this, "escape");
	public final KeyboardKey F1 = new KeyboardKey(this, "F1");
	public final KeyboardKey F2 = new KeyboardKey(this, "F2");
	public final KeyboardKey F3 = new KeyboardKey(this, "F3");
	public final KeyboardKey F4 = new KeyboardKey(this, "F4");
	public final KeyboardKey F5 = new KeyboardKey(this, "F5");
	public final KeyboardKey F6 = new KeyboardKey(this, "F6");
	public final KeyboardKey F7 = new KeyboardKey(this, "F7");
	public final KeyboardKey F8 = new KeyboardKey(this, "F8");
	public final KeyboardKey F9 = new KeyboardKey(this, "F9");
	public final KeyboardKey F10 = new KeyboardKey(this, "F10");
	public final KeyboardKey F11 = new KeyboardKey(this, "F11");
	public final KeyboardKey F12 = new KeyboardKey(this, "F12");
	public final KeyboardKey F13 = new KeyboardKey(this, "F13");
	public final KeyboardKey F14 = new KeyboardKey(this, "F14");
	public final KeyboardKey F15 = new KeyboardKey(this, "F15");
	public final KeyboardKey F16 = new KeyboardKey(this, "F16");
	public final KeyboardKey F17 = new KeyboardKey(this, "F17");
	public final KeyboardKey F18 = new KeyboardKey(this, "F18");
	public final KeyboardKey F19 = new KeyboardKey(this, "F19");
	public final KeyboardKey F20 = new KeyboardKey(this, "F20");
	
	// Directional keys
	public final KeyboardKey UP = new KeyboardKey(this, "up");
	public final KeyboardKey RIGHT = new KeyboardKey(this, "right");
	public final KeyboardKey DOWN = new KeyboardKey(this, "down");
	public final KeyboardKey LEFT = new KeyboardKey(this, "left");
	public final KeyboardKey PAGEUP= new KeyboardKey(this, "page up");
	public final KeyboardKey PAGEDOWN = new KeyboardKey(this, "page down");

	// White space
	public final KeyboardKey DELETE = new KeyboardKey(this, "delete");
	public final KeyboardKey BACKSPACE = new KeyboardKey(this, "backspace");
	public final KeyboardKey TAB = new KeyboardKey(this, "tab");
	public final KeyboardKey SPACE = new KeyboardKey(this, "space");
	public final KeyboardKey ENTER = new KeyboardKey(this, "enter");
	public final KeyboardKey CAPS_LOCK = new KeyboardKey(this, "caps lock");

	// Modifiers
	public final KeyboardKey SHIFT = new KeyboardKey(this, "shift");
	public final KeyboardKey CONTROL = new KeyboardKey(this, "control");
	public final KeyboardKey ALT = new KeyboardKey(this, "alt");
	public final KeyboardKey COMMAND = new KeyboardKey(this, "command");

	// Punctuation
	public final KeyboardKey BACKQUOTE = new KeyboardKey(this, "backquote");
	public final KeyboardKey LEFT_BRACKET = new KeyboardKey(this, "[");
	public final KeyboardKey RIGHT_BRACKET = new KeyboardKey(this, "]");
	public final KeyboardKey BACK_SLASH = new KeyboardKey(this, "\\");
	public final KeyboardKey FORWARD_SLASH = new KeyboardKey(this, "/");
	public final KeyboardKey SEMICOLON = new KeyboardKey(this, ";");
	public final KeyboardKey APOSTROPHE = new KeyboardKey(this, "'");
	public final KeyboardKey COMMA = new KeyboardKey(this, ",");
	public final KeyboardKey PERIOD = new KeyboardKey(this, ".");

	public final KeyboardKey UNKNOWN = new KeyboardKey(this, "UNKNOWN");
	
	private final KeyGroup letters = new KeyGroup(this, "letters");
	private final KeyGroup numbers = new KeyGroup(this, "numbers");
	private final KeyGroup punctuation = new KeyGroup(this, "punctuation");
	private final KeyGroup formatting = new KeyGroup(this, "formatting");
	private final KeyGroup functions = new KeyGroup(this, "functions");
	private final KeyGroup navigation = new KeyGroup(this, "navigation");
	private final KeyGroup modifiers = new KeyGroup(this, "modifiers");
	
	// A lookup table to find mouse buttons by button code
	private Hashtable<Integer,KeyboardKey> keyboardKeysByKeycode = new Hashtable<Integer,KeyboardKey>(120);
	private Hashtable<Text,KeyboardKey> keyboardKeysByName = new Hashtable<Text,KeyboardKey>(120);

	// The last character typed in a typed event.
	public static final Dec<Char> lastChar = new Dec<Char>();

	public final Event typed = new Typed(null, 0, this, null, null, new Bool(false), new Bool(false), new Bool(false));
	
	public static class Typed extends Event {

		public static final Dec<Char> character = new Dec<Char>();
		public static final Dec<Keyboard> keyboard = new Dec<Keyboard>((Element)null, true);
		public static final Dec<Bool> shift = new Dec<Bool>();
		public static final Dec<Bool> control = new Dec<Bool>(new Bool(false));
		public static final Dec<Bool> alt = new Dec<Bool>(new Bool(false));
		public static final Dec<Bool> meta = new Dec<Bool>(new Bool(false));
		
		public Typed(ArgumentList args) { super(args); }
		public Typed() { super(null, 0); }
		public Typed(char c) {
			
			super(null, 0);
			set(character, new Char(c));
			
		}
		public Typed(Window window, long newTimeStamp, Keyboard newKeyboard, 
				Char c, Bool shiftState, Bool controlState, Bool altState, Bool metaState) { 
			
			super(window, newTimeStamp); 
			set(keyboard, newKeyboard);
			set(character, c);
			set(shift, shiftState);
			set(control, controlState);
			set(alt, altState);
			set(meta, metaState);
			
		}

		public void handle() {

			get(keyboard).set(lastChar, get(character));
			if(!get(keyboard).handleKeyboardEvent(window, this)) {

				char c = get(character).value;
				System.err.println("" + (int)c + " wasn't handled");
				if((int)c == 8 || ((int)c >= 32 && (int)c <= 120))
					java.awt.Toolkit.getDefaultToolkit().beep();

			}

		}
		public boolean isNegligible() { return false; }
		public Bool isEquivalentTo(Element<?> e) { 
			
			if(!super.isEquivalentTo(e).value) return new Bool(false);
			
			// If they both specify a character, the characters must be equal
			if(get(control) != null && e.get(control) != null && get(control).value != e.get(control).value) return new Bool(false); 
			if(get(shift) != null && e.get(shift) != null && get(shift).value != e.get(shift).value) return new Bool(false); 
			if(get(alt) != null && e.get(alt) != null && get(alt).value != e.get(alt).value) return new Bool(false); 
			if(get(meta) != null && e.get(meta) != null && get(meta).value != e.get(meta).value) return new Bool(false); 

			if(get(character) != null && e.get(character) != null)
				return get(character).isEquivalentTo(e.get(character));
			else return new Bool(true);
			
		}
		
		public Text toText() { return new Text(toString()); }
		public String toString() {
			
			String result = "";
			if(get(control) != null && ((Bool)get(control)).value) result = result + "control+";//"^";
			if(get(shift) != null && ((Bool)get(shift)).value) result = result + "shift+";//\u21E7";
			if(get(alt) != null && ((Bool)get(alt)).value) result = result + "alt+";//"\u2325";
			if(get(meta) != null && ((Bool)get(meta)).value) result = result + "command+";//"\u2318";

			if(get(character) == null) result = result + "any";
			else result = result + get(character).value;
			return result;
			
		}
		
	}
	
	public static class KeyboardKeyEvent extends Event {
	
		public static final Dec<Text> key = new Dec<Text>((Element)null, true);
		public static final Dec<Bool> shift = new Dec<Bool>();
		public static final Dec<Bool> control = new Dec<Bool>();
		public static final Dec<Bool> alt = new Dec<Bool>();
		public static final Dec<Bool> meta = new Dec<Bool>();
	
		public KeyboardKeyEvent(ArgumentList args) { super(args); }
		public KeyboardKeyEvent(Window window, long timeStamp, Text newKey,
				 Bool shiftState, Bool controlState, Bool altState, Bool metaState) { 
			super(window, timeStamp); 
			set(key, newKey); 
			set(shift, shiftState);
			set(control, controlState);
			set(alt, altState);
			set(meta, metaState);			
		}
	
		public boolean isNegligible() { return false; }
		public Bool isEquivalentTo(Element<?> e) { 
			
			if(!super.isEquivalentTo(e).value) return new Bool(false);
			if(get(key) != null && e.get(key) != null && !get(key).isEquivalentTo(e.get(key)).value) return new Bool(false);
			if(get(control) != null && e.get(control) != null && get(control).value != e.get(control).value) return new Bool(false); 
			if(get(shift) != null && e.get(shift) != null && get(shift).value != e.get(shift).value) return new Bool(false); 
			if(get(alt) != null && e.get(alt) != null && get(alt).value != e.get(alt).value) return new Bool(false); 
			if(get(meta) != null && e.get(meta) != null && get(meta).value != e.get(meta).value) return new Bool(false); 			
			return new Bool(true);	
		}
		
		public Text toText() { return new Text(toString()); }
		public String toString() {
			
			String result = "";
			if(get(control) != null && ((Bool)get(control)).value) result = result + "control+";//"^";
			if(get(shift) != null && ((Bool)get(shift)).value) result = result + "shift+";//\u21E7";
			if(get(alt) != null && ((Bool)get(alt)).value) result = result + "alt+";//"\u2325";
			if(get(meta) != null && ((Bool)get(meta)).value) result = result + "command+";//"\u2318";

			result = result + get(key);
			return result;
			
		}
		
	}

	public static class Pressed extends KeyboardKeyEvent {
		
		public Pressed(ArgumentList args) { super(args); }
		public Pressed(String newKey) { this(null, 0, new Text(newKey), null, null, null, null); }
		public Pressed(Window window, long timeStamp, Text newKey,
				 Bool shiftState, Bool controlState, Bool altState, Bool metaState) { 
			super(window, timeStamp, newKey, shiftState, controlState, altState, metaState); 
		}
		
		public void handle() {
			
			KeyboardKey keyboardKey = App.keyboard.getKeyNamed(get(key));
			if(keyboardKey == null) return;
			keyboardKey.set(KeyboardKey.down, new Bool(true));
			Keyboard own = keyboardKey.getOwnerOfType(Keyboard.class);
			if(own != null)
				own.handleKeyboardEvent(window, this);

		}

	}

	public static class Released extends KeyboardKeyEvent {
		
		public Released(ArgumentList args) { super(args); }
		public Released(Window window, long timeStamp, Text newKey,
				 Bool shiftState, Bool controlState, Bool altState, Bool metaState) { 
			super(window, timeStamp, newKey, shiftState, controlState, altState, metaState); 
		}
		public void handle() { 
			
			KeyboardKey keyboardKey = App.keyboard.getKeyNamed(get(key));
			if(keyboardKey == null) return;
			keyboardKey.set(KeyboardKey.down, new Bool(false));
			keyboardKey.getOwnerOfType(Keyboard.class).handleKeyboardEvent(window, this);

		}
				
	}

	public Keyboard() {
		
		super("Keyboard");
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_A, A);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_B, B);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_C, C);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_D, D);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_E, E);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F, F);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_G, G);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_H, H);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_I, I);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_J, J);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_K, K);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_L, L);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_M, M);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_N, N);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_O, O);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_P, P);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_Q, Q);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_R, R);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_S, S);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_T, T);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_U, U);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_V, V);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_W, W);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_X, X);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_Y, Y);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_Z, Z);

		letters.addDevice(A);
		letters.addDevice(B);
		letters.addDevice(C);
		letters.addDevice(D);
		letters.addDevice(E);
		letters.addDevice(F);
		letters.addDevice(G);
		letters.addDevice(H);
		letters.addDevice(I);
		letters.addDevice(J);
		letters.addDevice(K);
		letters.addDevice(L);
		letters.addDevice(M);
		letters.addDevice(N);
		letters.addDevice(O);
		letters.addDevice(P);
		letters.addDevice(Q);
		letters.addDevice(R);
		letters.addDevice(S);
		letters.addDevice(T);
		letters.addDevice(U);
		letters.addDevice(V);
		letters.addDevice(W);
		letters.addDevice(X);
		letters.addDevice(Y);
		letters.addDevice(Z);
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_0, ZERO);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_1, ONE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_2, TWO);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_3, THREE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_4, FOUR);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_5, FIVE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_6, SIX);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_7, SEVEN);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_8, EIGHT);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_9, NINE);

		numbers.addDevice(ZERO);
		numbers.addDevice(ONE);
		numbers.addDevice(TWO);
		numbers.addDevice(THREE);
		numbers.addDevice(FOUR);
		numbers.addDevice(FIVE);
		numbers.addDevice(SIX);
		numbers.addDevice(SEVEN);
		numbers.addDevice(EIGHT);
		numbers.addDevice(NINE);
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_MINUS, MINUS);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_EQUALS, EQUALS);
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_ESCAPE, ESCAPE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F1, F1);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F2, F2);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F3, F3);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F4, F4);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F5, F5);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F6, F6);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F7, F7);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F8, F8);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F9, F9);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F10, F10);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F11, F11);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F12, F12);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F13, F13);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F14, F14);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F15, F15);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F16, F16);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F17, F17);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F18, F19);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F19, F19);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_F20, F20);

		functions.addDevice(ESCAPE);
		functions.addDevice(F1);
		functions.addDevice(F2);
		functions.addDevice(F3);
		functions.addDevice(F4);
		functions.addDevice(F5);
		functions.addDevice(F6);
		functions.addDevice(F7);
		functions.addDevice(F8);
		functions.addDevice(F9);
		functions.addDevice(F10);
		functions.addDevice(F11);
		functions.addDevice(F12);
		functions.addDevice(F13);
		functions.addDevice(F14);
		functions.addDevice(F15);
		functions.addDevice(F16);
		functions.addDevice(F17);
		functions.addDevice(F18);
		functions.addDevice(F19);
		functions.addDevice(F20);
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_UP, UP);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_DOWN, DOWN);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_LEFT, LEFT);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_RIGHT, RIGHT);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_PAGE_UP, PAGEUP);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_PAGE_DOWN, PAGEDOWN);

		navigation.addDevice(UP);
		navigation.addDevice(DOWN);
		navigation.addDevice(LEFT);
		navigation.addDevice(RIGHT);
		navigation.addDevice(PAGEUP);
		navigation.addDevice(PAGEDOWN);
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_TAB, TAB);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_SPACE, SPACE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_ENTER, ENTER);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_CAPS_LOCK, CAPS_LOCK);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_DELETE, DELETE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_BACK_SPACE, BACKSPACE);

		formatting.addDevice(TAB);
		formatting.addDevice(SPACE);
		formatting.addDevice(ENTER);
		formatting.addDevice(CAPS_LOCK);
		formatting.addDevice(DELETE);
		formatting.addDevice(BACKSPACE);
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_SHIFT, SHIFT);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_CONTROL, CONTROL);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_ALT, ALT);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_META, COMMAND);

		modifiers.addDevice(SHIFT);
		modifiers.addDevice(CONTROL);
		modifiers.addDevice(ALT);
		modifiers.addDevice(COMMAND);
		
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_BACK_QUOTE, BACKQUOTE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_OPEN_BRACKET, LEFT_BRACKET);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_CLOSE_BRACKET, RIGHT_BRACKET);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_BACK_SLASH, BACK_SLASH);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_SLASH, FORWARD_SLASH);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_SEMICOLON, SEMICOLON);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_QUOTE, APOSTROPHE);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_COMMA, COMMA);
		keyboardKeysByKeycode.put(java.awt.event.KeyEvent.VK_PERIOD, PERIOD);

		punctuation.addDevice(BACKQUOTE);
		punctuation.addDevice(LEFT_BRACKET);
		punctuation.addDevice(RIGHT_BRACKET);
		punctuation.addDevice(BACK_SLASH);
		punctuation.addDevice(FORWARD_SLASH);
		punctuation.addDevice(SEMICOLON);
		punctuation.addDevice(APOSTROPHE);
		punctuation.addDevice(COMMA);
		punctuation.addDevice(PERIOD);

		addDevice(letters);
		addDevice(numbers);
		addDevice(functions);
		addDevice(navigation);
		addDevice(formatting);
		addDevice(modifiers);
		addDevice(punctuation);
		
		for(KeyboardKey key : keyboardKeysByKeycode.values()) {
			addDevice(key);
			keyboardKeysByName.put(key.get(Device.name), key);
		}
		
		addDevice(UNKNOWN);
		
	}
	
	// Lookup a keyboard by by a Swing keycode.
	public KeyboardKey getKeyboardKeyByKeycode(int code) {
		
		KeyboardKey k = keyboardKeysByKeycode.get(code);
		if(k != null) return k;
		else {
			System.err.println("Couldn't find a keyboard key with code " + code);
			return UNKNOWN;
		}
		
	}
	
	public KeyboardKey getKeyNamed(Text name) { return keyboardKeysByName.get(name); }
	
	public Iterator getKeyboardKeys() { return keyboardKeysByKeycode.values().iterator(); }

	////////////////////////////////////////////////////////////////////////
	//
	// Common conditions and actions
	//
	////////////////////////////////////////////////////////////////////////

	public final Action focusBelow = new Action() { public boolean evaluate(View t) {
		View below = t.getWindow().getFocus().getFirstFocusableViewInRowBelow();
		if(below != null) t.getWindow().focusOn(below, "below");
		return true;
	}};

	public final Action focusAbove = new Action() { public boolean evaluate(View t) {
		View above = t.getWindow().getFocus().getLastFocusableViewInRowAbove();
		if(above != null) t.getWindow().focusOn(above, "above");
		return true;
	}};

	public final Action addFocusPaint = new Action() { public boolean evaluate(View t) {
		if(!t.get(View.foreground).contains(App.getGlobalStyle().get(Style.focusPaint)).value)
				t.addForegroundPaint(App.getGlobalStyle().get(Style.focusPaint));
		return false;
	}};
	
	public final Action removeFocusPaint = new Action() { public boolean evaluate(View t) {
		t.removeForegroundPaint(App.getGlobalStyle().get(Style.focusPaint));			
		return false;
	}};
		
	// Returns true if the event was handled.
	public boolean handleKeyboardEvent(Window window, Event event) {

		window.set(Window.paintCaret, new Bool(true));
		
		// If we're focused on a tile that's in a window, then have it handle it.
		// Otherwise, let the window handle it.
		View handler = App.windowInFocus.getFocus();
		if(handler == null) handler = App.windowInFocus;
		
		// Search up the tile hierarchy until something handles the event.
		while(handler != null && !handler.reactTo(event).value) {
			handler = handler.getParent();
		}
		
		return handler != null;
		
	}
	
	public Char getLastCharacterTyped() { return get(lastChar); }
	
}