package interpreter;

public record IntValue() implements RuntimeValue{
    @Override
    public String typeName() {
        return "int";
    }
}
