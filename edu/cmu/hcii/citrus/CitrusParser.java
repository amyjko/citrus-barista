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

import java.io.File;
import java.util.Vector;

public class CitrusParser {

	public static final String DO_KEYWORD = "do";
	public static final String HAS_KEYWORD = "has";
	public static final String REFS_KEYWORD = "refs";
	public static final String WHEN_KEYWORD = "when";
	public static final String RULE_KEYWORD = "rule";
	public static final String FUNCTION_KEYWORD = "fun";
	
	public static final char LIST_LEFT = '[';
	public static final char LIST_RIGHT = ']';
	public static final char FUNCTION_LEFT = '(';
	public static final char FUNCTION_RIGHT = ')';
	public static final char SET_LEFT = '{';
	public static final char SET_RIGHT = '}';
	public static final char TYPE_VARIABLE_LEFT = '<';
	public static final char TYPE_VARIABLE_RIGHT = '>';
	public static final char NAMESPACE = '.';
	public static final char PEEK = ':';
	public static final char ENDOFTYPE = '.';
	public static final char STRING_LEFT = '\"';
	public static final char STRING_RIGHT = '\"';
	public static final char COMMENT = '#';
	public static final char PROPERTY = '@';
	public static final char QUOTE = '\'';
	public static final char CHARACTER = '`';
	public static final char DEFAULT = '=';
	public static final String CONSTRAINT = "<-";
	public static final char NEGATION = '-';
	public static final char PARAMETER = '?';

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	private static enum TokenType {
		LIST_LEFT, LIST_RIGHT, 
		SET_LEFT, SET_RIGHT, 
		FUNCTION_LEFT, FUNCTION_RIGHT, 
		TYPE_VARIABLE_LEFT, TYPE_VARIABLE_RIGHT,
		NAMESPACE, 
		PEEK,
		STRING, 
		CHARACTER,
		NAME, 
		NUMBER, 
		BOOLEAN,
		QUOTE,
		PROPERTY,
		ARGUMENT,
		PARAMETER,
		ENDOFTYPE
	}

	private static class Token {
		
		public final String token;
		public final TokenType type;
		public final int line;
		
		public Token(String newToken, TokenType newType, int line) { 
			token = newToken; 
			type = newType; 
			this.line = line;
		}
		
		public String toString() { return token; }
		
	}

	public static class TokenList {
		
		private int currentToken = 0;
		private Vector<Token> tokens = new Vector<Token>();

		public int currentLine() { return tokens.get(currentToken).line; }
		public Vector<Token> getTokens() { return tokens; }
		public String eatAnyType() {
			return tokens.elementAt(currentToken++).token;			
		}
		public String eat(TokenType typeExpected) {
			
			if(tokens.elementAt(currentToken).type == typeExpected)
				return tokens.elementAt(currentToken++).token;
			else throw new ElementError("Expected token type " + typeExpected + " but I found " + tokens.elementAt(currentToken).type + context(this), null);

		}
		
		public boolean isType(TokenType type) { 

			if(currentToken < tokens.size())
				return tokens.elementAt(currentToken).type == type; 
			else throw new ElementError("Unexpected end of input: expected a token of type " + type + "\n" + context(this), null);
			
		}
		
		public void add(String token, TokenType type, int line) {
			
			tokens.add(new Token(token, type, line));

		}
		
		public boolean hasNext() { return currentToken < tokens.size(); }
		public boolean hasMoreThanOneLeft() { return currentToken + 1 < tokens.size(); }

		public int currentIndex() { return currentToken; }
		
		public Token tokenAtIndex(int i) { return tokens.elementAt(i); }

		public String currentToken() {
			if(currentToken < tokens.size()) return tokens.elementAt(currentToken).token; 
			else throw new ElementError("Unexpected end of input" + context(this), null);
		}
		public TokenType currentType() { return tokens.elementAt(currentToken).type; }
		
		public void reset() { currentToken = 0; }

		public String toString() { 
			
			String list = "";
			for(Token token : tokens)
				list = list + token.token + ", ";
			return list.substring(0, list.length() - 2);

		}
		
	}
	
	private static String context(TokenList tokens) {
		
		String context = "\n\n";
		for(int i = Math.max(0, tokens.currentIndex() - 19); i <= Math.min(tokens.getTokens().size() - 1, tokens.currentIndex()); i++)
			context = context + tokens.getTokens().elementAt(i) + 
				((tokens.getTokens().elementAt(i).line != tokens.getTokens().elementAt(i+1).line) ? 
					"\n" : " ");
		context = context + "<<<<<\nLine " + tokens.currentLine() + "\n";
		return context;
		
	}
	
	public static TokenList tokenize(String code) { return tokenize(code.toCharArray()); }
	public static TokenList tokenize(char[] characters) {

		int first = 0, last = 1;
		int line = 1;
		TokenList tokens = new TokenList();

		while(first < characters.length && characters[first] > 0) {

			TokenType type = null;
			
			// Read whitespace and comments until we don't see whitespace or comments.
			while(first < characters.length && characters[first] > 0 &&
				(Character.isWhitespace(characters[first]) || characters[first] == COMMENT)) {

				if(Character.isWhitespace(characters[first]))
					while(first < characters.length && 
							characters[first] > 0 &&
							Character.isWhitespace(characters[first])) { 
						if(characters[first] == '\n') line++;
						first++; last++;
					}
				
				if(first < characters.length && characters[first] == COMMENT) 
					do { first++; last++; } 
					while(	first < characters.length && characters[first] > 0 &&
							characters[first] != '\n' && 
							characters[first] != '\r');
				
			}
			
			if(first >= characters.length || characters[first] == 0) break;
			
			if(characters[first] == STRING_LEFT) {
				type = TokenType.STRING;
				while(last < characters.length && characters[last] > 0 && characters[last] != '\"') {
					if(characters[last] == '\\') last++;
					last++;
				}
				last++;
			}
			else if(characters[first] == CHARACTER) {
				type = TokenType.CHARACTER;
				while(last < characters.length && characters[last] > 0 && characters[last] != '`') {
					if(characters[last] == '\\') last++;
					last++;
				}
				last++;
			}
			// Numerical tokens
			else if(Character.isDigit(characters[first]) ||
					(characters[first] == NEGATION && Character.isDigit(characters[first+1])) ||
					(characters[first] == '.' && first + 1 < characters.length && Character.isDigit(characters[first + 1]))) {
				type = TokenType.NUMBER;
				while(last < characters.length && (Character.isDigit(characters[last]) || characters[last] == '.')) 
					last++;
			}
			else if(characters[first] == FUNCTION_LEFT) type = TokenType.FUNCTION_LEFT;
			else if(characters[first] == FUNCTION_RIGHT) type = TokenType.FUNCTION_RIGHT;
			else if(characters[first] == LIST_LEFT) type = TokenType.LIST_LEFT;
			else if(characters[first] == LIST_RIGHT) type = TokenType.LIST_RIGHT;
			else if(characters[first] == SET_LEFT) type = TokenType.SET_LEFT;
			else if(characters[first] == SET_RIGHT) type = TokenType.SET_RIGHT;
				else if(characters[first] == CONSTRAINT.charAt(0) && characters[first + 1] == CONSTRAINT.charAt(1)) {
				type = TokenType.ARGUMENT;
				last++;
			}
			// Check >= and <= before checking for type variables.
			else if((characters[first] == TYPE_VARIABLE_LEFT || characters[first] == TYPE_VARIABLE_RIGHT) && 
					characters[first+1] == '=') {
				type = TokenType.NAME;
				last++;
			}
			else if(characters[first] == TYPE_VARIABLE_LEFT) type = TokenType.TYPE_VARIABLE_LEFT;
			else if(characters[first] == TYPE_VARIABLE_RIGHT) type = TokenType.TYPE_VARIABLE_RIGHT;
			// NAMESPACE and ENDOFTYPE are identical.
			else if(characters[first] == NAMESPACE && (int)characters[first + 1] > 0 && !Character.isWhitespace(characters[first + 1]))
				type = TokenType.NAMESPACE;
			else if(characters[first] == ENDOFTYPE) type = TokenType.ENDOFTYPE;
			else if(characters[first] == PROPERTY) type = TokenType.PROPERTY;
			else if(characters[first] == QUOTE) type = TokenType.QUOTE;
			else if(characters[first] == DEFAULT) type = TokenType.ARGUMENT;
			else if(characters[first] == PARAMETER) type = TokenType.PARAMETER;
			else {
				while(last < characters.length && Character.isLetterOrDigit(characters[last])) 
					last++;
				String token = new String(characters, first, last - first);
				if(token.equals("true") || token.equals("false")) type = TokenType.BOOLEAN;
				else type = TokenType.NAME;	
			}
			
			String token = new String(characters, first, last - first);
			tokens.add(token, type, line);
			first = last;
			last = first + 1;
			
		}
		
		return tokens;
		
	}

	public static Element parse(String code) { return code(tokenize(code)); }
	
	public static Unit unit(Text path) { 

		char[] text = XMLParser.readCharactersFrom(path); 
		if(text == null) return null;
		else return unit(path.value, text);

	}
	
	public static Unit unit(File file) {

		char[] text = XMLParser.readCharactersFrom(file); 

		System.err.println("");
		for(int i = 0; i < levels; i++)
			System.err.print("  ");
		System.err.print("Loading " + file.getParentFile().getName() + " -> " + file.getName() + " ");
		levels++;
		
		Unit newUnit = null;
		if(text != null)
			newUnit = unit(file.getName(), text);

		levels--;
		return newUnit;
		
	}
	public static Unit unit(String filename, char[] text) {

		TokenList tokens = CitrusParser.tokenize(text);
		Unit l = CitrusParser.unit(filename, tokens);
		return l;
		
	}

	private static int levels = 1;
	public static Unit unit(String name, TokenList tokens) {

		// Eat "language"
		tokens.eat(TokenType.NAME);
		
		// The language name is the first argument
		String language = tokens.eat(TokenType.NAME);

		Unit newUnit = new Unit(name, language);
		Language unitsLanguage = Universe.getLanguage(language);
		// If we didn't find a language, create one.
		if(unitsLanguage == null) unitsLanguage = Universe.makeLanguage(language);
		unitsLanguage.include(newUnit);

		// Read "uses" lines until there are no more
		while(tokens.hasNext() && tokens.currentToken().equals("uses")) {

			// Eat "uses"
			tokens.eat(TokenType.NAME);
			// Eat the language name
			String languageName = tokens.eat(TokenType.STRING);
			languageName = languageName.substring(1, languageName.length() - 1);
			Language languageUsed = Universe.getLanguage(languageName);
			if(languageUsed == null)
				throw new ElementError("Couldn't find a language named " + languageName, null);
			else
				newUnit.usesLanguage(languageUsed);
			
		}

		// Parse the init if there is one.
		Expression<?> init = null;
		if(tokens.hasNext() && tokens.currentToken().equals("init")) {
			tokens.eat(TokenType.NAME);
			init = (Expression)code(tokens);
			newUnit.set(Unit.init, init);
		}

		// Keep reading until the end of input
		while(tokens.hasNext()) {
//			System.err.print(".");
			if(tokens.currentToken().equalsIgnoreCase("a") || tokens.currentToken().equalsIgnoreCase("an")) 
				newUnit.get(Unit.types).append(type(newUnit, tokens));
			else throw new ElementError("When reading a language, I found token " + 
					"\"" + tokens.currentToken() + "\"; I don't know how to read anything but types in a language. " + context(tokens), null);
		
		}
		
		return newUnit;

	}
	
	public static Type type(Unit newLanguage, TokenList tokens) {

		// Eat the "a"
		tokens.eat(TokenType.NAME);
		
		// Eat the name of the type
		Text typeName = new Text(tokens.eat(TokenType.NAME));
		
		// Eat "is a"
		tokens.eat(TokenType.NAME);
		tokens.eat(TokenType.NAME);

		// Eat "abstract"
		Bool isConcrete = Bool.TRUE;
		if(tokens.currentToken().equals("abstract")) {
			tokens.eat(TokenType.NAME);
			isConcrete = Bool.FALSE;
		}

		// Eat the prototype type expression
		TypeExpression prototype = typeExpression(tokens);

		// Eat "that"
		tokens.eat(TokenType.NAME);

		// Should we be making a regular type or a java type? We have to base this
		// decision on the prototype.
		BaseType newType = new BaseType();
		newType.set(BaseType.name, typeName);
		newType.set(BaseType.prototype, prototype);
		newType.set(BaseType.concrete, isConcrete);
		
		while(tokens.hasNext() && tokens.currentType() != TokenType.ENDOFTYPE) {
		
			if(tokens.currentToken().equals(HAS_KEYWORD) || 
			   tokens.currentToken().equals(REFS_KEYWORD) ||
			   tokens.currentToken().equals(RULE_KEYWORD)) {
				
				Dec dec = tokens.currentToken().equals(RULE_KEYWORD) ? rule(tokens) : has(tokens);
				newType.declareProperty(dec);
				
			}			
			else if(tokens.currentToken().startsWith("a"))
				newType.get(BaseType.types).append(type(newLanguage, tokens)); 		
	
			else if(tokens.currentToken().startsWith("fun"))
				newType.declareFunction(function(tokens)); 		
	
			else if(tokens.currentToken().startsWith(WHEN_KEYWORD))
				newType.get(BaseType.handlers).append(when(tokens)); 		
			else throw new ElementError("The only legal tokens in a type declaration are has, refs, a, fun, when, rule" + context(tokens), null);
			
		}
		
		if(!tokens.hasNext()) throw new ElementError("Ran out of tokens, but " + newType + " wasn't closed." + context(tokens), null);
		
		// Eat the dot
		tokens.eat(TokenType.ENDOFTYPE);
		
		return newType;
		
	}
	
	public static Dec has(TokenList tokens) {

		Dec<?> newDeclaration = new Dec();
		
		// Eat "has", "refs", or "rule"
		String tok = tokens.eat(TokenType.NAME);
		if(tok.equals(HAS_KEYWORD)) newDeclaration.set(Dec.isReference, new Bool(false));
		else if(tok.equals(REFS_KEYWORD)) newDeclaration.set(Dec.isReference, new Bool(true));
		else throw new ElementError("Only \"" + HAS_KEYWORD + "\" or \"" + REFS_KEYWORD + "\" are allowed here." + context(tokens), null);

		while(tokens.currentToken().equals("undoable") ||
				tokens.currentToken().equals("static") ||
				tokens.currentToken().equals("constant")) {
			if(tokens.currentToken().equals("undoable")) {
				tokens.eat(TokenType.NAME);
				newDeclaration.set(Dec.isUndoable, Bool.TRUE);
			}
			else if(tokens.currentToken().equals("static")) {
				tokens.eat(TokenType.NAME);
				newDeclaration.set(Dec.isStatic, Bool.TRUE);
			}
			else if(tokens.currentToken().equals("constant")) {
				tokens.eat(TokenType.NAME);
				newDeclaration.set(Dec.isConstant, Bool.TRUE);
			}
		}
		
		// Eat the type of the declaration if its not a rule
		newDeclaration.set(Dec.typeExpression, typeExpression(tokens));

		// Eat the name
		newDeclaration.set(Dec.name, new Text(tokens.eat(TokenType.NAME)));

		// Is there initialization expression?
		if(tokens.currentType() == TokenType.ARGUMENT) {
		
			String operator = tokens.eat(TokenType.ARGUMENT);
			if(operator.equals("" + DEFAULT))
				newDeclaration.set(Dec.functionIsConstraint, new Bool(false));
			else if(operator.equals(CONSTRAINT))
				newDeclaration.set(Dec.functionIsConstraint, new Bool(true));
			else throw new ElementError("Declarations can only be assigned using = or <-", null);
			
			// Is it code or a parameter?
			if(tokens.currentToken().equals("" + PARAMETER)) {
				tokens.eat(TokenType.PARAMETER);
				newDeclaration.set(Dec.valueExpression, new Parameter());
			}
			// Eat the value function
			else newDeclaration.set(Dec.valueExpression, code(tokens));
		
		}
		
		// Are there value restrictions? Eat 'em up.
		while(tokens.currentToken().equals("for")) {

			PropertyRestriction newRestriction = new PropertyRestriction();

			// Eat "for which"
			tokens.eat(TokenType.NAME);
			tokens.eat(TokenType.NAME);

			// Eat the condition
			newRestriction.set(PropertyRestriction.condition, (Expression)code(tokens));
			
			// Is there an otherwise?
			if(tokens.currentToken().equals("otherwise")) {
				
				// Eat "otherwise"
				tokens.eat(TokenType.NAME);
				
				// Eat the correction
				newRestriction.set(PropertyRestriction.correction, (Expression)code(tokens));
				
			}
			
			newDeclaration.restrictions.add(newRestriction);
			
		}
		
		return newDeclaration;		
		
	}
	
	public static Dec rule(TokenList tokens) {

		Dec newDeclaration = new Dec();
		
		// Eat "has", "refs", or "rule"
		String tok = tokens.eat(TokenType.NAME);

		// Eat the name
		newDeclaration.set(Dec.name, new Text(tokens.eat(TokenType.NAME)));

		// Eat the type of the declaration if its not a rule
		newDeclaration.set(Dec.typeExpression, new BaseTypeExpression(Boot.BOOL));

		newDeclaration.set(Dec.isRule, new Bool(true));
		newDeclaration.set(Dec.isReference, new Bool(false));
		newDeclaration.set(Dec.functionIsConstraint, new Bool(true));
		newDeclaration.set(Dec.valueExpression, (Expression)code(tokens));

		// This value must be true.
		PropertyRestriction newRestriction = new PropertyRestriction();
		newRestriction.set(PropertyRestriction.condition, new Ref(newDeclaration.text(Dec.name)));
		
		// Is there an otherwise? If so, read it.
		if(tokens.currentToken().equals("otherwise")) {
			
			// Eat "otherwise"
			tokens.eat(TokenType.NAME);
			
			// Eat the correction
			newRestriction.set(PropertyRestriction.correction, (Expression)code(tokens));
			
		}
		
		newDeclaration.restrictions.add(newRestriction);
		
		return newDeclaration;
		
	}

	public static BaseFunction function(TokenList tokens) {

		BaseFunction newFunction = new BaseFunction();

		// Eat "fun"
		tokens.eat(TokenType.NAME);

		if(tokens.currentToken().equals("static")) {
			tokens.eat(TokenType.NAME);
			newFunction.set(BaseFunction.isStatic, Bool.TRUE);
		}
		
		// Eat the type
		newFunction.set(BaseFunction.returnType, typeExpression(tokens));

		// Eat the name of the function
		newFunction.set(BaseFunction.name, new Text(tokens.eat(TokenType.NAME)));

		tokens.eat(TokenType.LIST_LEFT);
		
		while(tokens.currentToken().equals(HAS_KEYWORD) || tokens.currentToken().equals(REFS_KEYWORD))
			newFunction.get(BaseFunction.arguments).append(has(tokens));

		tokens.eat(TokenType.LIST_RIGHT);

		// Read the expression
		newFunction.set(BaseFunction.expression, code(tokens));
		
		return newFunction;
		
	}
	
	public static When when(TokenList tokens) {

		// Eat "when"
		tokens.eat(TokenType.NAME);
		
		// Eat the event name
		Text newName = new Text(tokens.eat(TokenType.NAME));
		When newWhen = new When(newName);

		tokens.eat(TokenType.FUNCTION_LEFT);

		// Eat the subject expression
		newWhen.set(When.subjectExpression, (Expression)code(tokens));

		// Read the event expression
		newWhen.set(When.eventExpression, (Expression)code(tokens));
		
		// Read the arguments until we reach the end of the list
		while(tokens.currentType() != TokenType.FUNCTION_RIGHT)
			newWhen.get(When.arguments).append(arg(tokens));

		tokens.eat(TokenType.FUNCTION_RIGHT);
		
		// Read the expression
		newWhen.set(When.response, (Expression)code(tokens));
		
		return newWhen;
		
	}
	
	// Translates an input string to an element tree.
	//
	// CODE 	::=	 		FUNCTION | STRING | INTEGER | DOUBLE | BOOLEAN | LIST | REF | FUNCTION.REF | QUOTE 	=> Evaluate
	public static Element code(String text) { return code(tokenize(text)); }
	public static Element code(TokenList tokens) {

		if(tokens.isType(TokenType.FUNCTION_LEFT)) return functionApplication(tokens);
		else if(tokens.currentToken().equals("has") || tokens.currentToken().equals("refs")) return has(tokens);
		else if(tokens.isType(TokenType.SET_LEFT)) return set(tokens);
		else if(tokens.isType(TokenType.LIST_LEFT)) return list(tokens);
		else if(tokens.isType(TokenType.STRING)) return string(tokens);
		else if(tokens.isType(TokenType.NUMBER)) return number(tokens);
		else if(tokens.isType(TokenType.BOOLEAN)) return bool(tokens);
		else if(tokens.isType(TokenType.QUOTE)) return quote(tokens);
		else if(tokens.isType(TokenType.CHARACTER)) return character(tokens);
		else if(tokens.isType(TokenType.PROPERTY)) return ref(tokens);
		else {

			if(tokens.hasMoreThanOneLeft() && tokens.tokenAtIndex(tokens.currentIndex() + 1).type == TokenType.TYPE_VARIABLE_LEFT)
				return typeExpression(tokens);
			else return ref(tokens);
		
		}

	}
	
	// ARG		::=		NAME [ (=|<-) EXPRESSION ]
	public static Arg arg(TokenList tokens) {
		
		// Lookahead for parameter name.
		String parameter = "";
		boolean valueIsConstraint = false;
		if(tokens.isType(TokenType.NAME) &&
			tokens.tokenAtIndex(tokens.currentIndex() + 1).type == TokenType.ARGUMENT) {

			// Eat the property name
			parameter = tokens.eat(TokenType.NAME);
			
			// Is the argument a constraint or value?
			String operator = tokens.eat(TokenType.ARGUMENT);
			if(operator.equals("" + DEFAULT)) valueIsConstraint = false;
			else if(operator.equals(CONSTRAINT)) valueIsConstraint = true;
			else throw new ElementError("Arguments can only be passed using = or <-", null);
			
		}

		return new Arg(parameter, valueIsConstraint, code(tokens));
		
	}
	
	// FUNCTION 	::=		( CODE name { [name:]CODE } )
	public static Expression functionApplication(TokenList tokens) {

		tokens.eat(TokenType.FUNCTION_LEFT);

		String specialForm = tokens.currentToken();

		if(specialForm.equals(DO_KEYWORD)) return (Expression)doStatement(tokens); 
		
		Evaluate<?> evaluate = new Evaluate();
		if(Evaluate.isSpecialForm(specialForm)) {
			evaluate.set(Evaluate.functionContext, code(tokens));
			evaluate.set(Evaluate.function, null);
		}
		else {
			evaluate.set(Evaluate.functionContext, code(tokens));
			evaluate.set(Evaluate.function, code(tokens));
		}
		
		while(!tokens.isType(TokenType.FUNCTION_RIGHT)) {

			// Append the argument
			evaluate.get(Evaluate.arguments).append(arg(tokens));
			
		}
		tokens.eat(TokenType.FUNCTION_RIGHT);		

		if(specialForm.equals("let")) {

			if(evaluate.get(Evaluate.arguments).length().value > 2)
				throw new ElementError("Lets can only contain one expression, not a list: " + evaluate.get(Evaluate.arguments) + "\n" + context(tokens), null);
			Let newLet = new Let();
			List funArgs = (List)evaluate.get(Evaluate.arguments).first().peek(Arg.value).evaluate(null);
			Element expr = evaluate.get(Evaluate.arguments).second().peek(Arg.value);
			newLet.set(Let.name, new Text("let"));
			newLet.set(Let.arguments, funArgs);
			newLet.set(BaseFunction.expression, expr);
			evaluate.set(Evaluate.function, newLet);
			
		}
		else if(specialForm.equals("fun")) {

			BaseFunction newFunction = new BaseFunction();
			List funArgs = (List)evaluate.get(Evaluate.arguments).first().peek(Arg.value).evaluate(null);
			Element expr = evaluate.get(Evaluate.arguments).second().peek(Arg.value);
			newFunction.set(BaseFunction.name, new Text("anonymous-function"));
			newFunction.set(BaseFunction.arguments, funArgs);
			newFunction.set(BaseFunction.expression, expr);
			evaluate.set(Evaluate.function, newFunction);
			
		}
		
		if(tokens.hasNext() && 
			(tokens.isType(TokenType.NAMESPACE) || tokens.isType(TokenType.PEEK))) {
			Bool peek = Bool.FALSE;
			if(tokens.isType(TokenType.NAMESPACE))
				tokens.eat(TokenType.NAMESPACE);
			else {
				tokens.eat(TokenType.PEEK);
				peek = Bool.TRUE;
			}
			return new Possessive(evaluate, ref(tokens), peek);			
		} else return evaluate;
		
	}
	
	// DO		::=		( do { FUNCTION } )
	public static Do doStatement(TokenList tokens) {

		tokens.eat(TokenType.NAME);
		Do newDo = new Do();
		// Eat all of the expressions.
		while(!tokens.isType(TokenType.FUNCTION_RIGHT)) {

			// Append the argument
			newDo.get(Do.expressions).append((Expression)code(tokens));
			
		}
		tokens.eat(TokenType.FUNCTION_RIGHT);		
		return newDo;
		
	}
	
	// LIST 		::=		[ FUNCTION { FUNCTION } ]		=>	NewList
	public static NewList<?> list(TokenList tokens) {
		
		tokens.eat(TokenType.LIST_LEFT);
		NewList<?> newList = new NewList();
		while(!tokens.isType(TokenType.LIST_RIGHT)) {
			Expression result = (Expression)code(tokens);
			newList.get(NewList.items).append(result);
		}
		tokens.eat(TokenType.LIST_RIGHT);
		return newList;
		
	}

	// SET		::=		{ FUNCTION { FUNCTION } }		=>	NewSet
	public static NewSet<?> set(TokenList tokens) {
		
		tokens.eat(TokenType.SET_LEFT);
		NewSet<?> newSet = new NewSet();
		while(!tokens.isType(TokenType.SET_RIGHT))
			newSet.get(NewSet.items).append(code(tokens));
		tokens.eat(TokenType.SET_RIGHT);
		return newSet;
		
	}

	// STRING 	::=	 	"{unicode}"					=> 	StringConstant
	public static Expression string(TokenList tokens) { 
		String token = tokens.eat(TokenType.STRING);
		// Eat the quotes
		token = token.substring(1, token.length() - 1);
		return new TextLiteral(token); 
	}
	
	// BOOLEAN 	::= 		false | true					=>	BooleanConstant
	public static Expression bool(TokenList tokens) { return new BoolLiteral(tokens.eat(TokenType.BOOLEAN)); }

	// INTEGER 	::= 		decimal{decimal}				=>	IntegerConstant
	// DOUBLE 	::= 		decimal{decimal}[.{decimal}]	=>	DoubleConstant
	public static Expression number(TokenList tokens) {

		if(tokens.currentToken().indexOf('.') < 0) 
			return new IntLiteral(tokens.eat(TokenType.NUMBER));
		else return new RealLiteral(tokens.eat(TokenType.NUMBER));
		
	}

	// QUOTE		::= 		'CODE						=>	Quote
	public static Expression quote(TokenList tokens) {
		
		tokens.eat(TokenType.QUOTE);
		return new Quote(code(tokens));
		
	}
	
	// CHARACTER ::= 		`char						=>	Char
	public static Expression character(TokenList tokens) {
		
		String token = tokens.eat(TokenType.CHARACTER);
		// Eat the quotes
		token = token.substring(1, token.length() - 1);

		// Parse the character string
		return new CharLiteral(token);
		
	}

	// REF		::=		[~]{unicode} | REF.REF			=>	Ref
	public static Expression ref(TokenList tokens) {

		Expression ref = null;
		if(tokens.isType(TokenType.PROPERTY)) {

			tokens.eat(TokenType.PROPERTY);
			ref = new PropertyRef(tokens.eatAnyType());
			if(tokens.hasNext() && tokens.isType(TokenType.NAMESPACE))
				throw new ElementError("Can't . a property", null);
			return ref;
		} else {
			ref = new Ref(tokens.eatAnyType());
			while(tokens.hasNext() && 
					(tokens.isType(TokenType.NAMESPACE) || tokens.isType(TokenType.PEEK))) {
				Bool peek = Bool.FALSE;
				if(tokens.isType(TokenType.NAMESPACE))
					tokens.eat(TokenType.NAMESPACE);
				else {
					tokens.eat(TokenType.PEEK);
					peek = Bool.TRUE;
				}
				ref = new Possessive(ref, ref(tokens), peek);
			}
			return ref;
		}
		
		
	}

	// TYPE_EXPRESSION 	::=	 REF | REF<{TYPE_EXPRESSION}>			=> 	(TYPE_EXPRESSION
	public static TypeExpression typeExpression(TokenList tokens) { 
		
		BaseTypeExpression newTypeExpression = new BaseTypeExpression();

		// Set the type name to look for
		newTypeExpression.set(BaseTypeExpression.name, new Text(tokens.eat(TokenType.NAME)));

		// Append the type variables
		if(tokens.isType(TokenType.TYPE_VARIABLE_LEFT)) {

			tokens.eat(TokenType.TYPE_VARIABLE_LEFT);
			while(!tokens.isType(TokenType.TYPE_VARIABLE_RIGHT))
				newTypeExpression.get(BaseTypeExpression.arguments).append(typeExpression(tokens));
			tokens.eat(TokenType.TYPE_VARIABLE_RIGHT);
			
		}
		
		return newTypeExpression;
		
	}

}
