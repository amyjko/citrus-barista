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
package edu.cmu.hcii.citrus;

import java.util.*;

import static edu.cmu.hcii.citrus.Boot.*;

public class Dec<ValueType extends Element> extends Expression<DecInterface> implements DecInterface<ValueType> {

	public static final BootDec<Text> name = new BootDec<Text>(new Text(""));
	public static final BootDec<TypeExpression> typeExpression = new BootDec<TypeExpression>(new BootTypeExpression(ELEMENT));
	public static final BootDec<Element<?>> valueExpression = new BootDec<Element<?>>(new Nothing());
	public static final BootDec<Bool> functionIsConstraint = new BootDec<Bool>(new Bool(false));
	public static final BootDec<Bool> isStatic = new BootDec<Bool>(new Bool(false));
	public static final BootDec<Bool> isUndoable = new BootDec<Bool>(new Bool(false));
	public static final BootDec<Bool> isOverridable = new BootDec<Bool>(new Bool(true));
	public static final BootDec<Bool> isReference = new BootDec<Bool>(new Bool(false));
	public static final BootDec<Bool> isConstant = new BootDec<Bool>(new Bool(false));
	public static final BootDec<Bool> isRule = new BootDec<Bool>(new Bool(false));

	// This will be set by this property declaration's type when the type is consolidated.
	private DecInterface declarationOverriden = null;
	
	// This is the result of evaluating the declaration's value restriction expression.
	// Note that it currently isn't a List because in Property.java we check to
	// see if the result of validation is a restriction to know if there was a violation.
	public Vector<PropertyRestriction<ValueType>> restrictions = new Vector<PropertyRestriction<ValueType>>(4);

	// Runtime constructors
	public Dec() { super(); replaceValueFunctionDeclaration(); }
	public Dec(ArgumentList arguments) { super(arguments); replaceValueFunctionDeclaration(); }

	public Dec(Element<ValueType> expr) { this(expr, false, false, false); }
	public Dec(String expr) { this((Element<ValueType>)parseExpression(expr), false, false, false); }

	public Dec(boolean newFunctionIsConstraint, Element<ValueType> expr) { this(expr, newFunctionIsConstraint, false, false); }
	public Dec(boolean newFunctionIsConstraint, String expr) { this((Element<ValueType>)parseExpression(expr), newFunctionIsConstraint, false, false); }
	
	public Dec(Element<ValueType> expr, boolean newIsReference) { this(expr, false, newIsReference, false); }
	public Dec(String expr, boolean newIsReference) { this((Element<ValueType>)parseExpression(expr), false, newIsReference, false); }

	public Dec(boolean newFunctionIsConstraint, Element<ValueType> expr, boolean isReference) { this(expr, newFunctionIsConstraint, isReference, false); }

	public Dec(Element<ValueType> newValueFunction, 
					  boolean newFunctionIsConstraint, 
					  boolean newIsReference,
					  boolean newIsStatic) {

		set(Dec.valueExpression, newValueFunction);
		set(Dec.functionIsConstraint, new Bool(newFunctionIsConstraint));
		set(Dec.isStatic, new Bool(newIsStatic));
		set(Dec.isReference, new Bool(newIsReference));
		replaceValueFunctionDeclaration();
		
	}

	private void replaceValueFunctionDeclaration() {

		BootDec<Element<?>> newDeclaration = new BootDec<Element<?>>(null) {
			public TypeExpression getTypeExpression() {
				return new BaseTypeExpression(Boot.ELEMENT, Dec.this.get(typeExpression));
			}
		};
		newDeclaration.name = valueExpression.getName();
		getPropertyByDeclaration(valueExpression).updateDeclaration(newDeclaration);
		
	}

	public boolean isStatic() { return peek(isStatic).value; }
	public boolean isConstant() { return peek(isConstant).value; }
	public boolean isOverridable() { return peek(isOverridable).value; }
	public boolean isUndoable() { return peek(isUndoable).value; }

	
	// We return a potentially stale value so that every property that uses this in its value set
	// does not depend on the name's value.
	public Text cachedName = null; 	// For efficiency, set by BaseType.consolidate.
	public Text getName() { if(cachedName == null) return peek(Dec.name); return cachedName; }

	public void is(PropertyRestriction newValueSet) {

		if(newValueSet == null) throw new ElementError("Can't give a property declaration a null value set.", this);
		restrictions.add(newValueSet);

	}
	
	// Always returns this declaration's value function; disregards the overriden declaration's.
	public Element<ValueType> getValueFunction() { 

		Element f = getPropertyByDeclaration(valueExpression).peek();
		if(f == null)
			if(declarationOverriden == null) return null;
			else return declarationOverriden.getValueFunction();
		else return (Element<ValueType>)f;

	}

	public Element validate(Property<ValueType> p, ValueType value) {

		boolean isValid = true;
		
		// First validate the parent declaration. If it returns false, return false.
		if(declarationOverriden != null) {
			value = (ValueType)declarationOverriden.validate(p, value);
			if(value instanceof PropertyRestriction) return value;
		}

		// Then validate this declaration.
		Iterator<PropertyRestriction<ValueType>> i = restrictions.iterator();
		while(i.hasNext()) {
			
			PropertyRestriction<ValueType> vs = i.next();
			
			// Validate the value. If the value set
			// was violated and it disallows invalid values, set the flag to false.
			if(!vs.isValid(p, value)) {

				// Try to correct it
				if(vs.correctable())
					value = vs.correct(p, value);
				else return vs;

			}
			
		}
		return value;
		
	}
	
	// Gather all of the value sets' valid values.
	public Set<ValueType> getValidValues(Property<ValueType> property, Set<ValueType> values) {
		
		// If there's a declaration this elaborates, first get its valid values.
		if(declarationOverriden != null) declarationOverriden.getValidValues(property, values);

		// Add the valid values from this value set.
		for(PropertyRestriction vs : restrictions)
			vs.getValidValues(property, values);

		return values;
		
	}

	public DecInterface<ValueType> getDeclarationOverridden() { return declarationOverriden; }
	
	public Bool overrides() { return new Bool(declarationOverriden != null); }
	
	// This should only be called by Type.consolidate().
	public void setDeclarationOverriden(DecInterface<ValueType> newDeclarationOverriden) { declarationOverriden = newDeclarationOverriden; }
		
	public Dec evaluate(Element<?> env) { return this; }
		
	// Should make a property based on this declaration.
	public Property<ValueType> make(Element owner) {
		return new Property<ValueType>(owner, this); }
	
	// Should return a copy of the default value for this declaration.
	public ValueType getDefaultValue(Element newElement) {

		// Get the default value expression
		Element<?> e = get(valueExpression);

		// Otherwise, evaluate the default value expression.
		if(e == null) {
			// And there's no elaborated declaration, duplicate the nothing.
			if(getDeclarationOverridden() == null) return null;
			// Otherwise, defer to the elaborated declaration.
			else return getDeclarationOverridden().getDefaultValue(newElement);
		}
		// Otherwise, evaluate the default value expression in the context of the new instance.
		else return (ValueType)e.evaluate(newElement);

	}
	
	// Should be checking if the default is a constant.
	public boolean valueIsRecoverable(ValueType value) { return false; }


	public TypeExpression getTypeExpression() { 
		
		TypeExpression type = peek(typeExpression); 
		if(type == null) return getDeclarationOverridden().getTypeExpression();
		else return type;
		
	}
	
	public Namespace<?> getType() { return DECLARATION; }
	
	public boolean isParameterized() { return peek(valueExpression) instanceof Parameter; }
	public boolean isReferenceOnly() { return peek(isReference).value; }
	public boolean functionIsConstraint() { return peek(functionIsConstraint).value; }

	public Context contextFor(Element e) { return (Namespace)ownerOfType(Boot.TYPE); }
			
	public String toString() { 

		String str = "" + get(typeExpression);
		
		if(getDeclarationOverridden() != null) str = str + " *" + get(name);
		else str = str + " " + get(name); 
		return str;
		
	}
	
	public Text toCitrus() {
		
		String str = "has " + get(typeExpression).toCitrus() + " " + get(name) + " ";
		str = str + (get(functionIsConstraint).value ? "<-" : "=") + " ";
		Element<?> e = peek(valueExpression);
		str = str + ((e == null) ? "null" : e.toCitrus());
		return new Text(str);
		
	}
	
	// Override with a new type and value expression
	public Dec override(TypeExpression newTypeExpression, Element<?> newValueExpression) {

		if(newTypeExpression == null) newTypeExpression = (TypeExpression)get(typeExpression).duplicate();

		Dec<?> newDec = new Dec();
		newDec.set(name, get(name));
		newDec.set(typeExpression, newTypeExpression);
		newDec.set(valueExpression, newValueExpression);
		
		return newDec;
		
	}
	
	// Search through the value set and find the smallest boundary
//	public double getMinimum() {
//
//		double minimum = Double.MAX_VALUE;
//		Iterator<ValueRestriction> li = getValueSets().iterator();
//		if(li != null) 
//			while(li.hasNext())	{
//				ValueRestriction vs = li.next();				
//				if(vs instanceof Boundary) {
//					Real boundary = ((Boundary)vs).getBoundary(this);
//					if(boundary.value < minimum) minimum = boundary.value;
//				}
//					
//			}
//		if(minimum == Double.MAX_VALUE) return Double.MIN_VALUE;
//		else return minimum;
//		
//	}
//	public double getMaximum() {
//	
//		double maximum = Double.MIN_VALUE;
//		Iterator<ValueRestriction> li = getValueSets().iterator();
//		if(li != null) 
//			while(li.hasNext()) {
//				ValueRestriction vs = li.next();
//				if(vs instanceof Boundary) {
//					Real boundary = ((Boundary)vs).getBoundary(this);
//					if(boundary.value > maximum) maximum = boundary.value;
//				}
//			}
//		if(maximum == Double.MIN_VALUE) return Double.MAX_VALUE;
//		else return maximum;
//		
//	}

	
}
