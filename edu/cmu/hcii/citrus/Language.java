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

import java.util.Hashtable;

import edu.cmu.hcii.citrus.views.Translator;

// Represents a named set of Citrus compilation units.
public class Language extends BaseElement<Unit> implements Context<Unit> {

	public static final BootDec<Text> name = new BootDec<Text>(new Text(""));
	public static final BootDec<List<Unit>> units = new BootDec<List<Unit>>(new List<Unit>());

	// A dictionary of types by name, for quick lookup. All of the language's units are
	// aggregated into this table.
	public Hashtable<String,Type> types = new Hashtable<String,Type>(100);

	public Language(String newName) { set(name, new Text(newName)); }
	public Language(ArgumentList args) { super(args); }
	
	public void extractTypesFromUnits() {
		
		for(Unit unit : get(units))			
			include(unit.get(Unit.types));
		
	}
	
	public Nothing extractViews() {
		
		for(Type type : types.values()) {
			if(type instanceof BaseType)
				Translator.addView((BaseType)type);
		}
		return null;
		
	}
	
	public void include(Iterable<Type> iterable) {

		for(Type t : iterable) include(t);
		
	}

	// The central way add a type to a language.
	private void include(Type type) {

		Type existingType = types.get(type.getName());
		if(existingType == null) {
			
			types.put(type.getName(), type);
		
		}
		else if(existingType != type) throw new ElementError("" + get(name) + " already has a type named " + type.getName(), null);
		
		
	}
	public Nothing include(Unit unit) { 
		
		get(units).append(unit); 
		include(unit.get(Unit.types));
		return null; 
	}
	
	
	public List subtypesOf(Namespace type) {
		
		List<Namespace> typeList = new List<Namespace>();
		for(Namespace t : types.values())
			if(t.isTypeOf(type).value) typeList.append(t);
		return typeList;
		
	}

	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> names) { return names; }
	public Set<Type> getTypesInContext(Set<Type> names) { return names; }
	public Set<Function> getFunctionsInContext(Set<Function> functions) { return functions; }
	public DecInterface<?> getDeclarationOf(Text name) { return null; }
	public Function getFunctionNamed(Text name, Bool isStatic) { return null; }


	// A cache of types by name
	private Hashtable<String,Type> javaTypesByName = new Hashtable<String,Type>(50);
	// A cache of the package represented by this language
	public Package javaPackage = null;

	public Type getTypeNamed(String name) { 
		
		// There are several primitive types that are included by default,
		// which always resolve first.
		if(name.equals(Boot.ELEMENT.getName())) return Boot.ELEMENT;
		else if(name.equals(Boot.TEXT.getName())) return Boot.TEXT;
		else if(name.equals(Boot.BOOL.getName())) return Boot.BOOL;
		else if(name.equals(Boot.CHAR.getName())) return Boot.CHAR;
		else if(name.equals(Boot.REAL.getName())) return Boot.REAL;
		else if(name.equals(Boot.INT.getName())) return Boot.INT;
		else if(name.equals(Boot.PROPERTY.getName())) return Boot.PROPERTY;
		else if(name.equals(Boot.SET.getName())) return Boot.SET;
		else if(name.equals(Boot.LIST.getName())) return Boot.LIST;
		else if(name.equals(Boot.FUNCTION.getName())) return Boot.FUNCTION;
		else if(name.equals(Boot.NOTHING.getName())) return Boot.NOTHING;
		else if(name.equals(Boot.TYPE.getName())) return Boot.TYPE;
		else if(name.equals("Dictionary")) return Reflection.getJavaType(Dictionary.class);

		Type t = types.get(name); 
		if(t != null) return t;

		// Can we find it in the units?
		for(Unit unit : peek(units)) {
			for(Type type : unit.peek(Unit.types)) {
				if(type.getName().equals(name)) {
					include(type);
					return type;
				}
			}
		}

		// Does the javaTypesByName table have it?
		Type javaType = javaTypesByName.get(name);
		if(javaType != null) return javaType == Boot.NOTHING ? null : javaType;

		// Have we cached the java package yet?
		if(javaPackage == null) javaPackage = Package.getPackage(getName());

		// Does a class of this name exist in a Java package of this language's name?
		if(javaPackage != null) {
			try {
				Class c = Class.forName(getName() + "." + name);
				if(Element.class.isAssignableFrom(c)) {
					javaType = Reflection.getJavaType(c);
					javaTypesByName.put(name, javaType);
					include(javaType);
					return javaType;
				}
			} 
			// If we didn't find one, put Nothing in the table to remember the failure.
			catch(ClassNotFoundException e) { 
				javaTypesByName.put(name, Boot.NOTHING); 
			}
			catch(NoClassDefFoundError e) {}
		}

		return null;
		
	}

	public String getName() { return peek(name).value; }
	
	public String toString() { return getName(); }
	
}
