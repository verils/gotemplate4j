package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
import io.github.verils.gotemplate.parse.ListNode;
import io.github.verils.gotemplate.parse.Node;

import java.io.IOException;
import java.io.Writer;
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

    private final ListNode root;


    public GoTemplate(GoTemplateFactory factory, String name, ListNode root) {
        this.factory = factory;
        this.name = name;
        this.root = root;
    }


    public void execute(Object data, Writer writer) throws IOException,
            TemplateNotFoundException, TemplateExecutionException {
        Executor executor = new Executor(factory, getFunctions());
        executor.execute(writer, name, data);
    }

    public String getName() {
        return name;
    }

    public Node root() {
        return root;
    }

    private Map<String, Function> getFunctions() {
        return factory.getFunctions();
    }
}
