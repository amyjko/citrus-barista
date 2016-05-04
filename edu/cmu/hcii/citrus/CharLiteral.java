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

public class CharLiteral extends Expression<Char> {

	// Restricted in Boot.java
	public static final Dec<Text> token = new Dec<Text>(new Text(" "));
	
	public static final Dec<Char> value = new Dec<Char>(true, new BaseElement<Char>() {
		public Char evaluate(Element<?> env) {

			String tok = env.get(token).value;
			if(tok.equals("")) return new Char((char)0);
			else if(tok.startsWith("\\")) {
				char second = tok.charAt(1);
				if(second == 'b') return new Char('\b');
				else if(second == 'n') return new Char('\n');
				else if(second == 't') return new Char('\t');
				else if(second == '`') return new Char('`');
				else if(second == 'u') {
					
					tok = tok.substring(2);
					System.err.println("Not parsing hexidecimal " + tok + " to unicode!");
					return new Char('\uFFFF');
					
				}
				else throw new ElementError("Ellegal escape code for character " + tok, null);
				
			}
			else return new Char(tok.charAt(0));
			
		}
	});

	public CharLiteral() {}
	public CharLiteral(ArgumentList args) { super(args); }
	public CharLiteral(String value) { set(token, new Text(value)); }

	public Char evaluate(Element<?> env) { return peek(value); }

	public Type resultingType() { return Boot.CHAR; }

	public Text toCitrus() { return get(value).toCitrus(); }
	public String toString() { return get(value).toString(); }
	
}