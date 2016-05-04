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

import java.util.LinkedList;

public class ArgumentList {

	public Element enclosingEnvironment = null;
	public LinkedList<Argument> arguments = new LinkedList<Argument>();

	public ArgumentList(Argument ... args) {
		
		for(Argument arg : args) arguments.add(arg);
		
	}
	
	public void add(Text name, boolean isConstraint, Element<?> value) {
		
		arguments.add(new Argument(name, isConstraint, value));
		
	}
	public void add(String name, boolean isConstraint, Element<?> value) {
		
		arguments.add(new Argument(new Text(name), isConstraint, value));
		
	}
	public void add(String name, Element<?> value) {
		
		arguments.add(new Argument(new Text(name), false, value));
		
	}
	
	public boolean isEmpty() { return enclosingEnvironment == null && arguments.isEmpty(); }

	public String toString() { return arguments.toString(); }
	
	public class Argument {
		
		Text name;
		boolean isConstraint;
		Element<?> value;
		
		public Argument(Text newName, boolean newIsConstraint, Element<?> newValue) {

			name = newName;
			isConstraint = newIsConstraint;
			value = newValue;
			
		}
		
		public String toString() { return "" + name + (isConstraint ? " <- " : " = ") + value; }
		
	}
	
}