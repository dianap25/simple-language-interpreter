package interpreter;

public final class StringValue implements RuntimeValue {

    private String value;
    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String typeName() {
        return "string";
    }

    @Override
    public String value() {
        return  value;
    }
}