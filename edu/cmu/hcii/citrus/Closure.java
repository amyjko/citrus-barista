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

public class Closure extends BaseElement<Function> implements Function {

	private final Function fun;
	private final Element<?> env;

	public Closure(Function fun, Element environment) {

		this.fun = fun;
		this.env = environment;
		
	}
	
	public Element instantiate(ArgumentList args) {

		// Create an instance of this namespace using the given arguments, and
		// return the evaluation of this function's expression in this namespace.
		Element<?> expr = fun.getExpression();
		args.enclosingEnvironment = env;
		Element<?> env = new BaseElement(fun, args);
		
		// We don't do lazy evaluation for functions.
		for(DecInterface dec : getDeclarationsToInstantiate())
			env.get(dec);
		
		Element<?> result = expr.evaluate(env);

		return result;

	}
	
	public Function getFunction() { return fun; }
	public Element<?> getExpression() { return fun.getExpression(); }
	
	public Iterable<DecInterface> getDeclarationsToInstantiate() { return fun.getDeclarationsToInstantiate(); }
	public Iterable<DecInterface> getDeclarationsDeclared() { return fun.getDeclarationsDeclared(); }
	public String getName() { return fun.getName(); }
	public String toString() { return "fun " + getName(); }
	public Bool isTypeOf(Namespace t) { return new Bool(t == this); }
	public Language getLanguage() { return fun.getLanguage(); }
	public int getNumberOfDeclarations() { return fun.getNumberOfDeclarations(); }
	public DecInterface<?> getDeclarationOf(Text name) { return fun.getDeclarationOf(name); }
	public Type getTypeNamed(String name) { return fun.getTypeNamed(name); }
	public Function getFunctionNamed(Text name, Bool isStatic) { return fun.getFunctionNamed(name, isStatic); }
	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) { return fun.getDeclarationsInContext(names); }
	public Set<Type> getTypesInContext(Set<Type> names) { return fun.getTypesInContext(names); }
	public Set<Function> getFunctionsInContext(Set<Function> functions) { return fun.getFunctionsInContext(functions); }
	public boolean isStatic() { return fun.isStatic(); }

}