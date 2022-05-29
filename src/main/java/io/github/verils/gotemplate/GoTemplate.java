package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
import io.github.verils.gotemplate.parse.ListNode;
import io.github.verils.gotemplate.parse.Parser;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class GoTemplate {

    /**
     * Common factory for a template group
     */
    private final GoTemplateFactory factory;

    /**
     * The name of this template
     */
    private final String name;

    /**
     * Parsed AST nodes
     */
    private final Map<String, ListNode> nodes;


    public GoTemplate(GoTemplateFactory factory, String name) {
        this.factory = factory;
        this.nodes = new HashMap<>();
        this.name = name;
    }


    public void parse(String template) {
        Parser parser = new Parser(getFunctions());
        parser.parse(nodes, name, template);
    }

    public void execute(Object data, Writer writer) throws IOException {
        Executor executor = new Executor(nodes, getFunctions());
        executor.execute(writer, name, data);
    }

    private Map<String, Function> getFunctions() {
        return factory.getFunctions();
    }

}
