package com.github.verils.gotemplate.parse;

public class BoolNode implements Node {

    private final boolean value;

    public BoolNode(String text) {
        this.value = Boolean.parseBoolean(text);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
