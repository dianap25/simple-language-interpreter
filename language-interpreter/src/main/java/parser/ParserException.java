package parser;

import lexer.Position;
import lexer.Token;

public class ParserException extends RuntimeException {
    private final Position position;

    public ParserException(Token token, String message) {
        super("[SyntaxError] line " + token.position().line()
                + ", column " + token.position().column()
                + ": " + message);
        this.position = token.position();
    }

    public Position position() {
        return position;
    }
}
