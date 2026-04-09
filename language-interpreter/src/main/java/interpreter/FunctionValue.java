package interpreter;

public sealed interface FunctionValue extends RuntimeValue permits UserFunction, BuiltinFunction {
    @Override
    default String typeName() {
        return "function";
    }
}