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

// A named namespace with a list of declarations and an expression that defines its evaluation.
// Name, arguments and expression
public class BaseFunction extends BaseElement<Function> implements Function {

	public static final Dec<Text> name = new Dec<Text>(new Text(""));
	public static final Dec<List<DecInterface>> arguments = new Dec<List<DecInterface>>(new NewList<DecInterface>());
	public static final Dec<Bool> isStatic = new Dec<Bool>(new Bool(false));
	public static final Dec<TypeExpression> returnType = new Dec<TypeExpression>();
	public static final Dec<Element> expression = new Dec<Element>();

	public Element instantiate(ArgumentList args) {

		// Create an instance of this namespace using the given arguments, and
		// return the evaluation of this function's expression in this namespace.
		Element<?> expr = peek(expression);
		Element<?> env = new BaseElement(this, args);
		
		// We don't do lazy evaluation for functions.
		for(DecInterface dec : peek(arguments)) env.get(dec);
		
		Element<?> result = expr.evaluate(env);

		return result;

	}

	public Element<?> getExpression() { return peek(expression); }

	public Iterable<DecInterface> getDeclarationsToInstantiate() { return peek(arguments); }
	public Iterable<DecInterface> getDeclarationsDeclared() { return peek(arguments); }
	public String getName() { return peek(name).value; }
	public boolean isStatic() { return peek(isStatic).value; }
	public String toString() { return "fun " + getName(); }
	public Bool isTypeOf(Namespace t) { return new Bool(t == this); }
	public Language getLanguage() { return getOwnerOfType(Language.class); }
	public int getNumberOfDeclarations() { return peek(arguments).length().value; }

	public DecInterface<?> getDeclarationOf(Text name) { 

		for(DecInterface dec : peek(arguments))
			if(dec.getName().value.equals(name.value)) return dec;
		return null;
		
	}
	
	// Functions are declared in types, so we ask the type that contains this function
	public Type getTypeNamed(String name) {
		
		BaseType type = getOwnerOfType(BaseType.class);
		if(type == null) {
			return null;
		}
		else return type.getTypeNamed(name);
		
	}

	public Function getFunctionNamed(Text name, Bool isStatic) {

		BaseType type = getOwnerOfType(BaseType.class);
		if(type == null) return null;
		return type.getFunctionNamed(name, isStatic);

	}

	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) {
		debug("Haven't implemented getDecInContext");
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Type> getTypesInContext(Set<Type> names) {
		debug("Haven't implemented getTypesInContext");
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Function> getFunctionsInContext(Set<Function> functions) {
		debug("Haven't implemented getFunctionsInContext");
		// TODO Auto-generated method stub
		return null;
	}

}
