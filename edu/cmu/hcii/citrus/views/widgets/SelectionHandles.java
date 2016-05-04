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
import edu.cmu.hcii.citrus.views.paints.*;

public class SelectionHandles extends View {

	public static class Resized extends BaseElement {
		
		public static final Dec<Point> point = new Dec<Point>();
		public static final Dec<Handle> handle = new Dec<Handle>();
		
		public Resized(Namespace type, ArgumentList args) { super(type, args); }
		
	}
	
	public static final Dec<View> selection = new Dec<View>((Element)null, true);

	public Real selectionsTop() { return get(selection) == null ? new Real(0.0) : localTopOf(get(selection)); }
	public Real selectionsLeft() { return get(selection) == null ? new Real(0.0) : localLeftOf(get(selection)); }
	public Real selectionsWidth() { return get(selection) == null ? new Real(0.0) : localRightOf(get(selection)).minus(get(left)); }
	public Real selectionsHeight() { return get(selection) == null ? new Real(0.0) : localBottomOf(get(selection)).minus(get(top)); }
	
	public static final Dec<Real> left = new Dec<Real>(true, "(this selectionsLeft)");
	public static final Dec<Real> width = new Dec<Real>(true, "(this selectionsWidth)");
	public static final Dec<Real> top = new Dec<Real>(true, "(this selectionsTop)");
	public static final Dec<Real> height = new Dec<Real>(true, "(this selectionsHeight)");
	
	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
	public static final Dec<Bool> hidden = new Dec<Bool>(true, "(selection is nothing)");
	
	public static final Dec<List<View>> children = new Dec<List<View>>(
		"[" +
		"(a Handle xAction=-1 yAction=-1)" +
		"(a Handle xAction=-1 yAction=0)" +
		"(a Handle xAction=-1 yAction=1)" +
		"(a Handle xAction=0 yAction=-1)" +
		"(a Handle xAction=0 yAction=1)" +
		"(a Handle xAction=1 yAction=-1)" +
		"(a Handle xAction=1 yAction=0)" +
		"(a Handle xAction=1 yAction=1)" +
		"]"
	);
	
	public static final Dec<List<Paint>> foreground = new Dec<List<Paint>>(new List<Paint>(
			new RectanglePaint(Color.blue, 0.2, 3.0, -2, -2, 2, 2, 0, 0)));	
	
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(
			App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
				App.mouse.pointer.pick.evaluate(t);
				Element<?> tileSelected = t.get(selection);
				return true;
			}}),
		new Behavior(
			App.mouse.leftButton.released, new Action() { public boolean evaluate(View t) {
				App.mouse.pointer.release.evaluate(t);
				return true;
			}}),
		new Behavior(App.mouse.pointer.dragged, 
			new Action() { public boolean evaluate(View t) {
				Element<?> tileSelected = t.get(selection);
				if(tileSelected == null) return false;
				((View)tileSelected).set(View.left, new Real(App.mouse.pointer.positionRelativeToPointPicked().getX()));
				((View)tileSelected).set(View.top, new Real(App.mouse.pointer.positionRelativeToPointPicked().getY()));	
				return true;
			}})
		));
	
	public SelectionHandles(Namespace subType, ArgumentList args) { super(subType, args); }
	
	public static class Handle extends View {

		public static double handleSize = 8;
		
		public Real leftFunction() {
			if(getParent() == null) return new Real(0.0);
			if(integer(xAction) < 0) return new Real(-handleSize);
			else if(integer(xAction) > 0) return getParent().get(width);
			else return new Real((getParent().get(width).value - handleSize) / 2);
			
		}

		public Real topFunction() {
			if(getParent() == null) return new Real(0.0);
			if(integer(yAction) < 0) return new Real(-handleSize);
			else if(integer(yAction) > 0) return getParent().get(height);
			else return new Real((getParent().get(height).value - handleSize) / 2);
		}
		
		public static final Dec<Real> left = new Dec<Real>(true, "(this leftFunction)");
		public static final Dec<Real> top = new Dec<Real>(true, "(this topFunction)");
		public static final Dec<Real> width = new Dec<Real>(new Real(8));
		public static final Dec<Real> height = new Dec<Real>(new Real(8));

		public static final Dec<Int> xAction = new Dec<Int>();
		public static final Dec<Int> yAction = new Dec<Int>();
		
		public static final Dec<List<Paint>> foreground = new Dec<List<Paint>>(new List<Paint>(
			new FilledRectanglePaint(Color.blue, new Color(160, 160, 255, 255), 1.0, 0, 0, 0, 0, 0, 0),
			new RectanglePaint(new Color(0, 0, 128, 255), 1.0, 1.0, 0, 0, 0, 0, 0, 0)				
		));

		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
			new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
				App.mouse.pointer.pick.evaluate(t);
				return true;
			}}),
			new Behavior(App.mouse.leftButton.released, new Action() { public boolean evaluate(View t) {
				App.mouse.pointer.release.evaluate(t);
				return true;
			}}),
			new Behavior(App.mouse.pointer.dragged, new Action() { public boolean evaluate(View handle) {

				View t = handle.get(SelectionHandles.selection);
				java.awt.geom.Point2D frp = t.globalToLocal(App.mouse.pointer.getPosition());
				double x = t.get(View.left).value;
				double y = t.get(View.top).value;
				double w = t.get(View.width).value;
				double h = t.get(View.height).value;
				double hp = t.get(View.hPad).value;
				double vp = t.get(View.vPad).value;
	
				int xPos = ((Handle)handle).get(xAction).value;
				int yPos = ((Handle)handle).get(yAction).value;

				// Right
				if(xPos > 0)  {
					t.set(View.width, new Real(frp.getX() - x - hp));
				}
				// Left
				else if(xPos < 0) {
					t.set(View.width, new Real(w - (frp.getX() - x - hp))); 
					t.set(View.left, new Real(frp.getX() - hp));
				}

				// Bottom
				if(yPos > 0)
					t.set(View.height, new Real(frp.getY() - y - vp));
				// Top
				else if(yPos < 0) {
					t.set(View.height, new Real(h - (frp.getY() - y - vp))); 
					t.set(View.top, new Real(frp.getY() - vp));
				}

				handle.getParent().notifyListenersOf(Reflection.getJavaType(Resized.class), 
						new Point(frp.getX(), frp.getY()), handle);
				
				return true;

			}})
		));

		public Handle(Namespace type, ArgumentList args) { super(type, args); }

	}
	
	public Nothing showAllHandles() {
		
		for(View child : get(children))
			child.set(hidden, Bool.FALSE);
		return null;
		
	}
	
	public Nothing showLineHandles() {
		
		for(View child : get(children))
			child.set(hidden, Bool.TRUE);
		get(children).first().set(hidden, Bool.FALSE);
		get(children).last().set(hidden, Bool.FALSE);
		return null;
		
	}

}
