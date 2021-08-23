package com.github.verils.gotemplate.parse;

import java.util.LinkedList;
import java.util.List;

public class ChainNode implements Node {

    private final Node node;
    private final List<String> fields = new LinkedList<>();

    public ChainNode(Node node) {
        this.node = node;
    }

    public void append(String value) {
        if (!value.startsWith(".")) {
            throw new ParseException("not in field");
        }

        value = value.substring(1);
        if ("".equals(value)) {
            throw new ParseException("empty field");
        }

        fields.add(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (node instanceof PipeNode) {
            sb.append('(').append(node).append(')');
        } else {
            sb.append(node);
        }
        for (String field : fields) {
            sb.append('.').append(field);
        }
        return sb.toString();
    }
}
