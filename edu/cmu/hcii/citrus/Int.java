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

public class Int extends BootElement<Int> implements Comparable<Int> {

	public final int value;
	
	public Int(int newValue) { value = newValue; }
	
	public Int plus(Int operand) { return new Int(value + operand.value); }
	public Int minus(Int operand) { return new Int(value - operand.value); }
	public Bool equals(Int operand) { return new Bool(value == operand.value); }
	
	public Bool greaterThan(Int operand) { return new Bool(value > operand.value); }
	
	public String toString() { return "" + value; }

	public Bool isEquivalentTo(Element<?> e) { return new Bool(e instanceof Int && ((Int)e).value == value); }
	public Element duplicate() { return new Int(value); }
	public Namespace<?> getType() { return Boot.INT; }
	public Text toCitrus() { return new Text("" + value); }
	public Text toCitrusReference() { return toCitrus(); }
	public Text toXML() { return toCitrus(); }

	public Int evaluate(Element<?> env) { return this; }

	public int compareTo(Int i) { return value < i.value ? -1 : value > i.value ? 1 : 0; }

}
