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

public class When extends BaseElement<When> implements Context<When> {

	// The name of the event in the handler
	public static final BootDec<Text> name = new BootDec<Text>(new Text(""));
	
	// The subject expression, which evaluates to the element to listen to.
	public static final BootDec<Expression> subjectExpression = new BootDec<Expression>(null);

	// The event expression, which evaluates to the type of event to watch for.
	public static final BootDec<Expression> eventExpression = new BootDec<Expression>(null);

	// The code that should be executed when the event occurs.
	public static final BootDec<Expression> response = new BootDec<Expression>(null);
	
	// The arguments sent to the function
	public static final BootDec<List<Arg>> arguments = new BootDec<List<Arg>>(new List());

	public final List<DecInterface> declarations = new List<DecInterface>();
	
	public When(Text newName) { 
		
		super(); 
		
		Dec eventDeclaration = new Dec(false, new Nothing());
		eventDeclaration.set(Dec.name, newName);
		set(name, newName);
		declarations.append(eventDeclaration);
		
	}
	
	public Text toCitrus() { return new Text("Haven't implemented When.toCitrus()"); }
	public String toString() { 
		return 	"when " + 
				peek(name) + 
				" (" + 
				peek(subjectExpression) + " " + 
				peek(eventExpression) + " " + 
				peek(arguments) + " " + 
				peek(response); 
	}

	public Set getDeclarationsInContext(Set names) { return names; }
	public Set getTypesInContext(Set names) { return names; }
	public Set getFunctionsInContext(Set functions) { return functions; }
	public DecInterface getDeclarationOf(Text name) { return null; }
	public Type getTypeNamed(String name) { return ((Type)ownerOfType(Boot.TYPE)).getTypeNamed(name); }
	public Function getFunctionNamed(Text name, Bool isStatic) { return ((Type)ownerOfType(Boot.TYPE)).getFunctionNamed(name, isStatic); }

}