package lexer;

public record NumberPart(
        long numericPart,
        int numberOfDigits,
        double divisor
) {
}
