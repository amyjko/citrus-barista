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

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

public class MouseButton extends Device {

	public final Event pressed = new Pressed(null, 0, this, null);
	public final Event released = new Released(null, 0, this, null);
	public final Event clicked = new Clicked(null, 0, this, null);
	public final Event doubleClicked = new DoubleClicked(null, 0, this, null);

	public abstract static class MouseButtonEvent extends Event {

		public static final Dec<MouseButton> button = new Dec<MouseButton>((Element)null, true);
		public static final Dec<Point> point = new Dec<Point>();

		public boolean isNegligible() { return false; }

		public MouseButtonEvent(ArgumentList args) { super(args); }
		public MouseButtonEvent(Window newWindow, long newTimeStamp, MouseButton newButton, Point newPoint) {
			
			super(newWindow, newTimeStamp);
			set(button, newButton);
			set(point, newPoint);

		}

	}
	
	public static class Pressed extends MouseButtonEvent {
		
		public Pressed() { this(null); }
		public Pressed(ArgumentList args) { super(args); }
		public Pressed(Window newWindow, long newTimeStamp, MouseButton newButton, Point newPoint) { 
			super(newWindow, newTimeStamp, newButton, newPoint); }

		public void handle() {
			
			get(button).set(down, new Bool(true));
			get(button).getOwnerOfType(Mouse.class).handle(this);
			
		}

	};
	
	public static class Released extends MouseButtonEvent {
		
		public Released() { this(null); }
		public Released(ArgumentList args) { super(args); }
		public Released(Window newWindow, long newTimeStamp, MouseButton newButton, Point newPoint) { 
			super(newWindow, newTimeStamp, newButton, newPoint); }

		public void handle() {
			
			get(button).set(down, new Bool(false));
			get(button).getOwnerOfType(Mouse.class).handle(this);
			
			App.enqueue(new Clicked(window, timeStamp, get(button), get(point)));
			
		}

	};

	public static class Clicked extends MouseButtonEvent {

		public Clicked(ArgumentList args) { super(args); }
		public Clicked(Window newWindow, long newTimeStamp, MouseButton newButton, Point newPoint) { 
			super(newWindow, newTimeStamp, newButton, newPoint); }

		public void handle() {

			boolean handleDouble = !get(button).lastClickHandled;
			long lastClick = get(button).lastClick;
			
			// Remember the time of the last click.
			get(button).lastClick = timeStamp;

			get(button).lastClickHandled = get(button).getOwnerOfType(Mouse.class).handle(this);
			
			// If two clicks have not been handled, and they we're within 500 ms
			if(handleDouble && 
					!get(button).lastClickHandled && 
					timeStamp - lastClick < 200) App.enqueue(new DoubleClicked(window, timeStamp, get(button), get(point)));
			
		}		
		
	}

	public static class DoubleClicked extends MouseButtonEvent {

		public DoubleClicked(ArgumentList args) { super(args); }
		public DoubleClicked(Window newWindow, long newTimeStamp, MouseButton newButton, Point newPoint) { 
			super(newWindow, newTimeStamp, newButton, newPoint); }

		public void handle() {

			get(button).lastClick = 0;
			
			// Handle this event.
			get(button).getOwnerOfType(Mouse.class).handle(this);
			
		}		
		
	}
	
	// Whether the button is down or not.
	public final static Dec<Bool> down = new Dec<Bool>(new Bool(false));
	
	// The time of the last click.
	private long lastClick;
	private boolean lastClickHandled = false;
	
	public MouseButton(Mouse newOwner, String newName) { super(newName); }
	public MouseButton(ArgumentList args) { super(args); }
		
}