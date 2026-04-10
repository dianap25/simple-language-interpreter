package interpreter;


public sealed interface RuntimeValue permits
        IntValue,
        DoubleValue,
        StringValue,
        BoolValue,
        ListValue,
        FunctionValue {

    String typeName();

    Object value();
}