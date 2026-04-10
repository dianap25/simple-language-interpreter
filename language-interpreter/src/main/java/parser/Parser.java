package parser;

import ast.Expr;
import ast.Program;
import ast.Stmt;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final Lexer lexer;
    private Token current;
    private Token next;
    private Token previous;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.current = lexer.nextToken();
        this.next = lexer.nextToken();
        this.previous = null;
    }

    /*
     * PROGRAM = { STATEMENT } ;
     */
    public Program parseProgram() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.EOF)) {
            statements.add(parseStatement());
        }

        return new Program(List.copyOf(statements));
    }

    /*
     * STATEMENT = SINGLE_STATEMENT, { ",", SINGLE_STATEMENT } ;
     */
    private Stmt parseStatement() {
        Token start = current;
        List<Stmt> statements = new ArrayList<>();
        statements.add(parseSingleStatement());

        while (match(TokenType.COMMA)) {
            statements.add(parseSingleStatement());
        }

        if (statements.size() == 1) {
            return statements.get(0);
        }

        return new Stmt.Sequence(List.copyOf(statements), start.position());
    }




    /*
     * SINGLE_STATEMENT = ASSIGNMENT
     *                  | IF
     *                  | WHILE
     *                  | FUNCTION
     *                  | RETURN
     *                  | BLOCK
     *                  | EXPRESSION_STATEMENT ;
     */
    private Stmt parseSingleStatement() {
        if(check(TokenType.IF)){
            return parseIf();
        }
        if(check(TokenType.WHILE)) {
            return parseWhile();
        }
        if (check(TokenType.FUN)) {
            return parseFunction();
        }
        if (check(TokenType.RETURN)) {
            return parseReturn();
        }
        if (check(TokenType.OPEN_CURLY_BRACKETS)) {
            return parseBlock();
        }
        if (check(TokenType.FUN)) {
            return parseFunction();
        }
        if (check(TokenType.RETURN)) {
            return parseReturn();
        }
        if (check(TokenType.OPEN_CURLY_BRACKETS)) {
            return parseBlock();
        }
        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGNMENT)) {
            return parseAssignment();
        }

        return parseExpressionStatement();
    }


    /*
     * ASSIGNMENT = IDENTIFIER, "=", EXPRESSION ;
     */
    private Stmt.Assignment parseAssignment() {
        Token name = expect(TokenType.IDENTIFIER, "expected identifier");
        expect(TokenType.ASSIGNMENT, "expected '=' after identifier");
        Expr value = parseExpression();

        return new Stmt.Assignment((String) name.value(), value, name.position());
    }


    /*
     * IF = "if", EXPRESSION, "then", SINGLE_STATEMENT, "else", SINGLE_STATEMENT ;
     */
    private Stmt.If parseIf() {
        Token ifToken = expect(TokenType.IF, "expected 'if' ");
        Expr condition = parseExpression();
        expect(TokenType.THEN, "expected 'then' ");

        Stmt thenBranch = parseSingleStatement();

        expect(TokenType.ELSE, "expected 'else' ");
        Stmt elseBranch = parseSingleStatement();

        return new Stmt.If(condition, thenBranch, elseBranch, ifToken.position());
    }

    /*
     * WHILE = "while", EXPRESSION, "do", STATEMENT ;
     */
    private Stmt.While parseWhile() {
        Token whileToken = expect(TokenType.WHILE, "expected 'while' ");
        Expr condition = parseExpression();
        expect(TokenType.DO, "expected 'do' after while condition");

        Stmt body = parseStatement();
        return new Stmt.While(condition, body, whileToken.position());
    }


    /*
     * FUNCTION = "fun", IDENTIFIER, "(", [PARAMS], ")", BLOCK ;
     */
    private Stmt.Function parseFunction() {
        Token funToken = expect(TokenType.FUN, "expected 'fun' ");
        Token name = expect(TokenType.IDENTIFIER, "expected function name");

        expect(TokenType.OPEN_PARENTHESES, "expected '(' after function name");

        List<String> parameters = new ArrayList<>();
        if(!check(TokenType.CLOSE_PARENTHESES)){
            parameters = parseParams();
        }

        expect(TokenType.CLOSE_PARENTHESES, "expected ')' ater parameter list");

        Stmt.Block body = parseBlock();
        return new Stmt.Function((String) name.value(), List.copyOf(parameters), body, funToken.position());
    }

    /*
     * PARAMS = IDENTIFIER, { ",", IDENTIFIER } ;
     */
    private List<String> parseParams(){
        List<String> parameters = new ArrayList<>();

        Token first = expect(TokenType.IDENTIFIER, "expected parameter name");
        parameters.add((String) first.value());

        while (match(TokenType.COMMA)) {
            Token parameter = expect(TokenType.IDENTIFIER, "expected parameter name after ','");
            parameters.add((String) parameter.value());
        }

        return parameters;
    }


    /*
     * RETURN = "return", EXPRESSION ;
     */
    private Stmt.Return parseReturn(){
        Token returnToken = expect(TokenType.RETURN, "expected 'return'");
        Expr value = parseExpression();

        return new Stmt.Return(value, returnToken.position());
    }


    /*
     * BLOCK = "{", { STATEMENT }, "}" ;
     */
    private Stmt.Block parseBlock(){
        Token open = expect(TokenType.OPEN_CURLY_BRACKETS, "expected '{'");

        List<Stmt> statements = new ArrayList<>();
        while(!check(TokenType.CLOSE_CURLY_BRACKETS) && !check(TokenType.EOF)){
            statements.add(parseStatement());
        }

        expect(TokenType.CLOSE_CURLY_BRACKETS, "expected '}' after block");

        return new Stmt.Block(List.copyOf(statements), open.position());
    }


    /*
     *  EXPRESSION_STATEMENT = EXPRESSION;
     */
    private Stmt.Expression parseExpressionStatement(){
        Token start = current;
        Expr expression = parseExpression();
        return new Stmt.Expression(expression, start.position());
    }

    /*
     * EXPRESSION = LOGICAL_OR;
     */
    private Expr parseExpression(){
        return parseLogicalOr();
    }

    /*
     * LOGICAL_OR = LOGICAL_AND, { "||", LOGICAL_AND };
     */
    private Expr parseLogicalOr(){
        Expr expr = parseLogicalAnd();

        while(match(TokenType.LOGICAL_OR)){
            Token operator = previous();
            Expr right = parseLogicalAnd();
            expr = new Expr.Binary(expr, "||", right, operator.position());
        }
        return expr;
    }


    /*
     * LOGICAL_AND = EQUALITY, { "&&", EQUALITY};
     */
    private Expr parseLogicalAnd(){
        Expr expr = parseEquality();

        while(match(TokenType.LOGICAL_AND)){
            Token operator = previous();
            Expr right = parseEquality();
            expr = new Expr.Binary(expr, "&&", right, operator.position());
        }
        return expr;
    }


    /*
     * EQUALITY = COMPARISON, { ("==" | "!="), COMPARISON };
     */
    private Expr parseEquality(){
        Expr expr = parseComparison();
        while(match(TokenType.EQUAL_TO, TokenType.NOT_EQUAL_TO)){
            Token operator = previous();
            Expr right = parseComparison();
            expr = new Expr.Binary(expr, operatorLexeme(operator.type()), right, operator.position());
        }
        return expr;
    }

    /*
     *  COMPARISON = TERM, { (">" | "<" | ">=" | "<="), TERM } ;
     */
    private Expr parseComparison(){
        Expr expr = parseTerm();

        while(match(
                TokenType.GREATER_THAN,
                TokenType.LESS_THAN,
                TokenType.GREATER_THAN_EQUAL,
                TokenType.LESS_THAN_EQUAL
        )){
            Token operator = previous();
            Expr right = parseTerm();
            expr = new Expr.Binary(expr, operatorLexeme(operator.type()), right, operator.position());
        }
        return expr;
    }

    /*
     * TERM = FACTOR, { ("+" | "-"), FACTOR } ;
     */
    private Expr parseTerm() {
        Expr expr = parseFactor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = parseFactor();
            expr = new Expr.Binary(expr, operatorLexeme(operator.type()), right, operator.position());
        }

        return expr;
    }

    /*
     * FACTOR = UNARY, { ("*" | "/"), UNARY } ;
     */
    private Expr parseFactor() {
        Expr expr = parseUnary();

        while (match(TokenType.TIMES, TokenType.DIVIDE)) {
            Token operator = previous();
            Expr right = parseUnary();
            expr = new Expr.Binary(expr, operatorLexeme(operator.type()), right, operator.position());
        }

        return expr;
    }


    /*
     * UNARY = ("!" | "-"), UNARY | POSTFIX ;
     */
    private Expr parseUnary() {
        if (match(TokenType.LOGICAL_NOT, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = parseUnary();
            return new Expr.Unary(operatorLexeme(operator.type()), right, operator.position());
        }

        return parsePostfix();
    }

    /*
     * POSTFIX = PRIMARY, { POSTFIXPART } ;
     * POSTFIXPART = (".", IDENTIFIER) | ("(", [ARGS], ")") ;
     */
    private Expr parsePostfix() {
        Expr expr = parsePrimary();

        while (true) {
            if (match(TokenType.OPEN_PARENTHESES)) {
                Token open = previous();
                List<Expr> arguments = new ArrayList<>();

                if (!check(TokenType.CLOSE_PARENTHESES)) {
                    arguments = parseArgs();
                }

                expect(TokenType.CLOSE_PARENTHESES, "expected ')' after argument list");
                expr = new Expr.Call(expr, List.copyOf(arguments), open.position());
                continue;
            }

            if (match(TokenType.DOT)) {
                Token property = expect(TokenType.IDENTIFIER, "expected identifier after '.'");
                expr = new Expr.PropertyAccess(expr, (String) property.value(), property.position());
                continue;
            }

            break;
        }

        return expr;
    }

    /*
     * PRIMARY = INT_LITERAL
     *         | DOUBLE_LITERAL
     *         | STRING_LITERAL
     *         | BOOL_LITERAL
     *         | IDENTIFIER
     *         | LIST
     *         | "(", EXPRESSION, ")" ;
     */
    private Expr parsePrimary() {
        if (match(TokenType.INT_LITERAL)) {
            Token token = previous();
            return new Expr.IntLiteral((Integer) token.value(), token.position());
        }

        if (match(TokenType.DOUBLE_LITERAL)) {
            Token token = previous();
            return new Expr.DoubleLiteral((Double) token.value(), token.position());
        }

        if (match(TokenType.STRING_LITERAL)) {
            Token token = previous();
            return new Expr.StringLiteral((String) token.value(), token.position());
        }

        if (match(TokenType.TRUE)) {
            Token token = previous();
            return new Expr.BoolLiteral(true, token.position());
        }

        if (match(TokenType.FALSE)) {
            Token token = previous();
            return new Expr.BoolLiteral(false, token.position());
        }

        if (match(TokenType.IDENTIFIER)) {
            Token token = previous();
            return new Expr.Variable((String) token.value(), token.position());
        }

        if (match(TokenType.OPEN_SQUARE_BRACKETS)) {
            Token openBracket = previous();
            return parseListLiteral(openBracket);
        }

        if (match(TokenType.OPEN_PARENTHESES)) {
            Token open = previous();
            Expr expression = parseExpression();
            expect(TokenType.CLOSE_PARENTHESES, "expected ')' after expression");
            return new Expr.Grouping(expression, open.position());
        }

        throw new ParserException(current, "expected expression");
    }

    /*
     * ARGS = EXPRESSION, { ",", EXPRESSION } ;
     */
    private List<Expr> parseArgs() {
        List<Expr> arguments = new ArrayList<>();
        arguments.add(parseExpression());

        while (match(TokenType.COMMA)) {
            arguments.add(parseExpression());
        }

        return arguments;
    }


    /*
     * LIST = "[", [ EXPRESSION, { ",", EXPRESSION } ], "]" ;
     */
    private Expr.ListLiteral parseListLiteral(Token openBracket) {
        List<Expr> elements = new ArrayList<>();

        if (!check(TokenType.CLOSE_SQUARE_BRACKETS)) {
            elements.add(parseExpression());

            while (match(TokenType.COMMA)) {
                elements.add(parseExpression());
            }
        }

        expect(TokenType.CLOSE_SQUARE_BRACKETS, "expected ']' after list literal");
        return new Expr.ListLiteral(List.copyOf(elements), openBracket.position());
    }

    private boolean check(TokenType type) {
        return current.type() == type;
    }

    private boolean checkNext(TokenType type) {
        return next.type() == type;
    }


    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token expect(TokenType type, String message) {
        if(!check(type)){
            throw new ParserException(current, message);
        }
        return advance();
    }

    private Token advance() {
        Token consumed = current;
        previous = consumed;
        current = next;
        next = lexer.nextToken();
        return consumed;
    }

    private Token previous(){
        if(previous == null){
            throw new ParserException(lexer.nextToken(), "No previous token");
        }
        return previous;
    }

    private String operatorLexeme(TokenType type) {
        return switch (type) {
            case PLUS -> "+";
            case MINUS -> "-";
            case TIMES -> "*";
            case DIVIDE -> "/";
            case EQUAL_TO -> "==";
            case NOT_EQUAL_TO -> "!=";
            case LESS_THAN -> "<";
            case GREATER_THAN -> ">";
            case LESS_THAN_EQUAL -> "<=";
            case GREATER_THAN_EQUAL -> ">=";
            case LOGICAL_AND -> "&&";
            case LOGICAL_OR -> "||";
            case LOGICAL_NOT -> "!";
            default -> throw new IllegalArgumentException("Unsupported operator token: " + type);
        };
    }
}
