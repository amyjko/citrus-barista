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

// A view is a tile that's a view of some property.
public class ElementView extends View {

	public static final Dec<Property> property = new Dec<Property>((Element)null, true);
	
	// The model of which this is a view. Subclasses of element view should override element's expected type
	// to match the type they are a view of.
	public static final Dec<Element> model = new Dec<Element>((Element)null, true);

	public ElementView(ArgumentList arguments) { this(null, arguments); }
	public ElementView(Namespace subtype, ArgumentList arguments) { super(subtype, arguments); }

	public Element<?> getModel() { return get(model); }
	
	public ElementView updatedViewFor(Property p, Element newValue, Transition t) { return Translator.toView(p); }	

	public String toString() { return super.toString() + " of " + getModel(); }

	public Nothing replaceAndFocusOnValue(Element newValue, Element valueToFocusOn) {

		// We don't want to set here; we want a general way to tell the property to update its value.
		// For example, if this property is pointing to a list, we'd like to replace the model
		// that this element view represents with this new model. This may involve explicitly setting
		// the property, or it may involve the element replacing something.
		Element oldValue = get(model);
		oldValue.getPropertyOwner().replaceWith(oldValue, newValue, App.getGlobalStyle().getQuickTransition());
		if(valueToFocusOn != null)
			valueToFocusOn.getFirstView().getDeepestFocusableAncestor(true).requestKeyboardFocus("previous");
		else
			newValue.getFirstView().getDeepestFocusableAncestor(true).requestKeyboardFocus("previous");
		
		return null;
			
	}

	public Bool replaceAndFocusOn(Element newValue, Text propertyName) {
		return replaceAndFocusOnOverTime(newValue, propertyName, null);
	}
	
	public Bool replaceAndFocusOnOverTime(Element newValue, Text propertyName, Transition t) {

		Element oldValue = get(model);
		if(oldValue.getElementOwner() instanceof Group) 
			oldValue.getElementOwner().replaceWith(oldValue, newValue, t);
		else oldValue.getPropertyOwner().replaceWith(oldValue, newValue, t);
		if(!propertyName.value.equals(""))
			focusOnProperty(newValue, propertyName);
//		else {
//			View view = newValue.getFirstView();
//			if(view == null) System.err.println("" + newValue + " has no view");
//			else view.requestKeyboardFocus("next");
//		}
		return new Bool(true);
		
	}
	
	public Bool parentWithAndFocusOn(Element newValue, Text propertyName, Text propertyToFocusOn) {

		// Disown this view's model.
		Element oldValue = get(model);
		oldValue.getPropertyOwner().replaceWith(oldValue, newValue, App.getGlobalStyle().getQuickTransition());
		newValue.set(propertyName, oldValue);
		focusOnProperty(newValue, propertyToFocusOn);
		return new Bool(true);
		
	}
	
	public Bool replaceWithChildAndFocusOn(Text propertyName, Text propertyToFocusOn) {
		
		Element oldValue = get(model);
		Element newValue = oldValue.getProperty(propertyName).get();
		oldValue.getProperty(propertyName).set(null);
		return replaceAndFocusOn(newValue, propertyToFocusOn);
		
	}

	public Bool focusOn(Text propertyName) { focusOnProperty(get(model), propertyName); return new Bool(true); }
	
	private void focusOnProperty(Element model, Text propertyName) {
		
		Property p = model.getProperty(propertyName);
		if(p == null) {
			System.err.println("Couldn't find a property named " + propertyName + " in " + model);
			return;
		}
		View propertyView = p.getFirstView();
		if(propertyView == null) propertyView = model.getFirstView();
		if(propertyView == null) {
			System.err.println("No view of property named " + propertyName);
			return;
		}
		View focusable = propertyView.getDeepestFocusableAncestor(true);
		if(focusable == null) {
			System.err.println("" + p.getFirstView() + " has no focusable child");
			return;
		}
		focusable.requestKeyboardFocus("previous");

	}
	
	public Bool focusOnProperty(Property p) {
		
		p.getFirstView().requestKeyboardFocus("next");
		return new Bool(true);		
		
	}
	
	public Bool focusOnModel(Element e) {

		View firstView = e.getFirstView();
		if(firstView == null) throw new ElementError("" + e + " has no view yet; could it be that it's owner's view's children property hasn't been initialized yet?", null);
		e.getFirstView().requestKeyboardFocus("next");
		return new Bool(true);
		
	}
	
	public List<ElementView> shortestPathTo(ElementView v) {

		List<ElementView> path = new List<ElementView>();

		if(this == v) {
			
			path.append(this);
			return path;
			
		}
		
		// What's the common ancestor?
		View commonAncestor = this.lowestCommonAncestorWith(v);

		// Find the first element view owner of the common ancestor.
		while(commonAncestor != null && !(commonAncestor instanceof ElementView))
			commonAncestor = commonAncestor.getParent();
		
		// If there is none, we return an empty path.
		if(commonAncestor == null) return path;

		// Is the common ancestor a list? If so, add the children of the list in the path
		if(commonAncestor instanceof ListView) {
		
			// What's the first child of the common ancestor in the path?
			View ancestor = this;
			ElementView firstChildOfAncestor = null;
			while(ancestor != commonAncestor) {
				if(ancestor instanceof ElementView)
					firstChildOfAncestor = (ElementView)ancestor;
				ancestor = ancestor.getParent();
			}

			// What's the last child of the common ancestor in teh path?
			ancestor = v;
			ElementView lastChildOfAncestor = null;
			while(ancestor != commonAncestor) {
				if(ancestor instanceof ElementView)
					lastChildOfAncestor = (ElementView)ancestor;
				ancestor = ancestor.getParent();
			}
			
			if(firstChildOfAncestor == null || lastChildOfAncestor == null) {
				
				path.append((ElementView)commonAncestor);
				return path;
				
			}

			int indexFirst = commonAncestor.get(children).indexOf(firstChildOfAncestor).value;
			int indexLast = commonAncestor.get(children).indexOf(lastChildOfAncestor).value;
			int direction = indexFirst < indexLast ? 1 : -1;
			for(int i = indexFirst; i != indexLast + direction; i += direction) 
				path.append((ElementView)commonAncestor.get(children).itemAt(new Int(i)));
			
		}
		// Otherwise, just include the common ancestor
		else path.append((ElementView)commonAncestor);

		return path;
		
	}

}