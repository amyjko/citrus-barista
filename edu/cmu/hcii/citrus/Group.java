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

import java.util.Iterator;
import java.util.LinkedList;

import edu.cmu.hcii.citrus.views.Transition;

public abstract class Group<CollectionType extends Element,ItemType extends Element> extends BootElement<CollectionType> implements Iterable<ItemType> {
	
	protected static final BootDec<?> itemDeclaration = new BootDec(null);
	static {
		itemDeclaration.name = new Text("item"); 
		itemDeclaration.type = new BootTypeExpression(Boot.ELEMENT);
	}

	// A wrapper for a set iterator.
	private class GroupIterator implements Iterator<ItemType> {
		
		private Property<ItemType> last = null;
		private Iterator<Property<ItemType>> iterator = propertyIterator();
		public boolean hasNext() { return iterator.hasNext(); }
		public ItemType next() {
			last = iterator.next();
			return last.peek();
		}
		public void remove() {
			// Make sure we release the property as owner
			iterator.remove(); 
			last.set(null);
		}
		
	}
	
	public Iterator<ItemType> iterator() { return new GroupIterator(); }
	public abstract Iterator<Property<ItemType>> propertyIterator();

	protected final Property<ItemType> generateProperty(ItemType item) {
		
		Property<ItemType> newProperty = new Property<ItemType>(this, (DecInterface<ItemType>)itemDeclaration);
		newProperty.initialize(item, false, null);
		return newProperty;

	}

	public Namespace<?> getType() { return Boot.GROUP; }

	public abstract Bool isEmpty();
	public final Bool remove(ItemType item) { return removeOverTime(item, null); }
	public abstract Bool removeOverTime(ItemType item, Transition t);

	public Bool contains(ItemType item) {
		
		for(ItemType i : this)
			if(i.isEquivalentTo(item).value) return new Bool(true);
		return new Bool(false);
		
	}
	
	public Bool containsItemThat(Function f) {

		for(ItemType i : this) {
			Bool b = (Bool)Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, i)));
			if(b.value) return b;	
		}
		return new Bool(false);
		
	}

	public Bool containsA(Type t) {

		for(ItemType i : this)
			if(i.isa(t).value) return Bool.TRUE;
		return Bool.FALSE;
		
	}

	public Nothing foreach(Function f) {
		
		for(ItemType i : this)
			Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, i)));
		return null;
		
	}

	public Nothing foreachProperty(Function f) {
		
		Iterator<Property<ItemType>> it = propertyIterator();
		while(it.hasNext())
			Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, it.next())));
		return null;
		
	}

	public ItemType itemThat(Function f) {
		
		for(ItemType i : this) {
			Bool b = (Bool)Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, i)));
			if(b.value) return i;
		}
		return null;
		
	}
	
	// Takes a two argument function, which is given the current top choice and a new candidate. It should
	// return true if the new candidate should be chosen over the current candidate.
	// fun [ refs Element topChoice refs Element candidate ]
	public ItemType choose(Function f) {

		ItemType choice = null;
		for(ItemType i : this) {
			Bool b = (Bool)Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, choice), new Arg("", false, i)));
			if(b.value) choice = i;
		}
		return choice;
		
	}
	
	public Bool removeItemsThat(Function f) {

		boolean removed = false;
		LinkedList<ItemType> itemsToRemove = new LinkedList<ItemType>();
		for(ItemType i : this) {
			Bool b = (Bool)Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, i)));
			if(b.value) itemsToRemove.add(i);
		}
		for(ItemType i : itemsToRemove)
			remove(i);
		
		return new Bool(itemsToRemove.size() > 0);
		
	}
	
	public Nothing foreachUntilFalse(Function f) {
		
		for(ItemType i : this) {
			if(!((Bool)Evaluate.eval(this, this, f, new List<Arg>(new Arg("", false, i)))).value)
				return null;
		}
		return null;
		
	}

}
