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

public abstract class Expression<TypeEvaluatesTo extends Element> extends BaseElement<TypeEvaluatesTo> {

	// This is actually constrained to "((this getElementOwner) contextFor this)" in Boot
	public static final BootDec<Context<?>> context = new BootDec<Context<?>>(null, true);

	public Expression() { super(); }
	public Expression(Namespace type, ArgumentList arguments) { super(type, arguments); }
	public Expression(ArgumentList arguments) { super(arguments); }

	public abstract TypeEvaluatesTo evaluate(Element<?> env);
	
	public String toString() {

		String str = "(" + getType().getName();
		for(DecInterface<?> pd : getType().getDeclarationsToInstantiate())
			str = str + " " + pd.getName() + "=" + get(pd) + " ";
		
		str = str.substring(0, str.length() - 1);
		str = str + ")";
		return str;

	}
	
}
