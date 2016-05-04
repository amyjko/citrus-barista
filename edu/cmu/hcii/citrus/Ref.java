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

import java.util.regex.Pattern;

public class Ref extends Expression<Element> {

	// Restricted in Boot.java
	public static final Dec<Text> token = new Dec<Text>(new Text(""));
	
	// Attempt to find the type or function statically.
//	public static final Dec<Element> binding = new Dec<Element>(true, new BaseElement<Element>() {
//		public Element evaluate(Element<?> env) {
//			Text tokText = ((Ref)env).get(token);
//			if(Character.isUpperCase(tokText.value.charAt(0)))
//				return Ref.resolve((Ref)env, null, tokText);
//			else return null;
//		}
//	});

	public Ref() {}
	public Ref(ArgumentList args) { super(args); }
	public Ref(String propName) { set(token, new Text(propName)); }

	public static enum ReferenceType { UNKNOWN, TYPE_OR_LANGUAGE, PROPERTY_OR_FUNCTION, KEYWORD }
	private ReferenceType typeOfReference = ReferenceType.UNKNOWN;

	private static Pattern keywordPattern = Pattern.compile("a|an|this|owner|enclosing|nothing|super");
	public Element evaluate(Element<?> env) { 

		// What name are we looking for?
		Text nameOfElementToFindText = peek(token);
		String nameOfElementToFind = nameOfElementToFindText.value;

		// If we haven't determined the reference type yet, do it now.
		if(typeOfReference == ReferenceType.UNKNOWN)
			typeOfReference = referenceTypeOf(nameOfElementToFind);
		
		Element result = resolve(this, env, nameOfElementToFindText, typeOfReference);
		if(result != null) return result;
		// Dump an error
		else { 

			Namespace<?> type = env.getType();
			Unit unit = (Unit)env.getType().ownerOfType(Boot.UNIT);
			Language lang = (Language)env.getType().ownerOfType(Boot.LANGUAGE);
			
			throw new ElementError("Couldn't find a " + typeOfReference + " with the name \"" + peek(token) + "\"" + 
								" from " + env + " of type " + env.getClass() + 
								", which has enclosing instance " + env.getEnclosingInstance() + ". " +
								"It's in type " + type + " which is in unit " + unit + 
								" which imports " + unit.get(Unit.uses), this);
		}
		
	}
	
	public static ReferenceType referenceTypeOf(String ref) {
		
		// Is it a keyword?
		if(keywordPattern.matcher(ref).matches()) 
			return ReferenceType.KEYWORD;
		
		// Is the first letter uppercase
		else if(Character.isUpperCase(ref.charAt(0))) {

			// Is it all upper case?
			boolean allUpper = true;
			for(int i = 1; i < ref.length() && allUpper; i++)
				if(!Character.isUpperCase(ref.charAt(i)))
					allUpper = false;
			return allUpper ? ReferenceType.PROPERTY_OR_FUNCTION : ReferenceType.TYPE_OR_LANGUAGE;			
			
		}
		// If not, then its a function or property
		else return ReferenceType.PROPERTY_OR_FUNCTION;
		
	}
	
	// The Citrus name resolution algorithm. Given a reference, environment, name, and type.
	public static Element resolve(Ref ref, Element instance, Text text, ReferenceType type) {
				
		String name = text.value;
		if(type == ReferenceType.UNKNOWN) type = referenceTypeOf(name);
		
		if(type == ReferenceType.KEYWORD) {
		
			// "a" or "an" is shorthand for the given instance, and is used in instantiations.
			if(name.equals("a") || name.equals("an")) return instance;
	
			// "this" refers to the instance of a type. If this is not an instanceof a type, we search
			// for the nearest enclosing instance that's an instance of a type.
			else if(name.equals("this")) {
				// Starting from the given instance, find the first instance of a Type.
				while(!(instance.getType() instanceof Type)) instance = instance.getEnclosingInstance();
				return instance;
			}
	
			// "owner" is the element that owns the property that owns "this"
			else if(name.equals("owner")) {
	
				// Starting from the given instance, find the first instance of a Type.
				while(!(instance.getType() instanceof Type)) instance = instance.getEnclosingInstance();
				if(instance == null) throw new ElementError("Somehow, there was no enclosing instance that was an instance of a type!", null);
				else {
					Element elOwner = instance.getElementOwner();
					// Convert the element owner to a nothing if its nothing.
					if(elOwner == null) return new Nothing();
					else return elOwner;
				}
	
			}
	
			// "enclosing" refers to the enclosing instance of this instance. In a funtion or let, this is
			// the environment in which they are executing. In an instance of a type, it refers to
			// the enclosing instance in which the instance was instantiated.
			else if(name.equals("enclosing")) {
				Element enclosing = instance.getEnclosingInstance();
				if(enclosing == null)
					throw new ElementError("" + instance + " has no enclosing instance.", null);
				return enclosing;
			}
	
			// "nothing" is the nothing literal, but we actually generate new instances of nothing because
			// they are objects as well.
			else if(name.equals("nothing")) return new Nothing();
	
			// "super" can only occur inside of an initialization of a property, where it
			// means the default value of the supertypes declaration of the property.
			else if(name.equals("super")) {
				
				// Get the declaration owner
				DecInterface dec = (DecInterface)ref.ownerOfType(Boot.DECLARATION);
				if(dec == null) throw new ElementError("" + ref + " isn't in a declaration, can't refer to super", null);
				else return dec.getDeclarationOverridden().getDefaultValue(instance);
				
			}
			else throw new ElementError("" + ref + " was supposed to be a keyword, but it didn't match any of the known keywords.", null);
			
		}
		// If this is a property, resolve in the context of its owner
//		else if(instance instanceof Property)
//			return Ref.resolve(ref, instance.getElementOwner(), text, type);
		// Citrus requires type and language references to be capitalized
		// and property and function references to be lower case. Speeds up
		// name resolution and improves readability.		
		else if(type == ReferenceType.TYPE_OR_LANGUAGE) {

			Element result = null;
		
			// If we're trying to resolve a name in the context of a Context (a type, function, language, etc.)
			// search it for types and static functions instead of searching its type for types and functions.
			if(instance instanceof Context) {

				result = ((Context)instance).getTypeNamed(name);
				if(result != null) return result;
			}

			// Search for a type if not a property
			Type typeFound = instance.getType().getTypeNamed(name);
			if(typeFound != null) return typeFound;

			// In case the enclosing instance is a context (for example, in an init), search it.
			Element enclosing = instance.getEnclosingInstance();
			if(enclosing instanceof Context) {
				typeFound = ((Context)enclosing).getTypeNamed(name);
				if(typeFound != null) return typeFound;
			}
			
			// Search for a language if not a type
			return Universe.getLanguage(name);
			
		}
		// If its lowercase, look for a property or function
		else if(type == ReferenceType.PROPERTY_OR_FUNCTION) {
				
			Element result = null;
			
			// Search the instance for a property of the given name. This implicitly
			// searches the enclosing instances of the instance for properties, so we don't 
			// have to do that here.
			result = instance.get(text);
			if(result != null) return result;

			// If we're trying to resolve a name in the context of a Context (a type, function, language, etc.)
			// search it for types and static functions instead of searching its type for types and functions.
			if(instance instanceof Context) {
				result = ((Context)instance).getFunctionNamed(text, Bool.TRUE);
				if(result != null) return result;
			}

			// Passing null to getFunctionNamed means it can either be a static or instance method;
			// we don't care.
			Function function = instance.getType().getFunctionNamed(text, null);
			if(function != null) return function;
		
			return null;

		}
		else {
			
			throw new ElementError("Don't know how to resolve reference type of " + type, null);

		}
		
	}

	public Type resultingType() { 

		String tok = get(token).value;
		
		Context env = get(context);
		if(env == null) return null;
		if(tok.equals("a") || tok.equals("an") || tok.equals("this")) return (Type)env;

		DecInterface declaration = env.getDeclarationOf(get(token));
		if(declaration != null) return declaration.getTypeExpression().getBaseType();
		Namespace type = env.getTypeNamed(get(token).value);
		if(type != null) return type.resultingType();
		return null;
		
	}

	public static class IsInEnv extends PropertyRestriction<Text> {
		public static final Dec<Bool> allowInvalid = new Dec<Bool>(new Bool(true));
		public boolean isValid(Property<Text> property, Text value) {
	
			Element<?> owner = property.getElementOwner();
			
			if(owner == null) return false;
			return true;
//			return owner.get(binding) != null;
//			Context env = owner.get(context);
//			if(env == null) return false;
//			return 
//				env.getDeclarationOf(value) != null || 
//				env.getTypeNamed(value.value) != null ||
//				env.getFunctionNamed(value.value) != null;
			
		}

		public Set<Text> getValidValues(Property<Text> property, Set<Text> values) {
			
			Element<?> owner = property.getElementOwner();
			if(owner == null) return values;
			Context<?> env = owner.get(context);
			if(env == null) return values;
			for(DecInterface<?> dec : env.getDeclarationsInContext(new Set<DecInterface>()))
				values.add(dec.get(Dec.name));
			for(Type type : env.getTypesInContext(new Set<Type>()))
				values.add(type.get(BaseType.name));
			for(Function function : env.getFunctionsInContext(new Set<Function>()))
				values.add(new Text(function.getName()));
			return values;
			
		}

		public String why(Property property) { return "Must be in environment"; }		
		
	}
	
	public Text toCitrus() { return new Text("" + peek(token)); }
	public String toString() { return toCitrus().value; }
	
}