package io.github.verils.gotemplate;

import io.github.verils.gotemplate.internal.Executor;
import io.github.verils.gotemplate.internal.IOUtils;
import io.github.verils.gotemplate.internal.Parser;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.TextNode;

import java.io.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Golang-like template api
 */
public class Template {

    private final String name;
    private final Map<String, Function> functions;

    private final Map<String, Node> nodes;


    /**
     * Create a template with name
     *
     * @param name The template name
     */
    public Template(String name) {
        this(name, Collections.emptyMap());
    }

    /**
     * Create a template with name, and declare functions to be used in template
     *
     * @param name      The template name
     * @param functions The functions called in template actions
     */
    public Template(String name, Map<String, Function> functions) {
        this.name = name;
        this.functions = Stream.concat(Functions.BUILTIN.entrySet().stream(), functions.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
        this.nodes = new LinkedHashMap<>();
    }


    /**
     * Parse the text content as template's body. If the content includes definition of additional
     * templates, those templates will be added into the parsed nodes.
     *
     * @param template text template content
     * @throws TemplateParseException if meet a syntax error or parsing error
     */
    public void parse(String template) throws TemplateParseException {
        Parser parser = new Parser(functions);
        Map<String, Node> nodes = parser.parse(name, template);
        nodes.forEach((name, node) -> {
            if (!this.nodes.containsKey(name)) {
                this.nodes.put(name, node);
                return;
            }

            if (!isEmpty(node)) {
                this.nodes.put(name, node);
            }
        });
    }

    private boolean isEmpty(Node currentNode) {
        if (currentNode instanceof ListNode) {
            ListNode listNode = (ListNode) currentNode;
            for (Node node : listNode) {
                if (!isEmpty(node)) {
                    return false;
                }
            }
            return true;
        }
        if (currentNode instanceof TextNode) {
            TextNode textNode = (TextNode) currentNode;
            return textNode.getText().trim().isEmpty();
        }
        return false;
    }

    /**
     * Parse the content from {@link InputStream} as template's body. If the content includes definition of additional
     * templates, those templates will be added into the parsed nodes.
     *
     * @param in InputStream providing the template content
     * @throws TemplateParseException if meet a syntax error or parsing error
     * @throws IOException            if can not read text content from the input stream
     */
    public void parse(InputStream in) throws TemplateParseException, IOException {
        parse(new InputStreamReader(in));
    }

    /**
     * Parse the content from {@link Reader} as template's body. If the content includes definition of additional
     * templates, those templates will be added into the parsed nodes.
     *
     * @param reader Reader providing the template content
     * @throws TemplateParseException if meet a syntax error or parsing error
     * @throws IOException            if can not read text content from the reader
     */
    public void parse(Reader reader) throws TemplateParseException, IOException {
        String text = IOUtils.read(reader);
        parse(text);
    }

    public void execute(OutputStream out, Object data) throws TemplateException, IOException {
        execute(new OutputStreamWriter(out), data);
    }

    public void execute(Writer writer, Object data) throws TemplateException, IOException {
        executeTemplate(writer, name, data);
    }

    public void executeTemplate(Writer writer, String name, Object data) throws TemplateException, IOException {
        Node rootNode = nodes.get(name);
        if (rootNode == null) {
            throw new TemplateNotFoundException(String.format("Template '%s' not found.", name));
        }

        Executor executor = new Executor(nodes, functions);
        executor.execute(name, data, writer);
    }

}
