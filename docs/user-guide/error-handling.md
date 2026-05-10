# Error Handling Guide

This guide explains error types, handling strategies, and best practices for robust template execution.

## Overview

gotemplate4j has a well-defined exception hierarchy:
- **TemplateException**: Base class for all template errors
- **TemplateParseException**: Errors during template parsing
- **TemplateExecutionException**: Errors during template execution
- **TemplateNotFoundException**: Template file not found

Understanding these exceptions helps you handle errors gracefully.

## Exception Hierarchy

```
TemplateException (base)
├── TemplateParseException
│   ├── Lexer errors
│   └── Parser errors
├── TemplateExecutionException
│   ├── Missing field errors
│   ├── Function errors
│   └── Runtime errors
└── TemplateNotFoundException
    └── File not found errors
```

## Parse Errors

Parse errors occur when template syntax is invalid.

### Common Parse Errors

#### Unclosed Action

```java
try {
    template.parse("Hello {{.Name");
} catch (TemplateParseException e) {
    System.err.println("Parse error: " + e.getMessage());
    // Output: unclosed action
}
```

#### Missing End

```java
try {
    template.parse("{{if .Active}}Active");
} catch (TemplateParseException e) {
    System.err.println("Parse error: " + e.getMessage());
    // Output: unexpected EOF, expected end
}
```

#### Invalid Pipeline

```java
try {
    template.parse("{{.Name | | upper}}");
} catch (TemplateParseException e) {
    System.err.println("Parse error: " + e.getMessage());
    // Output: missing pipeline operand
}
```

#### Undefined Function

```java
try {
    template.parse("{{undefinedFunc .Name}}");
} catch (TemplateParseException e) {
    System.err.println("Parse error: " + e.getMessage());
}
```

### Handling Parse Errors

Always wrap parse calls in try-catch:

```java
Template template = new Template("demo");
try {
    template.parse(templateText);
} catch (TemplateParseException e) {
    System.err.println("Failed to parse template: " + e.getMessage());
    // Log error, show user-friendly message, etc.
    return;
}
```

## Execution Errors

Execution errors occur when template runs with problematic data or encounters runtime issues.

### Common Execution Errors

#### Missing Field

```java
Map<String, Object> data = new HashMap<>();
// Name key is missing

template.parse("Hello, {{.Name}}!");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("Execution error: " + e.getMessage());
}
```

Behavior depends on MissingKeyPolicy (see below).

#### Null Pointer

```java
Map<String, Object> data = new HashMap<>();
data.put("User", null);

template.parse("{{.User.Name}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("Execution error: " + e.getMessage());
}
```

#### Type Mismatch

```java
Map<String, Object> data = new HashMap<>();
data.put("Count", "not a number");

template.parse("{{add .Count 1}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("Execution error: " + e.getMessage());
}
```

#### Function Error

```java
Map<String, Function> functions = new HashMap<>();
functions.put("divide", args -> {
    int a = (Integer) args[0];
    int b = (Integer) args[1];
    return a / b;  // May throw ArithmeticException
});

template.parse("{{divide 10 0}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
} catch (TemplateExecutionException e) {
    System.err.println("Execution error: " + e.getMessage());
}
```

### Handling Execution Errors

Wrap execute calls in try-catch:

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("Template execution failed: " + e.getMessage());
    // Handle error appropriately
} catch (IOException e) {
    System.err.println("IO error: " + e.getMessage());
}
```

## MissingKeyPolicy

Control how missing keys are handled during execution.

### Policy Options

#### DEFAULT

Missing keys render as empty string.

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);

template.parse("Name: '{{.Name}}'");

Map<String, Object> data = new HashMap<>();
// Name is missing

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());
// Output: Name: ''
```

#### ZERO

Missing keys render as zero value for the type.

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ZERO);

template.parse("Count: {{.Count}}, Name: '{{.Name}}'");

StringWriter writer = new StringWriter();
template.execute(writer, new HashMap<>());
System.out.println(writer.toString());
// Output: Count: 0, Name: ''
```

#### ERROR

Missing keys throw TemplateExecutionException.

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

template.parse("Name: {{.Name}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
} catch (TemplateExecutionException e) {
    System.err.println("Error: " + e.getMessage());
    // Output: can't evaluate field Name
}
```

### Choosing a Policy

- **DEFAULT**: Best for production, lenient handling
- **ZERO**: Useful for numeric-heavy templates
- **ERROR**: Best for development, catches issues early

## Template Not Found

Occurs when trying to load a non-existent template file.

```java
try {
    template.parseFile(Paths.get("nonexistent.tmpl"));
} catch (TemplateNotFoundException e) {
    System.err.println("Template not found: " + e.getMessage());
} catch (IOException e) {
    System.err.println("IO error: " + e.getMessage());
}
```

## Best Practices

### 1. Always Handle Exceptions

Never let template exceptions propagate unchecked.

❌ **Bad:**
```java
public void render() throws Exception {
    template.execute(writer, data);
}
```

✅ **Good:**
```java
public String render() {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        log.error("Template error", e);
        return "<error>Failed to render template</error>";
    }
}
```

### 2. Validate Templates Before Use

Test templates during development:

```java
public void validateTemplate(String templateText) {
    try {
        Template template = new Template("validation");
        template.parse(templateText);
        System.out.println("Template is valid");
    } catch (TemplateParseException e) {
        System.err.println("Invalid template: " + e.getMessage());
    }
}
```

### 3. Use ERROR Policy in Development

Catch missing keys early:

```java
Template template = new Template("demo");
if (isDevelopment()) {
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
} else {
    template.withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);
}
```

### 4. Check Data Before Execution

Validate data matches template expectations:

```java
public void executeWithData(Map<String, Object> data) {
    // Validate required fields
    if (!data.containsKey("Name")) {
        throw new IllegalArgumentException("Missing required field: Name");
    }
    
    try {
        template.execute(writer, data);
    } catch (TemplateExecutionException e) {
        log.error("Execution failed", e);
    }
}
```

### 5. Provide Fallback Content

Handle errors gracefully with fallback:

```java
public String renderWithFallback(Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        log.warn("Template failed, using fallback", e);
        return getDefaultContent();
    }
}
```

### 6. Log Detailed Error Information

Include context in error logs:

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    log.error("Template execution failed\n" +
              "Template: {}\n" +
              "Data keys: {}\n" +
              "Error: {}", 
              template.name(),
              data.keySet(),
              e.getMessage(),
              e);
}
```

### 7. Test Edge Cases

Test templates with:
- Empty data
- Null values
- Missing fields
- Wrong types
- Empty collections

```java
@Test
public void testWithEmptyData() {
    Map<String, Object> emptyData = new HashMap<>();
    
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, emptyData);
        // Verify output is acceptable
    } catch (TemplateExecutionException e) {
        fail("Should handle empty data: " + e.getMessage());
    }
}
```

## Error Recovery Strategies

### Strategy 1: Default Values

Provide defaults for missing data:

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", Optional.ofNullable(userData.getName())
                         .orElse("Anonymous"));
data.put("Age", Optional.ofNullable(userData.getAge())
                        .orElse(0));
```

### Strategy 2: Conditional Rendering

Check before accessing:

```gotemplate
{{if .User}}
  {{if .User.Name}}
    {{.User.Name}}
  {{else}}
    Anonymous
  {{end}}
{{else}}
  No user
{{end}}
```

### Strategy 3: Safe Functions

Create functions that handle errors:

```java
functions.put("safeGet", args -> {
    Map<String, Object> map = (Map<String, Object>) args[0];
    String key = (String) args[1];
    Object defaultValue = args[2];
    
    return map.getOrDefault(key, defaultValue);
});
```
```gotemplate
{{safeGet .Data "key" "default value"}}
```

### Strategy 4: Try-Catch in Java

Handle at application level:

```java
public class TemplateRenderer {
    public String render(String templateName, Map<String, Object> data) {
        try {
            Template template = getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        } catch (TemplateNotFoundException e) {
            log.error("Template not found: {}", templateName);
            return "<error>Template not available</error>";
        } catch (TemplateParseException e) {
            log.error("Parse error in {}: {}", templateName, e.getMessage());
            return "<error>Template syntax error</error>";
        } catch (TemplateExecutionException e) {
            log.error("Execution error in {}: {}", templateName, e.getMessage());
            return "<error>Failed to render template</error>";
        }
    }
}
```

## Debugging Tips

### 1. Enable Verbose Error Messages

Some exceptions include position information:

```
TemplateParseException: unexpected token "}" at line 3, column 10
```

Use this to locate errors quickly.

### 2. Test Small Sections

Isolate problematic template sections:

```java
// Test just the problematic part
Template test = new Template("test");
test.parse("{{.ProblematicField}}");
test.execute(writer, data);
```

### 3. Print Data Structure

Verify data matches expectations:

```java
System.out.println("Data keys: " + data.keySet());
System.out.println("User: " + data.get("User"));
```

### 4. Use Simpler Templates

Start simple and add complexity gradually:

```java
// Start with this
template.parse("{{.Name}}");

// Then add more
template.parse("{{if .Name}}{{.Name}}{{else}}No name{{end}}");
```

### 5. Check Template Syntax

Use online validators or linters if available.

## Common Error Scenarios

### Scenario 1: Production Error Handling

```java
@RestController
public class PageController {
    
    @Autowired
    private TemplateService templateService;
    
    @GetMapping("/page/{name}")
    public ResponseEntity<String> renderPage(@PathVariable String name) {
        try {
            Map<String, Object> data = buildPageData(name);
            String html = templateService.render(name, data);
            return ResponseEntity.ok(html);
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (TemplateException e) {
            log.error("Page render failed: {}", name, e);
            return ResponseEntity.status(500)
                                 .body("<h1>Internal Server Error</h1>");
        }
    }
}
```

### Scenario 2: Email Template with Fallback

```java
public String generateEmail(Map<String, Object> data) {
    try {
        Template template = loadEmailTemplate();
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (Exception e) {
        log.error("Email generation failed, using plain text", e);
        return generatePlainTextEmail(data);
    }
}
```

### Scenario 3: Batch Processing

```java
public void processBatch(List<Map<String, Object>> items) {
    for (Map<String, Object> item : items) {
        try {
            String result = templateService.render("item", item);
            saveResult(result);
        } catch (TemplateException e) {
            log.error("Failed to process item: {}", item.get("id"), e);
            // Continue with next item
        }
    }
}
```

## Performance Considerations

### 1. Parse Once, Execute Many Times

Don't parse templates repeatedly:

❌ **Bad:**
```java
for (Map<String, Object> data : dataList) {
    Template template = new Template("demo");
    template.parse(templateText);  // Parsed every iteration
    template.execute(writer, data);
}
```

✅ **Good:**
```java
Template template = new Template("demo");
template.parse(templateText);  // Parse once

for (Map<String, Object> data : dataList) {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);  // Execute multiple times
}
```

### 2. Cache Compiled Templates

```java
private Map<String, Template> templateCache = new ConcurrentHashMap<>();

public Template getTemplate(String name) {
    return templateCache.computeIfAbsent(name, n -> {
        Template template = new Template(n);
        template.parseFile(Paths.get("templates/" + n + ".tmpl"));
        return template;
    });
}
```

### 3. Avoid Expensive Error Handling

Don't use exceptions for flow control:

❌ **Bad:**
```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    // Expected case, not really an error
    useDefaultOutput();
}
```

✅ **Good:**
```java
if (isValidData(data)) {
    template.execute(writer, data);
} else {
    useDefaultOutput();
}
```

## Testing Error Handling

### Unit Tests

```java
@Test
public void testMissingKeyWithErrorPolicy() {
    Template template = new Template("test")
        .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    template.parse("{{.Missing}}");
    
    assertThrows(TemplateExecutionException.class, () -> {
        template.execute(new StringWriter(), new HashMap<>());
    });
}

@Test
public void testMissingKeyWithDefaultPolicy() {
    Template template = new Template("test")
        .withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);
    template.parse("{{.Missing}}");
    
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
    
    assertEquals("", writer.toString());
}

@Test
public void testParseError() {
    Template template = new Template("test");
    
    assertThrows(TemplateParseException.class, () -> {
        template.parse("{{invalid syntax");
    });
}
```

### Integration Tests

```java
@Test
public void testFullRenderWithErrorHandling() {
    Map<String, Object> data = new HashMap<>();
    data.put("Name", "Alice");
    // Missing other fields
    
    String result = renderer.render("profile", data);
    
    // Should not throw, should have fallback content
    assertNotNull(result);
    assertTrue(result.contains("Anonymous") || result.contains("error"));
}
```

---

**Next Steps:**
- 🔍 Review [Go Compatibility](../advanced/compatibility.md) for known differences
- 📚 Learn about [Performance Tuning](../advanced/performance.md) for optimization
- 💡 Explore [Best Practices](../advanced/best-practices.md) for comprehensive guidelines
