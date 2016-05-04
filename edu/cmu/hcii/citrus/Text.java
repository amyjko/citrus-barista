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

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text extends BootElement<Text> {

	public final String value;
	
	public Text(String newValue) { value = newValue; }
	
	public Text evaluate(Element<?> env) { return this; }
	public String toString() { return value; }
	public Text duplicate() { return new Text(value); }	
	
	public Bool isEquivalentTo(Element<?> e) { return new Bool((e.getType() == getType()) && ((Text)e).value.equals(value)); }

	// We use the string's equals and hashcode
	public boolean equals(Object e) { return (e instanceof Text) && ((Text)e).value.equals(value); }
	public int hashCode() { return value.hashCode(); }

	public Text toCitrus() { return this; }
	public Text toCitrusReference() { return convertToLiteral(); }
	public Text toXML() { return convertToLiteral(); }
	public Text convertToLiteral() {

		String tok = value;
		tok = tok.replace("\"", "\"");
		tok = tok.replace("\n", "\n");
		tok = tok.replace("\t", "\t");
		tok = tok.replace("\b", "\b");
		return new Text("\"" + tok + "\"");
		
	}
	
	public Namespace<?> getType() { return Boot.TEXT; }

	// Text-specific functions
	public Int length() { return new Int(value.length()); }	
	public char charAt(int index) { return value.charAt(index); }
	public Char charAt(Int index) { return new Char(value.charAt(index.value)); }
	public Bool isEmpty() { return new Bool(value.equals("")); }

	// Caches compiled patterns for SPEEDY matching.
	public static Hashtable<String,Matcher> patterns = new Hashtable<String,Matcher>();
	public Bool matches(Text regularExpression) { 

		Matcher pattern = patterns.get(regularExpression.value);
		if(pattern == null) {
			pattern = Pattern.compile(regularExpression.value).matcher("");
			patterns.put(regularExpression.value, pattern);
		}
		
		pattern.reset(value);
		return pattern.matches() ? new Bool(true) : new Bool(false);		
	
	}

	public Bool contains(Char c) { return new Bool(value.indexOf(c.value) >= 0); }
	public Text trim() { return new Text(value.trim()); }
	
	public List<Char> toCharList() {
		
		List<Char> charList = new List<Char>();
		char[] chars = value.toCharArray();
		for(char c : chars)
			charList.append(new Char(c));
		return charList;
		
	}
	
	public Bool startsWith(Text prefix) { return new Bool(value.startsWith(prefix.value)); }
	public Bool endsWith(Text postfix) { return new Bool(value.endsWith(postfix.value)); }
	
	public Text print() { System.err.println(value); return this; }
	
	public Text substring(Int start, Int end) { return new Text(value.substring(start.value, end.value)); }
	public Text withoutCharacterAt(Int index) { return new Text(value.substring(0, index.value - 1) + value.substring(index.value)); }
	public Text withCharacterAt(Int index, Char c) { return new Text(value.substring(0, index.value) + c.value + value.substring(index.value)); }
	public Text cat(Element<?> e) { return new Text(value + e.toCitrus()); }
	public Text concat(List<?> list) { 
		String s = value;
		for(Element<?> e : list) {
			s = s + ((Text)e).value;
		}
		return new Text(s); 
	}
	
}
