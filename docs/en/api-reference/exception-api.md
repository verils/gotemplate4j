# Exception API Reference

gotemplate4j uses a hierarchical exception model to handle errors during template parsing and execution. This reference documents all exception types and their usage.

---

## Table of Contents

- [Exception Hierarchy](#exception-hierarchy)
- [TemplateException](#templateexception)
- [TemplateParseException](#templateparseexception)
- [TemplateExecutionException](#templateexecutionexception)
- [TemplateNotFoundException](#templatenotfoundexception)
- [MissingKeyPolicy](#missingkeypolicy)
- [Error Handling Patterns](#error-handling-patterns)
- [Best Practices](#best-practices)

---

## Exception Hierarchy

```
java.lang.Exception
    └── TemplateException (base class)
        ├── TemplateParseException (parsing errors)
        ├── TemplateExecutionException (runtime errors)
        └── TemplateNotFoundException (missing templates)
```

All template-related exceptions extend `TemplateException`, making it easy to catch all template errors with a single catch block.

---

## TemplateException

**Package:** `io.github.verils.gotemplate`

**Type:** Checked exception

Base class for all template-related exceptions.

### Constructors

```java
public TemplateException(String message)
public TemplateException(String message, Throwable cause)
```

### When It's Used

`TemplateException` itself is rarely thrown directly. Instead, its subclasses are used for specific error scenarios. However, you can catch `TemplateException` to handle all template errors uniformly:

```java
try {
    template.parse("{{.Name}}");
    template.execute(writer, data);
} catch (TemplateException e) {
    // Catches ParseException, ExecutionException, or NotFoundException
    System.err.println("Template error: " + e.getMessage());
}
```

### Common Use Cases

- Catching all template errors in a unified error handler
- Wrapping template operations in try-catch blocks
- Logging template failures

---

## TemplateParseException

**Package:** `io.github.verils.gotemplate`

**Extends:** `TemplateException`

Thrown when a template contains syntax errors or cannot be parsed.

### Constructors

```java
public TemplateParseException(String message)
public TemplateParseException(String message, Throwable cause)
```

### When It's Thrown

This exception is thrown during the **parsing phase** when:

1. **Syntax errors:**
   ```java
   template.parse("{{if .Condition}}"); // Missing {{end}}
   // Throws: TemplateParseException
   ```

2. **Undefined variables:**
   ```java
   template.parse("{{$undefinedVar}}");
   // Throws: TemplateParseException
   ```

3. **Invalid syntax:**
   ```java
   template.parse("{{.Field invalid syntax}}");
   // Throws: TemplateParseException
   ```

4. **Mismatched delimiters:**
   ```java
   template.parse("{{if .Condition}}...{{else}}"); // Missing {{end}}
   // Throws: TemplateParseException
   ```

5. **Invalid function calls:**
   ```java
   template.parse("{{nonexistentFunc .Arg}}");
   // May throw: TemplateParseException
   ```

### Error Message Format

Error messages typically include:
- Description of the error
- Line and column numbers (when available)
- Context around the error

Example:
```
TemplateParseException: unexpected EOF in action at line 3, column 15
```

### Handling Parse Errors

```java
try {
    template.parse(templateText);
} catch (TemplateParseException e) {
    // Log the error with context
    logger.error("Failed to parse template: {}", e.getMessage(), e);
    
    // Provide user-friendly feedback
    showError("Template syntax error: " + e.getMessage());
}
```

### Prevention Tips

- Validate template syntax before deployment
- Use template linting tools
- Test templates with various inputs
- Keep templates simple and well-structured

---

## TemplateExecutionException

**Package:** `io.github.verils.gotemplate`

**Extends:** `TemplateException`

Thrown when template execution fails at runtime.

### Constructors

```java
public TemplateExecutionException(String message)
public TemplateExecutionException(String message, Throwable cause)
```

### When It's Thrown

This exception is thrown during the **execution phase** (after successful parsing) when:

1. **Accessing non-existent fields:**
   ```java
   template.parse("{{.NonExistentField}}");
   
   Map<String, Object> data = new HashMap<>();
   // 'NonExistentField' not in map
   
   template.execute(writer, data);
   // Throws: TemplateExecutionException (if policy is ERROR)
   ```

2. **Function invocation errors:**
   ```java
   template.parse("{{len .Value}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("Value", 123); // len expects String/Collection, not Integer
   
   template.execute(writer, data);
   // Throws: TemplateExecutionException
   ```

3. **Type conversion failures:**
   ```java
   template.parse("{{if gt .Value 10}}...{{end}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("Value", "not a number");
   
   template.execute(writer, data);
   // Throws: TemplateExecutionException
   ```

4. **Null pointer access:**
   ```java
   template.parse("{{.User.Name}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("User", null);
   
   template.execute(writer, data);
   // Throws: TemplateExecutionException
   ```

5. **Custom function errors:**
   ```java
   Function customFunc = args -> {
       throw new RuntimeException("Something went wrong");
   };
   
   template.parse("{{customFunc}}");
   template.execute(writer, data);
   // Throws: TemplateExecutionException (wrapping the RuntimeException)
   ```

6. **Missing key policy violations:**
   ```java
   Template template = new Template("demo")
       .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
   
   template.parse("{{index .Map \"missing\"}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("Map", new HashMap<>());
   
   template.execute(writer, data);
   // Throws: TemplateExecutionException
   ```

### Error Message Format

Error messages describe the runtime failure:

```
TemplateExecutionException: field NonExistentField not found in type java.util.HashMap
TemplateExecutionException: calling len on invalid type java.lang.Integer
TemplateExecutionException: nil pointer evaluating interface {}.Name
```

### Handling Execution Errors

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    // Log detailed error information
    logger.error("Template execution failed: {}", e.getMessage(), e);
    
    // Check if it's a missing field error
    if (e.getMessage().contains("not found")) {
        showWarning("Some data fields are missing");
    }
    
    // Provide fallback content
    writer.write("Error rendering content");
}
```

### Debugging Tips

1. **Enable verbose error messages:**
   ```java
   try {
       template.execute(writer, data);
   } catch (TemplateExecutionException e) {
       e.printStackTrace(); // Full stack trace
       logger.debug("Data: {}", data); // Log input data
   }
   ```

2. **Test with sample data:**
   ```java
   // Create test data that covers all template paths
   Map<String, Object> testData = createTestData();
   template.execute(new StringWriter(), testData);
   ```

3. **Use MissingKeyPolicy.ERROR during development:**
   ```java
   Template template = new Template("demo")
       .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
   // Catches missing fields early
   ```

---

## TemplateNotFoundException

**Package:** `io.github.verils.gotemplate`

**Extends:** `TemplateException`

Thrown when a referenced template cannot be found.

### Constructors

```java
public TemplateNotFoundException(String message)
public TemplateNotFoundException(String message, Throwable cause)
```

### When It's Thrown

This exception occurs when:

1. **Executing an unparsed template:**
   ```java
   Template template = new Template("my-template");
   // Never called template.parse()
   
   template.execute(writer, data);
   // Throws: TemplateNotFoundException
   ```

2. **Including undefined templates:**
   ```java
   template.parse("{{template \"undefined-template\" .}}");
   
   template.execute(writer, data);
   // Throws: TemplateNotFoundException
   ```

3. **Calling executeTemplate with non-existent name:**
   ```java
   template.parse("{{define \"header\"}}Header{{end}}");
   
   template.executeTemplate(writer, "footer", data); // "footer" not defined
   // Throws: TemplateNotFoundException
   ```

### Error Message Format

```
TemplateNotFoundException: Template 'undefined-template' not found.
```

### Handling Not Found Errors

```java
try {
    template.executeTemplate(writer, "optional-section", data);
} catch (TemplateNotFoundException e) {
    // Optional template not found - use default content
    writer.write("<div>Default content</div>");
}
```

### Prevention

Check if template exists before executing:

```java
if (template.hasTemplate("section-name")) {
    template.executeTemplate(writer, "section-name", data);
} else {
    writer.write("<div>Default content</div>");
}
```

Or use lookup:

```java
Template sectionTemplate = template.lookup("section-name");
if (sectionTemplate != null) {
    sectionTemplate.execute(writer, data);
} else {
    // Handle missing template
}
```

---

## MissingKeyPolicy

**Package:** `io.github.verils.gotemplate`

**Type:** Enum

Controls the behavior when accessing missing map keys during execution.

### Enum Values

#### `INVALID` (default)

Do nothing and continue execution. Missing keys produce no output.

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.INVALID);

template.parse("{{.MissingKey}}");
template.execute(writer, new HashMap<>());
// Output: (empty string)
```

**Use case:** Production environments where missing values should be silently ignored.

---

#### `ZERO`

Return the zero value for the map type's element.

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ZERO);

template.parse("{{.MissingKey}}");
template.execute(writer, new HashMap<>());
// Output: depends on expected type (null, 0, false, etc.)
```

**Use case:** When you want predictable default values instead of empty strings.

---

#### `ERROR`

Stop execution immediately with a `TemplateExecutionException`.

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

template.parse("{{.MissingKey}}");

try {
    template.execute(writer, new HashMap<>());
} catch (TemplateExecutionException e) {
    System.out.println(e.getMessage());
    // Output: map has no entry for key "MissingKey"
}
```

**Use case:** Development and testing to catch missing data early.

---

### Configuring MissingKeyPolicy

#### Using `withMissingKeyPolicy()`

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

#### Using `option()` method

```java
// Go-style option string
template.option("missingkey=error");
template.option("missingkey=zero");
template.option("missingkey=default"); // Same as INVALID
```

#### Checking current policy

```java
MissingKeyPolicy policy = template.missingKeyPolicy();
System.out.println("Current policy: " + policy);
```

### Policy Comparison

| Policy | Behavior | Use Case |
|--------|----------|----------|
| `INVALID` | Silent, no output | Production, optional fields |
| `ZERO` | Returns zero value | Predictable defaults |
| `ERROR` | Throws exception | Development, required fields |

### Best Practice

Use different policies for different environments:

```java
Template template = new Template("demo");

if (isDevelopment()) {
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
} else {
    template.withMissingKeyPolicy(MissingKeyPolicy.INVALID);
}
```

---

## Error Handling Patterns

### Pattern 1: Comprehensive Error Handling

```java
public String renderTemplate(String templateText, Map<String, Object> data) {
    Template template = new Template("render");
    
    try {
        // Parse
        template.parse(templateText);
        
        // Execute
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
        
    } catch (TemplateParseException e) {
        // Syntax error in template
        logger.error("Template syntax error: {}", e.getMessage());
        throw new RenderingException("Invalid template syntax", e);
        
    } catch (TemplateNotFoundException e) {
        // Referenced template not found
        logger.error("Template not found: {}", e.getMessage());
        throw new RenderingException("Missing template definition", e);
        
    } catch (TemplateExecutionException e) {
        // Runtime error during execution
        logger.error("Template execution error: {}", e.getMessage());
        logger.debug("Data: {}", data);
        throw new RenderingException("Failed to render template", e);
        
    } catch (IOException e) {
        // IO error writing output
        logger.error("IO error during rendering", e);
        throw new RenderingException("IO error", e);
    }
}
```

---

### Pattern 2: Graceful Degradation

```java
public String renderWithFallback(String templateText, Map<String, Object> data) {
    Template template = new Template("render");
    
    try {
        template.parse(templateText);
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
        
    } catch (TemplateException e) {
        // Any template error - return fallback content
        logger.warn("Template rendering failed, using fallback: {}", e.getMessage());
        return "<div>Content temporarily unavailable</div>";
    }
}
```

---

### Pattern 3: Validation Before Execution

```java
public void validateTemplate(String templateText) throws TemplateParseException {
    Template template = new Template("validation");
    template.parse(templateText);
    // If we get here, template syntax is valid
}

// Usage
try {
    validateTemplate(userProvidedTemplate);
    // Safe to use template
} catch (TemplateParseException e) {
    showError("Invalid template: " + e.getMessage());
}
```

---

### Pattern 4: Detailed Error Reporting

```java
public class TemplateErrorReporter {
    
    public static String formatError(TemplateException e, String templateName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Template Error in '").append(templateName).append("':\n");
        sb.append("Type: ").append(e.getClass().getSimpleName()).append("\n");
        sb.append("Message: ").append(e.getMessage()).append("\n");
        
        if (e.getCause() != null) {
            sb.append("Cause: ").append(e.getCause().getMessage()).append("\n");
        }
        
        return sb.toString();
    }
}

// Usage
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    String errorReport = TemplateErrorReporter.formatError(e, "user-profile");
    logger.error(errorReport, e);
}
```

---

### Pattern 5: Retry with Fallback Data

```java
public void executeWithRetry(Template template, Map<String, Object> data, Writer writer) 
        throws TemplateException, IOException {
    try {
        template.execute(writer, data);
    } catch (TemplateExecutionException e) {
        if (e.getMessage().contains("nil pointer")) {
            // Try with default data
            logger.warn("Execution failed, retrying with defaults");
            Map<String, Object> defaultData = createDefaultData();
            writer.getBuffer().setLength(0); // Clear previous output
            template.execute(writer, defaultData);
        } else {
            throw e; // Re-throw other errors
        }
    }
}
```

---

## Best Practices

### 1. Catch Specific Exceptions

Catch the most specific exception type you need:

```java
// Good: Specific exception handling
try {
    template.parse(text);
} catch (TemplateParseException e) {
    handleSyntaxError(e);
}

// Bad: Catching generic Exception
try {
    template.parse(text);
} catch (Exception e) {
    // Catches everything, including unrelated errors
}
```

---

### 2. Use Appropriate Policies by Environment

```java
Template template = new Template("app");

if (environment.isDevelopment()) {
    // Catch errors early during development
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
} else {
    // Be lenient in production
    template.withMissingKeyPolicy(MissingKeyPolicy.INVALID);
}
```

---

### 3. Log Context Information

When logging errors, include relevant context:

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    logger.error("Template execution failed\n" +
                 "Template: {}\n" +
                 "Data keys: {}\n" +
                 "Error: {}", 
                 template.name(),
                 data.keySet(),
                 e.getMessage(),
                 e);
}
```

---

### 4. Provide User-Friendly Messages

Translate technical errors into user-friendly messages:

```java
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    if (e instanceof TemplateParseException) {
        showUserError("The template has a syntax error. Please check the template code.");
    } else if (e instanceof TemplateNotFoundException) {
        showUserError("A required template is missing. Please contact support.");
    } else {
        showUserError("An error occurred while rendering the content. Please try again.");
    }
}
```

---

### 5. Fail Fast in Development

Configure strict error checking during development:

```java
Template template = new Template("dev")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

// Parse with validation
try {
    template.parse(templateText);
} catch (TemplateParseException e) {
    // Show detailed error to developer
    throw new DevelopmentException("Fix template syntax: " + e.getMessage(), e);
}
```

---

### 6. Handle Errors Gracefully in Production

Be resilient in production:

```java
public String safeRender(Template template, Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        logger.error("Rendering failed, using fallback", e);
        return getDefaultContent();
    }
}
```

---

### 7. Document Expected Exceptions

Document which methods throw which exceptions:

```java
/**
 * Parses and executes a template.
 *
 * @param templateText The template source
 * @param data The data model
 * @return Rendered output
 * @throws TemplateParseException if template syntax is invalid
 * @throws TemplateExecutionException if execution fails (e.g., missing fields)
 * @throws IOException if writing output fails
 */
public String render(String templateText, Map<String, Object> data) 
        throws TemplateParseException, TemplateExecutionException, IOException {
    // Implementation...
}
```

---

### 8. Test Error Scenarios

Write tests for error conditions:

```java
@Test
void testMissingFieldError() {
    Template template = new Template("test")
        .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    template.parse("{{.MissingField}}");
    
    assertThrows(TemplateExecutionException.class, () -> {
        template.execute(new StringWriter(), new HashMap<>());
    });
}

@Test
void testSyntaxError() {
    Template template = new Template("test");
    
    assertThrows(TemplateParseException.class, () -> {
        template.parse("{{if .Condition}}"); // Missing {{end}}
    });
}
```

---

## Summary

gotemplate4j provides a comprehensive exception hierarchy:

- **TemplateException**: Base class for all template errors
- **TemplateParseException**: Syntax errors during parsing
- **TemplateExecutionException**: Runtime errors during execution
- **TemplateNotFoundException**: Missing template references
- **MissingKeyPolicy**: Controls behavior for missing map keys

Best practices:
- Catch specific exception types
- Use appropriate policies by environment
- Log context information
- Provide user-friendly error messages
- Fail fast in development, be resilient in production
- Test error scenarios thoroughly

For more information:
- See [Template API](template-api.md) for configuration options
- See [Error Handling Guide](../user-guide/error-handling.md) for comprehensive strategies
- See [Functions API](function-api.md) for custom function error handling
