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

public class PopupMenu extends ElementView {

	public static final Dec<Real> vPad = new Dec<Real>("3.0");
	public static final Dec<Real> hPad = new Dec<Real>("3.0");

	// Fit around the first child, the view of the property's value.
	public static final Dec<Real> width = new Dec<Real>(true, "(this firstChildsRight)");
	public static final Dec<Real> height = new Dec<Real>(true, "(this firstChildsBottom)");

	public static final Dec<Bool> focusable = new Dec<Bool>("true");

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>("[(this getStyle).popupMenuPaint]");

	public static final Dec<List<Behavior>> behavior = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.focusReceived, App.keyboard.addFocusPaint),
		new Behavior(App.focusLost, App.keyboard.removeFocusPaint),
		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
/*
			PropertyMenu menu = ((PropertyPopupMenu)t).menu;
			Property prop = (Property)t.get(property);
			
			if(App.mouse.pointer.isPicked(t)) return false;
			
			// Create a menu with the property's valid values, if it hasn't
			// been created yet.
			if(menu == null) {
				Hashtable<DecInterface,Object> args = new Hashtable<DecInterface,Object>(1);
				args.put(PropertyMenu.property, prop);				
				menu = new PropertyMenu(args);
				menu.when(App.mouse.leftButton.released, hideAction);
				t.addChild(menu);
			}
			else menu.updateMenuItems();

			// In the ideal case, we'd position the menu at the position of the menu item
			// for the property's current value...
			MenuItem m = menu.getMenuItemFor(prop.get());
			double x = -t.get(hPad);
			double y = 0;
			if(m != null) y = -(m.get(top) + m.get(bottom)) / 2;
			
			// Move the menu over the value, focus on it, hoist it, and show it.
			menu.moveTo(menu.adjustPointWithinWindow(new java.awt.geom.Point2D.Double(x, y)));
			App.mouse.pointer.pick(menu);
			menu.getProperty(View.hoisted).set(true);
			menu.getProperty(View.hidden).set(false);
	*/
			return true; 

		}})
	));

	// Translate the property into an appropriate view.
//	public static final Dec<List<Tile>> children = new Dec<List<Tile>>(Tile.children,
//		new TranslateElementProperty(property));

	private static Action hideAction = new Action() { 
		public boolean evaluate(View t) {
			App.mouse.pointer.release();
			t.set(hoisted, new Bool(false));
			t.set(hidden, new Bool(true));
			return true; 
		}
	};

	public PopupMenu(Namespace subType, ArgumentList arguments) { super(subType, arguments); }
		
}