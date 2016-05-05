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

import java.util.Iterator;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.layouts.VerticalLayout;

public class GroupView extends ElementView {

	public GroupView(ArgumentList arguments) { this(null, arguments); }
	public GroupView(Namespace subtype, ArgumentList arguments) { 
	
		super(subtype, arguments); 
		updateWith(get(model)); 
	
	}

	public static final Dec<Group<?,?>> model = new Dec<Group<?,?>>((Element)null, true);

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 2));

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

	public static final Dec<Real> width = new Dec<Real>(true, "(if (model isEmpty) 45.0 (this rightmostChildsRight))");
	public static final Dec<Real> height = new Dec<Real>(true, "(if (model isEmpty) 20.0 (this lastChildsBottom))"); 

	public static final Dec<Paint> focusPaint = new Dec<Paint>("(this getStyle).listFocusPaint");
	
	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(true));

	public Nothing updateWith(Group<?,?> newValue) { 
		
		set(model, newValue);
		updatedViewFor(null, get(model), null); 	
		return null;
	
	}
	public ElementView updatedViewFor(Property p, Element newValue, Transition t) {

		Text includeFuncName = new Text("include");
		Text viewForFuncName = new Text("viewFor");
		
		List<View> oldChildren = get(children);
		List<View> newChildren = new List<View>();
		Iterator<Property> propertyIterator = (Iterator<Property>)(Iterator)get(model).propertyIterator();
		while(propertyIterator.hasNext()) {
			
			Property property = propertyIterator.next();
			Element e = property.get();
		
			// Does the old view list contain a view of this model?
			Iterator<View> viewIterator = oldChildren.iterator();
			View existingView = null;
			while(viewIterator.hasNext()) {
				View next = viewIterator.next();
				if(next instanceof ElementView && ((ElementView)next).get(model) == e) {
					existingView = next;
					viewIterator.remove();
					break;
				}
			}
			
			// If there's an existing view, append it.
			ArgumentList includeArgs = new ArgumentList();
			includeArgs.enclosingEnvironment = this;
			includeArgs.add("item", e);
			Bool result = (Bool)getType().getFunctionNamed(includeFuncName, Bool.FALSE).instantiate(includeArgs);
			if(result.value) {
				if(existingView != null) {
					existingView.remove();
					newChildren.appendOverTime(existingView, t);
				}
				else {
					ArgumentList args = new ArgumentList();
					args.enclosingEnvironment = this;
					args.add("p", false, property);
					View v = (View)getType().getFunctionNamed(viewForFuncName, Bool.FALSE).instantiate(args);
					newChildren.appendOverTime(v, t);
				}
			}
			
		}
		
		setChildrenOverTime(newChildren, t);
		
		return this;
		
	}
	
	public View viewFor(Property p) { return Translator.toView(p); }

	public Bool include(Element<?> item) { return new Bool(true); }
	
}
