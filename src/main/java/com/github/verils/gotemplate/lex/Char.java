package com.github.verils.gotemplate.lex;

final class Char {

    static final char EOF = (char) -1;
    static final char SPACE = ' ';
    static final char TAB = '\t';
    static final char RETURN = '\r';
    static final char NEW_LINE = '\n';
    static final char UNDERSCORE = '_';

    static final String DECIMAL_DIGITS = "0123456789_";
    static final String HEX_DIGITS = "0123456789abcdefABCDEF_";
    static final String OCTET_DIGITS = "01234567_";
    static final String BINARY_DIGITS = "01_";

    private Char() {
    }

    static boolean isSpace(char ch) {
        return ch == SPACE || ch == TAB || ch == RETURN || ch == NEW_LINE;
    }

    static boolean isAscii(char ch) {
        return ch < 0x7F;
    }

    static boolean isVisible(char ch) {
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
    static boolean isNumeric(char ch) {
        return '0' <= ch && ch <= '9';
    }

    /**
     * Is letter of '_' or a unicode letter or unicode digit?
     *
     * @param ch Character to be checked
     * @return true if letter is '_' or a unicode letter or unicode digit
     */
    static boolean isAlphabetic(char ch) {
        return ch == UNDERSCORE || Character.isLetterOrDigit(ch);
    }

    /**
     * Check if single character is one of the characters you expect
     *
     * @param ch    Character to be checked
     * @param valid An array including characters which you expect
     * @return true if ch is what you want
     */
    static boolean isValid(char ch, char... valid) {
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
    static boolean isValid(char ch, CharSequence valid) {
        return valid.chars().anyMatch(c -> c == ch);
    }
}
