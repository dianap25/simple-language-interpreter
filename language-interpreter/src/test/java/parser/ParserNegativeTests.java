package parser;

import lexer.Lexer;
import lexer.SourceReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserNegativeTests {

    private ParserException parseFails(String source) {
        Parser parser = new Parser(new Lexer(SourceReader.fromString(source)));
        return assertThrows(ParserException.class, parser::parseProgram);
    }

    @Test
    void failForAssignmentWithoutExpression() {
        ParserException ex = parseFails("x =");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForIfWithoutThen() {
        ParserException ex = parseFails("if x > 0 y = 1 else y = 0");
        assertTrue(ex.getMessage().contains("expected 'then'"));
    }

    @Test
    void failForIfWithoutElse() {
        ParserException ex = parseFails("if x > 0 then y = 1");
        assertTrue(ex.getMessage().contains("expected 'else'"));
    }

    @Test
    void failForIfWithoutElseBranch() {
        ParserException ex = parseFails("if x > 0 then y = 1 else");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForWhileWithoutDo() {
        ParserException ex = parseFails("while x < 3 x = x + 1");
        assertTrue(ex.getMessage().contains("expected 'do'"));
    }

    @Test
    void failForWhileWithoutBody() {
        ParserException ex = parseFails("while x < 3 do");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForFunctionWithoutName() {
        ParserException ex = parseFails("fun (a, b) { return a + b }");
        assertTrue(ex.getMessage().contains("expected function name"));
    }

    @Test
    void failForFunctionWithoutOpeningParenthesis() {
        ParserException ex = parseFails("fun add a, b) { return a + b }");
        assertTrue(ex.getMessage().contains("expected '('"));
    }

    @Test
    void failForFunctionWithoutClosingParenthesis() {
        ParserException ex = parseFails("fun add(a, b { return a + b }");
        assertTrue(ex.getMessage().contains("expected ')'"));
    }

    @Test
    void failForFunctionWithoutBlock() {
        ParserException ex = parseFails("fun add(a, b) return a + b");
        assertTrue(ex.getMessage().contains("expected '{'"));
    }

    @Test
    void failForFunctionWithMissingParameterAfterComma() {
        ParserException ex = parseFails("fun add(a, ) { return a }");
        assertTrue(ex.getMessage().contains("expected parameter name"));
    }

    @Test
    void failForReturnWithoutExpression() {
        ParserException ex = parseFails("return");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForUnclosedBlock() {
        ParserException ex = parseFails("{ x = 1");
        assertTrue(ex.getMessage().contains("expected '}'"));
    }

    @Test
    void failForUnclosedGrouping() {
        ParserException ex = parseFails("x = (1 + 2");
        assertTrue(ex.getMessage().contains("expected ')'"));
    }

    @Test
    void failForUnclosedCall() {
        ParserException ex = parseFails("x = add(1, 2");
        assertTrue(ex.getMessage().contains("expected ')'"));
    }

    @Test
    void failForMissingArgumentAfterComma() {
        ParserException ex = parseFails("x = add(1, )");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForEmptyArgumentBeforeComma() {
        ParserException ex = parseFails("x = add(, 1)");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForUnclosedListLiteral() {
        ParserException ex = parseFails("x = [1, 2, 3");
        assertTrue(ex.getMessage().contains("expected ']'"));
    }

    @Test
    void failForMissingListElementAfterComma() {
        ParserException ex = parseFails("x = [1, ]");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForPropertyAccessWithoutIdentifier() {
        ParserException ex = parseFails("x = values.");
        assertTrue(ex.getMessage().contains("expected identifier after '.'"));
    }

    @Test
    void failForSequenceEndingWithComma() {
        ParserException ex = parseFails("x = 1,");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForInvalidTopLevelClosingBrace() {
        ParserException ex = parseFails("}");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForInvalidTopLevelClosingParenthesis() {
        ParserException ex = parseFails(")");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForInvalidTopLevelClosingBracket() {
        ParserException ex = parseFails("]");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForBinaryOperatorWithoutRightOperand() {
        ParserException ex = parseFails("x = 1 +");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForLogicalOperatorWithoutRightOperand() {
        ParserException ex = parseFails("x = true &&");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForComparisonOperatorWithoutRightOperand() {
        ParserException ex = parseFails("x = a <");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForUnaryOperatorWithoutOperand() {
        ParserException ex = parseFails("x = !");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForInvalidExpressionStartAfterAssignment() {
        ParserException ex = parseFails("x = else");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForIfThenElseWithInvalidElseBranch() {
        ParserException ex = parseFails("if true then x = 1 else }");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForCallThenUnexpectedToken() {
        ParserException ex = parseFails("x = foo(1 2)");
        assertTrue(ex.getMessage().contains("expected ')'"));
    }

    @Test
    void failForListWithoutCommaBetweenElements() {
        ParserException ex = parseFails("x = [1 2]");
        assertTrue(ex.getMessage().contains("expected ']'"));
    }

    @Test
    void failForParamsWithoutCommaBetweenIdentifiers() {
        ParserException ex = parseFails("fun add(a b) { return a + b }");
        assertTrue(ex.getMessage().contains("expected ')'"));
    }

    @Test
    void failForElseAsStandaloneStatement() {
        ParserException ex = parseFails("else x = 1");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForThenAsStandaloneStatement() {
        ParserException ex = parseFails("then x = 1");
        assertTrue(ex.getMessage().contains("expected expression"));
    }

    @Test
    void failForDoAsStandaloneStatement() {
        ParserException ex = parseFails("do x = 1");
        assertTrue(ex.getMessage().contains("expected expression"));
    }
}