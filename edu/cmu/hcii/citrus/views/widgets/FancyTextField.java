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
import edu.cmu.hcii.citrus.views.View;

public class FancyTextField extends TextField {

	
	public FancyTextField(Namespace type, ArgumentList arguments) {
		super(type, arguments);
		// TODO Auto-generated constructor stub
	}
	
	public static final Dec<Set<Element>> validValues = new Dec<Set<Element>>(false, (Element)null);


	public static final Dec<List<View>> children = new Dec<List<View>>(
		"[(a PropertyLabel font=font text=(property name))]");

	public static class PropertyLabel extends Label {

		public static final Dec<Real> transparency = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
			return env.getEnclosingInstance().get(text).length().value > 0 ? new Real(0.0) : new Real(0.25);
		}});

		public PropertyLabel(ArgumentList arguments) { super(arguments); }
		
	}
/*
	new Behavior(App.keyboard.typed, true,
			new Action() { public boolean evaluate(View t) {
			
			Menu setView = (Menu)t.get(children).nth(new Int(2));

			boolean result = false;
			TextField tf = (TextField)t;
			if(App.keyboard.COMMAND.isDown() || App.keyboard.CONTROL.isDown()) return false;
			else if(App.keyboard.SHIFT.isDown() && App.keyboard.getLastCharacterTyped().value == ' ') {
					
				// If we don't have a set view, make one
				if(setView == null) {
					if(t.get(validValues) == null) t.set(validValues, new Set<Element>());
					ArgumentList args = new ArgumentList();
					args.add("property", false, t.getPropertyByDeclaration(validValues));
					args.add("model", false, t.get(validValues));
					setView = new Menu(args);
					t.getPropertyByDeclaration(validValues).addView(setView);
					t.addChild(setView);
				}

				// Position the view below the text field, hoist it and reveal it.
				setView.set(top, t.paddedHeight());
				setView.set(hoisted, new Bool(true));
				setView.set(hidden, new Bool(false));

				result = true;

			}
			else if(App.keyboard.ENTER.isDown()) {
				if(setView != null && !setView.get(hidden).value) {
					View selection = setView.get(Menu.selection);
					if(selection != null) t.set(text, (Text)selection.get(ElementView.model));
					setView.set(hoisted, new Bool(false));
					setView.set(hidden, new Bool(true));		
					t.getWindow().focusOn(t, "previous");
					result = true;
				}
				else result = false;
			}
			else if(App.keyboard.ALT.isDown()) return false;
			else if(App.keyboard.ESCAPE.isDown()) {
				
				if(setView != null && setView.bool(hidden)) {
					setView.set(hidden, new Bool(true));
					setView.set(hoisted, new Bool(true));
					result = true;
				}
				result = false;
				
			}			
			
			if(!result) {
				if(App.keyboard.getLastCharacterTyped().value == '\b')
					result = tf.backspace().value;
				else result = tf.insert(App.keyboard.getLastCharacterTyped()).value;
			}

			// Update the view if its being shown
			if(setView != null) {
				Set<Element> namesToRemove = new Set();
				Set<Element> namesToAdd = new Set();
				Set<Element> values = new Set();
				t.get(property).getValidValues(values);
				// Limit the set to names that start with the current text value.
				Set<Element> valuesThatStartWith = t.get(validValues);
				Text val = new Text(t.get(text).value.substring(0, t.get(caretIndex).value).trim());
				for(Element<?> value : values) {
					if(val.isEmpty().value && values.size().value > 20) namesToRemove.add(value);
					else if(value.toCitrusReference().startsWith(val).value) namesToAdd.add(value);
					else if(valuesThatStartWith.contains(value).value) namesToRemove.add(value);
				}
				valuesThatStartWith.addItems(namesToAdd);
				valuesThatStartWith.removeItems(namesToRemove);

			}
			
			return result;
			
		}}),

*/
}
