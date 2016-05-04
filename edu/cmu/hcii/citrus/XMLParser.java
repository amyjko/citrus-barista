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

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

public class XMLParser {
	
	public static Text readTextFrom(Text path) { return new Text(new String(readCharactersFrom(path))); }

	public static char[] readCharactersFrom(Text path) {

		return readCharactersFrom(new File(path.value));
		
	}
	
	public static char[] readCharactersFrom(File file) {
		
		char[] characters = new char[(int)file.length() * 4];
		try {
			FileReader reader = new FileReader(file);
			int numberOfCharacters = reader.read(characters, 0, characters.length);
			reader.close();
		} catch(java.io.FileNotFoundException ex) {
			System.err.println("Couldn't find file " + file + ": " + ex);
			return null;
		} catch(java.io.IOException ex) {
			System.err.println("IO Exception: " + ex);
			return null;
		}
		return characters;
		
	}

	public static Element readXMLFrom(Text path) {
		
		// Read the file
		Text text = readTextFrom(path);
		String[] tokenArray = text.value.split("[<>]");
		Vector<String> tokens = new Vector<String>();
		for(String tok : tokenArray) {
			tok = tok.trim();
			if(!tok.equals("")) tokens.add(tok);
		}
		
		Vector<Language> languagesUsed = new Vector<Language>();
		readToken("File", tokens, "Expected <File> at beginning of file");
		while(tokens.firstElement().equals("uses")) {
			readToken("uses", tokens, "Expected <uses>");
			String languageName = tokens.remove(0);
			Language language = Universe.getLanguage(languageName);
			if(language == null) {
				throw new ElementError("Couldn't find language named " + languageName, null);
			}
			languagesUsed.add(language);
			readToken("/uses", tokens, "Expected </uses>");
		}
		readToken("/File", tokens, "Expected </File> after uses declarations");
		
		return readElement(tokens, languagesUsed);
		
	}

	private static String readToken(String expectedString, Vector<String> tokens, String error) {

		if(tokens.firstElement().equals(expectedString)) return tokens.remove(0);
		else throw new ElementError("" + error, null);
		
	}
	
	private static Element<?> readElement(Vector<String> tokens, Vector<Language> languages) {
		
		// (1) Read the tag name
		String typeName = tokens.remove(0);
		
		// (2) Find the type
		Type t = null;
		for(Language l : languages) {
			t = l.getTypeNamed(typeName);
			if(t != null) break;
		}

		if(t == null) { throw new ElementError("Couldn't find a type named " + typeName + " among " + languages + "\n " + tokens, null); }//; return null; }
		
		ArgumentList arguments = new ArgumentList();

		// (4) Read properties until we read the terminating flag
		while(!tokens.firstElement().startsWith("/")) {

			// Eat the opening tag
			String propertyName = tokens.remove(0);

			// What's the type of the property?
			DecInterface<?> declaration = t.getDeclarationOf(new Text(propertyName));
			if(declaration == null) { System.err.println("" + t + " doesn't declare a property named " + propertyName); return null; }
			Type propertyType = declaration.getTypeExpression().getBaseType();
			if(propertyType == null)
				throw new ElementError("Why wasn't " + declaration.ownerOfType(Boot.LANGUAGE) + 
						"'s " + t + "'s declaration " + declaration + " type named " + 
						declaration.getTypeExpression().getBaseTypeName() + " ?", null);
			
			Element value = null;

			if(tokens.firstElement().equals("nothing")) {
				tokens.remove(0);
				value = new Nothing();
			}
			else if(propertyType == Boot.LIST) {

				List<Element> list = new List();
				while(!tokens.firstElement().startsWith("/"))
					list.append(readElement(tokens, languages));
				value = list;
				
			}
			else if(propertyType == Boot.SET) {

				Set<Element> list = new Set();
				while(!tokens.firstElement().startsWith("/"))
					list.add(readElement(tokens, languages));
				value = list;
				
			}
			else if(propertyType == Boot.REAL) {
				value = new Real(Double.parseDouble(tokens.remove(0)));
			}
			else if(propertyType == Boot.TEXT) {
				if(tokens.firstElement().startsWith("/")) value = new Text("");
				else {
					String tok = tokens.remove(0);
					tok = tok.substring(1, tok.length() - 1);
					tok = tok.replace("\\\"", "\"");
					tok = tok.replace("\\n", "\n");
					tok = tok.replace("\\t", "\t");
					tok = tok.replace("\\b", "\b");
					value = new Text(tok);
				}
			}
			else if(propertyType == Boot.BOOL) {
				value = new Bool(Boolean.valueOf(tokens.remove(0)));
			}
			else {
//				if(declaration.isReferenceOnly()) { 
//					String reference = tokens.remove(0);
//					System.err.println("Don't know how to resolve reference to " + reference);
//					value = null;
//				}
//				else 
					value = readElement(tokens, languages);
			}
			
			if(value != null) arguments.add(propertyName, false, value);

			// Remove the property closing tag
			readToken("/" + propertyName, tokens, "Expected " + "</" + propertyName + ">");
			
		}
		
		// Read the closing element tag
		readToken("/" + typeName, tokens, "Expected " + "</" + typeName + ">");
		
		// Instantiate the element with the arguments.
		return t.instantiate(arguments);
		
	}

}