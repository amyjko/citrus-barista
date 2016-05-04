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

// Nothing is a JavaElement that doesn't track its users, since there's nothing it
// would do with them anyway.
public class Nothing extends BootElement<Nothing> {

	// TYPE
	public Namespace<?> getType() { return Boot.NOTHING; }

	// EQUALITY
	public Bool isEquivalentTo(Element<?> e) { return new Bool(e instanceof Nothing); }
	public Bool is(Element e) { return new Bool(e == null || e instanceof Nothing); }
	
	// DUPLICATION
	public Element duplicate() { return new Nothing(); }

	// TRANSLATION
	public Text toCitrus() { return new Text("nothing"); }
	public Text toCitrusReference() { return new Text("nothing"); }
	public String toString() { return "nothing"; }
	public Text toXML() { return toCitrus(); }

	// PREDICATES
	public Bool isNothing() { return Bool.TRUE; }
	public Bool isSomething() { return Bool.FALSE; }

	// Evaluate the element using the given environment.
	public Nothing evaluate(Element<?> env) { return this; }

}