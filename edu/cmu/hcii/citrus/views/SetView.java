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

// A view is a tile that's a view of some property.
public class SetView extends GroupView {

	public static final Dec<Set<?>> model = new Dec<Set<?>>("(property get)", true);

	// An expression that takes two arguments and chooses one.
	public static final Dec<Element<Bool>> comparator = new Dec<Element<Bool>>();
	// The order in which to place the set elements.
	public static final Dec<Bool> direction = new Dec<Bool>(new Bool(true));

	public static final Dec<Bool> referenceViewsOnly = new Dec<Bool>(new Bool(false));

	public SetView() { this(null, null); }
	public SetView(ArgumentList arguments) { this(null, arguments); }
	public SetView(Namespace subtype, ArgumentList arguments) { super(subtype, arguments); updatedViewFor(null, get(model), null); }
	
}