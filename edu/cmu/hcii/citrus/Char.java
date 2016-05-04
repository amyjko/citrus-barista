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

public class Char extends BootElement<Char> {

	public final char value;

	public Char(char newValue) { value = newValue; }

	public Boolean valueFromString(String s) { 
		
		throw new ElementError("Not parsing chars yet.", this);

	}
	
	public Namespace<?> getType() { return Boot.CHAR; }

	public Bool isEquivalentTo(Element<?> e) { return new Bool(e.getType() == getType() && ((Char)e).value == value); }
	public Element duplicate() { return new Char(value); }
	
	public Text toCitrus() { return new Text("" + value); }
	public Text toCitrusReference() { return toCitrus(); }
	public String toString() { return "`" + value + "`"; }
	public Text toText() { return toCitrus(); }
	public Text toXML() { return toCitrus(); }
	public Bool isALetter() { return new Bool(Character.isLetter(value)); }	


	public Bool isWhitespace() { return new Bool(Character.isWhitespace(value)); }
	public Bool isLetter() { return new Bool(Character.isLetter(value)); }
	public Bool isDigit() { return new Bool(Character.isDigit(value)); }
	public Bool isVowel() { 
		return new Bool(value == 'a' || value == 'A' ||
						value == 'e' || value == 'E' ||
						value == 'i' || value == 'I' ||
						value == 'o' || value == 'O' ||
						value == 'u' || value == 'U'); 
	}
	
	public Char evaluate(Element<?> env) { return this; }

}