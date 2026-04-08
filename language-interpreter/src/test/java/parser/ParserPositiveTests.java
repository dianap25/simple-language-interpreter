package parser;

import ast.Expr;
import ast.Program;
import ast.Stmt;
import lexer.Lexer;
import lexer.SourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserPositiveTests {

    private Program parse(String source) {
        Parser parser = new Parser(new Lexer(SourceReader.fromString(source)));
        return parser.parseProgram();
    }

    @Test
    void parseEmptyProgram() {
        Program program = parse("");

        assertNotNull(program);
        assertTrue(program.statements().isEmpty());
    }

    @Test
    void parseSingleAssignment() {
        Program program = parse("x = 42");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.Assignment.class, program.statements().get(0));

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertEquals("x", assignment.name());

        assertInstanceOf(Expr.IntLiteral.class, assignment.value());
        Expr.IntLiteral literal = (Expr.IntLiteral) assignment.value();
        assertEquals(42, literal.value());
    }

    @Test
    void parseMultipleTopLevelStatements() {
        Program program = parse("""
                x = 1
                y = 2
                z = 3
                """);

        assertEquals(3, program.statements().size());
        assertInstanceOf(Stmt.Assignment.class, program.statements().get(0));
        assertInstanceOf(Stmt.Assignment.class, program.statements().get(1));
        assertInstanceOf(Stmt.Assignment.class, program.statements().get(2));
    }

    @Test
    void parseExpressionStatement() {
        Program program = parse("foo(1, 2)");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.Expression.class, program.statements().get(0));

        Stmt.Expression stmt = (Stmt.Expression) program.statements().get(0);
        assertInstanceOf(Expr.Call.class, stmt.expression());
    }

    @Test
    void parseArithmeticPrecedence() {
        Program program = parse("x = 1 + 2 * 3");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Binary.class, assignment.value());

        Expr.Binary plus = (Expr.Binary) assignment.value();
        assertEquals("+", plus.operator());

        assertInstanceOf(Expr.IntLiteral.class, plus.left());
        assertInstanceOf(Expr.Binary.class, plus.right());

        Expr.Binary multiply = (Expr.Binary) plus.right();
        assertEquals("*", multiply.operator());
    }

    @Test
    void parseGroupingChangesStructure() {
        Program program = parse("x = (1 + 2) * 3");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        Expr.Binary multiply = (Expr.Binary) assignment.value();

        assertEquals("*", multiply.operator());
        assertInstanceOf(Expr.Grouping.class, multiply.left());

        Expr.Grouping grouping = (Expr.Grouping) multiply.left();
        assertInstanceOf(Expr.Binary.class, grouping.expression());

        Expr.Binary plus = (Expr.Binary) grouping.expression();
        assertEquals("+", plus.operator());
    }

    @Test
    void parseUnaryMinus() {
        Program program = parse("x = -5");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Unary.class, assignment.value());

        Expr.Unary unary = (Expr.Unary) assignment.value();
        assertEquals("-", unary.operator());
        assertInstanceOf(Expr.IntLiteral.class, unary.right());
    }

    @Test
    void parseLogicalNot() {
        Program program = parse("x = !true");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Unary.class, assignment.value());

        Expr.Unary unary = (Expr.Unary) assignment.value();
        assertEquals("!", unary.operator());
        assertInstanceOf(Expr.BoolLiteral.class, unary.right());
    }

    @Test
    void parseComparison() {
        Program program = parse("x = a <= b");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Binary.class, assignment.value());

        Expr.Binary binary = (Expr.Binary) assignment.value();
        assertEquals("<=", binary.operator());
    }

    @Test
    void parseEquality() {
        Program program = parse("x = a == b");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        Expr.Binary binary = (Expr.Binary) assignment.value();

        assertEquals("==", binary.operator());
    }

    @Test
    void parseLogicalPrecedence() {
        Program program = parse("x = a || b && c");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Binary.class, assignment.value());

        Expr.Binary orExpr = (Expr.Binary) assignment.value();
        assertEquals("||", orExpr.operator());
        assertInstanceOf(Expr.Binary.class, orExpr.right());

        Expr.Binary andExpr = (Expr.Binary) orExpr.right();
        assertEquals("&&", andExpr.operator());
    }

    @Test
    void parseStringLiteral() {
        Program program = parse("x = \"hello\"");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.StringLiteral.class, assignment.value());

        Expr.StringLiteral literal = (Expr.StringLiteral) assignment.value();
        assertEquals("hello", literal.value());
    }

    @Test
    void parseDoubleLiteral() {
        Program program = parse("x = 3.14");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.DoubleLiteral.class, assignment.value());

        Expr.DoubleLiteral literal = (Expr.DoubleLiteral) assignment.value();
        assertEquals(3.14, literal.value());
    }

    @Test
    void parseBoolLiteral() {
        Program program = parse("x = false");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.BoolLiteral.class, assignment.value());

        Expr.BoolLiteral literal = (Expr.BoolLiteral) assignment.value();
        assertFalse(literal.value());
    }

    @Test
    void parseListLiteral() {
        Program program = parse("x = [1, 2, 3]");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.ListLiteral.class, assignment.value());

        Expr.ListLiteral list = (Expr.ListLiteral) assignment.value();
        assertEquals(3, list.elements().size());
        assertInstanceOf(Expr.IntLiteral.class, list.elements().get(0));
        assertInstanceOf(Expr.IntLiteral.class, list.elements().get(1));
        assertInstanceOf(Expr.IntLiteral.class, list.elements().get(2));
    }

    @Test
    void parseEmptyListLiteral() {
        Program program = parse("x = []");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        Expr.ListLiteral list = (Expr.ListLiteral) assignment.value();

        assertTrue(list.elements().isEmpty());
    }

    @Test
    void parseCallWithArguments() {
        Program program = parse("x = add(1, 2)");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Call.class, assignment.value());

        Expr.Call call = (Expr.Call) assignment.value();
        assertEquals(2, call.arguments().size());
        assertInstanceOf(Expr.Variable.class, call.callee());
    }

    @Test
    void parseCallWithoutArguments() {
        Program program = parse("x = foo()");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        Expr.Call call = (Expr.Call) assignment.value();

        assertTrue(call.arguments().isEmpty());
    }


    @Test
    void parsePropertyAccessThenCall() {
        Program program = parse("x = values.size()");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Call.class, assignment.value());

        Expr.Call call = (Expr.Call) assignment.value();
        assertInstanceOf(Expr.PropertyAccess.class, call.callee());
        assertTrue(call.arguments().isEmpty());
    }

    @Test
    void parseIfStatement() {
        Program program = parse("if x > 10 then y = 1 else y = 0");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.If.class, program.statements().get(0));

        Stmt.If stmt = (Stmt.If) program.statements().get(0);
        assertInstanceOf(Expr.Binary.class, stmt.condition());
        assertInstanceOf(Stmt.Assignment.class, stmt.thenStmt());
        assertInstanceOf(Stmt.Assignment.class, stmt.elseStmt());
    }

    @Test
    void parseWhileStatement() {
        Program program = parse("while x < 3 do x = x + 1");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.While.class, program.statements().get(0));

        Stmt.While stmt = (Stmt.While) program.statements().get(0);
        assertInstanceOf(Expr.Binary.class, stmt.condition());
        assertInstanceOf(Stmt.Assignment.class, stmt.body());
    }

    @Test
    void parseSequenceStatement() {
        Program program = parse("x = 1, y = 2, z = 3");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.Sequence.class, program.statements().get(0));

        Stmt.Sequence sequence = (Stmt.Sequence) program.statements().get(0);
        assertEquals(3, sequence.statements().size());
        assertInstanceOf(Stmt.Assignment.class, sequence.statements().get(0));
        assertInstanceOf(Stmt.Assignment.class, sequence.statements().get(1));
        assertInstanceOf(Stmt.Assignment.class, sequence.statements().get(2));
    }

    @Test
    void parseWhileWithSequenceBody() {
        Program program = parse("while x < 3 do y = y + 1, x = x + 1");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.While.class, program.statements().get(0));

        Stmt.While stmt = (Stmt.While) program.statements().get(0);
        assertInstanceOf(Stmt.Sequence.class, stmt.body());

        Stmt.Sequence sequence = (Stmt.Sequence) stmt.body();
        assertEquals(2, sequence.statements().size());
    }

    @Test
    void parseNestedIfInsideWhile() {
        Program program = parse("while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.While.class, program.statements().get(0));

        Stmt.While whileStmt = (Stmt.While) program.statements().get(0);
        assertInstanceOf(Stmt.If.class, whileStmt.body());

        Stmt.If ifStmt = (Stmt.If) whileStmt.body();

        assertInstanceOf(Stmt.Assignment.class, ifStmt.thenStmt());
        assertInstanceOf(Stmt.Sequence.class, ifStmt.elseStmt());

        Stmt.Sequence elseSequence = (Stmt.Sequence) ifStmt.elseStmt();
        assertEquals(2, elseSequence.statements().size());
        assertInstanceOf(Stmt.Assignment.class, elseSequence.statements().get(0));
        assertInstanceOf(Stmt.Assignment.class, elseSequence.statements().get(1));
    }

    @Test
    void parseBlock() {
        Program program = parse("{ x = 1 y = 2 }");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.Block.class, program.statements().get(0));

        Stmt.Block block = (Stmt.Block) program.statements().get(0);
        assertEquals(2, block.statements().size());
    }

    @Test
    void parseReturnStatement() {
        Program program = parse("return x + 1");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.Return.class, program.statements().get(0));

        Stmt.Return returnStmt = (Stmt.Return) program.statements().get(0);
        assertInstanceOf(Expr.Binary.class, returnStmt.value());
    }

    @Test
    void parseFunctionWithoutParameters() {
        Program program = parse("fun foo() { return 1 }");

        assertEquals(1, program.statements().size());
        assertInstanceOf(Stmt.Function.class, program.statements().get(0));

        Stmt.Function function = (Stmt.Function) program.statements().get(0);
        assertEquals("foo", function.name());
        assertTrue(function.parameters().isEmpty());
        assertEquals(1, function.body().statements().size());
        assertInstanceOf(Stmt.Return.class, function.body().statements().get(0));
    }

    @Test
    void parseFunctionWithParameters() {
        Program program = parse("fun add(a, b) { return a + b }");

        Stmt.Function function = (Stmt.Function) program.statements().get(0);

        assertEquals("add", function.name());
        assertEquals(List.of("a", "b"), function.parameters());
        assertEquals(1, function.body().statements().size());
    }

    @Test
    void parseFunctionWithMultipleStatementsInBody() {
        Program program = parse("""
                fun test(a) {
                    x = a
                    y = a + 1
                    return y
                }
                """);

        Stmt.Function function = (Stmt.Function) program.statements().get(0);
        assertEquals(3, function.body().statements().size());
        assertInstanceOf(Stmt.Assignment.class, function.body().statements().get(0));
        assertInstanceOf(Stmt.Assignment.class, function.body().statements().get(1));
        assertInstanceOf(Stmt.Return.class, function.body().statements().get(2));
    }

    @Test
    void parseRecursiveFunction() {
        Program program = parse("""
                fun fact(n) {
                    if n <= 0 then return 1 else return n * fact(n - 1)
                }
                """);

        Stmt.Function function = (Stmt.Function) program.statements().get(0);
        assertEquals("fact", function.name());
        assertEquals(List.of("n"), function.parameters());
        assertEquals(1, function.body().statements().size());
        assertInstanceOf(Stmt.If.class, function.body().statements().get(0));
    }

    @Test
    void parseProgramWithFunctionAndAssignment() {
        Program program = parse("""
                fun add(a, b) { return a + b }
                x = add(2, 3)
                """);

        assertEquals(2, program.statements().size());
        assertInstanceOf(Stmt.Function.class, program.statements().get(0));
        assertInstanceOf(Stmt.Assignment.class, program.statements().get(1));
    }

    @Test
    void parseNestedCalls() {
        Program program = parse("x = foo(bar(1), baz(2, 3))");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        Expr.Call outerCall = (Expr.Call) assignment.value();

        assertEquals(2, outerCall.arguments().size());
        assertInstanceOf(Expr.Call.class, outerCall.arguments().get(0));
        assertInstanceOf(Expr.Call.class, outerCall.arguments().get(1));
    }

    @Test
    void parseComplexExpression() {
        Program program = parse("x = !(a < 10) || foo(1, 2) && (b + 3) >= 7");

        Stmt.Assignment assignment = (Stmt.Assignment) program.statements().get(0);
        assertInstanceOf(Expr.Binary.class, assignment.value());

        Expr.Binary root = (Expr.Binary) assignment.value();
        assertEquals("||", root.operator());
    }

    @Test
    void parseCommentsIgnoredByLexerAndParser() {
        Program program = parse("""
                // comment
                x = 1
                // comment
                y = 2
                """);

        assertEquals(2, program.statements().size());
        assertInstanceOf(Stmt.Assignment.class, program.statements().get(0));
        assertInstanceOf(Stmt.Assignment.class, program.statements().get(1));
    }
}