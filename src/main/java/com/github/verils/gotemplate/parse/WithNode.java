package com.github.verils.gotemplate.parse;

public class WithNode implements Node {

    private PipeNode pipeNode;

    public PipeNode getPipeNode() {
        return pipeNode;
    }

    public void setPipeNode(PipeNode pipeNode) {
        this.pipeNode = pipeNode;
    }
}
