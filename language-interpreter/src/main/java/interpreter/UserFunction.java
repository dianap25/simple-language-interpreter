package interpreter;

import ast.Stmt;

import java.util.List;

public record UserFunction(
        String name,
        List<String> parameters,
        Stmt.Block body
) implements FunctionValue, CallableValue {

    @Override
    public RuntimeValue call(Interpreter interpreter, List<RuntimeValue> arguments) {
        return interpreter.callUserFunction(this, arguments);
    }

    @Override
    public int arity() {
        return parameters.size();
    }
}