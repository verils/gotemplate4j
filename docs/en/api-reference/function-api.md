# Function API Reference

The `Function` interface allows you to implement custom functions that can be called from within templates. This reference documents the interface and provides implementation examples.

---

## Table of Contents

- [Function Interface](#function-interface)
- [Implementing Custom Functions](#implementing-custom-functions)
- [Registering Functions](#registering-functions)
- [Built-in Functions](#built-in-functions)
- [Best Practices](#best-practices)
- [Common Patterns](#common-patterns)

---

## Function Interface

### Interface Definition

```java
package io.github.verils.gotemplate;

public interface Function {
    /**
     * Invokes the function with the provided arguments.
     *
     * @param args The arguments passed to the function from the template
     * @return The result value
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException if function execution fails
     */
    Object invoke(Object... args);
}
```

### Method: `Object invoke(Object... args)`

Invokes the function with the provided arguments.

**Parameters:**
- `args` - Variable number of arguments passed from the template. Can be zero or more arguments of any type.

**Returns:**
- The result value, which will be converted to a string for output or used as input to the next stage in a pipeline

**Throws:**
- `IllegalArgumentException` if the number or types of arguments are invalid
- `RuntimeException` if function execution fails for any reason

**Usage in Templates:**

Functions can be called in two ways:

1. **Direct invocation:**
   ```gotemplate
   {{myFunction arg1 arg2 arg3}}
   ```

2. **Pipeline invocation:**
   ```gotemplate
   {{arg1 | myFunction}}
   {{arg1 | myFunction arg2}}
   ```

---

## Implementing Custom Functions

### Basic Example: String Uppercase

```java
import io.github.verils.gotemplate.Function;

Function upperCase = new Function() {
    @Override
    public Object invoke(Object... args) {
        // Validate argument count
        if (args.length != 1) {
            throw new IllegalArgumentException("upper requires exactly one argument");
        }
        
        // Validate argument type
        if (!(args[0] instanceof String)) {
            throw new IllegalArgumentException("upper requires a string argument");
        }
        
        // Execute function logic
        return ((String) args[0]).toUpperCase();
    }
};
```

**Template usage:**
```gotemplate
{{.Name | upper}}
<!-- If .Name is "john", outputs: JOHN -->
```

---

### Lambda Expression (Java 8+)

Since `Function` is a functional interface, you can use lambda expressions:

```java
Function lowerCase = args -> {
    if (args.length != 1 || !(args[0] instanceof String)) {
        throw new IllegalArgumentException("lower requires one string argument");
    }
    return ((String) args[0]).toLowerCase();
};
```

---

### Multiple Arguments: String Repeat

```java
Function repeat = args -> {
    if (args.length != 2) {
        throw new IllegalArgumentException("repeat requires two arguments: string and count");
    }
    
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("first argument must be a string");
    }
    
    if (!(args[1] instanceof Number)) {
        throw new IllegalArgumentException("second argument must be a number");
    }
    
    String str = (String) args[0];
    int count = ((Number) args[1]).intValue();
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
        sb.append(str);
    }
    
    return sb.toString();
};
```

**Template usage:**
```gotemplate
{{repeat "-" 10}}
<!-- Outputs: ---------- -->

{{.Separator | repeat .Count}}
```

---

### Complex Logic: Date Formatting

```java
import java.text.SimpleDateFormat;
import java.util.Date;

Function formatDate = args -> {
    if (args.length < 1 || args.length > 2) {
        throw new IllegalArgumentException("formatDate requires 1-2 arguments");
    }
    
    if (!(args[0] instanceof Date)) {
        throw new IllegalArgumentException("first argument must be a Date");
    }
    
    Date date = (Date) args[0];
    String pattern = args.length == 2 ? (String) args[1] : "yyyy-MM-dd";
    
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    return sdf.format(date);
};
```

**Template usage:**
```gotemplate
{{formatDate .CreatedAt}}
<!-- Outputs: 2024-01-15 (default format) -->

{{formatDate .CreatedAt "MMM dd, yyyy"}}
<!-- Outputs: Jan 15, 2024 -->
```

---

## Registering Functions

### Single Function

```java
Map<String, Function> functions = new HashMap<>();
functions.put("upper", upperCase);

Template template = new Template("demo", functions);
template.parse("{{.Name | upper}}");
```

### Multiple Functions

```java
Map<String, Function> functions = new HashMap<>();
functions.put("upper", args -> ((String) args[0]).toUpperCase());
functions.put("lower", args -> ((String) args[0]).toLowerCase());
functions.put("trim", args -> ((String) args[0]).trim());
functions.put("length", args -> ((String) args[0]).length());

Template template = new Template("demo", functions);
template.parse("{{.Text | trim | upper}}");
```

### Function Overriding Built-ins

Custom functions take precedence over built-in functions with the same name:

```java
Map<String, Function> functions = new HashMap<>();
// Override built-in 'len' function
functions.put("len", args -> {
    // Custom length calculation
    return ((String) args[0]).codePointCount(0, ((String) args[0]).length());
});

Template template = new Template("demo", functions);
```

**Note:** Be cautious when overriding built-in functions as it may break existing templates.

---

## Built-in Functions

The following functions are available by default in all templates:

### Output Formatting

| Function | Description | Example |
|----------|-------------|---------|
| `print` | Convert args to string | `{{print .Name .Age}}` |
| `printf` | Format string (Go-style) | `{{printf "%s is %d" .Name .Age}}` |
| `println` | Convert args to string with newline | `{{println .Message}}` |

### Comparison Operators

| Function | Description | Example |
|----------|-------------|---------|
| `eq` | Equal (==) | `{{if eq .Status "active"}}...{{end}}` |
| `ne` | Not equal (!=) | `{{if ne .Count 0}}...{{end}}` |
| `lt` | Less than (<) | `{{if lt .Age 18}}...{{end}}` |
| `le` | Less than or equal (<=) | `{{if le .Score 100}}...{{end}}` |
| `gt` | Greater than (>) | `{{if gt .Price 100}}...{{end}}` |
| `ge` | Greater than or equal (>=) | `{{if ge .Rating 4}}...{{end}}` |

### Logical Operators

| Function | Description | Example |
|----------|-------------|---------|
| `and` | Logical AND | `{{if and .IsActive .IsAdmin}}...{{end}}` |
| `or` | Logical OR | `{{if or .IsAdmin .IsModerator}}...{{end}}` |
| `not` | Logical NOT | `{{if not .IsEmpty}}...{{end}}` |

### Collection Operations

| Function | Description | Example |
|----------|-------------|---------|
| `len` | Length of string/collection | `{{len .Items}}` |
| `index` | Access array/map element | `{{index .Array 0}}`, `{{index .Map "key"}}` |
| `slice` | Slice array/string | `{{slice .Array 1 3}}` |

### Type Inspection

| Function | Description | Example |
|----------|-------------|---------|
| `typeof` | Get Java class name | `{{typeof .Value}}` |
| `kindOf` | Get Go-like kind | `{{kindOf .Value}}` |
| `deepEqual` | Deep equality check | `{{if deepEqual .A .B}}...{{end}}` |

### Escaping Functions

| Function | Description | Example |
|----------|-------------|---------|
| `html` | HTML escape | `{{html .UserInput}}` |
| `js` | JavaScript escape | `{{js .UserInput}}` |
| `urlquery` | URL query escape | `{{urlquery .Param}}` |

### Other Functions

| Function | Description | Example |
|----------|-------------|---------|
| `call` | Call function dynamically | `{{call .Func arg1 arg2}}` |

For detailed documentation on built-in functions, see [Functions Guide](../user-guide/functions.md).

---

## Best Practices

### 1. Validate Arguments

Always validate the number and types of arguments:

```java
Function safeFunction = args -> {
    // Check argument count
    if (args.length != 2) {
        throw new IllegalArgumentException("Expected 2 arguments, got " + args.length);
    }
    
    // Check argument types
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("First argument must be String");
    }
    if (!(args[1] instanceof Number)) {
        throw new IllegalArgumentException("Second argument must be Number");
    }
    
    // Safe to use arguments now
    String str = (String) args[0];
    int num = ((Number) args[1]).intValue();
    
    return str + num;
};
```

### 2. Provide Clear Error Messages

Error messages should help template authors understand what went wrong:

```java
// Bad error message
throw new IllegalArgumentException("Invalid arguments");

// Good error message
throw new IllegalArgumentException(
    "join requires a list and a separator string, got " + args.length + " arguments"
);
```

### 3. Handle Null Values

Decide how your function should handle null arguments:

```java
Function nullSafe = args -> {
    if (args.length == 0 || args[0] == null) {
        return ""; // Return empty string for null
    }
    return args[0].toString().toUpperCase();
};
```

### 4. Keep Functions Simple

Each function should do one thing well. Complex logic should be split into multiple functions or handled in Java code before passing data to the template.

### 5. Document Your Functions

Document expected arguments, return types, and behavior:

```java
/**
 * Joins a list of strings with a separator.
 * 
 * Usage: {{join .Items ", "}}
 * Arguments:
 *   - args[0]: List<String> or String[] - items to join
 *   - args[1]: String - separator
 * Returns: String - joined result
 */
Function join = args -> {
    // Implementation...
};
```

### 6. Consider Performance

For functions called frequently in loops, avoid expensive operations:

```java
// Bad: Creates new SimpleDateFormat every call
Function badDateFormat = args -> {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return sdf.format((Date) args[0]);
};

// Good: Reuse SimpleDateFormat (thread-safe with synchronization)
private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = 
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

Function goodDateFormat = args -> {
    return DATE_FORMAT.get().format((Date) args[0]);
};
```

---

## Common Patterns

### Pattern 1: String Manipulation

```java
// Trim whitespace
Function trim = args -> ((String) args[0]).trim();

// Replace substring
Function replace = args -> {
    String str = (String) args[0];
    String oldStr = (String) args[1];
    String newStr = (String) args[2];
    return str.replace(oldStr, newStr);
};

// Substring
Function substr = args -> {
    String str = (String) args[0];
    int start = ((Number) args[1]).intValue();
    int end = args.length > 2 ? ((Number) args[2]).intValue() : str.length();
    return str.substring(start, end);
};
```

### Pattern 2: Mathematical Operations

```java
// Add numbers
Function add = args -> {
    double sum = 0;
    for (Object arg : args) {
        sum += ((Number) arg).doubleValue();
    }
    return sum;
};

// Multiply
Function multiply = args -> {
    double product = 1;
    for (Object arg : args) {
        product *= ((Number) arg).doubleValue();
    }
    return product;
};

// Round number
Function round = args -> {
    double value = ((Number) args[0]).doubleValue();
    int decimals = args.length > 1 ? ((Number) args[1]).intValue() : 0;
    double factor = Math.pow(10, decimals);
    return Math.round(value * factor) / factor;
};
```

### Pattern 3: Conditional Logic

```java
// Default value if null/empty
Function defaultIfEmpty = args -> {
    Object value = args[0];
    Object defaultValue = args[1];
    
    if (value == null) {
        return defaultValue;
    }
    if (value instanceof String && ((String) value).isEmpty()) {
        return defaultValue;
    }
    return value;
};
```

**Template usage:**
```gotemplate
{{defaultIfEmpty .Name "Anonymous"}}
<!-- Uses "Anonymous" if .Name is null or empty -->
```

### Pattern 4: Data Transformation

```java
// Convert list to comma-separated string
Function joinList = args -> {
    if (!(args[0] instanceof Collection)) {
        throw new IllegalArgumentException("Expected a collection");
    }
    
    String separator = args.length > 1 ? (String) args[1] : ", ";
    Collection<?> collection = (Collection<?>) args[0];
    
    return collection.stream()
        .map(Object::toString)
        .collect(Collectors.joining(separator));
};
```

**Template usage:**
```gotemplate
{{joinList .Tags ", "}}
<!-- If .Tags is ["java", "template", "go"], outputs: java, template, go -->
```

### Pattern 5: Lookup Tables

```java
// Map status codes to labels
Function statusLabel = args -> {
    int code = ((Number) args[0]).intValue();
    
    Map<Integer, String> labels = new HashMap<>();
    labels.put(0, "Pending");
    labels.put(1, "Active");
    labels.put(2, "Suspended");
    labels.put(3, "Deleted");
    
    return labels.getOrDefault(code, "Unknown");
};
```

**Template usage:**
```gotemplate
{{statusLabel .StatusCode}}
<!-- If .StatusCode is 1, outputs: Active -->
```

---

## Advanced Examples

### Example 1: Markdown Renderer

```java
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

Function markdown = args -> {
    if (args.length != 1 || !(args[0] instanceof String)) {
        throw new IllegalArgumentException("markdown requires a string argument");
    }
    
    Parser parser = Parser.builder().build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    
    String markdown = (String) args[0];
    var document = parser.parse(markdown);
    return renderer.render(document);
};
```

**Note:** This example requires an external Markdown library.

---

### Example 2: JSON Formatter

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

Function toJson = args -> {
    if (args.length != 1) {
        throw new IllegalArgumentException("toJson requires one argument");
    }
    
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(args[0]);
};
```

---

### Example 3: Internationalization

```java
import java.util.ResourceBundle;
import java.util.Locale;

class I18nFunction implements Function {
    private final ResourceBundle bundle;
    
    public I18nFunction(Locale locale) {
        this.bundle = ResourceBundle.getBundle("messages", locale);
    }
    
    @Override
    public Object invoke(Object... args) {
        if (args.length == 0 || !(args[0] instanceof String)) {
            throw new IllegalArgumentException("i18n requires a key string");
        }
        
        String key = (String) args[0];
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key; // Return key if translation not found
        }
    }
}

// Usage
Function i18n = new I18nFunction(Locale.FRENCH);
Map<String, Function> functions = new HashMap<>();
functions.put("t", i18n);
Template template = new Template("demo", functions);
```

**Template usage:**
```gotemplate
{{t "greeting"}}
<!-- Outputs localized greeting based on locale -->
```

---

## Testing Custom Functions

Always test your custom functions thoroughly:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomFunctionTest {
    
    @Test
    void testUpperCase() {
        Function upper = args -> ((String) args[0]).toUpperCase();
        
        assertEquals("HELLO", upper.invoke("hello"));
        assertEquals("WORLD", upper.invoke("world"));
    }
    
    @Test
    void testUpperCaseWithInvalidArgs() {
        Function upper = args -> {
            if (args.length != 1 || !(args[0] instanceof String)) {
                throw new IllegalArgumentException("Requires one string argument");
            }
            return ((String) args[0]).toUpperCase();
        };
        
        assertThrows(IllegalArgumentException.class, () -> upper.invoke());
        assertThrows(IllegalArgumentException.class, () -> upper.invoke(123));
    }
    
    @Test
    void testUpperCaseInTemplate() throws Exception {
        Map<String, Function> functions = new HashMap<>();
        functions.put("upper", args -> ((String) args[0]).toUpperCase());
        
        Template template = new Template("test", functions);
        template.parse("{{.Name | upper}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "john");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("JOHN", writer.toString());
    }
}
```

---

## Summary

The `Function` interface provides a powerful way to extend template capabilities:

- Implement the `invoke(Object... args)` method
- Validate arguments and provide clear error messages
- Register functions via the `Template` constructor
- Use lambda expressions for simple functions
- Follow best practices for robustness and performance

For more information:
- See [Template API](template-api.md) for template configuration
- See [Functions Guide](../user-guide/functions.md) for usage examples
- See [Exception API](exception-api.md) for error handling
