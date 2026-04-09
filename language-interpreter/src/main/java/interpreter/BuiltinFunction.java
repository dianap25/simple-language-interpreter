package interpreter;

import java.util.List;
import java.util.function.Function;

public final class BuiltinFunction implements FunctionValue, CallableValue {

    private final String name;
    private final int arity;
    private final Function<List<RuntimeValue>, RuntimeValue> implementation;

    public BuiltinFunction(String name, int arity, Function<List<RuntimeValue>, RuntimeValue> implementation) {
        this.name = name;
        this.arity = arity;
        this.implementation = implementation;
    }

    @Override
    public RuntimeValue call(Interpreter interpreter, List<RuntimeValue> arguments) {
        return implementation.apply(arguments);
    }

    @Override
    public int arity() {
        return arity;
    }

    public String name() {
        return name;
    }
}