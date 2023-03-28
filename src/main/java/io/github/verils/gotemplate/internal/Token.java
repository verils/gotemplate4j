package io.github.verils.gotemplate.internal;

public class Token {

    private final TokenType type;
    private final String val;
    private final int pos;

    public Token(TokenType type, String val, int pos) {
        this.type = type;
        this.val = val;
        this.pos = pos;
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
