package interpreter;

public final class IntValue implements RuntimeValue {
    private int value;

    public IntValue(int value) {
        this.value = value;
    }
    @Override
    public String typeName() {
        return "int";
    }

    @Override
    public Integer value() {
        return value;
    }
}