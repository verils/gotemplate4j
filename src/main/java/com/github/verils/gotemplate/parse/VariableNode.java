package com.github.verils.gotemplate.parse;

public class VariableNode implements Node {

    private final String[] identifiers;

    public VariableNode(String value) {
        this.identifiers = value.split("\\.");
    }

    @Override
    public String toString() {
        return String.join(".", identifiers);
    }
}
