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

public class Quote<TypeContained extends Element> extends Expression<TypeContained> {

	public static final Dec<Element<?>> value = new Dec<Element<?>>();
	
	public Quote() { super(); }
	public Quote(ArgumentList arguments) { super(arguments); }
	public Quote(TypeContained newValue) {
		
		set(value, newValue);
		
	}
	
	public TypeContained evaluate(Element<?> context) {
		
		return (TypeContained)peek(value);
		
	}
	
	public String toString() { return "" + CitrusParser.QUOTE + peek(value); }
	public Text toCitrus() { return new Text("" + CitrusParser.QUOTE + peek(value).toCitrus()); }

}
