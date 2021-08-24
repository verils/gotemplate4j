package com.github.verils.gotemplate.parse;

import java.util.LinkedList;
import java.util.List;

public class CommandNode implements Node {

    private final List<Node> arguments = new LinkedList<>();

    public void append(Node node) {
        arguments.add(node);
    }

    public Node getLastArgument() {
        return arguments.get(arguments.size() - 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            Node node = arguments.get(i);
            if (node instanceof PipeNode) {
                sb.append('(').append(node).append(')');
            } else {
                sb.append(node);
            }
        }
        return sb.toString();
    }
}
