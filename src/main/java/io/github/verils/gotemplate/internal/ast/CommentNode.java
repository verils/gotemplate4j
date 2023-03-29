package io.github.verils.gotemplate.internal.ast;

public class CommentNode implements Node {

    private final String comment;

    public CommentNode(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return comment;
    }
}
