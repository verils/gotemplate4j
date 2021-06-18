package com.github.verils.gotemplate.parse;

import java.util.List;

public class FieldNode implements Node {

    private final String[] identifiers;

    public FieldNode(String value) {
        this.identifiers = value.substring(1).split("\\.");
    }

    @Override
    public String toString() {
        return "." + String.join(".", identifiers);
    }
}
