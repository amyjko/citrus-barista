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

import java.lang.ref.WeakReference;
import java.util.Iterator;

// A low-memory-overhead linked-list-based set with WeakReferences to its elements.
// Provides a simple iterator to iterate through the elements in the list.
// Elements are automatically removed from the list when the weak references
// are finalized and removed.
public class WeakSet<T> implements Iterable<T> {

	private WeakReference<T> element;
	private WeakSet<T> next;

	// Create a weak reference to the element and point to the given list.
	public WeakSet(T newElement) {

		element = new WeakReference<T>(newElement);
		next = null;

	}
	
	private WeakSet(T newElement, WeakSet<T> newNext) { 
		
		element = new WeakReference<T>(newElement);
		next = newNext; 

	}
	
	// Search through the list for the given element. If its
	// not found, insert it at the end of the list.
	public boolean include(T newElement) {

    		WeakSet<T> current = this;
    		WeakSet<T> previous = null;
    		while(current != null) {
    			
    			if(current.getElement() == newElement) return false;
    			previous = current;
    			current = current.getNext();

    		}
    		
    		previous.next = new WeakSet<T>(newElement, previous.getNext());
    		return true;
    		
	}

	// Search through the list for the given element and remove it if found.
	// Returns false if not found.
	public boolean exclude(T elementToRemove) {
		
		WeakSet<T> previous = this;
		WeakSet<T> current = getNext();
		while(current != null) {
			
			if(current.getElement() == elementToRemove) {
				
				current.element = null;
				previous.next = current.next;
				return true;
				
			}
			previous = current;
			current = current.getNext();
			
		}
		return false;

	}
	
	// Gets the element stored in the weak reference. If its null,
	// this is removed from the list by setting this element's next
	// to the next's next.
	public T getElement() { 
	
		// If this element is null, set it to the next non-null element,
		// and set this node's next to that node's next.
		if(element.get() == null) {
		
			WeakSet<T> newNext = next;
			while(newNext != null && newNext.element.get() == null) newNext = newNext.next;
			if(newNext != null) {
				element = newNext.element;
				next = newNext.getNext();
			}
			
		}
		return element.get(); 
	
	}

	// Returns the next non-null element, removing any non-null weak
	// references on the way.
	public WeakSet<T> getNext() { 
	
		// Return the first non-null element found, removing any finalized 
		// elements on the way
		WeakSet<T> newNext = next;
		while(newNext != null && newNext.getElement() == null) newNext = newNext.next;
		next = newNext;

		return next; 
		
	}

	public Iterator<T> iterator() { return new WeakSetIterator<T>(this); }

	public String toString() {

		String s = "{ ";
		for(T item : this) {
			s = s + item + ", ";
		}
		/*
		WeakSet<T> l = this;
		while(l != null) {
			s = s + l.getElement() + ", ";
			l = l.getNext();
		}
		*/
		s = s + " }";
		return s;
		
	}
	
	// Provides a standard Iterator interface to this linked list.
	private class WeakSetIterator<IT> implements Iterator<IT> {

		private WeakSet<IT> current;
		private WeakSet<IT> previous;

		public WeakSetIterator(WeakSet<IT> list) { current = list; previous = null; }

		// Current and its element must be non-null
		public boolean hasNext() { return (current != null && current.getElement() != null); }

		// Since hasNext() must be called before next() is called, we know that current is
		// non-null and has an element.
		public IT next() {

			IT o = current.getElement();
			previous = current;
			current = current.getNext();
			return o;
		
		}

		// Remove the last element returned.
		public void remove() { 

			previous.element.clear();
			previous.next = current;
		
		}

	}
	
}