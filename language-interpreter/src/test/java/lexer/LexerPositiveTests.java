package lexer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerPositiveTests {

    @Test
    void readKeywordsAndIdentifiers() {
        Lexer lexer = new Lexer(SourceReader.fromString(
                "while do if then else fun return true false ident_2"
        ));

        assertEquals(TokenType.WHILE, lexer.nextToken().type());
        assertEquals(TokenType.DO, lexer.nextToken().type());
        assertEquals(TokenType.IF, lexer.nextToken().type());
        assertEquals(TokenType.THEN, lexer.nextToken().type());
        assertEquals(TokenType.ELSE, lexer.nextToken().type());
        assertEquals(TokenType.FUN, lexer.nextToken().type());
        assertEquals(TokenType.RETURN, lexer.nextToken().type());
        assertEquals(TokenType.TRUE, lexer.nextToken().type());
        assertEquals(TokenType.FALSE, lexer.nextToken().type());

        Token identifier = lexer.nextToken();
        assertEquals(TokenType.IDENTIFIER, identifier.type());
        assertEquals("ident_2", identifier.value());
    }

    @Test
    void readAllOperators() {
        Lexer lexer = new Lexer(SourceReader.fromString("+-*/ . , = == ! != < > <= >= && ||"));

        assertEquals(TokenType.PLUS, lexer.nextToken().type());
        assertEquals(TokenType.MINUS, lexer.nextToken().type());
        assertEquals(TokenType.TIMES, lexer.nextToken().type());
        assertEquals(TokenType.DIVIDE, lexer.nextToken().type());
        assertEquals(TokenType.DOT, lexer.nextToken().type());
        assertEquals(TokenType.COMMA, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.EQUAL_TO, lexer.nextToken().type());
        assertEquals(TokenType.LOGICAL_NOT, lexer.nextToken().type());
        assertEquals(TokenType.NOT_EQUAL_TO, lexer.nextToken().type());
        assertEquals(TokenType.LESS_THAN, lexer.nextToken().type());
        assertEquals(TokenType.GREATER_THAN, lexer.nextToken().type());
        assertEquals(TokenType.LESS_THAN_EQUAL, lexer.nextToken().type());
        assertEquals(TokenType.GREATER_THAN_EQUAL, lexer.nextToken().type());
        assertEquals(TokenType.LOGICAL_AND, lexer.nextToken().type());
        assertEquals(TokenType.LOGICAL_OR, lexer.nextToken().type());
    }

    @Test
    void readAllBrackets() {
        Lexer lexer = new Lexer(SourceReader.fromString("( ) { } [ ]"));

        assertEquals(TokenType.OPEN_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_CURLY_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_CURLY_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_SQUARE_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_SQUARE_BRACKETS, lexer.nextToken().type());
    }

    @Test
    void readInteger() {
        Lexer lexer = new Lexer(SourceReader.fromString("123 45 32"));

        Token first = lexer.nextToken();
        assertEquals(TokenType.INT_LITERAL, first.type());
        assertEquals(123, first.value());

        Token second = lexer.nextToken();
        assertEquals(TokenType.INT_LITERAL, second.type());
        assertEquals(45, second.value());

        Token third = lexer.nextToken();
        assertEquals(TokenType.INT_LITERAL, third.type());
        assertEquals(32, third.value());
    }

    @Test
    void readDouble() {
        Lexer lexer = new Lexer(SourceReader.fromString("123.45 0.45 3.2"));

        Token first = lexer.nextToken();
        assertEquals(TokenType.DOUBLE_LITERAL, first.type());
        assertEquals(123.45, first.value());

        Token second = lexer.nextToken();
        assertEquals(TokenType.DOUBLE_LITERAL, second.type());
        assertEquals(0.45, second.value());

        Token third = lexer.nextToken();
        assertEquals(TokenType.DOUBLE_LITERAL, third.type());
        assertEquals(3.2, third.value());
    }

    @Test
    void readString() {
        Lexer lexer = new Lexer(SourceReader.fromString("\"test\" \"a\\n\\t\\\"b\""));

        Token first = lexer.nextToken();
        assertEquals(TokenType.STRING_LITERAL, first.type());
        assertEquals("test", first.value());

        Token second = lexer.nextToken();
        assertEquals(TokenType.STRING_LITERAL, second.type());
        assertEquals("a\n\t\"b", second.value());
    }

    @Test
    void readEmptyString() {
        Lexer lexer = new Lexer(SourceReader.fromString("\"\""));

        Token token = lexer.nextToken();
        assertEquals(TokenType.STRING_LITERAL, token.type());
        assertEquals("", token.value());
    }

    @Test
    void readEscapedBackslash() {
        Lexer lexer = new Lexer(SourceReader.fromString("\"a\\\\b\""));

        Token token = lexer.nextToken();
        assertEquals(TokenType.STRING_LITERAL, token.type());
        assertEquals("a\\b", token.value());
    }

    @Test
    void readPunctuation() {
        Lexer lexer = new Lexer(SourceReader.fromString("== != <= >= && || ! = . , ( ) { } [ ]"));

        assertEquals(TokenType.EQUAL_TO, lexer.nextToken().type());
        assertEquals(TokenType.NOT_EQUAL_TO, lexer.nextToken().type());
        assertEquals(TokenType.LESS_THAN_EQUAL, lexer.nextToken().type());
        assertEquals(TokenType.GREATER_THAN_EQUAL, lexer.nextToken().type());
        assertEquals(TokenType.LOGICAL_AND, lexer.nextToken().type());
        assertEquals(TokenType.LOGICAL_OR, lexer.nextToken().type());
        assertEquals(TokenType.LOGICAL_NOT, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.DOT, lexer.nextToken().type());
        assertEquals(TokenType.COMMA, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_CURLY_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_CURLY_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_SQUARE_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_SQUARE_BRACKETS, lexer.nextToken().type());
    }

    @Test
    void skipCommentsAndTrackPosition() {
        Lexer lexer = new Lexer(SourceReader.fromString("// comment\n  return"));
        Token token = lexer.nextToken();

        assertEquals(TokenType.RETURN, token.type());
        assertEquals(new Position(2, 3), token.position());
    }

    @Test
    void readMultipleOperatorsWithoutSpace() {
        Lexer lexer = new Lexer(SourceReader.fromString("x=y+2"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.PLUS, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
    }

    @Test
    void trackPositionCorrectly() {
        Lexer lexer = new Lexer(SourceReader.fromString("abc\n  123"));

        Token first = lexer.nextToken();
        assertEquals(new Position(1, 1), first.position());

        Token second = lexer.nextToken();
        assertEquals(new Position(2, 3), second.position());
    }

    @Test
    void handleMultipleSpacesAndNewlines() {
        Lexer lexer = new Lexer(SourceReader.fromString("  \n  \t  x  =  5  "));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
    }

    @Test
    void readComplexExpression() {
        Lexer lexer = new Lexer(SourceReader.fromString("result = (a + b) * (c - d) / 2"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.PLUS, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.TIMES, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.MINUS, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.DIVIDE, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
    }

    @Test
    void readIfThenElseStatement() {
        Lexer lexer = new Lexer(SourceReader.fromString("if x > 10 then y = 1 else y = 0"));

        assertEquals(TokenType.IF, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.GREATER_THAN, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
        assertEquals(TokenType.THEN, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
        assertEquals(TokenType.ELSE, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
    }

    @Test
    void readWhileDoStatement() {
        Lexer lexer = new Lexer(SourceReader.fromString("while x < 3 do x = x + 1"));

        assertEquals(TokenType.WHILE, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.LESS_THAN, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
        assertEquals(TokenType.DO, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.PLUS, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
    }

    @Test
    void readFunctionDefinition() {
        Lexer lexer = new Lexer(SourceReader.fromString("fun add(a, b) { return a + b }"));

        assertEquals(TokenType.FUN, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.COMMA, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_PARENTHESES, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_CURLY_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.RETURN, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.PLUS, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_CURLY_BRACKETS, lexer.nextToken().type());
    }

    @Test
    void readListLiteral() {
        Lexer lexer = new Lexer(SourceReader.fromString("values = [1, 2, 3]"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.OPEN_SQUARE_BRACKETS, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
        assertEquals(TokenType.COMMA, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
        assertEquals(TokenType.COMMA, lexer.nextToken().type());
        assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());
        assertEquals(TokenType.CLOSE_SQUARE_BRACKETS, lexer.nextToken().type());
    }

    @Test
    void trackPositionAfterDoubleNewline() {
        Lexer lexer = new Lexer(SourceReader.fromString("a\n\nb"));

        Token first = lexer.nextToken();
        Token second = lexer.nextToken();

        assertEquals(new Position(1, 1), first.position());
        assertEquals(new Position(3, 1), second.position());
    }

    @Test
    void returnEOFForEmptyInput() {
        Lexer lexer = new Lexer(SourceReader.fromString(""));

        Token token = lexer.nextToken();
        assertEquals(TokenType.EOF, token.type());
    }

    @Test
    void returnEofRepeatedlyAfterEnd() {
        Lexer lexer = new Lexer(SourceReader.fromString("abc"));

        lexer.nextToken();
        assertEquals(TokenType.EOF, lexer.nextToken().type());
        assertEquals(TokenType.EOF, lexer.nextToken().type());
    }

    @Test
    void skipCommentAtEndOfFile() {
        Lexer lexer = new Lexer(SourceReader.fromString("// comment"));

        assertEquals(TokenType.EOF, lexer.nextToken().type());
    }

    @Test
    void readKeywordPrefixesAsIdentifiers() {
        Lexer lexer = new Lexer(SourceReader.fromString("ifx returnValue trueValue whileLoop funCall"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
    }

    @Test
    void shouldReadTokensFromFile() throws IOException {
        Path tempFile = Files.createTempFile("lexer-test", ".txt");
        Files.writeString(tempFile, "if abc 123\n123");

        try {
            Lexer lexer = new Lexer(SourceReader.fromPath(tempFile.toAbsolutePath()));

            assertEquals(TokenType.IF, lexer.nextToken().type());
            assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
            assertEquals(TokenType.INT_LITERAL, lexer.nextToken().type());

            Token first = lexer.nextToken();
            assertEquals(TokenType.INT_LITERAL, first.type());
            assertEquals(new Position(2, 1), first.position());

            assertEquals(TokenType.EOF, lexer.nextToken().type());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldSkipCommentFromFile() throws IOException {
        Path tempFile = Files.createTempFile("lexer-comment-test", ".txt");
        Files.writeString(tempFile, "// comment\nreturn");

        try {
            Lexer lexer = new Lexer(SourceReader.fromPath(tempFile.toAbsolutePath()));

            Token token = lexer.nextToken();
            assertEquals(TokenType.RETURN, token.type());
            assertEquals(TokenType.EOF, lexer.nextToken().type());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}