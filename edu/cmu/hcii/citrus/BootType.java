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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static edu.cmu.hcii.citrus.Boot.*;

public class BootType extends BootElement<Type> implements Type {

	private static Vector<BootType> bootstrappedTypes = new Vector<BootType>(20);
	public static Iterable<BootType> getBootstrappedTypes() { return bootstrappedTypes; }
	
	private static Class[] tableArgument = { ArgumentList.class };

	private boolean initialized = false;
	private boolean initializing = false;
	public String name = "UNINITIALIZED";
	private BootTypeExpression prototype = null;
	private Hashtable<String, DecInterface> propertyDeclarations = new Hashtable<String,DecInterface>();
	private Hashtable<String, Type> types = new Hashtable<String,Type>();
	private Hashtable<String, Function> functions = new Hashtable<String,Function>();
	private boolean concrete;
	public Class<? extends Object> javaClass;
	private Hashtable<String, DecInterface> declarationsToInstantiate = null;

	public BootType(Class javaClass, Class ... otherClasses) {
		
		this.javaClass = javaClass;

		// Map the given java class to this type in the languages table.
		Reflection.includeTypeForClass(javaClass, this);
		for(Class c : otherClasses) Reflection.includeTypeForClass(c, this);

		// Keep track of all of the bootstrapped types so that we can initialize them later.
		bootstrappedTypes.add(this);
		
	}

	// We delay the definition of bootstrapped types until they are all declared.
	public void initialize() {
		
		if(initializing) { System.err.println("Already initializing boot type named " + this + "!"); Thread.dumpStack(); }
		if(initialized) return;

		// Do this before defining the type, since otherwise, it will initialize itself before consolidating.
		initialized = true;
		initializing = true;
		
		name = Reflection.getTypeNameFromClass(javaClass);
		if(Element.class.isAssignableFrom(javaClass))
			Reflection.defineTypeFromClass(this, (Class<? extends Element>)javaClass);
		
		// Print a bit of status while we're bootstrapping
		System.err.print(".");
		
		initializing = false;
		
	}
	
	public final Namespace<?> getType() { return Boot.TYPE; }

	
	///////////////////////////////////////////////////////////////////
	//
	// ElementInterface
	//
	///////////////////////////////////////////////////////////////////

	// PROPERTIES
	public Set<Property<?>> getProperties() { throw new ElementError("Haven't implemented BootstrappedType.getProperties()", this); }
	public Property getProperty(Text name) { return null; }
	public boolean hasPropertyNamed(String testName) {

		return testName.equals("name") || testName.equals("prototype") || testName.equals("concrete");
		
	}
	
	public Element get(Text nameToFind) {
		
		if(nameToFind.value.equals("name")) return new Text(name);
		else if(nameToFind.value.equals("prototype")) return prototype;
		else if(nameToFind.value.equals("concrete")) return new Bool(concrete);
		else return null;
		
	}

	// Should return the value of the property of the given name.
	public <ValueType extends Element<?>> ValueType get(DecInterface<ValueType> declaration) {
		
		if(declaration == BaseType.name) return (ValueType)new Text(name);
		else if(declaration == BaseType.prototype) return (ValueType)prototype;
		else if(declaration == BaseType.concrete) return (ValueType)(new Bool(concrete));
		else if(declaration == BaseType.types) return (ValueType)new List();
		else throw new ElementError("Couldn't find property named " + declaration + " in bootstrapped type ", this);

	}

	public <ValueType extends Element<?>> boolean set(DecInterface<ValueType> dec, ValueType value) {

		if(dec == null) throw new ElementError("Bootstrapped type representing " + javaClass +" was passed a null property declaration!", null);
		if(dec == BaseType.name) name = ((Text)value).value;
		else if(dec == BaseType.concrete) concrete = ((Bool)value).value;
		else if(dec == BaseType.prototype) prototype = ((BootTypeExpression)value);
		else throw new ElementError("Couldn't set " + dec, this);		
		return true;
		
	}

	// DUPLICATION
	public Element duplicate() { return this; }

	// EQUIVALENCE
	public Bool isEquivalentTo(Element<?> e) { return new Bool(e == this); }

	// EVALUATION
	public Type evaluate(Element<?> env) { return this; }	

	// TRANSLATION
	public String toString() { return name; }
	public Text toCitrus() { return new Text("A " + name + " is a " + prototype.toCitrusReference()); }
	public Text toText() { return new Text(name); }
	public Text toCitrusReference() { return new Text(name); }
	
	///////////////////////////////////////////////////////////////////
	//
	// TypeInterface
	//
	///////////////////////////////////////////////////////////////////
	
	public String getName() { return name; }
	public Type getPrototype() { return (this == Boot.ELEMENT) ? null : prototype.getBaseType(); }
	public Type getEnclosingType() { return null; }
	public boolean isConcrete() { return concrete; }

	// Returns true if this type, or it's prototype's type, etc. is the given type.
	public Bool isTypeOf(Namespace t) {

		if(this == t) return new Bool(true);
		// Otherwise, if this has a prototype, ask if its prototype if the prototype is of the given type.
		else if(getPrototype() != null) return getPrototype().isTypeOf(t);
		// Otherwise, its not a prototype
		else return new Bool(false);

	}
	
	public String getLanguageName() { return Element.class.getPackage().getName(); }
	public Language getLanguage() { return CITRUS; }
	
	public Element instantiate(ArgumentList arguments) {

		if(needsConsolidation()) consolidate();
		
		if(arguments == null) {
			
			try {
				return (Element)javaClass.newInstance();
			} 
			catch(IllegalAccessException i) { throw new ElementError("Can't create a " + javaClass + "" + i, null); } 
			catch(InstantiationException i) { throw new ElementError("Can't create a " + javaClass + "" + i, null); }
			
		}
		else {
		
			// Create a new instance of the given type.
			try { 
				Object[] args = { arguments };
				return (Element)javaClass.getConstructor(tableArgument).newInstance(args);
			}
			catch(NoSuchMethodException nsme) { throw new ElementError("" + javaClass + " doesn't have an argument constructor", null); }
			catch(InvocationTargetException e) { throw new ElementError("Exception when executing constructor for " + javaClass + "\n" + e.getCause(), null); }
			catch(IllegalAccessException i) { throw new ElementError("Can't create a " + javaClass + "" + i, null); } 
			catch(InstantiationException i) { throw new ElementError("Can't create a " + javaClass + "" + i.getCause(), null); }
			
		}
		
	}

	public Iterable<DecInterface> getDeclarationsToInstantiate() { 
	
		if(needsConsolidation()) consolidate();
		return declarationsToInstantiate.values(); 
		
	}
	public Iterable<DecInterface> getDeclarationsDeclared() { 
	
		// Make sure this type is initialized before return this.
		initialize();
		return propertyDeclarations.values(); 
		
	}
	public DecInterface getNthDeclaredDeclaration(int n) { 
		throw new ElementError("Bad idea. BootstrappedType's declarations are stored as a hashtable, and so they aren't ordered.", this);
	}
	public Iterable<DecInterface<?>> getStaticDeclarations() { return new Vector<DecInterface<?>>(0); }
	public Iterable<DecInterface<?>> getDeclarationsExposed() { return new Vector<DecInterface<?>>(0); }	

	public Nothing declareProperty(DecInterface<?> newDeclaration) {
		
		if(propertyDeclarations.get(newDeclaration.getName()) != null)
			throw new ElementError("" + this + " already has a property declaration of name " + newDeclaration.getName(), this);
		propertyDeclarations.put(newDeclaration.getName().value, newDeclaration);
		return null;
		
	}

	public Nothing declareType(Type newType) {
		
		if(types.contains(newType.getName()))
			System.err.println("Warning, overwriting type " + types.get(newType.getName()));
		else types.put(newType.getName(), newType);
		return null;
		
	}
	
	public Nothing declareFunction(Function newFunction) {
		
		if(functions.contains(newFunction.getName()))
			System.err.println("Warning, overwriting function " + functions.get(newFunction.getName()));
		else functions.put(newFunction.getName(), newFunction);
		return null;
		
	}

	// Constructs a canonical list of property declarations that should be used to instantiate
	// elements of this type, by searching through this type's prototypes and finding all of the
	// property declarations that need to be instantiated, and in what order.
	public void consolidate() {
		
		if(!initialized) initialize();
		
		if(declarationsToInstantiate == null) declarationsToInstantiate = new Hashtable<String,DecInterface>(20);
		else declarationsToInstantiate.clear();

		if(this == ELEMENT) return;

		// Add all of the property declarations in this Type.
		for(DecInterface<?> pd : getDeclarationsDeclared())
			declarationsToInstantiate.put(pd.getName().value, pd);

		// Now, go through each prototype of the given type, and insert any property declarations that
		// have names that have not been used at the beginning of the list.
		Type proto = getPrototype();
		while(proto != null && proto != ELEMENT) {

			for(DecInterface<?> p : proto.getDeclarationsDeclared())
				if(!declarationsToInstantiate.containsKey(p.getName().value)) 
					declarationsToInstantiate.put(p.getName().value, p);
			
			// Get the prototype's prototype.
			proto = proto.getPrototype();
			
		}
				
	}

	public boolean needsConsolidation() { return declarationsToInstantiate == null; }
	
	public int getNumberOfDeclarations() { 
	
		initialize();
		if(needsConsolidation()) consolidate();
		return declarationsToInstantiate.size(); 
		
	}

	public DecInterface<?> getDeclarationOf(Text name) { 
		
		initialize();
		if(needsConsolidation()) consolidate();
		return declarationsToInstantiate.get(name.value); 
		
	}

	public Function getFunctionNamed(Text name, Bool isStatic) {

		Function function = functions.get(name.value);
		if(function != null) return function;

		Type proto = getPrototype();
		if(proto != null) {
			function = proto.getFunctionNamed(name, isStatic);
		}
		return function;

	}
	public Type getTypeNamed(String name) { 

		// Does type have a type of the given name?
		Type type = types.get(name);
		if(type != null) return type;

		// Does this type's prototype have a type by this name?
		Type proto = getPrototype();
		while(proto != null) {
			type = proto.getInnerTypeNamed(name);
			if(type != null) return type;
			proto = proto.getPrototype();
		}

		// Otherwise, this must be in a language. Does it have a type by this name?
		Unit lang = (Unit)ownerOfType(Boot.UNIT);
		if(lang == null) throw new ElementError("How is it that this BootType " + getName() + " doesn't have a Unit owner?", null);
		return lang.getTypeNamed(name);

	}
	
	public Type getInnerTypeNamed(String name) {
		
		return types.get(name);
		
	}
	
	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) {
		
		for(DecInterface dec : getDeclarationsDeclared()) names.add(dec);

		Namespace proto = getPrototype();
		if(proto != null) proto.getDeclarationsInContext(names);
		return names;

	}

	public Set<Type> getTypesInContext(Set<Type> names) {
		
		for(Type type : types.values()) names.add(type);
		Type proto = getPrototype();
		if(proto != null) proto.getTypesInContext(names);
		Unit lang = (Unit)ownerOfType(Reflection.getJavaType(Unit.class));
		if(lang != null) for(Type type : lang.get(Unit.types)) names.add(type);
		return names;

	}
	
	public Set<Function> getFunctionsInContext(Set<Function> names) {
		
		for(Function type : functions.values()) names.add(type);
		Namespace proto = getPrototype();
		if(proto != null) proto.getFunctionsInContext(names);
		return names;

	}

}
