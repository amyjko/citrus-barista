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

import edu.cmu.hcii.citrus.views.Transition;

public class Array<ItemType extends Element<?>> extends BootElement<Array> {

	protected static final BootDec<?> itemDeclaration = new BootDec(null);
	static {
		itemDeclaration.name = new Text("item"); 
		itemDeclaration.type = new BootTypeExpression(Boot.ELEMENT);
	}

	public final Property<ItemType>[] items;
	
	public Array(int length) { 
	
		items = new Property[length];
		for(int i = 0; i < items.length; i++) {
			Property<ItemType> newProperty = new Property<ItemType>(this, (DecInterface<ItemType>)itemDeclaration);
			newProperty.initialize(null, false, null);
			items[i] = newProperty;
		}

	}

	public Namespace<?> getType() { return Boot.ARRAY; }
	
	public Bool set(Int index, ItemType value) {
	
		if(index.value < 1 || index.value > items.length)
			throw new ElementError("Array index out of bounds: " + index, null);
		return Bool.valueOf(items[index.value - 1].set(value));
		
	}                                       
	
	public ItemType get(Int index) {
		
		if(index.value < 1 || index.value > items.length)
			throw new ElementError("Array index out of bounds: " + index, null);
		return items[index.value - 1].get();
		
	}

	public int hashCode() { return items.hashCode(); }

	public Element duplicate() { 
	
		Array<ItemType> newSet = new Array<ItemType>(items.length);
		for(int i = 0; i < items.length; i++)
			newSet.items[i].set(items[i].get());
		return newSet;
		
	}
	public Text toCitrus() { 

		String s = "|";
		for(Property<ItemType> item : items) {
			if(item == null) s = s + " nothing";
			else s = s + " " + item.get().toText();
		}
		s = s + " |";
		return new Text(s);

	}
	
	public Int size() { return new Int(items.length); }
	
	public String toString() { return toCitrus().value; }

	public Text toXML() {

		String s = "";
		for(Property<ItemType> item : items) s = s + item.get().toXML() + "\n";
		return new Text(s);
		
	}

	public Array<ItemType> evaluate(Element<?> env) { return this; }

	public final Element replaceWith(Element oldElement, Element newElement, Transition t) {

		throw new ElementError("Haven't implemented replaceWith for arrays", null);
		
	}	

	public void touchUsers(Transition t) {
		
		Property<?> owner = getPropertyOwner();
		if(owner != null) owner.touch(t);
		for(Property p : getUsers()) p.touch(t);
		
	}
	
	public Bool isEquivalentTo(Element<?> o) { return new Bool(this.equals(o)); }

}