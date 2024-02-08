package io.github.verils.gotemplate.internal;

public class Token {

    private final TokenType type;
    private final String val;
    private final int pos;
    private final int line;
    private final int column;

    public Token(TokenType type, String val, int pos, int line, int column) {
        this.type = type;
        this.val = val;
        this.pos = pos;
        this.line = line;
        this.column = column;
    }

    public TokenType type() {
        return type;
    }

    public String value() {
        return val;
    }

    public int pos() {
        return pos;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    @Override
    public String toString() {
        switch (type) {
            case EOF:
                return "EOF";
            case KEYWORD:
                return '<' + val + '>';
            case ERROR:
            default:
                return val;
        }
    }
}
