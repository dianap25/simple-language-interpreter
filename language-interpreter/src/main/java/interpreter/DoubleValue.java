package interpreter;

import com.sun.jdi.Value;

public record DoubleValue() implements RuntimeValue {
    @Override
    public String typeName() {
        return "double";
    }
}
