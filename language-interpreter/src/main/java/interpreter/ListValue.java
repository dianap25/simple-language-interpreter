package interpreter;

import java.util.ArrayList;
import java.util.List;

public final class ListValue implements RuntimeValue {

    private final List<RuntimeValue> elements;

    public ListValue(List<RuntimeValue> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public List<RuntimeValue> elements() {
        return List.copyOf(elements);
    }

    @Override
    public String typeName() {
        return "list";
    }
}