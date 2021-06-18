package com.github.verils.gotemplate;

import com.github.verils.gotemplate.parse.Node;
import com.github.verils.gotemplate.parse.Parser;

public class GoTemplate {

    private final String template;

    private final Node root;

    public GoTemplate(String template) {
        this.template = template;

        this.root = new Parser().parse(template);
    }


    public String execute(Object data) {
        StringBuilder sb = new StringBuilder();

        Visitor visitor = new Visitor(sb);
        visitor.traverse(root, data);

        return sb.toString();
    }

    public String getTemplate() {
        return template;
    }
}
