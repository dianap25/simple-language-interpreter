package interpreter;

import java.util.LinkedHashMap;
import java.util.Map;

public class Environment {

    private final Environment parent;
    private final Map<String, RuntimeValue> values = new LinkedHashMap<>();

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String name, RuntimeValue value) {
        values.put(name, value);
    }

    public boolean existsInCurrentScope(String name) {
        return values.containsKey(name);
    }

    public boolean exists(String name) {
        if (values.containsKey(name)) {
            return true;
        }
        return parent != null && parent.exists(name);
    }

    public RuntimeValue get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new IllegalStateException("Undefined variable: " + name);
    }

    public void assign(String name, RuntimeValue value) {
        if (values.containsKey(name)) {
            RuntimeValue existing = values.get(name);
            validateTypeCompatibility(name, existing, value);
            values.put(name, value);
            return;
        }

        if (parent != null && parent.exists(name)) {
            parent.assign(name, value);
            return;
        }

        values.put(name, value);
    }

    public Map<String, RuntimeValue> snapshotCurrentScope() {
        return Map.copyOf(values);
    }

    private void validateTypeCompatibility(String name, RuntimeValue existing, RuntimeValue newValue) {
        if (!existing.getClass().equals(newValue.getClass())) {
            throw new IllegalStateException(
                    "Variable '" + name + "' cannot change type from "
                            + existing.typeName() + " to " + newValue.typeName()
            );
        }
    }
}