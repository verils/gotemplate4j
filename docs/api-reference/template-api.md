# Template API Reference

The `Template` class is the main entry point for parsing and executing Go-style templates in Java. This reference documents all public constructors, methods, and configuration options.

---

## Table of Contents

- [Constructors](#constructors)
- [Parsing Methods](#parsing-methods)
- [Execution Methods](#execution-methods)
- [Template Introspection](#template-introspection)
- [Configuration Methods](#configuration-methods)
- [File System Helpers](#file-system-helpers)
- [Thread Safety](#thread-safety)

---

## Constructors

### `Template(String name)`

Creates a new template with the specified name and default delimiters (`{{` and `}}`).

```java
Template template = new Template("my-template");
```

**Parameters:**
- `name` - The template name, used for identification and template references

**Throws:**
- `IllegalArgumentException` if name is null or empty

**Since:** 0.1.0

---

### `Template(String name, String leftDelimiter, String rightDelimiter)`

Creates a new template with custom delimiters.

```java
// Use <% %> instead of {{ }}
Template template = new Template("my-template", "<%", "%>");
template.parse("<% .Name %>");
```

**Parameters:**
- `name` - The template name
- `leftDelimiter` - Left delimiter (default: "{{")
- `rightDelimiter` - Right delimiter (default: "}}")

**Throws:**
- `IllegalArgumentException` if name is null or empty

**Since:** 0.5.0

---

### `Template(String name, String leftDelimiter, String rightDelimiter, String leftComment, String rightComment)`

Creates a new template with custom delimiters and comment markers.

```java
// Custom delimiters and comment style
Template template = new Template("my-template", "<%", "%>", "<!--", "-->");
template.parse("<% /* This is a comment */ .Name %>");
```

**Parameters:**
- `name` - The template name
- `leftDelimiter` - Left delimiter
- `rightDelimiter` - Right delimiter
- `leftComment` - Left comment delimiter (default: "/*")
- `rightComment` - Right comment delimiter (default: "*/")

**Throws:**
- `IllegalArgumentException` if name is null or empty

**Since:** 0.5.0

---

### `Template(String name, Map<String, Function> functions)`

Creates a new template with custom functions.

```java
Map<String, Function> customFunctions = new HashMap<>();
customFunctions.put("upper", args -> ((String) args[0]).toUpperCase());

Template template = new Template("my-template", customFunctions);
template.parse("{{.Name | upper}}");
```

**Parameters:**
- `name` - The template name
- `functions` - A map of custom functions where key is function name and value is Function implementation

**Throws:**
- `IllegalArgumentException` if name is null or empty

**See Also:**
- [Function Interface](function-api.md) for implementing custom functions

**Since:** 0.1.0

---

### `Template(String name, Map<String, Function> functions, String leftDelimiter, String rightDelimiter)`

Creates a new template with custom functions and delimiters.

```java
Map<String, Function> customFunctions = new HashMap<>();
customFunctions.put("lower", args -> ((String) args[0]).toLowerCase());

Template template = new Template("my-template", customFunctions, "[[", "]]");
template.parse("[[.Name | lower]]");
```

**Parameters:**
- `name` - The template name
- `functions` - Custom functions map
- `leftDelimiter` - Left delimiter
- `rightDelimiter` - Right delimiter

**Throws:**
- `IllegalArgumentException` if name is null or empty

**Since:** 0.5.0

---

### `Template(Template other)`

Copy constructor that creates a deep copy of an existing template. Useful for thread-safe execution.

```java
// Create and parse template once
Template baseTemplate = new Template("master");
baseTemplate.parse("{{.message}}");

// In each thread, create a copy before use
Template threadSafe = new Template(baseTemplate);
StringWriter writer = new StringWriter();
threadSafe.execute(writer, data);
```

**Parameters:**
- `other` - The template to copy

**Since:** 0.5.0

---

## Parsing Methods

### `void parse(String template)`

Parses the provided text as a template body.

```java
Template template = new Template("greeting");
template.parse("Hello, {{.Name}}!");
```

**Parameters:**
- `template` - The template text content to parse

**Throws:**
- `TemplateParseException` if the template contains syntax errors, undefined variables, or other parsing issues

**Example with template definitions:**

```java
template.parse(
    "{{define \"header\"}}Header: {{.Title}}{{end}}" +
    "{{template \"header\" .}}"
);
```

**Since:** 0.1.0

---

### `void parse(InputStream in)`

Parses template content from an InputStream using UTF-8 encoding.

```java
try (InputStream in = Files.newInputStream(Paths.get("template.tmpl"))) {
    template.parse(in);
}
```

**Parameters:**
- `in` - InputStream providing the template content

**Throws:**
- `TemplateParseException` if parsing fails
- `IOException` if the stream cannot be read

**Since:** 0.1.0

---

### `void parse(Reader reader)`

Parses template content from a Reader.

```java
try (Reader reader = new FileReader("template.tmpl")) {
    template.parse(reader);
}
```

**Parameters:**
- `reader` - Reader providing the template content

**Throws:**
- `TemplateParseException` if parsing fails
- `IOException` if the reader cannot be read

**Since:** 0.1.0

---

## Execution Methods

### `void execute(Writer writer, Object data)`

Executes the template with the provided data and writes the result to a Writer.

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "World");

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString()); // Output: Hello, World!
```

**Parameters:**
- `writer` - Writer to receive the template output
- `data` - The data object for template variable substitution

**Throws:**
- `TemplateException` if template execution fails
- `IOException` if writing to the writer fails
- `TemplateNotFoundException` if the template has not been parsed

**Data Access Patterns:**
- `{{.FieldName}}` - Calls `getFieldName()` on the data object
- `{{.Field.SubField}}` - Chains getter calls: `getField().getSubField()`
- `{{index .ArrayOrMap 0}}` - Accesses array/map elements

**Since:** 0.1.0

---

### `void execute(OutputStream out, Object data)`

Executes the template and writes the result to an OutputStream using UTF-8 encoding.

```java
ByteArrayOutputStream out = new ByteArrayOutputStream();
template.execute(out, data);
String result = out.toString("UTF-8");
```

**Parameters:**
- `out` - OutputStream to write the template output
- `data` - The data object

**Throws:**
- `TemplateException` if execution fails
- `IOException` if writing fails
- `TemplateNotFoundException` if template not found

**Since:** 0.1.0

---

### `void executeTemplate(Writer writer, String name, Object data)`

Executes a named template with the provided data.

```java
// Parse multiple template definitions
template.parse(
    "{{define \"header\"}}<h1>{{.Title}}</h1>{{end}}" +
    "{{define \"footer\"}}<footer>{{.Copyright}}</footer>{{end}}"
);

// Execute a specific template
StringWriter writer = new StringWriter();
template.executeTemplate(writer, "header", data);
```

**Parameters:**
- `writer` - Writer to receive the output
- `name` - The name of the template to execute
- `data` - The data object

**Throws:**
- `TemplateException` if execution fails
- `IOException` if writing fails
- `TemplateNotFoundException` if no template with the given name exists

**Since:** 0.1.0

---

## Template Introspection

### `String name()`

Returns the root template name.

```java
Template template = new Template("my-template");
System.out.println(template.name()); // Output: my-template
```

**Returns:** Root template name

**Since:** 0.6.0

---

### `boolean hasTemplate(String name)`

Checks whether a parsed template with the given name exists in this template set.

```java
template.parse("{{define \"header\"}}Header{{end}}");
if (template.hasTemplate("header")) {
    System.out.println("Header template is defined");
}
```

**Parameters:**
- `name` - Template name to check

**Returns:** `true` if the template is defined

**Since:** 0.6.0

---

### `Set<String> definedTemplates()`

Returns the names of parsed templates in stable parse order.

```java
template.parse(
    "{{define \"header\"}}Header{{end}}" +
    "{{define \"footer\"}}Footer{{end}}"
);

Set<String> templates = template.definedTemplates();
// Contains: ["main-template", "header", "footer"]
```

**Returns:** Immutable set of template names

**Since:** 0.6.0

---

### `Template lookup(String name)`

Looks up a parsed template by name and returns an independent copy.

```java
Template headerTemplate = template.lookup("header");
if (headerTemplate != null) {
    StringWriter writer = new StringWriter();
    headerTemplate.execute(writer, data);
}
```

**Parameters:**
- `name` - Template name to look up

**Returns:** Independent template copy, or `null` if no template exists with that name

**Since:** 0.6.0

---

### `List<Template> templates()`

Returns independent copies for all parsed templates in stable parse order.

```java
List<Template> allTemplates = template.templates();
for (Template t : allTemplates) {
    System.out.println("Template: " + t.name());
}
```

**Returns:** Immutable list of template copies

**Since:** 0.6.0

---

## Configuration Methods

### `Template withMissingKeyPolicy(MissingKeyPolicy policy)`

Configures how the template handles missing map keys during execution.

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

**Available Policies:**
- `INVALID` (default) - Do nothing and continue execution
- `ZERO` - Return zero value for the map type's element
- `ERROR` - Stop execution with TemplateExecutionException

**Parameters:**
- `policy` - Missing-key policy; `null` resets to `INVALID`

**Returns:** This template (for method chaining)

**See Also:**
- [MissingKeyPolicy Enum](exception-api.md#missingkeypolicy)

**Since:** 0.6.0

---

### `Template option(String option)`

Applies Go-style string options.

```java
// Set missing key policy via option string
template.option("missingkey=error");
template.option("missingkey=zero");
template.option("missingkey=default");
```

**Supported Options:**
- `missingkey=default` or `missingkey=invalid` - Sets policy to INVALID
- `missingkey=zero` - Sets policy to ZERO
- `missingkey=error` - Sets policy to ERROR

**Parameters:**
- `option` - Option string in format "key=value"

**Returns:** This template (for method chaining)

**Throws:**
- `IllegalArgumentException` if the option format is invalid or unsupported

**Since:** 0.6.0

---

### `MissingKeyPolicy missingKeyPolicy()`

Returns the currently configured missing-key policy.

```java
MissingKeyPolicy policy = template.missingKeyPolicy();
System.out.println("Current policy: " + policy);
```

**Returns:** Current missing-key policy

**Since:** 0.6.0

---

## File System Helpers

### `void parseFile(Path path)`

Parses template content from a file using UTF-8 encoding.

```java
Path templatePath = Paths.get("templates/greeting.tmpl");
template.parseFile(templatePath);
```

**Parameters:**
- `path` - The path to the file

**Throws:**
- `TemplateParseException` if parsing fails
- `IOException` if the file cannot be read

**Since:** 0.6.0

---

### `void parseFiles(Path... paths)`

Parses template content from multiple files.

```java
template.parseFiles(
    Paths.get("templates/header.tmpl"),
    Paths.get("templates/footer.tmpl"),
    Paths.get("templates/body.tmpl")
);
```

**Parameters:**
- `paths` - The paths to the files

**Throws:**
- `TemplateParseException` if any template fails to parse
- `IOException` if any file cannot be read

**Note:** If multiple files define the same template name, the last one parsed wins (unless it is empty).

**Since:** 0.6.0

---

### `void parseGlob(Path directory, String glob)`

Parses template files matching a glob pattern in a directory.

```java
// Parse all .tmpl files in the templates directory
template.parseGlob(Paths.get("templates"), "*.tmpl");

// Parse all .html files
template.parseGlob(Paths.get("views"), "*.html");
```

**Parameters:**
- `directory` - The directory to search
- `glob` - The glob pattern (e.g., "*.tmpl", "*.html")

**Throws:**
- `TemplateParseException` if any template fails to parse
- `IOException` if the directory cannot be accessed

**Since:** 0.6.0

---

## Thread Safety

The `Template` class has specific thread safety characteristics:

### During Parsing
**NOT thread-safe.** Parsing modifies internal state and should only be done by a single thread.

### After Parsing
**Thread-safe for execution** if different `Writer` instances are used for each execution. Multiple threads can safely call `execute()` concurrently on the same parsed template.

### For Concurrent Modification
If you need to modify a template (add definitions, change configuration) while other threads are executing it, use the copy constructor:

```java
// Create and parse template once
Template baseTemplate = new Template("master");
baseTemplate.parse("{{.message}}");

// In each thread, create a copy before use
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    final int taskId = i;
    executor.submit(() -> {
        Template threadSafe = new Template(baseTemplate);
        StringWriter writer = new StringWriter();
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Task " + taskId);
        
        try {
            threadSafe.execute(writer, data);
            System.out.println(writer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
}
executor.shutdown();
```

This ensures each thread has its own independent copy that can be modified without affecting others.

---

## Summary

The `Template` class provides a complete API for:
- Creating templates with custom delimiters and functions
- Parsing template text from strings, streams, readers, or files
- Executing templates with arbitrary Java objects as data
- Introspecting template sets to discover defined templates
- Configuring error handling policies
- Managing thread safety through cloning

For more information:
- See [Function API](function-api.md) for custom function implementation
- See [Exception API](exception-api.md) for error handling details
- See [User Guide](../user-guide/template-syntax.md) for template syntax reference
