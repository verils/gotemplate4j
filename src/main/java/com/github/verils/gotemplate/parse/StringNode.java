package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.StringUtils;

public class StringNode implements Node {

    private final String origin;
    private final String unquoted;

    public StringNode(String origin) {
        this.origin = origin;
        this.unquoted = StringUtils.unquote(origin);
    }

    @Override
    public String toString() {
        return origin;
    }
}
