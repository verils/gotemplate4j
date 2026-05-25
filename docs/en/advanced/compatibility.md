# Go Template Compatibility

gotemplate4j implements a Java-compatible subset of Go's `text/template` package. The v0.10.0 development line requires Java 11 or later. The goal is practical compatibility for Java applications that need to evaluate Go-style templates, while keeping Java-specific behavior explicit.

This document provides detailed information about what works the same as Go, what differs, and why those differences exist.

---

## Table of Contents

- [Covered Core Behavior](#covered-core-behavior)
- [Java-Specific Behavior](#java-specific-behavior)
- [Missing Keys](#missing-keys)
- [Unsupported or Deferred Go APIs](#unsupported-or-deferred-go-apis)
- [Behavior Differences Summary](#behavior-differences-summary)
- [Java Alternatives](#java-alternatives)

---

## Covered Core Behavior

gotemplate4j supports most common Go template features used in practice.

### Template Syntax

- **Actions and pipelines**: text actions, field chains, variables, parenthesized pipeline arguments, and multi-command pipelines.
- **Comments**: `{{/* comment */}}` style comments that are removed from output.
- **Pipes**: Chain functions with `|` operator (e.g., `{{.name | upper | trim}}`).
- **Variable assignment**: `$var := value` syntax for local variables.

### Control Flow

- **Conditionals**: `if`, `else`, `else if` with boolean evaluation.
- **Loops**: `range` over arrays, lists, maps, and strings.
- **Context switching**: `with` and `else with` for changing the dot context.
- **Loop control**: `break` and `continue` statements within range loops.
- **Else clauses**: `range ... else` and `with ... else` for empty/null cases.

### Template Sets

- **Definitions**: `define` to create named template blocks.
- **Invocation**: `template` to include other templates.
- **Blocks**: Block-style overrides for template inheritance.
- **Re-parsing**: Templates can be parsed multiple times to add definitions.
- **Named execution**: Execute specific templates from a set by name.

### Built-in Functions

All standard Go template functions are supported:

- **Comparison**: `eq`, `ne`, `lt`, `le`, `gt`, `ge`
- **Logical**: `and`, `or`, `not`
- **Collection**: `len`, `index`
- **Formatting**: `printf`, `print`, `println`
- **String escaping**: `html`, `js`, `urlquery`
- **Function calling**: `call` for invoking Function instances
- **gotemplate4j extensions**: `default`, `deepEqual`, `typeof`, `kindOf`

### Error Handling

- **Parse errors**: Include source context showing where parsing failed.
- **Execution errors**: Function runtime failures wrapped in `TemplateExecutionException`.
- **IO errors**: Writer `IOException` values propagate to callers.

## Java-Specific Behavior

These behaviors differ from Go templates due to Java language characteristics.

### Data Access

- **JavaBean getters**: Properties are accessed through getter methods (e.g., `getName()` → `{{.Name}}`).
- **Public fields**: Direct field access is supported for public fields.
- **No-argument methods**: Public methods with no arguments can be called in field chains.
- **Methods with arguments**: Intentionally not supported from templates for security reasons.

Example:
```java
public class User {
    private String name;
    
    public String getName() { return name; }  // Accessible as {{.Name}}
    public int getAge() { return 30; }        // Accessible as {{.Age}}
}
```

### Optional Handling

- **Automatic unwrapping**: `Optional` values are automatically unwrapped.
- **Empty optionals**: Behave like `null` (falsey, print as empty).

```java
Optional<String> name = Optional.of("Alice");
// Template: {{.name}} → "Alice"

Optional<String> empty = Optional.empty();
// Template: {{.empty}} → "" (empty string)
```

### Enum Support

- **toString() rendering**: Enums render using their `toString()` method.
- **Method access**: Public no-argument methods like `name()` and `ordinal()` are accessible.

```java
public enum Status { ACTIVE, INACTIVE }

// Template examples:
{{.Status}}           // → "ACTIVE" (via toString)
{{.Status.name}}      // → "ACTIVE" (via name() method)
{{.Status.ordinal}}   // → 0 (via ordinal() method)
```

### Null Handling

- **Falsey evaluation**: `null` values evaluate to false in conditionals.
- **Empty output**: `null` values print as empty strings.
- **Safe navigation**: Field chains stop at `null` without throwing exceptions (by default).

```gotemplate
{{if .Name}}Name exists{{else}}No name{{end}}
{{.Name}}  <!-- Prints nothing if null -->
```

### Map Iteration Order

- **Java Map order**: Iteration follows the map's implementation order.
- **No automatic sorting**: Unlike Go, gotemplate4j does not sort map keys.
- **Deterministic order**: Use `LinkedHashMap` when output order matters.

```java
// Go behavior: keys are sorted alphabetically
// gotemplate4j behavior: keys follow insertion order
Map<String, String> map = new LinkedHashMap<>();
map.put("z", "last");
map.put("a", "first");
// Range will produce: z, a (insertion order)
```

### Custom Extensions

These functions are gotemplate4j additions, not part of Go's standard library:

- **`default`**: Provide fallback values (`{{.value | default "fallback"}}`)
- **`deepEqual`**: Deep comparison of complex objects
- **`typeof`**: Get the Java type name of a value
- **`kindOf`**: Get the kind/category of a value

## Missing Keys

The default behavior preserves backward compatibility: missing map keys evaluate to `null` and print as empty output.

### Configuration

```java
Template template = new Template("demo");
template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

Or using Go-style string option:

```java
template.option("missingkey=error");
```

### Supported Policies

- **`DEFAULT`**: Missing map keys evaluate to `null`. This is the default behavior.
- **`ZERO`**: Returns a Java zero-like value when the target type is knowable. For missing map keys without a knowable type, falls back to `null`.
- **`ERROR`**: Throws `TemplateExecutionException` for missing map keys and missing field-chain segments after a `null` value.

### Use Cases

- **Development**: Use `ERROR` policy to catch issues early during development.
- **Production**: Use `DEFAULT` or `ZERO` for graceful degradation in production.
- **Strict validation**: Use `ERROR` when all required fields must be present.

## Unsupported or Deferred Go APIs

These Go template features are not currently supported in gotemplate4j.

### Filesystem and IO

- **`ParseFS`**: Go filesystem abstractions are not available. Use Java streams, readers, or caller-managed file loading instead.
  ```java
  // Instead of ParseFS, use:
  template.parseFile(Paths.get("template.tmpl"));
  template.parseFiles(Paths.get("file1.tmpl"), Paths.get("file2.tmpl"));
  template.parseGlob(Paths.get("templates/*.tmpl"));
  ```

### Range Limitations

- **Integer range**: `range $i := 5` (iterating over integers) is not yet supported.
- **Channels**: Go channels cannot be used from Java.
- **Iterators**: `iter.Seq` and `iter.Seq2` are Go-specific and not available.

Workaround: Convert data to Java arrays, collections, or maps before passing to templates.

### Method Invocation

- **Methods with arguments**: General method calls with arguments from templates are intentionally disabled for security reasons.
  ```java
  // NOT supported in templates:
  // {{.object.method "arg1" "arg2"}}
  
  // Instead, use custom functions:
  Function myFunc = args -> { /* logic */ };
  template.addFunction("myFunc", myFunc);
  // Template: {{myFunc .object "arg1" "arg2"}}
  ```

### Function Values

- **Go function-valued fields**: Go's ability to store functions in struct fields or map entries is not supported.
- **`call` function**: In gotemplate4j, `call` accepts only `Function` interface instances, not arbitrary methods.

### Numeric Formatting

- **Complex number support**: Full Go numeric and complex formatting parity is not implemented.
- **Format verbs**: Some advanced `printf` format verbs may have limited support compared to Go.

---

## Behavior Differences Summary

This table summarizes key differences between Go templates and gotemplate4j:

| Feature | Go `text/template` | gotemplate4j | Reason |
|---------|-------------------|--------------|--------|
| Map iteration order | Sorted keys | Insertion order | Java Map behavior |
| Missing keys | Configurable (default: error) | Configurable (default: null) | Backward compatibility |
| Null display | `<no value>` | Empty string | Java convention |
| Method calls | Any public method | No-argument methods only | Security |
| Integer range | Supported | Not yet supported | Implementation priority |
| Channels | Supported | Not applicable | Java doesn't have channels |
| Optional | N/A | Auto-unwrapped | Java-specific feature |
| Enums | N/A | toString() + methods | Java-specific feature |
| Custom functions | Go funcs | `Function` interface | Java type system |
| ParseFS | Supported | Not supported | Java IO model differs |

---

## Java Alternatives

These are the recommended approaches for common Go template patterns in gotemplate4j.

### Template Loading

Instead of Go's filesystem abstractions, use Java IO:

```java
// Load from file
Template template = new Template("mytemplate");
template.parseFile(Paths.get("templates/mytemplate.tmpl"));

// Load from multiple files
template.parseFiles(
    Paths.get("base.tmpl"),
    Paths.get("partials/header.tmpl"),
    Paths.get("partials/footer.tmpl")
);

// Load from glob pattern
template.parseGlob(Paths.get("templates/*.tmpl"));

// Load from InputStream or Reader (caller-managed IO)
try (InputStream is = getClass().getResourceAsStream("/template.tmpl")) {
    template.parse(is);
}
```

### Data Models

Use JavaBeans, public fields, maps, and enums as template data models:

```java
// JavaBean (recommended for structured data)
public class User {
    private String name;
    private int age;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

// Map (for dynamic data)
Map<String, Object> data = new HashMap<>();
data.put("name", "Alice");
data.put("age", 30);

// Enum
public enum Status { ACTIVE, INACTIVE }
```

### Custom Functions

Implement the `Function` interface for custom logic:

```java
import io.github.verils.gotemplate.Function;

// Simple function
Function upperCase = args -> {
    return ((String) args[0]).toUpperCase();
};

// Function with validation
Function divide = args -> {
    if (args.length != 2) {
        throw new IllegalArgumentException("divide requires 2 arguments");
    }
    double a = ((Number) args[0]).doubleValue();
    double b = ((Number) args[1]).doubleValue();
    if (b == 0) {
        throw new ArithmeticException("Division by zero");
    }
    return a / b;
};

// Register functions
Map<String, Function> functions = new HashMap<>();
functions.put("upper", upperCase);
functions.put("divide", divide);

Template template = new Template("demo", functions);
```

### Template Introspection

Inspect parsed template sets before execution:

```java
Template template = new Template("base");
template.parseFiles(Paths.get("base.tmpl"), Paths.get("page.tmpl"));

// Get template name
String name = template.name();  // "base"

// List all defined templates
Set<String> names = template.definedTemplates();  // ["base", "page", "header", ...]

// Check if template exists
boolean hasHeader = template.hasTemplate("header");  // true or false

// Lookup specific template (returns independent copy)
Template headerTemplate = template.lookup("header");

// Get all templates as map
Map<String, Template> all = template.templates();
```

### Error Handling

Handle different types of errors appropriately:

```java
try {
    // Parse phase
    Template template = new Template("demo");
    template.parse(templateText);
    
    // Execution phase
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    
} catch (TemplateParseException e) {
    // Syntax errors in template
    System.err.println("Parse error: " + e.getMessage());
    
} catch (TemplateExecutionException e) {
    // Runtime errors during execution
    System.err.println("Execution error: " + e.getMessage());
    
} catch (TemplateNotFoundException e) {
    // Referenced template not found
    System.err.println("Template not found: " + e.getMessage());
}
```

---

## See Also

- [Migration Guide](migration.md) - Step-by-step migration from Go templates
- [User Guide](user-guide/) - Comprehensive usage documentation
- [Examples](examples/) - Real-world code examples
- [API Reference](api-reference/) - Detailed API documentation
