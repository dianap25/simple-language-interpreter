package interpreter;

public record StringValue() implements RuntimeValue{
    @Override
    public String typeName() {
        return "string";
    }
}
