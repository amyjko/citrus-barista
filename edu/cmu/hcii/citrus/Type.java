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

public interface Type extends Namespace<Type> {

	// Returns true if the element can be created.
	public boolean isConcrete();
	
	public Type getEnclosingType();
	
	// Should return this type's prototype.
	public Type getPrototype();

	// Adds this declaration to a type's list of instance or static declarations, depending on the declaration's static flag.
	// Throws an exception if a declaration with the give name already exists.
	public Nothing declareProperty(DecInterface<?> newDeclaration);

	// Adds the given inner type to the type's list of type declarations.
	public Nothing declareType(Type newType);
	public Nothing declareFunction(Function newFunction);
	
	// Should return an iterator for all of the static declarations for this type.
	public Iterable<DecInterface<?>> getStaticDeclarations();

	// Constructs a canonical list of property declarations that should be used to instantiate
	// elements of this type, by searching through this type's prototypes and finding all of the
	// property declarations that need to be instantiated, and in what order.
	public void consolidate();
	
	public boolean needsConsolidation();

	public Type getInnerTypeNamed(String name);
	
}
