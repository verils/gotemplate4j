package com.github.verils.gotemplate;

import com.github.verils.gotemplate.parse.Node;

public class Visitor {

    private final StringBuilder sb;

    public Visitor(StringBuilder sb) {
        this.sb = sb;
    }

    public void traverse(Node node, Object data) {
        
    }
}
