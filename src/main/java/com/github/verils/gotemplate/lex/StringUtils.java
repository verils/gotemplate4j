package com.github.verils.gotemplate.lex;

import com.github.verils.gotemplate.parse.ParseException;

public final class StringUtils {

    private StringUtils() {
    }


    public static String quote(String str) {
        return '"' + str + '"';
    }

    public static String unquote(String str) {
        int length = str.length();
        if (length < 2) {
            throw new ParseException("invalid syntax");
        }

        char quote = str.charAt(0);
        if (quote != str.charAt(length - 1)) {
            throw new ParseException("invalid syntax");
        }

        String unquoted = str.substring(1, length - 1);
        if (quote == '`') {
            if (unquoted.contains("`")) {
                throw new ParseException("invalid syntax");
            }
            if (unquoted.contains("\r")) {
                unquoted = unquoted.replace("\r", "");
            }
            return unquoted;
        }

        if (quote != '"' && quote != '\'') {
            throw new ParseException("invalid syntax");
        }
        if (unquoted.contains("\n")) {
            throw new ParseException("invalid syntax");
        }

        return unquoted;
    }
}
