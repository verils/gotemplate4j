# Quick Start Guide

Get up and running with gotemplate4j in 5 minutes! This guide walks you through creating your first template.

## Prerequisites

- Java 11 or later installed
- A Java project set up (Maven, Gradle, or plain Java)
- gotemplate4j added to your project (see [Installation](installation.md))

## Step 1: Create a Simple Template

Let's start with the classic "Hello, World!" example.

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        // Step 1: Create a template
        Template template = new Template("greeting");
        template.parse("Hello, {{.Name}}!");
        
        // Step 2: Prepare data
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        
        // Step 3: Execute template
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        // Step 4: Get result
        String result = writer.toString();
        System.out.println(result);  // Output: Hello, World!
    }
}
```

**Key Concepts:**
- `{{.Name}}` - Accesses the "Name" field from the data
- The dot (`.`) represents the current data context
- Template execution substitutes variables with actual values

## Step 2: Use Conditionals

Add logic to your templates with `if`/`else`:

```java
Template template = new Template("conditional");
template.parse(
    "{{if .IsActive}}" +
    "User is active" +
    "{{else}}" +
    "User is inactive" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("IsActive", true);

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());  // Output: User is active
```

## Step 3: Iterate Over Collections

Use `range` to loop over arrays, lists, or maps:

```java
Template template = new Template("loop");
template.parse(
    "Items:\n" +
    "{{range .Items}}" +
    "- {{.}}\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Items", new String[]{"Apple", "Banana", "Cherry"});

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());
// Output:
// Items:
// - Apple
// - Banana
// - Cherry
```

## Step 4: Use Built-in Functions

gotemplate4j includes many useful functions:

```java
Template template = new Template("functions");
template.parse(
    "Upper: {{upper .Text}}\n" +
    "Length: {{len .Items}}\n" +
    "Formatted: {{printf \"%.2f\" .Price}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Text", "hello");
data.put("Items", new String[]{"a", "b", "c"});
data.put("Price", 19.99);

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());
// Output:
// Upper: HELLO
// Length: 3
// Formatted: 19.99
```

## Step 5: Work with Java Objects

Templates work seamlessly with JavaBeans:

```java
// Define a Java class
public class User {
    private String name;
    private int age;
    
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
}

// Use it in a template
Template template = new Template("user");
template.parse("Name: {{.Name}}, Age: {{.Age}}");

User user = new User("Alice", 30);

StringWriter writer = new StringWriter();
template.execute(writer, user);
System.out.println(writer.toString());  // Output: Name: Alice, Age: 30
```

## Complete Example

Here's a more realistic example combining multiple features:

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class CompleteExample {
    public static void main(String[] args) throws Exception {
        // Template with multiple features
        String templateText = 
            "Report for {{.Title}}\n" +
            "====================\n" +
            "\n" +
            "{{if .ShowSummary}}" +
            "Summary: {{.Summary}}\n" +
            "\n" +
            "{{end}}" +
            "Items ({{len .Items}} total):\n" +
            "{{range $index, $item := .Items}}" +
            "{{add $index 1}}. {{upper $item}}\n" +
            "{{end}}" +
            "\n" +
            "Generated at: {{.Timestamp}}";
        
        Template template = new Template("report");
        template.parse(templateText);
        
        // Prepare complex data
        Map<String, Object> data = new HashMap<>();
        data.put("Title", "Monthly Sales");
        data.put("ShowSummary", true);
        data.put("Summary", "Sales increased by 15%");
        data.put("Items", Arrays.asList("widgets", "gadgets", "doohickeys"));
        data.put("Timestamp", new Date().toString());
        
        // Execute
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        System.out.println(writer.toString());
    }
}
```

**Output:**
```
Report for Monthly Sales
====================

Summary: Sales increased by 15%

Items (3 total):
1. WIDGETS
2. GADGETS
3. DOOHICKEYS

Generated at: Sun May 10 16:00:00 CST 2026
```

## What's Next?

Now that you've mastered the basics, explore these topics:

- 📖 [Basic Concepts](basic-concepts.md) - Understand core concepts in detail
- 📚 [Template Syntax](../user-guide/template-syntax.md) - Complete syntax reference
- 🔧 [Functions](../user-guide/functions.md) - All built-in functions
- 💡 [Examples](../examples/basic-examples.md) - More practical examples

## Common Patterns

### Loading Templates from Files

```java
import java.nio.file.Paths;

Template template = new Template("mytemplate");
template.parseFile(Paths.get("templates/mytemplate.tmpl"));
```

### Using Custom Functions

```java
import io.github.verils.gotemplate.Function;

Map<String, Function> functions = new HashMap<>();
functions.put("double", args -> {
    int value = (Integer) args[0];
    return value * 2;
});

Template template = new Template("custom", functions);
template.parse("Double of 5 is {{double 5}}");
```

### Error Handling

```java
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    System.err.println("Template error: " + e.getMessage());
} catch (IOException e) {
    System.err.println("IO error: " + e.getMessage());
}
```

## Tips for Success

1. **Start Simple**: Begin with basic variable substitution, then add complexity
2. **Test Templates**: Verify templates with different data inputs
3. **Use Meaningful Names**: Name templates and variables clearly
4. **Handle Errors**: Always handle `TemplateException` and `IOException`
5. **Reuse Templates**: Use `define` and `template` for reusable components

## Need Help?

- 📚 Browse the full [Documentation](../index.md)
- ❓ Check the [FAQ](../faq.md)
- 🐛 Report issues on [GitHub](https://github.com/verils/gotemplate4j/issues)
