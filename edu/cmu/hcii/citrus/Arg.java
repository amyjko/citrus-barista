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

public class Arg extends BaseElement<Arg> {

	public static final BootDec<Text> param = new BootDec<Text>(new Text(""));
	public static final BootDec<Bool> valueIsConstraint = new BootDec<Bool>(new Bool(false));
	public static final BootDec<Element<?>> value = new BootDec<Element<?>>(new Nothing());

	public Arg() {}
	public Arg(ArgumentList args) { super(args); }
	public Arg(String newParam, boolean newValueIsConstraint, Element<?> newValue) {

		set(param, new Text(newParam));
		set(valueIsConstraint, new Bool(newValueIsConstraint));
		set(value, newValue);
		
	}
	
	public Element value() { return peek(value); }
	
	public String toString() { 

		String par = (get(Arg.param) == null || get(Arg.param).value.equals("")) ? "" : get(param).toString();
		if(!par.equals("")) par = par + (bool(valueIsConstraint) ? CitrusParser.CONSTRAINT : "" + CitrusParser.DEFAULT);
		return "" + par + get(Arg.value); 
		
	}
	
}
