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

import java.io.*;
import java.util.Hashtable;

import static edu.cmu.hcii.citrus.Boot.*;

public class Universe extends BaseElement<Universe> {

	public static Hashtable<String,Language> languages; // Set in Bootstrapping.init()
	public static void init() {}

	public Universe() {
		
		languages.put(CITRUS.getName(), CITRUS);

	}	
	
	public String print(Object o) { return o.toString(); }
	
	public Text properties(Element<?> env) {

		String result = "";
		if(env == null) return new Text(result);

		for(DecInterface dec : env.getType().getDeclarationsToInstantiate())
			result = result + "" + dec.getName() + "=" + env.get(dec) + "\n";

		if(env.hasa(environment.getName()).value)
			result = result + properties(env.get(environment)).value;

		return new Text(result);
		
	}
	
	public static Language include(Package p) { return getLanguage(p.getName()); }
	
	public static void include(Language newLanguage) {

		if(newLanguage == null) throw new NullPointerException("Can't include a null language!");
		
		// Does such a language exist?
		Language existingLanguage = languages.get(newLanguage.getName());
		
		// If so, put the language in the hash
		if(existingLanguage == null) languages.put(newLanguage.getName(), newLanguage);		
		// Otherwise, throw exception
		else throw new ElementError("Somehow, adding multiple languages of the same name " + newLanguage.getName(), null);

	}

	// Overrides find on itself so that if get fails, it searches the imported languages
	public Element get(Text name) {
		
		Element result = null;
		
		result = super.get(name);
		if(result != null) return result;

		for(Language language : languages.values()) {
			result = language.getTypeNamed(name.value);
			if(result != null) return result;
		}

		for(Language language : languages.values())
			if(language.getName().equals(name)) return language;

		throw new ElementError("Couldn't find anything named \"" + name + "\" in the universe", this);
		
	}
	
	public Type getTypeNamed(String name) {
				
		Type result = null;
		for(Language language : languages.values()) {
			result = language.getTypeNamed(name);
			if(result != null) return result;
		}
		return null;
		
	}

	public static Language getLanguageOf(Class classWhosePackageToConvert) { 

		Package p = classWhosePackageToConvert.getPackage();
		if(p == null) throw new ElementError("" + classWhosePackageToConvert + " has no package", null);
		return getLanguage(p.getName());

	}
	
	public static Language getLanguage(String name) {

		if(name.equals(BaseElement.class.getPackage().getName())) return Boot.CITRUS;

		Language lang = languages.get(name);
		if(lang != null) return lang;
		
		// If a Java package with this name exists, include it.
		Package p = Package.getPackage(name);
		if(p != null) {
			Language newLanguage = new Language(name);
			include(newLanguage);
			return newLanguage;
		}

		lang = loadLanguageNamed(name);
		if(lang != null) return lang;

		return lang;

	}

	public static Language makeLanguage(String name) {
		
		// If we couldn't load one, create one.
		Language lang = new Language(name);
		include(lang);
		return lang;

	}
	// Finds the folder with the given name in the language path and then loads all of its units.
	public static Language loadLanguageNamed(String languageName) {

		// Find the directory with the given name in the language path
		File languageFolder = new File(getLanguagePathname() + languageName);
		if(languageFolder.exists() && languageFolder.isDirectory()) {

			Language newLanguage = new Language(languageName);
			languages.put(newLanguage.getName(), newLanguage);
			
			for(File file : languageFolder.listFiles()) {
				if(file.getName().endsWith(".citrus")) {
					Unit unit = CitrusParser.unit(file);
					newLanguage.include(unit);
				}
			}
			newLanguage.extractTypesFromUnits();
			return newLanguage;
			
		}
		else return null;
		
	}

	public static boolean hasLanguage(String name) { return languages.containsKey(name); }

	public Nothing functions(Element env) {

		if(env == null) return new Nothing();

		Type type = (Type)env.getType();
		while(type != null) {

			System.err.println("Functions of " + type);
			for(Namespace dec : type.get(BaseType.types))
				System.err.println("" + dec.getName());
		
			if(env.hasa(environment.getName()).value)
				functions(env.get(environment));
			
			type = type.getPrototype();
			
		}
		
		return new Nothing();
		
	}

	public Namespace function(Text name, List parameters, Expression body) {
		
		BaseType newFunction = new BaseType();
		newFunction.set(BaseType.name, name);
		for(Object o : parameters)
			newFunction.get(BaseType.properties).append((DecInterface)o);	
		return newFunction;
		
	}


	// By default, the citrus path name is the working directory of the JVM.
	public static String citrusPath = System.getProperty("user.dir");
	public static String getCitrusPathname() { return citrusPath + File.separator + "Citrus" + File.separator; }
	public static String getLanguagePathname() { return getCitrusPathname() + "languages" + File.separator; }
	public static String getViewsPathname() { return getCitrusPathname() + "views" + File.separator; }
	public static String getImagesPathname() { return getCitrusPathname() + "images" + File.separator; }
	public static String getStylesPathname() { return getCitrusPathname() + "styles" + File.separator; }
	
	public static List subtypesOf(Namespace type) {
		
		List<Namespace> typeList = new List<Namespace>();
		for(Language l : languages.values())
			typeList.appendList(l.subtypesOf(type));
		return typeList;
		
	}

}
