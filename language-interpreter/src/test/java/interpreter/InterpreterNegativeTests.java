package interpreter;

import ast.Program;
import lexer.Lexer;
import lexer.SourceReader;
import org.junit.jupiter.api.Test;
import parser.Parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpreterNegativeTests {

    private RuntimeException runFails(String source) {
        Parser parser = new Parser(new Lexer(SourceReader.fromString(source)));
        Program program = parser.parseProgram();

        Interpreter interpreter = new Interpreter();
        return assertThrows(RuntimeException.class, () -> interpreter.interpret(program));
    }

    @Test
    void failForUndefinedVariable() {
        RuntimeException ex = runFails("""
                x = y + 1
                """);

        assertTrue(ex.getMessage().contains("undefined variable"));
    }

    @Test
    void failForDivisionByZeroInt() {
        RuntimeException ex = runFails("""
                x = 10 / 0
                """);

        assertTrue(ex.getMessage().contains("division by zero"));
    }

    @Test
    void failForDivisionByZeroDouble() {
        RuntimeException ex = runFails("""
                x = 10.0 / 0.0
                """);

        assertTrue(ex.getMessage().contains("division by zero"));
    }

    @Test
    void failForInvalidArithmeticTypes() {
        RuntimeException ex = runFails("""
                x = "abc" - 2
                """);

        assertTrue(ex.getMessage().contains("operator '-'"));
    }

    @Test
    void failForInvalidLogicalTypes() {
        RuntimeException ex = runFails("""
                x = 1 && true
                """);

        assertTrue(ex.getMessage().contains("operator '&&'"));
    }

    @Test
    void failForInvalidUnaryNotType() {
        RuntimeException ex = runFails("""
                x = !5
                """);

        assertTrue(ex.getMessage().contains("operator '!'"));
    }

    @Test
    void failForInvalidUnaryMinusType() {
        RuntimeException ex = runFails("""
                x = -"abc"
                """);

        assertTrue(ex.getMessage().contains("operator '-'"));
    }

    @Test
    void failForNonBoolIfCondition() {
        RuntimeException ex = runFails("""
                if 5 then x = 1 else x = 2
                """);

        assertTrue(ex.getMessage().contains("condition must evaluate to bool"));
    }

    @Test
    void failForNonBoolWhileCondition() {
        RuntimeException ex = runFails("""
                while 5 do x = 1
                """);

        assertTrue(ex.getMessage().contains("condition must evaluate to bool"));
    }

    @Test
    void failForWrongNumberOfFunctionArgumentsTooFew() {
        RuntimeException ex = runFails("""
                fun add(a, b) { return a + b }
                x = add(1)
                """);

        assertTrue(ex.getMessage().contains("wrong number of arguments"));
    }

    @Test
    void failForWrongNumberOfFunctionArgumentsTooMany() {
        RuntimeException ex = runFails("""
                fun add(a, b) { return a + b }
                x = add(1, 2, 3)
                """);

        assertTrue(ex.getMessage().contains("wrong number of arguments"));
    }

    @Test
    void failForCallingNonCallableExpression() {
        RuntimeException ex = runFails("""
                x = 5
                y = x()
                """);

        assertTrue(ex.getMessage().contains("not callable"));
    }

    @Test
    void failForTypeChangeOfVariable() {
        RuntimeException ex = runFails("""
                x = 5
                x = "abc"
                """);

        assertTrue(ex.getMessage().contains("cannot change type"));
    }

    @Test
    void failForBuiltinIntConversionFromString() {
        RuntimeException ex = runFails("""
                x = int("abc")
                """);

        assertTrue(ex.getMessage().contains("convert"));
    }

    @Test
    void failForStringIntOnWrongType() {
        RuntimeException ex = runFails("""
                x = string_int(true)
                """);

        assertTrue(ex.getMessage().contains("string_int"));
    }

    @Test
    void failForStringDoubleOnWrongType() {
        RuntimeException ex = runFails("""
                x = string_double(5)
                """);

        assertTrue(ex.getMessage().contains("string_double"));
    }

    @Test
    void failForStringBoolOnWrongType() {
        RuntimeException ex = runFails("""
                x = string_bool(1)
                """);

        assertTrue(ex.getMessage().contains("string_bool"));
    }

    @Test
    void failForUnknownProperty() {
        RuntimeException ex = runFails("""
                values = [1, 2, 3]
                x = values.unknown()
                """);

        assertTrue(ex.getMessage().contains("property"));
    }

    @Test
    void failForPropertyOnNonList() {
        RuntimeException ex = runFails("""
                x = 5
                y = x.size()
                """);

        assertTrue(ex.getMessage().contains("property"));
    }

    @Test
    void failForFunctionWithoutReturn() {
        RuntimeException ex = runFails("""
                fun test() { x = 1 }
                y = test()
                """);

        assertTrue(ex.getMessage().contains("must return"));
    }
}