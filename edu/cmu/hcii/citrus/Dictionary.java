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

public class Dictionary extends BaseElement<Dictionary> {

	protected static final BootDec<?> itemDeclaration = new BootDec(null);
	static { 
		itemDeclaration.name = new Text("item"); 
		itemDeclaration.type = new BootTypeExpression(Boot.ELEMENT);
	}

	public final Hashtable<Element,Property<?>> items = new Hashtable<Element,Property<?>>();
	
	public Dictionary() {}
	public Dictionary(Namespace type, ArgumentList args) { super(type, args); }

	public int hashCode() { return items.hashCode(); }

	protected final Property<?> generateProperty(Element item) {
		
		Property<?> newProperty = new Property(this, (DecInterface<?>)itemDeclaration);
		newProperty.initialize(item, false, null);
		return newProperty;

	}

	public Bool define(Element key, Element value) { 
		
		Bool result = new Bool(items.containsKey(key));
		items.put(key, generateProperty(value));
		return result;
	
	}
	
	public Element get(Element key) {

		Property p = items.get(key);
		if(p == null) return null;
		return p.peek();
		
	}

	public Nothing foreachValue(Function f) {
		
		java.util.Enumeration<Property<?>> enumeration = items.elements();
		while(enumeration.hasMoreElements()) {
			Property p = enumeration.nextElement();
			Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, p.peek())));
		}
		return null;
		
	}

	public Nothing foreachKey(Function f) {
		
		java.util.Enumeration<Element> enumeration = items.keys();
		while(enumeration.hasMoreElements())
			Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, enumeration.nextElement())));
		return null;
		
	}

	public Bool isEmpty() { return new Bool(items.isEmpty()); }
	
	public Text toCitrus() { 
	
		String s = "{";
		for(Element<?> item : items.values())
			s = s + " " + item.toCitrus();
		s = s + " }";
		return new Text(s);

	}
	
	public Int size() { return new Int(items.size()); }
	
	public String toString() { return toCitrus().value; }

	public Text toXML() {

		throw new ElementError("Haven't implemented dictionary.toXML", null);
		
	}

	public Dictionary evaluate(Element<?> env) { return this; }
	
	public Bool isEquivalentTo(Element<?> o) { return new Bool(this.equals(o)); }

	public Element duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

}