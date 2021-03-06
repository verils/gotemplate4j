package io.github.verils.gotemplate.runtime.simple.parse;

public class FieldNode implements Node {

    private final String[] identifiers;

    public FieldNode(String value) {
        this.identifiers = value.substring(1).split("\\.");
    }

    public String[] getIdentifiers() {
        return identifiers;
    }

    @Override
    public String toString() {
        return "." + String.join(".", identifiers);
    }
}
