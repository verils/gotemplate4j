package io.github.verils.gotemplate.internal.ast;

import io.github.verils.gotemplate.internal.lang.StringUtils;

public class TemplateNode implements Node {

    private final String name;

    private PipeNode pipeNode;

    public TemplateNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PipeNode getPipeNode() {
        return pipeNode;
    }

    public void setPipeNode(PipeNode pipeNode) {
        this.pipeNode = pipeNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{{template ").append(StringUtils.quote(name));
        if (pipeNode != null) {
            sb.append(' ').append(pipeNode);
        }
        sb.append("}}");
        return sb.toString();
    }
}
