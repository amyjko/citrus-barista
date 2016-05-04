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

import java.util.Hashtable;
import java.util.Vector;

public class Reflection {
	
	private static final boolean debug = false;
	
	// A convenience cache of types by class.
	private static final Hashtable<Class,Type> classTypes = new Hashtable<Class,Type>(500);

	// We track the classes being reflected upon in order to debug cycles.
	private static final Vector<Class<?>> classesBeingReflectedUpon = new Vector<Class<?>>();
	
	public static Iterable<Type> getTypes() { return classTypes.values(); }
	public static void includeTypeForClass(Class c, Type t) { classTypes.put(c, t); }
	
	public static void debug(String message) { if(debug) System.err.println("Reflection : " + message); }
	public static void warning(String message) { System.err.println("Warning" + message); }
	
	public static String getLanguageNameFromClass(Class c) {
	
		Package classPackage = c.getPackage();
		return (classPackage == null) ? "default" : c.getPackage().getName();
	
	}

	public static String getTypeNameFromClass(Class c) {
		
		// The classes name, without the package name.
		String name = c.getName().substring(c.getName().lastIndexOf('.') + 1);
		
		// The class name without the preceding Name$Name
		int indexOfDollarSign = name.lastIndexOf('$');
		if(indexOfDollarSign > 0)
			name = name.substring(name.lastIndexOf('$') + 1);

		return name;
	
	}

	// Defines most of a type's properties given a Java class. 
	// Searches for all final fields that are subclasses of Declaration and adds them as property declarations.
	// Also creates functions for each method with an appropriate signature.
	public static void defineTypeFromClass(Type typeToDefine, Class<? extends Element> classToReflectUpon) {
		
		debug("Defining " + classToReflectUpon);
		
		if(classesBeingReflectedUpon.contains(classToReflectUpon))
			throw new ElementError("There's a cycle in the type dependencies: " + classesBeingReflectedUpon, null);

		// Push the class on the stack.
		classesBeingReflectedUpon.insertElementAt(classToReflectUpon, 0);
		
		//////////////////////////////////////////////////////
		// Class name -> Type name
		// 		Set the name of the type to the name of the class.
		//////////////////////////////////////////////////////
		typeToDefine.set(BaseType.name, new Text(Reflection.getTypeNameFromClass(classToReflectUpon)));

		//////////////////////////////////////////////////////
		// Class's Package name -> Type's language name
		// 		Set the language name to the class' package name
		//////////////////////////////////////////////////////

		// Create a language for this package if we haven't yet.
		String languageName = Reflection.getLanguageNameFromClass(classToReflectUpon);
		Language typesLanguage = Universe.getLanguage(languageName);
		debug("" + classToReflectUpon + " is in language " + typesLanguage);

		Unit typesUnit = null;
		if(typesLanguage != null) {
			
			// Add the unit to the language if it's not enclosed in another type.
			if(classToReflectUpon.getEnclosingClass() == null) {
				
				// Create a unit for this type and add the type to its type list.
				typesUnit = new Unit(classToReflectUpon.getCanonicalName(), typesLanguage.getName());
				typesUnit.get(Unit.types).append(typeToDefine);
				typesLanguage.include(typesUnit);
			}

		}

		//////////////////////////////////////////////////////
		// abstract -> !concrete
		//////////////////////////////////////////////////////
		typeToDefine.set(BaseType.concrete, new Bool(!java.lang.reflect.Modifier.isAbstract(classToReflectUpon.getModifiers())));

		//////////////////////////////////////////////////////
		// Class's superclass -> Type's supertype
		// 		Set the type's prototype to the type that represents the class's superclass. Note that
		// 		"Element" is not a valid superclass. We don't want things that are both subclasses
		// 		of elements to be assignable.
		//////////////////////////////////////////////////////

		// Should be getting the complete type expression this way
//		java.lang.reflect.Type genericSupertype = classToReflectUpon.getGenericSuperclass();
//		typeToDefine.set(BaseType.prototype,
//				(typeToDefine instanceof BootType) ? 
//				getTypeExpressionFromJavaType(BootTypeExpression.class, genericSupertype) :
//				getTypeExpressionFromJavaType(BaseTypeExpression.class, genericSupertype));

		// But instead, we're just getting the base type.
		Class superclass = classToReflectUpon.getSuperclass();
		TypeExpression elementType = Boot.makeBootTypeExpressions ?
				new BootTypeExpression(Boot.ELEMENT) : new BaseTypeExpression(Boot.ELEMENT);
		if(superclass != null) {
			if(classToReflectUpon == BaseElement.class) {}
			else if(BaseElement.class.isAssignableFrom(superclass)) typeToDefine.set(BaseType.prototype, new BootTypeExpression(getJavaType(superclass)));
			else if(Element.class.isAssignableFrom(superclass)) typeToDefine.set(BaseType.prototype, new BootTypeExpression(getJavaType(superclass)));//elementType);
			else typeToDefine.set(BaseType.prototype, elementType);
		}
		else if(classToReflectUpon.isInterface()) {
			Class[] interfaces = classToReflectUpon.getInterfaces();
			if(interfaces.length == 1)
				typeToDefine.set(BaseType.prototype, new BootTypeExpression(getJavaType(interfaces[0])));				
			else throw new ElementError("" + classToReflectUpon + " isn't allowed to extend more than one Java interface", null);
		}

		//////////////////////////////////////////////////////
		// Fields -> PropertyDecs
		//////////////////////////////////////////////////////
		Field[] fields = classToReflectUpon.getDeclaredFields();

		// Indicates whether any of the fields were null, which tells us if the class has
		// been fully initialized yet.
		boolean foundAllDeclarations = true;
		
		// For each field, check if its of type PropertyDecInterface, and if so, add the declaration.
		for(Field field : fields) {

			Vector<Type> typesUsed = new Vector<Type>(10);

			if(DecInterface.class.isAssignableFrom(field.getType())) {

				// If public static final
				if(	Modifier.isPublic(field.getModifiers()) &&
					Modifier.isFinal(field.getModifiers()) &&
					Modifier.isStatic(field.getModifiers())) {

					try {

						DecInterface<?> declaration = null;
						try {
							declaration = (DecInterface)field.get(classToReflectUpon);
						}
						catch(ExceptionInInitializerError e) { 
							warning("Exception when getting " + field + ":\n" + e.getCause()); 
							e.printStackTrace();
						}

						// If the declaration is null, then we'll have to come back and add the declaration later.
						if(declaration == null) {
							warning("Tried to reflect on " + field.getName() + " in " + classToReflectUpon.getName() + ", but it was null.");
							foundAllDeclarations = false;
						}
						// Otherwise, if the type doesn't contain it yet, append it.
						else {

							if(typeToDefine instanceof BootType && !(declaration instanceof BootDec))
								throw new ElementError("BootTypes may only declare BootDecs", null);
							
							// If the field is overriden, set it's name to the name of the declaration overriden. Otherwise,
							// set the name to the name of the field.
							DecInterface<?> declarationOverriden = declaration.getDeclarationOverridden();
							if(declarationOverriden != null) declaration.set(Dec.name, declarationOverriden.getName());
							else declaration.set(Dec.name, new Text(field.getName()));

							// Declare the declaration, letting the type worry about whether its static or not.
							typeToDefine.declareProperty(declaration);

							// Converted the field's type to a Citrus type expression.
							java.lang.reflect.Type genericType = field.getGenericType();
							if(genericType instanceof Class) 
								warning("" + classToReflectUpon + "'s " + genericType + " doesn't specify a property type");
							else if(genericType instanceof ParameterizedType) {

								java.lang.reflect.Type[] args = ((ParameterizedType)genericType).getActualTypeArguments();
								genericType = args[0];
								declaration.set(Dec.typeExpression,
									(declaration instanceof BootDec) ? 
									getTypeExpressionFromJavaType(BootTypeExpression.class, genericType, typesUsed) :
									getTypeExpressionFromJavaType(BaseTypeExpression.class, genericType, typesUsed));
								
							}
							else System.err.println("Don't know how to reflect on " + genericType);
							
							// If the declaration overrides one of its supertype's declarations, make sure
							// it's the appropriate supertype's.
							if(declaration.getDeclarationOverridden() != null) {
	
								Type proto = typeToDefine.getPrototype();
								DecInterface firstDeclarationOfName = null;
								while(firstDeclarationOfName == null && proto != null) {
									firstDeclarationOfName = proto.getDeclarationOf(declaration.getName());
									proto = proto.getPrototype();
								}
								
								// The declaration's elaboration should be identical to the first declaration
								// found in the supertypes.
								if(firstDeclarationOfName != declaration.getDeclarationOverridden())
									throw new ElementError("" + declaration + " tries to override " + 
														declaration.getDeclarationOverridden() + 
														", but the most immediate declaration to override is " + 
														firstDeclarationOfName, typeToDefine);

							}

						}
						
					} catch(IllegalAccessException iae) { 
						
						System.err.println("Illegal access on " + field + ":\n" + iae); 
						Thread.dumpStack(); 
						
					}
					
				}
				
				// This is our hack for importing without being able to reflect on imports in 
				// a java file. We include the 
				for(Type type : typesUsed) {
					Language lang = type.getLanguage();
					if(typesUnit != null && lang != null) typesUnit.usesLanguage(lang);
				}

			}
			else if(When.class.isAssignableFrom(field.getType())) {

				// If public static final
				if(	Modifier.isPublic(field.getModifiers()) &&
					Modifier.isFinal(field.getModifiers()) &&
					Modifier.isStatic(field.getModifiers())) {

					When when = null;
					try {
						when = (When)field.get(classToReflectUpon);
					}
					catch(ExceptionInInitializerError e) { 
						warning("Exception when getting " + field + ":\n" + e.getCause()); 
						e.printStackTrace();
					}
					catch(IllegalAccessException e) { e.printStackTrace(); } 

					if(when != null) typeToDefine.get(BaseType.handlers).append(when);
					
				}
				
			}
			
		}
		
		// Declare the functions
		defineFunctionsFromClass(typeToDefine, classToReflectUpon);
		
		//////////////////////////////////////////////////////
		// Class's declared classes-> Inner Types
		//////////////////////////////////////////////////////

		Class[] innerTypes = classToReflectUpon.getDeclaredClasses();
		
		// Are any of the inner types "public static ... extends ElementInterface" Elements?
		for(Class<?> innerClass : innerTypes) {
			
			if(	Modifier.isPublic(innerClass.getModifiers()) &&
				Modifier.isStatic(innerClass.getModifiers()) &&
				Element.class.isAssignableFrom(innerClass.getSuperclass())) {

				Type innerType = getJavaType((Class<? extends Element>)innerClass);
				typeToDefine.declareType(innerType);
				if(innerType instanceof BaseType) ((BaseType)innerType).enclosingType = typeToDefine;

			}
			
		}

		//////////////////////////////////////////////////////
		// Consolidate the new type
		//////////////////////////////////////////////////////

		// Construct the list of declarations to instantiate when an instance of this type is created.
		if(!foundAllDeclarations) throw new ElementError("Didn't find all declarations in type " + classToReflectUpon, null);

		// Pop the class off the stack.
		classesBeingReflectedUpon.remove(0);
		
	}
	
	public static void defineFunctionsFromClass(Type typeToDefine, Class<? extends Element> classToReflectOn) {

		//////////////////////////////////////////////////////
		// Class's public declared methods-> Functions
		//////////////////////////////////////////////////////
		Method[] methods = classToReflectOn.getDeclaredMethods();
		for(Method method : methods) {

			// Public instance methods get converted to functions
			if(	Modifier.isPublic(method.getModifiers()) &&
				!Modifier.isStatic(method.getModifiers()) &&
				!Modifier.isVolatile(method.getModifiers())) {

				// Are the method's parameters and return type of type ElementInterface
				boolean validSignature = true;
				for(Class<?> parameter : method.getParameterTypes())
					if(!Element.class.isAssignableFrom(parameter)) validSignature = false;
				if(!Element.class.isAssignableFrom(method.getReturnType())) {
					validSignature = false;
				}
				
				// Make a subclass of a JavaFunction, creating adding a parameterized declaration for
				// each parameter of the method.
				if(validSignature) {
					typeToDefine.declareFunction(new JavaFunction(method));	
				}
				
			}			
		
		}		

	}


	public static TypeExpression getTypeExpressionFromJavaType(Class typeOfTypeExpressionToMake, java.lang.reflect.Type genericType, Vector<Type> types) {

		debug("Getting type expression for " + genericType);
		try {

			TypeExpression typeExpression = (TypeExpression)typeOfTypeExpressionToMake.newInstance();
			Type baseType = Boot.ELEMENT;
	
			Class typeArgument = null;
			if(genericType instanceof Class) baseType = getJavaType((Class<? extends Element>)genericType);
			else if(genericType instanceof ParameterizedType) {

				// Get the raw type.
				baseType = getJavaType((Class<? extends Element>)((ParameterizedType)genericType).getRawType());

				// Add a new type expression for each type argument.
				java.lang.reflect.Type[] args = ((ParameterizedType)genericType).getActualTypeArguments();
				for(java.lang.reflect.Type t : args) {
					if(!(t instanceof WildcardType))
						typeExpression.getTypeArguments().append(getTypeExpressionFromJavaType(typeOfTypeExpressionToMake, t, types));
				}

			}
			else if(genericType instanceof WildcardType) {}
			else warning("Don't know how to handle a " + genericType);

			// Set the base type of the type expression we've created.
			if(typeOfTypeExpressionToMake == BootTypeExpression.class) ((BootTypeExpression)typeExpression).type = baseType;
			else {
				((BaseTypeExpression)typeExpression).set(BaseTypeExpression.type, baseType);
				((BaseTypeExpression)typeExpression).set(BaseTypeExpression.name, baseType.get(BaseType.name));
			}
			
			// Add the base type to the types used list.
			types.add(baseType);

			return typeExpression;

		}
		catch(IllegalAccessException e) { e.printStackTrace(); } 
		catch(InstantiationException e) { e.printStackTrace(); }
		
		return null;
		
	}

	// Get the Type for the given class.
	public static Type getJavaType(Class<? extends Element> classToReflectUpon) {

		// Have we already created a class for the given type?
		Type type = classTypes.get(classToReflectUpon);
		
		// If not, create one, implicitly putting it in the table.
		if(type == null) {
			debug("Didn't find definition for " + classToReflectUpon);
			type = new BaseType(classToReflectUpon);
			includeTypeForClass(classToReflectUpon, type);
			defineTypeFromClass(type, classToReflectUpon);			
		}
		
		// Return the type.
		return type;
	
	}

}