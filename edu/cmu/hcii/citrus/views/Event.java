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

// Represents a generic input event, consisting of a time stamp, 
// the event that occured, and a record of the event's data.
public class Event extends BaseElement<Event> {
	
	protected Window window;
	protected long timeStamp;
	
	public Event(Namespace type, ArgumentList args) { super(type, args); }
	public Event(ArgumentList args) { super(args); }
	public Event(Window newWindow, long newTimeStamp) {
		
		this.window = newWindow;
		this.timeStamp = newTimeStamp;

	}
	
	public Window getWindow() { return window; }
	public long getTimeStamp() { return timeStamp; }
	
	public boolean isNegligible() { return false; }
	
	// Equivalent if type equivalent
	public Bool isEquivalentTo(Element<?> e) { return getType().is(e.getType()); }
	
	// Subclasses should handle the event instance appropriately.
	public void handle() {}

	public String toString() { return "" + getType() + " @ " + timeStamp; }
	public Text toText() { return new Text(toString()); }
	
}
