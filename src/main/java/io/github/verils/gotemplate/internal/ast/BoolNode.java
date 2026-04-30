package io.github.verils.gotemplate.internal.ast;

public class BoolNode implements Node {

    private final boolean value;

    public BoolNode(String text) {
        this.value = Boolean.parseBoolean(text);
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
