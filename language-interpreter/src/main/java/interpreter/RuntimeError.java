package interpreter;

import lexer.Position;

public class RuntimeError extends RuntimeException {

    public RuntimeError(Position position, String message) {
        super("[RuntimeError] line " + position.line() + ", column " + position.column() + ": " + message);
    }
}