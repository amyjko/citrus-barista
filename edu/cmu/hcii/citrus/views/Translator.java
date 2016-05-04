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

import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;

public class Translator {

	public static boolean debug = false;
	
	public static boolean noTranslation = false;
	
	// The root view table, storing a table for all specific types of elements.
	public static ViewTable ownershipViews = new ViewTable(Boot.ELEMENT, null);
	public static ViewTable referenceViews = new ViewTable(Boot.ELEMENT, null);

	// Mimics the structure of a type expression, with a table for the various arguments.
	private static class ViewTable {
		
		private Type baseType;
		private Type baseView;
		private Hashtable<Type,ViewTable> views = new Hashtable<Type,ViewTable>(5);
		
		public ViewTable(Type newBaseType, Type newBaseView) {
			
			baseType = newBaseType;
			baseView = newBaseView;
			
		}

		public void addView(Type view, TypeExpression type) {

			if(type.getBaseType() == null) {
				System.err.println("Can't add view " + view + " for type with no base type: " + type);
				return;
			}
			
			// Is there a table for this base type?
			ViewTable tableForBaseType = views.get(type.getBaseType());
			if(tableForBaseType == null) {

				// Is this view a generic version of this base type?
				if(type.getTypeArguments().isEmpty().value)
					views.put(type.getBaseType(), new ViewTable(type.getBaseType(), view));
				// Otherwise, make a table with no generic view, and add this specific view
				else {
					ViewTable newTable = new ViewTable(type.getBaseType(), null);
					views.put(type.getBaseType(), newTable);
					newTable.addView(view, type.getTypeArguments().first());					
				}
				
			}			
			// Otherwise, add this view to this table
			else {

				if(type.getTypeArguments().isEmpty().value) tableForBaseType.setGenericView(view);
				else tableForBaseType.addView(view, type.getTypeArguments().first());
			
			}
						
		}
		
		public void setGenericView(Type newBaseView) {
			
			if(baseView != null) throw new ElementError("Can't have two generic views of " + baseType + ": " + 
													baseView + " and " + newBaseView, null);
			else baseView = newBaseView;
			
		}

		public Type getViewFor(TypeExpression type) {
			
			if(type == null) return baseView;
			
			if(type.getBaseType() == null) {
				return baseView;
			}
			
			ViewTable tableForBaseType = views.get(type.getBaseType());

			if(tableForBaseType == null) {
				return baseView;
			}

			Type view = null;
			if(type.getTypeArguments() != null)
				view = tableForBaseType.getViewFor(type.getTypeArguments().first());

			if(view == null) return baseView;
			else return view;
			
		}
		
		public Type getBaseViewFor(Type type) {
			
			ViewTable tableForBaseType = views.get(type);
			if(tableForBaseType == null) return baseView;
			else return tableForBaseType.getViewFor(null);			
			
		}
		
		public String toString() {
			
			return "Base type = " + baseType + "\n" + "Base view = " + baseView + "\n" + "tables = " + views.values();
			
		}
		
	}
	
	public static void addView(Class view) { addView((BaseType)Reflection.getJavaType(view)); }
	public static void addView(BaseType view) {

		// Is this an ownership or reference view?
		if(!view.isTypeOf(Reflection.getJavaType(ElementView.class)).value) return;
		
		// What type does the view's model expect?
		// Get the base type of the view's model
		DecInterface<?> declaration = null;
		for(DecInterface dec : view.get(BaseType.properties)) 
			if(dec.getName().equals(ElementView.model.getName())) {
				declaration = dec;
				break;
			}

		if(declaration != null) {
			if(view.isTypeOf(Reflection.getJavaType(ReferenceView.class)).value)
				referenceViews.addView(view, declaration.getTypeExpression());
			else
				ownershipViews.addView(view, declaration.getTypeExpression());
		}

	}

	public static void addViews(Unit language) {
		
		for(Type type : language.get(Unit.types)) 
			if(type instanceof BaseType)
				addView((BaseType)type);
		
	}
	
	public static ElementView toView(Property<?> p) {
		
		if(noTranslation) {
			ArgumentList args = new ArgumentList();
			args.add("property", p);
			args.add("model", p.get());
			return new NoTranslatorView(args);
		}

		Element model = p.get();
		if(p.valueIsNothing()) model = p.getNothing();		

		ElementView newView = null;
		
		newView = toView(p, p.isReference(), p.getTypeExpression().getTypeArguments(), model);

		if(newView != null) p.addView(newView);		

		return newView;

	}

	public static ElementView toView(Property p, boolean reference, List<TypeExpression> typeArgs, Element<?> model) {
		
		ArgumentList args = new ArgumentList();

		ViewTable tableToLookIn = reference ? referenceViews : ownershipViews;
		if(model.isNothing().value) tableToLookIn = ownershipViews;

		args.add("model", model);
		if(p != null) args.add("property", p);

		Type translator = null;
				
		// Make a custom type expression to hold the specific base type and the arguments.
		// This essentially merges the model's type and the model's expected arguments.
		BootTypeExpression mergedType = new BootTypeExpression((Type)model.getType());
		// If the model is nothing, we use the property's type args
		mergedType.arguments = model.isNothing().value ? new List(p.getTypeExpression()) : typeArgs;
		translator = tableToLookIn.getViewFor(mergedType);
		
		if(translator == null) {
			Type prototype = (Type)model.getType();
			while(translator == null && prototype != null) {
				translator = tableToLookIn.getBaseViewFor(prototype);
				prototype = prototype.getPrototype();
			}
		}

		ElementView newView = null;

		if(translator == null) {
			if(!reference) {
				System.err.println("No translator for " + model + " of type " + model.getType() + " for property " + p + " owned by " + p.getElementOwner());
				newView = new NoTranslatorView(args);
			}
			else {
				System.err.println("No reference translator for " + model + " of type " + model.getType() + " for property " + p +" owned by " + p.getElementOwner());
				newView = new NoTranslatorView(args);
			}
		}
		else {
			
			// Look for an existing unused view
			for(ElementView ev : model.getViews()) {
				if(ev.getType() == translator && ev.getParent() == null && ev.getPreviousSibling() == null) {
					System.err.println("Warning: Using existing view " + ev + ", which may cause cycles.");
					newView = ev;
					break;
				}
				//else System.err.println("Not using existing view of " + model + ", " + ev + ", which is in window " + ev.getWindow());
			}
			
			// If we didn't find one, use the translator to instantiate it.
			if(newView == null)
				newView = (ElementView)translator.instantiate(args);
						
		}
		
		model.addView(newView);
		return newView;

	}

}