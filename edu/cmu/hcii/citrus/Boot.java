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

import java.util.*;

public class Boot {
	
	// This method, when called, is only invoked after all of the static initializer code is executed below.
	public static void init() {}
			
	/////////////////////////////////////////////////////
	//
	// This static initialization code creates all of the
	// Citrus types that are necessary for instantiating Citrus
	// elements. This code must run before any other Citrus element
	// is created.
	// 
	/////////////////////////////////////////////////////

	static { 
		
		System.err.print("Starting Citrus ..."); 
		Universe.languages = new Hashtable<String,Language>(20);
	
	}
	
	// Avoid validating anything while creating the Citrus language.
	static { BootDec.validate = false; }

	// Initalize ALL of the bootstrapped types before initializing any of them.
	// Note that the first class we send in the list is the one that's analyzed for
	// types, names, properties and functions.
	public static final BootType ELEMENT = new BootType(BaseElement.class, Element.class, AbstractElement.class, BootElement.class);
	public static final BootType EXPRESSION = new BootType(Expression.class);
	public static final BootType BOOL = new BootType(Bool.class);	
	public static final BootType CHAR = new BootType(Char.class);	
	public static final BootType REAL = new BootType(Real.class);
	public static final BootType INT = new BootType(Int.class);
	public static final BootType TEXT = new BootType(Text.class);

	public static final BootType PARAMETER = new BootType(Parameter.class);
	public static final BootType GROUP = new BootType(Group.class);
	public static final BootType LIST = new BootType(List.class);
	public static final BootType ARRAY = new BootType(Array.class);

	public static final BootType WHEN = new BootType(When.class);
	
	public static final BootType VALUE_RESTRICTION = new BootType(PropertyRestriction.class);
	public static final BootType DECLARATION = new BootType(Dec.class, DecInterface.class);
	public static final BootType CONTEXT = new BootType(Context.class);
	public static final BootType NAMESPACE = new BootType(Namespace.class);
	public static final BootType FUNCTION = new BootType(Function.class);
	public static final BootType TYPE = new BootType(BaseType.class, Type.class, BootType.class);
	public static final BootType TYPE_VARIABLE = new BootType(TypeVariable.class);
	public static final BootType TYPE_EXPRESSION = new BootType(BaseTypeExpression.class, BootTypeExpression.class, TypeExpression.class);
	public static final BootType UNIT = new BootType(Unit.class);
	public static final BootType LANGUAGE = new BootType(Language.class);

	public static final BootType NOTHING = new BootType(Nothing.class);

	public static final BootType PROPERTY = new BootType(Property.class);
	public static final BootType SET = new BootType(Set.class);

	public static final BootType ARGUMENT = new BootType(Arg.class);

	public static boolean makeBootTypeExpressions = true;
	
	static { 

		// Once we've created all of the bootstrapped types, initialize all of them.
		Reflection.defineTypeFromClass(ELEMENT, AbstractElement.class);
		for(BootType type : BootType.getBootstrappedTypes()) type.initialize(); 

		// Correct the names of the ones with interfaces
		ELEMENT.name = Reflection.getTypeNameFromClass(Element.class);
		TYPE.name = Reflection.getTypeNameFromClass(Type.class);
		TYPE_EXPRESSION.name = Reflection.getTypeNameFromClass(TypeExpression.class);
		
	}	

	// Create and initialize the bootstrapped version of the Citrus language using all of the types created.
	public static final Language CITRUS = new Language(BaseElement.class.getPackage().getName());
	public static final Unit CITRUS_UNIT = new Unit();
	static { 
		for(Type t : Reflection.getTypes()) CITRUS_UNIT.get(Unit.types).append(t);
		CITRUS.include(CITRUS_UNIT); 
	}

	// Consolidate all of the bootstrapped types.
	static {
		
		for(BootType type : BootType.getBootstrappedTypes()) type.consolidate();

	}
	
	static {

		// Now add value restrictions to each of the property declarations that need them. We can't do it
		// where they're declared, since we need to have already initialized the complete language.

		BaseTypeExpression.type.functionIsConstraint = true;
		BaseTypeExpression.type.defaultValue = new BaseElement<Type>() {
			public Type evaluate(Element<?> env) {
				Context context = env.get(Expression.context);
				if(context == null) return null;
				else {
					Type t = context.getTypeNamed(env.get(BaseTypeExpression.name).value);
					return t;
				}
			}
		};
		Expression.context.functionIsConstraint = true;
		Expression.context.defaultValue = new BaseElement<Context>() {
			public Context evaluate(Element<?> env) { 
				Element<?> owner = env.getElementOwner();
				if(owner == null) return null;
				return owner.contextFor(env); 
			}
		};
//		BaseTypeExpression.name.is(new BaseTypeExpression.IsInEnv());

		// We can now stop making boot type expressions
		makeBootTypeExpressions = false;

		// Set up default values we couldn't set up before
		BaseType.prototype.defaultValue = new BaseTypeExpression(Boot.ELEMENT);
		Dec.typeExpression.defaultValue = new BaseTypeExpression(ELEMENT);

		Bool.TRUE = new Bool(true);
		Bool.FALSE = new Bool(false);

		// TypeExpression base type names must be valid type names
//		BaseTypeExpression.name.is(new PropertyRestriction("(name matches \"[a-zA-Z]*\")", "name"));

		// Language names must be one or more characters
		Unit.language.is(new PropertyRestriction("(language matches \"[a-zA-Z0-9\\\\.]+\")", "language"));

		// Property declaration names must be one or more characters
//		Dec.name.is(new PropertyRestriction("(name matches \".+\")", "value"));

		// Type names must be one or more characters
//		BaseType.name.is(new PropertyRestriction("(name matches \".+\")", "value"));

		// No whitespace.
//		Ref.token.is(new PropertyRestriction("((value matches \"[\\\\s\\\\[\\\\]\\\\(\\\\)\\\"\\\\.@=<`']*\") not))", null));
		// Not "true" or "false".
		Ref.token.is(new PropertyRestriction("((token matches \"true|false\") not)", "token"));
		// Can't start with a number
		Ref.token.is(new PropertyRestriction("((token matches \"[0-9].*\") not)", "token"));
		// Must be at least one character, but we'll allow zero temporarily.
//		Ref.token.is(new PropertyRestriction("(value matches \".+\")", "value"));			

		BoolLiteral.token.is(new PropertyRestriction("(token matches \"true|false\")", null));
//		TextLiteral.token.is(new PropertyRestriction("(token matches \"(.|\\\\.)*\")", null)); 

		// A valid floating point number:
		// 		- or + (zero or one times)
		// 		a sequence of numbers, followed by a period (zero or one times)
		// 		a sequence of one or more numbers
		RealLiteral.token.is(new PropertyRestriction("(token matches \"[-+]?([0-9]*\\.)?[0-9]*\")", null)); 

		IntLiteral.token.is(new PropertyRestriction("(token matches \"[-+]?[0-9]+\")", null)); 
		CharLiteral.token.is(new PropertyRestriction("(token matches \".|\\\\\\.\")", null)); 

		Property.VALUE_WILL_CHANGE = Reflection.getJavaType(Property.ValueWillChange.class);
		Property.VALUE_CHANGED = Reflection.getJavaType(Property.ValueChanged.class);
		Property.VALIDITY_WILL_CHANGE = Reflection.getJavaType(Property.ValidityWillChange.class);
		Property.VALIDITY_CHANGED = Reflection.getJavaType(Property.ValidityChanged.class);
		Property.MARKED_OUT_OF_DATE = Reflection.getJavaType(Property.OutOfDate.class);
		Property.CYCLE_DETECTED = Reflection.getJavaType(Property.CycleDetected.class);

	}

	static { System.err.println(" done."); }
	
	// Now that we're done creating the language, we can validate.
	static { BootDec.validate = true; }

}