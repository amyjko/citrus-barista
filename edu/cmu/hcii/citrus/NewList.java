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

public class NewList<ItemType extends Element> extends Expression<List<ItemType>> {

	// The item expressions to be evaluated and inserted into a new list
	public static final BootDec<List<Expression<?>>> items = new BootDec<List<Expression<?>>>(new List());

	public NewList() { super(); }	
	public NewList(Namespace type, ArgumentList args) { super(type, args); }	
	public NewList(Expression ... items) { 
		
		super(); 
		for(Expression item : items) appendItem(item);
		
	}	
	
	public void appendItem(Expression item) { get(items).append(item); }

	public List evaluate(Element<?> env) {

		List<Element> newList = new List<Element>();

		// Evaluate the arguments using the current environment, putting each in the function instance's local environment.
		for(Element item : peek(items))
			newList.append(item.evaluate(env));

		// Evaluate the function using the enclosing environment (which may be null) and return its value.
		return newList;
	
	}

	public Type resultingType() { return Boot.LIST; }
	public Context contextFor(Element e) { return get(context); }
	
	public String toString() {
		
		String result = "[ ";
		for(Element<?> e : get(items))
			result = result + " " + e.toString();
		result = result + " ]";
		return result;		
		
	}

}
