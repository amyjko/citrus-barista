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

// So that we can boostrap the Types in the Elements language without having to create actual types.
public interface Namespace<ValueType extends Element> extends Context<ValueType> {

	// The name of this type of namespace
	public String getName();

	// Returns true if the this type is a type of the given type.
	public Bool isTypeOf(Namespace t);

	// Return the language that this is part of.
	public Language getLanguage();

	// Make an element by using the type's default values in the declarations and the arguments passed in.
	public Element instantiate(ArgumentList arguments);
	
	// Return an iterator for all of the declarations that the Type declares
	// and inherits from its super types, in the order they should be instantiated.
	public Iterable<DecInterface> getDeclarationsToInstantiate();
	
	// Return an iterator for all of the declarations that the Type declares.
	public Iterable<DecInterface> getDeclarationsDeclared();

	// Should return the number of property declarations.
	public int getNumberOfDeclarations();
	
}