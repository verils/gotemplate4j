package com.github.verils.gotemplate.parse;

public class NumberNode implements Node {

    private final String text;

    public NumberNode(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
