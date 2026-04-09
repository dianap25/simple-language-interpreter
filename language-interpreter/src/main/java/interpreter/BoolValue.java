package interpreter;

public record BoolValue() implements RuntimeValue{
    @Override
    public String typeName() {
        return "bool";
    }
}
