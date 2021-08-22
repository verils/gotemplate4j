package com.github.verils.gotemplate.parse;

public class WithNode implements Node {

    private PipeNode pipeNode;
    private ListNode ifListNode;
    private ListNode elseListNode;

    public PipeNode getPipeNode() {
        return pipeNode;
    }

    public void setPipeNode(PipeNode pipeNode) {
        this.pipeNode = pipeNode;
    }

    @Override
    public String toString() {
        return "";
    }
}
