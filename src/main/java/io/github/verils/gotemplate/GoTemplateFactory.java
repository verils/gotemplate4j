package io.github.verils.gotemplate;

import io.github.verils.gotemplate.internal.IOUtils;
import io.github.verils.gotemplate.internal.Parser;
import io.github.verils.gotemplate.internal.ast.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Factory for creating and managing multiple templates with shared functions.
 * <p>
 * This class provides a way to parse and manage a collection of related templates that can
 * reference each other. It's useful for template libraries or when you need to share custom
 * functions across multiple templates.
 * <p>
 * Example usage:
 * <pre>{@code
 * GoTemplateFactory factory = new GoTemplateFactory();
 * 
 * // Parse multiple templates
 * factory.parse("header", "{{define \"header\"}}<h1>{{.Title}}</h1>{{end}}");
 * factory.parse("footer", "{{define \"footer\"}}<footer>{{.Copyright}}</footer>{{end}}");
 * factory.parse("layout", "{{template \"header\" .}}<div>{{.Content}}</div>{{template \"footer\" .}}");
 * 
 * // Get and execute a specific template
 * GoTemplate template = factory.getTemplate("layout");
 * StringWriter writer = new StringWriter();
 * template.execute(data, writer);
 * }</pre>
 * <p>
 * Note: This class is marked as @Deprecated. Consider using {@link Template} directly for simpler use cases.
 *
 * @see Template for the recommended API
 * @deprecated Redundant factory object - use {@link Template} directly instead
 */
public class GoTemplateFactory {

    private static final String DEFAULT_LEFT_DELIM = "{{";
    private static final String DEFAULT_RIGHT_DELIM = "}}";
    private static final String DEFAULT_LEFT_COMMENT = "/*";
    private static final String DEFAULT_RIGHT_COMMENT = "*/";

    private final Map<String, Function> functions;
    private final String leftDelimiter;
    private final String rightDelimiter;
    private final String leftComment;
    private final String rightComment;

    private final Map<String, Node> rootNodes;


    /**
     * Creates a factory with default settings and all built-in functions.
     */
    public GoTemplateFactory() {
        this(null);
    }

    /**
     * Creates a factory with custom functions in addition to built-in functions.
     * <p>
     * Custom functions will be merged with the built-in functions. If a custom function
     * has the same name as a built-in function, the custom function takes precedence.
     *
     * @param functions A map of custom functions where the key is the function name
     *                  Can be null to use only built-in functions
     * @see Function for implementing custom template functions
     */
    public GoTemplateFactory(Map<String, Function> functions) {
        this(functions, DEFAULT_LEFT_DELIM, DEFAULT_RIGHT_DELIM, DEFAULT_LEFT_COMMENT, DEFAULT_RIGHT_COMMENT);
    }

    /**
     * Creates a factory with custom functions and custom delimiters.
     * <p>
     * Custom functions will be merged with the built-in functions. If a custom function
     * has the same name as a built-in function, the custom function takes precedence.
     *
     * @param functions      A map of custom functions where the key is the function name
     *                       Can be null to use only built-in functions
     * @param leftDelimiter  Left delimiter (default: "{{")
     * @param rightDelimiter Right delimiter (default: "}}")
     * @see Function for implementing custom template functions
     * @since 0.5.0
     */
    public GoTemplateFactory(Map<String, Function> functions, String leftDelimiter, String rightDelimiter) {
        this(functions, leftDelimiter, rightDelimiter, DEFAULT_LEFT_COMMENT, DEFAULT_RIGHT_COMMENT);
    }

    /**
     * Creates a factory with custom functions and custom delimiters.
     * <p>
     * Custom functions will be merged with the built-in functions. If a custom function
     * has the same name as a built-in function, the custom function takes precedence.
     *
     * @param functions      A map of custom functions where the key is the function name
     *                       Can be null to use only built-in functions
     * @param leftDelimiter  Left delimiter (default: "{{")
     * @param rightDelimiter Right delimiter (default: "}}")
     * @param leftComment    Left comment delimiter (default: "/*")
     * @param rightComment   Right comment delimiter (default: "* /")
     * @see Function for implementing custom template functions
     * @since 0.5.0
     */
    public GoTemplateFactory(Map<String, Function> functions, String leftDelimiter, String rightDelimiter,
                             String leftComment, String rightComment) {
        LinkedHashMap<String, Function> map = new LinkedHashMap<>(Functions.BUILTIN);
        if (functions != null) {
            map.putAll(functions);
        }
        this.functions = Collections.unmodifiableMap(map);
        this.leftDelimiter = leftDelimiter != null ? leftDelimiter : DEFAULT_LEFT_DELIM;
        this.rightDelimiter = rightDelimiter != null ? rightDelimiter : DEFAULT_RIGHT_DELIM;
        this.leftComment = leftComment != null ? leftComment : DEFAULT_LEFT_COMMENT;
        this.rightComment = rightComment != null ? rightComment : DEFAULT_RIGHT_COMMENT;
        this.rootNodes = new LinkedHashMap<>();
    }


    /**
     * Parses an unnamed template from text content.
     * <p>
     * Templates defined within the content using {{define}} or {{block}} actions will be
     * registered and available for use. The main content (outside of definitions) will be
     * associated with an empty string name.
     *
     * @param text Template text content to parse
     * @throws TemplateParseException if the template contains syntax errors
     * @see #parse(String, String) for named templates
     */
    public void parse(String text) throws TemplateParseException {
        parse("", text);
    }

    /**
     * Parses a named template from text content.
     * <p>
     * The parsed template can be retrieved later using {@link #getTemplate(String)}.
     * Any templates defined within the content using {{define}} or {{block}} will also be registered.
     *
     * @param name The template name for later retrieval
     * @param text Template text content to parse
     * @throws TemplateParseException if the template contains syntax errors
     * @see #getTemplate(String)
     */
    public void parse(String name, String text) throws TemplateParseException {
        Parser parser = new Parser(functions, leftDelimiter, rightDelimiter, leftComment, rightComment);
        Map<String, Node> parsedNodes = parser.parse(name, text);
        rootNodes.putAll(parsedNodes);
    }

    /**
     * Parses a named template from an InputStream.
     * <p>
     * The stream is read using UTF-8 encoding and processed as template text.
     *
     * @param name The template name for later retrieval
     * @param in   InputStream providing the template content
     * @throws TemplateParseException if the template contains syntax errors
     * @throws IOException            if the stream cannot be read
     * @see #parse(String, String)
     */
    public void parse(String name, InputStream in) throws TemplateParseException, IOException {
        parse(name, new InputStreamReader(in));
    }

    /**
     * Parses a named template from a Reader.
     * <p>
     * The reader's content is read completely and processed as template text.
     *
     * @param name   The template name for later retrieval
     * @param reader Reader providing the template content
     * @throws TemplateParseException if the template contains syntax errors
     * @throws IOException            if the reader cannot be read
     * @see #parse(String, String)
     */
    public void parse(String name, Reader reader) throws TemplateParseException, IOException {
        String text = IOUtils.read(reader);
        parse(name, text);
    }

    /**
     * Retrieves a previously parsed template by name.
     * <p>
     * The template must have been parsed using one of the parse() methods before calling this method.
     *
     * @param name The name of the template to retrieve
     * @return The GoTemplate instance for execution
     * @throws TemplateNotFoundException if no template with the given name has been parsed
     * @see #parse(String, String)
     * @see #parse(String, InputStream)
     * @see #parse(String, Reader)
     */
    public GoTemplate getTemplate(String name) throws TemplateNotFoundException {
        Node rootNode = rootNodes.get(name);
        if (rootNode == null) {
            throw new TemplateNotFoundException(String.format("Template '%s' not found.", name));
        }
        return new GoTemplate(this, name, rootNode);
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public Map<String, Node> getRootNodes() {
        return rootNodes;
    }

}
