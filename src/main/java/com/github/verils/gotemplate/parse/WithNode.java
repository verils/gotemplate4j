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

    public void setIfListNode(ListNode listNode) {
        this.ifListNode = listNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{{with ").append(pipeNode).append("}}").append(ifListNode);
        if (elseListNode != null) {
            sb.append("{{else}}").append(elseListNode);
        }
        sb.append("{{end}}");
        return sb.toString();
    }
}
