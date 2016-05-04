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

public class MouseWheel extends Device {

	public Event moved = new Moved(null, 0, this, null);
	
	public static class Moved extends Event {

		public static final Dec<MouseWheel> wheel = new Dec<MouseWheel>((Element)null, true);
		public static final Dec<Int> unitsMoved = new Dec<Int>();
		
		public Moved(Window newWindow, long newTimeStamp, MouseWheel newWheel, Int newUnitsMoved) {
			super(newWindow, newTimeStamp);
			set(wheel, newWheel);
			set(unitsMoved, newUnitsMoved);
		}
		
		public void handle() {
		
			get(wheel).set(unitsMoved, get(unitsMoved));
			Mouse mouse = get(wheel).getOwnerOfType(Mouse.class);
			if(mouse != null) mouse.handle(this);
			else System.err.println("Warning: " + wheel + " has no mouse owner");
			
		}

		public boolean isNegligible() { return false; }

	};

	public static final Dec<Int> unitsMoved = new Dec(new Int(0));
	
	private Mouse mouse;

	public MouseWheel(Mouse newOwner, String name) {
		
		super(name);
		mouse = newOwner;

	}
	
	public int getUnitsMoved() { return integer(unitsMoved); }
	
}
