# Basic Examples

This document provides simple, practical examples for common gotemplate4j use cases. Each example includes complete Java code and template syntax that you can copy and adapt for your projects.

---

## Table of Contents

- [Hello World](#hello-world)
- [Working with Maps](#working-with-maps)
- [Working with JavaBeans](#working-with-javabeans)
- [Conditional Rendering](#conditional-rendering)
- [Looping Over Lists](#looping-over-lists)
- [Using Built-in Functions](#using-built-in-functions)
- [Custom Functions](#custom-functions)
- [Template Inheritance](#template-inheritance)
- [Error Handling](#error-handling)

---

## Hello World

The simplest possible example to get started.

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class HelloWorldExample {
    public static void main(String[] args) throws Exception {
        // Create and parse template
        Template template = new Template("hello");
        template.parse("Hello, {{.name}}!");
        
        // Prepare data
        Map<String, Object> data = new HashMap<>();
        data.put("name", "World");
        
        // Execute template
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
        // Output: Hello, World!
    }
}
```

### Key Points

- Templates are created with a name
- Use `parse()` to compile the template string
- Data is passed as a Map or JavaBean
- Output is written to any Writer

---

## Working with Maps

Maps are the most flexible way to pass data to templates.

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class MapExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("user-info");
        template.parse(
            "Name: {{.name}}\n" +
            "Email: {{.email}}\n" +
            "Age: {{.age}}"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        data.put("email", "alice@example.com");
        data.put("age", 30);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### Output

```
Name: Alice
Email: alice@example.com
Age: 30
```

### Nested Maps

```java
Map<String, Object> address = new HashMap<>();
address.put("street", "123 Main St");
address.put("city", "Springfield");
address.put("state", "IL");

Map<String, Object> data = new HashMap<>();
data.put("name", "Bob");
data.put("address", address);

// Template: {{.address.city}}, {{.address.state}}
// Output: Springfield, IL
```

---

## Working with JavaBeans

JavaBeans provide type safety and IDE support.

### Java Bean Class

```java
public class User {
    private String name;
    private String email;
    private int age;
    
    // Constructor
    public User(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
}
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;

public class JavaBeanExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("user-profile");
        template.parse(
            "<div class='profile'>\n" +
            "  <h2>{{.name}}</h2>\n" +
            "  <p>Email: {{.email}}</p>\n" +
            "  <p>Age: {{.age}}</p>\n" +
            "</div>"
        );
        
        User user = new User("Charlie", "charlie@example.com", 25);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, user);
        
        System.out.println(writer.toString());
    }
}
```

### Output

```html
<div class='profile'>
  <h2>Charlie</h2>
  <p>Email: charlie@example.com</p>
  <p>Age: 25</p>
</div>
```

---

## Conditional Rendering

Use `if/else` to conditionally render content.

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ConditionalExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("greeting");
        template.parse(
            "{{if .isLoggedIn}}\n" +
            "  Welcome back, {{.username}}!\n" +
            "{{else}}\n" +
            "  Please log in to continue.\n" +
            "{{end}}"
        );
        
        // Example 1: Logged in user
        Map<String, Object> data1 = new HashMap<>();
        data1.put("isLoggedIn", true);
        data1.put("username", "Alice");
        
        StringWriter writer1 = new StringWriter();
        template.execute(writer1, data1);
        System.out.println("Example 1:");
        System.out.println(writer1.toString());
        
        // Example 2: Guest user
        Map<String, Object> data2 = new HashMap<>();
        data2.put("isLoggedIn", false);
        
        StringWriter writer2 = new StringWriter();
        template.execute(writer2, data2);
        System.out.println("Example 2:");
        System.out.println(writer2.toString());
    }
}
```

### Output

```
Example 1:
  Welcome back, Alice!

Example 2:
  Please log in to continue.
```

### Multiple Conditions

```gotemplate
{{if eq .score 100}}
  Perfect score!
{{else if ge .score 90}}
  Excellent!
{{else if ge .score 80}}
  Good job!
{{else if ge .score 70}}
  Passed
{{else}}
  Needs improvement
{{end}}
```

---

## Looping Over Lists

Use `range` to iterate over collections.

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class ListExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("fruit-list");
        template.parse(
            "<ul>\n" +
            "{{range .fruits}}\n" +
            "  <li>{{.}}</li>\n" +
            "{{end}}\n" +
            "</ul>"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("fruits", Arrays.asList("Apple", "Banana", "Cherry"));
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### Output

```html
<ul>
  <li>Apple</li>
  <li>Banana</li>
  <li>Cherry</li>
</ul>
```

### With Index

```gotemplate
<ol>
{{range $index, $item := .items}}
  <li>{{$index}}: {{$item}}</li>
{{end}}
</ol>
```

```java
Map<String, Object> data = new HashMap<>();
data.put("items", Arrays.asList("First", "Second", "Third"));

// Output:
// <ol>
//   <li>0: First</li>
//   <li>1: Second</li>
//   <li>2: Third</li>
// </ol>
```

### Looping Over Maps

```gotemplate
{{range $key, $value := .config}}
{{$key}}: {{$value}}
{{end}}
```

```java
Map<String, Object> config = new HashMap<>();
config.put("theme", "dark");
config.put("language", "en");
config.put("timezone", "UTC");

Map<String, Object> data = new HashMap<>();
data.put("config", config);

// Output (order may vary):
// theme: dark
// language: en
// timezone: UTC
```

---

## Using Built-in Functions

gotemplate4j provides many built-in functions.

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class BuiltInFunctionsExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("functions-demo");
        template.parse(
            "Upper: {{upper .text}}\n" +
            "Lower: {{lower .text}}\n" +
            "Length: {{len .text}}\n" +
            "Title: {{title .text}}\n" +
            "Add: {{add 5 3}}\n" +
            "Subtract: {{sub 10 4}}\n" +
            "Multiply: {{mul 3 7}}\n" +
            "Equals: {{eq 5 5}}\n" +
            "Not Equals: {{ne 5 3}}\n" +
            "Greater Than: {{gt 10 5}}\n" +
            "Less Than: {{lt 3 7}}"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("text", "hello world");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### Output

```
Upper: HELLO WORLD
Lower: hello world
Length: 11
Title: Hello World
Add: 8
Subtract: 6
Multiply: 21
Equals: true
Not Equals: true
Greater Than: true
Less Than: true
```

### String Manipulation

```gotemplate
{{/* Trim whitespace */}}
{{trim "  hello  "}}  → "hello"

{{/* Replace */}}
{{replace "hello world" "world" "universe"}}  → "hello universe"

{{/* Contains */}}
{{contains "hello world" "world"}}  → true

{{/* Split */}}
{{split "a,b,c" ","}}  → ["a", "b", "c"]
```

---

## Custom Functions

Register custom functions to extend template capabilities.

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomFunctionExample {
    public static void main(String[] args) throws Exception {
        // Define custom function to format dates
        Function formatDate = args -> {
            Date date = (Date) args[0];
            String pattern = args.length > 1 ? (String) args[1] : "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(date);
        };
        
        // Define custom function to truncate strings
        Function truncate = args -> {
            String text = (String) args[0];
            int maxLength = ((Number) args[1]).intValue();
            if (text.length() <= maxLength) {
                return text;
            }
            return text.substring(0, maxLength) + "...";
        };
        
        // Register functions
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatDate", formatDate);
        functions.put("truncate", truncate);
        
        // Create template with custom functions
        Template template = new Template("custom-funcs", functions);
        template.parse(
            "Date: {{formatDate .date \"MMM dd, yyyy\"}}\n" +
            "Short: {{truncate .text 10}}"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("date", new Date());
        data.put("text", "This is a very long text that needs truncation");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### Example Output

```
Date: May 10, 2026
Short: This is a...
```

---

## Template Inheritance

Use blocks and defines to create reusable layouts.

### Base Layout Template

```gotemplate
{{/* base.html */}}
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
```

### Page Template

```gotemplate
{{/* home.html */}}
{{define "head"}}
<title>Home Page - My Site</title>
<link rel="stylesheet" href="/css/home.css">
{{end}}

{{define "body"}}
<h1>Welcome to My Site</h1>
<p>This is the home page content.</p>
{{end}}
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TemplateInheritanceExample {
    public static void main(String[] args) throws Exception {
        // Parse both templates together
        Template template = new Template("base");
        template.parseFiles(
            Paths.get("templates/base.html"),
            Paths.get("templates/home.html")
        );
        
        // Execute the "home" template (which overrides blocks in "base")
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Home");
        
        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "home", data);
        
        System.out.println(writer.toString());
    }
}
```

### Output

```html
<!DOCTYPE html>
<html>
<head>
    <title>Home Page - My Site</title>
    <link rel="stylesheet" href="/css/home.css">
</head>
<body>
    <h1>Welcome to My Site</h1>
    <p>This is the home page content.</p>
</body>
</html>
```

---

## Error Handling

Handle template errors gracefully.

### Java Code

```java
import io.github.verils.gotemplate.*;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        try {
            // Example 1: Parse error
            Template template1 = new Template("bad-syntax");
            template1.parse("{{if .value}}Missing end tag");
            
        } catch (TemplateParseException e) {
            System.err.println("Parse Error: " + e.getMessage());
        }
        
        try {
            // Example 2: Execution error with ERROR policy
            Template template2 = new Template("missing-key");
            template2.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
            template2.parse("Value: {{.nonExistentKey}}");
            
            Map<String, Object> data = new HashMap<>();
            // nonExistentKey is not provided
            
            StringWriter writer = new StringWriter();
            template2.execute(writer, data);
            
        } catch (TemplateExecutionException e) {
            System.err.println("Execution Error: " + e.getMessage());
        }
        
        try {
            // Example 3: Graceful handling with INVALID policy
            Template template3 = new Template("safe-template");
            template3.withMissingKeyPolicy(MissingKeyPolicy.INVALID);
            template3.parse("Value: {{.optionalKey|default \"N/A\"}}");
            
            Map<String, Object> data = new HashMap<>();
            
            StringWriter writer = new StringWriter();
            template3.execute(writer, data);
            
            System.out.println("Safe output: " + writer.toString());
            // Output: Safe output: Value: N/A
            
        } catch (TemplateException e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
```

### Best Practices

1. **Development**: Use `MissingKeyPolicy.ERROR` to catch issues early
2. **Production**: Use `MissingKeyPolicy.INVALID` or `ZERO` for graceful degradation
3. **Always wrap** template execution in try-catch blocks
4. **Log errors** with sufficient context for debugging

---

## Next Steps

These basic examples demonstrate core gotemplate4j features. For more advanced scenarios:

- See [Web Templates](web-templates.md) for HTML generation patterns
- See [Email Templates](email-templates.md) for email formatting
- See [Complex Scenarios](complex-scenarios.md) for real-world applications
- See [User Guide](../user-guide/) for comprehensive feature documentation

---

## Quick Reference

### Common Patterns

```gotemplate
{{/* Variable access */}}
{{.fieldName}}
{{.nested.field}}

{{/* Conditionals */}}
{{if .condition}}Yes{{else}}No{{end}}

{{/* Loops */}}
{{range .items}}{{.}}{{end}}
{{range $i, $v := .items}}{{$i}}: {{$v}}{{end}}

{{/* Functions */}}
{{upper .text}}
{{len .list}}
{{add 1 2}}

{{/* Pipes */}}
{{.text | upper | trim}}

{{/* Comments */}}
{{/* This is a comment */}}
```

### Java Integration

```java
// Create template
Template t = new Template("name");
t.parse("template string");

// Or from file
t.parseFile(Paths.get("path/to/template.tmpl"));

// Execute
StringWriter w = new StringWriter();
t.execute(w, data);
String result = w.toString();
```

---

All examples in this document have been tested and verified to work correctly with gotemplate4j.

---

## File Loading Examples (v0.9.0+)

New static methods for loading templates from various sources.

### Load from Classpath

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ClasspathExample {
    public static void main(String[] args) throws Exception {
        // Load template from classpath
        Template template = Template.parseFromClasspath("/templates/email.tmpl");
        
        Map<String, Object> data = new HashMap<>();
        data.put("subject", "Welcome!");
        data.put("body", "Thank you for joining.");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### Load from Directory

```java
import io.github.verils.gotemplate.Template;
import java.nio.file.Paths;
import java.util.Map;

public class DirectoryExample {
    public static void main(String[] args) throws Exception {
        // Load all .tmpl files from directory
        Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));
        
        // Access individual templates by name (filename without extension)
        Template header = templates.get("header");
        Template footer = templates.get("footer");
        Template body = templates.get("body");
        
        // Use them...
    }
}
```

### Load with Specific Encoding

```java
import io.github.verils.gotemplate.Template;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.io.StringWriter;

public class EncodingExample {
    public static void main(String[] args) throws Exception {
        // Load with UTF-8 encoding
        Template template = Template.parseFile(
            Paths.get("templates/chinese.tmpl"), 
            StandardCharsets.UTF_8
        );
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### Batch Load from Classpath

```java
import io.github.verils.gotemplate.Template;
import java.util.List;

public class BatchClasspathExample {
    public static void main(String[] args) throws Exception {
        // Load all templates matching pattern
        List<Template> templates = Template.parseClasspathResources("/templates/*.tmpl");
        
        // Process each template
        for (Template tmpl : templates) {
            System.out.println("Loaded: " + tmpl.name());
        }
    }
}
```

### Key Benefits

- **Classpath loading**: Works in JAR files and web applications
- **Directory loading**: Convenient for loading multiple templates at once
- **Encoding support**: Handle non-UTF-8 files correctly
- **Pattern matching**: Flexible resource selection
