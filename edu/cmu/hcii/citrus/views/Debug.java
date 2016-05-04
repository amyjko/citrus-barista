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
package edu.cmu.hcii.citrus.views;

import edu.cmu.hcii.citrus.*;

public class Debug extends BaseElement<Debug> {
	
	public static final Dec<Bool> worker = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> memory = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> events = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> damage = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> postDamage = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> clip = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> children = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> layout = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> paintBoundaries = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> fps = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> windowState = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> navigation = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> translation = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> elementTranslation = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> propertyFinalization = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> elementFinalization = new Dec<Bool>(new Bool(false));
	
	public static final Debug debug = new Debug();
	
	public static final void print(String message) { System.err.println(message); }

	public static boolean threads() { return debug.peek(worker).value; }
	public static boolean memory() { return debug.peek(memory).value; }
	public static boolean input() { return debug.peek(events).value; }
	public static boolean damage() { return debug.peek(damage).value; }
	public static boolean postDamage() { return debug.peek(postDamage).value; }
	public static boolean clip() { return debug.peek(clip).value; }
	public static boolean children() { return debug.peek(children).value; }
	public static boolean layout() { return debug.peek(layout).value; }
	public static boolean boundaries() { return debug.peek(paintBoundaries).value; }
	public static boolean fps() { return debug.peek(fps).value; }
	public static boolean window() { return debug.peek(windowState).value; }
	public static boolean keyboard() { return debug.peek(navigation).value; }
	public static boolean view() { return debug.peek(translation).value; }
	public static boolean newView() { return debug.peek(elementTranslation).value; }
	public static boolean propertyFinalization() { return debug.peek(propertyFinalization).value; }
	public static boolean elementFinalization() { return debug.peek(elementFinalization).value; }

}