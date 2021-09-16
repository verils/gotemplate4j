package com.github.verils.gotemplate.parse;

public class RangeNode implements Node {

    private BranchNode branchNode;

    public void setBranch(BranchNode branchNode) {
        this.branchNode = branchNode;
    }

    @Override
    public String toString() {
        return branchNode.toString();
    }
}
