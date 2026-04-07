package lexer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Lexer {
    private static final Map<String, TokenType> KEYWORDS = createKeywords();
    private final SourceReader sourceReader;
    private final LexerConfig config;

    public Lexer(SourceReader sourceReader) {
        this(sourceReader, LexerConfig.defaultConfig());
    }

    public Lexer(SourceReader sourceReader, LexerConfig config) {
        this.sourceReader = sourceReader;
        this.config = config;
    }


    public Token nextToken() {
        skipWhitespaceAndComments();
        Position start = sourceReader.position();

        if(sourceReader.isEOF()){
            return new Token(TokenType.EOF, null, start);
        }

        int ch = sourceReader.get();

        if(Character.isDigit(ch)){
            return readNumber();
        }

        if(ch == '"'){
            return readString();
        }


        if(Character.isLetterOrDigit(ch) || ch == '_'){
            return readIdentifierOrKeyword();
        }


        return switch (ch){
            case '+' -> singleChar(TokenType.PLUS);
            case '-' -> singleChar(TokenType.MINUS);
            case '*' -> singleChar(TokenType.TIMES);
            case '/' -> singleChar(TokenType.DIVIDE);
            case '.' -> singleChar(TokenType.DOT);
            case ',' -> singleChar(TokenType.COMMA);
            case '(' -> singleChar(TokenType.OPEN_PARENTHESES);
            case ')' -> singleChar(TokenType.CLOSE_PARENTHESES);
            case '{' -> singleChar(TokenType.OPEN_CURLY_BRACKETS);
            case '}' -> singleChar(TokenType.CLOSE_CURLY_BRACKETS);
            case '[' -> singleChar(TokenType.OPEN_SQUARE_BRACKETS);
            case ']' -> singleChar(TokenType.CLOSE_SQUARE_BRACKETS);
            case '=' -> twoOrOneChar('=', TokenType.EQUAL_TO, TokenType.ASSIGNMENT);
            case '!' -> twoOrOneChar('=', TokenType.NOT_EQUAL_TO, TokenType.LOGICAL_NOT);
            case '<' -> twoOrOneChar('=', TokenType.LESS_THAN_EQUAL, TokenType.LESS_THAN);
            case '>' -> twoOrOneChar('=', TokenType.GREATER_THAN_EQUAL, TokenType.GREATER_THAN);
            case '&' -> requireDouble('&', TokenType.LOGICAL_AND, "invalid character '&'");
            case '|' -> requireDouble('|', TokenType.LOGICAL_OR, "invalid character '|'");
            default -> throw new LexicalException(
                    "InvalidCharacter",
                    start,
                    "invalid character '" + printableChar(ch) + "'"
            );
        };
    }


    private Token readNumber() {
        Position start = sourceReader.position();
        NumberPart numberPart = readNumericPart(start, 0);
        long integerPart = numberPart.numericPart();
        int digitsCounter = numberPart.numberOfDigits();

        if (!sourceReader.isEOF() && sourceReader.get() == '.') {
            sourceReader.read();

            if (sourceReader.isEOF() || !Character.isDigit(sourceReader.get())) {
                throw new LexicalException("MalformedNumber", start, "digit expected after '.'");
            }

            NumberPart secondNumberPart = readNumericPart(start, digitsCounter);
            long fractionPart = secondNumberPart.numericPart();
            double divisor = secondNumberPart.divisor();

            // Reject cases 12.3abc or 12.3_test
            validateNoIdentifierAfterNumber(start);

            double value = integerPart + fractionPart / divisor;
            if (value > config.maxDoubleNumber()) {
                throw new LexicalException("MalformedNumber", start, "numeric literal out of range");
            }

            return new Token(TokenType.DOUBLE_LITERAL, value, start);
        }

        // Reject cases  123abc or 123_test
        validateNoIdentifierAfterNumber(start);

        if (integerPart > config.maxIntegerNumber()) {
            throw new LexicalException("MalformedNumber", start, "numeric literal out of range");
        }

        return new Token(TokenType.INT_LITERAL, (int) integerPart, start);
    }

    private NumberPart readNumericPart(Position start, int digitsCounterValue ) {
        long numericPart = 0;
        int digitsCounter = digitsCounterValue;
        double divisor = 1.0;

        while (!sourceReader.isEOF() && Character.isDigit(sourceReader.get())) {
            int digit = Character.digit(sourceReader.read(), 10);
            digitsCounter++;

            if (digitsCounter > config.maxDigitsNumber()) {
                throw new LexicalException("MalformedNumber", start, "numeric literal too long");
            }

            numericPart = numericPart * 10 + digit;
            divisor *= 10.0;
        }
        return new NumberPart(numericPart, digitsCounter, divisor);
    }

    private void validateNoIdentifierAfterNumber(Position start) {
        if (!sourceReader.isEOF() && (Character.isLetter(sourceReader.get()) || sourceReader.get() == '_')) {
            throw new LexicalException(
                    "InvalidIdentifier",
                    start,
                    "identifier cannot start with digit"
            );
        }
    }

    private Token readString(){
        Position start = sourceReader.position();
        sourceReader.read();

        StringBuilder string = new StringBuilder();
        while(!sourceReader.isEOF()){
            int ch = sourceReader.read();

            if(ch == '"'){
                return new Token(TokenType.STRING_LITERAL, string.toString(), start);
            }

            if (ch == '\n' || ch == '\r') {
                throw new LexicalException("NewlineInString", start, "newline in string literal");
            }

            if(ch == '\\'){
                if(sourceReader.isEOF()){
                    throw new LexicalException("UnterminatedString", start, "unterminated string literal");
                }
                int escaped = sourceReader.read();
                switch(escaped){
                    case '"' -> string.append('"');
                    case '\\' -> string.append('\\');
                    case 'n' -> string.append('\n');
                    case 't' -> string.append('\t');
                    default -> throw new LexicalException(
                            "InvalidEscapeSequence",
                            sourceReader.position(),
                            "invalid escape sequence '\\" + printableChar(escaped) + "'");
                }
            }else{
                string.append((char) ch);
            }

            if(string.length() > config.maxStringLength()){
                throw new LexicalException("MalformedString", start, "string too long");
            }
        }
        throw new LexicalException("UnterminatedString", start, "unterminated string literal");
    }


    private Token readIdentifierOrKeyword(){
        Position start = sourceReader.position();

        StringBuilder identifier = new StringBuilder();
        while(!sourceReader.isEOF()){
            int ch = sourceReader.get();
            if(Character.isLetterOrDigit(ch) || ch == '_'){
                if(identifier.isEmpty() && (ch == '_' || Character.isDigit(ch))){
                    throw new LexicalException("InvalidIdentifier", start, "identifier cannot start with " + ch);
                } else if(identifier.length() >= config.maxIdentifierLength()){
                    throw new LexicalException("InvalidIdentifier", start, "identifier too long");
                }
                identifier.append((char) sourceReader.read());
            }else{
                break;
            }
        }

        String text = identifier.toString();
        TokenType tokenType = KEYWORDS.get(text);
        return new Token(Objects.requireNonNullElse(tokenType, TokenType.IDENTIFIER), text, start);
    }

    private void skipWhitespaceAndComments(){
        boolean progress;

        do{
            progress = false;

            while(!sourceReader.isEOF() && Character.isWhitespace(sourceReader.get())){
                sourceReader.read();
                progress = true;
            }

            if(!sourceReader.isEOF() && sourceReader.get() == '/' && sourceReader.getNext() == '/'){
                int length = 0;
                while (!sourceReader.isEOF() && sourceReader.get() != '\n' &&  sourceReader.get() != '\r'){
                    sourceReader.read();
                    length++;

                    if(length > config.maxCommentLength()){
                        throw new LexicalException("UnterminatedComment", sourceReader.position(), "comment too long");
                    }
                }
                progress = true;
            }
        }while (progress);
    }


    private static String printableChar(int ch){
        return switch (ch){
            case '\n' -> "\\n";
            case '\t' -> "\\t";
            case '\r' -> "\\r";
            default -> Character.toString((char)ch);
        };
    }

    private Token singleChar (TokenType type){
        Position start = sourceReader.position();
        sourceReader.read();
        return new Token(type, null, start);
    }

    private Token twoOrOneChar(char expectedSecond, TokenType twoCharType, TokenType oneCharType){
        Position start = sourceReader.position();
        sourceReader.read();
        if(sourceReader.get() == expectedSecond){
            sourceReader.read();
            return new Token(twoCharType, null, start);
        }
        return new Token(oneCharType, null, start);
    }

    private Token requireDouble(char expectedSecond, TokenType type, String message){
        Position start = sourceReader.position();
        sourceReader.read();
        if(sourceReader.get() != expectedSecond){
            throw new LexicalException("InvalidCharacter", start, message);
        }
        sourceReader.read();
        return new Token(type, null, start);
    }

    private static Map<String, TokenType> createKeywords(){
        return Map.of(
                "if", TokenType.IF,
                "then", TokenType.THEN,
                "else", TokenType.ELSE,
                "while", TokenType.WHILE,
                "do", TokenType.DO,
                "fun", TokenType.FUN,
                "return", TokenType.RETURN,
                "true", TokenType.TRUE,
                "false",  TokenType.FALSE
        );
    }

}
