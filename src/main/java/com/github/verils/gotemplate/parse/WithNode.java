package com.github.verils.gotemplate.parse;

public class WithNode implements Node {

    private BranchNode branchNode;

    public void setBranch(BranchNode branchNode) {
        this.branchNode = branchNode;
    }

    @Override
    public String toString() {
        return branchNode.toString();
    }
}
