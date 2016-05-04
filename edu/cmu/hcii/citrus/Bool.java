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

public class Bool extends BootElement<Bool> {

	// Both set in Boot.java
	public static Bool TRUE, FALSE;
	
	public final boolean value;

	public Bool(boolean newValue) { value = newValue; }

	public Bool not() { return value ? FALSE : TRUE; }
	public Bool and(Bool operand) { return new Bool(value && operand.value); }
	public Bool or(Bool operand) { return new Bool(value || operand.value); }

	public Boolean valueFromString(String s) { 
		
		if(s.equals("true")) return Boolean.TRUE;
		else if(s.equals("false")) return Boolean.FALSE;
		else throw new ElementError("A Boolean must be either \"true\" or \"false\"", this);

	}
	
	public Namespace<?> getType() { return Boot.BOOL; }

	public Bool isEquivalentTo(Element<?> e) { return new Bool(e instanceof Bool && ((Bool)e).value == value); }
	public Element duplicate() { return new Bool(value); }
	
	public Text toCitrus() { return new Text("" + value); }
	public Text toCitrusReference() { return new Text("" + value); }
	public String toString() { return "" + value; }
	public Text toXML() { return toCitrus(); }
	public static Bool valueOf(boolean bool) { return bool ? TRUE : FALSE; }
	
	public Bool evaluate(Element<?> env) { return this; }

}
