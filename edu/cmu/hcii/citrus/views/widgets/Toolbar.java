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
package edu.cmu.hcii.citrus.views.widgets;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.HorizontalLayout;

// A widget that shows all of the valid values for a property with a finite value set.
public class Toolbar extends View {

	public static final Dec<Real> width = new Dec<Real>(true, "(if vertical (this widestChildsWidth) (this parentsWidth))");
	public static final Dec<Real> height = new Dec<Real>(true, "(if vertical (this parentsHeight) (this tallestChildsHeight))");
	
	public static final Dec<List<View>> tools = new Dec<List<View>>(new Parameter(), true);
	
	public static final Dec<Bool> clipsChildren = new Dec<Bool>("false");

	public static final Dec<HorizontalLayout> layout = new Dec<HorizontalLayout>(true, "(if vertical (a VerticalLayout) (a HorizontalLayout spacing=5.0 alignment=\"vertically-centered\"))");

	public static final Dec<Bool> vertical = new Dec<Bool>("false");
	
	public static final Dec<Real> vPad = new Dec<Real>("1.0");
	public static final Dec<Real> hPad = new Dec<Real>("1.0");

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>("[(this getStyle).lighterBackgroundPaint]");

	public static final Dec<List<View>> children = new Dec<List<View>>("[(a Container) (a Picture image=(this getImage \"ToolbarRightArrow.png\"))]");

	public Toolbar(ArgumentList arguments) { super(arguments); }
	
	// A widget that shows all of the valid values for a property with a finite value set.
	public static class Container extends View {

		public static final Dec<Real> width = new Dec<Real>(true, "(if vertical (this widestChildsWidth) ((this parentsRemainingWidth) minus (2.0 * (children length))))");
		public static final Dec<Real> height = new Dec<Real>(true, "(if vertical (this parentsRemainingHeight) (this tallestChildsHeight))");
		
		public static final Dec<Real> vPad = new Dec<Real>("3.0");
		public static final Dec<Real> hPad = new Dec<Real>("3.0");

		public static final Dec<Bool> clipsChildren = new Dec<Bool>("true");

		public static final Dec<Layout> layout = new Dec<Layout>(true, "(if vertical (a VerticalLayout) (a HorizontalLayout spacing=5.0 alignment=\"vertically-centered\"))");

		public static final Dec<List<View>> children = new Dec<List<View>>("tools");
		
		public Container(Namespace type, ArgumentList arguments) { super(arguments); }

	}
	
}