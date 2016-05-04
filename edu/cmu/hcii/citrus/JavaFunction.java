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

import java.lang.reflect.*;
import java.util.*;

public class JavaFunction extends BootElement<Function> implements Function {

	private final Text name;
	private final Vector<DecInterface> formalParameters = new Vector<DecInterface>();
	private final Method method;
	private final boolean isStatic = false;

	public JavaFunction(Method newMethod) { 

		// Remember the method!
		method = newMethod; 

		// Set the name!
		name = new Text(method.getName());
		
		// Declare the arguments
		int number = 1;
		for(Class<?> type : method.getParameterTypes()) {

			String argName = "arg" + number;
			BootDec dec = null;
			// This is a reference
			if(Element.class.isAssignableFrom(type)) dec = new BootDec(null, true);
			else throw new ElementError("JavaFunctions may only have element parameters, but the signature is " + method, null);

			dec.name = new Text(argName);
			
			// Make and add the declaration
			if(dec != null) {
				dec.parameterized = true;
				formalParameters.add(dec);

				// Next letter, starting at 'a'
				number++;

			}

		}
		
	}
	
	public String toString() {
		
		return name.value;
		
	}

	public boolean isStatic() { return isStatic; }

	public Element instantiate(ArgumentList arguments) {

		if(arguments.enclosingEnvironment == null) throw new ElementError("Can't call " + this + " on null", null);
		Element[] formalArguments = new Element[formalParameters.size()];
		int i = 0;
		for(ArgumentList.Argument arg : arguments.arguments)
			formalArguments[i++] = arg.value;
		
		try {
			return (Element)method.invoke(arguments.enclosingEnvironment, formalArguments);
		} catch (IllegalArgumentException e) {
			throw new ElementError("Wasn't able to invoke " + method + " on " + arguments.enclosingEnvironment + " using " + arguments + " because " + e, arguments.enclosingEnvironment);
		} catch (IllegalAccessException e) {
			throw new ElementError("Wasn't able to invoke " + method + " because " + e, null);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new ElementError("Wasn't able to invoke " + method + " because " + e.getCause(), null);
		}

	}

	public Element<?> getExpression() { return null; }
	public String getName() { return name.value; }
	public Bool isTypeOf(Namespace t) { return t.isTypeOf(Boot.EXPRESSION); }
	public Language getLanguage() { return Universe.getLanguage(BaseElement.class.getPackage().getName()); }
	public Iterable<DecInterface> getDeclarationsToInstantiate() { return formalParameters; }
	public Iterable<DecInterface> getDeclarationsDeclared() { return formalParameters; }

	public int getNumberOfDeclarations() { return formalParameters.size(); }

	public Iterable<DecInterface<?>> getStaticDeclarations() {
		// TODO Auto-generated method stub
		return null;
	}

	public DecInterface<?> getDeclarationOf(Text name) {

		for(DecInterface<?> dec : formalParameters)
			if(dec.getName().value.equals(name.value)) return dec;
		return null;
		
	}

	////////////////////////
	// ElementInterface
	////////////////////////
	
	// PROPERTIES
	public Set<Property<?>> getProperties() { throw new ElementError("Haven't implemented this in JavaFunction", null); }
	public Property getProperty(Text name) { throw new ElementError("Haven't implemented this in JavaFunction", null); }
	public boolean hasPropertyNamed(String name) { throw new ElementError("Haven't implemented this in JavaFunction", null); }
	public Element get(Text name) { throw new ElementError("Should be able to get(name) on a JavaFunction, but haven't implemented yet.", null); }
	public <ValueType extends Element<?>> ValueType get(DecInterface<ValueType> declaration) { 
	
		if(declaration == BaseType.name) return (ValueType)name;
		else throw new ElementError("Haven't implemented this in JavaFunction", null); 
		
	}
	public <ValueType extends Element<?>> boolean set(DecInterface<ValueType> name, ValueType value) { throw new ElementError("Haven't implemented this in JavaFunction", null); }

	// DUPLICATION
	public Element duplicate() { throw new ElementError("Haven't implemented this in JavaFunction", null); }

	// TRANSLATION
	public Text toCitrus() { return name; }
	public Text toCitrusReference() { return name; }

	// EVALUATION
	public Function evaluate(Element<?> env) { return this; }

	public Namespace<?> getType() { return Boot.TYPE; }

	// CONTEXT
	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) {

		for(DecInterface param : formalParameters) names.add(param);
		((Namespace)ownerOfType(Boot.TYPE)).getDeclarationsInContext(names);
		return names;
		
	}

	public Type getTypeNamed(String name) { return null; }
	public Set<Type> getTypesInContext(Set<Type> names) { 
		
		return ((Namespace)ownerOfType(Boot.TYPE)).getTypesInContext(names); 
		
	}
	
	public Function getFunctionNamed(Text name, Bool isStatic) { return null; }
	public Set<Function> getFunctionsInContext(Set<Function> names) { 
		
		return ((Namespace)ownerOfType(Boot.TYPE)).getFunctionsInContext(names); 
		
	}
	
	public Bool isEquivalentTo(Element<?> o) { return new Bool(this.equals(o)); }

}