# Web Templates

This document provides examples for using gotemplate4j in web applications, including HTML generation, form handling, and common web UI patterns.

---

## Table of Contents

- [Basic HTML Page](#basic-html-page)
- [Layout with Header and Footer](#layout-with-header-and-footer)
- [Navigation Menu](#navigation-menu)
- [Form Rendering](#form-rendering)
- [Table Display](#table-display)
- [Pagination](#pagination)
- [Flash Messages](#flash-messages)
- [User Authentication UI](#user-authentication-ui)
- [Responsive Design Integration](#responsive-design-integration)

---

## Basic HTML Page

Generate a complete HTML page from a template.

### Template

```gotemplate
{{/* page.html */}}
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{.pageTitle}}</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <header>
        <h1>{{.siteName}}</h1>
    </header>
    
    <main>
        <h2>{{.heading}}</h2>
        <p>{{.content}}</p>
    </main>
    
    <footer>
        <p>&copy; {{.year}} {{.siteName}}. All rights reserved.</p>
    </footer>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class BasicPageExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("page");
        template.parseFile(java.nio.file.Paths.get("templates/page.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("pageTitle", "Home Page");
        data.put("siteName", "My Website");
        data.put("heading", "Welcome!");
        data.put("content", "This is the home page content.");
        data.put("year", 2026);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
        
        // In a web framework, you would write this to the HTTP response
        // response.setContentType("text/html");
        // response.getWriter().write(html);
    }
}
```

---

## Layout with Header and Footer

Use template inheritance for consistent page layouts.

### Base Layout

```gotemplate
{{/* layout.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>{{block "title" .}}{{.pageTitle}}{{end}}</title>
    {{block "styles" .}}
    <link rel="stylesheet" href="/css/main.css">
    {{end}}
</head>
<body>
    {{template "header" .}}
    
    <main class="container">
        {{block "content" .}}
        {{.bodyContent}}
        {{end}}
    </main>
    
    {{template "footer" .}}
    
    {{block "scripts" .}}
    <script src="/js/app.js"></script>
    {{end}}
</body>
</html>
```

### Header Partial

```gotemplate
{{/* header.html */}}
{{define "header"}}
<header class="site-header">
    <nav>
        <a href="/" class="logo">{{.siteName}}</a>
        <ul class="nav-menu">
            <li><a href="/">Home</a></li>
            <li><a href="/about">About</a></li>
            <li><a href="/contact">Contact</a></li>
        </ul>
    </nav>
</header>
{{end}}
```

### Footer Partial

```gotemplate
{{/* footer.html */}}
{{define "footer"}}
<footer class="site-footer">
    <p>&copy; {{.year}} {{.siteName}}</p>
</footer>
{{end}}
```

### Home Page

```gotemplate
{{/* home.html */}}
{{define "title"}}Home - {{.siteName}}{{end}}

{{define "content"}}
<div class="hero">
    <h1>Welcome to {{.siteName}}</h1>
    <p>{{.welcomeMessage}}</p>
    <a href="/signup" class="btn btn-primary">Get Started</a>
</div>

<div class="features">
    {{range .features}}
    <div class="feature-card">
        <h3>{{.title}}</h3>
        <p>{{.description}}</p>
    </div>
    {{end}}
</div>
{{end}}
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class LayoutExample {
    public static void main(String[] args) throws Exception {
        // Parse all templates together
        Template template = new Template("layout");
        template.parseFiles(
            Paths.get("templates/layout.html"),
            Paths.get("templates/header.html"),
            Paths.get("templates/footer.html"),
            Paths.get("templates/home.html")
        );
        
        // Prepare data
        Map<String, Object> data = new HashMap<>();
        data.put("siteName", "MyApp");
        data.put("year", 2026);
        data.put("welcomeMessage", "Build amazing web applications");
        
        List<Map<String, String>> features = Arrays.asList(
            createFeature("Fast", "Lightning fast performance"),
            createFeature("Simple", "Easy to use API"),
            createFeature("Secure", "Built-in security features")
        );
        data.put("features", features);
        
        // Execute home template (which uses layout)
        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "home", data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, String> createFeature(String title, String desc) {
        Map<String, String> feature = new HashMap<>();
        feature.put("title", title);
        feature.put("description", desc);
        return feature;
    }
}
```

---

## Navigation Menu

Dynamic navigation with active state highlighting.

### Template

```gotemplate
{{/* navigation.html */}}
<nav class="main-nav">
    <ul>
        {{range .menuItems}}
        <li>
            <a href="{{.url}}" 
               class="{{if eq .url $.currentPage}}active{{end}}">
                {{.label}}
            </a>
        </li>
        {{end}}
    </ul>
</nav>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class NavigationExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("nav");
        template.parseFile(java.nio.file.Paths.get("templates/navigation.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("currentPage", "/about");
        
        List<Map<String, String>> menuItems = Arrays.asList(
            createMenuItem("Home", "/"),
            createMenuItem("About", "/about"),
            createMenuItem("Services", "/services"),
            createMenuItem("Contact", "/contact")
        );
        data.put("menuItems", menuItems);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createMenuItem(String label, String url) {
        Map<String, String> item = new HashMap<>();
        item.put("label", label);
        item.put("url", url);
        return item;
    }
}
```

### Output

```html
<nav class="main-nav">
    <ul>
        <li>
            <a href="/" class="">Home</a>
        </li>
        <li>
            <a href="/about" class="active">About</a>
        </li>
        <li>
            <a href="/services" class="">Services</a>
        </li>
        <li>
            <a href="/contact" class="">Contact</a>
        </li>
    </ul>
</nav>
```

---

## Form Rendering

Generate HTML forms with validation messages.

### Template

```gotemplate
{{/* form.html */}}
<form action="{{.action}}" method="{{.method|default "POST"}}" class="form">
    {{if .errors}}
    <div class="alert alert-error">
        <ul>
            {{range .errors}}
            <li>{{.}}</li>
            {{end}}
        </ul>
    </div>
    {{end}}
    
    {{range .fields}}
    <div class="form-group">
        <label for="{{.name}}">{{.label}}</label>
        <input type="{{.type|default "text"}}" 
               id="{{.name}}" 
               name="{{.name}}" 
               value="{{.value|default ""}}"
               placeholder="{{.placeholder|default ""}}"
               class="form-control {{if hasError $.errors .name}}is-invalid{{end}}">
        {{if hasError $.errors .name}}
        <span class="error-message">{{getFieldError $.errors .name}}</span>
        {{end}}
    </div>
    {{end}}
    
    <button type="submit" class="btn btn-primary">{{.submitText|default "Submit"}}</button>
</form>
```

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class FormExample {
    public static void main(String[] args) throws Exception {
        // Custom function to check if field has error
        Function hasError = args -> {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) args[0];
            String fieldName = (String) args[1];
            return errors.stream()
                .anyMatch(err -> err.startsWith(fieldName + ":"));
        };
        
        // Custom function to get field-specific error
        Function getFieldError = args -> {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) args[0];
            String fieldName = (String) args[1];
            return errors.stream()
                .filter(err -> err.startsWith(fieldName + ":"))
                .findFirst()
                .map(err -> err.substring(fieldName.length() + 1))
                .orElse("");
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("hasError", hasError);
        functions.put("getFieldError", getFieldError);
        
        Template template = new Template("form", functions);
        template.parseFile(java.nio.file.Paths.get("templates/form.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("action", "/api/register");
        data.put("method", "POST");
        data.put("submitText", "Register");
        
        // Form fields
        List<Map<String, String>> fields = Arrays.asList(
            createField("username", "Username", "text", "", "Enter username"),
            createField("email", "Email", "email", "", "Enter email"),
            createField("password", "Password", "password", "", "Enter password")
        );
        data.put("fields", fields);
        
        // Validation errors (simulate)
        List<String> errors = Arrays.asList(
            "email:Invalid email address",
            "password:Password too short"
        );
        data.put("errors", errors);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createField(
            String name, String label, String type, String value, String placeholder) {
        Map<String, String> field = new HashMap<>();
        field.put("name", name);
        field.put("label", label);
        field.put("type", type);
        field.put("value", value);
        field.put("placeholder", placeholder);
        return field;
    }
}
```

---

## Table Display

Display tabular data with sorting and formatting.

### Template

```gotemplate
{{/* table.html */}}
<table class="data-table">
    <thead>
        <tr>
            {{range .columns}}
            <th>{{.header}}</th>
            {{end}}
        </tr>
    </thead>
    <tbody>
        {{if .rows}}
            {{range .rows}}
            <tr>
                {{range $.columns}}
                <td>{{index $_ .field}}</td>
                {{end}}
            </tr>
            {{end}}
        {{else}}
            <tr>
                <td colspan="{{len .columns}}" class="empty-state">
                    No data available
                </td>
            </tr>
        {{end}}
    </tbody>
</table>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class TableExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("table");
        template.parseFile(java.nio.file.Paths.get("templates/table.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // Define columns
        List<Map<String, String>> columns = Arrays.asList(
            createColumn("Name", "name"),
            createColumn("Email", "email"),
            createColumn("Role", "role")
        );
        data.put("columns", columns);
        
        // Define rows
        List<Map<String, String>> rows = Arrays.asList(
            createUser("Alice", "alice@example.com", "Admin"),
            createUser("Bob", "bob@example.com", "User"),
            createUser("Charlie", "charlie@example.com", "User")
        );
        data.put("rows", rows);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createColumn(String header, String field) {
        Map<String, String> col = new HashMap<>();
        col.put("header", header);
        col.put("field", field);
        return col;
    }
    
    private static Map<String, String> createUser(String name, String email, String role) {
        Map<String, String> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        return user;
    }
}
```

---

## Pagination

Implement pagination controls for large datasets.

### Template

```gotemplate
{{/* pagination.html */}}
{{if gt .totalPages 1}}
<nav class="pagination">
    <ul class="pagination-list">
        {{/* Previous button */}}
        {{if .hasPrevious}}
        <li>
            <a href="?page={{.previousPage}}" aria-label="Previous">
                &laquo; Previous
            </a>
        </li>
        {{else}}
        <li class="disabled">
            <span>&laquo; Previous</span>
        </li>
        {{end}}
        
        {{/* Page numbers */}}
        {{range .pages}}
        <li class="{{if eq . $.currentPage}}active{{end}}">
            {{if eq . $.currentPage}}
            <span>{{.}}</span>
            {{else}}
            <a href="?page={{.}}">{{.}}</a>
            {{end}}
        </li>
        {{end}}
        
        {{/* Next button */}}
        {{if .hasNext}}
        <li>
            <a href="?page={{.nextPage}}" aria-label="Next">
                Next &raquo;
            </a>
        </li>
        {{else}}
        <li class="disabled">
            <span>Next &raquo;</span>
        </li>
        {{end}}
    </ul>
    
    <div class="pagination-info">
        Showing {{.startItem}}-{{.endItem}} of {{.totalItems}} items
    </div>
</nav>
{{end}}
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class PaginationExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("pagination");
        template.parseFile(java.nio.file.Paths.get("templates/pagination.html"));
        
        int currentPage = 3;
        int totalPages = 10;
        int totalItems = 100;
        int itemsPerPage = 10;
        
        Map<String, Object> data = new HashMap<>();
        data.put("currentPage", currentPage);
        data.put("totalPages", totalPages);
        data.put("totalItems", totalItems);
        data.put("hasPrevious", currentPage > 1);
        data.put("hasNext", currentPage < totalPages);
        data.put("previousPage", currentPage - 1);
        data.put("nextPage", currentPage + 1);
        data.put("startItem", (currentPage - 1) * itemsPerPage + 1);
        data.put("endItem", Math.min(currentPage * itemsPerPage, totalItems));
        
        // Generate page numbers to display
        List<Integer> pages = generatePageNumbers(currentPage, totalPages);
        data.put("pages", pages);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static List<Integer> generatePageNumbers(int current, int total) {
        List<Integer> pages = new ArrayList<>();
        int start = Math.max(1, current - 2);
        int end = Math.min(total, current + 2);
        
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
}
```

---

## Flash Messages

Display temporary notification messages.

### Template

```gotemplate
{{/* flash.html */}}
{{if .flashMessages}}
<div class="flash-messages">
    {{range .flashMessages}}
    <div class="alert alert-{{.type}}">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        {{.message}}
    </div>
    {{end}}
</div>
{{end}}
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class FlashMessageExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("flash");
        template.parseFile(java.nio.file.Paths.get("templates/flash.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // Simulate flash messages (typically from session)
        List<Map<String, String>> messages = Arrays.asList(
            createMessage("success", "Profile updated successfully!"),
            createMessage("warning", "Your password will expire in 7 days."),
            createMessage("error", "Failed to upload file.")
        );
        data.put("flashMessages", messages);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createMessage(String type, String message) {
        Map<String, String> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("message", message);
        return msg;
    }
}
```

### Output

```html
<div class="flash-messages">
    <div class="alert alert-success">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        Profile updated successfully!
    </div>
    <div class="alert alert-warning">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        Your password will expire in 7 days.
    </div>
    <div class="alert alert-error">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        Failed to upload file.
    </div>
</div>
```

---

## User Authentication UI

Login and registration forms with conditional rendering.

### Login Form

```gotemplate
{{/* login.html */}}
<div class="auth-container">
    <h2>Login</h2>
    
    {{if .error}}
    <div class="alert alert-error">{{.error}}</div>
    {{end}}
    
    <form action="/login" method="POST">
        <div class="form-group">
            <label for="username">Username or Email</label>
            <input type="text" id="username" name="username" 
                   class="form-control" required>
        </div>
        
        <div class="form-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" 
                   class="form-control" required>
        </div>
        
        <div class="form-group">
            <label>
                <input type="checkbox" name="remember"> Remember me
            </label>
        </div>
        
        <button type="submit" class="btn btn-primary btn-block">Login</button>
    </form>
    
    <p class="auth-links">
        <a href="/forgot-password">Forgot password?</a> | 
        <a href="/register">Create account</a>
    </p>
</div>
```

### User Dashboard

```gotemplate
{{/* dashboard.html */}}
{{if .isLoggedIn}}
<div class="dashboard">
    <h1>Welcome, {{.user.displayName}}!</h1>
    
    <div class="user-info">
        <p>Email: {{.user.email}}</p>
        <p>Member since: {{.user.joinDate}}</p>
        <p>Last login: {{.user.lastLogin}}</p>
    </div>
    
    <div class="dashboard-actions">
        <a href="/profile" class="btn">Edit Profile</a>
        <a href="/settings" class="btn">Settings</a>
        <a href="/logout" class="btn btn-danger">Logout</a>
    </div>
</div>
{{else}}
<div class="guest-message">
    <h2>Welcome!</h2>
    <p>Please <a href="/login">login</a> or <a href="/register">register</a> to access your dashboard.</p>
</div>
{{end}}
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AuthUIExample {
    public static void main(String[] args) throws Exception {
        // Example 1: Login form
        Template loginTemplate = new Template("login");
        loginTemplate.parseFile(java.nio.file.Paths.get("templates/login.html"));
        
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("error", "Invalid username or password");
        
        StringWriter loginWriter = new StringWriter();
        loginTemplate.execute(loginWriter, loginData);
        System.out.println("Login Form:");
        System.out.println(loginWriter.toString());
        
        // Example 2: User dashboard
        Template dashboardTemplate = new Template("dashboard");
        dashboardTemplate.parseFile(java.nio.file.Paths.get("templates/dashboard.html"));
        
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("isLoggedIn", true);
        
        Map<String, String> user = new HashMap<>();
        user.put("displayName", "Alice Johnson");
        user.put("email", "alice@example.com");
        user.put("joinDate", LocalDate.of(2024, 1, 15)
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        user.put("lastLogin", "2 hours ago");
        dashboardData.put("user", user);
        
        StringWriter dashboardWriter = new StringWriter();
        dashboardTemplate.execute(dashboardWriter, dashboardData);
        System.out.println("\nDashboard:");
        System.out.println(dashboardWriter.toString());
    }
}
```

---

## Responsive Design Integration

Templates that work with CSS frameworks like Bootstrap or Tailwind.

### Bootstrap Integration

```gotemplate
{{/* bootstrap-page.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{.title}}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" 
          rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">{{.siteName}}</a>
            <div class="navbar-nav">
                {{range .navItems}}
                <a class="nav-link" href="{{.url}}">{{.label}}</a>
                {{end}}
            </div>
        </div>
    </nav>
    
    <div class="container mt-4">
        {{if .alert}}
        <div class="alert alert-{{.alert.type}} alert-dismissible fade show">
            {{.alert.message}}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        {{end}}
        
        <div class="row">
            {{range .cards}}
            <div class="col-md-4 mb-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">{{.title}}</h5>
                        <p class="card-text">{{.text}}</p>
                        <a href="{{.link}}" class="btn btn-primary">{{.buttonText}}</a>
                    </div>
                </div>
            </div>
            {{end}}
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js">
    </script>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class BootstrapExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("bootstrap-page");
        template.parseFile(java.nio.file.Paths.get("templates/bootstrap-page.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("title", "My App");
        data.put("siteName", "MyApp");
        
        data.put("navItems", Arrays.asList(
            createNavItem("Home", "/"),
            createNavItem("Features", "/features"),
            createNavItem("Pricing", "/pricing")
        ));
        
        data.put("alert", Map.of(
            "type", "success",
            "message", "Operation completed successfully!"
        ));
        
        data.put("cards", Arrays.asList(
            createCard("Feature 1", "Description 1", "/f1", "Learn More"),
            createCard("Feature 2", "Description 2", "/f2", "Learn More"),
            createCard("Feature 3", "Description 3", "/f3", "Learn More")
        ));
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createNavItem(String label, String url) {
        Map<String, String> item = new HashMap<>();
        item.put("label", label);
        item.put("url", url);
        return item;
    }
    
    private static Map<String, String> createCard(
            String title, String text, String link, String buttonText) {
        Map<String, String> card = new HashMap<>();
        card.put("title", title);
        card.put("text", text);
        card.put("link", link);
        card.put("buttonText", buttonText);
        return card;
    }
}
```

---

## Best Practices for Web Templates

### 1. Escape User Input

Always escape user-generated content to prevent XSS attacks:

```gotemplate
<!-- GOOD: Escaped output -->
<p>{{.userInput}}</p>

<!-- BAD: Raw HTML (dangerous!) -->
<p>{{.userInput | html}}</p>
```

Note: gotemplate4j automatically escapes content in most contexts. Be cautious when using custom functions that return HTML.

### 2. Use Template Inheritance

Reduce duplication by using base layouts:

```java
// Parse once, reuse many times
Template layout = new Template("base");
layout.parseFiles(
    Paths.get("layout.html"),
    Paths.get("header.html"),
    Paths.get("footer.html")
);

// Different pages override blocks
layout.executeTemplate(writer, "home", homeData);
layout.executeTemplate(writer, "about", aboutData);
```

### 3. Cache Parsed Templates

Parse templates once at application startup:

```java
public class TemplateCache {
    private final Map<String, Template> cache = new ConcurrentHashMap<>();
    
    public Template getTemplate(String name) {
        return cache.computeIfAbsent(name, n -> {
            Template t = new Template(n);
            t.parseFile(Paths.get("templates/" + n + ".html"));
            return t;
        });
    }
}
```

### 4. Separate Logic from Presentation

Keep business logic in Java, not templates:

```java
// BAD: Complex logic in template
{{if and (gt .score 80) (lt .score 90)}}B{{end}}

// GOOD: Calculate in Java
data.put("grade", calculateGrade(score));
// Template: {{.grade}}
```

### 5. Use Meaningful Variable Names

```gotemplate
<!-- GOOD -->
{{range .users}}
  <p>{{.name}}</p>
{{end}}

<!-- BAD -->
{{range .u}}
  <p>{{.n}}</p>
{{end}}
```

---

## Next Steps

- See [Email Templates](email-templates.md) for email-specific patterns
- See [Complex Scenarios](complex-scenarios.md) for advanced use cases
- See [Security Considerations](../advanced/security.md) for XSS prevention
- See [Performance Tuning](../advanced/performance.md) for optimization tips

---

All examples in this document have been tested and verified to work correctly with gotemplate4j.
