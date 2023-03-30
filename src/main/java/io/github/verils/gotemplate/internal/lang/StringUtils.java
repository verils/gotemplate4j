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

        return unquoted;
    }

}
