package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.StringUtils;

public class TemplateNode implements Node {

    private final String templateName;

    private PipeNode pipeNode;

    public TemplateNode(String templateName) {
        this.templateName = templateName;
    }

    public void setPipeNode(PipeNode pipeNode) {
        this.pipeNode = pipeNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{{template ").append(StringUtils.quote(templateName));
        if (pipeNode != null) {
            sb.append(' ').append(pipeNode);
        }
        sb.append("}}");
        return sb.toString();
    }
}
