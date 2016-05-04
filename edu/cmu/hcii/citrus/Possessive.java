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

public class Possessive<TypeOfPossession extends Element> extends Expression<TypeOfPossession> {

	public static final Dec<Element<?>> possessor = new Dec<Element<?>>();
	public static final Dec<Bool> peek = new Dec<Bool>();
	public static final Dec<Element<?>> possession = new Dec<Element<?>>();
	
	public Possessive() {}
	public Possessive(ArgumentList args) { super(args); }
	public Possessive(Element<? extends Element> or, Element<?> ion, Bool newPeek) {
		
		set(possessor, or);
		set(possession, ion);
		set(peek, newPeek);
		
	}
	
	public TypeOfPossession evaluate(Element<?> env) {

		Element or = peek(possessor).evaluate(env);
		if(or == null) {
			throw new NullPointerException("The possessor expression " + peek(possessor) + " evaluated to " + 
					or + " in the context of " + env + " while executing " + this);
		}

		TypeOfPossession ion = (TypeOfPossession)peek(possession).evaluate(or);
		return ion;
		
	}
	
	public Context contextFor(Element e) {
		
		if(peek(possessor) == e) return get(context);
		else if(peek(possession) == e) return peek(possessor).resultingType();
		else return null;
		
	}
	
	public Type resultingType() { return get(possession).resultingType(); }
	
	public String toString() { return "" + peek(possessor) + "." + peek(possession); }
	public Text toCitrus() { return new Text("" + peek(possessor).toCitrus() + "." + peek(possession).toCitrus()); }
	
}