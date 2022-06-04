package io.github.verils.gotemplate.runtime.simple.parse;

public class ActionNode implements Node {

    private PipeNode pipeNode;

    public PipeNode getPipeNode() {
        return pipeNode;
    }

    public void setPipeNode(PipeNode pipeNode) {
        this.pipeNode = pipeNode;
    }

    @Override
    public String toString() {
        String pipe = pipeNode != null ? pipeNode.toString() : "";
        return "{{" + pipe + "}}";
    }
}
