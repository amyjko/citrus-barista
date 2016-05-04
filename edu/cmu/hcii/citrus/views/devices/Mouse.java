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

import java.awt.Cursor;

import java.util.Hashtable;

import edu.cmu.hcii.citrus.Dec;
import edu.cmu.hcii.citrus.views.*;

public class Mouse extends Device {

	///////////////////////////
	//
	// Cursor types.
	//
	///////////////////////////
	
	public static final int DEFAULT_CURSOR = Cursor.DEFAULT_CURSOR;
	public static final int RESIZE_NS_CURSOR = Cursor.N_RESIZE_CURSOR;
	public static final int RESIZE_EW_CURSOR = Cursor.W_RESIZE_CURSOR;
	public static final int RESIZE_N_CURSOR = Cursor.N_RESIZE_CURSOR;
	public static final int RESIZE_W_CURSOR = Cursor.W_RESIZE_CURSOR;
	public static final int RESIZE_S_CURSOR = Cursor.S_RESIZE_CURSOR;
	public static final int RESIZE_E_CURSOR = Cursor.E_RESIZE_CURSOR;
	public static final int RESIZE_NW_CURSOR = Cursor.NW_RESIZE_CURSOR;
	public static final int RESIZE_SW_CURSOR = Cursor.SW_RESIZE_CURSOR;
	public static final int RESIZE_NE_CURSOR = Cursor.NE_RESIZE_CURSOR;
	public static final int RESIZE_SE_CURSOR = Cursor.SE_RESIZE_CURSOR;
	public static final int HAND_CURSOR = Cursor.HAND_CURSOR;	
	public static final int MOVE_CURSOR = Cursor.MOVE_CURSOR;	
	public static final int TEXT_CURSOR = Cursor.TEXT_CURSOR;	
	public static final int WAIT_CURSOR = Cursor.WAIT_CURSOR;	

	////////////////////////////////////////////////////////////////////////
	//
	// A Mouse has several devices on it, including several buttons,
	// a pointer, and often a mouse wheel.
	//
	////////////////////////////////////////////////////////////////////////

	private Hashtable<Integer,MouseButton> mouseButtonsBySwingMouseButtonCode = new Hashtable<Integer,MouseButton>(3);

	public static final Dec<MousePointer> cursor = new Dec<MousePointer>("(a MousePointer)");
	public static final Dec<MouseButton> left = new Dec<MouseButton>("(a MouseButton name=\"left\")");
	public static final Dec<MouseButton> right = new Dec<MouseButton>("(a MouseButton name=\"right\")");

	public final MouseButton leftButton = get(left);
	public final MouseButton rightButton = get(right);
	public final MouseWheel wheel = new MouseWheel(this, "wheel");
	public final MousePointer pointer = get(cursor);

	public Mouse() {
	
		super("Mouse");

		addDevice(leftButton);
		addDevice(rightButton);
		addDevice(wheel);
		addDevice(pointer);

		// Create a button lookup by the Swing mouse button codes.
		mouseButtonsBySwingMouseButtonCode.put(java.awt.event.MouseEvent.BUTTON1, leftButton);
		mouseButtonsBySwingMouseButtonCode.put(java.awt.event.MouseEvent.BUTTON2, rightButton);

	}
		
	public MouseButton getMouseButtonByCode(int code) {
		
		MouseButton b = mouseButtonsBySwingMouseButtonCode.get(code);
		return b;
		
	}
	
	////////////////////////////////////////////////
	//
	// Common conditions and actions
	//
	////////////////////////////////////////////////		

	public Action setCursorTo(final int cursor) { 
		return new Action() { 
			public boolean evaluate(View t) {
				Window w = t.getWindow();
				if(w != null) w.setCursorTo(cursor); return true; 
			}
		};
	};

	public Action setCursorToDefault = new Action() { public boolean evaluate(View t) { 
		Window w = t.getWindow();
		if(w != null) w.setCursorTo(Mouse.DEFAULT_CURSOR); return true; 
	}};

	// Handle a mouse event.
	public boolean handle(Event e) {

		// If the mouse pointer has a focus, send this event to one of its tiles. If none contain
		// it, send it to the focus itself.
		if(pointer.picked() && pointer.getViewPicked().getWindow() != null) {

			// React to the event until one of the tiles under the handles it.
			boolean handled = false;
			for(View view : App.getViewsUnderCursor())
				if(view.reactTo(e).value) {
					handled = true;
					break;
				}

			// If none of the children handled it, have the focus handle it.
			if(!handled) return pointer.getViewPicked().reactTo(e).value;
			else return true;
			
		}
		// Otherwise, send it to one of the tiles under the cursor.
		else {

			// React to the event until one of the tiles under the handles it.
			for(View view : App.getViewsUnderCursor())
				if(view.reactTo(e).value) return true;
			return false;

		}
		
	}

}