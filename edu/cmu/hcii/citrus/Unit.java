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

import static edu.cmu.hcii.citrus.Boot.*;

public class Unit extends BaseElement<Unit> implements Context<Unit> {

	public static final BootDec<Text> language = new BootDec<Text>(new Text(""));
	public static final BootDec<List<Text>> uses = new BootDec<List<Text>>(new List<Text>());
	public static final BootDec<List<Type>> types = new BootDec<List<Type>>(new List<Type>());
	public static final BootDec<Expression> init = new BootDec<Expression>(null);
	private String filename = "";
	
	public Unit() { this((ArgumentList)null, null, null, null); }
	public Unit(ArgumentList arguments) { this(arguments, null, null, null); }
	public Unit(String fileName, String language) { this(null, fileName, language, null); }
	public Unit(ArgumentList arguments, String fileName, String languageName, Iterable<Type> newTypes) {

		super(arguments);
		filename = fileName;
		if(languageName != null) set(language, new Text(languageName));
		if(newTypes != null) for(Type t : newTypes) get(types).append(t);
		
	}

	public String getLanguageName() { return peek(Unit.language).toString(); }
	
	public Nothing usesLanguage(Language languageUsed) {

		if(languageUsed == null) throw new NullPointerException("Received null for language to use");
//		if(languageUsed.getName().equalsIgnoreCase(get(language).value))
//			throw new ElementError("This unit uses " + languageUsed.getName() + "; a unit can't used the language its declared in", null);
		if(!get(uses).contains(languageUsed.get(Language.name)).value)
			get(uses).append(languageUsed.get(Language.name));		
		return null;

	}
	
	// Returns true if this language contains a type with the given name.
	public boolean hasTypeNamed(String name) {

		for(Namespace type : getTypes())
			if(type.getName().equals(name)) return true;
		return false;
		
	}
	
	public Function getFunctionNamed(Text name, Bool isStatic) { return null; }
	public Set<Function> getFunctionsInContext(Set<Function> functions) { return functions; }

	public DecInterface getDeclarationOf(Text name) { return null; }
	public Set<DecInterface> getDeclarationsInContext(Set<DecInterface> declarations) { return declarations; }

	public Type getTypeNamed(String name) {

		Type t = null;

		t = getLanguage().getTypeNamed(name);
		if(t != null) return t;

		t = getImportedType(name);
		return t;
		
	}

	public Language getLanguage() { return (Language)peekAtOwnerOfType(Boot.LANGUAGE); }
	
	private Type getImportedType(String name) {

		Type t = null;

		for(Text languageName : peek(uses)) {
			if(!languageName.isEquivalentTo(this).value) {
				Language lang = Universe.getLanguage(languageName.value);
				if(lang != null) {
					t = lang.getTypeNamed(name);
					if(t != null) return t;
				}
				else System.err.println("Couldn't find language " + languageName);
			}
		}

		return null;

	}
	
	public List<Type> getTypes() { return peek(Unit.types); }
	public Namespace<?> getType() { return UNIT; }

	public Set<Type> getTypesInContext(Set<Type> typesSet) {
		
		for(Type t : peek(types)) typesSet.add(t);
		for(Text languageName : peek(uses)) {
			if(!languageName.isEquivalentTo(this).value) {
				Language lang = Universe.getLanguage(languageName.value);
				for(Type t : lang.peek(types)) typesSet.add(t);
			}
		}
		for(Type t : Boot.CITRUS.get(types)) typesSet.add(t);
		return typesSet;
		
	}
	
	// Add the type, checking for a type of a duplicate name.
	public void addType(Type newType) {
		
		if(hasTypeNamed(newType.getName()))
			throw new ElementError("Found a type of the same name in " + this + "," + newType, this);		
		else get(types).append(newType);
		
	}

	// Search through the known types for types that are of the given type.
	public Set<Type> getConcreteTypesOfType(Type prototype) {

		Set<Type> s = new Set<Type>();		
		for(Type t : get(types)) if(t.isConcrete() && t.isTypeOf(prototype).value) s.add(t);	
		return s;
		
	}
	
	public String typesToString() {
		
		String typesString = "language " + getLanguageName();
		return typesString;

	}
	
	public String toString() { return "Unit of " + getLanguage() + " with types " + getTypes(); }

	public Text toCitrusReference() { return get(language); }
	public Text toCitrus() { 

		String str = "language " + get(language) + "\n\n";
		for(Text use : get(uses)) str = str + "uses " + use.toCitrus();
		str = str + "\n";
		for(Namespace t : getTypes()) str = str + t.toCitrus() + "\n";
		return new Text(str);
		
	}
	
}