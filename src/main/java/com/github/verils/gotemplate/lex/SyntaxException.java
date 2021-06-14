package com.github.verils.gotemplate.lex;

public class SyntaxException extends RuntimeException {

    public SyntaxException() {
        super();
    }

    public SyntaxException(String message) {
        super(message);
    }

    public SyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
