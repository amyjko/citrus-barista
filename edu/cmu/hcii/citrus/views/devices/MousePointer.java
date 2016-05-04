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
package edu.cmu.hcii.citrus.views.devices;

import java.awt.geom.Point2D;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

public class MousePointer extends Device {

	private Mouse owner;

	public static final Dec<Real> left = new Dec<Real>(new Real(0.0));
	public static final Dec<Real> top = new Dec<Real>(new Real(0.0));
	
	// The previous and current pointer positions
	private Point2D position, previousPosition;

	// The point on the picked tile that was picked.
	private Point2D pointPicked;
	
	// The offset from the point picked to the mouse position.
	private Point2D offsetFromPointPicked;
	
	// The offset at which the mouseFocus was obtained
	private double xOffset, yOffset;
	private Window windowPointerIsIn = null;
	
	// The tile picked is the tile that is currently receiving mouse events. This is
	// typically captured after a click, but need not be. This causes all pointer
	// and button events to go to the mouse focus.
	public static final Dec<View> viewPicked = new Dec<View>((Element)null, true);

	private Listener pickListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Object oldValue, Object newValue) {
			if(newValue instanceof View) pick((View)newValue);
		}
	};
	
	public final Event moved = new Moved(null, 0, this, null, null);
	public final Event dragged = new Dragged(null, 0, this, null, null);

	public final Event picked = new Picked(null, 0);
	public final Event released = new Released(null, 0);
	
	public final Event entered = new Entered(null, 0);
	public final Event exited = new Exited(null, 0);

	public final Event draggedOver = new DraggedOver(null, 0);
	public final Event draggedIn = new DraggedIn(null, 0);
	public final Event draggedOut = new DraggedOut(null, 0);
	public final Event droppedOver = new DroppedOver(null, 0);
	public final Event dropFailed = new DropFailed(null, 0);
	public final Event dropSucceeded = new DropSucceeded(null, 0);

	public abstract static class PointerMoveEvent extends Event {

		public static final Dec<Real> x = new Dec<Real>();
		public static final Dec<Real> y = new Dec<Real>();
		public static final Dec<MousePointer> pointer = new Dec<MousePointer>((Element)null, true);

		public PointerMoveEvent(ArgumentList args) { super(args); }
		public PointerMoveEvent(Window newWindow, long newTimeStamp, MousePointer newPointer, Real newX, Real newY) {

			super(newWindow, newTimeStamp);
			set(pointer, newPointer);
			set(x, newX);
			set(y, newY);
			
		}

		public boolean isNegligible() { return true; }


	}
	
	public static class Moved extends PointerMoveEvent {

		public Moved() { super(null, 0, null, new Real(0), new Real(0)); }
		public Moved(ArgumentList args) { super(args); }
		public Moved(Window newWindow, long newTimeStamp, MousePointer newPointer, Real newX, Real newY) {
			super(newWindow, newTimeStamp, newPointer, newX, newY);
		}

		public void handle() {

			MousePointer mousePointer = get(pointer);
			
			// Update the window that the cursor is in.
			mousePointer.windowPointerIsIn = window;
			
			// Save the previous mouse position and update the new one.
			mousePointer.previousPosition.setLocation(mousePointer.position);
			mousePointer.position.setLocation(real(x), real(y));
			mousePointer.set(left, get(x));
			mousePointer.set(top, get(y));
			
			// If there's a tile in focus, figure out which focused tiles are under the cursor.
			if(mousePointer.picked()) {

				// Send the dragged event to the focus.
				mousePointer.getViewPicked().reactTo(new Dragged(window, timeStamp, mousePointer, get(x), get(y)));

			}
			else {

				// React to the moved event until one of the tiles under the handles it.
				for(View view : App.getViewsUnderCursor())
					if(view.reactTo(this).value) break;
				
			}
						
		}

	}

	// This event is sent to a tile when it it has focus and the mouse is moving.
	public static class Dragged extends PointerMoveEvent {
		public Dragged() { super(null); }
		public Dragged(ArgumentList args) { super(args); }
		public Dragged(Window newWindow, long newTimeStamp, MousePointer newPointer, Real newX, Real newY) {
			super(newWindow, newTimeStamp, newPointer, newX, newY);
		}
		public void handle() {}
	}

	public abstract static class PointerEvent extends Event {

		public PointerEvent(ArgumentList args) { super(args); }
		public PointerEvent(Window newWindow, long newTimeStamp) {
			super(newWindow, newTimeStamp);			
		}

		public boolean isNegligible() { return false; }

	}
	
	// This event is sent to a tile when it receives focus.
	public static class Picked extends PointerEvent {
		public Picked(ArgumentList args) { super(args); }
		public Picked(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}

	// This event is sent to a tile when it receives focus.
	public static class Released extends PointerEvent {
		public Released(ArgumentList args) { super(args); }
		public Released(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}

	// This event is sent to a tile when the pointer has moved inside the tile's boundaries.
	public static class Entered extends PointerEvent {
		public Entered() { super(null, 0); }
		public Entered(ArgumentList args) { super(args); }
		public Entered(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}

	// This event is sent to a tile when the pointer has moved outside a tile's boundaries.
	public static class Exited extends PointerEvent {
		public Exited() { super(null, 0); }
		public Exited(ArgumentList args) { super(args); }
		public Exited(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}

	// This event is sent to the tile that the mouse moves when another tile is in focus.
	public static class DraggedOver extends PointerEvent {
		public DraggedOver(ArgumentList args) { super(args); }
		public DraggedOver(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}

	// This event is sent to the tile that the mouse moves when another tile is in focus.
	public static class DraggedIn extends PointerEvent {
		public DraggedIn(ArgumentList args) { super(args); }
		public DraggedIn(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}

	// This event is sent to the tile that the mouse moves when another tile is in focus.
	public static class DraggedOut extends PointerEvent {
		public DraggedOut(ArgumentList args) { super(args); }
		public DraggedOut(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}
	
	public static class DroppedOver extends PointerEvent {
		public DroppedOver(ArgumentList args) { super(args); }
		public DroppedOver(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}
	public static class DropFailed extends PointerEvent {
		public DropFailed(ArgumentList args) { super(args); }
		public DropFailed(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}
	public static class DropSucceeded extends PointerEvent {
		public DropSucceeded(ArgumentList args) { super(args); }
		public DropSucceeded(Window newWindow, long newTimeStamp) { super(newWindow, newTimeStamp); }
		public void handle() {}
	}

	public Action pick = new Action() { public boolean evaluate(View t) { 
		pick(t); return true; }};

	public Action pickAndHoist = new Action() { public boolean evaluate(View t) { 
		pick(t); t.set(View.hoisted, new Bool(true)); return true; }};

	public Action release = new Action() { public boolean evaluate(View t) { 
		if(isPicked(t)) { release(); return true; } else return false; }};

	public Action releaseAndUnhoist = new Action() { public boolean evaluate(View t) { 
		t.set(View.hoisted, new Bool(false)); release(); return true; }};

	public MousePointer(ArgumentList args) { super(args); init(); }
	public MousePointer(Mouse owner, String name) {
		
		super(name);
		init();
		
	}
	
	public void init() {
		
		this.owner = owner;

		position = new Point2D.Double(0.0, 0.0);
		previousPosition = new Point2D.Double(0.0, 0.0);
		pointPicked = new Point2D.Double(0.0, 0.0);
		offsetFromPointPicked = new Point2D.Double(0.0, 0.0);
		xOffset = 0;
		yOffset = 0;
				
		getPropertyByDeclaration(viewPicked).addListener(pickListener);
		
	}
	
	// Returns the current and previous positions
	public Point2D getPosition() { return position; }
	public Point2D getPreviousPosition() { return previousPosition; }

	// Gives focus to the given tile and records the position at which the focus was picked at.
	public Nothing pick(View newTilePicked) { 
		
		if(newTilePicked == null)
			throw new ViewError("Can't focus on nothing");
		else if(newTilePicked.getWindow() == null) {
			System.err.println("Can't focus on a tile that's not in a window");
			return null;
		}
		else if(picked() && newTilePicked != getViewPicked()) return null;
		
		set(viewPicked, newTilePicked);
		
		// What is the offset of the selection in the selected tile's coordinate system?
		pointPicked.setLocation(newTilePicked.globalToLocal(position));
		xOffset = pointPicked.getX() - newTilePicked.real(View.left);
		yOffset = pointPicked.getY() - newTilePicked.real(View.top);

		// Enqueue a picked even on the focused tile.
		newTilePicked.reactTo(picked);
		
		return null;
		
	}

	// Releases focus from the current focus. Enqueues a dropped over event to the tile that the mouse is
	// over (not including the tile focused on).
	public Nothing release() { 

		View tp = getViewPicked();
		if(tp == null) return null;
		
		// We remember the tile picked, in case is its unpicked when reacting to dropped over, or released.
		View tempPicked = tp;
		
		// Send the released event on the focused tile.
		tempPicked.reactTo(released);
		
		// Find a tile under the cursor to react to the dropped over event.
		boolean handled = false;
		for(View view : App.getViewsUnderViewPicked())
			if(view.reactTo(droppedOver).value) break;

		// If the dropped over was not handled (the drop failed), send the drop failed event.
		// This goes up the hierarchy.
		View reactor = tempPicked;
		Event eventToSend = handled ? new DropSucceeded(null, 0) : new DropFailed(null, 0);
		while(reactor != null && !reactor.reactTo(eventToSend).value) reactor = reactor.getParent();
		
		// Nullify the tile picked
		set(viewPicked, null);
	
		return null;
		
	}
	
	public Window getWindowPointerIsIn() { return windowPointerIsIn; }
	
	public boolean picked() { return getViewPicked() != null; }
	
	public View getViewPicked() { return get(viewPicked); }
	
	public boolean isPicked(View queryFocus) { return getViewPicked() == queryFocus; }
	
	public Point2D positionRelativeToTilePicked() {
		
		if(picked()) return getViewPicked().globalToLocal(position);
		else throw new ViewError("There's no focus; can't return a focus relative position");
		
	}

	public Point2D positionRelativeToContentOfTilePicked() {
		
		if(picked()) return getViewPicked().globalToContent(position);
		else throw new ViewError("There's no focus; can't return a focus relative content position");
		
	}	

	// Returns the offset from the mouse position to the focus point.
	public Point2D positionRelativeToPointPicked() {
		
		if(picked()) {

			// Put the pointer position in the focus' local coordinates.
			Point2D localMouse = getViewPicked().globalToLocal(position);
			offsetFromPointPicked.setLocation(localMouse.getX() - xOffset, localMouse.getY() - yOffset);
			return offsetFromPointPicked;
			
		} else throw new ViewError("There is no focus!");

	}
	
	public Point getPointRelativeToPointPicked() {
		
		Point2D point = positionRelativeToPointPicked();
		return new Point(point.getX(), point.getY());
		
	}

}