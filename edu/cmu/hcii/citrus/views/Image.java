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

public class Image extends BootElement<Image> {

	public final java.awt.Image value;
	
	public Image(java.awt.Image image) {
		
		value = image;
		
	}
	
	public Namespace<?> getType() { return Reflection.getJavaType(Image.class); }
	
	public Real width() { return new Real(value.getWidth(null)); }
	public Real height() { return new Real(value.getHeight(null)); }
	
	public Image evaluate(Element<?> env) { return this; }

	public Element duplicate() { return new Image(value); }
	public Text toCitrus() { return new Text(value.toString()); }
	public String toString() { return value.toString(); }
	
	public Bool isEquivalentTo(Element<?> o) { return new Bool(this.equals(o)); }

}