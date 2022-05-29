package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
import io.github.verils.gotemplate.parse.ListNode;

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

    private final ListNode root;


    public GoTemplate(GoTemplateFactory factory, String name, ListNode root) {
        this.factory = factory;
        this.name = name;
        this.root = root;
    }


    public void execute(Object data, Writer writer) throws IOException {
        Executor executor = new Executor(factory, getFunctions());
        executor.execute(writer, name, data);
    }

    public String getName() {
        return name;
    }

    public ListNode root() {
        return root;
    }

    private Map<String, Function> getFunctions() {
        return factory.getFunctions();
    }
}
