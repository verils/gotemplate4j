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
     * 是否是数字0-9
     *
     * @param ch 字符
     * @return 满足定义则返回true
     */
    static boolean isNumeric(char ch) {
        return '0' <= ch && ch <= '9';
    }

    /**
     * 是否是下划线'_'或Unicode定义的Letter
     *
     * @param ch 字符
     * @return 满足定义则返回true
     */
    static boolean isAlphabetic(char ch) {
        return ch == UNDERSCORE || Character.isLetterOrDigit(ch);
    }

    /**
     * 检查字符是否是合法字符列表里定义的
     *
     * @param ch    字符
     * @param valid 合法字符列表
     * @return 如果字符在合法字符列表里就返回true
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
     * 检查字符是否是合法字符串里定义的
     *
     * @param ch    字符
     * @param valid 合法字符串
     * @return 如果字符在合法字符串里就返回true
     */
    static boolean isValid(char ch, CharSequence valid) {
        return valid.chars().anyMatch(c -> c == ch);
    }
}
