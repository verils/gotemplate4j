package io.github.verils.gotemplate.internal.ast;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PipeNode implements Node {

    private final String context;
    private final List<VariableNode> variables = new LinkedList<>();
    private final List<CommandNode> commands = new LinkedList<>();

    public PipeNode(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void append(VariableNode variableNode) {
        variables.add(variableNode);
    }

    public List<VariableNode> getVariables() {
        return variables;
    }

    public int getVariableCount() {
        return variables.size();
    }

    public void append(CommandNode commandNode) {
        commands.add(commandNode);
    }

    public List<CommandNode> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        String variableString = !variables.isEmpty() ?
                variables.stream().map(Objects::toString).collect(Collectors.joining(", ")) + " := " :
                "";
        return variableString + commands.stream().map(Objects::toString).collect(Collectors.joining(" | "));
    }
}
