package com.github.verils.gotemplate;

import com.github.verils.gotemplate.parse.Node;
import com.github.verils.gotemplate.parse.Parser;

public class GoTemplate {

    private final String template;

    private final Node root;

    public GoTemplate(String template) {
        this.template = template;

        this.root = new Parser(template).getRoot();
    }


    public String execute(Object data) {
        StringBuilder sb = new StringBuilder();

        Writer writer = new Writer(sb);
        writer.write(root, data);

        return sb.toString();
    }

    public String getTemplate() {
        return template;
    }
}
