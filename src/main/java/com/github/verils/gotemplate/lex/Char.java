package com.github.verils.gotemplate.lex;

public final class Char {

    private Char() {
    }

    static boolean isSpace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    static boolean isNumeric(char ch) {
        return '0' <= ch && ch <= '9';
    }

    public static boolean isAlphabetic(char ch) {
        return ch == '_' || Character.isLetterOrDigit(ch);
    }
}
