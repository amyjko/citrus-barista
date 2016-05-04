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

public class RealLiteral extends Expression<Real> {

	// Restricted in Boot.java
	public static final Dec<Text> token = new Dec<Text>(new Text("0.0"));
	
	public static final Dec<Real> value = new Dec<Real>(true, new BaseElement<Real>() {
		public Real evaluate(Element<?> env) {
			
			double real = 0.0;
			try {
				real = Double.parseDouble(env.get(token).value);
			}
			catch(NumberFormatException e) {}
			return new Real(real);
			
		}
	});

	public RealLiteral() {}
	public RealLiteral(ArgumentList args) { super(args); }
	public RealLiteral(String value) { set(token, new Text(value)); }

	public Real evaluate(Element<?> env) { return peek(value); }

	public Type resultingType() { return Boot.REAL; }

	public Text toCitrus() { return get(token); }
	public String toString() { return get(token).toString(); }
	
}