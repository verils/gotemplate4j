package com.github.verils.gotemplate.parse;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandNode implements Node {

    private final Deque<Node> arguments = new LinkedList<>();

    public void append(Node node) {
        arguments.add(node);
    }

    public Node getLastArgument() {
        return arguments.getLast();
    }

    @Override
    public String toString() {
        return arguments.stream().map(Objects::toString).collect(Collectors.joining(" "));
    }
}
