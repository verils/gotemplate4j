package com.github.verils.gotemplate.lex;

public final class Char {

    public static final char EOF = (char) -1;
    public static final char SPACE = ' ';
    public static final char TAB = '\t';
    public static final char RETURN = '\r';
    public static final char NEW_LINE = '\n';
    public static final char UNDERSCORE = '_';

    public static final String DECIMAL_DIGITS = "0123456789_";
    public static final String HEX_DIGITS = "0123456789abcdefABCDEF_";
    public static final String OCTET_DIGITS = "01234567_";
    public static final String BINARY_DIGITS = "01_";

    private Char() {
    }

    public static boolean isSpace(char ch) {
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

    /**
     * Is number a single digit in 0-9 ?
     *
     * @param ch Character to be checked
     * @return true if character is a single digit in 0-9
     */
    public static boolean isNumeric(char ch) {
        return '0' <= ch && ch <= '9';
    }

    /**
     * Is letter of '_' or a unicode letter or unicode digit?
     *
     * @param ch Character to be checked
     * @return true if letter is '_' or a unicode letter or unicode digit
     */
    public static boolean isAlphabetic(char ch) {
        return ch == UNDERSCORE || Character.isLetterOrDigit(ch);
    }

    /**
     * Check if single character is one of the characters you expect
     *
     * @param ch    Character to be checked
     * @param valid An array including characters which you expect
     * @return true if ch is what you want
     */
    public static boolean isValid(char ch, char... valid) {
        for (char c : valid) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if single character is one of the characters you expect
     * <p>
     * This is same with {@link Char#isValid(char, char...)} but accept a CharSequence to indicate expected characters
     *
     * @param ch    Character to be checked
     * @param valid A CharSequence including characters which you expect
     * @return true if ch is what you want
     */
    public static boolean isValid(char ch, CharSequence valid) {
        return valid.chars().anyMatch(c -> c == ch);
    }

    public static char unquotedChar(String str) {
        if (str.length() == 0) {
            throw new SyntaxException("invalid syntax: " + str);
        }
        if (str.charAt(0) != '\'' && str.charAt(2) != '\'') {
            throw new SyntaxException("invalid syntax: " + str);
        }
        return str.charAt(1);
    }
}
