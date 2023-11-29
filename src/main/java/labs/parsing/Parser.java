package labs.parsing;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * The parser class for calculation the mathematical expression
 * @see Parser.Token
 * @see #calculate(String, String)
 */
public class Parser {
	/**
	 * Represents token (a.k.a. indivisible part of the expression)
	 * 
	 * Has enumeration Token.Type for representing which type of the tokens given
	 */
	public static class Token {
		/**
		 * Type for classification of the tokens
		 */
		public enum Type {
			/// Represents symbol '('
			LEFT_PARENTHESIS,
			/// Represents symbol ')'
			RIGHT_PARENTHESIS,
			/// Represents symbol '+'
			PLUS,
			/// Represents symbol '-'
			MINUS,
			/// Represents symbol '*'
			MULTIPLY,
			/// Represents symbol '/'
			DIVIDE,
			/// Represents number as multi-symbol string
			NUMBER,
			/// Last one for utility purposes
			/// TODO: нужен ли?
			LAST
		}

		public static Token.Type getType(char c) {
			switch (c) {
			case '(':
				return Type.LEFT_PARENTHESIS;
			case ')':
				return Type.RIGHT_PARENTHESIS;
			case '+':
				return Type.PLUS;
			case '-':
				return Type.MINUS;
			case '*':
				return Type.MULTIPLY;
			case '/':
				return Type.DIVIDE;
			default: // if nothing matches it's a number
				return Type.NUMBER;
			}
		}

		public static boolean isNumericalChar(char c) {
			return getType(c) == Type.NUMBER;
		}

		/// Type of the token
		private Token.Type type;
		/// Concrete value of the token
		private String value;

		public Token() {
			this.type = Type.LAST;
		}

		public Token(Token.Type type, String value) {
			this.value = value;
			this.type = type;
		}

		@Override
		public String toString() {
			return "Token with value " + value;
		}
	}

	/**
	 * Calculates the result of a mathematical expression
	 * 
	 * @param expression The expression itself
	 * @param variables  A string containing variables and their values
	 * @return The mathematical result
	 */
	public Integer calculate(String expression, String variables) {
		// splitting
		if (!variables.isEmpty()) {
			String[] splitted = variables.split(" ");
			for (int i = 0; i < splitted.length; i += 2) {
				expression = expression.replace(splitted[i], splitted[i + 1]);
			}
		}
		splitToTokens(expression);
		try {
			return calculateExpression();
		} catch (RuntimeException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	/**
	 * Splits the given expression to tokens
	 * 
	 * @param expression Expression itself
	 * 
	 * Initialize tokens and iterator variables 
	 * Warning: Must have been called before any operations!
	 * 
	 * @see Token
	 * @see #tokens
	 * @see #iterator
	 */
	private void splitToTokens(String expression) {
		for (int i = 0; i < expression.length(); ++i) {
			char c = expression.charAt(i);
			if (!Token.isNumericalChar(c)) {
				tokens.add(new Token(Token.getType(c), Character.toString(c)));
			} else {
				StringBuilder sb = new StringBuilder();
				do {
					sb.append(c);
					i++;
					if (i >= expression.length()) {
						break;
					}
					c = expression.charAt(i);
				} while (Token.isNumericalChar(c));
				tokens.add(new Token(Token.Type.NUMBER, sb.toString()));
			}
		}
		iterator = tokens.listIterator();
	}

	/**
	 * The main calculate function
	 * 
	 * @return The result of the expression
	 */
	private int calculateExpression() {
		// TODO: handle exception
		Parser.Token token = iterator.next();
		if (token.type == Parser.Token.Type.LAST) {
			return 0;
		} else {
			iterator.previous();
			return handlePlusMinusCase();
		}
	}

	/**
	 * Calculates subtraction and addition operations as operations with the lowest
	 * priority
	 *
	 * @return The result of addition and subtraction
	 */
	private int handlePlusMinusCase() {
		int value = handlerMultiplyDivideCase();
		while (true) {
			Parser.Token token = iterator.next();
			switch (token.type) {
			case PLUS:
				value += handlerMultiplyDivideCase();
				break;
			case MINUS:
				value -= handlerMultiplyDivideCase();
				break;
			case LAST: // $FALL_THROUGH$
			case RIGHT_PARENTHESIS:
				iterator.previous();
				return value;
			default:
				throw new RuntimeException(
						"Unexpected token: " + token.value + " at position: " + iterator.previousIndex());
			// previousIndex() is because we already got the token by calling
			// iterator.next()
			// and therefore we need the index of the given ("previous") element
			}
		}
	}

	/**
	 * Calculates multiplication and division operations as operations with higher
	 * priority
	 *
	 * @return The result of multiplication and division.
	 */
	private int handlerMultiplyDivideCase() {
		int value = handleElementary();
		while (true) {
			Parser.Token token = iterator.next();
			switch (token.type) {
			case MULTIPLY:
				value *= handleElementary();
				break;
			case DIVIDE:
				value /= handleElementary();
				break;
			case LAST: 				// $FALL_THROUGH$
			case RIGHT_PARENTHESIS: // $FALL_THROUGH$
			case PLUS: 				// $FALL_THROUGH$
			case MINUS:
				iterator.previous();
				return value;
			default:
				throw new RuntimeException(
						"Unexpected token: " + token.value + " at position: " + iterator.previousIndex());
			}
		}
	}

	/**
	 * Calculates elementary cases such as numbers itself and parentheses
	 *
	 * @return The result of the elementary case
	 */
	private int handleElementary() {
		Parser.Token token = iterator.next();
		switch (token.type) {
		case NUMBER:
			return Integer.parseInt(token.value);
		case LEFT_PARENTHESIS:
			int value = handlePlusMinusCase();
			token = iterator.next();
			if (token.type != Parser.Token.Type.LEFT_PARENTHESIS) {
				throw new RuntimeException(
						"Unexpected token: " + token.value + " at position: " + iterator.previousIndex());
			}
			return value;
		default:
			throw new RuntimeException(
					"Unexpected token: " + token.value + " at position: " + iterator.previousIndex());
			// previousIndex() is because we already got the token by calling
			// iterator.next()
			// and therefore we need the index of the given ("previous") element
		}
	}

	/**
	 * Iterator for going back and forth across the container @see {@link #tokens}
	 */
	private ListIterator<Token> iterator;
	/**
	 * Container of the tokens itself @see Token
	 * Warning: must be processed very carefully because it's sort of global object inside the class
	 */
	private LinkedList<Token> tokens;
}