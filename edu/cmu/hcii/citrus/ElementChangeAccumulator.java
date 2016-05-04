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
package edu.cmu.hcii.citrus;

import java.util.LinkedList;

public class ElementChangeAccumulator extends ElementChangeEvent {

	// Events are recorded reverse-chronologically.
	private final LinkedList<ElementChangeEvent> events = new LinkedList<ElementChangeEvent>();
	
	public ElementChangeAccumulator(Element cause) {
		
		super(cause);
		
	}
	
	public void recordEvent(ElementChangeEvent event) {

		events.addFirst(event);
		
	}
	
	public boolean hasEvents() { return !events.isEmpty(); }
	
	static int blah = 0;
	public void undo() {

		//for(int i = 0; i < blah; i++) System.err.print("\t");
		//System.err.println("Undoing " + elementChanged);
		//blah++;
		
		// Undo in reverse chronological order
		for(ElementChangeEvent event : events) {
		
			//for(int i = 0; i < blah; i++) System.err.print("\t");
			//System.err.println("Undoing " + event);
			event.undo();
			
		}
		
		//blah--;
	
	}
	
	public String toString() {
		
		String result = "" + elementChanged + " changed ";
		return result;

	}
	
}
