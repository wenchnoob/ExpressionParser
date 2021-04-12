
import java.util.*;

public class Tester {

	public static void main(String[] args) throws InvalidExpressionException {
		AST ast = new AST("3**2+6*2-12");
		System.out.println(new Calculator().solve(ast));
	}
}

class AST {
	ASTNode root;
	String expression;
	
	public AST(String expression) throws InvalidExpressionException {
		this.expression = expression;
		init();
	}
	
	public ASTNode getRoot() {
		return root;
	}
	
	private void init() throws InvalidExpressionException {
		Lexer lex = new Lexer(expression);
		while(lex.hasMoreTokens()) {
			insert(lex.nextToken());
		}
	}
	
	private void insert(Token token) throws InvalidExpressionException {
		
		if (token.type() == Token.Type.EXPRESSION) {
			if (root == null) {
				ASTNode head = new ASTNode(new Token<String>("()", Token.Type.OPERATOR));
				String value = (String)token.value();
				AST temp = new AST(value.substring(1, value.length()-1));
				head.setRight(temp.getRoot());
				this.root = head;
			} else {
				ASTNode head = new ASTNode(new Token<String>("()", Token.Type.OPERATOR));
				String value = (String)token.value();
				AST temp = new AST(value.substring(1, value.length()-1));
				head.setRight(temp.getRoot());
				insertTo(root, head);
			}
		} else {
			if (root == null) {
				root = new ASTNode(token);
			} else {
				insertTo(root, new ASTNode(token));
			}
		}
	}
	
	private void insertTo(ASTNode insertionPoint, ASTNode toInsert) throws InvalidExpressionException {
	if (Calculator.checkPrecedence(insertionPoint.value()).compareTo(Calculator.checkPrecedence(toInsert.value())) > 0) {
			if (toInsert.left() == null) {
				toInsert.setLeft(insertionPoint);
				
				ASTNode oldParent = insertionPoint.parent();
				if (oldParent != null) {	
					if (oldParent.right() == insertionPoint) {
						oldParent.setRight(toInsert);
					} else {
						oldParent.setLeft(toInsert);
					}
				} else {
					this.root = toInsert;
					insertionPoint.setParent(toInsert);
				}
			} else if (toInsert.right() == null) {
				toInsert.setRight(insertionPoint);
				
				ASTNode oldParent = insertionPoint.parent();
				if (oldParent != null) {	
					if (oldParent.right() == insertionPoint) {
						oldParent.setRight(toInsert);
					} else {
						oldParent.setLeft(toInsert);
					}
				} else {
					this.root = toInsert;
					insertionPoint.setParent(toInsert);
				}
			} else {
				insertTo(insertionPoint.right(), toInsert);
			}
		} else {
			if (insertionPoint.left() == null) {
				insertionPoint.setLeft(toInsert);
				toInsert.setParent(insertionPoint);
			} else if (insertionPoint.right() == null) {
				insertionPoint.setRight(toInsert);
				toInsert.setParent(insertionPoint);
			} else {
				insertTo(insertionPoint.right(), toInsert);
			}
		}
	}
	
	public void  print() {
		printFrom(root);
		System.out.println();
	}
	
	private void printFrom(ASTNode from) {
		if (from == null) return;
		if(String.valueOf(from.value().value()).equals("()")) System.out.print("(");
		printFrom(from.left());
		if(!String.valueOf(from.value().value()).equals("()")) System.out.print(from);
		printFrom(from.right());
		if(String.valueOf(from.value().value()).equals("()")) System.out.print(")");
	}
}

class ASTNode {

	Token value;
	ASTNode parent;
	ASTNode left;
	ASTNode right;
	
	public ASTNode(Token value) {
		this.value = value;
	}
	
	public Token value() {
		return value;
	}
	
	public ASTNode left() {
		return left;
	}
	
	public ASTNode right() {
		return right;
	}
	
	public ASTNode parent() {
		return parent;
	}
	
	public void setValue(Token value) {
		this.value = value;
	}
	
	public void setLeft(ASTNode left) {
		this.left = left;
	}
	
	public void setRight(ASTNode right) {
		this.right = right;
	}
	
	public void setParent(ASTNode parent) {
		this.parent = parent;
	}
	
	public String toString() {
		return String.valueOf(value.value());
	}
}


class Calculator {
	private final static Set<Operator> supportedOperators = new HashSet<Operator>();
	private ArithmeticUnit au = ArithmeticUnit.getInstance();
	
	
	
	static {
		supportedOperators.add(new Operator("()", Operator.Precedence.NONE));
	
		supportedOperators.add(new Operator("+", Operator.Precedence.FIRST));
		supportedOperators.add(new Operator("-", Operator.Precedence.FIRST));
		supportedOperators.add(new Operator("*", Operator.Precedence.SECOND));
		supportedOperators.add(new Operator("/", Operator.Precedence.SECOND));
		supportedOperators.add(new Operator("**", Operator.Precedence.THIRD));
		
		supportedOperators.add(new Operator("+-", Operator.Precedence.FIRST));
		supportedOperators.add(new Operator("--", Operator.Precedence.FIRST));
		supportedOperators.add(new Operator("*-", Operator.Precedence.SECOND));
		supportedOperators.add(new Operator("/-", Operator.Precedence.SECOND));
		supportedOperators.add(new Operator("**-", Operator.Precedence.THIRD));
	}
	
	public static Operator.Precedence checkPrecedence(Token token) throws InvalidExpressionException {
		if (token.value().getClass() == Double.class) return Operator.Precedence.NONE; 
		for (Operator op: supportedOperators)
			if (op.value().equals((String)token.value())) return op.precedence();
		throw new InvalidExpressionException("There is an invalid operator in your expression!");
	}
	
	public static Operator getOperator(String opString) {
		for (Operator op: supportedOperators)
			if (op.value().equals(opString)) return op;
		return null;
	}
	
	public double solve(AST tree) {
		evalNode(tree.getRoot());
		return (Double)tree.getRoot().value().value();
	}
	
	public void evalNode (ASTNode node) {
		if (node == null || node.value().type() == Token.Type.NUMBER) return;
		
		evalNode(node.left());
		evalNode(node.right());
		
		if (((String)(node.value().value())).equals("()")) {
			if (node.left() != null) {
				node.setValue(node.left().value());
				node.setRight(null);
				node.setLeft(null);
				return;
			} else if (node.right() != null) {
				node.setValue(node.right().value());
				node.setRight(null);
				node.setLeft(null);
				return;
			}	
		} else {
			if (node.left() != null && node.right() != null) {
				Operator op = getOperator((String)(node.value().value()));
				Double ans = binaryEval(op, (Double)node.left().value().value(), (Double)node.right().value().value());
				node.setValue(new Token<Double>(ans, Token.Type.NUMBER));
			}
			
		}
		
	}
	
	private double binaryEval(Operator operator, Double oper1, Double oper2) {
		double ans = 0;
		switch(operator.value()) {
			case "+":
				ans = au.add(oper1, oper2);
				break;
			case "-":
				ans = au.subtract(oper1, oper2);
				break;
			case "*":
				ans = au.multiply(oper1, oper2);
				break;
			case "/":
				ans = au.divide(oper1, oper2);
				break;
			case "**":
				ans = au.exponentiate(oper1, oper2);
				break;
			
			case "+-":
				ans = au.add(oper1, -1*oper2);
				break;
			case "--":
				ans = au.subtract(oper1, -1*oper2);
				break;
			case "*-":
				ans = au.multiply(oper1, -1*oper2);
				break;
			case "/-":
				ans = au.divide(oper1, -1*oper2);
				break;
			case "**-":
				ans = au.exponentiate(oper1, -1*oper2);
				break;
		}
		return ans;
	}
	
	public double unaryEval(Operator operator, Double operand) {
		return operand;
	}
}

class ArithmeticUnit {
	
	private static ArithmeticUnit self;
	
	private ArithmeticUnit() {}
	
	public static ArithmeticUnit getInstance() {
		if (self == null) self = new ArithmeticUnit();
		return self;
	}
	
	
	public double add(double addend1, double addend2) {
		return addend1 + addend2;
	}
	
	public double subtract(double minuend, double subtrahend) {
		return minuend - subtrahend;
	}
	
	public double multiply(double multiplicand, double multiplier) {
		return multiplicand * multiplier;
	}
	
	public double divide(double dividend, double divisor) {
		return dividend/divisor;
	}
	
	public double exponentiate(double base, double exponent) {
		return Math.pow(base, exponent);
	}
}

class InvalidExpressionException extends Exception {
	public InvalidExpressionException(String msg) { super(msg); }
}


class Lexer {
	private int position = 0;
	private final String expression;
	
	public Lexer(String expression) {
		this.expression = expression;
	}
	
	public Token nextToken() {
		if (Character.isDigit(expression.charAt(position))) {
			return getNumber();
		} else {
			if (expression.charAt(position) == '(') {
				return getSubExpression();
			} else {	
				return getOperator();
			}
		}
	}
	
	private Token getNumber() {
		int initialPosition = position;
		StringBuilder value = new StringBuilder();
		
		while(position < expression.length() && Character.isDigit(expression.charAt(position))) {
			value.append(expression.charAt(position));
			position++;
		}
		
		return new Token<Double>(Double.parseDouble(value.toString()) , Token.Type.NUMBER);
	}
	
	private Token getOperator() {
		int initialPosition = position;
		
		StringBuilder value = new StringBuilder();
		while(position < expression.length() && !Character.isDigit(expression.charAt(position)) && expression.charAt(position) != '(') {
			value.append(expression.charAt(position));
			position++;
		}
		
		return new Token<String>(value.toString(), Token.Type.OPERATOR);
	}
	
	private Token getSubExpression() {
		int initialPosition = position;
		
		StringBuilder value = new StringBuilder();
		while(position < expression.length() && expression.charAt(position) != ')') {
			value.append(expression.charAt(position));
			position++;
		}
		try {
			value.append(expression.charAt(position++));
		} catch (IndexOutOfBoundsException ex) {};
		
		return new Token<String>(value.toString(), Token.Type.EXPRESSION);
	}
	
	public boolean hasMoreTokens() {
		if (position >= expression.length()) return false;
		return true;
	}
}

class Token<T> {
	private Type type;
	private T value;
	
	public Token(T value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public Type type() {
		return type;
	}
	
	public T value() {
		return value;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public String toString() {
		if (type == Type.NUMBER) {
			return "Number: " + value;
		} else if (type == Type.OPERATOR) {
			return "Operator: " + value;
		} else {
			return "Expression: " + value;
		}
	}
	
	enum Type {
		NUMBER, OPERATOR, EXPRESSION;
	}
}

class Operator implements Comparable<Operator> {

	private String value;
	private Precedence precedence;
	
	public Operator(String value, Precedence precedence) {
		this.value = value;
		this.precedence = precedence;
	}
	
	public Operator(String value) {
		this(value, Precedence.NONE);
	}
	
	public String value() {
		return value;
	}
	
	public Precedence precedence() {
		return precedence;
	}
	
	public int compareTo(Operator o) {
		return precedence.compareTo(o.precedence());
	}
	
	enum Precedence {
		ZERO, FIRST, SECOND, THIRD, FOURTH, NONE;
	}
}


