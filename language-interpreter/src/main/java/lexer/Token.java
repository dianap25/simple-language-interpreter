package lexer;

public record Token(
        TokenType type,
        Object value,
        Position position
) {}
