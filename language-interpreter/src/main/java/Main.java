import ast.Program;
import interpreter.BoolValue;
import interpreter.DoubleValue;
import interpreter.FunctionValue;
import interpreter.IntValue;
import interpreter.Interpreter;
import interpreter.ListValue;
import interpreter.RuntimeValue;
import interpreter.StringValue;
import lexer.Lexer;
import lexer.SourceReader;
import parser.Parser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        try {
            String source = readAllFromStdin();

            Lexer lexer = new Lexer(SourceReader.fromString(source));
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(program);

            printGlobals(interpreter.globalVariables());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static String readAllFromStdin() {
        return new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static void printGlobals(Map<String, RuntimeValue> globals) {
        for (Map.Entry<String, RuntimeValue> entry : globals.entrySet()) {
            String name = entry.getKey();
            RuntimeValue value = entry.getValue();

            if (value instanceof FunctionValue) {
                continue;
            }

            System.out.println(name + ": " + formatValue(value));
        }
    }

    private static String formatValue(RuntimeValue value) {
        if (value instanceof IntValue intValue) {
            return String.valueOf(intValue.value());
        }

        if (value instanceof DoubleValue doubleValue) {
            return String.valueOf(doubleValue.value());
        }

        if (value instanceof StringValue stringValue) {
            return stringValue.value();
        }

        if (value instanceof BoolValue boolValue) {
            return String.valueOf(boolValue.value());
        }

        if (value instanceof ListValue listValue) {
            return listValue.elements().stream()
                    .map(Main::formatValue)
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        return value.toString();
    }
}