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

import java.util.Stack;

import edu.cmu.hcii.citrus.*;

public class Behavior extends BaseElement<Behavior> {	
	
	// Global undo stack
	public static final Stack<ElementChangeAccumulator> undoStack = new Stack<ElementChangeAccumulator>();

	// Global behavior stack
	public static final Stack<ElementChangeAccumulator> behaviorStack = new Stack<ElementChangeAccumulator>();

	public static boolean executingUndo = false;
	public static void pushUndoable(ElementChangeAccumulator acc) {
		
		undoStack.push(acc);
		
	}
	
	public static boolean undo() {
		
		if(undoStack.empty()) {
			System.err.println("Nothing to undo.");
			return false;
		}
		ElementChangeAccumulator acc = undoStack.pop();
		executingUndo = true;
		acc.undo();
		executingUndo = false;
		return true;		
		
	}
	
	public static final Dec<Event> event = new Dec<Event>();
	public static final Dec<Element<Bool>> action = new Dec(new Bool(false));
	public static final Dec<Bool> undoable = new Dec(Bool.TRUE);
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>());
	public static final Dec<Text> description = new Dec(new Text(""));

	public Behavior() {}
	public Behavior(Namespace t, ArgumentList args) { super(t, args); }
	public Behavior(Event eventInstance, Element<Bool> newAction) {

		set(event, eventInstance);
		set(action, newAction);
		
	}
	public Behavior(Event eventInstance, boolean isUndoable, Element<Bool> newAction) {

		set(event, eventInstance);
		set(action, newAction);
		set(undoable, new Bool(isUndoable));
		
	}
	public Behavior(Event eventInstance, String newAction) {

		set(event, eventInstance);
		set(action, (Element<Bool>)CitrusParser.parse(newAction));
		
	}

	public Bool reactsTo(Event e) { 

		if(get(event) != null && get(event).isEquivalentTo(e).value) return new Bool(true); 
		for(Behavior next : get(behaviors))
			if(next.reactsTo(e).value) return new Bool(true);
		return new Bool(false);		
	
	}
	
	// If this behavior's condition is true, execute the action and return true.
	public boolean reactTo(Event newEvent, View view) {

		// The action determines if the event was consumed.
		try {
			
			Element<Event> e = get(event);
			
			if(e != null && newEvent.isEquivalentTo(e.evaluate(view)).value) {

				Element actionToDo = get(action);
				Element result = new Bool(false);
				
				// Push a property set event accumulator 
				boolean isUndoable = get(undoable).value;
				ElementChangeAccumulator acc = new ElementChangeAccumulator(this);
				if(isUndoable && !executingUndo) {
					behaviorStack.push(acc);
					Property.pushAccumulator(acc);
				}
				
				if(actionToDo instanceof Function) {

					result = Evaluate.evalFunction(null, (Function)actionToDo, view, newEvent);
					//result = Evaluate.eval(this, this, (Function)actionToDo, new List<Arg>(new Arg("", false, view)));
					
				}
				else result = actionToDo.evaluate(view);

				// Pop it!
				if(isUndoable && !executingUndo) {
					Property.popAccumulator();
					behaviorStack.pop();
					// Record the event if it has events and there's an event on the behavior stack.
					if(acc.hasEvents() && !behaviorStack.empty())
						behaviorStack.peek().recordEvent(acc);
				}
				
				if(result instanceof Bool) return ((Bool)result).value;
				else {
					
					System.err.println("" + this + " should be returning a boolean");
					return false;
					
				}				

			}
			
			boolean reacted = false;
			for(Behavior next : get(behaviors))
				if(next.reactTo(newEvent, view))
					reacted = true;
			return reacted;
			
		} catch(Exception e) { 
			
			debug("Error during reaction to " + this + " on " + view + ": " + e);
			e.printStackTrace();
			return false;
			
		}		
				
	}
      
	// The context for a behavior is its enclosing type
	public Context contextFor(Element e) { return (Namespace)ownerOfType(Boot.TYPE); }
	
	public String toString() { return "when " + get(event) + ": " + get(action); }
	
}