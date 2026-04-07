package lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LexerNegativeTests {

    @Test
    void failForInvalidIdentifierStartingWithUnderscore() {
        Lexer lexer = new Lexer(SourceReader.fromString("_test"));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidIdentifier", ex.errorType());
    }

    @Test
    void failForInvalidIdentifierStartingWithDigit() {
        Lexer lexer = new Lexer(SourceReader.fromString("123abc"));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidIdentifier", ex.errorType());
    }

    @Test
    void failForNumberFollowedByUnderscoreIdentifier() {
        Lexer lexer = new Lexer(SourceReader.fromString("123_test"));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidIdentifier", ex.errorType());
    }

    @Test
    void failForDoubleFollowedByIdentifier() {
        Lexer lexer = new Lexer(SourceReader.fromString("123.0abc"));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidIdentifier", ex.errorType());
    }

    @Test
    void failForUnterminatedStringAtEndOfInput() {
        Lexer lexer = new Lexer(SourceReader.fromString("\"abc"));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("UnterminatedString", ex.errorType());
    }

    @Test
    void failForUnterminatedStringAfterIdentifier() {
        Lexer lexer = new Lexer(SourceReader.fromString("abc \"test"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("UnterminatedString", ex.errorType());
    }

    @Test
    void failForNewlineInString() {
        Lexer lexer = new Lexer(SourceReader.fromString("\"abc\nxyz\""));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("NewlineInString", ex.errorType());
    }

    @Test
    void failForInvalidEscapeSequence() {
        Lexer lexer = new Lexer(SourceReader.fromString("\"\\x\""));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidEscapeSequence", ex.errorType());
    }

    @Test
    void failForInvalidCharacter() {
        Lexer lexer = new Lexer(SourceReader.fromString("x = y @ z"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());
        assertEquals(TokenType.ASSIGNMENT, lexer.nextToken().type());
        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidCharacter", ex.errorType());
        assertTrue(ex.getMessage().contains("@"));
    }

    @Test
    void failForSingleAmpersand() {
        Lexer lexer = new Lexer(SourceReader.fromString("a & b"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidCharacter", ex.errorType());
        assertTrue(ex.getMessage().contains("&"));
    }

    @Test
    void failForSinglePipe() {
        Lexer lexer = new Lexer(SourceReader.fromString("a | b"));

        assertEquals(TokenType.IDENTIFIER, lexer.nextToken().type());

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidCharacter", ex.errorType());
        assertTrue(ex.getMessage().contains("|"));
    }

    @Test
    void failForMalformedDoubleMissingFractionPart() {
        Lexer lexer = new Lexer(SourceReader.fromString("123."));

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("MalformedNumber", ex.errorType());
        assertTrue(ex.getMessage().contains("digit expected"));
    }

    @Test
    void failForIntegerTooLarge() {
        LexerConfig config = new LexerConfig(4096, 4096, 128, 64, 100, Double.MAX_VALUE);
        Lexer lexer = new Lexer(SourceReader.fromString("999"), config);

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("MalformedNumber", ex.errorType());
        assertTrue(ex.getMessage().contains("out of range"));
    }

    @Test
    void failForIdentifierTooLong() {
        LexerConfig config = new LexerConfig(4096, 4096, 5, 64, Integer.MAX_VALUE, Double.MAX_VALUE);
        Lexer lexer = new Lexer(SourceReader.fromString("abcdefgh"), config);

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("InvalidIdentifier", ex.errorType());
        assertTrue(ex.getMessage().contains("too long"));
    }

    @Test
    void failForStringTooLong() {
        LexerConfig config = new LexerConfig(5, 4096, 128, 64, Integer.MAX_VALUE, Double.MAX_VALUE);
        Lexer lexer = new Lexer(SourceReader.fromString("\"abcdef\""), config);

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("MalformedString", ex.errorType());
    }

    @Test
    void failForNumberWithTooManyDigits() {
        LexerConfig config = new LexerConfig(4096, 4096, 128, 3, Integer.MAX_VALUE, Double.MAX_VALUE);
        Lexer lexer = new Lexer(SourceReader.fromString("1234"), config);

        LexicalException ex = assertThrows(LexicalException.class, lexer::nextToken);
        assertEquals("MalformedNumber", ex.errorType());
        assertTrue(ex.getMessage().contains("too long"));
    }
}