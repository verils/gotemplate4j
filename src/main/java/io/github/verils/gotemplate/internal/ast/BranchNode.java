package io.github.verils.gotemplate.internal.ast;

public abstract class BranchNode implements Node {

    private PipeNode pipeNode;
    private ListNode ifListNode;
    private ListNode elseListNode;

    public PipeNode getPipeNode() {
        return pipeNode;
    }

    public void setPipeNode(PipeNode pipeNode) {
        this.pipeNode = pipeNode;
    }

    public ListNode getIfListNode() {
        return ifListNode;
    }

    public void setIfListNode(ListNode listNode) {
        this.ifListNode = listNode;
    }

    public ListNode getElseListNode() {
        return elseListNode;
    }

    public void setElseListNode(ListNode elseListNode) {
        this.elseListNode = elseListNode;
    }

    @Override
    public String toString() {
        String name = pipeNode.getContext();
        switch (name) {
            case "if":
            case "range":
            case "with":
                break;
            default:
                throw new IllegalStateException("unknown branch type");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{{").append(name).append(' ').append(pipeNode).append("}}");
        if (ifListNode != null) {
            // Avoid printing "null"
            sb.append(ifListNode);
        }
        if (elseListNode != null) {
            sb.append("{{else}}").append(elseListNode);
        }
        sb.append("{{end}}");
        return sb.toString();
    }
}
