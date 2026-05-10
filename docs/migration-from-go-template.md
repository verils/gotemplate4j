# Migration from Go text/template

This guide provides a comprehensive mapping of common Go `text/template` usage patterns to gotemplate4j equivalents. It's designed to help developers migrate existing Go templates to Java applications with minimal friction.

---

## Table of Contents

- [Data Models](#data-models)
- [Null and Missing Values](#null-and-missing-values)
- [Map Iteration](#map-iteration)
- [Custom Functions](#custom-functions)
- [Template Sets](#template-sets)
- [Control Flow](#control-flow)
- [Built-in Functions](#built-in-functions)
- [Go-Only APIs to Avoid](#go-only-apis-to-avoid)
- [Common Migration Patterns](#common-migration-patterns)
- [Testing Your Migration](#testing-your-migration)

---

## Data Models

Go structs usually map to JavaBeans in gotemplate4j.

### Go Struct → JavaBean

**Go:**
```go
type User struct {
    Name string
    Age  int
}
```

**Java:**
```java
public class User {
    private String name;
    private int age;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
}
```

**Template (same for both):**
```gotemplate
{{.Name}} is {{.Age}} years old
```

Templates can access this with `{{.Name}}`. Public fields are also supported, and maps can be used when the data shape is dynamic.

### Enum Support

Enums render through `toString()` and expose public no-argument methods:

```java
public enum Status {
    ACTIVE,
    INACTIVE;
    
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
```

```gotemplate
{{.Status}}           // → "active" (via toString)
{{.Status.name}}      // → "ACTIVE" (via name() method)
{{.Status.ordinal}}   // → 0 (via ordinal() method)
```

## Null and Missing Values

By default, `null` and missing map keys are falsey and print as empty output:

```gotemplate
{{if .Name}}{{.Name}}{{else}}anonymous{{end}}
```

### Go vs Java Behavior

**Go:**
- Default: Missing keys cause an error
- Can configure with `.Option("missingkey=error/invalid/zero")`
- Null values display as `<no value>`

**gotemplate4j:**
- Default: Missing keys return `null` (prints as empty)
- Can configure with `withMissingKeyPolicy()` or `.option("missingkey=...")`
- Null values print as empty string

### Configuring Strict Mode

For stricter production checks, configure:

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

Or using Go-style option string:

```java
template.option("missingkey=error");
```

`MissingKeyPolicy.ZERO` is available for Go-style naming, but Java often cannot infer a zero value for absent map entries.

## Map Iteration

Go templates sort maps with basic ordered key types. gotemplate4j follows Java `Map` iteration order instead.

### Go Behavior

```go
// Go automatically sorts map keys for deterministic output
{{range $key, $value := .MyMap}}
  {{$key}}: {{$value}}
{{end}}
// Output: keys in alphabetical order
```

### gotemplate4j Behavior

```java
// Java preserves insertion order (for LinkedHashMap)
Map<String, String> map = new LinkedHashMap<>();
map.put("z", "last");
map.put("a", "first");
data.put("MyMap", map);
```

```gotemplate
{{range $key, $value := .MyMap}}
  {{$key}}: {{$value}}
{{end}}
// Output: z: last, a: first (insertion order)
```

**Recommendation**: Use `LinkedHashMap` when output order matters, or sort keys in Java before passing to template.

---

## Control Flow

Control flow syntax is identical between Go and gotemplate4j.

### Conditionals

```gotemplate
{{if .Condition}}
  True branch
{{else if .OtherCondition}}
  Other branch
{{else}}
  False branch
{{end}}
```

### Loops

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{else}}
  No items
{{end}}
```

### Context Switching

```gotemplate
{{with .User}}
  Name: {{.Name}}
  Email: {{.Email}}
{{else}}
  No user logged in
{{end}}
```

### Loop Control

```gotemplate
{{range .Items}}
  {{if .Skip}}{{continue}}{{end}}
  {{if .Stop}}{{break}}{{end}}
  {{.Value}}
{{end}}
```

---

## Built-in Functions

All standard Go template functions work the same way in gotemplate4j.

### Comparison Functions

```gotemplate
{{eq .Value 10}}    // Equal
{{ne .Value 10}}    // Not equal
{{lt .Value 10}}    // Less than
{{le .Value 10}}    // Less than or equal
{{gt .Value 10}}    // Greater than
{{ge .Value 10}}    // Greater than or equal
```

### Logical Functions

```gotemplate
{{and .A .B}}       // Logical AND
{{or .A .B}}        // Logical OR
{{not .A}}          // Logical NOT
```

### Collection Functions

```gotemplate
{{len .Items}}      // Length of array/slice/map/string
{{index .Items 0}}  // Get element at index
```

### Formatting Functions

```gotemplate
{{printf "%s: %d" .Name .Age}}  // Formatted string
{{print .Name .Age}}            // Concatenate with spaces
{{println .Name .Age}}          // Concatenate with newline
```

### String Escaping

```gotemplate
{{html .Content}}     // HTML escape
{{js .Content}}       // JavaScript escape
{{urlquery .Content}} // URL query escape
```

### gotemplate4j Extensions

These are additional functions not in Go's standard library:

```gotemplate
{{default .Value "fallback"}}  // Provide default value
{{deepEqual .A .B}}            // Deep equality check
{{typeof .Value}}              // Get Java type name
{{kindOf .Value}}              // Get value kind/category
```

## Custom Functions

Go template functions map to gotemplate4j `Function` instances.

### Go Custom Function

```go
funcMap := template.FuncMap{
    "upper": strings.ToUpper,
    "add": func(a, b int) int { return a + b },
}
tmpl := template.New("demo").Funcs(funcMap)
```

### gotemplate4j Custom Function

```java
import io.github.verils.gotemplate.Function;

Map<String, Function> functions = new HashMap<>();
functions.put("upper", args -> String.valueOf(args[0]).toUpperCase());
functions.put("add", args -> {
    double a = ((Number) args[0]).doubleValue();
    double b = ((Number) args[1]).doubleValue();
    return a + b;
});

Template template = new Template("demo", functions);
```

Function failures are wrapped in `TemplateExecutionException` during execution.

## Template Sets

Use `define` and `template` in template text as usual. After parsing, inspect the set with introspection APIs.

### Defining Templates

```gotemplate
{{define "header"}}
<header>
  <h1>{{.Title}}</h1>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>&copy; 2026</p>
</footer>
{{end}}
```

### Using Templates

```gotemplate
{{template "header" .}}
<main>Content here</main>
{{template "footer" .}}
```

### Introspection API

After parsing, inspect the template set:

```java
Template template = new Template("base");
template.parseFiles(Paths.get("base.tmpl"), Paths.get("partials.tmpl"));

// Get template name
String name = template.name();  // "base"

// List all defined templates
Set<String> names = template.definedTemplates();  // ["base", "header", "footer"]

// Check if template exists
boolean hasHeader = template.hasTemplate("header");  // true

// Lookup specific template (returns independent copy)
Template headerTemplate = template.lookup("header");

// Get all templates as map
Map<String, Template> all = template.templates();
```

`lookup` and `templates` return independent template copies so callers cannot mutate the original parsed set by accident.

---

## Go-Only APIs to Avoid

These Go features don't have direct equivalents in gotemplate4j.

### Filesystem Operations

**Go:**
```go
tmpl, err := template.ParseFS(os.DirFS("templates"), "*.tmpl")
```

**gotemplate4j:**
```java
// Use Java IO instead
Template template = new Template("demo");
template.parseFile(Paths.get("templates/demo.tmpl"));
template.parseFiles(Paths.get("templates/base.tmpl"), Paths.get("templates/page.tmpl"));
template.parseGlob(Paths.get("templates/*.tmpl"));
```

### Channels and Iterators

**Go:**
```go
// Range over channels
{{range $item := .Channel}}
  {{$item}}
{{end}}

// Range over integers
{{range $i := 5}}
  {{$i}}
{{end}}
```

**gotemplate4j:**
```java
// Convert to collections first
List<Integer> numbers = Arrays.asList(0, 1, 2, 3, 4);
data.put("numbers", numbers);
```

```gotemplate
{{range $i := .numbers}}
  {{$i}}
{{end}}
```

### Method Calls with Arguments

**Go:**
```go
// Can call methods with arguments
type User struct { Name string }
func (u User) Greet(greeting string) string {
    return greeting + ", " + u.Name
}
```

```gotemplate
{{.User.Greet "Hello"}}  // Works in Go
```

**gotemplate4j:**
```java
// NOT supported - use custom functions instead
Function greet = args -> {
    User user = (User) args[0];
    String greeting = (String) args[1];
    return greeting + ", " + user.getName();
};

Map<String, Function> functions = new HashMap<>();
functions.put("greet", greet);
```

```gotemplate
{{greet .User "Hello"}}  // Use function instead
```

### Function Values

**Go:**
```go
// Can store functions in struct fields or maps
type Data struct {
    Transform func(string) string
}
```

```gotemplate
{{call .Transform "input"}}
```

**gotemplate4j:**
```java
// Use Function interface
Function transform = args -> ((String) args[0]).toUpperCase();
data.put("transform", transform);
```

```gotemplate
{{call .transform "input"}}  // Works with Function instances
```

---

## Common Migration Patterns

### Pattern 1: Simple Template

**Go:**
```go
tmpl, _ := template.New("demo").Parse("Hello, {{.Name}}!")
var buf bytes.Buffer
tmpl.Execute(&buf, map[string]string{"Name": "Alice"})
```

**gotemplate4j:**
```java
Template template = new Template("demo");
template.parse("Hello, {{.Name}}!");

StringWriter writer = new StringWriter();
Map<String, String> data = new HashMap<>();
data.put("Name", "Alice");
template.execute(writer, data);
```

### Pattern 2: Template with Functions

**Go:**
```go
funcMap := template.FuncMap{
    "upper": strings.ToUpper,
}
tmpl := template.New("demo").Funcs(funcMap)
tmpl.Parse("{{upper .Name}}")
```

**gotemplate4j:**
```java
Map<String, Function> functions = new HashMap<>();
functions.put("upper", args -> ((String) args[0]).toUpperCase());

Template template = new Template("demo", functions);
template.parse("{{upper .Name}}");
```

### Pattern 3: Template Files

**Go:**
```go
tmpl, _ := template.ParseFiles("base.tmpl", "page.tmpl")
```

**gotemplate4j:**
```java
Template template = new Template("base");
template.parseFiles(
    Paths.get("base.tmpl"),
    Paths.get("page.tmpl")
);
```

### Pattern 4: Error Handling

**Go:**
```go
tmpl, err := template.New("demo").Parse(text)
if err != nil {
    log.Fatal(err)
}

err = tmpl.Execute(&buf, data)
if err != nil {
    log.Fatal(err)
}
```

**gotemplate4j:**
```java
try {
    Template template = new Template("demo");
    template.parse(text);
    
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateParseException e) {
    // Handle parse error
    e.printStackTrace();
} catch (TemplateExecutionException e) {
    // Handle execution error
    e.printStackTrace();
}
```

---

## Testing Your Migration

### Step 1: Verify Syntax

Test that all templates parse correctly:

```java
@Test
void testTemplateParsing() {
    Template template = new Template("test");
    assertDoesNotThrow(() -> template.parse(templateText));
}
```

### Step 2: Compare Output

Run the same data through both Go and Java templates and compare output:

```java
@Test
void testOutputMatches() throws Exception {
    Map<String, Object> data = createTestData();
    
    // Execute gotemplate4j
    Template template = new Template("test");
    template.parse(templateText);
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    String javaOutput = writer.toString();
    
    // Compare with expected output (from Go)
    assertEquals(expectedGoOutput, javaOutput);
}
```

### Step 3: Test Edge Cases

Test null values, empty collections, missing keys:

```java
@Test
void testNullHandling() throws Exception {
    Template template = new Template("test");
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    template.parse("{{if .Value}}{{.Value}}{{else}}empty{{end}}");
    
    Map<String, Object> data = new HashMap<>();
    // Value is missing
    
    assertThrows(TemplateExecutionException.class, () -> {
        template.execute(new StringWriter(), data);
    });
}
```

### Step 4: Performance Testing

Benchmark template execution:

```java
@Benchmark
public void benchmarkTemplateExecution() throws Exception {
    Template template = new Template("test");
    template.parse(templateText);
    
    for (int i = 0; i < 1000; i++) {
        StringWriter writer = new StringWriter();
        template.execute(writer, testData);
    }
}
```

---

## See Also

- [Compatibility Guide](go-template-compatibility.md) - Detailed compatibility information
- [User Guide](user-guide/) - Comprehensive usage documentation
- [Examples](examples/) - Real-world code examples
- [API Reference](api-reference/) - Detailed API documentation
