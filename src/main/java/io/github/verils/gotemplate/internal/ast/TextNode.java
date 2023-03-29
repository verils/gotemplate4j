package io.github.verils.gotemplate.internal.ast;

public class TextNode implements Node {

    private final String text;

    public TextNode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return '"' + text + '"';
    }
}
