package com.github.verils.gotemplate.parse;

public class IdentifierNode implements Node {

    private final String identifier;

    public IdentifierNode(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
