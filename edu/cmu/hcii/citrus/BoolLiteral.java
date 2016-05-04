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

public class BoolLiteral extends Expression<Bool> {

	// Restricted in Boot.java
	public static final Dec<Text> token = new Dec<Text>(new Text("true"));
	
	public static final Dec<Bool> value = new Dec<Bool>(true, new BaseElement<Bool>() {
		public Bool evaluate(Element<?> env) {
			
			if(env.get(token).value.equals("true")) return new Bool(true);
			else return new Bool(false);
			
		}
	});

	public BoolLiteral() {}
	public BoolLiteral(ArgumentList args) { super(args); }
	public BoolLiteral(String value) { set(token, new Text(value)); }

	public Bool evaluate(Element<?> env) { return peek(value); }

	public Type resultingType() { return Boot.BOOL; }

	public Text toCitrus() { return get(value).toCitrus(); }
	public String toString() { return get(value).toString(); }
	
}