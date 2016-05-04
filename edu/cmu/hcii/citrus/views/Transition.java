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

///////////////////////////////////////////////////////////////
//
// A data-agnostic animation class. Data comes in as objects, 
// and leaves as objects, and this class could care less about its type.
//
///////////////////////////////////////////////////////////////
public abstract class Transition extends BaseElement {
    
	// The time of the start of the transition
	protected long startTime;
	public static final Dec<Real> duration = new Dec<Real>(new Real(150.0));

	// Created by passing a duration. Started by something else.
	public Transition(Namespace type, ArgumentList args) { super(type, args); } 
	public Transition(double newDuration) {
		
		set(duration, new Real(newDuration));
		reset();
		
	}
	
	public void reset() { 
		
		this.startTime = System.currentTimeMillis();
		
	}
	
	// Returns true if the elapsed time is greater than the duration.
	public boolean isComplete(long t) { return (t - startTime) > real(duration); }
	protected double percentElapsed(long t) { return Math.min(1.0, (t - startTime) / real(duration)); }
	
	// The temporal function that determines the transition's behavior.
	public abstract double value(long t, double startValue, double endValue);

}