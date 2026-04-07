package lexer;

public enum TokenType {
    // keywords
    IF,
    THEN,
    ELSE,
    WHILE,
    DO,
    FUN,
    RETURN,
    TRUE,
    FALSE,

    // operators
    PLUS,
    MINUS,
    TIMES,
    DIVIDE,
    EQUAL_TO,
    NOT_EQUAL_TO,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,
    LOGICAL_AND,
    LOGICAL_OR,
    LOGICAL_NOT,
    ASSIGNMENT,

    // punctuation
    DOT,
    COMMA,
    OPEN_PARENTHESES,
    CLOSE_PARENTHESES,
    OPEN_CURLY_BRACKETS,
    CLOSE_CURLY_BRACKETS,
    OPEN_SQUARE_BRACKETS,
    CLOSE_SQUARE_BRACKETS,

    // identifiers and literals
    IDENTIFIER,
    INT_LITERAL,
    DOUBLE_LITERAL,
    STRING_LITERAL,

    EOF
}