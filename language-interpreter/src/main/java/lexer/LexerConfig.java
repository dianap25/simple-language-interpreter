package lexer;

public record LexerConfig(
        int maxStringLength,
        int maxCommentLength,
        int maxIdentifierLength,
        int maxDigitsNumber,
        int maxIntegerNumber,
        double maxDoubleNumber
) {

    public static LexerConfig defaultConfig() {
        return new LexerConfig(4096, 4096, 128, 64, 2147483647, Double.MAX_VALUE);
    }
}
