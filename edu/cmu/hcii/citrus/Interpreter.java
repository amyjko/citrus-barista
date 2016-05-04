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

public class Interpreter extends Universe {
	
	public static final Dec<Bool> showLexing = new Dec<Bool>(new Bool(true));
	public static final Dec<Element> env = new Dec<Element>((Element)null, true);
	public static final Dec<Element> temp = new Dec<Element>((Element)null, true);
	public static final Dec<Element> app = new Dec<Element>((Element)null, true);
	
	public Interpreter(Element newEnvironment) {
		
		set(env, this);
		set(app, newEnvironment);
		
	}
	
	public void readEvalPrint() {
		
		String input = "";
		try {

			System.err.print("\n\n\n> ");

			char c = ' ';
			while(true) {
				c = (char)System.in.read();
				if(c == '\n') {
					if(input.length() > 0) {
						try {
							if(bool(showLexing)) System.err.println(CitrusParser.tokenize(input));
							Element code = CitrusParser.code(input);
							//System.err.println("Evaluating " + code);
							System.err.println("\n" + code.evaluate(get(env)));
						} catch(Exception e) { e.printStackTrace(); }
						System.err.println("\n");
					}
					input = "";
					System.err.print("" + get(env) + " > ");
				}
				else input = input + c;
			}
			
		} catch(java.io.IOException e) { System.err.println("Error reading input"); }
		
	}
	
}
