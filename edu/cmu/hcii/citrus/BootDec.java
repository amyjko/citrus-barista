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

public class BootDec<ValueType extends Element> extends BootElement<DecInterface> implements DecInterface<ValueType> {

	// A hack for preventing validation during bootstrapping.
	public static boolean validate = true;
	
	public Text name;
	public Element<?> defaultValue;
	public TypeExpression type;
	
	public LinkedList<PropertyRestriction<ValueType>> valueSets = new LinkedList<PropertyRestriction<ValueType>>();
	private DecInterface<ValueType> declarationOverriden = null;
	public boolean parameterized = false;
	protected boolean referenceOnly = false;
	public boolean isUndoable = false;
	public boolean functionIsConstraint = false;

	public BootDec(Element newDefaultValue) { defaultValue = newDefaultValue; }
	public BootDec(Element newDefaultValue, boolean isReference) { defaultValue = newDefaultValue; referenceOnly = isReference; }

	// NAME
	public Text getName() { return name; }

	// MODIFIERS
	public boolean isStatic() { return false; }
	public boolean isOverridable() { return false; }
	public boolean isConstant() { return false; }
	public boolean isUndoable() { return isUndoable; }
	public boolean isReferenceOnly() { return referenceOnly; }

	// TYPE
	public TypeExpression getTypeExpression() { return type; }

	// RESTRICTIONS	
	public void is(PropertyRestriction<ValueType> newValueSet) { valueSets.add(newValueSet); }
	public Element validate(Property<ValueType> p, ValueType value) {

		if(!validate) return value;
		
		boolean isValid = true;
		
		// First validate the parent declaration. If it returns false, return false.
		if(declarationOverriden != null) {
			value = (ValueType)declarationOverriden.validate(p, value);
			if(value instanceof PropertyRestriction) return value;
		}

		// Then validate this declaration.
		Iterator<PropertyRestriction<ValueType>> i = (Iterator<PropertyRestriction<ValueType>>)valueSets.iterator();
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
	public Set<ValueType> getValidValues(Property<ValueType> property, Set<ValueType> values) {
		
		if(!validate) return values;

		// Add the valid values from this value set.
		for(PropertyRestriction vs : valueSets)
			vs.getValidValues(property, values);
		return values;
		
	}

	// DEFAULT	
	public ValueType getDefaultValue(Element newElement) { 
	
		if(defaultValue == null) return null;
		else if(functionIsConstraint) return null;
		else return (ValueType)defaultValue.duplicate();
		
	}
	public boolean valueIsRecoverable(ValueType value) { return true; }
	public boolean isParameterized() { return parameterized; }
	public boolean functionIsConstraint() { return functionIsConstraint; }

	// CONSTRAINTS
	public Element<ValueType> getValueFunction() { return functionIsConstraint ? (Element<ValueType>)defaultValue : null; }
	
	// OVERRIDING
	public DecInterface<ValueType> getDeclarationOverridden() { return null; }
	public void setDeclarationOverriden(DecInterface<ValueType> pd) { declarationOverriden = pd; }

	public Property<ValueType> make(Element owner) { return new Property<ValueType>(owner, this); }

	////////////////////////////////////////////////
	//
	// ElementInterface implementation
	//
	////////////////////////////////////////////////
	
	public Namespace<?> getType() { return DECLARATION; }
	
	// Should return the value of the property of the given name.
	public <VType extends Element<?>> VType get(DecInterface<VType> declaration) {

		return (VType)get(declaration.getName());

	}
	
	public <VType extends Element<?>> boolean set(DecInterface<VType> dec, VType value) {

		if(dec == Dec.name) name = (Text)value;
		else if(dec == Dec.typeExpression) {
			if(type == value) return false;
			else { type = (TypeExpression)value; return true; }
		}
		else throw new ElementError("Haven't implemented set(" + dec + ") in BootstrappedPropertyDec.set()", this);		
		return true;
		
	}
	
	public Element get(Text name) {

		if(name.value.equals("name")) return this.name;
		else if(name.value.equals("type")) return getTypeExpression();
		else return null;
		
	}

	public Element duplicate() { throw new RuntimeException("Can't duplicate a bootstrapped property declaration"); }
	
	public Bool isEquivalentTo(Element<?> e) { return new Bool(this == e); }

	public DecInterface evaluate(Element<?> env) { return this; }
	
	public String toString() { 
	
		return "has " + getTypeExpression() + " " + getName();
		
	}
	
	public Text toCitrus() { return new Text(toString()); }
	public Text toCitrusReference() { return toCitrus(); }

	
}