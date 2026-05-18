package io.github.verils.gotemplate;

import io.github.verils.gotemplate.internal.Executor;
import io.github.verils.gotemplate.internal.IOUtils;
import io.github.verils.gotemplate.internal.Parser;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.TextNode;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
 *   <li>Template cloning for thread-safe reuse</li>
 * </ul>
 * <p>
 * Thread Safety: This class is NOT thread-safe during parsing. Once parsing is complete,
 * execution is thread-safe if different Writer instances are used for each execution.
 * For concurrent scenarios, use {@link #Template(Template)} copy constructor to create independent copies.
 *
 * @see Function for implementing custom template functions
 */
public class Template {

    private static final String DEFAULT_LEFT_DELIM = "{{";
    private static final String DEFAULT_RIGHT_DELIM = "}}";
    private static final String DEFAULT_LEFT_COMMENT = "/*";
    private static final String DEFAULT_RIGHT_COMMENT = "*/";

    private final String name;

    private final Map<String, Function> functions;
    private final Map<String, Function> customFunctions; // Store custom functions for cloning

    private final String leftDelimiter;
    private final String rightDelimiter;
    private final String leftComment;
    private final String rightComment;

    private final Map<String, Node> nodes;

    private MissingKeyPolicy missingKeyPolicy;

    private boolean mapKeySorting; // Whether to sort map keys during iteration
    
    // Unified ClassMetadata cache: replaces beanInfoCache, annotationCache, and propertyDescriptorCache
    // This cache is instance-level, so it gets GC'd when the Template is GC'd (no memory leak)
    private final Map<Class<?>, io.github.verils.gotemplate.internal.ClassMetadata> classMetadataCache = new ConcurrentHashMap<>();

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
        // Validate required parameters
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        
        // Validate delimiters
        if (leftDelimiter != null && leftDelimiter.isEmpty()) {
            throw new IllegalArgumentException("Left delimiter cannot be empty");
        }
        if (rightDelimiter != null && rightDelimiter.isEmpty()) {
            throw new IllegalArgumentException("Right delimiter cannot be empty");
        }
        
        // Validate comment delimiters
        if (leftComment != null && leftComment.isEmpty()) {
            throw new IllegalArgumentException("Left comment delimiter cannot be empty");
        }
        if (rightComment != null && rightComment.isEmpty()) {
            throw new IllegalArgumentException("Right comment delimiter cannot be empty");
        }
        
        this.name = name;
        this.customFunctions = functions != null ? new LinkedHashMap<>(functions) : Collections.emptyMap();
        this.functions = Stream.concat(Functions.BUILTIN.entrySet().stream(), this.customFunctions.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
        this.leftDelimiter = leftDelimiter != null ? leftDelimiter : DEFAULT_LEFT_DELIM;
        this.rightDelimiter = rightDelimiter != null ? rightDelimiter : DEFAULT_RIGHT_DELIM;
        this.leftComment = leftComment != null ? leftComment : DEFAULT_LEFT_COMMENT;
        this.rightComment = rightComment != null ? rightComment : DEFAULT_RIGHT_COMMENT;

        this.nodes = new LinkedHashMap<>();

        this.missingKeyPolicy = MissingKeyPolicy.INVALID;
        this.mapKeySorting = true; // Default to true for Go template compatibility
    }

    /**
     * Copy constructor that creates a deep copy of an existing template.
     * <p>
     * This constructor is useful for creating thread-safe copies of templates that have
     * already been parsed. The cloned template has its own independent copy of all parsed
     * template definitions, allowing safe concurrent modification and execution.
     * <p>
     * Example usage for thread safety:
     * <pre>{@code
     * // Create and parse template once
     * Template baseTemplate = new Template("master");
     * baseTemplate.parse("{{.message}}");
     *
     * // In each thread, create a copy before use
     * Template threadSafe = new Template(baseTemplate);
     * StringWriter writer = new StringWriter();
     * threadSafe.execute(writer, data);
     * }</pre>
     *
     * @param other The template to copy
     * @since 0.5.0
     */
    public Template(Template other) {
        this(other, other.name);
    }

    private Template(Template other, String name) {
        this.name = name;
        this.customFunctions = new LinkedHashMap<>(other.customFunctions);
        this.functions = other.functions; // Functions map is immutable after construction
        this.leftDelimiter = other.leftDelimiter;
        this.rightDelimiter = other.rightDelimiter;
        this.leftComment = other.leftComment;
        this.rightComment = other.rightComment;
        this.missingKeyPolicy = other.missingKeyPolicy;
        this.mapKeySorting = other.mapKeySorting;
        this.nodes = new LinkedHashMap<>(other.nodes);
    }

    /**
     * Returns the root template name.
     *
     * @return root template name
     * @since 0.6.0
     */
    public String name() {
        return name;
    }

    /**
     * Configures how this template handles missing map keys and field-chain segments during execution.
     *
     * @param missingKeyPolicy missing-key policy; {@code null} resets to {@link MissingKeyPolicy#INVALID}
     * @return this template
     * @since 0.6.0
     */
    public Template withMissingKeyPolicy(MissingKeyPolicy missingKeyPolicy) {
        this.missingKeyPolicy = missingKeyPolicy != null ? missingKeyPolicy : MissingKeyPolicy.INVALID;
        return this;
    }



    /**
     * Configures whether map keys should be sorted during {@code range} iteration.
     * <p>
     * When enabled (default), map keys are sorted using natural ordering (for {@link Comparable} keys)
     * or {@code toString()} comparison (for other keys). This provides deterministic output
     * matching Go's default template behavior.
     * <p>
     * Go template specification: "If the value is a map and the keys are of basic type with a 
     * defined order, the elements will be visited in sorted key order."
     *
     * @param mapKeySorting {@code true} to sort map keys (default), {@code false} to preserve insertion order
     * @return this template
     * @since 0.7.0
     * @see <a href="https://pkg.go.dev/text/template#hdr-Actions">Go template range documentation</a>
     */
    public Template withMapKeySorting(boolean mapKeySorting) {
        this.mapKeySorting = mapKeySorting;
        return this;
    }

    /**
     * Applies Go-style string options supported by gotemplate4j.
     * <p>
     * Supported values are {@code missingkey=default}, {@code missingkey=zero}, and {@code missingkey=error}.
     *
     * @param option option string
     * @return this template
     * @throws IllegalArgumentException if the option is not supported
     * @since 0.6.0
     */
    public Template option(String option) {
        if (option == null) {
            throw new IllegalArgumentException("option can not be null");
        }

        int pos = option.indexOf('=');
        if (pos == -1) {
            throw new IllegalArgumentException("option must be in format 'key=value'");
        }

        String key = option.substring(0, pos);
        String value = option.substring(pos + 1);

        if ("missingkey".equals(key)) {
            switch (value) {
                case "default":
                case "invalid":
                    return withMissingKeyPolicy(MissingKeyPolicy.INVALID);
                case "zero":
                    return withMissingKeyPolicy(MissingKeyPolicy.ZERO);
                case "error":
                    return withMissingKeyPolicy(MissingKeyPolicy.ERROR);
                default:
                    throw new IllegalArgumentException("unsupported option: " + option);
            }
        }



        return this;
    }

    /**
     * Returns whether a parsed template with the given name exists in this template set.
     *
     * @param name template name
     * @return {@code true} if the template is defined
     * @since 0.6.0
     */
    public boolean hasTemplate(String name) {
        return nodes.containsKey(name);
    }

    /**
     * Returns the names of parsed templates in stable parse order.
     *
     * @return immutable set of template names
     * @since 0.6.0
     */
    public Set<String> definedTemplates() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(orderedTemplateNames()));
    }

    /**
     * Looks up a parsed template by name.
     * <p>
     * The returned template is an independent copy of this template set with the requested template as its root name.
     * Parsing more definitions into the returned template does not mutate the original set.
     *
     * @param name template name
     * @return independent template copy, or {@code null} if no template exists with that name
     * @since 0.6.0
     */
    public Template lookup(String name) {
        if (!hasTemplate(name)) {
            return null;
        }
        return new Template(this, name);
    }

    /**
     * Returns independent copies for all parsed templates in stable parse order.
     *
     * @return immutable list of template copies
     * @since 0.6.0
     */
    public List<Template> templates() {
        List<Template> templates = new ArrayList<>();
        for (String templateName : orderedTemplateNames()) {
            templates.add(new Template(this, templateName));
        }
        return Collections.unmodifiableList(templates);
    }

    /**
     * Returns the currently configured missing-key policy.
     *
     * @return missing-key policy
     * @since 0.6.0
     */
    public MissingKeyPolicy missingKeyPolicy() {
        return missingKeyPolicy;
    }



    /**
     * Returns whether map key sorting is enabled.
     * <p>
     * By default, this is {@code true} to match Go template behavior where map iteration
     * produces deterministic, sorted output for keys of basic types with defined order.
     *
     * @return {@code true} if map keys are sorted during iteration (default), {@code false} otherwise
     * @since 0.7.0
     */
    public boolean mapKeySorting() {
        return mapKeySorting;
    }

    private List<String> orderedTemplateNames() {
        List<String> names = new ArrayList<>();
        if (nodes.containsKey(name)) {
            names.add(name);
        }
        for (String templateName : nodes.keySet()) {
            if (!templateName.equals(name)) {
                names.add(templateName);
            }
        }
        return names;
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

            if (isNotEmpty(node)) {
                this.nodes.put(name, node);
            }
        });
    }

    private boolean isNotEmpty(Node currentNode) {
        if (currentNode instanceof ListNode) {
            ListNode listNode = (ListNode) currentNode;
            for (Node node : listNode) {
                if (isNotEmpty(node)) {
                    return true;
                }
            }
            return false;
        }
        if (currentNode instanceof TextNode) {
            TextNode textNode = (TextNode) currentNode;
            return !textNode.getText().trim().isEmpty();
        }
        return true;
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
        parse(in, StandardCharsets.UTF_8);
    }

    /**
     * Parses template content from an InputStream with specified charset.
     * <p>
     * The stream is read using the specified charset and processed as template text.
     * The stream will be closed after reading.
     *
     * @param in      InputStream providing the template content
     * @param charset the charset to use for reading the stream
     * @throws TemplateParseException if the template contains syntax errors or parsing issues
     * @throws IOException            if the stream cannot be read or is closed
     * @see #parse(String)
     * @see #parse(Reader)
     * @since 0.9.0
     */
    public void parse(InputStream in, Charset charset) throws TemplateParseException, IOException {
        parse(new InputStreamReader(in, charset));
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
     * Parses template content from a file.
     * <p>
     * The file is read using UTF-8 encoding. The template name for definitions inside
     * the file will default to the file's base name (without extension) unless explicitly
     * defined within the template text.
     *
     * @param path the path to the file
     * @throws TemplateParseException if the template contains syntax errors or parsing issues
     * @throws IOException            if the file cannot be read
     * @since 0.6.0
     */
    public void parseFile(Path path) throws TemplateParseException, IOException {
        parseFile(path, StandardCharsets.UTF_8);
    }

    /**
     * Parses template content from a file with specified charset.
     * <p>
     * The file is read using the specified charset encoding.
     *
     * @param path    the path to the file
     * @param charset the charset to use for reading the file
     * @throws TemplateParseException if the template contains syntax errors or parsing issues
     * @throws IOException            if the file cannot be read
     * @since 0.9.0
     */
    public void parseFile(Path path, Charset charset) throws TemplateParseException, IOException {
        if (!Files.exists(path)) {
            throw new IOException(String.format(
                    "Template file not found: %s\n" +
                            "Absolute path: %s\n" +
                            "Tip: Check if the file exists and the path is correct.",
                    path, path.toAbsolutePath()));
        }
        try (InputStream in = Files.newInputStream(path)) {
            parse(in, charset);
        }
    }

    /**
     * Parses template content from multiple files.
     * <p>
     * Each file is processed as if by {@link #parseFile(Path)}. If multiple files define
     * the same template name, the last one parsed wins (unless it is empty).
     *
     * @param paths the paths to the files
     * @throws TemplateParseException if any template contains syntax errors or parsing issues
     * @throws IOException            if any file cannot be read
     * @since 0.6.0
     */
    public void parseFiles(Path... paths) throws TemplateParseException, IOException {
        for (Path path : paths) {
            parseFile(path);
        }
    }

    /**
     * Parses all template files (.tmpl) in the specified directory.
     * <p>
     * This is a convenience method that parses all files with the .tmpl extension
     * in the given directory. Files are parsed in the order returned by the file system.
     *
     * @param directory the directory containing template files
     * @throws TemplateParseException if any template contains syntax errors or parsing issues
     * @throws IOException            if the directory cannot be accessed
     * @since 0.9.0
     */
    public void parseDirectory(Path directory) throws TemplateParseException, IOException {
        if (!Files.exists(directory)) {
            throw new IOException(String.format(
                    "Template directory not found: %s\n" +
                            "Absolute path: %s\n" +
                            "Tip: Check if the directory exists and the path is correct.",
                    directory, directory.toAbsolutePath()));
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException(String.format(
                    "Path is not a directory: %s\n" +
                            "Tip: Provide a valid directory path.",
                    directory));
        }
        parseGlob(directory, "*.tmpl");
    }

    /**
     * Parses template files matching a glob pattern in a directory.
     * <p>
     * This method searches the given directory for files matching the glob pattern
     * (e.g., "*.tmpl") and parses them. The order of parsing is determined by the
     * underlying file system iteration.
     *
     * @param directory the directory to search
     * @param glob      the glob pattern (e.g., "*.tmpl", "*.html")
     * @throws TemplateParseException if any template contains syntax errors or parsing issues
     * @throws IOException            if the directory cannot be accessed
     * @since 0.6.0
     */
    public void parseGlob(Path directory, String glob) throws TemplateParseException, IOException {
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, entry -> matcher.matches(entry.getFileName()))) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    parseFile(path);
                }
            }
        }
    }

    /**
     * Parses template content from a classpath resource.
     * <p>
     * The resource is loaded using the current thread's context class loader.
     * The resource path can be absolute (starting with '/') or relative.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Absolute path from classpath root
     * template.parseFromClasspath("/templates/base.tmpl");
     *
     * // Relative path from package
     * template.parseFromClasspath("base.tmpl");
     * }</pre>
     *
     * @param resourcePath the path to the classpath resource (e.g., "/templates/base.tmpl")
     * @throws TemplateParseException   if the template contains syntax errors or parsing issues
     * @throws IOException              if the resource cannot be found or read
     * @throws IllegalArgumentException if resourcePath is null or empty
     * @since 0.9.0
     */
    public void parseFromClasspath(String resourcePath) throws TemplateParseException, IOException {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be null or empty");
        }

        // For ClassLoader.getResourceAsStream(), remove leading '/' as it expects relative paths
        String normalizedPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;

        // Try multiple class loaders for better compatibility
        InputStream in = null;
        ClassLoader classLoader = null;
        
        // First try: Thread context class loader
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                in = classLoader.getResourceAsStream(normalizedPath);
            }
        } catch (Exception e) {
            // Ignore and try next approach
        }
        
        // Second try: This class's class loader
        if (in == null) {
            try {
                classLoader = Template.class.getClassLoader();
                if (classLoader != null) {
                    in = classLoader.getResourceAsStream(normalizedPath);
                }
            } catch (Exception e) {
                // Ignore and try next approach
            }
        }
        
        // Third try: System class loader
        if (in == null) {
            try {
                classLoader = ClassLoader.getSystemClassLoader();
                if (classLoader != null) {
                    in = classLoader.getResourceAsStream(normalizedPath);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        if (in == null) {
            throw new IOException(String.format(
                    "Template resource not found: %s\n" +
                            "Searched in classpath using ClassLoader: %s\n" +
                            "Tip: Ensure the resource exists and the path is correct (case-sensitive).\n" +
                            "Note: Resource paths should not start with '/'.",
                    resourcePath, 
                    classLoader != null ? classLoader.getClass().getName() : "null"));
        }
        
        try {
            parse(in);
        } finally {
            in.close();
        }
    }

    /**
     * Parses multiple template files from classpath resources matching a glob pattern.
     * <p>
     * This method loads all resources from the classpath that match the given pattern.
     * For example, to load all .tmpl files in a directory: Template.parseClasspathResources("/templates/*.tmpl")
     * <p>
     * Note: This method uses the class loader to find resources, which may behave differently
     * depending on the environment (standalone JVM vs application servers).
     *
     * @param pattern the glob pattern to match resources (e.g., "/templates/*.tmpl")
     * @return a new Template instance containing all parsed templates
     * @throws TemplateParseException if any template contains syntax errors or parsing issues
     * @throws IOException            if any resource cannot be found or read
     * @throws IllegalArgumentException if pattern is null or empty
     * @since 0.9.0
     */
    public static Template parseClasspathResources(String pattern) throws TemplateParseException, IOException {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }

        // Extract base path and pattern for matching
        String basePath = pattern;
        String globPattern;
        
        int lastSlash = pattern.lastIndexOf('/');
        if (lastSlash > 0) {
            String fileNamePattern = pattern.substring(lastSlash + 1);
            // Handle simple wildcards
            if (fileNamePattern.contains("*")) {
                globPattern = fileNamePattern;
            } else {
                globPattern = fileNamePattern;
            }
        } else {
            globPattern = "*.tmpl";
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = Template.class.getClassLoader();
        }
        
        // Create a temporary template to hold all parsed content
        Template template = new Template("classpath-resources");
        
        // Attempt to get resources from classpath using the classloader
        java.net.URL baseUrl = classLoader.getResource(basePath.startsWith("/") ? basePath.substring(1) : basePath);
        if (baseUrl != null && baseUrl.getProtocol().equals("file")) {
            // If it's a file URL, we can scan the directory
            java.io.File baseDir = new java.io.File(baseUrl.getPath());
            if (baseDir.isDirectory()) {
                java.io.File[] files = baseDir.listFiles((dir, name) -> {
                    java.nio.file.PathMatcher matcher = java.nio.file.FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
                    return matcher.matches(java.nio.file.Paths.get(name));
                });
                
                if (files != null) {
                    for (java.io.File file : files) {
                        if (file.isFile()) {
                            template.parseFile(java.nio.file.Paths.get(file.getAbsolutePath()));
                        }
                    }
                }
            }
        } else {
            // Fallback: try to load specific resources if pattern is not a directory
            // This is a simplified approach for JAR resources
            // We'll try to load resources based on common naming conventions
            java.util.Enumeration<java.net.URL> resources = classLoader.getResources(
                pattern.startsWith("/") ? pattern.substring(1) : pattern);
            
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] data = new byte[1024];
            while (resources.hasMoreElements()) {
                java.net.URL url = resources.nextElement();
                try (java.io.InputStream inputStream = url.openStream()) {
                    int n;
                    while ((n = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, n);
                    }
                    String content = buffer.toString("UTF-8");
                    template.parse(content);
                    buffer.reset(); // Reset for next resource
                }
            }
        }
        
        return template;
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
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        execute(writer, data);
        writer.flush();
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

        Executor executor = new Executor(nodes, functions, missingKeyPolicy, mapKeySorting, classMetadataCache);
        executor.execute(name, data, writer);
    }


}
