package interpreter;

import java.util.List;

public final class ListValue implements RuntimeValue {

    private final List<RuntimeValue> elements;

    public ListValue(List<RuntimeValue> elements) {
        this.elements = elements;
    }

    @Override
    public String typeName() {
        return "list";
    }

    @Override
    public Object value() {
        return elements;
    }

    public List<RuntimeValue> elements() {
        return List.copyOf(elements);
    }
}