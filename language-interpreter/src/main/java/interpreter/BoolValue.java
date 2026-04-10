package interpreter;

public final class BoolValue implements RuntimeValue {

    private boolean value;
    public BoolValue(boolean value) {
        this.value = value;
    }
    @Override
    public String typeName() {
        return "bool";
    }

    @Override
    public Boolean value() {
        return value;
    }
}