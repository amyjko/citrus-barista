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
import edu.cmu.hcii.citrus.views.layouts.HorizontalLayout;
import edu.cmu.hcii.citrus.views.paints.FilledRectanglePaint;
import edu.cmu.hcii.citrus.views.widgets.Label;

//
// @author Andrew J. Ko
//
public class NoTranslatorView extends ElementView {

	public static final Dec<Property> property = new Dec<Property>();

	public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, 3));
	
	public static final Dec<Real> width = new Dec<Real>("(this lastChildsRight)");
	public static final Dec<Real> height = new Dec<Real>("(this tallestChildsHeight)");	

	public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
	public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

	public static final Dec<List<FilledRectanglePaint>> background = 
		new Dec<List<FilledRectanglePaint>>(
			"[(a FilledRectanglePaint primaryColor=(a Color r=200.0 g=200.0 b=200.0) secondaryColor=(a Color r=220.0 g=220.0 b=220.0))]");
	
	public static final Dec<List<Label>> children = new Dec<List<Label>>(
		"[" +
		"(a Label text=\"View of \")" +
		"(a Label text=(model getType).name font=(this getStyle).boldFont)" +
		"]"
			
	);

	public NoTranslatorView(ArgumentList arguments) { super(arguments); }
	
}