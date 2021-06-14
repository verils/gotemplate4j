package com.github.verils.gotemplate;

import com.github.verils.gotemplate.lex.Lexer;
import com.github.verils.gotemplate.tree.Tree;

public class GoTemplate {

    private final String template;

    private final Tree tree;

    public GoTemplate(String template) {
        this.template = template;
        this.tree = parse(template);
    }

    private Tree parse(String template) {
        Tree tree = new Tree();
        tree.parse(template);
        return tree;
    }


    public String exec(Object data) {

        return "null";
    }

}
