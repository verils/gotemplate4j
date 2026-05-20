# Template Syntax Reference

This guide provides a complete reference for gotemplate4j template syntax, compatible with Go's `text/template` package.

## Overview

gotemplate4j templates are text files with embedded actions. Actions are enclosed in double curly braces `{{` and `}}`.

### Basic Structure

```gotemplate
Static text {{action}} more static text {{another action}}
```

Actions are evaluated and replaced with their results during template execution.

## Delimiters

### Standard Delimiters

Default delimiters are `{{` and `}}`:

```gotemplate
{{.Name}}
```

### Custom Delimiters

You can change delimiters when creating a template:

```java
Template template = new Template("demo", "{{%", "%}}");
template.parse("<% .Name %>");
```

### Whitespace Trimming

Use `-` to trim whitespace:

```gotemplate
{{- .Name -}}
```

- `{{-` trims whitespace before the action
- `-}}` trims whitespace after the action

Example:

```gotemplate
Hello   {{- .Name -}}   !
```

With `.Name = "Alice"`, output is: `HelloAlice!` (spaces removed)

## Comments

Comments don't appear in output:

```gotemplate
{{/* This is a comment */}}
```

Multi-line comments:

```gotemplate
{{/*
  This is a
  multi-line comment
*/}}
```

Inline comments:

```gotemplate
Hello {{.Name}} {{/* display name */}}
```

## Variables

### The Dot (`.`)

The dot represents the current data context:

```gotemplate
{{.}}           <!-- Entire data -->
{{.Name}}       <!-- Name field -->
{{.User.Age}}   <!-- Nested field -->
```

### Variable Assignment

Create variables with `:=`:

```gotemplate
{{$name := .Name}}
{{$count := len .Items}}
```

Variables start with `$` and are scoped to their block:

```gotemplate
{{range .Items}}
  {{$item := .}}  <!-- $item is scoped to this range block -->
{{end}}
<!-- $item is not accessible here -->
```

### Multiple Assignment

Assign multiple variables at once:

```gotemplate
{{$index, $value := range .Items}}
```

## Pipelines

Pipelines chain operations using `|`:

```gotemplate
{{.Name | upper}}
{{.Text | lower | trim}}
{{printf "Hello, %s!" .Name}}
```

### Pipeline Rules

1. Data flows left to right
2. Each function receives previous output as last argument
3. Final result is rendered

Examples:

```gotemplate
<!-- These are equivalent -->
{{upper .Name}}
{{.Name | upper}}

<!-- Chaining -->
{{.Text | trim | upper | printf "Result: %s"}}
```

### Parentheses

Use parentheses for clarity or to override precedence:

```gotemplate
{{printf "%s (%d)" (.Name | upper) (len .Items)}}
```

## Conditional Actions

### If Statement

Basic if:

```gotemplate
{{if .IsActive}}
  User is active
{{end}}
```

If/Else:

```gotemplate
{{if .IsActive}}
  Active
{{else}}
  Inactive
{{end}}
```

If/Else If/Else:

```gotemplate
{{if eq .Status "active"}}
  Active
{{else if eq .Status "pending"}}
  Pending
{{else}}
  Unknown
{{end}}
```

### Truthiness

Values are considered **false** if they are:
- `false` (boolean)
- `0` (any numeric type)
- `null` or missing
- Empty string `""`
- Empty collection (length 0)

All other values are **true**.

Examples:

```gotemplate
{{if 0}}False{{end}}        <!-- Won't print -->
{{if 1}}True{{end}}         <!-- Will print -->
{{if ""}}False{{end}}       <!-- Won't print -->
{{if "x"}}True{{end}}       <!-- Will print -->
{{if nil}}False{{end}}      <!-- Won't print -->
```

### Comparison Operators

| Operator | Meaning | Example |
|----------|---------|---------|
| `eq` | Equal | `{{eq .A .B}}` |
| `ne` | Not equal | `{{ne .A .B}}` |
| `lt` | Less than | `{{lt .A .B}}` |
| `lte` | Less than or equal | `{{lte .A .B}}` |
| `gt` | Greater than | `{{gt .A .B}}` |
| `gte` | Greater than or equal | `{{gte .A .B}}` |

Examples:

```gotemplate
{{if eq .Age 18}}Exactly 18{{end}}
{{if gt .Age 18}}Adult{{end}}
{{if lte .Score 100}}Valid score{{end}}
```

### Logical Operators

| Operator | Meaning | Example |
|----------|---------|---------|
| `and` | Logical AND | `{{and .A .B}}` |
| `or` | Logical OR | `{{or .A .B}}` |
| `not` | Logical NOT | `{{not .Flag}}` |

Examples:

```gotemplate
{{if and .IsActive .IsVerified}}Active and verified{{end}}
{{if or .IsAdmin .IsModerator}}Has permissions{{end}}
{{if not .IsDeleted}}Not deleted{{end}}
```

Short-circuit evaluation:

```gotemplate
<!-- If .A is false, .B is not evaluated -->
{{if and .A .B}}...{{end}}

<!-- If .A is true, .B is not evaluated -->
{{if or .A .B}}...{{end}}
```

## Range Action

Iterate over collections (arrays, lists, maps, channels).

### Basic Range

```gotemplate
{{range .Items}}
  {{.}}
{{end}}
```

### With Index

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

### Range Over Map

```gotemplate
{{range $key, $value := .MapData}}
  {{$key}}: {{$value}}
{{end}}
```

Note: Map iteration order is not guaranteed unless you enable map key sorting.

### Range Over Integer (Go-compatible)

Iterate from 0 to n-1:

**Single Variable Form**:
```gotemplate
{{range $i := 5}}
  Iteration {{$i}}
{{end}}
```

Output:
```
Iteration 0
Iteration 1
Iteration 2
Iteration 3
Iteration 4
```

**Two Variable Form** (v0.9.0+):

You can also use two variables where both index and value are the same:

```gotemplate
{{range $index, $value := 3}}
  Index: {{$index}}, Value: {{$value}}
{{end}}
```

Output:
```
Index: 0, Value: 0
Index: 1, Value: 1
Index: 2, Value: 2
```

This matches Go's `text/template` behavior for integer ranges.

### Empty Collection

If the collection is empty or nil, the range body doesn't execute:

```gotemplate
{{range .Items}}
  Has items
{{else}}
  No items
{{end}}
```

### Break and Continue

Control loop execution:

```gotemplate
{{range .Items}}
  {{if eq . "stop"}}
    {{break}}
  {{end}}
  {{.}}
{{end}}
```

```gotemplate
{{range .Items}}
  {{if eq . "skip"}}
    {{continue}}
  {{end}}
  {{.}}
{{end}}
```

## With Action

Change the dot context:

```gotemplate
{{with .User}}
  Name: {{.Name}}
  Age: {{.Age}}
  City: {{.Address.City}}
{{end}}
```

Equivalent to:

```gotemplate
{{if .User}}
  Name: {{.User.Name}}
  Age: {{.User.Age}}
  City: {{.User.Address.City}}
{{end}}
```

### With/Else

```gotemplate
{{with .User}}
  {{.Name}}
{{else}}
  No user
{{end}}
```

## Template Actions

### Define

Define a named template:

```gotemplate
{{define "header"}}
<html>
<head><title>{{.Title}}</title></head>
{{end}}
```

Defined templates don't produce output until invoked.

### Template

Invoke a defined template:

```gotemplate
{{template "header" .}}
```

Syntax:
- `{{template "name"}}` - Invoke with current context
- `{{template "name" .}}` - Invoke with explicit context
- `{{template "name" $data}}` - Invoke with variable

### Block

Define and execute inline (Go-compatible):

```gotemplate
{{block "content" .}}Default content{{end}}
```

Equivalent to:

```gotemplate
{{define "content"}}Default content{{end}}
{{template "content" .}}
```

Blocks can be overridden by later definitions.

## Function Calls

Call functions with arguments:

```gotemplate
{{len .Items}}
{{upper .Name}}
{{add 1 2}}
{{printf "%s is %d years old" .Name .Age}}
```

### Built-in Functions

See [Functions Guide](functions.md) for complete list.

Common functions:

| Category | Functions |
|----------|-----------|
| String | `upper`, `lower`, `trim`, `len`, `replace` |
| Math | `add`, `sub`, `mul`, `div`, `mod` |
| Logic | `and`, `or`, `not`, `eq`, `ne`, `lt`, `gt` |
| Format | `printf`, `print`, `println` |
| Type | `typeof`, `kindof` |

### Custom Functions

Register custom functions:

```java
Map<String, Function> functions = new HashMap<>();
functions.put("double", args -> {
    int value = (Integer) args[0];
    return value * 2;
});

Template template = new Template("demo", functions);
template.parse("Double: {{double 5}}");
```

## String Literals

Use quotes for string literals:

```gotemplate
{{"hello"}}
{{'world'}}
```

Escape characters:

```gotemplate
{{"line1\nline2"}}    <!-- Newline -->
{{"tab\there"}}       <!-- Tab -->
{{"quote: \"hi\""}}   <!-- Quote -->
{{"backslash: \\"}}   <!-- Backslash -->
```

## Numeric Literals

Integers:

```gotemplate
{{42}}
{{-10}}
{{0}}
```

Floats:

```gotemplate
{{3.14}}
{{-0.5}}
{{1.0e10}}
```

## Boolean Literals

```gotemplate
{{true}}
{{false}}
```

## Nil

Represent null/nil:

```gotemplate
{{nil}}
```

## Dot Node

Access the current context:

```gotemplate
{{.}}
```

Useful for passing entire context:

```gotemplate
{{template "partial" .}}
```

## Field Access

Access object fields or map keys:

```gotemplate
{{.Name}}
{{.User.Age}}
{{.Address.City}}
```

For JavaBeans, calls getter methods:
- `.Name` → `getName()`
- `.IsActive` → `isActive()` or `getIsActive()`

For Maps, accesses keys:
- `.Name` → `map.get("Name")`

## Method Calls (Limited)

Call zero-argument methods:

```gotemplate
{{.GetFullName}}
{{.ToString}}
```

Note: Methods with arguments are not supported for security reasons.

## Examples

### Complete Template

```gotemplate
{{/* User Profile Template */}}
<html>
<head>
  <title>{{.Title | default "Profile"}}</title>
</head>
<body>
  <h1>{{.User.Name | upper}}</h1>
  
  {{with .User}}
  <div class="profile">
    <p>Name: {{.Name}}</p>
    <p>Age: {{.Age}}</p>
    <p>Email: {{.Email}}</p>
    
    {{if .Addresses}}
    <h2>Addresses</h2>
    <ul>
    {{range $i, $addr := .Addresses}}
      <li>
        {{$addr.Street}}, {{$addr.City}}
        {{if $addr.IsPrimary}}(Primary){{end}}
      </li>
    {{end}}
    </ul>
    {{else}}
    <p>No addresses on file.</p>
    {{end}}
  </div>
  {{end}}
  
  {{template "footer" .}}
</body>
</html>
```

### Data

```java
Map<String, Object> data = new HashMap<>();
data.put("Title", "User Profile");

Map<String, Object> user = new HashMap<>();
user.put("Name", "Alice");
user.put("Age", 30);
user.put("Email", "alice@example.com");

List<Map<String, Object>> addresses = new ArrayList<>();
Map<String, Object> addr1 = new HashMap<>();
addr1.put("Street", "123 Main St");
addr1.put("City", "Beijing");
addr1.put("IsPrimary", true);
addresses.add(addr1);

user.put("Addresses", addresses);
data.put("User", user);
```

## Syntax Errors

Common syntax errors:

### Missing End

```gotemplate
{{if .Active}}
  Active
<!-- Missing {{end}} -->
```

Error: `unexpected EOF, expected end`

### Unclosed Action

```gotemplate
{{.Name
```

Error: `unclosed action`

### Invalid Pipeline

```gotemplate
{{.Name | | upper}}
```

Error: `missing pipeline operand`

## Best Practices

### 1. Use Descriptive Variable Names

❌ **Bad:**
```gotemplate
{{range $x := .Items}}{{$x}}{{end}}
```

✅ **Good:**
```gotemplate
{{range $item := .Items}}{{$item}}{{end}}
```

### 2. Keep Conditionals Simple

Move complex logic to Java code.

❌ **Bad:**
```gotemplate
{{if and (or .A .B) (and (not .C) .D)}}...{{end}}
```

✅ **Good:**
```java
data.put("ShouldShow", calculateCondition());
```
```gotemplate
{{if .ShouldShow}}...{{end}}
```

### 3. Handle Missing Values

```gotemplate
{{if .Name}}{{.Name}}{{else}}Anonymous{{end}}
```

Or use default function:

```gotemplate
{{.Name | default "Anonymous"}}
```

### 4. Use Whitespace Trimming Judiciously

Only trim when necessary for clean output.

### 5. Comment Complex Logic

```gotemplate
{{/* Show premium badge only for active premium users */}}
{{if and .IsActive .IsPremium}}
  <span class="badge">Premium</span>
{{end}}
```

## Quick Reference Card

### Actions

| Action | Syntax |
|--------|--------|
| Variable | `{{.Field}}` |
| If | `{{if COND}}...{{end}}` |
| If/Else | `{{if COND}}...{{else}}...{{end}}` |
| Range | `{{range COLLECTION}}...{{end}}` |
| With | `{{with VALUE}}...{{end}}` |
| Define | `{{define "NAME"}}...{{end}}` |
| Template | `{{template "NAME" DATA}}` |
| Block | `{{block "NAME" DATA}}...{{end}}` |
| Comment | `{{/* COMMENT */}}` |

### Operators

| Type | Operators |
|------|-----------|
| Comparison | `eq`, `ne`, `lt`, `lte`, `gt`, `gte` |
| Logical | `and`, `or`, `not` |
| Arithmetic | `add`, `sub`, `mul`, `div`, `mod` |

### Special Characters

| Character | Meaning |
|-----------|---------|
| `.` | Current context |
| `$` | Variable prefix |
| `\|` | Pipeline operator |
| `:=` | Variable assignment |
| `-` | Whitespace trimming |

---

**Next Steps:**
- 🔧 Learn about [Built-in Functions](functions.md)
- 📊 Understand [Data Models](data-models.md)
- 🎯 Explore [Control Flow](control-flow.md) in detail
