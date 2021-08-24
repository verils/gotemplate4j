package com.github.verils.gotemplate.parse;

public class IfNode implements Node {

    private PipeNode pipeNode;
    private ListNode ifListNode;
    private ListNode elseListNode;

    public void setPipeNode(PipeNode pipeNode) {
        this.pipeNode = pipeNode;
    }

    public void setIfListNode(ListNode listNode) {
        this.ifListNode = listNode;
    }

    public void setElseListNode(ListNode elseListNode) {
        this.elseListNode = elseListNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{{if ").append(pipeNode).append("}}").append(ifListNode);
        if (elseListNode != null) {
            sb.append("{{else}}").append(elseListNode);
        }
        sb.append("{{end}}");
        return sb.toString();
    }
}
