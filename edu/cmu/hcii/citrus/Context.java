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

public interface Context<ContextType extends Element> extends Element<ContextType> {

	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names);
	public Set<Type> getTypesInContext(Set<Type> names);
	public Set<Function> getFunctionsInContext(Set<Function> functions);

	// Searches the type and its prototypes for types, functions, etc.
	public DecInterface<?> getDeclarationOf(Text name);
	public Type getTypeNamed(String name);
	public Function getFunctionNamed(Text name, Bool isStatic);

}