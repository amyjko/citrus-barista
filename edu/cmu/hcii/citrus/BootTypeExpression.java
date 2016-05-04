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

public class BootTypeExpression extends BootElement<TypeExpression> implements TypeExpression {

	public Type type = Boot.ELEMENT;
	public List<TypeExpression> arguments = new List<TypeExpression>();
	
	public BootTypeExpression() {}
	public BootTypeExpression(Type newType, TypeExpression ... newTypeVariables) {
		
		type = newType;
		for(TypeExpression t : newTypeVariables) arguments.append(t);
		
	}
	
	// TYPE EXPRESSION INTERFACE
	public Type getBaseType() { return type; }
	public Text getBaseTypeName() { return new Text(type.getName()); }
	public List<TypeExpression> getTypeArguments() { return arguments; }
	public Bool canBeAssignedA(Namespace t) { return t.isTypeOf(type); }

	// TYPE
	public Namespace<?> getType() { return Boot.TYPE_EXPRESSION; }

	// EQUIVALENCE
	public Bool isEquivalentTo(Element<?> o) { 

		if(!(o instanceof TypeExpression)) return new Bool(false);
		else return new Bool(false);
	
	}

	// DUPLICATION
	public Element duplicate() { return new BaseTypeExpression(type, arguments); }

	// TRANSLATION
	public Text toCitrus() { 
		String str = "" + type;
		if(!arguments.isEmpty().value) str = str + "<";
		for(TypeExpression t : arguments) str = str + t.toCitrus() + " ";
		if(!arguments.isEmpty().value) str = str.trim() + ">";
		return new Text(str);
	}
	public Text toCitrusReference() { return toCitrus(); }
	public String toString() { return toCitrus().value; }

	// EVALUATION
	public TypeExpression evaluate(Element<?> env) { return this; }

}
