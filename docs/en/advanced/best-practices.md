# Best Practices Guide

This guide provides comprehensive best practices for using gotemplate4j effectively, covering design patterns, code organization, error handling, and maintainability.

---

## Table of Contents

- [Design Principles](#design-principles)
- [Template Organization](#template-organization)
- [Data Modeling](#data-modeling)
- [Function Design](#function-design)
- [Error Handling](#error-handling)
- [Testing Strategies](#testing-strategies)
- [Code Organization](#code-organization)
- [Maintainability](#maintainability)
- [Common Patterns](#common-patterns)

---

## Design Principles

### 1. Keep Templates Simple

Templates should focus on presentation, not business logic.

```gotemplate
<!-- BAD: Complex logic in template -->
{{if and (gt .score 80) (lt .score 90)}}
  Grade: B
{{else if and (ge .score 90) (le .score 100)}}
  Grade: A
{{else if and (ge .score 70) (lt .score 80)}}
  Grade: C
{{end}}
```

```java
// GOOD: Logic in Java code
public class GradeCalculator {
    public static String calculateGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "F";
    }
}

String grade = GradeCalculator.calculateGrade(score);
data.put("grade", grade);
```

```gotemplate
<!-- Template is simple -->
Grade: {{.grade}}
```

**Rationale:** Business logic in Java is easier to test, debug, and maintain.

---

### 2. Single Responsibility

Each template should have one clear purpose.

```gotemplate
<!-- BAD: One template does everything -->
{{define "page"}}
  <html>
    <head>{{template "header" .}}</head>
    <body>
      {{template "navigation" .}}
      {{template "content" .}}
      {{template "sidebar" .}}
      {{template "footer" .}}
    </body>
  </html>
{{end}}

<!-- GOOD: Separate templates for separate concerns -->
<!-- layout.html -->
<html>
  <head>{{block "head" .}}{{end}}</head>
  <body>{{block "body" .}}{{end}}</body>
</html>

<!-- page.html -->
{{define "head"}}<title>{{.title}}</title>{{end}}
{{define "body"}}
  {{template "navigation" .}}
  <main>{{.content}}</main>
{{end}}
```

---

### 3. DRY (Don't Repeat Yourself)

Use template definitions to avoid duplication.

```gotemplate
<!-- BAD: Repeated HTML structure -->
<div class="card">
  <h3>{{.item1.name}}</h3>
  <p>{{.item1.description}}</p>
</div>
<div class="card">
  <h3>{{.item2.name}}</h3>
  <p>{{.item2.description}}</p>
</div>

<!-- GOOD: Use range with template -->
{{define "card"}}
<div class="card">
  <h3>{{.name}}</h3>
  <p>{{.description}}</p>
</div>
{{end}}

{{range .items}}
  {{template "card" .}}
{{end}}
```

---

## Template Organization

### Directory Structure

Organize templates logically:

```
templates/
├── layouts/
│   ├── base.html
│   ├── admin.html
│   └── email.html
├── partials/
│   ├── header.html
│   ├── footer.html
│   ├── navigation.html
│   └── sidebar.html
├── pages/
│   ├── home.html
│   ├── profile.html
│   └── settings.html
└── emails/
    ├── welcome.html
    └── notification.html
```

### Naming Conventions

Use consistent naming:

```java
// GOOD: Clear, descriptive names
Template userDashboard = new Template("user-dashboard");
Template welcomeEmail = new Template("email-welcome");
Template navigationPartial = new Template("partial-navigation");

// BAD: Unclear names
Template t1 = new Template("t1");
Template temp = new Template("temp");
```

### Template Inheritance Hierarchy

Design clear inheritance chains:

```gotemplate
<!-- base.html - Root template -->
<!DOCTYPE html>
<html>
<head>
  {{block "head" .}}
    <title>{{.title}}</title>
  {{end}}
</head>
<body>
  {{block "body" .}}
    {{.content}}
  {{end}}
</body>
</html>

<!-- page.html - Extends base -->
{{define "head"}}
  <title>{{.pageTitle}} - My Site</title>
  <link rel="stylesheet" href="/css/page.css">
{{end}}

{{define "body"}}
  {{template "header" .}}
  <main>{{.pageContent}}</main>
  {{template "footer" .}}
{{end}}
```

---

## Data Modeling

### Use DTOs for Templates

Create specific data transfer objects for templates:

```java
// GOOD: Dedicated DTO
public class UserProfileView {
    private String displayName;
    private String email;
    private String avatarUrl;
    private int followerCount;
    
    // Getters and setters
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    // ...
}

// Usage
UserProfileView view = new UserProfileView();
view.setDisplayName(user.getName());
view.setEmail(user.getEmail());
view.setAvatarUrl(user.getAvatarUrl());
view.setFollowerCount(user.getFollowerCount());

template.execute(writer, view);
```

```java
// BAD: Passing domain entity directly
template.execute(writer, userEntity); // May expose sensitive fields
```

### Flatten Nested Structures

Avoid deeply nested data access in templates:

```java
// BAD: Deep nesting
Map<String, Object> data = new HashMap<>();
data.put("user", Map.of(
    "profile", Map.of(
        "settings", Map.of(
            "theme", "dark"
        )
    )
));
// Template: {{.user.profile.settings.theme}}
```

```java
// GOOD: Flat structure
Map<String, Object> data = new HashMap<>();
data.put("theme", "dark");
// Template: {{.theme}}
```

### Handle Null Values Explicitly

Provide defaults for optional values:

```java
Map<String, Object> data = new HashMap<>();
data.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
data.put("bio", user.getBio() != null ? user.getBio() : "No bio yet");
```

Or use a custom function:

```java
Function defaultIfNull = args -> {
    return args[0] != null ? args[0] : args[1];
};

// Template: {{defaultIfNull .nickname .username}}
```

---

## Function Design

### Small, Focused Functions

Each function should do one thing well:

```java
// GOOD: Single responsibility
Function upperCase = args -> ((String) args[0]).toUpperCase();
Function lowerCase = args -> ((String) args[0]).toLowerCase();
Function titleCase = args -> StringUtils.capitalize((String) args[0]);

// BAD: Multi-purpose function
Function stringTransform = args -> {
    String operation = (String) args[0];
    String value = (String) args[1];
    switch (operation) {
        case "upper": return value.toUpperCase();
        case "lower": return value.toLowerCase();
        case "title": return StringUtils.capitalize(value);
        default: throw new IllegalArgumentException("Unknown operation");
    }
};
```

### Document Functions

Provide clear documentation for custom functions:

```java
/**
 * Formats a number as currency.
 * 
 * Usage: {{currency .amount}} or {{currency .amount "USD"}}
 * 
 * Arguments:
 *   - amount (Number): The amount to format
 *   - currencyCode (String, optional): ISO currency code (default: USD)
 * 
 * Returns: Formatted string (e.g., "$1,234.56")
 */
Function currency = args -> {
    if (args.length < 1 || args.length > 2) {
        throw new IllegalArgumentException("currency requires 1-2 arguments");
    }
    
    Number amount = (Number) args[0];
    String currencyCode = args.length == 2 ? (String) args[1] : "USD";
    
    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
    return formatter.format(amount.doubleValue());
};
```

### Validate Arguments

Always validate function inputs:

```java
Function safeSubstring = args -> {
    // Check argument count
    if (args.length < 2 || args.length > 3) {
        throw new IllegalArgumentException(
            "substring requires 2-3 arguments: string, start, [length]"
        );
    }
    
    // Check types
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("First argument must be a string");
    }
    if (!(args[1] instanceof Number)) {
        throw new IllegalArgumentException("Second argument must be a number");
    }
    
    String str = (String) args[0];
    int start = ((Number) args[1]).intValue();
    
    // Validate ranges
    if (start < 0 || start > str.length()) {
        throw new IllegalArgumentException("Start index out of bounds");
    }
    
    if (args.length == 3) {
        if (!(args[2] instanceof Number)) {
            throw new IllegalArgumentException("Third argument must be a number");
        }
        int length = ((Number) args[2]).intValue();
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be negative");
        }
        return str.substring(start, Math.min(start + length, str.length()));
    }
    
    return str.substring(start);
};
```

### Pure Functions

Prefer pure functions (no side effects):

```java
// GOOD: Pure function
Function add = args -> {
    double sum = 0;
    for (Object arg : args) {
        sum += ((Number) arg).doubleValue();
    }
    return sum;
};

// BAD: Function with side effects
Function logAndReturn = args -> {
    System.out.println("Function called: " + Arrays.toString(args)); // Side effect!
    return args[0];
};
```

---

## Error Handling

### Use Appropriate MissingKeyPolicy

Different policies for different environments:

```java
public class TemplateFactory {
    
    public static Template createDevelopmentTemplate(String name) {
        // Strict policy catches errors early
        return new Template(name)
            .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    }
    
    public static Template createProductionTemplate(String name) {
        // Lenient policy prevents crashes
        return new Template(name)
            .withMissingKeyPolicy(MissingKeyPolicy.INVALID);
    }
}
```

### Graceful Degradation

Handle errors gracefully in production:

```java
public String safeRender(Template template, Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        logger.error("Template rendering failed", e);
        return "<div class='error'>Content temporarily unavailable</div>";
    }
}
```

### Detailed Error Messages in Development

Provide helpful error information during development:

```java
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    if (isDevelopmentMode()) {
        String errorMessage = String.format(
            "Template Error:\n" +
            "Type: %s\n" +
            "Message: %s\n" +
            "Template: %s\n" +
            "Data keys: %s",
            e.getClass().getSimpleName(),
            e.getMessage(),
            template.name(),
            data.keySet()
        );
        throw new RuntimeException(errorMessage, e);
    } else {
        logger.error("Template execution failed", e);
        throw new RuntimeException("Rendering failed", e);
    }
}
```

---

## Testing Strategies

### Unit Test Templates

Test templates with various inputs:

```java
class UserProfileTemplateTest {
    
    private Template template;
    
    @BeforeEach
    void setUp() throws TemplateParseException {
        template = new Template("user-profile");
        template.parse("Hello, {{.name}}! You have {{.messageCount}} messages.");
    }
    
    @Test
    void testBasicRendering() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        data.put("messageCount", 5);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Hello, Alice! You have 5 messages.", writer.toString());
    }
    
    @Test
    void testZeroMessages() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Bob");
        data.put("messageCount", 0);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Hello, Bob! You have 0 messages.", writer.toString());
    }
    
    @Test
    void testMissingField() throws Exception {
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Charlie");
        // messageCount is missing
        
        assertThrows(TemplateExecutionException.class, () -> {
            template.execute(new StringWriter(), data);
        });
    }
}
```

### Test Custom Functions

Thoroughly test custom functions:

```java
class CurrencyFunctionTest {
    
    private Function currency;
    
    @BeforeEach
    void setUp() {
        currency = args -> {
            Number amount = (Number) args[0];
            String code = args.length == 2 ? (String) args[1] : "USD";
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            return formatter.format(amount.doubleValue());
        };
    }
    
    @Test
    void testDefaultCurrency() {
        assertEquals("$1,234.56", currency.invoke(1234.56));
    }
    
    @Test
    void testZeroAmount() {
        assertEquals("$0.00", currency.invoke(0));
    }
    
    @Test
    void testNegativeAmount() {
        assertEquals("-$100.00", currency.invoke(-100));
    }
    
    @Test
    void testInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> currency.invoke());
        assertThrows(IllegalArgumentException.class, () -> currency.invoke("not a number"));
    }
}
```

### Integration Tests

Test complete template workflows:

```java
class EmailTemplateIntegrationTest {
    
    @Test
    void testWelcomeEmailFlow() throws Exception {
        // Setup
        Template emailTemplate = new Template("welcome-email");
        emailTemplate.parseFile(Paths.get("templates/emails/welcome.html"));
        
        User newUser = new User("alice", "alice@example.com");
        
        // Execute
        Map<String, Object> data = new HashMap<>();
        data.put("username", newUser.getUsername());
        data.put("email", newUser.getEmail());
        
        StringWriter writer = new StringWriter();
        emailTemplate.execute(writer, data);
        
        // Verify
        String result = writer.toString();
        assertTrue(result.contains("Welcome, alice!"));
        assertTrue(result.contains("alice@example.com"));
        assertTrue(result.contains("<html>"));
    }
}
```

---

## Code Organization

### Template Factory Pattern

Centralize template creation:

```java
public class TemplateFactory {
    
    private static final Map<String, Function> SHARED_FUNCTIONS = new HashMap<>();
    
    static {
        SHARED_FUNCTIONS.put("formatDate", new FormatDateFunction());
        SHARED_FUNCTIONS.put("currency", new CurrencyFunction());
        SHARED_FUNCTIONS.put("truncate", new TruncateFunction());
    }
    
    public static Template createWebTemplate(String name) {
        return new Template(name, SHARED_FUNCTIONS)
            .withMissingKeyPolicy(getPolicyForEnvironment());
    }
    
    public static Template createEmailTemplate(String name) {
        Map<String, Function> emailFunctions = new HashMap<>(SHARED_FUNCTIONS);
        emailFunctions.put("unsubscribeLink", new UnsubscribeLinkFunction());
        
        return new Template(name, emailFunctions)
            .withMissingKeyPolicy(getPolicyForEnvironment());
    }
    
    private static MissingKeyPolicy getPolicyForEnvironment() {
        return isProduction() ? MissingKeyPolicy.INVALID : MissingKeyPolicy.ERROR;
    }
}
```

### Template Service Layer

Encapsulate template operations:

```java
@Service
public class TemplateRenderingService {
    
    private final TemplateCache templateCache;
    
    public TemplateRenderingService(TemplateCache templateCache) {
        this.templateCache = templateCache;
    }
    
    public String renderUserProfile(User user) {
        Template template = templateCache.get("user-profile");
        
        Map<String, Object> data = new HashMap<>();
        data.put("displayName", user.getDisplayName());
        data.put("joinDate", formatDate(user.getJoinDate()));
        data.put("stats", getUserStats(user));
        
        try {
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        } catch (Exception e) {
            throw new RenderingException("Failed to render user profile", e);
        }
    }
    
    public String renderWelcomeEmail(User user) {
        Template template = templateCache.get("email-welcome");
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("verificationLink", generateVerificationLink(user));
        
        try {
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        } catch (Exception e) {
            throw new RenderingException("Failed to render welcome email", e);
        }
    }
}
```

---

## Maintainability

### Version Control Templates

Treat templates as code:

```bash
# Commit templates with your code
git add templates/
git commit -m "Update user profile template layout"
```

### Document Template Changes

Keep a changelog for template modifications:

```markdown
## Template Changes

### 2024-01-15
- Updated `user-profile.html` to show follower count
- Added `email-welcome.html` for new user onboarding

### 2024-01-10
- Refactored `base.html` to use blocks instead of includes
- Fixed XSS vulnerability in `comment-list.html`
```

### Use Consistent Formatting

Format templates consistently:

```gotemplate
<!-- GOOD: Consistent indentation -->
<div class="user-card">
  <h2>{{.name}}</h2>
  <p>{{.bio}}</p>
  {{if .isActive}}
    <span class="badge">Active</span>
  {{end}}
</div>

<!-- BAD: Inconsistent formatting -->
<div class="user-card">
<h2>{{.name}}</h2>
  <p>{{.bio}}</p>
    {{if .isActive}}
<span class="badge">Active</span>
  {{end}}
    </div>
```

### Comment Complex Templates

Add comments for clarity:

```gotemplate
{{/* Main page layout - extends base.html */}}
{{define "head"}}
  {{/* Page-specific stylesheets */}}
  <link rel="stylesheet" href="/css/page.css">
{{end}}

{{define "body"}}
  {{/* Navigation bar */}}
  {{template "navigation" .}}
  
  {{/* Main content area */}}
  <main>
    {{range .articles}}
      {{/* Article card component */}}
      {{template "article-card" .}}
    {{end}}
  </main>
  
  {{/* Footer section */}}
  {{template "footer" .}}
{{end}}
```

---

## Common Patterns

### Pattern 1: Layout with Blocks

```gotemplate
<!-- base.html -->
<!DOCTYPE html>
<html>
<head>
  {{block "head" .}}
    <title>{{.title}}</title>
  {{end}}
</head>
<body>
  {{block "body" .}}
    {{.content}}
  {{end}}
</body>
</html>

<!-- page.html -->
{{define "head"}}
  <title>{{.pageTitle}} - My Site</title>
  <link rel="stylesheet" href="/css/page.css">
{{end}}

{{define "body"}}
  <h1>{{.heading}}</h1>
  <p>{{.text}}</p>
{{end}}
```

### Pattern 2: Component Composition

```gotemplate
{{define "button"}}
<button class="btn btn-{{.style|default "primary"}}">
  {{.label}}
</button>
{{end}}

{{define "card"}}
<div class="card">
  <div class="card-header">{{.title}}</div>
  <div class="card-body">{{.content}}</div>
  {{if .showFooter}}
    <div class="card-footer">{{.footer}}</div>
  {{end}}
</div>
{{end}}

<!-- Usage -->
{{template "card" (dict "title" "My Card" "content" "Content here" "showFooter" true)}}
```

### Pattern 3: Conditional Sections

```gotemplate
{{if .isAdmin}}
  {{template "admin-controls" .}}
{{else if .isModerator}}
  {{template "moderator-controls" .}}
{{else}}
  {{template "user-controls" .}}
{{end}}
```

### Pattern 4: List with Empty State

```gotemplate
{{if .items}}
  <ul>
    {{range .items}}
      <li>{{.name}}</li>
    {{end}}
  </ul>
{{else}}
  <p class="empty-state">No items found.</p>
{{end}}
```

### Pattern 5: Pagination

```gotemplate
<div class="pagination">
  {{if .hasPrevious}}
    <a href="?page={{.previousPage}}">Previous</a>
  {{end}}
  
  <span>Page {{.currentPage}} of {{.totalPages}}</span>
  
  {{if .hasNext}}
    <a href="?page={{.nextPage}}">Next</a>
  {{end}}
</div>
```

---

## Performance Best Practices

See [Performance Tuning Guide](performance.md) for detailed performance optimization strategies.

Quick tips:
- Parse templates once, execute many times
- Cache parsed templates
- Keep templates simple
- Minimize data passed to templates
- Avoid expensive operations in loops

---

## Security Best Practices

See [Security Considerations](security.md) for comprehensive security guidelines.

Quick tips:
- Never parse user input as templates
- Escape output appropriately
- Validate all function arguments
- Limit resource usage
- Use whitelisted templates only

---

## Summary

Best practices for gotemplate4j:

1. **Keep templates simple** - Presentation logic only
2. **Organize systematically** - Clear directory structure and naming
3. **Model data carefully** - Use DTOs, flatten structures
4. **Design focused functions** - Single responsibility, validated inputs
5. **Handle errors appropriately** - Different policies per environment
6. **Test thoroughly** - Unit tests, integration tests
7. **Organize code well** - Factory pattern, service layer
8. **Maintain diligently** - Version control, documentation, formatting
9. **Follow common patterns** - Layouts, components, conditionals
10. **Prioritize performance and security** - Cache, validate, escape

For more information:
- See [Performance Tuning](performance.md) for optimization strategies
- See [Security Considerations](security.md) for security guidelines
- See [API Reference](../api-reference/) for detailed API documentation
- See [Examples](../examples/) for real-world usage patterns
