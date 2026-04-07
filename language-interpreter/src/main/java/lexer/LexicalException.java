package lexer;

public class LexicalException extends RuntimeException {

    private final String errorType;
    private final Position position;

    public LexicalException(String errorType, Position position, String message) {
        super("[" + errorType + "] line " + position.line() + ", column " + position.column() + ": " + message);
        this.errorType = errorType;
        this.position = position;
    }

    public String errorType() {
        return errorType;
    }

    public Position position() {
        return position;
    }
}