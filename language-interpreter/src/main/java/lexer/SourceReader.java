package lexer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceReader{

    private final Reader reader;
    private final int EOF = -1;
    private int currentChar;
    private int nextChar;
    private int line = 1;
    private int column = 1;


    public SourceReader(Reader reader) {
        this.reader = reader;
        try {
            this.currentChar = reader.read();
            this.nextChar = reader.read();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize source", e);
        }
    }

    public static SourceReader fromString(String input) {
        return new SourceReader(new StringReader(input));
    }


    public static SourceReader fromPath(Path path){
        try{
            return new SourceReader(Files.newBufferedReader(path, StandardCharsets.UTF_8));
        }catch(IOException e){
            throw new IllegalStateException("Cannot open file: " + path, e);
        }
    }


    public int get() {
        return currentChar;
    }

    public int getNext() {
        return nextChar;
    }


    public int read() {
        int result = currentChar;
        if(result == EOF){
            return EOF;
        }

        try{
            currentChar = nextChar;
            nextChar = reader.read();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read source", e);
        }

        if (result == '\r') {
            if (currentChar == '\n') {
                try {
                    currentChar = nextChar;
                    nextChar = reader.read();
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot read source", e);
                }
            }
            line++;
            column = 1;
        } else if (result == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return result;
    }

    public boolean isEOF() {
        return currentChar == EOF;
    }

    public Position position() {
        return new Position(line, column);
    }
}
