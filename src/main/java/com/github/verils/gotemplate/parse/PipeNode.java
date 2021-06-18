package com.github.verils.gotemplate.parse;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PipeNode implements Node {

    private final String context;
    private final List<CommandNode> commands = new LinkedList<>();

    public PipeNode(String context) {
        this.context = context;
    }

    public void append(CommandNode commandNode) {
        commands.add(commandNode);
    }

    public void check() {
        if (commands.isEmpty()) {
            throw new TemplateParseException("missing value for " + context);
        }
    }

    @Override
    public String toString() {
        return commands.stream().map(Objects::toString).collect(Collectors.joining( " | "));
    }
}
