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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import edu.cmu.hcii.citrus.views.Transition;

public class Set<ItemType extends Element<?>> extends Group<Set<ItemType>,ItemType> implements Iterable<ItemType> {

	public final HashSet<Property<ItemType>> items = new HashSet<Property<ItemType>>();
	
	public Set() {}
	public Set(Collection<ItemType> newItems) { for(ItemType i : newItems) add(i); }
	
	public Namespace<?> getType() { return Boot.SET; }

	public int hashCode() { return items.hashCode(); }

	public Bool add(ItemType item) { 
		
		if(contains(item).value) return Bool.FALSE;
		Bool result = new Bool(items.add(generateProperty(item))); 
		touchUsers(null);
		return result;
	
	}
	public Bool addItems(Group<?, ItemType> newItems) { 

		for(ItemType i : newItems) add(i);
		return new Bool(true);
	
	}
	
	public Bool removeItems(Set<ItemType> newItems) {
		
		for(ItemType i : newItems) remove(i);
		return new Bool(true);
		
	}
	
	public Nothing removeAll() {
		
		for(Property<ItemType> i : items)
			remove(i.get());
		return null;
		
	}

	public Bool removeOverTime(ItemType item, Transition t) { 

		Bool result = new Bool(false);
		Iterator<Property<ItemType>> iterator = items.iterator();
		while(iterator.hasNext()) {

			Property<ItemType> p = iterator.next();
			if(p.peek() == item) {
				iterator.remove();
				p.set(null);
				result = new Bool(true);
				break;
			}

		}
		if(result.value) touchUsers(null);
		return result;
		
	}

	public Bool isEmpty() { return new Bool(items.isEmpty()); }
	
	public Element duplicate() { 
	
		Set<ItemType> newSet = new Set<ItemType>();
		for(ItemType i : this) newSet.add(i);
		return newSet;
		
	}
	public Text toCitrus() { 
	
		String s = "{";
		for(Element<?> item : this) {
			if(item == null) s = s + " nothing";
			else s = s + " " + item.toString();
		}
		s = s + " }";
		return new Text(s);

	}
	
	public Int size() { return new Int(items.size()); }
	
	public String toString() { return toCitrus().value; }

	public Text toXML() {

		String s = "";
		for(ItemType item : this) s = s + item.toXML() + "\n";
		return new Text(s);
		
	}

	public Set<ItemType> evaluate(Element<?> env) { return this; }

	public Iterator<Property<ItemType>> propertyIterator() { return items.iterator(); }

	public final Element replaceWith(Element oldElement, Element newElement, Transition t) {

		if(oldElement == this) return super.replaceWith(oldElement, newElement, t);
		else {
			remove((ItemType)oldElement);
			add((ItemType)newElement);
			return newElement;
		}
		
	}	

	public void touchUsers(Transition t) {
		
		Property<?> owner = getPropertyOwner();
		if(owner != null) owner.touch(t);
		for(Property p : getUsers()) p.touch(t);
		
	}
	
	public Bool isEquivalentTo(Element<?> o) { return new Bool(this.equals(o)); }

}