package io.github.verils.gotemplate;

import io.github.verils.gotemplate.runtime.simple.Executor;
import io.github.verils.gotemplate.runtime.simple.parse.Node;

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

    private final Node rootNode;


    public GoTemplate(GoTemplateFactory factory, String name, Node rootNode) {
        this.factory = factory;
        this.name = name;
        this.rootNode = rootNode;
    }


    public void execute(Object data, Writer writer) throws IOException,
            GoTemplateNotFoundException, GoTemplateExecutionException {
        Executor executor = new Executor(factory, getFunctions());
        executor.execute(name, data, writer);
    }

    public String getName() {
        return name;
    }

    public Node root() {
        return rootNode;
    }

    private Map<String, Function> getFunctions() {
        return factory.getFunctions();
    }
}
