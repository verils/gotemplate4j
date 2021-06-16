package com.github.verils.gotemplate.lex;

final class Char {

    static final char EOF = (char) -1;
    static final char SPACE = ' ';
    static final char TAB = '\t';
    static final char RETURN = '\r';
    static final char NEW_LINE = '\n';
    static final char UNDERSCORE = '_';

    private Char() {
    }

    static boolean isSpace(char ch) {
        return ch == SPACE || ch == TAB || ch == RETURN || ch == NEW_LINE;
    }

    public static boolean isAscii(char ch) {
        return ch < 0x7F;
    }

    public static boolean isVisible(char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return !Character.isISOControl(ch)
                && block != null
                && block != Character.UnicodeBlock.SPECIALS;
    }

    static boolean isNumeric(char ch) {
        return '0' <= ch && ch <= '9';
    }

    static boolean isAlphabetic(char ch) {
        return ch == UNDERSCORE || Character.isLetterOrDigit(ch);
    }

    static boolean isValid(char ch, char... valid) {
        for (char c : valid) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    static boolean isValid(char ch, CharSequence valid) {
        return valid.chars().anyMatch(c -> c == ch);
    }
}
