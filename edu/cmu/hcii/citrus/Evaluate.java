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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

import edu.cmu.hcii.citrus.views.Transition;

public class Evaluate<ResultType extends Element> extends Expression<ResultType> {

	public static boolean recordingTimes = false;
	public static final Hashtable<Evaluate,Profile> profiles = new Hashtable<Evaluate,Profile>(2000);
	
	// A stack of evals; only for debugging right now.
	public static final Stack<Evaluate> evalStack = new Stack<Evaluate>();
	
	// The environment to find the function in and which to evaluate in
	public static final Dec<Element<?>> functionContext = new Dec<Element<?>>((Element)null, false);

	// The function to be evaluated
	public static final Dec<Element<?>> function = new Dec<Element<?>>();

	// The arguments to be evaluated and passed into the local environment.
	public static final Dec<List<Arg>> arguments = new Dec<List<Arg>>(new NewList<Arg>());

	public static class TypesAndFunctions extends PropertyRestriction<Text> {
		public static final Dec<Bool> allowInvalid = new Dec<Bool>(new Bool(true));
		public boolean isValid(Property<Text> property, Text value) {

			Element<?> owner = property.getElementOwner();
			if(owner == null) return false;
			Context env = owner.get(context);
			if(env == null) return false;
			return env.getTypeNamed(value.value) != null || env.getFunctionNamed(value, null) != null;
			
		}

		public Set<Text> getValidValues(Property<Text> property, Set<Text> values) {
			
			Element<?> owner = property.getElementOwner();
			if(owner == null) return values;
			Context<?> env = owner.get(context);
			if(env == null) return values;
			for(Type type : env.getTypesInContext(new Set<Type>()))
				values.add(type.get(BaseType.name));
			for(Function function : env.getFunctionsInContext(new Set<Function>()))
				values.add(new Text(function.getName()));
			return values;
			
		}

		public String why(Property property) { return "Must be in environment"; }		
		
	}

	
	public Evaluate() { super(); }	
	public Evaluate(Namespace type, ArgumentList args) { super(type, args); }	
	public Evaluate(Element newContext, Element newFunction, Arg ... args) { 
		
		super(); 
		set(functionContext, newContext);
		set(function, newFunction);
		for(Arg arg : args) get(arguments).append(arg);
	
	}	
	
	public Bool isSpecialForm() {
		
		Element<?> contextExpression = get(functionContext);
		if(contextExpression instanceof Ref) {
			String ref = ((Ref)contextExpression).get(Ref.token).value;
			return new Bool(isSpecialForm(ref));
		}
		return new Bool(false);
		
	}
	
	public static boolean isSpecialForm(String s) {
		
		if(s.equals("if")) return true;
		else if(s.equals("do")) return true;
		else if(s.equals("array")) return true;
		else if(s.equals("return")) return true;
		else if(s.equals("fun")) return true;
		else if(s.equals("let")) return true;
		else if(s.equals("eval")) return true;
		else if(s.equals("cond")) return true;
		else if(s.equals("while")) return true;
		else if(s.equals("debug")) return true;
		else if(s.equals("profiler")) return true;
		else return false;
		
	}

	public static class Profile implements Comparable {
		
		private final Evaluate eval;
		public long totalTime = 0;
		public int totalExecutions = 0;
		public Profile(Evaluate newEval, long firstTime) {
			eval = newEval;
			totalTime = firstTime;
			totalExecutions = 1;
		}
		public double averageExecutionTime() { return totalTime / totalExecutions; }
		public int compareTo(Object p) {
			
//			Double thisTime = new Double(averageExecutionTime());
//			Double thatTime = new Double(((Profile)p).averageExecutionTime());
			Double thisTime = new Double(totalTime);
			Double thatTime = new Double(((Profile)p).totalTime);
			return thatTime.compareTo(thisTime);
		}
		public String toString() {

			String eString = eval.toString();
			eString = eString.substring(0, Math.min(eString.length(), 80)) + " ...";
			return "" + totalExecutions + " times over " + totalTime + " ms = " + 
						(averageExecutionTime() / 1000.0) + " sec/eval\t\t" + 
						eval.get(Evaluate.function) + ": " + eString;

		}
	}
	
	// Create a new local environment to pass to the function,
	// and set up the local environments formal parameter values.
	// We evaluate the instance in which to evaluate the function
	// in order to determine the local environments enclosing environment.
	public ResultType evaluate(Element<?> env) {

		evalStack.push(this);

		ResultType result = null;
		if(recordingTimes) {
			Profile p = profiles.get(this);
			long t1 = System.currentTimeMillis();
			result = (ResultType)eval(env, peek(functionContext), peek(function), peek(arguments));		
			long t2 = System.currentTimeMillis();
			if(p== null)
				profiles.put(this, new Profile(this, t2-t1));
			else {
				p.totalExecutions++;
				p.totalTime += t2-t1;
			}				
		}
		else 
			result = (ResultType)eval(env, peek(functionContext), peek(function), peek(arguments));		

		evalStack.pop();
		return result;
		
	}

	// env - the environment in which the arguments are evaluated
	// context - in which the namespace expression is evaluated to find the function (or type)
	// namespaceExpression - a function name, type name (i.e., View, View.LostFocus, postDamage)
	// args - a list of arguments to be evaluated and passed to the function.
	public static Element eval(
			Element env, 
			Element<?> objectExpression, 
			Element<?> functionOrTypeExpression, 
			List<Arg> argExpressions) {

		// Special forms for the context
		if(objectExpression instanceof Ref) {
			String ref = ((Ref)objectExpression).peek(Ref.token).value;
			if(ref.equals("if")) {		
				Element conditionValue = argExpressions.first().peek(Arg.value).evaluate(env);
				Bool condition = conditionValue instanceof Bool ? (Bool)conditionValue : Bool.FALSE;
				if(condition.value) return argExpressions.second().peek(Arg.value).evaluate(env);
				else if(argExpressions.length().value > 2) return argExpressions.third().peek(Arg.value).evaluate(env);
				else return null;
			}
			else if(ref.equals("array")) {

				Int length = (Int)argExpressions.first().peek(Arg.value).evaluate(env);
				Array newArray = new Array(length.value);
				return newArray;
				
			}
			else if(ref.equals("fun")) {

				// Create a closure using the function's body and arguments and the current environment.
				return new Closure((BaseFunction)functionOrTypeExpression, env);
				
			}
			else if(ref.equals("let")) {

				ArgumentList arguments = new ArgumentList();
				arguments.enclosingEnvironment = env;
				return ((Let)functionOrTypeExpression).instantiate(arguments);
				
			}
			else if(ref.equals("eval")) {
				
				List<Arg> newArgs = new List<Arg>();
				Function f = null;
				for(Arg arg : argExpressions) {
					if(f == null) f = (Function)arg.peek(Arg.value).evaluate(env);
					else newArgs.append(arg);
				}				
				return Evaluate.eval(env, env, f, newArgs);
				
			}
			else if(ref.equals("dump")) {
				
				Thread.dumpStack();
				return null;
			}
			else if(ref.equals("profiler")) {

				String action = ((Ref)argExpressions.first().value()).text(Ref.token);
				
				if(action.equals("start")) {	
					profiles.clear();
					recordingTimes = true;
				}
				else if(action.equals("stop")) {
					recordingTimes = false;
				}
				else if(action.equals("print")) {
					
					TreeSet<Profile> map = new TreeSet(profiles.values());
					for(Profile e : map)
						System.err.println("" + e);
					
				}
				
				return null;
			}
			else if(ref.equals("cond")) {
				
				// 1st, 3rd, 5th, etc are conditions
				// 2nd, 4th, 6th, etc are responses
				Iterator<Arg> argumentIterator = argExpressions.iterator();
				while(argumentIterator.hasNext()) {
					Bool condition = (Bool)argumentIterator.next().peek(Arg.value).evaluate(env);
					Element response = argumentIterator.next().peek(Arg.value);
					if(condition.value) return response.evaluate(env);					
				}
				throw new ElementError("One of a cond's conditions must evaluate to true!", env);
				
			}
			else if(ref.equals("debug")) {

				Namespace type = Ref.resolve(null, env, new Text("this"), Ref.ReferenceType.UNKNOWN).getType();
				if(!(type instanceof BaseType) || (type instanceof BaseType && ((BaseType)type).get(BaseType.debug).value)) {
					Iterator<Arg> argumentIterator = argExpressions.iterator();
					while(argumentIterator.hasNext()) {
						Element arg = argumentIterator.next().peek(Arg.value).evaluate(env);
						System.err.print("" + arg);
					}
					System.err.println("");
				}
				return null;
				
			}
			else if(ref.equals("while")) {
				
				Element condition = argExpressions.first().value();
				Element action = argExpressions.second().value();
				try {
					while(((Bool)condition.evaluate(env)).value) {
						
						action.evaluate(env);
						
					}
				} catch(ClassCastException e) {
					throw new ElementError("While loop's condition didn't return a boolean", null);					
				}
				
				return null;
				
			}

		}
		
		// Get the environment to evaluate in
		Element environmentToEvaluateIn = objectExpression == null ? env : objectExpression.evaluate(env);
		if(environmentToEvaluateIn == null) environmentToEvaluateIn = new Nothing();

		if(environmentToEvaluateIn instanceof Nothing) {
			
			if(!(functionOrTypeExpression instanceof Ref))
				throw new ElementError("Can only call functions on null, not " + functionOrTypeExpression, null);
			String propertyFunction = ((Ref)functionOrTypeExpression).peek(Ref.token).value;			
			if(propertyFunction.equals("toText"))
				return environmentToEvaluateIn.toText();
			
		}
		if(environmentToEvaluateIn instanceof List && functionOrTypeExpression instanceof Ref) {

			String propertyFunction = ((Ref)functionOrTypeExpression).peek(Ref.token).value;			
			if(propertyFunction.equals("getPropertyOwner"))
				return environmentToEvaluateIn.getPropertyOwner();
			
		}
		else if(environmentToEvaluateIn instanceof Property) {

			if(!(functionOrTypeExpression instanceof Ref))
				throw new ElementError("When calling a function on a property, function must be a single word, not " + functionOrTypeExpression, null);
			return handlePropertyFunction(
					env,
					(Property)environmentToEvaluateIn, 
					((Ref)functionOrTypeExpression).peek(Ref.token).value,
					argExpressions);

		}
		else if(environmentToEvaluateIn instanceof Real) {
			
			String fun = ((Ref)functionOrTypeExpression).peek(Ref.token).value;
			
			if(fun.equals("/")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				if(arg2 instanceof Real) return new Real(((Real)environmentToEvaluateIn).value / ((Real)arg2).value);
				else if(arg2 instanceof Int) return new Real(((Real)environmentToEvaluateIn).value / ((Int)arg2).value);
			}
			else if(fun.equals("*")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				if(arg2 instanceof Real) return new Real(((Real)environmentToEvaluateIn).value * ((Real)arg2).value);
				else if(arg2 instanceof Int) return new Real(((Real)environmentToEvaluateIn).value * ((Int)arg2).value);
			}
			else if(fun.equals("+")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				return new Real(((Real)environmentToEvaluateIn).value + ((Real)arg2).value);
			}
			else if(fun.equals("-")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				return new Real(((Real)environmentToEvaluateIn).value - ((Real)arg2).value);
			}
			else if(fun.equals(">")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				return new Bool(((Real)environmentToEvaluateIn).value > ((Real)arg2).value);
			}
			else if(fun.equals("<")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				return new Bool(((Real)environmentToEvaluateIn).value < ((Real)arg2).value);
			}
			else if(fun.equals(">=")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				return new Bool(((Real)environmentToEvaluateIn).value >= ((Real)arg2).value);
			}
			else if(fun.equals("<=")) {
				Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
				return new Bool(((Real)environmentToEvaluateIn).value <= ((Real)arg2).value);
			}
			else if(fun.equals("min")) {
				Real min = (Real)environmentToEvaluateIn;
				for(BaseElement<?> expr : argExpressions) {
					Real val = (Real)expr.peek(Arg.value).evaluate(env);
					if(val.value < min.value) min = val;
				}
				return min;
			}
			else if(fun.equals("max")) {
				Real max = (Real)environmentToEvaluateIn;
				for(BaseElement<?> expr : argExpressions) {
					Real val = (Real)expr.peek(Arg.value).evaluate(env);
					if(val.value > max.value) max = val;
				}
				return max;
			}
			else if(fun.equals("toText")) return environmentToEvaluateIn.toText();
			
		}
		else if(environmentToEvaluateIn instanceof Int) {
			
			Element result = 
				handleIntFunction(
					env, 
					(Int)environmentToEvaluateIn, 
					((Ref)functionOrTypeExpression).peek(Ref.token).value, 
					argExpressions);
			if(result != null) return result;

		}
		else if(environmentToEvaluateIn instanceof Bool) {
			
			String fun = ((Ref)functionOrTypeExpression).peek(Ref.token).value;
			if(fun.equals("or")) {
				if(((Bool)environmentToEvaluateIn).value) return environmentToEvaluateIn;
				for(Arg orArg : argExpressions) {
					Bool result = (Bool)orArg.peek(Arg.value).evaluate(env);
					if(result.value) return result;
				}
				return new Bool(false);
			}
			else if(fun.equals("and")) {
				if(!((Bool)environmentToEvaluateIn).value) return environmentToEvaluateIn;
				for(Arg orArg : argExpressions) {
					Bool result = (Bool)orArg.peek(Arg.value).evaluate(env);
					if(!result.value) return result;
				}
				return new Bool(true);
			}
			else if(fun.equals("toText")) return environmentToEvaluateIn.toText();
			
		}
		else if(environmentToEvaluateIn instanceof Text) {
			
			String fun = ((Ref)functionOrTypeExpression).peek(Ref.token).value;
			if(fun.equals("+")) {

				String result = ((Text)environmentToEvaluateIn).value;
				for(Arg orArg : argExpressions) {
					Element text = orArg.peek(Arg.value).evaluate(env);
					if(text instanceof Text) result = result + ((Text)text).value;
					else throw new ElementError("Passed non-text to function + ", functionOrTypeExpression);					
				}
				return new Text(result);
				
			}
			else if(fun.equals("toText")) return environmentToEvaluateIn.toText();
			
		}

		boolean instantiation = (objectExpression instanceof Ref) && 
			(((Ref)objectExpression).peek(Ref.token).value.equals("a") ||
			((Ref)objectExpression).peek(Ref.token).value.equals("an"));

		// Find the type to instantiate or function to evaluate.		
		Namespace<?> namespaceToInstantiate = null;
		Element namespaceValue = functionOrTypeExpression.evaluate(environmentToEvaluateIn);
		if(namespaceValue instanceof Namespace)
			namespaceToInstantiate = ((Namespace)namespaceValue);
		else 
			throw new ElementError("" + environmentToEvaluateIn + " evaluated to " + namespaceValue + ", which isn't a namespace: ", null);
		
		if(namespaceValue == null)
			throw new ElementError(
					"Didn't find function or type named \"" + functionOrTypeExpression + 
					"\" from " + environmentToEvaluateIn + " among " + 
					environmentToEvaluateIn.getType().getFunctionsInContext(new Set()) + " or " +
					environmentToEvaluateIn.getType().getTypesInContext(new Set()), null);

		
		// Set up the local environment's arguments, passing the enclosing instance if there is one.
		ArgumentList functionArgs = new ArgumentList();

		// Put the enclosing instance in the argument table.
		if(namespaceToInstantiate instanceof Function)
			functionArgs.enclosingEnvironment = environmentToEvaluateIn;
		else if(namespaceToInstantiate instanceof Type && ((Type)namespaceToInstantiate).getEnclosingType() != null)
			functionArgs.enclosingEnvironment = environmentToEvaluateIn;			
		
		// Evaluate the arguments using the current environment, putting each in the function instance's local environment.		

		if(namespaceToInstantiate instanceof Function && 
		   namespaceToInstantiate.getNumberOfDeclarations() != argExpressions.length().value)
			throw new ElementError("Incorrect number of arguments to function " + namespaceToInstantiate + 
					", declared in object " + environmentToEvaluateIn + 
					" of type " + environmentToEvaluateIn.getType(), null);
		
		if(!argExpressions.isEmpty().value && argExpressions.first().peek(Arg.param).isEmpty().value) {
		
			Iterator<Arg> argsIter = argExpressions.iterator();
			for(DecInterface<?> pd : namespaceToInstantiate.getDeclarationsToInstantiate()) {
				if(argsIter.hasNext()) {
					Arg arg = argsIter.next();
					boolean valueIsConstraint = arg.peek(Arg.valueIsConstraint).value;
					Element valueExpression = arg.peek(Arg.value);
					functionArgs.add(pd.getName(), valueIsConstraint, valueExpression == null ? new Nothing() : valueExpression.evaluate(env));
				}
				else throw new ElementError("" + namespaceToInstantiate.getName() + " expects " + namespaceToInstantiate.getDeclarationsToInstantiate() +
										  " but it only received received " + functionArgs,
											namespaceToInstantiate);
			}

		}
		// Keyword passing
		else {
			for(Arg arg : argExpressions)
				functionArgs.add(arg.peek(Arg.param), arg.peek(Arg.valueIsConstraint).value, arg.peek(Arg.value).evaluate(env));
		}

		// Create the local environment, passing the arguments.
		try {
			Element instance = namespaceToInstantiate.instantiate(functionArgs);
	
			// Convert null to a nothing.
			if(!instantiation && instance == null) return new Nothing();
			else return instance;
		} catch(StackOverflowError err) {
			
			throw new ElementError("Stack overflow! " + err, null);
			
		}

	}	

	// This is used when we have an object context, and the arguments already, but not the namespace
	public static Element evalFunction(Element object, String function, Element ... arguments) {

		Function fun = object.getType().getFunctionNamed(new Text(function), Bool.FALSE);
		if(fun == null) throw new ElementError("Couldn't find function named " + function + " from " + object, null);
		return evalFunction(object, fun, arguments);
		
	}
		
	public static Element evalFunction(Element object, Function fun, Element ... arguments) {
		

		ArgumentList functionArgs = new ArgumentList();
		functionArgs.enclosingEnvironment = object;

		Iterator<DecInterface> decIter = fun.getDeclarationsToInstantiate().iterator();
		for(Element arg : arguments) {
			if(decIter.hasNext()) {
				DecInterface dec = decIter.next();
				functionArgs.add(dec.getName(), false, arg);
			}
			else throw new ElementError("" + fun.getName() + " expects " + fun.getDeclarationsToInstantiate() +
									  " but it only received received " + functionArgs,
									  fun);
		}
		
		return fun.instantiate(functionArgs);
		
	}
	
	public static Element handlePropertyFunction(Element env, Property property, String propertyFunction, List<Arg> argExpressions) {
		
		if(propertyFunction.equals("set")) {
			Element value = argExpressions.first().peek(Arg.value).evaluate(env);

			Transition t = null;
			if(argExpressions.length().value > 1) {
				Element potentialTransition = argExpressions.nth(new Int(2)).peek(Arg.value).evaluate(env);
				if(potentialTransition instanceof Transition) t = (Transition)potentialTransition;
				else throw new ElementError("Passed non-transition as transition to set: " + potentialTransition, null);
			}
			return new Bool(property.set(value, t));
		}
		else if(propertyFunction.equals("name"))
			return property.getDeclaration().get(Dec.name);
		else if(propertyFunction.equals("get"))
			return property.get();
		else if(propertyFunction.equals("touch")) {
			property.markOutOfDate(null);
			return null;
		}
		else if(propertyFunction.equals("outOfDate"))
			return Bool.valueOf(property.isOutOfDate());
		else if(propertyFunction.equals("function"))
			return property.getValueFunction();
		else if(propertyFunction.equals("outgoing"))
			return new Text(property.outgoingEdgesToString());
		else if(propertyFunction.equals("incoming"))
			return new Text(property.incomingEdgesToString());
		else if(propertyFunction.equals("owner"))
			return property.getElementOwner();
		else if(propertyFunction.equals("toView"))
			return edu.cmu.hcii.citrus.views.Translator.toView(property);
		else if(propertyFunction.equals("firstView"))
			return property.getFirstView();
		else if(propertyFunction.equals("isValid"))
			return new Bool(property.isValid());
		else if(propertyFunction.equals("is"))
			return property.is(argExpressions.first().peek(Arg.value).evaluate(env));
		else if(propertyFunction.equals("declaration"))
			return property.getDeclaration();
		else if(propertyFunction.equals("isReference"))
			return new Bool(property.getDeclaration().isReferenceOnly());
		else if(propertyFunction.equals("requestOwnership"))
			return property.requestOwnership();
		else throw new ElementError("Properties don't have a function named " + propertyFunction, null);

	}
	
	public static Element handleIntFunction(Element env, Int i, String fun, List<Arg> argExpressions) {
		
		if(fun.equals("greaterThan") || fun.equals(">"))
			return new Bool(i.value > ((Int)argExpressions.first().value().evaluate(env)).value);
		else if(fun.equals("greaterThanOrEqualTo") || fun.equals(">="))
			return new Bool(i.value >= ((Int)argExpressions.first().value().evaluate(env)).value);
		else if(fun.equals("lessThan") || fun.equals("<"))
			return new Bool(i.value < ((Int)argExpressions.first().value().evaluate(env)).value);
		else if(fun.equals("lessThanOrEqualTo") || fun.equals("<="))
			return new Bool(i.value <= ((Int)argExpressions.first().value().evaluate(env)).value);
		else if(fun.equals("+")) {
			Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
			return new Int(i.value + ((Int)arg2).value);
		}
		else if(fun.equals("-")) {
			Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
			return new Int(i.value - ((Int)arg2).value);
		}
		else if(fun.equals("*")) {
			Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
			return new Int(i.value * ((Int)arg2).value);
		}
		else if(fun.equals("/")) {
			Element arg2 = argExpressions.first().peek(Arg.value).evaluate(env);
			return new Int(i.value / ((Int)arg2).value);
		}
		else if(fun.equals("toText")) return i.toText();

		return null;
		
	}

	public Context contextFor(Element e) {
		
		if(peek(functionContext) == e) return get(context);
		else if(peek(function) == e) return peek(functionContext).resultingType();
		else return get(context);
		
	}
	
	// The resulting type of the function with the given name in the resulting type of the function context
	public Type resultingType() { 
//		System.err.println("Note, evaluate.resultingType is broken");
		return null;
//		return peek(functionContext).resultingType().getTypeNamed(peek(function).value).resultingType(); 
		
	}

	
	public String toString() {
		
		String str = "( ";
		
		str = str + get(functionContext) + " ";
		if(get(function) != null) str = str + get(function) + " ";
		for(Arg arg : get(arguments))
			str = str + arg.toString() + " ";
		str = str + ")";
		return str;
		
	}
	
}