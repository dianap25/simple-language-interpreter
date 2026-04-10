package interpreter;

import ast.Expr;
import ast.Program;
import ast.Stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Interpreter {

    private final Environment globals = new Environment(null);
    private Environment environment = globals;
    private final StringBuilder output = new StringBuilder();

    public Interpreter() {
        registerBuiltins();
    }

    public void interpret(Program program) {
        for (Stmt statement : program.statements()) {
            execute(statement);
        }
    }

    public Map<String, RuntimeValue> globalVariables() {
        return globals.snapshotCurrentScope();
    }

    public String output() {
        return output.toString();
    }

    public RuntimeValue callUserFunction(UserFunction function, List<RuntimeValue> arguments) {
        if (arguments.size() != function.parameters().size()) {
            throw new RuntimeException("Wrong number of arguments for function " + function.name());
        }

        Environment previousEnv = environment;
        environment = new Environment(globals);

        try {
            for (int i = 0; i < function.parameters().size(); i++) {
                environment.define(function.parameters().get(i), arguments.get(i));
            }

            try {
                execute(function.body());
            } catch (ReturnSignal signal) {
                return signal.value();
            }

            throw new RuntimeException("Function '" + function.name() + "' must return a value");
        } finally {
            environment = previousEnv;
        }
    }

    private void execute(Stmt stmt) {
        if (stmt instanceof Stmt.Assignment assignment) {
            RuntimeValue value = evaluate(assignment.value());
            assignOrDefine(assignment.name(), value);
            return;
        }

        if (stmt instanceof Stmt.Expression expressionStmt) {
            evaluate(expressionStmt.expression());
            return;
        }

        if (stmt instanceof Stmt.Return returnStmt) {
            RuntimeValue value = evaluate(returnStmt.value());
            throw new ReturnSignal(value);
        }

        if (stmt instanceof Stmt.Sequence sequence) {
            for (Stmt nested : sequence.statements()) {
                execute(nested);
            }
            return;
        }

        if (stmt instanceof Stmt.Block block) {
            executeBlock(block.statements(), new Environment(environment));
            return;
        }

        if (stmt instanceof Stmt.If ifStmt) {
            RuntimeValue condition = evaluate(ifStmt.condition());
            boolean boolValue = requireBool(condition, ifStmt.condition().position());
            if (boolValue) {
                execute(ifStmt.thenStmt());
            } else {
                execute(ifStmt.elseStmt());
            }
            return;
        }

        if (stmt instanceof Stmt.While whileStmt) {
            while (requireBool(evaluate(whileStmt.condition()), whileStmt.condition().position())) {
                execute(whileStmt.body());
            }
            return;
        }

        if (stmt instanceof Stmt.Function functionStmt) {
            UserFunction function = new UserFunction(
                    functionStmt.name(),
                    functionStmt.parameters(),
                    functionStmt.body()
            );
            environment.define(functionStmt.name(), function);
            return;
        }

        throw new IllegalStateException("Unsupported statement: " + stmt.getClass().getSimpleName());
    }

    private void executeBlock(List<Stmt> statements, Environment blockEnvironment) {
        Environment previousEnv = environment;
        environment = blockEnvironment;

        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            environment = previousEnv;
        }
    }

    private RuntimeValue evaluate(Expr expr) {
        if (expr instanceof Expr.IntLiteral literal) {
            return new IntValue(literal.value());
        }

        if (expr instanceof Expr.DoubleLiteral literal) {
            return new DoubleValue(literal.value());
        }

        if (expr instanceof Expr.StringLiteral literal) {
            return new StringValue(literal.value());
        }

        if (expr instanceof Expr.BoolLiteral literal) {
            return new BoolValue(literal.value());
        }

        if (expr instanceof Expr.Variable variable) {
            try {
                return environment.get(variable.name());
            } catch (IllegalStateException ex) {
                throw new RuntimeError(variable.position(), "undefined variable '" + variable.name() + "'");
            }
        }

        if (expr instanceof Expr.Grouping grouping) {
            return evaluate(grouping.expression());
        }

        if (expr instanceof Expr.ListLiteral listLiteral) {
            List<RuntimeValue> elements = new ArrayList<>();
            for (Expr element : listLiteral.elements()) {
                elements.add(evaluate(element));
            }
            return new ListValue(elements);
        }

        if (expr instanceof Expr.Unary unary) {
            RuntimeValue right = evaluate(unary.right());
            return evaluateUnary(unary, right);
        }

        if (expr instanceof Expr.Binary binary) {
            RuntimeValue left = evaluate(binary.left());
            RuntimeValue right = evaluate(binary.right());
            return evaluateBinary(binary, left, right);
        }

        if (expr instanceof Expr.PropertyAccess propertyAccess) {
            RuntimeValue target = evaluate(propertyAccess.target());
            return evaluatePropertyAccess(propertyAccess, target);
        }

        if (expr instanceof Expr.Call call) {
            RuntimeValue callee = evaluate(call.callee());

            if (!(callee instanceof CallableValue callable)) {
                throw new RuntimeError(call.position(), "expression is not callable");
            }

            List<RuntimeValue> arguments = new ArrayList<>();
            for (Expr argumentExpr : call.arguments()) {
                arguments.add(evaluate(argumentExpr));
            }

            if (arguments.size() != callable.arity()) {
                throw new RuntimeError(
                        call.position(),
                        "wrong number of arguments: expected " + callable.arity() + ", got " + arguments.size()
                );
            }

            return callable.call(this, arguments);
        }

        throw new IllegalStateException("Unsupported expression: " + expr.getClass().getSimpleName());
    }

    private RuntimeValue evaluateUnary(Expr.Unary unary, RuntimeValue right) {
        return switch (unary.operator()) {
            case "-" -> {
                if (right instanceof IntValue value) {
                    yield new IntValue(- value.value());
                }
                if (right instanceof DoubleValue value) {
                    yield new DoubleValue(- value.value());
                }
                throw new RuntimeError(unary.position(), "operator '-' is not defined for type " + right.typeName());
            }
            case "!" -> {
                if (right instanceof BoolValue value) {
                    yield new BoolValue(!value.value());
                }
                throw new RuntimeError(unary.position(), "operator '!' is not defined for type " + right.typeName());
            }
            default -> throw new RuntimeError(unary.position(), "unsupported unary operator '" + unary.operator() + "'");
        };
    }

    private RuntimeValue evaluateBinary(Expr.Binary binary, RuntimeValue left, RuntimeValue right) {
        String operator = binary.operator();

        if (operator.equals("+")) {
            if (left instanceof IntValue l && right instanceof IntValue r) {
                return new IntValue(l.value() + r.value());
            }
            if (left instanceof DoubleValue l && right instanceof DoubleValue r) {
                return new DoubleValue(l.value() + r.value());
            }
            if (left instanceof StringValue l && right instanceof StringValue r) {
                return new StringValue(l.value() + r.value());
            }
        }

        if (operator.equals("-")) {
            if (left instanceof IntValue l && right instanceof IntValue r) {
                return new IntValue(l.value() - r.value());
            }
            if (left instanceof DoubleValue l && right instanceof DoubleValue r) {
                return new DoubleValue(l.value() - r.value());
            }
        }

        if (operator.equals("*")) {
            if (left instanceof IntValue l && right instanceof IntValue r) {
                return new IntValue(l.value() * r.value());
            }
            if (left instanceof DoubleValue l && right instanceof DoubleValue r) {
                return new DoubleValue(l.value() * r.value());
            }
        }

        if (operator.equals("/")) {
            if (left instanceof IntValue l && right instanceof IntValue r) {
                if (r.value() == 0) {
                    throw new RuntimeError(binary.position(), "division by zero");
                }
                return new IntValue(l.value() / r.value());
            }
            if (left instanceof DoubleValue l && right instanceof DoubleValue r) {
                if (r.value() == 0.0) {
                    throw new RuntimeError(binary.position(), "division by zero");
                }
                return new DoubleValue(l.value() / r.value());
            }
        }

        if (operator.equals("&&")) {
            if (left instanceof BoolValue l && right instanceof BoolValue r) {
                return new BoolValue(l.value() && r.value());
            }
        }

        if (operator.equals("||")) {
            if (left instanceof BoolValue l && right instanceof BoolValue r) {
                return new BoolValue(l.value() || r.value());
            }
        }

        if (operator.equals("==")) {
            return new BoolValue(equalsValue(left, right));
        }

        if (operator.equals("!=")) {
            return new BoolValue(!equalsValue(left, right));
        }

        if (operator.equals("<")) {
            return compare(binary, left, right, "<");
        }

        if (operator.equals(">")) {
            return compare(binary, left, right, ">");
        }

        if (operator.equals("<=")) {
            return compare(binary, left, right, "<=");
        }

        if (operator.equals(">=")) {
            return compare(binary, left, right, ">=");
        }

        throw new RuntimeError(
                binary.position(),
                "operator '" + operator + "' is not defined for types "
                        + left.typeName() + " and " + right.typeName()
        );
    }

    private BoolValue compare(Expr.Binary binary, RuntimeValue left, RuntimeValue right, String operator) {
        if (left instanceof IntValue l && right instanceof IntValue r) {
            return new BoolValue(compareInts(l.value(), r.value(), operator));
        }
        if (left instanceof DoubleValue l && right instanceof DoubleValue r) {
            return new BoolValue(compareDoubles(l.value(), r.value(), operator));
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            int cmp = l.value().compareTo(r.value());
            return new BoolValue(compareInts(cmp, 0, operator));
        }
        if (left instanceof BoolValue l && right instanceof BoolValue r) {
            int li = l.value() ? 1 : 0;
            int ri = r.value() ? 1 : 0;
            return new BoolValue(compareInts(li, ri, operator));
        }

        throw new RuntimeError(
                binary.position(),
                "operator '" + operator + "' is not defined for types "
                        + left.typeName() + " and " + right.typeName()
        );
    }

    private boolean compareInts(int left, int right, String operator) {
        return switch (operator) {
            case "<" -> left < right;
            case ">" -> left > right;
            case "<=" -> left <= right;
            case ">=" -> left >= right;
            default -> throw new IllegalArgumentException("Unsupported comparison operator: " + operator);
        };
    }

    private boolean compareDoubles(double left, double right, String operator) {
        return switch (operator) {
            case "<" -> left < right;
            case ">" -> left > right;
            case "<=" -> left <= right;
            case ">=" -> left >= right;
            default -> throw new IllegalArgumentException("Unsupported comparison operator: " + operator);
        };
    }

    private boolean equalsValue(RuntimeValue left, RuntimeValue right) {
        if (!left.getClass().equals(right.getClass())) {
            return false;
        }

        if (left instanceof IntValue l && right instanceof IntValue r) {
            return l.value() == r.value();
        }
        if (left instanceof DoubleValue l && right instanceof DoubleValue r) {
            return l.value() == r.value();
        }
        if (left instanceof StringValue l && right instanceof StringValue r) {
            return l.value().equals(r.value());
        }
        if (left instanceof BoolValue l && right instanceof BoolValue r) {
            return l.value() == r.value();
        }
        if (left instanceof ListValue l && right instanceof ListValue r) {
            return l.elements().equals(r.elements());
        }

        return left.equals(right);
    }

    private RuntimeValue evaluatePropertyAccess(Expr.PropertyAccess propertyAccess, RuntimeValue target) {
        if (target instanceof ListValue listValue) {
            if (propertyAccess.property().equals("size")) {
                return new BuiltinFunction("size", 0, args -> new IntValue(listValue.elements().size()));
            }
        }

        throw new RuntimeError(
                propertyAccess.position(),
                "property '" + propertyAccess.property() + "' is not defined for type " + target.typeName()
        );
    }

    private boolean requireBool(RuntimeValue value, lexer.Position position) {
        if (value instanceof BoolValue boolValue) {
            return boolValue.value();
        }
        throw new RuntimeError(position, "condition must evaluate to bool");
    }

    private void assignOrDefine(String name, RuntimeValue value) {
        try {
            environment.assign(name, value);
        } catch (IllegalStateException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private void registerBuiltins() {
        globals.define("int", new BuiltinFunction("int", 1, args -> {
            RuntimeValue value = args.get(0);

            if (value instanceof IntValue intValue) {
                return intValue;
            }
            if (value instanceof DoubleValue doubleValue) {
                return new IntValue(doubleValue.value().intValue());
            }
            if (value instanceof BoolValue boolValue) {
                return new IntValue(boolValue.value() ? 1 : 0);
            }

            throw new RuntimeException("cannot convert " + value.typeName() + " to int");
        }));

        globals.define("string_int", new BuiltinFunction("string_int", 1, args -> {
            RuntimeValue value = args.get(0);
            if (value instanceof IntValue intValue) {
                return new StringValue(String.valueOf(intValue.value()));
            }
            throw new RuntimeException("string_int expects int");
        }));

        globals.define("string_double", new BuiltinFunction("string_double", 1, args -> {
            RuntimeValue value = args.get(0);
            if (value instanceof DoubleValue doubleValue) {
                return new StringValue(String.valueOf(doubleValue.value()));
            }
            throw new RuntimeException("string_double expects double");
        }));

        globals.define("string_bool", new BuiltinFunction("string_bool", 1, args -> {
            RuntimeValue value = args.get(0);
            if (value instanceof BoolValue boolValue) {
                return new StringValue(String.valueOf(boolValue.value()));
            }
            throw new RuntimeException("string_bool expects bool");
        }));

        globals.define("print", new BuiltinFunction("print", 1, args -> {
            RuntimeValue value = args.get(0);
            output.append(formatValue(value)).append(System.lineSeparator());
            return value;
        }));
    }

    private String formatValue(RuntimeValue value) {
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
                    .map(this::formatValue)
                    .reduce((a, b) -> a + ", " + b)
                    .map(s -> "[" + s + "]")
                    .orElse("[]");
        }
        if (value instanceof UserFunction function) {
            return "<function " + function.name() + ">";
        }
        if (value instanceof BuiltinFunction builtin) {
            return "<builtin " + builtin.name() + ">";
        }

        return value.toString();
    }
}