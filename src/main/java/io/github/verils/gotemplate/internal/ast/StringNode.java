package io.github.verils.gotemplate.internal.ast;

import io.github.verils.gotemplate.internal.lang.StringEscapeUtils;
import io.github.verils.gotemplate.internal.lang.StringUtils;

public class StringNode implements Node {

    private final String origin;
    private final String text;

    public StringNode(String origin) {
        this.origin = origin;
        String unquoted = StringUtils.unquote(origin);
        this.text = isRawString() ? unquoted : StringEscapeUtils.unescape(unquoted);
    }

    public String getText() {
        return text;
    }

    public boolean isRawString() {
        return origin.startsWith("`");
    }

    @Override
    public String toString() {
        return origin;
    }
}
