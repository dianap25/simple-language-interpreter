package interpreter;

public final class DoubleValue implements RuntimeValue {

    private double value;
    public DoubleValue(double value) {
        this.value = value;
    }

    @Override
    public String typeName() {
        return "double";
    }

    @Override
    public Double value() {
        return value;
    }
}