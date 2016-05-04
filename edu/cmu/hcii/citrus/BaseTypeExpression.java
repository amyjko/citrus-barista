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

import static edu.cmu.hcii.citrus.Boot.*;

// Consists of a base type, "type", and a list of type arguments.
// For example, a [Paint] List, a [String Property] Dictionary
// The canonical form would be (a TypeExpression [Type [TypeArgument ... ]] )
public class BaseTypeExpression extends Expression<TypeExpression> implements TypeExpression {

	public static final BootDec<Text> name = new BootDec<Text>(new Text(""));

	// This is constrainted in Boot.java to be the type with the given name.
	public static final BootDec<Type> type = new BootDec<Type>(ELEMENT, true);
	public static final BootDec<List<TypeExpression>> arguments = new BootDec<List<TypeExpression>>(new List<TypeExpression>());
	
	public BaseTypeExpression() { super(); init(); }
	public BaseTypeExpression(ArgumentList args) { super(args); init(); }
	public BaseTypeExpression(Type newType, List<TypeExpression> args) {
		
		set(name, newType.get(BaseType.name));
		set(type, newType);
		for(TypeExpression t : args) get(arguments).append(t);		
		init();
		
	}
	public BaseTypeExpression(Type newType, TypeExpression ... args) { 
		
		set(name, newType.get(BaseType.name));
		set(type, newType); 
		for(TypeExpression t : args) get(arguments).append(t);
		init();
		
	}
	
	private void init() {

		if(getPropertyByDeclaration(type).getValueFunction() == null) throw new ElementError("Can't create base type expression before constraint on BaseTypeExpression's on type is assigned.", null);
		
	}

	public TypeExpression evaluate(Element<?> env) { return this; }

	public Context contextFor(Element e) { return get(context); }
	
	// Type expression interface
	public Type getBaseType() { 
	
		Type t = peek(type);
		if(t == null) {
			getPropertyByDeclaration(type).markOutOfDate(null);
			return peek(type); 
		} else return t;
		
	}
	public Text getBaseTypeName() { return peek(name); }
	public List<TypeExpression> getTypeArguments() { return get(arguments); }
	public Bool canBeAssignedA(Namespace t) { return t.isTypeOf(get(type)); }
	
	public String toString() { 
		String str = "" + get(type);
		if(!get(arguments).isEmpty().value) str = str + "<";
		for(TypeExpression t : get(arguments)) str = str + t.toString() + " ";
		if(!get(arguments).isEmpty().value) str = str.trim() + ">";
		return str;
	}
	
	public Text toCitrus() {
	
		String str = "" + get(type).toCitrusReference();
		if(!get(arguments).isEmpty().value) str = str + "<";
		for(TypeExpression t : get(arguments)) str = str + t.toCitrus() + " ";
		if(!get(arguments).isEmpty().value) str = str.trim() + ">";
		return new Text(str);

	}
	
	public static class IsInEnv extends PropertyRestriction<Text> {
		public IsInEnv() { super(); }
		public boolean isValid(Property<Text> property, Text value) {
	
			Element<?> owner = property.getElementOwner();
			if(owner == null) return false;
			Context env = owner.get(context);
			if(env == null) return false;
			return env.getTypeNamed(value.value) != null;
			
		}

		public Set<Text> getValidValues(Property<Text> property, Set<Text> values) {
			
			Element<?> owner = property.getElementOwner();
			if(owner == null) return values;
			Context<?> env = owner.get(context);
			if(env == null) return values;
			for(Type type : env.getTypesInContext(new Set<Type>()))
				values.add(type.get(BaseType.name));
			return values;
			
		}

		public String why(Property property) { return "Must be in environment"; }		
		
	}

}
