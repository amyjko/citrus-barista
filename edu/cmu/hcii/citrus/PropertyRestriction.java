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

//
// Describes a set of values that a property may hold. For sets of infinite size,
// this set is described as a the conjunction of a set of boolean-valued functions. 
// For sets of finite size, the set is stored as a collection of values.
//
// NOTE: If any two ValueSet functions are in conflict (for example, X > 5 and X < 0)
// the ORDER of the value sets' evaluation determines the resolution. Either order them
// properly or avoid these conflicts altogether.
public class PropertyRestriction<ValueType extends Element> extends BaseElement<PropertyRestriction> {

	public PropertyRestriction() {}
	public PropertyRestriction(ArgumentList args) { super(args); }
	public PropertyRestriction(String conditionExpression, String correctionExpression) {
		
		set(condition, (Expression)parseExpression(conditionExpression));
		if(correctionExpression != null)
			set(correction, (Expression)parseExpression(correctionExpression));
		
	}

	public static final BootDec<Expression> condition = new BootDec<Expression>(null);
	public static final BootDec<Expression> correction = new BootDec<Expression>(null);

	// By default, this can't correct the value and simply returns it. Override this
	// to correct the invalid value given.
	public ValueType correct(Property property, ValueType value) { 

		Element correctionExpression = peek(correction);
		if(correctionExpression == null) return value;
		else return (ValueType)correctionExpression.evaluate(property.getElementOwner());
		
	}
	
	public boolean correctable() { return peek(correction) != null; }
	
	// Should return true if the value is valid.
	public boolean isValid(Property<ValueType> property, ValueType value) {

		return ((Bool)peek(condition).evaluate(property.getElementOwner())).value;
		
	}

	// Should returns a collection of the legal values. Should returns null if there are an
	// infinite number of valid values.
	public Set<ValueType> getValidValues(Property<ValueType> property, Set<ValueType> values) {
		
		return values;
		
	}

	// Should return a string describing the test, such as "instance of ***" or "less than ***".
	public String why(Property property) {
		
		return "Must satisfy " + peek(condition);
		
	}
	
	public String toString() { 
		
		return "value must satisfy " + peek(condition);
		
	}
	
}