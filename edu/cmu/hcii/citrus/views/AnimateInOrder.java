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

public class AnimateInOrder extends AnimationStatement {

	public static final Dec<List<AnimationStatement>> statements = new Dec<List<AnimationStatement>>(new NewList<AnimationStatement>());

	// Runtime state
	private AnimationStatement statementAnimating = null;
	private int index = 1;

	public AnimateInOrder() {}
	public AnimateInOrder(Namespace subType, ArgumentList args) { super(subType, args); }
	
	public boolean startAnimating(Element context) {

		index = 1;
		return false;
		
	}
	
	public boolean doneAnimating(Element context) {

		// If we have no more statements, we're done.
		if(index > get(statements).length().value) return true;
		
		// If there's no statement animating, get one from the current index
		// and start it.
		if(statementAnimating == null) {
			statementAnimating = get(statements).nth(new Int(index));
			statementAnimating.startAnimating(context);
		}
		// Otherwise, if the statement we're waiting on is done, proceed to the next.
		else if(statementAnimating.doneAnimating(context)) {

//			debug("Done animating " + statementAnimating);
			
			index++;
			
			// If that was the last statement, nullify the statement animating.
			if(index > get(statements).length().value) statementAnimating = null;			
			// Otherwise, start the next one.
			else {
				
				statementAnimating = get(statements).nth(new Int(index));
				statementAnimating.startAnimating(context);
				
			}
			
		}
		
		// Done animating if we're at the end of the list.
		return index > get(statements).length().value;
		
	}
	
}