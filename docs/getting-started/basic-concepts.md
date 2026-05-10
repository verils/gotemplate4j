# Basic Concepts

This guide explains the fundamental concepts of gotemplate4j to help you understand how templates work.

## What is a Template?

A template is a text document with embedded expressions that are evaluated and replaced with actual values during execution. Templates separate presentation logic from data, making it easier to generate dynamic content.

### Template Structure

A template consists of two types of content:

1. **Static Text**: Regular text that appears in the output unchanged
2. **Actions**: Expressions enclosed in `{{` and `}}` that are evaluated

```gotemplate
Hello, {{.Name}}!  <!-- "Hello, " is static text, "{{.Name}}" is an action -->
```

## The Data Context (Dot)

The dot (`.`) represents the current data context. It's the most important concept in gotemplate4j.

### Accessing Fields

Use the dot followed by a field name to access data:

```java
// Java code
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");
data.put("Age", 30);

Template template = new Template("demo");
template.parse("Name: {{.Name}}, Age: {{.Age}}");
```

**Output:**
```
Name: Alice, Age: 30
```

### Nested Data

Access nested fields using dot notation:

```java
Map<String, Object> user = new HashMap<>();
user.put("Name", "Bob");

Map<String, Object> address = new HashMap<>();
address.put("City", "Beijing");
user.put("Address", address);

template.parse("{{.Name}} lives in {{.Address.City}}");
```

**Output:**
```
Bob lives in Beijing
```

### The Dot Changes in Different Contexts

The value of `.` changes depending on the context:

```gotemplate
{{range .Items}}
  {{.}}  <!-- Here, . refers to the current item in the loop -->
{{end}}
```

## Actions

Actions are expressions enclosed in `{{` and `}}`. They control what appears in the output.

### Variable Substitution

The simplest action outputs a value:

```gotemplate
{{.Name}}
{{.Age}}
{{.Price}}
```

### Conditional Actions

Use `if`/`else`/`end` for conditional logic:

```gotemplate
{{if .IsActive}}
  User is active
{{else}}
  User is inactive
{{end}}
```

### Loop Actions

Use `range` to iterate over collections:

```gotemplate
{{range .Items}}
  - {{.}}
{{end}}
```

You can also capture index and value:

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

### With Action

The `with` action changes the dot context:

```gotemplate
{{with .User}}
  Name: {{.Name}}
  Age: {{.Age}}
{{end}}
```

Inside the `with` block, `.` refers to `.User` instead of the root data.

## Pipelines

Pipelines chain operations together using the pipe operator (`|`):

```gotemplate
{{.Name | upper}}           <!-- Convert to uppercase -->
{{.Text | lower | trim}}    <!-- Chain multiple operations -->
{{printf "%.2f" .Price}}    <!-- Format number -->
```

### How Pipelines Work

1. The value flows from left to right
2. Each function receives the output of the previous step
3. The final result is rendered

```gotemplate
{{"hello" | upper | printf "Greeting: %s"}}
<!-- Output: Greeting: HELLO -->
```

## Functions

Functions perform operations on data. gotemplate4j includes many built-in functions.

### Using Functions

Call functions with arguments:

```gotemplate
{{len .Items}}              <!-- Get length -->
{{upper .Name}}             <!-- Convert to uppercase -->
{{add 1 2}}                 <!-- Add numbers -->
```

### Common Built-in Functions

| Function | Description | Example |
|----------|-------------|---------|
| `len` | Get length | `{{len .Items}}` |
| `upper` | Uppercase | `{{upper .Text}}` |
| `lower` | Lowercase | `{{lower .Text}}` |
| `trim` | Trim whitespace | `{{trim .Text}}` |
| `add` | Addition | `{{add 1 2}}` |
| `sub` | Subtraction | `{{sub 5 3}}` |
| `printf` | Format string | `{{printf "%d" .Num}}` |
| `eq` | Equal | `{{eq .A .B}}` |
| `ne` | Not equal | `{{ne .A .B}}` |
| `and` | Logical AND | `{{and .A .B}}` |
| `or` | Logical OR | `{{or .A .B}}` |
| `not` | Logical NOT | `{{not .Flag}}` |

See [Functions Guide](../user-guide/functions.md) for complete reference.

## Template Sets

Templates can contain multiple named templates that reference each other.

### Define Templates

Use `define` to create named templates:

```gotemplate
{{define "header"}}
<html>
<head><title>{{.Title}}</title></head>
<body>
{{end}}

{{define "footer"}}
</body>
</html>
{{end}}
```

### Invoke Templates

Use `template` to invoke defined templates:

```gotemplate
{{template "header" .}}
<h1>Hello, {{.Name}}!</h1>
{{template "footer" .}}
```

### Block Templates

Use `block` to define and execute inline (Go-compatible):

```gotemplate
{{block "content" .}}Default content{{end}}
```

See [Template Sets Guide](../user-guide/template-sets.md) for details.

## Comments

Add comments using `{{/*` and `*/}}`:

```gotemplate
{{/* This is a comment */}}
Hello, {{.Name}}! {{/* Inline comment */}}
```

Comments don't appear in the output.

## Whitespace Control

By default, actions preserve surrounding whitespace. Use `-` to trim whitespace:

```gotemplate
{{- if .Show -}}
  Visible
{{- end -}}
```

The `-` removes whitespace on that side of the action.

## Error Handling

Templates can fail during parsing or execution.

### Parse Errors

Occur when template syntax is invalid:

```java
try {
    template.parse("{{invalid syntax");
} catch (TemplateParseException e) {
    System.err.println("Parse error: " + e.getMessage());
}
```

### Execution Errors

Occur when template runs with problematic data:

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("Execution error: " + e.getMessage());
}
```

See [Error Handling Guide](../user-guide/error-handling.md) for best practices.

## Data Types

gotemplate4j works with various Java data types:

### Supported Types

- **Strings**: `"hello"`
- **Numbers**: `int`, `long`, `float`, `double`, `BigInteger`, `BigDecimal`
- **Booleans**: `true`, `false`
- **Collections**: `List`, `Set`, `Array`, `Map`
- **JavaBeans**: Objects with getter methods
- **Enums**: Enum constants
- **Optional**: `Optional<T>` values
- **Null**: Handled based on MissingKeyPolicy

### JavaBeans

Public fields and getter methods are accessible:

```java
public class User {
    private String name;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// In template: {{.Name}} calls getName()
```

### Maps

Map keys are accessed like fields:

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");

// In template: {{.Name}} accesses map key "Name"
```

## Execution Flow

Understanding how templates execute helps debug issues:

1. **Parse Phase**: Template text is parsed into an Abstract Syntax Tree (AST)
2. **Execute Phase**: AST is traversed with data context
3. **Render Phase**: Actions produce output text

```java
Template template = new Template("demo");
template.parse("Hello, {{.Name}}!");  // Parse phase

StringWriter writer = new StringWriter();
template.execute(writer, data);       // Execute + Render phases
```

## Best Practices

### 1. Keep Templates Simple

Avoid complex logic in templates. Move business logic to Java code.

❌ **Bad:**
```gotemplate
{{if and (gt .Age 18) (eq .Status "active") (or .Premium .VIP)}}
```

✅ **Good:**
```java
data.put("CanAccess", user.isEligible());
```
```gotemplate
{{if .CanAccess}}
```

### 2. Use Meaningful Names

Name variables and templates clearly:

❌ **Bad:**
```gotemplate
{{range .X}}...{{end}}
```

✅ **Good:**
```gotemplate
{{range .Users}}...{{end}}
```

### 3. Handle Missing Values

Configure how missing values are handled:

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);  // Fail on missing keys
```

### 4. Test with Various Data

Test templates with:
- Empty collections
- Null values
- Edge cases (zero, negative numbers)
- Large datasets

### 5. Reuse Templates

Define reusable components:

```gotemplate
{{define "user-card"}}
<div class="card">
  <h3>{{.Name}}</h3>
  <p>{{.Email}}</p>
</div>
{{end}}

{{range .Users}}
  {{template "user-card" .}}
{{end}}
```

## Next Steps

Now that you understand the basics:

- 📚 Read the [Template Syntax Guide](../user-guide/template-syntax.md) for detailed syntax reference
- 🔧 Explore [Functions](../user-guide/functions.md) for all available functions
- 💡 Check out [Examples](../examples/basic-examples.md) for practical use cases
- 🔍 Learn about [Data Models](../user-guide/data-models.md) for working with Java objects

## Quick Reference

### Action Types

| Type | Syntax | Example |
|------|--------|---------|
| Variable | `{{.Field}}` | `{{.Name}}` |
| If | `{{if ...}}{{end}}` | `{{if .Active}}Yes{{end}}` |
| Range | `{{range ...}}{{end}}` | `{{range .Items}}{{.}}{{end}}` |
| With | `{{with ...}}{{end}}` | `{{with .User}}{{.Name}}{{end}}` |
| Template | `{{template ...}}` | `{{template "header" .}}` |
| Define | `{{define ...}}{{end}}` | `{{define "foo"}}...{{end}}` |
| Block | `{{block ...}}{{end}}` | `{{block "foo" .}}...{{end}}` |

### Pipeline Examples

```gotemplate
{{.Name | upper}}
{{printf "Hello, %s!" .Name}}
{{len .Items | printf "Count: %d"}}
```

### Common Patterns

```gotemplate
<!-- Default value -->
{{if .Name}}{{.Name}}{{else}}Anonymous{{end}}

<!-- Loop with index -->
{{range $i, $v := .Items}}{{$i}}: {{$v}}{{end}}

<!-- Nested conditionals -->
{{if .User}}{{if .User.Name}}{{.User.Name}}{{end}}{{end}}
```

---

**Need more details?** Continue to the [User Guide](../user-guide/template-syntax.md) for comprehensive documentation.
