# Working with Java Data Models

This guide explains how to work with different Java data types in gotemplate4j templates.

## Overview

gotemplate4j can work with various Java data structures:
- Maps
- JavaBeans (objects with getters)
- Lists and Arrays
- Enums
- Optional values
- Primitive types

Understanding how to access data from these structures is essential for effective template writing.

## The Data Context

When you execute a template, you pass data as the context:

```java
Template template = new Template("demo");
template.parse("Hello, {{.Name}}!");

// Pass data as Map
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");

StringWriter writer = new StringWriter();
template.execute(writer, data);
```

The dot (`.`) represents this data context in the template.

## Maps

Maps are the most flexible data structure for templates.

### Basic Map Access

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");
data.put("Age", 30);
data.put("City", "Beijing");

template.parse("{{.Name}} is {{.Age}} years old, lives in {{.City}}");
```

**Output:**
```
Alice is 30 years old, lives in Beijing
```

### Nested Maps

```java
Map<String, Object> user = new HashMap<>();
user.put("Name", "Bob");

Map<String, Object> address = new HashMap<>();
address.put("Street", "123 Main St");
address.put("City", "Shanghai");

user.put("Address", address);

template.parse("{{.Name}} lives at {{.Address.Street}}, {{.Address.City}}");
```

**Output:**
```
Bob lives at 123 Main St, Shanghai
```

### Map Iteration

Iterate over map entries:

```java
Map<String, Object> config = new HashMap<>();
config.put("host", "localhost");
config.put("port", 8080);
config.put("debug", true);

template.parse(
    "{{range $key, $value := .Config}}" +
    "{{$key}}: {{$value}}\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Config", config);
```

**Output** (order may vary):
```
host: localhost
port: 8080
debug: true
```

### Map Key Types

Maps with String keys work best. Other key types use `toString()`:

```java
Map<Integer, String> numberNames = new HashMap<>();
numberNames.put(1, "one");
numberNames.put(2, "two");

// Access using string representation
template.parse("{{.NumberNames.1}}");  // Outputs: one
```

## JavaBeans

JavaBeans are objects with getter methods. Templates access properties via getters.

### Basic JavaBean

```java
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

// Use in template
User user = new User("Alice", 30);
template.parse("Name: {{.Name}}, Age: {{.Age}}");

StringWriter writer = new StringWriter();
template.execute(writer, user);
```

**Output:**
```
Name: Alice, Age: 30
```

### Getter Method Resolution

Templates look for getters in this order:
1. `getFieldName()` - Standard getter
2. `isFieldName()` - Boolean getter
3. Public field `fieldName` (if no getter exists)

Example:

```java
public class Account {
    private boolean active;
    private String status;
    
    public boolean isActive() { return active; }
    public String getStatus() { return status; }
}

template.parse("Active: {{.Active}}, Status: {{.Status}}");
```

### Nested JavaBeans

```java
public class Address {
    private String street;
    private String city;
    
    public String getStreet() { return street; }
    public String getCity() { return city; }
}

public class User {
    private String name;
    private Address address;
    
    public String getName() { return name; }
    public Address getAddress() { return address; }
}

User user = new User("Bob", new Address("123 Main St", "Beijing"));
template.parse("{{.Name}} lives in {{.Address.City}}");
```

### Mixed Maps and Beans

You can mix Maps and JavaBeans:

```java
Map<String, Object> data = new HashMap<>();
data.put("User", user);  // JavaBean
data.put("Settings", settingsMap);  // Map

template.parse("User: {{.User.Name}}, Debug: {{.Settings.Debug}}");
```

### Field Name Mapping with @TemplateField

Starting from version 0.8.0, you can use the `@TemplateField` annotation to explicitly control how Java fields and methods are accessed in templates.

#### Why Use @TemplateField?

By default, gotemplate4j uses Go-style name conversion (capitalizing the first letter) to map Java property names to template identifiers. The `@TemplateField` annotation gives you explicit control over this mapping, which is useful when:

- You want to use a different name in templates than the Java field name
- You need to support legacy template names during refactoring
- You want to expose private fields without creating public getters
- You need precise control over the template API

#### Basic Usage

**On Fields:**

```java
import io.github.verils.gotemplate.TemplateField;

public class User {
    @TemplateField("UserName")
    private String userName = "Alice";
    
    @TemplateField("user_email")
    private String email = "alice@example.com";
}
```

**Template:**
```gotemplate
Name: {{.UserName}}, Email: {{.user_email}}
```

**Output:**
```
Name: Alice, Email: alice@example.com
```

**On Methods:**

```java
public class User {
    private String firstName = "John";
    private String lastName = "Doe";
    
    @TemplateField("FullName")
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

**Template:**
```gotemplate
{{.FullName}}
```

**Output:**
```
John Doe
```

#### Lookup Priority

When resolving a template field reference, gotemplate4j checks in this order:

1. **@TemplateField annotation** - Exact match on annotated fields/methods
2. **Exact match** - Direct match on Java property/field name
3. **Go-style capitalization** - First letter capitalized (e.g., `userName` → `UserName`)

Example demonstrating priority:

```java
public class Product {
    // This can be accessed as {{.Price}} via annotation
    @TemplateField("Price")
    private double price = 99.99;
    
    // This can be accessed as {{.name}} (exact match) or {{.Name}} (Go-style)
    public String name = "Widget";
}
```

#### Field vs Method Precedence

If both a field and its getter method have `@TemplateField` annotations with the same value, the **field takes precedence**:

```java
public class Example {
    @TemplateField("value")
    public String fieldValue = "from-field";
    
    @TemplateField("value")
    public String getFieldValue() {
        return "from-method";
    }
}
```

**Template:**
```gotemplate
{{.value}}  // Outputs: from-field
```

#### Private Field Access

The `@TemplateField` annotation allows templates to access private fields directly without requiring public getters:

```java
public class Config {
    @TemplateField("ApiKey")
    private String apiKey = "secret-key";  // Private but accessible
    
    // No getter needed!
}
```

**Template:**
```gotemplate
API Key: {{.ApiKey}}
```

This is particularly useful for:
- Encapsulating data while keeping templates clean
- Avoiding boilerplate getter methods
- Maintaining backward compatibility during refactoring

#### Inheritance Support

Annotations work across class hierarchies:

```java
public class BaseEntity {
    @TemplateField("Id")
    private Long id;
    
    public Long getId() { return id; }
}

public class User extends BaseEntity {
    @TemplateField("UserName")
    private String userName;
    
    public String getUserName() { return userName; }
}
```

**Template:**
```gotemplate
ID: {{.Id}}, Name: {{.UserName}}
```

#### Best Practices

1. **Use consistent naming**: Choose a naming convention (camelCase, PascalCase, or snake_case) and stick with it
2. **Document your API**: When using custom names, document them for template authors
3. **Prefer methods for computed values**: Use `@TemplateField` on methods that compute or format data
4. **Keep annotations close to declaration**: Place annotations directly on fields/methods for clarity
5. **Avoid duplicate names**: Don't use the same template name for multiple fields/methods

#### Migration Example

When refactoring field names, use `@TemplateField` to maintain backward compatibility:

```java
public class LegacyUser {
    // Old field name - keep supporting old templates
    @TemplateField("usr_name")
    private String userName;  // New Java field name
    
    // Can gradually migrate templates from {{.usr_name}} to {{.UserName}}
}
```

This allows you to refactor Java code without breaking existing templates.

## Lists and Arrays

Collections are accessed using `range` or by index.

### List Iteration

```java
List<String> items = Arrays.asList("Apple", "Banana", "Cherry");

template.parse(
    "Items:\n" +
    "{{range .Items}}" +
    "- {{.}}\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Items", items);
```

**Output:**
```
Items:
- Apple
- Banana
- Cherry
```

### Array Iteration

Arrays work the same way:

```java
String[] items = new String[]{"Apple", "Banana", "Cherry"};
data.put("Items", items);
// Same template works
```

### With Index

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

**Output:**
```
0: Apple
1: Banana
2: Cherry
```

### Access by Index

Use indexing to access specific elements:

```gotemplate
First item: {{index .Items 0}}
Second item: {{index .Items 1}}
```

Note: The `index` function is used for accessing elements by position.

### Empty Collections

Check if collection is empty:

```gotemplate
{{if .Items}}
  Has {{len .Items}} items
{{else}}
  No items
{{end}}
```

Or check length explicitly:

```gotemplate
{{if eq (len .Items) 0}}
  Empty
{{end}}
```

### List of Objects

```java
List<User> users = Arrays.asList(
    new User("Alice", 30),
    new User("Bob", 25),
    new User("Charlie", 35)
);

template.parse(
    "Users:\n" +
    "{{range .Users}}" +
    "- {{.Name}} ({{.Age}})\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Users", users);
```

**Output:**
```
Users:
- Alice (30)
- Bob (25)
- Charlie (35)
```

## Enums

Enums are supported and can be compared.

### Enum Definition

```java
public enum Status {
    ACTIVE, INACTIVE, PENDING
}

public class User {
    private String name;
    private Status status;
    
    public String getName() { return name; }
    public Status getStatus() { return status; }
}
```

### Using Enums in Templates

```java
User user = new User("Alice", Status.ACTIVE);

template.parse(
    "{{if eq .Status \"ACTIVE\"}}" +
    "User is active" +
    "{{else}}" +
    "User is not active" +
    "{{end}}"
);
```

### Enum Comparison

Compare enum values:

```gotemplate
{{if eq .Status "ACTIVE"}}Active{{end}}
{{if ne .Status "INACTIVE"}}Not inactive{{end}}
```

### Enum in Switch-like Pattern

```gotemplate
{{if eq .Status "ACTIVE"}}
  Active user
{{else if eq .Status "PENDING"}}
  Pending approval
{{else}}
  Inactive user
{{end}}
```

## Optional Values

Java 8 `Optional` is supported.

### Basic Optional

```java
Optional<String> name = Optional.of("Alice");
Optional<String> email = Optional.empty();

Map<String, Object> data = new HashMap<>();
data.put("Name", name);
data.put("Email", email);

template.parse(
    "Name: {{.Name}}\n" +
    "Email: {{.Email}}"
);
```

**Output:**
```
Name: Alice
Email: 
```

Empty Optionals render as empty string.

### Checking Optional

```gotemplate
{{if .Email}}
  Email: {{.Email}}
{{else}}
  No email provided
{{end}}
```

### Optional in Conditionals

Optionals are truthy if present, falsy if empty:

```gotemplate
{{if .Name}}Has name{{else}}No name{{end}}
```

## Primitive Types

All Java primitive types and their wrappers are supported.

### Numbers

```java
Map<String, Object> data = new HashMap<>();
data.put("Count", 42);
data.put("Price", 19.99);
data.put("Large", 1000000L);
data.put("Small", 0.001f);

template.parse(
    "Count: {{.Count}}\n" +
    "Price: {{.Price}}\n" +
    "Large: {{.Large}}\n" +
    "Small: {{.Small}}"
);
```

### Booleans

```java
data.put("IsActive", true);
data.put("IsDeleted", false);

template.parse(
    "{{if .IsActive}}Active{{end}}\n" +
    "{{if .IsDeleted}}Deleted{{else}}Not deleted{{end}}"
);
```

### Strings

```java
data.put("Name", "Alice");
data.put("Empty", "");
data.put("Null", null);

template.parse(
    "Name: '{{.Name}}'\n" +
    "Empty: '{{.Empty}}'\n" +
    "Null: '{{.Null}}'"
);
```

## Null Handling

Null values are handled based on the MissingKeyPolicy.

### Default Behavior

By default, null values render as empty string:

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", null);

template.parse("Name: '{{.Name}}'");
// Output: Name: ''
```

### MissingKeyPolicy Options

Configure how missing/null keys are handled:

```java
// Option 1: DEFAULT - Render as empty string
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);

// Option 2: ZERO - Render as zero value
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ZERO);

// Option 3: ERROR - Throw exception
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

### Checking for Null

```gotemplate
{{if .Name}}
  Name: {{.Name}}
{{else}}
  No name provided
{{end}}
```

## Complex Data Structures

Real-world data often combines multiple types.

### Example: User Profile

```java
// Define classes
public class Address {
    private String street;
    private String city;
    private String country;
    
    // Getters...
}

public class User {
    private String name;
    private int age;
    private String email;
    private List<Address> addresses;
    private Map<String, String> preferences;
    
    // Getters...
}

// Create data
Address addr1 = new Address("123 Main St", "Beijing", "China");
Address addr2 = new Address("456 Oak Ave", "Shanghai", "China");

User user = new User();
user.setName("Alice");
user.setAge(30);
user.setEmail("alice@example.com");
user.setAddresses(Arrays.asList(addr1, addr2));

Map<String, String> prefs = new HashMap<>();
prefs.put("theme", "dark");
prefs.put("language", "en");
user.setPreferences(prefs);

// Template
String templateText = 
    "User: {{.Name}}\n" +
    "Age: {{.Age}}\n" +
    "Email: {{.Email}}\n" +
    "\n" +
    "Addresses:\n" +
    "{{range .Addresses}}" +
    "- {{.Street}}, {{.City}}, {{.Country}}\n" +
    "{{end}}" +
    "\n" +
    "Preferences:\n" +
    "{{range $key, $value := .Preferences}}" +
    "{{$key}}: {{$value}}\n" +
    "{{end}}";

Template template = new Template("profile");
template.parse(templateText);

StringWriter writer = new StringWriter();
template.execute(writer, user);
System.out.println(writer.toString());
```

**Output:**
```
User: Alice
Age: 30
Email: alice@example.com

Addresses:
- 123 Main St, Beijing, China
- 456 Oak Ave, Shanghai, China

Preferences:
theme: dark
language: en
```

## Type Conversion

gotemplate4j performs automatic type conversion in some contexts.

### Numeric Conversion

Numbers can be compared across types:

```gotemplate
{{if eq .IntValue .LongValue}}Equal{{end}}
```

### String Conversion

Values are converted to strings for display:

```java
data.put("Number", 42);
template.parse("Value: {{.Number}}");  // Output: Value: 42
```

### Boolean Conversion

In conditionals, values are evaluated for truthiness (see [Template Syntax](template-syntax.md)).

## Best Practices

### 1. Use Maps for Simple Data

For simple templates, Maps are easiest:

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");
data.put("Age", 30);
```

### 2. Use JavaBeans for Complex Data

For structured data with behavior, use JavaBeans:

```java
public class User {
    private String name;
    private int age;
    
    public String getDisplayName() {
        return name + " (Age: " + age + ")";
    }
    
    // Getters...
}
```

### 3. Avoid Deep Nesting

Keep data structure shallow when possible:

❌ **Bad:**
```java
data.put("User", Map.of(
    "Profile", Map.of(
        "Details", Map.of(
            "Name", "Alice"
        )
    )
));
```
```gotemplate
{{.User.Profile.Details.Name}}
```

✅ **Good:**
```java
data.put("UserName", "Alice");
```
```gotemplate
{{.UserName}}
```

### 4. Handle Missing Keys

Always consider what happens when data is missing:

```gotemplate
{{if .Name}}{{.Name}}{{else}}Anonymous{{end}}
```

### 5. Document Data Structure

Document what data your template expects:

```java
/**
 * Template expects:
 * - User: User object with Name, Email
 * - Items: List of Item objects
 * - ShowFooter: boolean
 */
```

## Common Patterns

### Pattern 1: Default Values

```gotemplate
{{if .Title}}{{.Title}}{{else}}Default Title{{end}}
```

Or create a custom `default` function:

```java
functions.put("default", args -> {
    if (args[0] == null || args[0].equals("")) {
        return args[1];
    }
    return args[0];
});
```
```gotemplate
{{.Title | default "Default Title"}}
```

### Pattern 2: Conditional Sections

```gotemplate
{{if .ShowDetails}}
  <div class="details">
    {{.Details}}
  </div>
{{end}}
```

### Pattern 3: Loop with Alternating Styles

```gotemplate
{{range $index, $item := .Items}}
  <div class="{{if mod $index 2 | eq 0}}even{{else}}odd{{end}}">
    {{$item}}
  </div>
{{end}}
```

### Pattern 4: Nested Data with Fallback

```gotemplate
{{if .User}}
  {{if .User.Name}}
    {{.User.Name}}
  {{else}}
    Anonymous User
  {{end}}
{{else}}
  No User
{{end}}
```

## Troubleshooting

### Problem: Field Not Found

**Error:** `can't evaluate field X in type Y`

**Solution:** Check that:
1. The field name is correct (case-sensitive)
2. The getter method exists (for JavaBeans)
3. The key exists (for Maps)

### Problem: Null Pointer Exception

**Solution:** Check for null before accessing:

```gotemplate
{{if .User}}{{.User.Name}}{{end}}
```

### Problem: Wrong Type

**Error:** `unexpected type`

**Solution:** Ensure you're using the right operations for the type:

```gotemplate
<!-- Can't iterate over a string -->
{{range .Name}}...{{end}}  <!-- Error if .Name is string -->

<!-- Can't compare incompatible types -->
{{if eq .StringField .IntField}}...{{end}}  <!-- May fail -->
```

---

**Next Steps:**
- 🔧 Learn about [Functions](functions.md) for data manipulation
- 🎯 Explore [Control Flow](control-flow.md) for logic
- 📚 Review [Template Syntax](template-syntax.md) for complete reference
