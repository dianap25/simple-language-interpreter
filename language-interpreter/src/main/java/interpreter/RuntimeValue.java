package interpreter;

public sealed interface RuntimeValue permits
        IntValue,
        DoubleValue,
        BoolValue,
        StringValue,
        ListValue
{
    String typeName();
}
