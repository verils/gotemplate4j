# Security Considerations

This guide covers security best practices for using gotemplate4j, including template injection prevention, safe function implementation, and data validation.

---

## Table of Contents

- [Security Overview](#security-overview)
- [Template Injection Attacks](#template-injection-attacks)
- [Input Validation](#input-validation)
- [Safe Function Implementation](#safe-function-implementation)
- [Escaping and Sanitization](#escaping-and-sanitization)
- [Access Control](#access-control)
- [Denial of Service Prevention](#denial-of-service-prevention)
- [Security Checklist](#security-checklist)

---

## Security Overview

Template engines can be security-sensitive components in your application. Improper use can lead to:

- **Template Injection**: Attackers inject malicious template code
- **Information Disclosure**: Templates expose sensitive data
- **Denial of Service**: Malicious templates cause resource exhaustion
- **Cross-Site Scripting (XSS)**: Unescaped output in web contexts

gotemplate4j provides features to mitigate these risks, but secure usage depends on proper implementation.

---

## Template Injection Attacks

### What is Template Injection?

Template injection occurs when user-controlled input is used as part of a template without proper validation:

```java
// VULNERABLE: User input used directly in template
String userInput = request.getParameter("template");
Template template = new Template("user-template");
template.parse(userInput); // DANGEROUS!
template.execute(writer, data);
```

An attacker could provide:
```gotemplate
{{.secretPassword}}
{{index .privateData "creditCard"}}
```

### Prevention Strategies

#### 1. Never Parse User Input as Template

```java
// BAD: Allows template injection
String userTemplate = getUserInput();
template.parse(userTemplate);

// GOOD: Use user input as data, not template
Map<String, Object> data = new HashMap<>();
data.put("userContent", getUserInput());
template.execute(writer, data);
```

#### 2. Use Whitelisted Templates

Only allow pre-defined templates:

```java
public class SafeTemplateEngine {
    private final Map<String, Template> allowedTemplates;
    
    public SafeTemplateEngine() {
        allowedTemplates = new HashMap<>();
        
        // Register only approved templates
        Template welcome = new Template("welcome");
        welcome.parse("Hello, {{.name}}!");
        allowedTemplates.put("welcome", welcome);
        
        Template goodbye = new Template("goodbye");
        goodbye.parse("Goodbye, {{.name}}!");
        allowedTemplates.put("goodbye", goodbye);
    }
    
    public String render(String templateName, Map<String, Object> data) throws Exception {
        Template template = allowedTemplates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not allowed: " + templateName);
        }
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    }
}
```

#### 3. Validate Template Names

If you must load templates dynamically, validate names strictly:

```java
public String render(String templateName, Map<String, Object> data) throws Exception {
    // Only allow alphanumeric and underscore
    if (!templateName.matches("^[a-zA-Z0-9_]+$")) {
        throw new IllegalArgumentException("Invalid template name");
    }
    
    // Prevent path traversal
    if (templateName.contains("..") || templateName.contains("/")) {
        throw new IllegalArgumentException("Invalid template name");
    }
    
    Path templatePath = Paths.get("templates").resolve(templateName + ".tmpl");
    Template template = new Template(templateName);
    template.parseFile(templatePath);
    
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    return writer.toString();
}
```

---

## Input Validation

### Validate Data Before Passing to Templates

Always validate and sanitize data before passing it to templates:

```java
public void renderUserProfile(User user) throws Exception {
    // Validate input
    if (user.getName() == null || user.getName().isEmpty()) {
        throw new IllegalArgumentException("Name is required");
    }
    
    if (user.getName().length() > 100) {
        throw new IllegalArgumentException("Name too long");
    }
    
    // Sanitize if needed
    String sanitizedName = HtmlUtils.htmlEscape(user.getName());
    
    Map<String, Object> data = new HashMap<>();
    data.put("name", sanitizedName);
    
    template.execute(writer, data);
}
```

### Limit Data Exposure

Only pass necessary data to templates:

```java
// BAD: Passing entire user object with sensitive fields
template.execute(writer, userObject); // May contain password, SSN, etc.

// GOOD: Extract only needed fields
Map<String, Object> data = new HashMap<>();
data.put("displayName", user.getDisplayName());
data.put("email", user.getEmail());
template.execute(writer, data);
```

### Use MissingKeyPolicy.ERROR in Development

Catch accidental data exposure early:

```java
Template template = new Template("profile")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

// Will throw exception if template references undefined field
template.execute(writer, data);
```

---

## Safe Function Implementation

Custom functions can introduce security vulnerabilities if not implemented carefully.

### Validate Function Arguments

Always validate argument types and values:

```java
// UNSAFE: No validation
Function unsafeReadFile = args -> {
    String filename = (String) args[0]; // No validation!
    return Files.readString(Paths.get(filename)); // Can read any file!
};

// SAFE: Validated and restricted
Function safeReadFile = args -> {
    if (args.length != 1 || !(args[0] instanceof String)) {
        throw new IllegalArgumentException("Expected one string argument");
    }
    
    String filename = (String) args[0];
    
    // Validate filename
    if (filename.contains("..") || filename.startsWith("/")) {
        throw new IllegalArgumentException("Invalid filename");
    }
    
    // Restrict to specific directory
    Path basePath = Paths.get("/safe/templates");
    Path filePath = basePath.resolve(filename).normalize();
    
    if (!filePath.startsWith(basePath)) {
        throw new IllegalArgumentException("Access denied");
    }
    
    return Files.readString(filePath);
};
```

### Avoid Dangerous Operations

Don't implement functions that can execute arbitrary code or access sensitive resources:

```java
// DANGEROUS: Allows arbitrary code execution
Function exec = args -> {
    String command = (String) args[0];
    Process process = Runtime.getRuntime().exec(command);
    // ...
};

// DANGEROUS: Allows reading arbitrary files
Function readFile = args -> {
    String path = (String) args[0];
    return Files.readString(Paths.get(path));
};

// DANGEROUS: Allows network access
Function fetchUrl = args -> {
    String url = (String) args[0];
    return new URL(url).openStream();
};
```

### Limit Resource Usage

Prevent denial of service by limiting resource consumption:

```java
// SAFE: Limited string repetition
Function safeRepeat = args -> {
    String str = (String) args[0];
    int count = ((Number) args[1]).intValue();
    
    // Limit maximum repetitions
    if (count > 1000) {
        throw new IllegalArgumentException("Count too large (max 1000)");
    }
    
    if (str.length() * count > 100000) {
        throw new IllegalArgumentException("Result too large");
    }
    
    return str.repeat(count);
};
```

### Time-Limit Functions

For potentially slow operations, add timeouts:

```java
Function safeComputation = args -> {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Object> future = executor.submit(() -> {
        // Potentially slow operation
        return performCalculation(args);
    });
    
    try {
        return future.get(5, TimeUnit.SECONDS); // 5 second timeout
    } catch (TimeoutException e) {
        future.cancel(true);
        throw new RuntimeException("Operation timed out");
    } finally {
        executor.shutdownNow();
    }
};
```

---

## Escaping and Sanitization

### HTML Escaping

When generating HTML, escape user input to prevent XSS:

```java
// Using built-in html function
template.parse("<div>{{html .userInput}}</div>");

// Or escape in Java code
import org.owasp.encoder.Encode;

String safeInput = Encode.forHtml(userInput);
data.put("userInput", safeInput);
```

### JavaScript Escaping

When embedding data in JavaScript:

```gotemplate
<script>
  var userName = "{{js .userName}}";
</script>
```

Or in Java:
```java
import org.owasp.encoder.Encode;

String safeJs = Encode.forJavaScript(userInput);
data.put("userName", safeJs);
```

### URL Escaping

When constructing URLs:

```gotemplate
<a href="/search?q={{urlquery .searchTerm}}">Search</a>
```

Or in Java:
```java
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
data.put("searchTerm", encoded);
```

### Context-Aware Escaping

Different contexts require different escaping:

```java
public class ContextAwareEscaper {
    
    public static String escapeForHtml(String input) {
        return input == null ? "" : 
            input.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;")
                 .replace("'", "&#x27;");
    }
    
    public static String escapeForHtmlAttribute(String input) {
        return escapeForHtml(input);
    }
    
    public static String escapeForJavaScript(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
    
    public static String escapeForUrl(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Auto-Escaping Strategy

Consider implementing auto-escaping based on context:

```java
public class AutoEscapingTemplate {
    private final Template template;
    
    public AutoEscapingTemplate(Template template) {
        this.template = template;
    }
    
    public void execute(Writer writer, Map<String, Object> data, String context) 
            throws Exception {
        // Auto-escape based on context
        Map<String, Object> escapedData = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                switch (context) {
                    case "html":
                        escapedData.put(entry.getKey(), escapeForHtml(value));
                        break;
                    case "javascript":
                        escapedData.put(entry.getKey(), escapeForJavaScript(value));
                        break;
                    case "url":
                        escapedData.put(entry.getKey(), escapeForUrl(value));
                        break;
                    default:
                        escapedData.put(entry.getKey(), value);
                }
            } else {
                escapedData.put(entry.getKey(), entry.getValue());
            }
        }
        
        template.execute(writer, escapedData);
    }
}
```

---

## Access Control

### Restrict Template Capabilities

Create templates with limited function sets:

```java
// Safe function set - no dangerous operations
Map<String, Function> safeFunctions = new HashMap<>();
safeFunctions.put("upper", args -> ((String) args[0]).toUpperCase());
safeFunctions.put("lower", args -> ((String) args[0]).toLowerCase());
safeFunctions.put("trim", args -> ((String) args[0]).trim());
// Note: No file I/O, no network access, no code execution

Template safeTemplate = new Template("safe", safeFunctions);
```

### Role-Based Template Access

Restrict which templates users can access:

```java
public class RoleBasedTemplateEngine {
    private final Map<String, Set<String>> rolePermissions;
    private final Map<String, Template> templates;
    
    public RoleBasedTemplateEngine() {
        // Define permissions
        rolePermissions = new HashMap<>();
        rolePermissions.put("admin", Set.of("admin-dashboard", "user-list", "settings"));
        rolePermissions.put("user", Set.of("profile", "dashboard"));
        rolePermissions.put("guest", Set.of("welcome", "login"));
        
        // Load templates...
    }
    
    public String render(String userRole, String templateName, Map<String, Object> data) 
            throws Exception {
        Set<String> allowedTemplates = rolePermissions.get(userRole);
        if (allowedTemplates == null || !allowedTemplates.contains(templateName)) {
            throw new SecurityException("Access denied to template: " + templateName);
        }
        
        Template template = templates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    }
}
```

### Sandboxing Templates

Run templates with restricted permissions using Java SecurityManager (advanced):

```java
// Advanced: Use SecurityManager to restrict template execution
// This requires careful configuration and is beyond basic usage
```

---

## Denial of Service Prevention

### Limit Template Complexity

Prevent complex templates from consuming excessive resources:

```java
public class ResourceLimitedTemplate {
    private static final int MAX_TEMPLATE_SIZE = 10000; // 10KB
    private static final int MAX_EXECUTION_TIME_MS = 5000; // 5 seconds
    
    public String render(String templateText, Map<String, Object> data) throws Exception {
        // Check template size
        if (templateText.length() > MAX_TEMPLATE_SIZE) {
            throw new IllegalArgumentException("Template too large");
        }
        
        // Execute with timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            Template template = new Template("limited");
            template.parse(templateText);
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        });
        
        try {
            return future.get(MAX_EXECUTION_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Template execution timed out");
        } finally {
            executor.shutdownNow();
        }
    }
}
```

### Limit Iteration Count

Prevent infinite or excessively long loops:

```java
// In custom range function
Function safeRange = args -> {
    int count = ((Number) args[0]).intValue();
    
    // Limit maximum iterations
    if (count > 10000) {
        throw new IllegalArgumentException("Range too large (max 10000)");
    }
    
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
        result.add(i);
    }
    return result;
};
```

### Monitor Resource Usage

Track and limit resource consumption:

```java
public class MonitoredTemplate {
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private static final long MAX_TOTAL_TIME_MS = 60000; // 1 minute
    
    public void execute(Template template, Writer writer, Object data) throws Exception {
        // Check if we've exceeded time budget
        if (totalExecutionTime.get() > MAX_TOTAL_TIME_MS) {
            throw new RuntimeException("Resource limit exceeded");
        }
        
        long start = System.currentTimeMillis();
        try {
            template.execute(writer, data);
            executionCount.incrementAndGet();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            totalExecutionTime.addAndGet(elapsed);
        }
    }
    
    public void resetBudget() {
        totalExecutionTime.set(0);
    }
}
```

---

## Security Checklist

Use this checklist to ensure secure template usage:

### Template Input
- [ ] Never parse user input as template code
- [ ] Use whitelisted templates only
- [ ] Validate template names strictly
- [ ] Prevent path traversal attacks
- [ ] Limit template size

### Data Handling
- [ ] Validate all data before passing to templates
- [ ] Only pass necessary data (minimize exposure)
- [ ] Sanitize user-provided data
- [ ] Use MissingKeyPolicy.ERROR in development
- [ ] Don't pass sensitive data (passwords, tokens, etc.)

### Function Safety
- [ ] Validate all function arguments
- [ ] Don't implement dangerous operations (file I/O, code execution)
- [ ] Limit resource usage in functions
- [ ] Add timeouts for slow operations
- [ ] Use principle of least privilege

### Output Escaping
- [ ] Escape HTML output appropriately
- [ ] Escape JavaScript context
- [ ] Escape URL parameters
- [ ] Use context-aware escaping
- [ ] Consider auto-escaping strategies

### Access Control
- [ ] Restrict available functions per use case
- [ ] Implement role-based template access
- [ ] Validate user permissions
- [ ] Don't expose admin templates to regular users

### DoS Prevention
- [ ] Limit template complexity
- [ ] Set execution timeouts
- [ ] Limit iteration counts
- [ ] Monitor resource usage
- [ ] Implement rate limiting

### Monitoring and Logging
- [ ] Log template execution errors
- [ ] Monitor for unusual patterns
- [ ] Track resource consumption
- [ ] Alert on security violations
- [ ] Regular security audits

### Testing
- [ ] Test for template injection vulnerabilities
- [ ] Validate escaping in all contexts
- [ ] Test with malicious input
- [ ] Performance test under load
- [ ] Security penetration testing

---

## Common Security Anti-Patterns

### Anti-Pattern 1: Trusting User Input

```java
// BAD: Trusting user input
String userTemplate = request.getParameter("tpl");
template.parse(userTemplate);

// GOOD: Validate and restrict
if (!ALLOWED_TEMPLATES.contains(userTemplate)) {
    throw new SecurityException("Invalid template");
}
```

### Anti-Pattern 2: Exposing Sensitive Data

```java
// BAD: Passing entire object
template.execute(writer, userWithPassword);

// GOOD: Extract safe fields
Map<String, Object> safeData = new HashMap<>();
safeData.put("username", user.getUsername());
safeData.put("email", user.getEmail());
template.execute(writer, safeData);
```

### Anti-Pattern 3: Missing Escaping

```gotemplate
<!-- BAD: Unescaped user input -->
<div>{{.userComment}}</div>

<!-- GOOD: Escaped output -->
<div>{{html .userComment}}</div>
```

### Anti-Pattern 4: Dangerous Custom Functions

```java
// BAD: Allows arbitrary file read
Function readFile = args -> Files.readString(Paths.get((String) args[0]));

// GOOD: Restricted file access
Function readTemplate = args -> {
    String name = (String) args[0];
    if (!name.matches("^[a-zA-Z0-9_-]+$")) {
        throw new IllegalArgumentException("Invalid name");
    }
    return Files.readString(TEMPLATES_DIR.resolve(name + ".tmpl"));
};
```

---

## Security Resources

### OWASP Guidelines

Follow OWASP recommendations for template security:
- [OWASP Template Injection](https://cheatsheetseries.owasp.org/cheatsheets/Injection_Prevention_Cheat_Sheet.html)
- [OWASP XSS Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)

### Recommended Libraries

For enhanced security:
- **OWASP Java Encoder**: `org.owasp.encoder:encoder`
- **JSoup**: HTML sanitization `org.jsoup:jsoup`
- **Apache Commons Text**: String escaping `org.apache.commons:commons-text`

---

## Summary

Security best practices for gotemplate4j:

1. **Never parse user input as templates** - Most critical rule
2. **Validate and sanitize all data** - Before passing to templates
3. **Escape output appropriately** - Based on context (HTML, JS, URL)
4. **Implement safe custom functions** - Validate arguments, limit resources
5. **Control access** - Restrict templates and functions by role
6. **Prevent DoS** - Limit complexity, set timeouts
7. **Monitor and audit** - Track usage, detect anomalies

Remember: Security is a process, not a product. Regularly review and update your security measures.

For more information:
- See [Best Practices](best-practices.md) for general guidelines
- See [Error Handling](../user-guide/error-handling.md) for secure error reporting
- See [Examples](../examples/) for secure usage patterns
