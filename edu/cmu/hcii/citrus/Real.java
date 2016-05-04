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

import edu.cmu.hcii.citrus.views.Transition;

public class Real extends BootElement<Real> implements Comparable<Real> {

	public final double value;
	
	public Real(double newValue) { value = newValue; }
	
	public Real evaluate(Element<?> env) { return this; }
	public Namespace<?> getType() { return Boot.REAL; }
	public Element duplicate() { return new Real(value); }
	public String toString() { return "" + value; }
	public Text toCitrus() { return new Text("" + value); }
	public Text toCitrusReference() { return toCitrus(); }

	public Real plus(Real operand) { return new Real(value + operand.value); }
	public Real minus(Real operand) { return new Real(value - operand.value); }
	public Real times(Real operand) { return new Real(value * operand.value); }
	public Real divide(Real operand) { return new Real(value / operand.value); }

	public Bool greaterThan(Real operand) { return new Bool(value > operand.value); }
	public Bool greaterThanOrEqualTo(Real operand) { return new Bool(value >= operand.value); }
	public Bool lessThan(Real operand) { return new Bool(value < operand.value); }

	public Real inc() { return new Real(value + 1.0); }
	public Real dec() { return new Real(value - 1.0); }
	
	public Real min(Real r) { return value < r.value ? this : r; }
	public Real max(Real r) { return value > r.value ? this : r; }
	public Real abs() { return value < 0 ? this : new Real(-value); }
	
	public Real getTransitionalValue(Transition t, Real start, Real end, long time) {

		return new Real(t.value(time, start.value, end.value));
		
	}
	
	public int compareTo(Real i) { return value < i.value ? -1 : value > i.value ? 1 : 0; }

	public Bool isEquivalentTo(Element<?> o) { return new Bool(o instanceof Real && ((Real)o).value == value); }

}