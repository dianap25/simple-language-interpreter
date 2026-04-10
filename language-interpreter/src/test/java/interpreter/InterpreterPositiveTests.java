package interpreter;

import ast.Program;
import lexer.Lexer;
import lexer.SourceReader;
import org.junit.jupiter.api.Test;
import parser.Parser;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterPositiveTests {

    private Interpreter run(String source) {
        Parser parser = new Parser(new Lexer(SourceReader.fromString(source)));
        Program program = parser.parseProgram();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(program);
        return interpreter;
    }

    private Map<String, RuntimeValue> globals(Interpreter interpreter) {
        return interpreter.globalVariables();
    }

    private int intGlobal(Interpreter interpreter, String name) {
        RuntimeValue value = globals(interpreter).get(name);
        assertNotNull(value, "Missing global variable: " + name);
        assertInstanceOf(IntValue.class, value);
        return ((IntValue) value).value();
    }

    private double doubleGlobal(Interpreter interpreter, String name) {
        RuntimeValue value = globals(interpreter).get(name);
        assertNotNull(value, "Missing global variable: " + name);
        assertInstanceOf(DoubleValue.class, value);
        return ((DoubleValue) value).value();
    }

    private String stringGlobal(Interpreter interpreter, String name) {
        RuntimeValue value = globals(interpreter).get(name);
        assertNotNull(value, "Missing global variable: " + name);
        assertInstanceOf(StringValue.class, value);
        return ((StringValue) value).value();
    }

    private boolean boolGlobal(Interpreter interpreter, String name) {
        RuntimeValue value = globals(interpreter).get(name);
        assertNotNull(value, "Missing global variable: " + name);
        assertInstanceOf(BoolValue.class, value);
        return ((BoolValue) value).value();
    }

    @Test
    void shouldExecuteSimpleArithmeticProgram() {
        Interpreter interpreter = run("""
                x = 2
                y = (x + 2) * 2
                """);

        assertEquals(2, intGlobal(interpreter, "x"));
        assertEquals(8, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldExecuteIfTrueBranchProgram() {
        Interpreter interpreter = run("""
                x = 20
                if x > 10 then y = 100 else y = 0
                """);

        assertEquals(20, intGlobal(interpreter, "x"));
        assertEquals(100, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldExecuteIfFalseBranchProgram() {
        Interpreter interpreter = run("""
                x = 5
                if x > 10 then y = 100 else y = 0, x = 4
                """);

        assertEquals(4, intGlobal(interpreter, "x"));
        assertEquals(0, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldExecuteWhileWithNestedIfProgram() {
        Interpreter interpreter = run("""
                x = 0
                y = 0
                while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
                """);

        assertEquals(3, intGlobal(interpreter, "x"));
        assertEquals(11, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldExecuteSimpleFunctionCallProgram() {
        Interpreter interpreter = run("""
                fun add(a, b) { return a + b }
                four = add(2, 2)
                """);

        assertEquals(4, intGlobal(interpreter, "four"));
    }

    @Test
    void shouldExecuteRecursiveFactorialProgram() {
        Interpreter interpreter = run("""
                fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
                a = fact_rec(5)
                """);

        assertEquals(120, intGlobal(interpreter, "a"));
    }

    @Test
    void shouldExecuteIterativeFactorialProgram() {
        Interpreter interpreter = run("""
                fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
                b = fact_iter(5)
                """);

        assertEquals(120, intGlobal(interpreter, "b"));
    }

    @Test
    void shouldRespectOperatorPrecedence() {
        Interpreter interpreter = run("""
                x = 2 + 3 * 4
                y = (2 + 3) * 4
                """);

        assertEquals(14, intGlobal(interpreter, "x"));
        assertEquals(20, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldEvaluateUnaryMinus() {
        Interpreter interpreter = run("""
                x = -5
                y = -(-3)
                """);

        assertEquals(-5, intGlobal(interpreter, "x"));
        assertEquals(3, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldEvaluateLogicalNot() {
        Interpreter interpreter = run("""
                x = !false
                y = !true
                """);

        assertTrue(boolGlobal(interpreter, "x"));
        assertFalse(boolGlobal(interpreter, "y"));
    }

    @Test
    void shouldEvaluateLogicalOperators() {
        Interpreter interpreter = run("""
                a = true && true
                b = true && false
                c = false || true
                d = false || false
                """);

        assertTrue(boolGlobal(interpreter, "a"));
        assertFalse(boolGlobal(interpreter, "b"));
        assertTrue(boolGlobal(interpreter, "c"));
        assertFalse(boolGlobal(interpreter, "d"));
    }

    @Test
    void shouldEvaluateEqualityAndInequality() {
        Interpreter interpreter = run("""
                a = 5 == 5
                b = 5 != 6
                c = "abc" == "abc"
                d = true != false
                """);

        assertTrue(boolGlobal(interpreter, "a"));
        assertTrue(boolGlobal(interpreter, "b"));
        assertTrue(boolGlobal(interpreter, "c"));
        assertTrue(boolGlobal(interpreter, "d"));
    }

    @Test
    void shouldEvaluateComparisons() {
        Interpreter interpreter = run("""
                a = 1 < 2
                b = 2 <= 2
                c = 3 > 2
                d = 4 >= 4
                e = "abc" < "abd"
                """);

        assertTrue(boolGlobal(interpreter, "a"));
        assertTrue(boolGlobal(interpreter, "b"));
        assertTrue(boolGlobal(interpreter, "c"));
        assertTrue(boolGlobal(interpreter, "d"));
        assertTrue(boolGlobal(interpreter, "e"));
    }

    @Test
    void shouldSupportStringConcatenation() {
        Interpreter interpreter = run("""
                name = "Diana"
                msg = "Hello, " + name
                """);

        assertEquals("Diana", stringGlobal(interpreter, "name"));
        assertEquals("Hello, Diana", stringGlobal(interpreter, "msg"));
    }

    @Test
    void shouldSupportDoubleArithmetic() {
        Interpreter interpreter = run("""
                x = 1.5 + 2.5
                y = 6.0 / 2.0
                z = 3.0 * 2.0
                """);

        assertEquals(4.0, doubleGlobal(interpreter, "x"));
        assertEquals(3.0, doubleGlobal(interpreter, "y"));
        assertEquals(6.0, doubleGlobal(interpreter, "z"));
    }

    @Test
    void shouldHandleSequenceStatements() {
        Interpreter interpreter = run("""
                x = 1, y = 2, z = x + y
                """);

        assertEquals(1, intGlobal(interpreter, "x"));
        assertEquals(2, intGlobal(interpreter, "y"));
        assertEquals(3, intGlobal(interpreter, "z"));
    }


    @Test
    void shouldCallFunctionWithExpressionArguments() {
        Interpreter interpreter = run("""
                fun add(a, b) { return a + b }
                x = add(1 + 2, 3 + 4)
                """);

        assertEquals(10, intGlobal(interpreter, "x"));
    }

    @Test
    void shouldSupportNestedFunctionCalls() {
        Interpreter interpreter = run("""
                fun add(a, b) { return a + b }
                x = add(add(1, 2), add(3, 4))
                """);

        assertEquals(10, intGlobal(interpreter, "x"));
    }

    @Test
    void shouldUseFunctionParameterLocally() {
        Interpreter interpreter = run("""
                fun identity(x) { return x }
                y = identity(7)
                """);

        assertEquals(7, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldReturnFromInsideIfInFunction() {
        Interpreter interpreter = run("""
                fun absLike(x) { if x < 0 then return -x else return x }
                a = absLike(-5)
                b = absLike(4)
                """);

        assertEquals(5, intGlobal(interpreter, "a"));
        assertEquals(4, intGlobal(interpreter, "b"));
    }

    @Test
    void shouldReturnFromInsideWhileInFunction() {
        Interpreter interpreter = run("""
                fun firstPositive(n) {
                    while true do if n > 0 then return n else n = n + 1
                }
                x = firstPositive(-3)
                """);

        assertEquals(1, intGlobal(interpreter, "x"));
    }

    @Test
    void shouldSupportListLiteral() {
        Interpreter interpreter = run("""
                values = [1, 2, 3]
                """);

        RuntimeValue value = globals(interpreter).get("values");
        assertNotNull(value);
        assertInstanceOf(ListValue.class, value);

        ListValue list = (ListValue) value;
        assertEquals(3, list.elements().size());
        assertInstanceOf(IntValue.class, list.elements().get(0));
        assertInstanceOf(IntValue.class, list.elements().get(1));
        assertInstanceOf(IntValue.class, list.elements().get(2));
    }

    @Test
    void shouldSupportEmptyListLiteral() {
        Interpreter interpreter = run("""
                values = []
                """);

        RuntimeValue value = globals(interpreter).get("values");
        assertNotNull(value);
        assertInstanceOf(ListValue.class, value);

        ListValue list = (ListValue) value;
        assertTrue(list.elements().isEmpty());
    }

    @Test
    void shouldSupportListSizeBuiltinPropertyCall() {
        Interpreter interpreter = run("""
                values = [1, 2, 3, 4]
                sizeValue = values.size()
                """);

        assertEquals(4, intGlobal(interpreter, "sizeValue"));
    }

    @Test
    void shouldSupportBuiltinIntConversionFromDouble() {
        Interpreter interpreter = run("""
                x = int(5.9)
                """);

        assertEquals(5, intGlobal(interpreter, "x"));
    }

    @Test
    void shouldSupportBuiltinIntConversionFromBool() {
        Interpreter interpreter = run("""
                x = int(true)
                y = int(false)
                """);

        assertEquals(1, intGlobal(interpreter, "x"));
        assertEquals(0, intGlobal(interpreter, "y"));
    }

    @Test
    void shouldSupportStringIntConversion() {
        Interpreter interpreter = run("""
                x = string_int(123)
                """);

        assertEquals("123", stringGlobal(interpreter, "x"));
    }

    @Test
    void shouldSupportStringDoubleConversion() {
        Interpreter interpreter = run("""
                x = string_double(3.5)
                """);

        assertEquals("3.5", stringGlobal(interpreter, "x"));
    }

    @Test
    void shouldSupportStringBoolConversion() {
        Interpreter interpreter = run("""
                x = string_bool(true)
                y = string_bool(false)
                """);

        assertEquals("true", stringGlobal(interpreter, "x"));
        assertEquals("false", stringGlobal(interpreter, "y"));
    }

    @Test
    void shouldSupportPrintBuiltin() {
        Interpreter interpreter = run("""
                print(1)
                print("abc")
                print(true)
                """);

        String output = interpreter.output();
        assertTrue(output.contains("1"));
        assertTrue(output.contains("abc"));
        assertTrue(output.contains("true"));
    }

    @Test
    void shouldAllowBooleanConditionInWhile() {
        Interpreter interpreter = run("""
                x = 0
                while x < 2 do x = x + 1
                """);

        assertEquals(2, intGlobal(interpreter, "x"));
    }

    @Test
    void shouldSupportBooleanLiteralInInfiniteLoopWithReturn() {
        Interpreter interpreter = run("""
                fun test() { while true do return 7 }
                x = test()
                """);

        assertEquals(7, intGlobal(interpreter, "x"));
    }

    @Test
    void shouldEvaluateComplexProgram() {
        Interpreter interpreter = run("""
                fun add(a, b) { return a + b }
                fun fact(n) { if n <= 0 then return 1 else return n * fact(n - 1) }
                x = add(2, 3)
                y = fact(4)
                z = (x + y) * 2
                """);

        assertEquals(5, intGlobal(interpreter, "x"));
        assertEquals(24, intGlobal(interpreter, "y"));
        assertEquals(58, intGlobal(interpreter, "z"));
    }

    @Test
    void shouldKeepFunctionInGlobalsButNotBreakVariableAssertions() {
        Interpreter interpreter = run("""
                fun add(a, b) { return a + b }
                x = 1
                """);

        assertTrue(globals(interpreter).containsKey("add"));
        assertEquals(1, intGlobal(interpreter, "x"));
    }
}