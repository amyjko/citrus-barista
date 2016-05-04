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
package edu.cmu.hcii.citrus.views.behaviors;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.devices.Mouse;

public class Resizable extends Behavior {

	public static final Text DONT_KNOW = new Text("Don't Know");
	public static final Text MOVE = new Text("Move");
	public static final Text RESIZE = new Text("Resize");
	
	public static final Text RESIZE_LENGTH = new Text("Resize");
	public static final Text MOVE_OFFSET = new Text("Move");
	public static final Text NO_OP = new Text("NoOp");
	
	public static final Dec<Text> moveOrResize = new Dec<Text>(DONT_KNOW);
	public static final Dec<Text> xAction = new Dec<Text>(DONT_KNOW);
	public static final Dec<Text> yAction = new Dec<Text>(DONT_KNOW);
	public static final Dec<View> tilePicked = new Dec<View>((Element)null, true);
	
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(

		new Behavior(App.mouse.pointer.moved, new Action() {
			public boolean evaluate(View t) {

				int xAction, yAction;
				java.awt.geom.Point2D frp = t.globalToLocal(App.mouse.pointer.getPosition());

				if((frp.getX() - t.get(View.left).value) / t.get(View.width).value > .9) xAction = 1;
				else if((frp.getX() - t.get(View.left).value) / t.get(View.width).value < .1) xAction = 2;
				else xAction = 3;
				
				if((frp.getY() - t.get(View.top).value) / t.get(View.height).value > .9) yAction = 1;
				else if((frp.getY() - t.get(View.top).value) / t.get(View.height).value < .1) yAction = 2;
				else yAction = 3;

				if(xAction == 1 && yAction == 1) t.getWindow().setCursorTo(Mouse.RESIZE_SE_CURSOR);
				else if(xAction == 1 && yAction == 2) t.getWindow().setCursorTo(Mouse.RESIZE_NE_CURSOR);
				else if(xAction == 1 && yAction == 3) t.getWindow().setCursorTo(Mouse.RESIZE_W_CURSOR);
				else if(xAction == 2 && yAction == 1) t.getWindow().setCursorTo(Mouse.RESIZE_SW_CURSOR);
				else if(xAction == 2 && yAction == 2) t.getWindow().setCursorTo(Mouse.RESIZE_NW_CURSOR);
				else if(xAction == 2 && yAction == 3) t.getWindow().setCursorTo(Mouse.RESIZE_E_CURSOR);				
				else if(xAction == 3 && yAction == 1) t.getWindow().setCursorTo(Mouse.RESIZE_N_CURSOR);
				else if(xAction == 3 && yAction == 2) t.getWindow().setCursorTo(Mouse.RESIZE_S_CURSOR);					
				else if(xAction == 3 && yAction == 3) t.getWindow().setCursorTo(Mouse.MOVE_CURSOR);	

				return true;
				
			}
		}),

		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
	
			// Pick the tile clicked on
			App.mouse.pointer.pick.evaluate(t);				

			set(moveOrResize, DONT_KNOW);
			set(xAction, DONT_KNOW);
			set(yAction, DONT_KNOW);

			// If we haven't chosen actions yet, choose one of three actions for the x coordinate

			java.awt.geom.Point2D frp = App.mouse.pointer.positionRelativeToTilePicked();
			double x = t.get(View.left).value;
			double y = t.get(View.top).value;
			double w = t.get(View.width).value;
			double h = t.get(View.height).value;
			double hp = t.get(View.hPad).value;
			double vp = t.get(View.vPad).value;
	
			if(get(moveOrResize) == DONT_KNOW || get(tilePicked) != t) {

				set(tilePicked, t);
				if((frp.getX() - x) / w > .9) set(xAction, RESIZE_LENGTH);
				else if((frp.getX() - x) / w < .1) set(xAction, MOVE_OFFSET);
				else set(xAction, NO_OP);
				
				if((frp.getY() - y) / h > .9) set(yAction, RESIZE_LENGTH);
				else if((frp.getY() - y) / h < .1) set(yAction, MOVE_OFFSET);
				else set(yAction, NO_OP);

				if(get(xAction) == NO_OP && get(yAction) == NO_OP) set(moveOrResize, MOVE);
				else set(moveOrResize, RESIZE);
				
			}
			return true;
		}}),

		new Behavior(App.mouse.pointer.dragged, new Action() { public boolean evaluate(View t) {

			if(App.mouse.pointer.isPicked(t)) {

				java.awt.geom.Point2D frp = App.mouse.pointer.positionRelativeToTilePicked();
				double x = t.get(View.left).value;
				double y = t.get(View.top).value;
				double w = t.get(View.width).value;
				double h = t.get(View.height).value;
				double hp = t.get(View.hPad).value;
				double vp = t.get(View.vPad).value;
					
				// Right
				if(get(xAction) == RESIZE_LENGTH)  {
					t.set(View.width, new Real(frp.getX() - x - hp));
				}
				// Left
				else if(get(xAction) == MOVE_OFFSET) {
					t.set(View.width, new Real(w - (frp.getX() - x - hp))); 
					t.set(View.left, new Real(frp.getX() - hp));
				}
				// Middle
				else if(get(xAction) == NO_OP && get(moveOrResize) == MOVE)
					t.set(View.left, new Real(App.mouse.pointer.positionRelativeToPointPicked().getX()));
				
				// Bottom
				if(get(yAction) == RESIZE_LENGTH)
					t.set(View.height, new Real(frp.getY() - y - vp));
				// Top
				else if(get(yAction) == MOVE_OFFSET) {
					t.set(View.height, new Real(h - (frp.getY() - y - vp))); 
					t.set(View.top, new Real(frp.getY() - vp));
				}
				// Middle
				else if(get(yAction) == NO_OP && get(moveOrResize) == MOVE)
					t.set(View.top, new Real(App.mouse.pointer.positionRelativeToPointPicked().getY()));

				return true;
			}
			else return false;
			
		}}),

		new Behavior(App.mouse.leftButton.released, new Action() { 
			public boolean evaluate(View t) {
				if(App.mouse.pointer.getViewPicked() == t) {
					App.mouse.pointer.release.evaluate(t);
					return true;
				} else return false;
		}})

	));

}