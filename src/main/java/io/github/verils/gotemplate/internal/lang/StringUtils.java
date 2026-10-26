package io.github.verils.gotemplate.internal.lang;

public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Quote string with double-quote
     *
     * @param str string to be quoted
     * @return quoted string
     */
    public static String quote(String str) {
        return '"' + str + '"';
    }

    /**
     * Unquote string
     *
     * @param str string with quotes at the beginning and ending
     * @return unquoted string
     * @throws IllegalArgumentException when string is not quoted properly, such as: [<code>"hello, world</code>]
     */
    public static String unquote(String str) throws IllegalArgumentException {
        int length = str.length();
        if (length < 2) {
            throw new IllegalArgumentException("invalid syntax: " + str);
        }

        char quote = str.charAt(0);
        if (quote != str.charAt(length - 1)) {
            throw new IllegalArgumentException("invalid syntax: " + str);
        }

        String unquoted = str.substring(1, length - 1);
        if (quote == '`') {
            if (unquoted.contains("`")) {
                throw new IllegalArgumentException("invalid syntax: " + str);
            }
            if (unquoted.contains("\r")) {
                unquoted = unquoted.replace("\r", "");
            }
            return unquoted;
        }

        if (quote != '"' && quote != '\'') {
            throw new IllegalArgumentException("invalid syntax: " + str);
        }
        if (unquoted.contains("\n")) {
            throw new IllegalArgumentException("invalid syntax: " + str);
        }

        String decoded = decodeEscapes(unquoted, quote);
        if (quote == '\'' && (decoded.isEmpty() || decoded.codePointCount(0, decoded.length()) != 1)) {
            throw new IllegalArgumentException("character literal must contain exactly one code point: " + str);
        }
        return decoded;
    }

    static String decodeEscapes(String text, char quote) {
        StringBuilder result = new StringBuilder(text.length());
        int offset = 0;
        while (offset < text.length()) {
            char current = text.charAt(offset);
            if (current == quote) {
                throw invalidEscape("unescaped quote");
            }
            if (current == '\n') {
                throw invalidEscape("newline");
            }
            if (current != '\\') {
                int codePoint = text.codePointAt(offset);
                if (Character.getType(codePoint) == Character.SURROGATE) {
                    throw invalidEscape("unpaired surrogate");
                }
                result.appendCodePoint(codePoint);
                offset += Character.charCount(codePoint);
                continue;
            }

            if (offset + 1 >= text.length()) {
                throw invalidEscape("trailing backslash");
            }
            char escape = text.charAt(offset + 1);
            offset += 2;

            switch (escape) {
                case 'a':
                    result.append('\u0007');
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'v':
                    result.append('\u000B');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                case '\'':
                case '"':
                    if (quote != 0 && escape != quote) {
                        throw invalidEscape("\\" + escape);
                    }
                    result.append(escape);
                    break;
                case 'x':
                    result.append((char) (int) parseHex(text, offset, 2, "\\x"));
                    offset += 2;
                    break;
                case 'u':
                    result.appendCodePoint(parseUnicodeEscape(text, offset, 4, "\\u"));
                    offset += 4;
                    break;
                case 'U':
                    result.appendCodePoint(parseUnicodeEscape(text, offset, 8, "\\U"));
                    offset += 8;
                    break;
                default:
                    if (escape >= '0' && escape <= '7') {
                        int value = escape - '0';
                        if (offset + 1 >= text.length()) {
                            throw invalidEscape("octal escape");
                        }
                        for (int i = 0; i < 2; i++) {
                            char digit = text.charAt(offset + i);
                            if (digit < '0' || digit > '7') {
                                throw invalidEscape("octal escape");
                            }
                            value = (value << 3) | (digit - '0');
                        }
                        if (value > 255) {
                            throw invalidEscape("octal escape out of range");
                        }
                        offset += 2;
                        result.append((char) value);
                    } else {
                        throw invalidEscape("\\" + escape);
                    }
            }
        }
        return result.toString();
    }

    private static int parseUnicodeEscape(String text, int offset, int length, String escapeName) {
        long codePoint = parseHex(text, offset, length, escapeName);
        if (codePoint > Character.MAX_CODE_POINT
                || Character.getType((int) codePoint) == Character.SURROGATE) {
            throw invalidEscape(escapeName + " code point");
        }
        return (int) codePoint;
    }

    private static long parseHex(String text, int offset, int length, String escapeName) {
        if (offset + length > text.length()) {
            throw invalidEscape(escapeName + " escape");
        }
        long value = 0;
        for (int i = 0; i < length; i++) {
            int digit = Character.digit(text.charAt(offset + i), 16);
            if (digit < 0) {
                throw invalidEscape(escapeName + " escape");
            }
            value = (value << 4) | digit;
        }
        return value;
    }

    private static IllegalArgumentException invalidEscape(String detail) {
        return new IllegalArgumentException("invalid escape sequence: " + detail);
    }

}
