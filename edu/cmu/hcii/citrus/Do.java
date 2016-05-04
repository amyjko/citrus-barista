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

// A primitive language construct that executes a sequence of statements,
// some of which may be declarations.
public class Do extends Expression<Element> implements Namespace<Element> {

	public static final Dec<List<Expression>> expressions = new Dec<List<Expression>>(new NewList<Expression>());
	
	public Element evaluate(Element env) {

		// Create a new context
		ArgumentList args = new ArgumentList();
		args.enclosingEnvironment = env;
		BaseElement<?> namespace = new BaseElement(this, args);

		// Evaluate all of the arguments, returning the last evaluation.
		// If we encounter a declaration, instantiate and add to environment.
		Element result = null;
		for(Expression expression: peek(expressions)) {
			if(expression instanceof DecInterface)
				namespace.addProperty(((DecInterface)expression).make(namespace).initialize(null, false, null));
			else
				result = expression.evaluate(namespace);
		}
		return result;
			
	}

	public String getName() { return "Anonymous do"; }
	public Bool isTypeOf(Namespace t) { return Bool.FALSE; }
	public Language getLanguage() { return (Language)getOwnerOfType(Language.class); }
	public Element instantiate(ArgumentList arguments) {
		return null;
	}

	public Iterable getDeclarationsToInstantiate() {
		return null;
	}

	public Iterable getDeclarationsDeclared() {
		return null;
	}

	public int getNumberOfDeclarations() {
		return 0;
	}

	public DecInterface getDeclarationOf(Text name) {
		// TODO Auto-generated method stub
		return null;
	}

	// Functions are declared in types, so we ask the type that contains this function
	public Type getTypeNamed(String name) {
		
		Type type = (Type)getOwnerOfType(BaseType.class);
		if(type == null) {
			return null;
		}
		else return type.getTypeNamed(name);
		
	}

	public Function getFunctionNamed(Text name, Bool isStatic) {

		Type type = (Type)getOwnerOfType(BaseType.class);
		if(type == null) return null;
		return type.getFunctionNamed(name, isStatic);

	}

	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) {
		debug("Haven't implemented getDecInContext");
		// TODO Auto-generated method stub
		return names;
	}

	public Set<Type> getTypesInContext(Set<Type> names) {
		debug("Haven't implemented getTypesInContext");
		// TODO Auto-generated method stub
		return names;
	}

	public Set<Function> getFunctionsInContext(Set<Function> functions) {
		debug("Haven't implemented getFunctionsInContext");
		// TODO Auto-generated method stub
		return functions;
	}

	public String toString() {
		
		String string = "(do ";
		for(Expression expr : peek(expressions))
			string = string + expr.toString() + " ";
		return string + ")";
		
	}
	
}