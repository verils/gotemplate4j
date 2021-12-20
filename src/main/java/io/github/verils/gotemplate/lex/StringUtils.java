package io.github.verils.gotemplate.lex;

public final class StringUtils {

    private StringUtils() {
    }


    public static String quote(String str) {
        return '"' + str + '"';
    }

    public static String unquote(String str) {
        int length = str.length();
        if (length < 2) {
            throw new SyntaxException("invalid syntax: " + str);
        }

        char quote = str.charAt(0);
        if (quote != str.charAt(length - 1)) {
            throw new SyntaxException("invalid syntax: " + str);
        }

        String unquoted = str.substring(1, length - 1);
        if (quote == '`') {
            if (unquoted.contains("`")) {
                throw new SyntaxException("invalid syntax: " + str);
            }
            if (unquoted.contains("\r")) {
                unquoted = unquoted.replace("\r", "");
            }
            return unquoted;
        }

        if (quote != '"' && quote != '\'') {
            throw new SyntaxException("invalid syntax: " + str);
        }
        if (unquoted.contains("\n")) {
            throw new SyntaxException("invalid syntax: " + str);
        }

        return unquoted;
    }

}
