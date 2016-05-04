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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static edu.cmu.hcii.citrus.Boot.*;

public class BaseType extends BaseElement<Type> implements Type {

	///////////////////////////////
	// JAVA SUPPORT
	private static Class[] typeTableArguments = { Namespace.class, ArgumentList.class };
	private static Class[] tableArgument = { ArgumentList.class };
	private static Class[] noArguments = {};

	protected Class<?> javaElementClass;
	private Constructor<?> constructorWithTypeAndArguments;
	private Constructor<?> constructorWithArguments;
	private Constructor<?> constructorWithoutArguments;
	//////////////////////////////////

	public static final BootDec<Text> name = new BootDec<Text>(new Text(""));
	public static final BootDec<List<TypeVariable>> typeVariables = new BootDec<List<TypeVariable>>(new List<TypeVariable>());
	public static final BootDec<TypeExpression> prototype = new BootDec<TypeExpression>(null);
	public static final BootDec<List<DecInterface>> properties = new BootDec<List<DecInterface>>(new List<DecInterface>());
	public static final BootDec<List<Type>> types = new BootDec<List<Type>>(new List<Type>());
	public static final BootDec<List<Function>> functions = new BootDec<List<Function>>(new List<Function>());
	public static final BootDec<List<Function>> staticFunctions = new BootDec<List<Function>>(new List<Function>());
	public static final BootDec<List<When>> handlers = new BootDec<List<When>>(new List<When>());
	public static final BootDec<List<DecInterface<?>>> staticProperties = new BootDec<List<DecInterface<?>>>(new List<DecInterface<?>>());
	public static final BootDec<Bool> concrete = new BootDec<Bool>(new Bool(true));
	public static final BootDec<Expression> evaluation = new BootDec<Expression>(null);
	public static final BootDec<Bool> debug = new BootDec<Bool>(new Bool(true));

	// A pointer to the type that contains this type.
	public Type enclosingType = null;	

	// These are used at runtime to create instances of this type.
	// Null indicates that this hasn't been consolidated yet.
	private Hashtable<String, DecInterface> declarationsToInstantiate = null;

	public BaseType() { this((ArgumentList)null); }
	public BaseType(Class<?> newJavaElementClass) { 

		this((ArgumentList)null);
		
		javaElementClass = newJavaElementClass;

	}
	public BaseType(ArgumentList args) { super(TYPE, args); }
	
	public boolean isConcrete() { return bool(BaseType.concrete); }
	
	private String nameCache = null;
	public String getName() { 
	
		if(nameCache == null)
			nameCache = peek(BaseType.name).value;
		return nameCache;
		
	}
	
	public Language getLanguage() { return getOwnerOfType(Language.class); }

	public BaseType evaluate(Element<?> env) { return this; }

	// Searches the hashtable for a property of the given name.
	public Property<?> getProperty(Text name) { 

		Property<?> p = super.getProperty(name);
		if(p != null) return p;
		
		// Does this have a static property by this name?
		DecInterface<?> declaration = getStaticDeclarationNamed(name);
		if(declaration != null)
			return addProperty(declaration.make(this)).initialize(null, false, null);
		
		return null;

	}

	public Iterable<DecInterface> getDeclarationsToInstantiate() { 
	
		if(needsConsolidation()) consolidate();
		return declarationsToInstantiate.values(); 
		
	}
	public Iterable<DecInterface> getDeclarationsDeclared() { return get(BaseType.properties); }

	public DecInterface getNthDeclaredDeclaration(int n) {
		
		int i = 1;
		for(DecInterface dec : getDeclarationsDeclared())
			if(i == n) return dec;
		return null;
		
	}

	public Nothing declareProperty(DecInterface<?> declaration) {
		
		if(declaration.isStatic()) {
			if(getStaticDeclarationNamed(declaration.getName()) == null)
				get(staticProperties).append(declaration);
			else throw new ElementError("" + this + " already has a static declaration with name " + declaration.getName(), null);
		}
		else {
			for(DecInterface<?> pd : get(properties))
				if(pd.getName().equals(declaration.getName()))
					throw new ElementError("" + this + " already has a declaration with name " + declaration.getName(), null);			
			get(properties).append(declaration);
		}
		return null;
		
	}
	
	public Nothing declareType(Type newType) {
	
		for(Type type : get(types))
			if(type.getName().equals(newType.getName())) {
				System.err.println("Warning: not overwriting existing definition of " + newType.getName());
//				throw new ElementError("" + this + " already has a type with the name " + newType.getName() + ": " + newType, this);
			}
		get(types).append(newType);
		return null;
	}
	
	public Nothing declareFunction(Function newFunction) {

		List<Function> functionListToAppendTo = newFunction.isStatic() ? get(staticFunctions) : get(functions);
		for(Function fun : functionListToAppendTo)
			if(fun.getName().equals(newFunction.getName()))
				System.err.println("Warning: not overwriting existing definition of " + newFunction.getName());

		functionListToAppendTo.append(newFunction);			
		return null;
	}
	
	// Returns the type name of the property of the given name by searching in the property declarations
	// list for a property declaration with the given name.
	public DecInterface<?> getDeclarationOf(Text name) { 

		if(needsConsolidation()) consolidate();
		return declarationsToInstantiate.get(name.value);
		
	}

	public DecInterface getStaticDeclarationNamed(Text name) {
		
		for(DecInterface<?> pd : getStaticDeclarations())
			if(pd.getName().value.equals(name.value)) return pd;

		return null;
		
	}

	public Iterable<DecInterface<?>> getStaticDeclarations() { return peek(BaseType.staticProperties); }
	
	// Returns true if this type, or it's prototype's type, etc. is the given type.
	public Bool isTypeOf(Namespace t) {

		Element prototype = getPrototype();

		if(t == Boot.ELEMENT) return Bool.TRUE;
		else if(this == t) return Bool.TRUE;
		// Otherwise, if this has a prototype, ask if its prototype if the prototype is of the given type.
		else if(prototype instanceof Namespace) return ((Namespace)prototype).isTypeOf(t);
		// Otherwise, return false.
		else return Bool.FALSE;
		
	}
	
	// Abstract Element methods
	public Namespace<?> getType() { return Boot.TYPE; }
	
	public Type getPrototype() { return peek(prototype).getBaseType(); }

	public Type getEnclosingType() { return enclosingType; }

	// Instantiates a new element.
	public Element instantiate(ArgumentList arguments) { 

		if(needsConsolidation()) consolidate();
		Element enclosingInstance = null;
		if(enclosingType != null) {
			
			if(arguments.enclosingEnvironment == null) {
				debug("Should be sending enclosing instance to " + this);
				throw new ElementError("" + this + " is an inner type of " + enclosingType + 
						" and thus an enclosing instance must be sent in the argument table in order to instantiate it.", this);
			}

		}
		if(!bool(concrete))
			throw new ElementError("Can't instantiate an abstract type", this);

		// Create a new instance of the given type.
		Element<?> newElement = null;
		if(javaElementClass == null) newElement = new BaseElement(this, arguments);
		else {

			try {
				
				if(arguments != null && !arguments.isEmpty()) {
				
					if(constructorWithTypeAndArguments != null) {
						Object[] args = { this, arguments };
						newElement = (Element)constructorWithTypeAndArguments.newInstance(args);
					}
					else if(constructorWithArguments != null) {
						Object[] args = { arguments };
						newElement = (Element)constructorWithArguments.newInstance(args);	
					}
					else throw new ElementError("" + javaElementClass + " has no constructor that takes arguments, but I have these arguments " + arguments, this);
	
				}
				else {
					if(constructorWithTypeAndArguments != null) {
						Object[] args = { this, new ArgumentList() };
						newElement = (Element)constructorWithTypeAndArguments.newInstance(args);
					}
					else if(constructorWithoutArguments == null)
						if(constructorWithArguments == null)
							throw new ElementError("" + javaElementClass + " has no constructor with no arguments!", this);
						else { 			
							Object[] args = { arguments };
							newElement = (BaseElement)constructorWithArguments.newInstance(args);	
						}
					else {
						newElement = (BaseElement)javaElementClass.newInstance();
					}
				}
	
			}
			catch(IllegalAccessException i) { throw new ElementError("Can't create a " + javaElementClass + " of type " + this + ": " + i, this); } 
			catch(InstantiationException i) { throw new ElementError("Can't create a " + javaElementClass + " of type " + this + ": " + i, this); }
			catch(InvocationTargetException i) {
				i.getCause().printStackTrace(); 
				throw new ElementError("Exception thrown during construction of a " + javaElementClass, this);
			}
			catch(IllegalArgumentException i) { throw new ElementError("When instantiating " + this + ", " + i, null); }
	
			if(newElement.getType() != this) {
				if(constructorWithTypeAndArguments == null) System.err.println("NO CONSTRUCTOR WITH TYPE AND ARGUMENTS");
				if(constructorWithArguments == null) System.err.println("NO CONSTRUCTOR WITH ARGUMENTS");
				throw new ElementError("Instantiated " + this + " but ended up with an element of type " + newElement.getType() + "\n" +
										"Must not have provided a constructor with a Type parameter.", this);
			}
		
		}
		
		return newElement;

	}

	// Constructs a canonical list of property declarations that should be used to instantiate
	// elements of this type, by searching through this type's prototypes and finding all of the
	// property declarations that need to be instantiated, and in what order.
	//
	// Also sets each property's declaration overriden, based on the property's type's supertype.
	public void consolidate() {
		
		// If we haven't consolidated yet, instantiate a new table of declarations to instantiate.
		if(declarationsToInstantiate == null) declarationsToInstantiate = new Hashtable<String,DecInterface>(20);
		// Otherwise, we'll start from scratch.
		else declarationsToInstantiate.clear();

		// Add all of the declarations that this particular type declares.
		// Keep track of properties that are exposed.
		for(DecInterface<?> p : getDeclarationsDeclared()) {
			
			declarationsToInstantiate.put(p.getName().value, p);
			if(p instanceof Dec) ((Dec)p).cachedName = p.getName();
			
		}

		// Now, go through each prototype of the given type, and insert any property declarations that
		// have names that have not been used at the beginning of the list.
		// For each property of a given name already included, set the overriden declaration.
		Hashtable<DecInterface,DecInterface> declarationsOverriden = new Hashtable<DecInterface,DecInterface>(20);
		Type prototype = get(BaseType.prototype).getBaseType();
		if(prototype == null) { throw new ElementError("Couldn't find " + getName() + "'s prototype named " + get(BaseType.prototype).getBaseTypeName(), null); }
		else if(prototype.needsConsolidation()) prototype.consolidate();
		
		if(javaElementClass == null && prototype instanceof BaseType)
			javaElementClass = ((BaseType)prototype).javaElementClass;

		if(javaElementClass != null) {
			try { constructorWithTypeAndArguments = javaElementClass.getConstructor(typeTableArguments); } catch(NoSuchMethodException e) {}		
			try { constructorWithoutArguments = javaElementClass.getConstructor(noArguments); } catch(NoSuchMethodException e) {}		
			try { constructorWithArguments = javaElementClass.getConstructor(tableArgument); } catch(NoSuchMethodException nsme) {}
		}

		while(prototype != ELEMENT && prototype != null) {

			for(DecInterface<?> p : prototype.getDeclarationsDeclared()) {

				boolean alreadyIncluded = false;
				
				// Does the table of declarations to instantiate already have a property by this name?
				DecInterface declarationInTable = declarationsToInstantiate.get(p.getName().value);

				// If not, add it to the table (and to the exposed list).
				if(declarationInTable == null) declarationsToInstantiate.put(p.getName().value, p);
				// If it is already in the table, and we haven't overridden it yet, make sure its overriding this immediate declaration.
				else if(declarationsOverriden.get(declarationInTable) == null) {
					declarationInTable.setDeclarationOverriden(p);
					declarationsOverriden.put(declarationInTable, p);
				}
				
			}
			
			// Get the prototype's prototype.
			Type prototypesPrototype = ((Type)prototype).getPrototype();			
			if(prototypesPrototype == null) {
				System.err.println("Couldn't find " + prototype + "'s prototype!");
				declarationsToInstantiate = null;
				return;				
			}
			else prototype = prototypesPrototype;
			
		}	
		
		// Now that we're done, consolidate this type's inner types.
		for(Type type : get(BaseType.types)) type.consolidate();
				
		// Is this an inner type itself? If so, assign its enclosing type for convenience,
		// so we don't have to search for it again.
		Type newEnclosingType = (Type)ownerOfType(Boot.TYPE);
		if(newEnclosingType != null) enclosingType = newEnclosingType;

	}

	// Creates a subclass that overrides all of this type's declarations with duplicate value functions
	public BaseType createSubType(Text name) {

		BaseType subType = new BaseType();

		// Set the prototype to this type
		TypeExpression newTypeExpression = new BaseTypeExpression(this);
		subType.set(BaseType.prototype, newTypeExpression);

		// Assign the name
		subType.set(BaseType.name, name);

		return subType;
		
	}
	
	private Hashtable<String,Element> cachedFunctions = new Hashtable<String,Element>(20);
	public Function getFunctionNamed(Text name, Bool isStatic) {
		
		if(isStatic == null || isStatic.value) return getFunctionNamedHelper(name, isStatic);

		Element fun = cachedFunctions.get(name.value);
		if(fun == null) {
			Function f = getFunctionNamedHelper(name, isStatic);
			if(f == null) cachedFunctions.put(name.value, new Nothing());
			else cachedFunctions.put(name.value, f);
			return f;
		}
		else if(fun.isNothing().value) return null;
		else return (Function)fun;
		
	}
	private Function getFunctionNamedHelper(Text name, Bool isStatic) {

		if(isStatic != null) {
			for(Function function : isStatic.value ? peek(staticFunctions) : peek(functions))
				if(function.getName().equals(name.value))
					return function;
		}
		else {
			for(Function function : peek(functions)) if(function.getName().equals(name.value)) return function;
			for(Function function : peek(staticFunctions)) if(function.getName().equals(name.value)) return function;			
		}

		// If there's an enclosing type, does it have a type by this name?
		if(enclosingType != null) {
			Function function = enclosingType.getFunctionNamed(name, isStatic);
			if(function != null) return function;
		}

		// Does this type's prototype have a type by this name?
		Type proto = getPrototype();
		if(proto != null) return proto.getFunctionNamed(name, isStatic);
		else return null;

	}

	private Hashtable<String,Element> cachedTypes = new Hashtable<String,Element>(10);
	public Type getTypeNamed(String name) {
		
		Element type = cachedTypes.get(name);
		if(type == null) {
			Type t = getTypeNamedHelper(name);
			if(t == null) cachedTypes.put(name, new Nothing());
			else cachedTypes.put(name, t);
			return t;
		}
		else if(type.isNothing().value) return null;
		else return (Type)type;
		
	}
	private Type getTypeNamedHelper(String name) {

		// Does type have a type of the given name?
		for(Type t : peek(types))
			if(t.getName().equals(name)) return t;

		Type type = null;

		// Does this type's prototype have a type by this name?
		Type proto = getPrototype();
		while(proto != null) {
			type = proto.getInnerTypeNamed(name);
			if(type != null) return type;
			proto = proto.getPrototype();
		}

		// If there's an enclosing type, does it have a type by this name?
		if(enclosingType != null) { 
			return enclosingType.getTypeNamed(name);
		}
		// Otherwise, this must be in a language. Does it have a type by this name?
		else {
			
			
			// This should be peeking at the language owner, otherwise, the owners will be part of
			// constraints.
			Unit lang = (Unit)peekAtOwnerOfType(Boot.UNIT);
			if(lang != null) return lang.getTypeNamed(name);
			
		}
		
		return null;
		
	}
	
	public Type getInnerTypeNamed(String name) {
		
		for(Type type : peek(types))
			if(type.getName().equals(name)) return type;		
		return null;
		
	}
	
	public int getNumberOfDeclarations() { 
		
		if(needsConsolidation()) consolidate();
		return declarationsToInstantiate.size(); 
		
	}
	
	public boolean needsConsolidation() { return declarationsToInstantiate == null; }
	
	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) {
		
		for(DecInterface dec : getDeclarationsDeclared()) names.add(dec);

		Namespace proto = getPrototype();
		if(proto != null) proto.getDeclarationsInContext(names);
		return names;

	}

	public Set<Type> getTypesInContext(Set<Type> names) {
		
		for(Type type : get(types)) names.add(type);
		Type proto = getPrototype();
		if(proto != null) proto.getTypesInContext(names);
		getOwnerOfType(Unit.class).getTypesInContext(names);
		return names;

	}

	public Set<Function> getFunctionsInContext(Set<Function> names) {
		
		for(Function type : get(functions)) names.add(type);
		Namespace proto = getPrototype();
		if(proto != null) proto.getFunctionsInContext(names);
		return names;

	}
	
	// The context for the prototype is the type's surrounding context (a type or language)
	// while the rest of the type gets the type itself.
	public Context contextFor(Element e) { 

		if(e == peek(prototype)) {
			if(enclosingType != null) return enclosingType;
			else return (Unit)ownerOfType(Boot.UNIT);
		}
		else return this; 
		
	}

	public String declarationsToString() {
		
		if(needsConsolidation()) throw new ElementError("Can't get declarations to instantiate before consolidation", this);
		String s = "";
		for(DecInterface pd : getDeclarationsToInstantiate()) 
			s = s + "\nhas a " + pd;
		return s;
		
	}
	
	public String toString() { return getName(); }
	
	public Text toCitrusReference() { return peek(name); }
	public Text toText() { return peek(name); }
	public Text toCitrus() {
		
		String str = "a " + peek(name) + " is a " + peek(prototype) + " that\n\n";
		for(DecInterface dec : get(properties)) str = str + dec.toCitrus() + "\n";		
		return new Text(str); 
		
	}
		
}