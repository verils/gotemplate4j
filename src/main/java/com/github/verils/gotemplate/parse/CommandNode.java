package com.github.verils.gotemplate.parse;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandNode implements Node {

    private final List<Node> args = new LinkedList<>();

    public void append(Node node) {
        args.add(node);
    }

    @Override
    public String toString() {
        return args.stream().map(Objects::toString).collect(Collectors.joining(" "));
    }
}
