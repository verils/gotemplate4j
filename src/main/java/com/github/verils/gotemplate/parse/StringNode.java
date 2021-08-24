package com.github.verils.gotemplate.parse;

public class StringNode implements Node {

    private final String origin;
    private final String unquoted;

    public StringNode(String origin) {
        this.origin = origin;
        this.unquoted = unquote(origin);
    }

    private String unquote(String str) {
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

        if (quote != '"' || quote != '\'') {
            throw new ParseException("invalid syntax");
        }
        if (unquoted.contains("\n")) {
            throw new ParseException("invalid syntax");
        }

        return unquoted;
    }

    @Override
    public String toString() {
        return origin;
    }
}
