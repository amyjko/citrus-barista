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

import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.util.Vector;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.devices.*;

// A main application window, with a thread for doing all of the work.
public class App extends Window {

    public static final Mouse mouse = new Mouse();
    public static final Keyboard keyboard = new Keyboard();

	public static final Device virtual = new Device("Virtual devices");

	public static final Event focusReceived = new FocusReceived(null, 0);
	public static final Event windowClosing = new Closing(null, 0);
	public static final Event focusLost = new FocusLost(null, 0);

	public static class Action extends Event {

		public static final Dec<Expression> action = new Dec<Expression>();
		
		public Action(Window window, long timeStamp, Expression newAction) { 
			
			super(window, timeStamp); 
			set(action, newAction); 
		
		}
		public Action(ArgumentList args) { super(args); }

		public void handle() { get(action).evaluate(window); }
		public boolean isNegligible() { return false; }
		public Bool isEquivalentTo() { return new Bool(false); }
		
	}
	
	// The window closing event.
	public static class Closing extends Event {
		
		public Closing(Window window, long timeStamp) { super(window, timeStamp); }
		public Closing(ArgumentList args) { super(args); }

		public void handle() { window.reactTo(this); }
		public boolean isNegligible() { return false; }
	
	}

	public static class FocusReceived extends Event {
		
		public FocusReceived() { super(null, 0); }
		public FocusReceived(Window window, long timeStamp) { super(window, timeStamp); }
		public FocusReceived(ArgumentList args) { super(args); }

		public void handle() {}
		public boolean isNegligible() { return false; }
	
	}

	public static class ChildReceivedFocus extends Event {
		
		public ChildReceivedFocus() { super(null, 0); }
		public ChildReceivedFocus(Window window, long timeStamp) { super(window, timeStamp); }
		public ChildReceivedFocus(ArgumentList args) { super(args); }

		public void handle() {}
		public boolean isNegligible() { return false; }
	
	}

	public static class FocusLost extends Event {

		public FocusLost() { super(null, 0); }
		public FocusLost(Window window, long timeStamp) { super(window, timeStamp); }
		public FocusLost(ArgumentList args) { super(args); }

		public void handle() {}
		public boolean isNegligible() { return false; }
	
	}
	
	public static class ChildLostFocus extends Event {
		
		public ChildLostFocus() { super(null, 0); }
		public ChildLostFocus(Window window, long timeStamp) { super(window, timeStamp); }
		public ChildLostFocus(ArgumentList args) { super(args); }

		public void handle() {}
		public boolean isNegligible() { return false; }
	
	}

	public static BlinkCaret blinkCaret = new BlinkCaret();
	public static class BlinkCaret extends Event {
		
		public BlinkCaret() { super(null); }
		public BlinkCaret(ArgumentList args) { super(args); }
		
		public void handle() {
			
			if(windowInFocus == null) return;
			windowInFocus.set(Window.paintCaret, windowInFocus.get(Window.paintCaret).not());
			View focus = windowInFocus.getFocus();
			if(focus != null) 
				focus.reactTo(this);
			
		}
		
	}

	
	public static final Dec<Style> style = new Dec<Style>();

	public static Window windowInFocus = null;
		
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.keyboard.F11.pressed, new BaseElement<Bool>() { public Bool evaluate(Element t) {
			Debug.debug.set(Debug.damage, Debug.debug.get(Debug.damage).not());
			return new Bool(true);
		}}),
		new Behavior(App.keyboard.F12.pressed, new BaseElement<Bool>() { public Bool evaluate(Element t) {
			if(App.keyboard.COMMAND.isDown())
				App.show(new DebugWindow());
			return new Bool(true);
		}}),
		new Behavior(App.windowClosing, new BaseElement<Bool>() { public Bool evaluate(Element<?> t) { 
			System.exit(0); return new Bool(true); }})
	));
	
	public App() { super(); initialize(null); }
	public App(Namespace type, ArgumentList args) { super(type, args); initialize(null); }
	public App(String styleName, String title, boolean resizable, int width, int height) {

		super(title, resizable, width, height);
		initialize(styleName);
		
	}
		
	private void initialize(String styleName) {
		
		windows.add(this);

        // Load images, if necessary
		Images.setComponent(frame.getContentPane());

		if(styleName != null) {
			
			java.io.File styleFile = new java.io.File(Universe.getStylesPathname() + styleName);
			if(styleFile.exists()) {
				Unit lang = CitrusParser.unit(new Text(Universe.getStylesPathname() + styleName));
				globalStyle = (Style)lang.getTypes().first().instantiate(null);
				System.err.println("Read " + lang);
			}
			System.err.println("Style file is " + styleFile + " resulting in " + globalStyle);
		}
		
	}

	public Nothing show() {

		if(!worker.isAlive()) worker.start();
		// Mark the width out of date so that everything is updated after being shown.
		getPropertyByDeclaration(width).markOutOfDate(null);
		super.show();
		// Rid of all of the startup stuff
		System.gc();
		return null;
		
	}
	
    public static Graphics2D getGraphics() { 
	    	if(windows.isEmpty()) return null;
	    	else return windows.firstElement().getGraphicsContext(); 
    	}
    
    public static App getApp() { 
    
    		if(windows.isEmpty()) return null;
    		else return (App)windows.firstElement(); 
    	
    }

    // Object to pause on
    private static Object objectToPauseOn = null;
    
   	// The list of windows that this Application owns.
	private static Vector<Window> windows = new Vector<Window>(5);
	
	// The queue of user events to process.
	private static Event[] eventQueue = new Event[1024];
	private static int nextEmptyEventQueueIndex = 0;
	private static int indexOfNextEventToProcess = 0;
	public static Event eventJustProcessed;
	
	// Whether or not the application is paused
	private static int paused = 0;
	
	// Frames per second data
	private static long longest = 0;
	private static long totalTime = 0;
	private static long numberOfTimes = 0;
	private static long beforePaintingWindow = 0;
	private static long afterRepaintingWindow = 0;
	private static long beforePaintingDamagedTiles = 0;
	private static long beforeUpdatingVisibleProperties = 0;

	// Data for handling out of date visible properties
	private static HashSet<Property> outOfDateProperties = new HashSet<Property>(100);
	private static Vector<Property> outOfDatePropertiesWhileUpdating = new Vector<Property>(100);
	private static HashSet<Property> propertiesWhoseTilesNeedPainting = new HashSet<Property>(100);
	private static boolean updatingOutOfDateVisibleProperties = false;
	private static long updateTime = System.currentTimeMillis();
	private static boolean postingDamage = false;

	// A table of AnimationStatements and the contexts in which they should be evaluated.
	private static Hashtable<AnimationStatement,Element> animations = new Hashtable<AnimationStatement,Element>();

	// Must be instantiated after out of date properties, since this creates paints.
	private static Style globalStyle = new Style();

	public static Runnable doWork = new Runnable() {
		public void run() {
			App.work();
		}
	};
	
	// The worker thread. While this application is running, do any work that
	// needs to be done. But don't do any while the out of date visible properties are
	// being updated. If there's no work, wait for something to notify us about any work.
	private static Thread worker = new Thread("Worker") {
		public void run() {
			while(true) {
				// If there's work to do, do it!
				if(haveWorkToDo()) {
					try {
						EventQueue.invokeAndWait(doWork);
					} 
					catch(InterruptedException e) {}
					catch(java.lang.reflect.InvocationTargetException e) {}
				}
				// Otherwise, wait until we get some work.
				else {
					synchronized(worker) {
						try { 	
							// If we made an object wait for a pause, we're now about to pause,
							// so wake it up. Then nullify the object to pause on so that we don't 
							// repeatedly wake it up.
							if(objectToPauseOn != null) synchronized(objectToPauseOn) { 
							
								objectToPauseOn.notify(); 
								objectToPauseOn = null;
								
							}
							worker.wait();
						} catch(InterruptedException e) {} 
					}	
				}
			}
		}
	};

	// Create a new window and return its ui.
	public static void show(Window w) {

		windows.addElement(w);
		w.show();

	}

	// Remove the window from the list of windows.
	public static void remove(Window w) {

		windows.removeElement(w);
		
	}
	
	////////////////////////////////////////////////////
	//
	// Look and Feel
	//
	////////////////////////////////////////////////////

	public static Style getGlobalStyle() { 
		return globalStyle;
	}
	public Style getStyle() {
		
		if(get(style) == null) return globalStyle;
		else return get(style);
		
	}
	
	//////////////////////////////////////////////////
	// 
	// Event Handling
	//
	//////////////////////////////////////////////////

	public static void enqueueAction(Window w, Expression a) {
		
		App.enqueue(new Action(w, System.currentTimeMillis(), a));

	}
	
	// Takes a UserEvent and enqueues it in the event queue.
	// If the most recent event has the same event type as the new
	// event and the new event is negligible, the new event
	// replaces the old event. This allows the system to ignore
	// events that come frequently, such as moves.
	public Nothing enqueueEvent(Event e) { App.enqueue(e); return null; }
	public static void enqueue(Event e) {

		synchronized(eventQueue) {

			// If the new event has the same type as the previous event, and
			// the event is negligible, replace the old with the new.
			if(e.isNegligible()) {
				Event mostRecentEvent = eventQueue[getPreviousEventQueueIndex(nextEmptyEventQueueIndex)];
				if(mostRecentEvent != null && mostRecentEvent.isEquivalentTo(e).value)
					nextEmptyEventQueueIndex = getPreviousEventQueueIndex(nextEmptyEventQueueIndex);
			}

			// Add the new event to the end of the queue.
			eventQueue[nextEmptyEventQueueIndex] = e;

			// Increment the index, wrapping if necessary.
			nextEmptyEventQueueIndex = getNextEventQueueIndex(nextEmptyEventQueueIndex);
			
			synchronized(worker) { if(haveWorkToDo()) worker.notify(); }

		}
		
	}
	
	private static int getNextEventQueueIndex(int index) {
		
		if(index + 1 > eventQueue.length - 1) return 0;
		else return index + 1;
		
	}
	
	private static int getPreviousEventQueueIndex(int index) {
		
		if(index - 1 < 0) return eventQueue.length - 1;
		else return index - 1;
		
	}

	
	// A list of the tiles that contain the mouse cursor
	private static Vector<View> viewsUnderViewPicked = new Vector<View>(20);
	private static Vector<View> priorViewsUnderViewPicked = new Vector<View>(20);
	private static Vector<View> viewsUnderCursor = new Vector<View>(20);	
	private static Vector<View> priorViewsUnderCursor = new Vector<View>(20);
	private static View viewDraggedInto = null;
	private static View viewThatHandledDirectEntry = null;
	
	public static Iterable<View> getViewsUnderViewPicked() { return viewsUnderViewPicked; }
	public static Iterable<View> getViewsUnderCursor() { return viewsUnderCursor; }

	// Process a single event.
	private static void processAnEvent() {

		synchronized(eventQueue) {
			
			if(indexOfNextEventToProcess != nextEmptyEventQueueIndex) {
			
//				if(Debug.debugWorker.b()) 
//					System.err.println("" + Thread.currentThread().getName() + ": " + 
//									  eventQueue.size() + " events remaining to process.");
				
				// Get the next event to process
				eventJustProcessed = eventQueue[indexOfNextEventToProcess];
				eventQueue[indexOfNextEventToProcess] = null;
				indexOfNextEventToProcess = getNextEventQueueIndex(indexOfNextEventToProcess);
	
				if(Debug.input()) Debug.print("" + eventJustProcessed);

				// Handle the event.
				ElementChangeAccumulator acc = new ElementChangeAccumulator(eventJustProcessed);
				
				Behavior.behaviorStack.push(acc);
				eventJustProcessed.handle();
				Behavior.behaviorStack.pop();
				
				// Push the events on the undo stack
				if(acc.hasEvents())
					Behavior.pushUndoable(acc);
				
				// Copy the tiles under the cursor.
				priorViewsUnderCursor.removeAllElements();
				priorViewsUnderCursor.addAll(viewsUnderCursor);

				// Copy the tiles under the tile picked
				priorViewsUnderViewPicked.removeAllElements();
				priorViewsUnderViewPicked.addAll(viewsUnderViewPicked);
				
				// Which tile contains the cursor now? If something's in focus, we check it.
				// Otherwise, we check the window.
				viewsUnderCursor.removeAllElements();
				viewsUnderViewPicked.removeAllElements();

				// Get the tile picked
				View tilePicked = mouse.pointer.getViewPicked();
				
				// If there's a tile picked, get all of the tiles the mouse is over in the tile picked,
				// and get all of the tiles that the mouse over under the tile picked.
				if(tilePicked != null) {
					tilePicked.whichViewsContain(mouse.pointer.getPosition(), viewsUnderCursor);
					mouse.pointer.getWindowPointerIsIn().whichViewsContain(mouse.pointer.getPosition(), viewsUnderViewPicked);
					viewsUnderViewPicked.removeAll(viewsUnderCursor);
				}
				// Otherwise, just get the tiles under the cursor inside the window.
				else if(mouse.pointer.getWindowPointerIsIn() != null) 
					mouse.pointer.getWindowPointerIsIn().whichViewsContain(mouse.pointer.getPosition(), viewsUnderCursor);
				
				// We begin by remembering the last tile dragged into and assuming that 
				// this time around there won't be a tile dragged into.

				// The first tile that isn't in the old set but in the new set
				// (and thus was just dragged in) gets the "draggedIn" event.
				// We continue until we find one that reacts to the dragged in event.
				View newTileDraggedInto = null;
				Iterator<View> newTilesUnderTilePicked = viewsUnderViewPicked.iterator();
				while(newTilesUnderTilePicked.hasNext()) {
					View t = newTilesUnderTilePicked.next();
					if(t != tilePicked)
						if(t == viewDraggedInto && t.reactTo(mouse.pointer.draggedOver).value ||
						   t != viewDraggedInto && t.reactTo(mouse.pointer.draggedIn).value) {
							newTileDraggedInto = t;
							break;
						}
				}

				// If the tile that was last dragged into has either changed or is no longer
				// under the tile picked, we send it the dragged out event.
				if(viewDraggedInto != null && viewDraggedInto != newTileDraggedInto) {
					viewDraggedInto.reactTo(mouse.pointer.draggedOut);
					viewDraggedInto = null;
				}

				viewDraggedInto = newTileDraggedInto;
								
				// The tiles that are not in the old set but in the new set get the "entered" event
				Iterator<View> newTiles = viewsUnderCursor.iterator();
				while(newTiles.hasNext()) {
					View t = newTiles.next();
					if(!priorViewsUnderCursor.contains(t)) {
						t.reactTo(mouse.pointer.entered);
					}
				}

				// The tiles that are not in the new set but in the old set get the "exited" event
				Iterator<View> oldTiles = priorViewsUnderCursor.iterator();
				while(oldTiles.hasNext()) {
					View t = oldTiles.next();
					if(!viewsUnderCursor.contains(t)) {
						t.reactTo(mouse.pointer.exited);
					}
				}
								
			}

		}
		
	}	

	// Causes "haveWorkToDo" to return false, effectively halting the worker until unpause
	// is called. This does not happen immediately. It causes the object to pause on 
	// to wait until the worker is ready to pause.
	public static void pause(Object newObjectToPauseOn) { 

		synchronized(worker) {

			paused++; 

			// Notify the worker, in case it's asleep, so that it can wake up the thread we're about to pause.
			objectToPauseOn = newObjectToPauseOn;
			if(Debug.threads()) 
				System.err.println("" + Thread.currentThread().getName() + ": PAUSING WORKER for " + objectToPauseOn + "...");
			worker.notify();

		}

		// We don't want to wait if the worker itself is pausing; it would never wake up.
		if(Thread.currentThread() == worker) return;
		
		// Cause the object to pause on to wait until the worker thread is done.
		if(objectToPauseOn != null) {
			synchronized(objectToPauseOn) {
	
				try {
					if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": ASKING " + objectToPauseOn + " to wait ");
					objectToPauseOn.wait();
				} catch(InterruptedException e) {}
				
			}
		}

	}
	
	// Causes"haveWorkToDo" to return true if there's actually work to do, "waking" the worker thread.
	public static void unpause() {
		
		synchronized(worker) {

			objectToPauseOn = null;
			paused--;
			if(paused == 0) worker.notify();
			
			if(Debug.threads() && paused == 0) 
				System.err.println("" + Thread.currentThread().getName() + ": UNPAUSING WORKER...");		
			
		}
		
	}
	
	public static boolean isPaused() { return paused > 0; }

	public static Thread getWorker() { return worker; }
	
	// The basic UI cycle. This is synchronized since the tile window (the AWT thread)
	// often calls quicklyResize(), while this is working.
	public static void work() {
		
		threadDebug("WORKING.");
		
		// Update the update time.
		updateTime = System.currentTimeMillis();

		// Process the next event. This will involve putting children
		// in new parents, moving tiles, and doing other various
		// operations which change the properties  of tiles.
		processAnEvent();
		
		// Update the update time.
		updateTime = System.currentTimeMillis();

		beforeUpdatingVisibleProperties = System.currentTimeMillis();
		
		// Now that we've gathered all of the property changes, 
		// update any out of date visible properties
		updateOutOfDateVisibleProperties();

		// Now that we've updated animating properties, update the animations.
		Iterator<AnimationStatement> animationIterator = animations.keySet().iterator();
		while(animationIterator.hasNext()) {
			AnimationStatement as = animationIterator.next();
			if(as.doneAnimating(animations.get(as))) 
				animationIterator.remove();
		}
		
		beforePaintingDamagedTiles = System.currentTimeMillis();
		
		View.numCalls = 0;
		
		// Now that we've gathered all of the properties that have actually
		// changed, post damage on all of their tiles.
		postDamageOnViewsOfPropertiesChanged();
		
		// Remember when we started painting.
		beforePaintingWindow = System.currentTimeMillis();
		
		// Now that we've gathered up all of the damage resulting from
		// the property changes, repair the damage in each window.
		for(Window w : windows) w.repairDamagedArea();

		// Calculate how long it took to paint.
		afterRepaintingWindow = System.currentTimeMillis();
		if(afterRepaintingWindow - beforePaintingWindow > 0) {
			numberOfTimes++;
			totalTime += afterRepaintingWindow - beforePaintingWindow;
			if(afterRepaintingWindow - beforePaintingWindow > longest) {
				longest = afterRepaintingWindow - beforePaintingWindow;
			}
		}
		
		if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": DONE WORKING.\n");
		
		if(Debug.fps()) {
			System.err.println(getAverageFPS() + "fps / " + View.numCalls + " calls");
		}
		
	}
	
	public static double getAverageFPS() { return (numberOfTimes / (totalTime / 1000.0)); }
	
	// If the event queue isn't empty, there's damage to paint, or there are properties to animate,
	// then there's work to do.
	private static boolean haveWorkToDo() {
    	
		// This should wait if its paused or it has no work to do.
		return (paused == 0 &&
				(nextEmptyEventQueueIndex != indexOfNextEventToProcess || 
				 !outOfDateProperties.isEmpty() || 
				 !propertiesWhoseTilesNeedPainting.isEmpty() ||
				 !animations.isEmpty()));
    	
	}
	
	// Notifies the application that the given property has changed, and impacts
	// the screen. The property is saved, so that its tile can be repainted. 
	public static void propertyIsOutOfDate(Property property) {

		if(worker == null) return;
		// TODO: Can we really ignore these?
		if(updatingOutOfDateVisibleProperties) {
//			outOfDatePropertiesWhileUpdating.add(property);
			return;
		}
		else {

			synchronized(outOfDateProperties) {
				
				// Its a set, so we won't make a duplicate.
				outOfDateProperties.add(property);
				// If there's any work to do, wake up the worker.
				synchronized(worker) { if(haveWorkToDo()) worker.notify(); }
				
			}

		}
		
	}

	// Update the current list of out of date visible properties,
	// posting damage before and after the update.
	private static void updateOutOfDateVisibleProperties() {

		synchronized(outOfDateProperties) {

			// Only do work if there's work to do.	
			if(outOfDateProperties.isEmpty()) return;
			
			if(Debug.threads() && outOfDateProperties.size() > 0) 
				System.err.println("" + Thread.currentThread().getName() + ": UPDATING " + 
								  outOfDateProperties.size() + " properties...");
	
			// Set this flag so that we can catch any out of date properties that
			// are made out of date due to updating an out of date visible property.
			updatingOutOfDateVisibleProperties = true;
			
			// We start with a list of properties that claim to be out of date and request their values,
			// implicitly causing "changed" events to be called, adding to the propertiesWhoseTilesNeedPainting set.
			Iterator<Property> outOfDateIterator = outOfDateProperties.iterator();
			while(outOfDateIterator.hasNext()) { 
				Property<?> p = outOfDateIterator.next();
				p.get();
			}
			outOfDateProperties.clear();
		
			// We're done updating.
			updatingOutOfDateVisibleProperties = false;
	
			if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": DONE UPDATING properties.");

		}
		
	}
	
	// Record the tiles of the property changed.
	public static void propertyChanged(Property property) {

		synchronized(propertiesWhoseTilesNeedPainting) {

			// TODO: Can we really ignore properties that change while posting damage?
			// Where do they come from? They come from properties being marked out of date
			// while updating out of date properties (change handlers changing stuff).
			if(!postingDamage) {

				propertiesWhoseTilesNeedPainting.add(property);
			
				// If there's any work to do, wake up the worker.
				if(worker ==  null) return;
				synchronized(worker) { if(haveWorkToDo()) worker.notify(); }
				
			}
		}
		
	}

	private static void postDamageOnViewsOfPropertiesChanged() {

		synchronized(propertiesWhoseTilesNeedPainting) {
		
			postingDamage = true;
			
			if(propertiesWhoseTilesNeedPainting.size() > 0)
				threadDebug("POSTING DAMAGE ON " + propertiesWhoseTilesNeedPainting.size() + " property's views");

			Type viewType = Reflection.getJavaType(View.class);
			
			int tilesPainted = 0;
			Iterator<Property> tileIterator = propertiesWhoseTilesNeedPainting.iterator();
			Property<?> propertyToUpdate = null;
			while(tileIterator.hasNext()) {
	
				propertyToUpdate = tileIterator.next();
				// Add every view that is an owner of an out of date property
				Element owner = propertyToUpdate.getElementOwner();
				if(owner instanceof View) {
					((View)owner).postDamage();
				} else if(owner instanceof Paint || owner instanceof Color) {

					View viewOwner = (View)owner.ownerOfType(viewType);
					if(viewOwner != null) {
						viewOwner.postDamage();
					}
					
					// There seem to be some concurrent modifications to this list that
					// cause infinite looping.
					Vector<Element> users = new Vector<Element>(10);
					Iterator<Element> userIterator = owner.getUsers().iterator();
					while(userIterator.hasNext()) { users.add(userIterator.next()); }
					for(Element user : users) {
						View viewUser = (View)user.ownerOfType(viewType);
						if(viewUser != null)
							viewUser.postDamage();
					}
				}
				
				// If this property is done, remove it from the list.
				if(propertyToUpdate.isDoneTransitioning()) tileIterator.remove();
	
			}

			if(Debug.threads() && (tilesPainted > 0 || propertiesWhoseTilesNeedPainting.size() > 0)) 
				System.err.println("" + Thread.currentThread().getName() + ": " + tilesPainted + " tiles painted, " +
								  "and " + propertiesWhoseTilesNeedPainting.size() + " still animating.");

			postingDamage = false;

		}
		
	}
	
	public static long getUpdateTime() { return updateTime; }

	public static void enqueueAnimationStatement(AnimationStatement statement, Element evaluationContext) {

		if(animations.get(statement) != null) throw new ViewError("Warning: already animating " + statement + "!");
		if(!statement.doneAnimating(evaluationContext)) animations.put(statement, evaluationContext);
		
	}
	
	public static void threadDebug(String message) {
		
		if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": " + message);
		
	}

}