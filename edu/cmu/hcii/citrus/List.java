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
import java.util.Iterator;
import java.util.LinkedList;

import edu.cmu.hcii.citrus.views.Transition;

public class List<ItemType extends Element> extends Group<List<ItemType>, ItemType> {

	private boolean shouldRecord() { 
		
		Property p = getPropertyOwner();
		if(p == null) return false;
		return p.getDeclaration().isUndoable(); 
		
	}
	
	public static class ItemInserted extends BaseElement {
		
		public static final BootDec<Element> item = new BootDec<Element>(null);
		public ItemInserted(ArgumentList args) { super(args); }
		
	}
	public static class ItemRemoved extends BaseElement {
		
		public static final BootDec<Element> item = new BootDec<Element>(null);
		public ItemRemoved(ArgumentList args) { super(args); }
		
	}

	public abstract static class ListEvent extends ElementChangeEvent {
		
		protected final int index;
		protected final Element value;

		public ListEvent(List list, int index, Element value) {
			
			super(list);
			this.index = index;
			this.value = value;
			
		}
		
		public abstract void undo();

	}
	
	public static class InsertEvent extends ListEvent {

		public InsertEvent(List list, int index, Element value) {
			super(list, index, value);
		}

		public void undo() { ((List)elementChanged).remove(value); }
		
		public String toString() { return "" + value + " was inserted at index " + index; }
		
	}

	public static class RemoveEvent extends ListEvent {

		public RemoveEvent(List list, int index, Element value) {
			super(list, index, value);
		}

		public void undo() { ((List)elementChanged).insertAfterIndex(new Int(index - 1), value); }

		
		public String toString() { return "" + value + " was removed from index " + index; }
		
	}

	public final LinkedList<Property<ItemType>> items;
	
	public List() { items = new LinkedList<Property<ItemType>>(); }
	public List(ItemType ... newItems) { 
		items = new LinkedList<Property<ItemType>>();
		for(ItemType item : newItems) append(item);
	}
	public List(Collection<ItemType> newItems) {
		items = new LinkedList<Property<ItemType>>();
		for(ItemType item : newItems) append(item);
	}
	
	public Namespace<?> getType() { return Boot.LIST; }
	
	public int hashCode() { return items.hashCode(); }

	public Set<Property<?>> getProperties() { 
		
		return (Set<Property<?>>)new Set(items);
		
	}

	// A list gets its context from its element owner
	public Context contextFor(Element e) { 
		
		Element<?> elementOwner = getElementOwner(); 
		if(elementOwner == null) return null;
		else return elementOwner.contextFor(e);
	}
	
	public Element duplicate() { 
	
		List<ItemType> newList = new List<ItemType>();
		for(ItemType item : this) newList.append((ItemType)item.duplicate());
		return newList;
	
	}
	public Text toCitrus() {

		String s = "[";
		for(ItemType item : this)
			s = s + " " + item.toCitrus();
		s = s + " ]";
		return new Text(s);
		
	}
	
	public Text toText() { 

		String s = "[";
		for(ItemType item : this)
			s = s + " " + item.toText();
		s = s + " ]";
		return new Text(s);

	}
	
	public String toString() {
		
		String s = "[";
		for(ItemType item : this) {
			s = s + " " + item.toString();
		}
		return s + " ]";

	}
	public Text toXML() {

		String s = "";
		for(ItemType item : this) s = s + item.toXML() + "\n";
		return new Text(s);
		
	}
	public Text toFormattedText(Text delimiter, Bool last) {
		
		String result = "";
		Iterator<ItemType> i = iterator();
		while(i.hasNext()) {
			Element item = i.next();
			result = result + ((Text)Evaluate.eval(item, item, new Ref("toText"), new List<Arg>())).value;
			if(i.hasNext() || last.value) result = result + delimiter.value;
		}
		return new Text(result);
		
	}

	public List evaluate(Element<?> env) { return this; }

	public Iterator<Property<ItemType>> propertyIterator() { return items.iterator(); }
	
	public Int length() { return new Int(items.size()); }

	public Bool isEmpty() { return new Bool(items.isEmpty()); }
	public ItemType first() { return items.isEmpty() ? null : items.getFirst().peek(); }
	public ItemType second() { return items.size() < 2 ? null : items.get(1).peek(); }
	public ItemType third() { return items.size() < 3 ? null : items.get(2).peek(); }
	public ItemType last() { return items.isEmpty() ? null : items.getLast().peek(); }
	public ItemType nth(Int index) { 
		if(index.value >= 1 && index.value <= items.size())
			return items.get(index.value - 1).peek(); 
		else return null;
	}

	public ItemType firstItemOfType(Type t) {
		
		for(ItemType item : this)
			if(item.getType().isTypeOf(t).value) return item;
		return null;
		
	}
	
	public List<ItemType> ofType(Type t) {

		List<ItemType> newList = new List<ItemType>();
		for(ItemType item : this)
			if(item.getType().isTypeOf(t).value) newList.append(item);
		return newList;
		
	}

	// Swap with next if next is true, with previous if previous is true.
	// Returns true if a swap occured.
	public Bool swapItemAtWith(Int index, Bool next) { return swapItemAtWithOverTime(index, next, null); }
	public Bool swapItemAtWithOverTime(Int index, Bool next, Transition t) {
		
		ItemType item = itemAt(index);
		ItemType itemAfterOrBefore = next.value ? itemAfter(item) : itemBefore(item);
		if(itemAfterOrBefore == null) return new Bool(false);
		remove(itemAfterOrBefore);
		if(next.value) return insertBeforeOverTime(item, itemAfterOrBefore, t);
		else return insertAfterOverTime(item, itemAfterOrBefore, t);	
		
	}
	
	public List appendList(Group<?,?> newItems) {
		
		for(Element item : newItems)
			append((ItemType)item);
		return this;
		
	}
	
	public Bool appendListAt(List<Element> newItems, Int index) {
		
		while(!newItems.isEmpty().value) {
			Element item = newItems.first();
			newItems.remove(item);
			insertAfterIndex(index, (ItemType)item);
			index = new Int(index.value + 1);
		}
		return Bool.TRUE;
		
	}

	public List append(ItemType item) { return appendOverTime(item, null); }
	public List appendOverTime(ItemType item, Transition t) { 
		if(item == null) throw new ElementError("null items are not allowed in lists", this);
		insertAfterIndexOverTime(length(), item, t);
		return this;
	}
	
	public ItemType insert(ItemType item) { return insertAfterIndexOverTime(new Int(0), item, null); }	
	public ItemType insertOverTime(ItemType item, Transition t) { return insertAfterIndexOverTime(new Int(0), item, t); }	

	public ItemType insertAfterIndex(Int index, ItemType item) { return insertAfterIndexOverTime(index, item, null); }
	public ItemType insertAfterIndexOverTime(Int index, ItemType item, Transition t) {
		
		Property newProperty = generateProperty(item);
		items.add(index.value, newProperty);
		touchUsers(t);
		
		ElementChangeAccumulator acc = Property.peekAccumulator();
		if(acc != null && !Property.initializing() && shouldRecord()) {
			ElementChangeEvent event = new InsertEvent(this, index.value, item);
			acc.recordEvent(event);
		}
		if(listenersListening != null)
			notifyListenersOf(Reflection.getJavaType(ItemInserted.class), item);

		return item;

	}
	
	public Bool insertBefore(ItemType itemToInsertBefore, ItemType item) { return insertBeforeOverTime(itemToInsertBefore, item, null); }
	public Bool insertBeforeOverTime(ItemType itemToInsertBefore, ItemType item, Transition t) {
		
		Int indexOfItemToInsertBefore = indexOf(itemToInsertBefore);
		if(indexOfItemToInsertBefore.value < 0) return new Bool(false);
		insertAfterIndexOverTime(new Int(indexOfItemToInsertBefore.value - 1), item, t);
		return new Bool(true);
		
	}
	public Bool insertAfter(ItemType itemToInsertAfter, ItemType item) { return insertAfterOverTime(itemToInsertAfter, item, null); }
	public Bool insertAfterOverTime(ItemType itemToInsertAfter, ItemType item, Transition t) {
		
		Int indexOfItemToInsertAfter = indexOf(itemToInsertAfter);
		if(indexOfItemToInsertAfter.value < 0) return new Bool(false);
		insertAfterIndexOverTime(indexOfItemToInsertAfter, item, t);
		return new Bool(true);
		
	}	
	
	// Lists disown by removing the element from the list
	public Bool disown(Element<?> e, Transition t) {

		if(removeOverTime((ItemType)e, t).value) return new Bool(true);
		else return super.disown(e, t);
		
	}
	
	public Bool removeAll() {
		
		Int one = new Int(1);
		while(!isEmpty().value) removeOverTime(itemAt(one), null);
		return Bool.TRUE;
		
	}
	
	public Bool removeItemAt(Int index) { return removeItemAtOverTime(index, null); }
	public Bool removeItemAtOverTime(Int index, Transition t) {

		if(index.value < 0) return Bool.FALSE;
		else if(index.value > items.size()) return Bool.FALSE;
		
		Property<?> propertyRemoved = items.remove(index.value - 1);
		Element valueRemoved = propertyRemoved.get();
		propertyRemoved.set(null);
		
		touchUsers(t);
		ElementChangeAccumulator acc = Property.peekAccumulator();
		if(acc != null && !Property.initializing() && shouldRecord()) {	
			ElementChangeEvent event = new RemoveEvent(this, index.value, valueRemoved);
			acc.recordEvent(event);
		}

		if(listenersListening != null)
			notifyListenersOf(Reflection.getJavaType(ItemRemoved.class), valueRemoved);

		return Bool.TRUE;
		
	}

	public Bool removeOverTime(ItemType item, Transition t) { 

		Int indexOf = indexOf(item);
		if(indexOf.value < 0) return Bool.FALSE;
		else return removeItemAtOverTime(indexOf, t);

	}

	public Bool replace(ItemType itemToReplace, ItemType replacement) {
		return replaceOverTime(itemToReplace, replacement, null);
	}
	public Bool replaceOverTime(ItemType itemToReplace, ItemType replacement, Transition t) {

		insertAfterOverTime(itemToReplace, replacement, t);
		Bool result = removeOverTime(itemToReplace, t);
		return result;
		
	}
	
	public Bool replaceItemWithItems(ItemType itemToReplace, List<Element> replacements) {
		
		Int indexofItemToReplace = indexOf(itemToReplace);
		if(indexofItemToReplace.value < 0) return Bool.FALSE;
		appendListAt(replacements, indexofItemToReplace);
		remove(itemToReplace);
		return Bool.TRUE;
		
	}

	public Int indexOf(ItemType item) { 
		
		Iterator<ItemType> iterator = iterator();
		int i = 1;
		while(iterator.hasNext()) {
			if(iterator.next().isEquivalentTo(item).value) return new Int(i);
			i++;
		}
		return new Int(-1);
	
	}

	public ItemType itemAt(Int index) {
		
		if(index.value < 1 || index.value > items.size()) return null;
		return items.get(index.value - 1).peek();
		
	}
	
	public ItemType itemAfter(ItemType item) { return itemAt(indexOf(item).plus(new Int(1))); }
	public ItemType itemBefore(ItemType item) { return itemAt(indexOf(item).minus(new Int(1))); }

	public Bool isListEquivalentTo(List<?> e) {
		
		if(!(e instanceof List)) return new Bool(false);
		Iterator<ItemType> eItems = ((List<ItemType>)e).iterator();
		for(ItemType item : this) {
			if(!eItems.hasNext()) return new Bool(false);
			if(!eItems.next().isEquivalentTo(item).value) return new Bool(false);
		}
		if(eItems.hasNext()) return new Bool(false);
		else return new Bool(true);
		
	}
	
	public Element getFirstChildOfType(Namespace t) {

		Element result = null;
		for(ItemType item : this) {
			if(item.getType().isTypeOf(t).value) return item;
			result = item.getFirstChildOfType(t);
			if(result != null) return result;
		}
		return null;
		
	}

	// Overrides replace with the replace items in the collection.
	public final Element replaceWith(Element oldElement, Element newElement, Transition t) {

		if(oldElement == this) return super.replaceWith(oldElement, newElement, t);
		else {
			replaceOverTime((ItemType)oldElement, (ItemType)newElement, t);
			return newElement;
		}
		
	}	

	public void touchUsers(Transition t) {

		Property<?> owner = getPropertyOwner();
		if(owner != null) owner.touch(t);
		for(Property p : getUsers()) p.touch(t);
		
	}
	
	public void propogateListener(Listener listener, boolean add) {

		// Iterate through all properties, propogating the listener
		for(ItemType item : this) {
			item.propogateListener(listener, add);
		}
		
	}	

}
