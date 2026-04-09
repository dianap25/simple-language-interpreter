package interpreter;

import java.util.List;

public interface CallableValue {
    RuntimeValue call(Interpreter interpreter, List<RuntimeValue> arguments);
    int arity();
}