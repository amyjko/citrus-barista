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

public class TextLiteral extends Expression<Text> {

	private Text cache = null;
	public static final Dec<Text> token = new Dec<Text>(new Text(""));
	
	public static final Dec<Text> value = new Dec<Text>(true, new BaseElement<Text>() {
		public Text evaluate(Element<?> env) {

			if(((TextLiteral)env).cache == null) {

				String tok = env.get(token).value;

	//			if(tok.startsWith("\\u")) {
	//				
	//				
	//			}
	//			else {
				
				tok = tok.replace("\\\"", "\"");
				tok = tok.replace("\\\\", "\\");
				tok = tok.replace("\\n", "\n");
				tok = tok.replace("\\t", "\t");
				tok = tok.replace("\\b", "\b");
	
				// TODO: Still need to parse strings.
				
				((TextLiteral)env).cache = new Text(tok);
			}
			return ((TextLiteral)env).cache;
			
		}
	});

	public TextLiteral() {}
	public TextLiteral(ArgumentList args) { super(args); }
	public TextLiteral(String value) { set(token, new Text(value)); }

	public Text evaluate(Element<?> env) { return peek(value); }

	public Type resultingType() { return Boot.TEXT; }

	public Text toCitrus() { return cache; }//get(token).toCitrus(); }
	public String toString() { return "\"" + cache + "\""; }//get(token).toString() + "\""; }
	
}