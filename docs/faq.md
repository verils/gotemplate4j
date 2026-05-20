# Frequently Asked Questions (FAQ)

This document answers the most common questions about gotemplate4j. If you don't find your answer here, please check the [User Guide](../user-guide/) or report an issue on [GitHub](https://github.com/verils/gotemplate4j/issues).

## Table of Contents

- [General Questions](#general-questions)
- [Installation & Setup](#installation--setup)
- [Template Syntax](#template-syntax)
- [Data Handling](#data-handling)
- [Functions](#functions)
- [Error Handling](#error-handling)
- [Performance](#performance)
- [Go Compatibility](#go-compatibility)
- [Troubleshooting](#troubleshooting)

---

## General Questions

### What is gotemplate4j?

Gotemplate4j is a Java implementation of Go's `text/template` package. It allows you to use Go template syntax in Java applications for generating dynamic text output from templates and data.

### What is the purpose of gotemplate4j?

Gotemplate4j is designed for **Java developers who need to work with Go templates**. It's not meant to replace Go's native `text/template` or compete with other Java template engines.

**Use gotemplate4j when:**
- You're a Java developer working in a Go ecosystem
- You need to process existing Go templates in Java applications
- You want familiarity with Go template syntax for cross-language projects
- You're migrating from Go to Java and want to reuse templates

**Don't use gotemplate4j if:**
- You're building a pure Go application (use Go's native `text/template`)
- You need advanced Java template features (use Thymeleaf, FreeMarker, etc.)
- You're looking for maximum performance (Go's native implementation is faster)

Gotemplate4j prioritizes **compatibility and simplicity** over feature richness or performance.

### Is gotemplate4j production-ready?

Yes, gotemplate4j is stable and well-tested with 90%+ code coverage. However, it's designed for simplicity and Go compatibility rather than feature richness. For complex enterprise needs, evaluate whether the feature set meets your requirements.

### What is the current version and release cycle?

Current version is **0.8.0**. The project follows semantic versioning. Check the [CHANGELOG](../CHANGELOG) for release history.

---

## Installation & Setup

For detailed installation instructions, please refer to the [Installation Guide](getting-started/installation.md).

### Quick Links

- **Maven/Gradle setup**: See [Installation Guide - Maven & Gradle](getting-started/installation.md#maven)
- **Manual installation**: See [Installation Guide - Manual Installation](getting-started/installation.md#manual-installation)
- **Verify installation**: See [Installation Guide - Verify Installation](getting-started/installation.md#verify-installation)
- **Troubleshooting**: See [Installation Guide - Troubleshooting](getting-started/installation.md#troubleshooting)

---

## Template Syntax

### How do I access object properties in templates?

Use dot notation. Gotemplate4j supports:
- JavaBean getters: `{{.name}}` calls `getName()`
- Public fields: `{{.field}}` accesses public field directly
- Map keys: `{{.key}}` accesses `map.get("key")`

Example:
```java
public class User {
    private String name;
    public String getName() { return name; }
}

// In template: {{.name}}
```

### How do I check if a value is null or empty?

Use the `if` action with truthiness rules:

```gotemplate
{{if .value}}Value exists and is not empty{{end}}
{{if not .value}}Value is missing or empty{{end}}
```

**Truthiness rules:**
- `null` → false
- `Boolean.FALSE` → false
- Empty string `""` → false
- Empty collections/arrays → false
- Zero numbers → false
- Everything else → true

### How do I iterate over a list?

Use the `range` action:

```gotemplate
{{range $index, $item := .items}}
Item {{$index}}: {{$item}}
{{end}}
```

For simple iteration without index:
```gotemplate
{{range .items}}
Item: {{.}}
{{end}}
```

### Can I nest templates?

Yes, use the `template` action:

```gotemplate
{{define "header"}}<h1>{{.title}}</h1>{{end}}

{{template "header" .}}
<p>Main content</p>
```

### How do I include external template files?

Use the file parsing helpers:

```java
// Parse single file
Template tmpl = Template.parseFile("path/to/template.tmpl");

// Parse multiple files
Template tmpl = Template.parseFiles(
    "header.tmpl",
    "footer.tmpl", 
    "content.tmpl"
);

// Parse using glob pattern
Template tmpl = Template.parseGlob("templates/*.tmpl");
```

---

## Data Handling

### What Java types can I pass to templates?

Gotemplate4j supports:
- **JavaBeans** (objects with getters)
- **Maps** (`Map<String, Object>`)
- **Lists/Arrays** (`List<?>`, arrays)
- **Enums**
- **Optional** (`Optional<T>`)
- **Primitives** and their wrappers
- **Strings**, Numbers, Dates

### How do I pass multiple values to a template?

Wrap them in a Map or a custom object:

**Using Map:**
```java
Map<String, Object> data = new HashMap<>();
data.put("name", "John");
data.put("age", 30);
template.execute(data, writer);
```

**Using Object:**
```java
public class UserData {
    private String name;
    private int age;
    // getters...
}

UserData data = new UserData("John", 30);
template.execute(data, writer);
```

### How do I handle null values?

By default, null values render as empty strings. You can configure this:

```java
// Show error on missing keys
template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);

// Show zero values for missing keys
template.withMissingKeyPolicy(MissingKeyPolicy.ZERO);

// Default: show empty string
template.withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);
```

### Can I use Java 8 Optional?

Yes, gotemplate4j automatically unwraps `Optional`:

```java
Optional<String> name = Optional.of("John");
// In template: {{.name}} renders as "John"

Optional<String> empty = Optional.empty();
// In template: {{.name}} renders as "" (empty)
```

### How do I work with nested objects?

Use dot chains:

```java
public class Order {
    private Customer customer;
    public Customer getCustomer() { return customer; }
}

public class Customer {
    private String name;
    public String getName() { return name; }
}

// In template: {{.customer.name}}
```

---

## Functions

### What built-in functions are available?

Gotemplate4j provides 18+ built-in functions:

**Comparison:** `eq`, `ne`, `lt`, `le`, `gt`, `ge`  
**Logical:** `and`, `or`, `not`  
**String:** `len`, `print`, `printf`, `println`  
**Type conversion:** `js`, `html`, `urlquery`  
**Introspection:** `index`, `call`

See the [Functions Guide](user-guide/functions.md) for complete documentation.

### How do I create custom functions?

Implement the `Function` interface:

```java
public class UpperCaseFunction implements Function {
    @Override
    public String name() {
        return "upper";
    }
    
    @Override
    public Object apply(Object... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("upper expects 1 argument");
        }
        return args[0].toString().toUpperCase();
    }
}

// Register function
Template template = new Template("demo")
    .func(new UpperCaseFunction());
```

Usage in template:
```gotemplate
{{upper "hello"}}  <!-- Outputs: HELLO -->
```

### Can I override built-in functions?

Yes, register a custom function with the same name:

```java
template.func(new CustomPrintFunction()); // Overrides "print"
```

⚠️ **Warning:** Overriding built-in functions may break template compatibility.

### How do I pass arguments to functions?

Functions accept variable arguments:

```gotemplate
{{printf "%s is %d years old" .name .age}}
{{add 1 2 3}}  <!-- Sums all arguments -->
```

In custom functions, access via `args` array:
```java
@Override
public Object apply(Object... args) {
    // args[0], args[1], etc.
}
```

---

## Error Handling

### What types of errors can occur?

Three main exception types:

1. **TemplateParseException** - Syntax errors during parsing
2. **TemplateNotFoundException** - Template file not found
3. **TemplateExecutionException** - Runtime errors during execution

All extend `TemplateException`.

### How do I handle parse errors?

Catch exceptions during template creation:

```java
try {
    Template template = new Template("demo")
        .parse("{{if .name}}Hello {{.name}}{{end"); // Missing }}
} catch (TemplateParseException e) {
    System.err.println("Parse error: " + e.getMessage());
}
```

### How do I handle execution errors?

Catch exceptions during template execution:

```java
try {
    template.execute(data, writer);
} catch (TemplateExecutionException e) {
    System.err.println("Execution error: " + e.getMessage());
}
```

### How do I get detailed error information?

Exceptions include context:

```java
catch (TemplateException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("Template: " + e.getTemplateName());
    System.err.println("Line: " + e.getLineNumber());
    e.printStackTrace();
}
```

### Should I use MissingKeyPolicy.ERROR in production?

It depends:

**Use ERROR when:**
- Developing/testing to catch missing data early
- Data completeness is critical
- You want fail-fast behavior

**Use DEFAULT or ZERO when:**
- Some fields are genuinely optional
- You prefer graceful degradation
- Templates handle missing values with `if` checks

---

## Performance

### How fast is gotemplate4j?

Gotemplate4j is optimized for typical use cases:
- **Parsing**: ~1-5ms for medium templates
- **Execution**: ~0.1-1ms per execution (depends on data complexity)

See [TemplateBenchmark.java](../src/test/java/io/github/verils/gotemplate/TemplateBenchmark.java) for detailed benchmarks.

### How can I improve performance?

**Best practices:**

1. **Reuse parsed templates** - Parse once, execute many times
   ```java
   Template template = new Template("demo").parse("{{.name}}");
   // Reuse 'template' for multiple executions
   ```

2. **Clone templates for thread safety**
   ```java
   Template shared = new Template("demo").parse("{{.name}}");
   // In each thread:
   Template clone = shared.clone();
   clone.execute(data, writer);
   ```

3. **Avoid complex expressions in loops**
   ```gotemplate
   {{/* Bad */}}
   {{range .items}}
     {{call expensiveFunc .}}
   {{end}}
   
   {{/* Good */}}
   Pre-compute values in Java before passing to template
   ```

4. **Use StringBuilder for large outputs**
   ```java
   StringWriter writer = new StringWriter();
   template.execute(data, writer);
   String result = writer.toString();
   ```

### Is gotemplate4j thread-safe?

**Parsed templates are immutable and thread-safe for execution.** However, template configuration (adding functions, etc.) is not thread-safe.

**Recommended pattern:**
```java
// Create and configure once (during startup)
Template baseTemplate = new Template("demo")
    .parse("{{.name}}")
    .func(myFunction);

// Clone for each thread/request
Template threadSafe = baseTemplate.clone();
threadSafe.execute(data, writer);
```

### Does gotemplate4j support template caching?

Not built-in, but easy to implement:

```java
ConcurrentHashMap<String, Template> cache = new ConcurrentHashMap<>();

public Template getTemplate(String name) {
    return cache.computeIfAbsent(name, n -> {
        try {
            return Template.parseFile(n + ".tmpl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
```

---

## Go Compatibility

### How compatible is gotemplate4j with Go templates?

Gotemplate4j implements **core Go `text/template` features**:
- ✅ All control structures (if, range, with, block)
- ✅ Dot notation and variable assignment
- ✅ Built-in functions (eq, len, printf, etc.)
- ✅ Template definition and inclusion
- ✅ Pipeline syntax

**Known differences:** See [Go Compatibility Guide](advanced/compatibility.md)

### Can I use my existing Go templates?

Most simple templates work without changes. Common issues:

**Works as-is:**
```gotemplate
{{.name}}
{{if .active}}Active{{end}}
{{range .items}}{{.}}{{end}}
```

**May need adjustments:**
- Go-specific functions (not implemented)
- Channel operations (not supported in Java)
- Method calls with arguments (limited support)

See [Migration Guide](advanced/migration.md) for details.

### Why doesn't method invocation with arguments work?

Go templates support `{{.Method arg1 arg2}}`, but gotemplate4j limits this for security reasons. Use custom functions instead:

```gotemplate
{{/* Go style (not supported) */}}
{{.FormatDate "2006-01-02"}}

{{/* Java style (use custom function) */}}
{{formatDate .date "yyyy-MM-dd"}}
```

### Does gotemplate4j support Go's html/template?

No, gotemplate4j only implements `text/template`. For HTML escaping, use the `html` function:

```gotemplate
{{html .userInput}}
```

Or pre-escape in Java before passing to template.

---

## Troubleshooting

### My template renders nothing. What's wrong?

Common causes:

1. **Empty data** - Check that your data object has values
2. **Wrong property names** - Verify getter names match template
3. **Syntax errors** - Check for unclosed `{{` or `}}`
4. **Silent failures** - Enable error reporting

Debug:
```java
System.out.println("Data: " + data);
template.execute(data, new PrintWriter(System.out));
```

### I get "unexpected EOF" error. What does it mean?

You have an unclosed action. Check for:
- Missing `{{end}}` for `if`, `range`, `with`, `block`
- Unclosed `{{` or `}}`
- Mismatched braces

Example:
```gotemplate
{{/* Wrong */}}
{{if .name}}
Hello {{.name}}

{{/* Correct */}}
{{if .name}}
Hello {{.name}}
{{end}}
```

### Why does my number comparison fail?

Go templates compare numbers by value, but type matters. Ensure consistent types:

```gotemplate
{{/* May fail if types differ */}}
{{eq .count 5}}

{{/* Safer: convert to same type */}}
{{eq (int .count) 5}}
```

### How do I debug template variables?

Use `printf` to inspect values:

```gotemplate
{{printf "DEBUG: name=%v type=%T" .name .name}}
```

Or print the entire data structure:
```gotemplate
{{printf "DEBUG: data=%v" .}}
```

### My custom function isn't being called. Why?

Check:
1. Function is registered: `.func(myFunction)`
2. Function name matches: `name()` returns correct name
3. Argument count is correct
4. No exceptions thrown in `apply()`

Debug:
```java
System.out.println("Registered functions: " + template.functions());
```

### Template execution is slow. How do I optimize?

See [Performance section](#performance) above. Key points:
- Reuse parsed templates
- Clone for thread safety
- Pre-compute complex values in Java
- Profile to identify bottlenecks

### Can I use gotemplate4j with Spring Boot?

Yes, integrate manually:

```java
@Component
public class TemplateService {
    private final Map<String, Template> templates = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() throws IOException {
        templates.put("email", Template.parseFile("templates/email.tmpl"));
    }
    
    public String render(String name, Object data) {
        Template template = templates.get(name).clone();
        StringWriter writer = new StringWriter();
        template.execute(data, writer);
        return writer.toString();
    }
}
```

Or look for community Spring integration libraries.

---

## v0.9.0 New Features

### What's new in gotemplate4j v0.9.0?

Version 0.9.0 introduces three major enhancements:

1. **Enhanced File Loading APIs** - Load templates from classpath, directories, or with specific encoding
2. **Integer Range Support** - Go-style `{{range $i := 5}}` syntax for iterating over number sequences
3. **Enhanced Error Diagnostics** - Intelligent error messages with typo suggestions and available options listing

See the [CHANGELOG](../CHANGELOG) for complete details.

### How do I load templates from classpath?

Use the new static method:

```java
Template template = Template.parseFromClasspath("/templates/email.tmpl");
```

This is especially useful in web applications and JAR deployments where templates are bundled inside the application.

### How do I load all templates from a directory?

Use `parseDirectory()` to load all `.tmpl` files at once:

```java
Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));

// Access by filename (without extension)
Template header = templates.get("header");
```

### Can I specify file encoding when loading templates?

Yes! Use `parseFile()` with a Charset parameter:

```java
Template template = Template.parseFile(
    Paths.get("template.tmpl"), 
    StandardCharsets.UTF_8
);
```

This ensures correct handling of non-ASCII characters in your templates.

### How does integer range iteration work?

You can now iterate over a sequence of numbers:

```gotemplate
{{range $i := 5}}
  Index: {{$i}}
{{end}}
```

Output:
```
Index: 0
Index: 1
Index: 2
Index: 3
Index: 4
```

This matches Go's `text/template` behavior and is useful for generating repeated content.

### What are enhanced error diagnostics?

Error messages now include helpful information:

**Before v0.9.0:**
```
can't evaluate field FristName
```

**After v0.9.0:**
```
can't evaluate field User.FristName. Available fields: [age, firstName, lastName]. Did you mean 'firstName'?
```

The enhanced diagnostics provide:
- Full field path context
- List of available fields/keys/functions
- Typo suggestions using fuzzy matching
- Argument count information for function errors

See [Error Handling Guide](user-guide/error-handling.md#enhanced-error-diagnostics-v090) for details.

### Is v0.9.0 backward compatible?

Yes! v0.9.0 is fully backward compatible with v0.8.x. All existing templates and code will continue to work without changes.

### What Java version does v0.9.0 require?

v0.9.0 requires **Java 8 or higher**. This is the last version to support Java 8. Starting from v0.10.0, Java 11+ will be required.

---

## Still Have Questions?

- 📖 Browse the [User Guide](user-guide/) for detailed documentation
- 💡 Check [Examples](examples/) for real-world use cases
- 🐛 Report bugs or ask questions on [GitHub Issues](https://github.com/verils/gotemplate4j/issues)
- 📝 Read the [CHANGELOG](../CHANGELOG) for recent updates

---

*Last updated: 2026-05-20 for gotemplate4j v0.9.0*
