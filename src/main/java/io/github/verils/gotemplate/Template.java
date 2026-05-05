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
 * A Go-like template engine implementation for Java.
 * <p>
 * This class provides the main API for parsing and executing Go-style templates.
 * Templates can include variables, conditionals, loops, function calls, and nested templates.
 * <p>
 * Basic usage example:
 * <pre>{@code
 * Template template = new Template("greeting");
 * template.parse("Hello, {{.Name}}!");
 * 
 * Map<String, Object> data = new HashMap<>();
 * data.put("Name", "World");
 * 
 * StringWriter writer = new StringWriter();
 * template.execute(writer, data);
 * System.out.println(writer.toString()); // Output: Hello, World!
 * }</pre>
 * <p>
 * Features:
 * <ul>
 *   <li>Variable substitution with dot notation (e.g., {{.Field.SubField}})</li>
 *   <li>Conditional execution with {{if}}, {{else}}, {{end}}</li>
 *   <li>Iteration with {{range}}...{{end}}</li>
 *   <li>Context switching with {{with}}...{{end}}</li>
 *   <li>Template inclusion with {{template "name" .}}</li>
 *   <li>Template definition with {{define "name"}}...{{end}}</li>
 *   <li>Pipeline operations with | operator</li>
 *   <li>Built-in functions (print, printf, println, eq, ne, lt, le, gt, ge, and, or, len, index, slice, call, html, js, urlquery, deepEqual, typeof, kindOf, not)</li>
 *   <li>Custom function registration</li>
 * </ul>
 * <p>
 * Thread Safety: This class is NOT thread-safe during parsing. Once parsing is complete,
 * execution is thread-safe if different Writer instances are used for each execution.
 *
 * @see GoTemplateFactory for creating and managing multiple templates
 * @see Function for implementing custom template functions
 */
public class Template {

    private static final String DEFAULT_LEFT_DELIM = "{{";
    private static final String DEFAULT_RIGHT_DELIM = "}}";
    private static final String DEFAULT_LEFT_COMMENT = "/*";
    private static final String DEFAULT_RIGHT_COMMENT = "*/";

    private final String name;
    private final Map<String, Function> functions;
    private final String leftDelimiter;
    private final String rightDelimiter;
    private final String leftComment;
    private final String rightComment;

    private final Map<String, Node> nodes;


    /**
     * Creates a new template with the specified name.
     * <p>
     * The template will have access to all built-in Go template functions.
     *
     * @param name The template name, used for identification and template references
     * @throws IllegalArgumentException if name is null or empty
     */
    public Template(String name) {
        this(name, Collections.emptyMap());
    }

    /**
     * Creates a new template with the specified name and custom delimiters.
     * <p>
     * The template will have access to all built-in Go template functions.
     *
     * @param name           The template name, used for identification and template references
     * @param leftDelimiter  Left delimiter (default: "{{")
     * @param rightDelimiter Right delimiter (default: "}}")
     * @throws IllegalArgumentException if name is null or empty
     * @since 0.5.0
     */
    public Template(String name, String leftDelimiter, String rightDelimiter) {
        this(name, Collections.emptyMap(), leftDelimiter, rightDelimiter, DEFAULT_LEFT_COMMENT, DEFAULT_RIGHT_COMMENT);
    }

    /**
     * Creates a new template with the specified name and custom delimiters.
     * <p>
     * The template will have access to all built-in Go template functions.
     *
     * @param name           The template name, used for identification and template references
     * @param leftDelimiter  Left delimiter (default: "{{")
     * @param rightDelimiter Right delimiter (default: "}}")
     * @param leftComment    Left comment delimiter (default: "/*")
     * @param rightComment   Right comment delimiter (default: "* /")
     * @throws IllegalArgumentException if name is null or empty
     * @since 0.5.0
     */
    public Template(String name, String leftDelimiter, String rightDelimiter, String leftComment, String rightComment) {
        this(name, Collections.emptyMap(), leftDelimiter, rightDelimiter, leftComment, rightComment);
    }

    /**
     * Creates a new template with the specified name and custom functions.
     * <p>
     * The template will have access to both built-in Go template functions and the provided custom functions.
     * If a custom function has the same name as a built-in function, the custom function takes precedence.
     *
     * @param name      The template name, used for identification and template references
     * @param functions A map of custom functions where the key is the function name and the value is the Function implementation
     * @throws IllegalArgumentException if name is null or empty
     * @see Function for implementing custom template functions
     */
    public Template(String name, Map<String, Function> functions) {
        this(name, functions, DEFAULT_LEFT_DELIM, DEFAULT_RIGHT_DELIM, DEFAULT_LEFT_COMMENT, DEFAULT_RIGHT_COMMENT);
    }

    /**
     * Creates a new template with the specified name, custom functions, and custom delimiters.
     * <p>
     * The template will have access to both built-in Go template functions and the provided custom functions.
     * If a custom function has the same name as a built-in function, the custom function takes precedence.
     *
     * @param name           The template name, used for identification and template references
     * @param functions      A map of custom functions where the key is the function name and the value is the Function implementation
     * @param leftDelimiter  Left delimiter (default: "{{")
     * @param rightDelimiter Right delimiter (default: "}}")
     * @throws IllegalArgumentException if name is null or empty
     * @see Function for implementing custom template functions
     * @since 0.5.0
     */
    public Template(String name, Map<String, Function> functions, String leftDelimiter, String rightDelimiter) {
        this(name, functions, leftDelimiter, rightDelimiter, DEFAULT_LEFT_COMMENT, DEFAULT_RIGHT_COMMENT);
    }

    /**
     * Creates a new template with the specified name, custom functions, and custom delimiters.
     * <p>
     * The template will have access to both built-in Go template functions and the provided custom functions.
     * If a custom function has the same name as a built-in function, the custom function takes precedence.
     *
     * @param name           The template name, used for identification and template references
     * @param functions      A map of custom functions where the key is the function name and the value is the Function implementation
     * @param leftDelimiter  Left delimiter (default: "{{")
     * @param rightDelimiter Right delimiter (default: "}}")
     * @param leftComment    Left comment delimiter (default: "/*")
     * @param rightComment   Right comment delimiter (default: "* /")
     * @throws IllegalArgumentException if name is null or empty
     * @see Function for implementing custom template functions
     * @since 0.5.0
     */
    public Template(String name, Map<String, Function> functions, String leftDelimiter, String rightDelimiter,
                    String leftComment, String rightComment) {
        this.name = name;
        this.functions = Stream.concat(Functions.BUILTIN.entrySet().stream(), functions.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
        this.leftDelimiter = leftDelimiter != null ? leftDelimiter : DEFAULT_LEFT_DELIM;
        this.rightDelimiter = rightDelimiter != null ? rightDelimiter : DEFAULT_RIGHT_DELIM;
        this.leftComment = leftComment != null ? leftComment : DEFAULT_LEFT_COMMENT;
        this.rightComment = rightComment != null ? rightComment : DEFAULT_RIGHT_COMMENT;
        this.nodes = new LinkedHashMap<>();
    }


    /**
     * Parses the provided text as a template body.
     * <p>
     * The template text can contain:
     * <ul>
     *   <li>Plain text that will be output as-is</li>
     *   <li>Actions enclosed in {{ }} that perform operations</li>
     *   <li>Template definitions using define/end blocks</li>
     *   <li>Comments</li>
     * </ul>
     * <p>
     * If the content includes definitions of additional templates, those templates will be
     * registered and available for use via the template action.
     * <p>
     * Example:
     * <pre>{@code
     * template.parse(
     *     "{{define \"header\"}}Header: {{.Title}}{{end}}" +
     *     "{{template \"header\" .}}"
     * );
     * }</pre>
     *
     * @param template The template text content to parse
     * @throws TemplateParseException if the template contains syntax errors, undefined variables,
     *                                or other parsing issues. The exception includes line and column information.
     * @see #parse(InputStream)
     * @see #parse(Reader)
     */
    public void parse(String template) throws TemplateParseException {
        Parser parser = new Parser(functions, leftDelimiter, rightDelimiter, leftComment, rightComment);
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
     * Parses template content from an InputStream.
     * <p>
     * The stream is read using UTF-8 encoding and processed as template text.
     * The stream will be closed after reading.
     *
     * @param in InputStream providing the template content
     * @throws TemplateParseException if the template contains syntax errors or parsing issues
     * @throws IOException            if the stream cannot be read or is closed
     * @see #parse(String)
     * @see #parse(Reader)
     */
    public void parse(InputStream in) throws TemplateParseException, IOException {
        parse(new InputStreamReader(in));
    }

    /**
     * Parses template content from a Reader.
     * <p>
     * The reader's content is read completely and processed as template text.
     * The reader will be closed after reading.
     *
     * @param reader Reader providing the template content
     * @throws TemplateParseException if the template contains syntax errors or parsing issues
     * @throws IOException            if the reader cannot be read or is closed
     * @see #parse(String)
     * @see #parse(InputStream)
     */
    public void parse(Reader reader) throws TemplateParseException, IOException {
        String text = IOUtils.read(reader);
        parse(text);
    }

    /**
     * Executes the template with the provided data and writes the result to an OutputStream.
     * <p>
     * The output is encoded using UTF-8.
     *
     * @param out  OutputStream to write the template output
     * @param data The data object to use for template variable substitution. Can be any Java object.
     *             Fields are accessed via getter methods (e.g., getName() for {{.Name}}).
     * @throws TemplateException         if template execution fails (undefined field, function error, etc.)
     * @throws IOException               if writing to the output stream fails
     * @throws TemplateNotFoundException if the template has not been parsed
     * @see #execute(Writer, Object)
     */
    public void execute(OutputStream out, Object data) throws TemplateException, IOException {
        execute(new OutputStreamWriter(out), data);
    }

    /**
     * Executes the template with the provided data and writes the result to a Writer.
     * <p>
     * This is the primary execution method. The template processes the data object,
     * substituting variables and evaluating actions, then writes the result to the writer.
     * <p>
     * Data access patterns:
     * <ul>
     *   <li>{{.FieldName}} - Calls getFieldName() on the data object</li>
     *   <li>{{.Field.SubField}} - Chains getter calls: getField().getSubField()</li>
     *   <li>{{index .ArrayOrMap 0}} - Accesses array/map elements</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     * User user = new User();
     * user.setName("Alice");
     * 
     * StringWriter writer = new StringWriter();
     * template.execute(writer, user);
     * String result = writer.toString();
     * }</pre>
     *
     * @param writer Writer to receive the template output
     * @param data   The data object for template variable substitution
     * @throws TemplateException         if template execution fails
     * @throws IOException               if writing to the writer fails
     * @throws TemplateNotFoundException if the template has not been parsed
     * @see #execute(OutputStream, Object)
     * @see #executeTemplate(Writer, String, Object)
     */
    public void execute(Writer writer, Object data) throws TemplateException, IOException {
        executeTemplate(writer, name, data);
    }

    /**
     * Executes a named template with the provided data and writes the result to a Writer.
     * <p>
     * This method allows executing a specific template by name, which is useful when multiple
     * templates have been defined using {{define}} actions or when working with template groups.
     * <p>
     * Example:
     * <pre>{@code
     * // Parse a template with multiple definitions
     * template.parse(
     *     "{{define \"header\"}}<h1>{{.Title}}</h1>{{end}}" +
     *     "{{define \"footer\"}}<footer>{{.Copyright}}</footer>{{end}}"
     * );
     * 
     * // Execute a specific template
     * StringWriter writer = new StringWriter();
     * template.executeTemplate(writer, "header", data);
     * }</pre>
     *
     * @param writer Writer to receive the template output
     * @param name   The name of the template to execute (must have been parsed)
     * @param data   The data object for template variable substitution
     * @throws TemplateException         if template execution fails
     * @throws IOException               if writing to the writer fails
     * @throws TemplateNotFoundException if no template with the given name exists
     * @see #execute(Writer, Object)
     */
    public void executeTemplate(Writer writer, String name, Object data) throws TemplateException, IOException {
        Node rootNode = nodes.get(name);
        if (rootNode == null) {
            throw new TemplateNotFoundException(String.format("Template '%s' not found.", name));
        }

        Executor executor = new Executor(nodes, functions);
        executor.execute(name, data, writer);
    }

}
