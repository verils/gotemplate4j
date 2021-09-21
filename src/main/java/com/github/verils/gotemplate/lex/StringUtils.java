package com.github.verils.gotemplate.lex;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StringUtils {

    public static final Map<CharSequence, CharSequence> ESCAPE_MAP;
    public static final Map<CharSequence, CharSequence> UNESCAPE_MAP;

    static {
        final Map<CharSequence, CharSequence> initialMap = new LinkedHashMap<>();
        initialMap.put("\b", "\\b");
        initialMap.put("\n", "\\n");
        initialMap.put("\t", "\\t");
        initialMap.put("\f", "\\f");
        initialMap.put("\r", "\\r");
        ESCAPE_MAP = initialMap;

        final Map<CharSequence, CharSequence> invertMap = new LinkedHashMap<>();
        for (Map.Entry<CharSequence, CharSequence> entry : initialMap.entrySet()) {
            invertMap.put(entry.getValue(), entry.getKey());
        }
        UNESCAPE_MAP = invertMap;
    }

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

    public static String unescape(String input) {
        StringBuilder sb = new StringBuilder();

        int pos = 0;
        int len = input.length();
        while (pos < len) {
            int consumed;
//input.substring(pos, )

            pos++;
        }

        return input;
    }
}
