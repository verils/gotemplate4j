package io.github.verils.gotemplate.runtime.simple.parse;

public class IdentifierNode implements Node {

    private final String identifier;

    public IdentifierNode(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
