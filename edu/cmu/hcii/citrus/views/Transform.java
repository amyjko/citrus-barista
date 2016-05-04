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

import java.awt.geom.AffineTransform;

import edu.cmu.hcii.citrus.*;

public class Transform extends BootElement<Transform> {

	public final AffineTransform value;

	public Transform() { value = new AffineTransform(); }
	public Transform(AffineTransform at) { value = at; }
	
	public Transform evaluate(Element<?> env) { return this; }
	
	public Transform duplicate() { return new Transform(value); }

	public Text toCitrus() { return new Text(value.toString()); }
	public String toString() { return value.toString(); }

	// Returns a linear interpolation between the start and end transforms.
	public Transform getTransitionalValue(Transition t, Transform start, Transform end, long currentTime) {

		if(start == null) {
			if(end == null) return null;
			else {
				System.err.println("Animating from nothing to " + end);
				return end;
			}
		
		}
		else if(end == null) {
			System.err.println("Animating from " + start + " to nothing");			
			return start;
		}

		// Recover the attributes of the start transform.
		double startScaleX = start.value.getScaleX();
		double startScaleY = start.value.getScaleY();
		double startTranslateX = start.value.getTranslateX();
		double startTranslateY = start.value.getTranslateY();

		// Recover the attributes of the end transform.
		double endScaleX = end.value.getScaleX();
		double endScaleY = end.value.getScaleY();
		double endTranslateX = end.value.getTranslateX();
		double endTranslateY = end.value.getTranslateY();
		
		// Find the transitional values using the transition.
		double scaleX = t.value(currentTime, startScaleX, endScaleX);
		double scaleY = t.value(currentTime, startScaleY, endScaleY);
		double translateX = t.value(currentTime, startTranslateX, endTranslateX);
		double translateY = t.value(currentTime, startTranslateY, endTranslateY);

		return new Transform(new AffineTransform(scaleX, 0, 0, scaleY, translateX, translateY));
		
	}
	
	public Bool isEquivalentTo(Element<?> o) { return new Bool(o instanceof Transform && ((Transform)o).value.equals(value)); }
	
}
