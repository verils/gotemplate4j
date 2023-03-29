package io.github.verils.gotemplate;

import io.github.verils.gotemplate.internal.Executor;
import io.github.verils.gotemplate.internal.IOUtils;
import io.github.verils.gotemplate.internal.Parser;
import io.github.verils.gotemplate.internal.ast.Node;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Golang-like template api
 */
public class Template {

    private final String name;
    private final Map<String, Function> functions;

    private final Map<String, Node> rootNodes;


    public Template(String name) {
        this(name, Functions.BUILT_IN);
    }

    public Template(String name, Map<String, Function> functions) {
        this.name = name;
        this.functions = functions;
        this.rootNodes = new LinkedHashMap<>();
    }


    public void parse(String template) throws GoTemplateParseException {
        Parser parser = new Parser(functions);
        Map<String, Node> nodes = parser.parse(name, template);
        rootNodes.putAll(nodes);
    }


    public void parse(InputStream in) throws GoTemplateParseException, IOException {
        parse(new InputStreamReader(in));
    }


    public void parse(Reader reader) throws GoTemplateParseException, IOException {
        String text = IOUtils.read(reader);
        parse(text);
    }

    public void execute(OutputStream out, Object data) throws GoTemplateException, IOException {
        execute(new OutputStreamWriter(out), data);
    }

    public void execute(Writer writer, Object data) throws GoTemplateException, IOException {
        executeTemplate(writer, name, data);
    }

    public void executeTemplate(Writer writer, String name, Object data) throws GoTemplateException, IOException {
        Node rootNode = rootNodes.get(name);
        if (rootNode == null) {
            throw new GoTemplateNotFoundException(String.format("Template '%s' not found.", name));
        }

        Executor executor = new Executor(rootNodes, functions);
        executor.execute(name, data, writer);
    }

}
