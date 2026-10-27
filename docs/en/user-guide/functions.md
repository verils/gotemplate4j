# Functions Guide

This guide covers all built-in functions in gotemplate4j and how to create custom functions.

## Overview

Functions perform operations on data in templates. gotemplate4j includes many built-in functions and allows you to register custom functions.

### Function Call Syntax

```gotemplate
{{functionName arg1 arg2 arg3}}
```

Or using pipeline:

```gotemplate
{{arg1 | functionName}}
{{arg1 | functionName arg2}}
```

## Built-in Functions

### String Functions

#### upper

Convert string to uppercase.

```gotemplate
{{upper "hello"}}
<!-- Output: HELLO -->

{{.Name | upper}}
```

#### lower

Convert string to lowercase.

```gotemplate
{{lower "HELLO"}}
<!-- Output: hello -->

{{.Name | lower}}
```

#### title

Convert string to title case (first letter of each word capitalized).

```gotemplate
{{title "hello world"}}
<!-- Output: Hello World -->
```

#### trim

Remove leading and trailing whitespace.

```gotemplate
{{trim "  hello  "}}
<!-- Output: hello -->

{{.Text | trim}}
```

#### trimLeft

Remove leading whitespace.

```gotemplate
{{trimLeft "  hello  "}}
<!-- Output: hello   -->
```

#### trimRight

Remove trailing whitespace.

```gotemplate
{{trimRight "  hello  "}}
<!-- Output:   hello -->
```

#### trimPrefix

Remove prefix from string.

```gotemplate
{{trimPrefix "Hello" "He"}}
<!-- Output: llo -->
```

#### trimSuffix

Remove suffix from string.

```gotemplate
{{trimSuffix "Hello" "lo"}}
<!-- Output: Hel -->
```

#### replace

Replace all occurrences of a substring.

```gotemplate
{{replace "hello world" "world" "go"}}
<!-- Output: hello go -->

{{.Text | replace "old" "new"}}
```

#### repeat

Repeat string n times.

```gotemplate
{{repeat "ab" 3}}
<!-- Output: ababab -->
```

#### len

Get length of string, array, list, map, or channel.

```gotemplate
{{len "hello"}}
<!-- Output: 5 -->

{{len .Items}}
<!-- Output: number of items -->
```

#### substr

Extract substring (Go-compatible).

```gotemplate
{{substr "hello" 1 4}}
<!-- Output: ell -->
```

### Mathematical Functions

#### add

Add numbers.

```gotemplate
{{add 1 2}}
<!-- Output: 3 -->

{{add 1 2 3 4}}
<!-- Output: 10 -->

{{add .Count 1}}
```

#### sub

Subtract numbers.

```gotemplate
{{sub 10 3}}
<!-- Output: 7 -->

{{sub .Total .Used}}
```

#### mul

Multiply numbers.

```gotemplate
{{mul 3 4}}
<!-- Output: 12 -->

{{mul .Price .Quantity}}
```

#### div

Divide numbers.

```gotemplate
{{div 10 2}}
<!-- Output: 5 -->

{{div .Total .Count}}
```

Note: Integer division truncates. Use floats for decimal results.

#### mod

Modulo (remainder) operation.

```gotemplate
{{mod 10 3}}
<!-- Output: 1 -->

{{if mod .Index 2 | eq 0}}Even{{else}}Odd{{end}}
```

#### pow

Power function (if available).

```gotemplate
{{pow 2 3}}
<!-- Output: 8 -->
```

### Comparison Functions

#### eq

Equality comparison. `eq` requires at least two arguments and returns true when
the first value equals any following value.

```gotemplate
{{eq 1 1}}
<!-- Output: true -->

{{eq .Status "active" "pending"}}
<!-- True when .Status is either active or pending. -->

{{eq .Status "active"}}
{{if eq .Age 18}}Exactly 18{{end}}
```

#### ne

Not-equal comparison. It requires exactly two arguments.

```gotemplate
{{ne 1 2}}
<!-- Output: true -->

{{if ne .Status "inactive"}}Not inactive{{end}}
```

#### lt

Less than. It requires exactly two arguments.

```gotemplate
{{lt 1 2}}
<!-- Output: true -->

{{if lt .Age 18}}Minor{{end}}
```

#### le

Less than or equal. It requires exactly two arguments.

```gotemplate
{{le 2 2}}
<!-- Output: true -->

{{if le .Score 100}}Valid score{{end}}
```

#### gt

Greater than. It requires exactly two arguments.

```gotemplate
{{gt 2 1}}
<!-- Output: true -->

{{if gt .Age 18}}Adult{{end}}
```

#### ge

Greater than or equal. It requires exactly two arguments.

```gotemplate
{{ge 2 2}}
<!-- Output: true -->

{{if ge .Score 60}}Pass{{end}}
```

Comparisons use Go-style type groups. Integer types can compare with each other,
and decimal types with each other, but integers and decimals are not
interchangeable. Boolean values only support `eq/ne`. Arrays, collections, and
maps must be compared with `deepEqual`; incompatible types raise a template
execution error.

### Logical Functions

#### and

Logical AND with short-circuit evaluation.

```gotemplate
{{and true true}}
<!-- Output: true -->

{{and true false}}
<!-- Output: false -->

{{if and .IsActive .IsVerified}}Active and verified{{end}}
```

Returns first false value or last value if all are true.

#### or

Logical OR with short-circuit evaluation.

```gotemplate
{{or true false}}
<!-- Output: true -->

{{or false false}}
<!-- Output: false -->

{{if or .IsAdmin .IsModerator}}Has permissions{{end}}
```

Returns first true value or last value if all are false.

#### not

Logical NOT.

```gotemplate
{{not true}}
<!-- Output: false -->

{{not false}}
<!-- Output: true -->

{{if not .IsDeleted}}Not deleted{{end}}
```

### Formatting Functions

#### printf

Format a string using the supported Go-style format verbs.

```gotemplate
{{printf "Hello, %s!" "World"}}
<!-- Output: Hello, World! -->

{{printf "%d items" 5}}
<!-- Output: 5 items -->

{{printf "%.2f" 3.14159}}
<!-- Output: 3.14 -->

{{printf "%05d" 42}}
<!-- Output: 00042 -->

{{printf "%#x %q %T" 255 "hello" .Age}}
```

Supported verbs:
- `%v` - Default value representation; arrays use `deepToString`
- `%T` - Java fully qualified class name, or `<nil>` for null
- `%t` - Boolean
- `%q` - Quoted and escaped string or Character
- `%d` - Integer
- `%s` - String or Character
- `%x`, `%X` - Lowercase or uppercase hexadecimal integer
- `%f` - Floating-point value
- `%%` - Literal percent sign

The `-`, `+`, space, `0`, and `#` flags, decimal width, and `.precision` are
supported where Java formatting defines them. Dynamic width/precision (`*`) and
argument indexes such as `%[1]s` are not supported.

Collections, maps, and ordinary objects use their Java text representation;
Go's recursive aggregate formatting is not emulated. Unsupported verbs,
malformed specifications, argument count mismatches, and incompatible value
types raise a template execution error.

#### print

Convert arguments using the default `%v` representation. A space is inserted
only when neither the current nor the previous operand is a string.

```gotemplate
{{print "Hello" " " "World"}}
<!-- Output: Hello World -->

{{print "hello" "world"}}
<!-- Output: helloworld -->

{{print 1 2}}
<!-- Output: 1 2 -->

{{print .Name " (" .Age ")"}}
```

#### println

Like print, but always places one space between operands and appends a newline.

```gotemplate
{{println "Hello" "World"}}
<!-- Output: Hello World\n -->

{{println}}
<!-- Output: a single newline -->
```

### Type Functions

#### typeof

Get the type name of a value.

```gotemplate
{{typeof .Name}}
<!-- Output: java.lang.String (or similar) -->
```

#### kindof

Get the kind of a value (Go-compatible).

```gotemplate
{{kindof .Name}}
<!-- Output: String -->

{{kindof .Items}}
<!-- Output: Slice, Map, etc. -->
```

### Default and Validation Functions

#### default

Return a fallback when the input has a Go-template zero value. Falsy inputs are
`null`, `false`, numeric zero, empty strings or character sequences, empty
arrays, collections and maps, and empty `Optional` values. Whitespace-only
strings and other non-null objects are truthy.

```gotemplate
{{default .Name "Anonymous"}}
{{.Name | default "Anonymous"}}
<!-- Both forms output Anonymous when .Name is null or empty. -->

{{if eq (.Name | default "") ""}}No name{{end}}
```

Direct and pipeline calls use the same order conceptually: the value is checked
and the explicit argument supplies the fallback.

#### index

Access an element in arrays, lists, collections, strings, or maps. Additional
arguments continue indexing the previous result, so nested containers can be
accessed in one expression.

```gotemplate
{{index .Items 0}}
<!-- First item -->

{{index .MapData "key"}}
<!-- Map value -->

{{index .Matrix 1 2}}
<!-- Multi-dimensional indexing -->

{{index .Users 0 "Name"}}
<!-- Index a list, then index the resulting map -->
```

Sequence indexes must be integral Java values. Strings, floating-point values,
`null`, negative indexes, and out-of-range indexes raise a template execution
error. Missing map keys continue to follow `MissingKeyPolicy`.

#### slice

Extract a slice from an array, list, or string. The function accepts zero to
three indexes:

```gotemplate
{{slice .Items}}
<!-- The complete sequence -->

{{slice .Items 1}}
<!-- From index 1 to the end -->

{{slice "hello" 1 4}}
<!-- Output: ell -->

{{slice .Items 1 4 5}}
<!-- Bounded slice: start=1, end=4, max=5 -->
```

Indexes must be integral and non-negative, and must not exceed the sequence
length. Reversed or out-of-range ranges raise an error. The third index is not
allowed for strings.

### URL and HTML Functions

#### urlquery

URL-encode values using Go `text/template` query-escaping semantics. The
function accepts zero or more arguments and joins them using Go's `Sprint`
spacing rules.

```gotemplate
{{urlquery "hello world"}}
<!-- Output: hello+world -->

{{urlquery "a~*"}}
<!-- Output: a~%2A -->

<a href="/search?q={{urlquery .Query}}">Search</a>
```

#### html

HTML-escape values using Go `text/template` semantics. The function accepts zero
or more arguments, preserves printable Unicode, and replaces NUL with the
Unicode replacement character.

```gotemplate
{{html "<script>alert('xss')</script>"}}
<!-- Output: &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt; -->

{{html "\"quoted\""}}
<!-- Output: &#34;quoted&#34; -->
```

#### js

JavaScript-escape values using Go's `JSEscapeString` semantics. HTML-sensitive
characters and ASCII control characters are emitted as Unicode escapes, while
printable Unicode characters are preserved. The function accepts zero or more
arguments.

```gotemplate
{{js "hello\nworld"}}
<!-- Output: hello\u000Aworld -->

{{js "</script>"}}
<!-- Output: \u003C/script\u003E -->
```

The `js` function is a string escaper from Go `text/template`; it is not a
replacement for the contextual auto-escaping provided by Go `html/template`.
Keep escaped values inside quoted JavaScript strings and use context-appropriate
validation when inserting them into other JavaScript contexts.

### Date and Time Functions

#### now

Get current time.

```gotemplate
{{now}}
<!-- Output: current timestamp -->
```

#### date

Format date/time (if available).

```gotemplate
{{date "2006-01-02" .Timestamp}}
<!-- Output: formatted date -->
```

Note: Date formatting uses Go's reference time: `Mon Jan 2 15:04:05 MST 2006`

### JSON Functions

#### json

Encode value as JSON.

```gotemplate
{{json .Data}}
<!-- Output: JSON representation -->

<script>
var data = {{json .Data}};
</script>
```

#### jsonify

Alias for json (Go-compatible).

```gotemplate
{{jsonify .Data}}
```

## Custom Functions

Register custom functions when creating a template.

### Basic Custom Function

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.util.HashMap;
import java.util.Map;

// Create function map
Map<String, Function> functions = new HashMap<>();

// Register custom function
functions.put("double", args -> {
    int value = (Integer) args[0];
    return value * 2;
});

// Create template with custom functions
Template template = new Template("demo", functions);
template.parse("Double of 5 is {{double 5}}");

StringWriter writer = new StringWriter();
template.execute(writer, new HashMap<>());
System.out.println(writer.toString());
// Output: Double of 5 is 10
```

### Function with Multiple Arguments

```java
functions.put("multiply", args -> {
    int a = (Integer) args[0];
    int b = (Integer) args[1];
    return a * b;
});

template.parse("{{multiply 3 4}}");
// Output: 12
```

### Function with Variable Arguments

```java
functions.put("sum", args -> {
    int total = 0;
    for (Object arg : args) {
        total += ((Number) arg).intValue();
    }
    return total;
});

template.parse("{{sum 1 2 3 4 5}}");
// Output: 15
```

### Function Returning Complex Types

```java
functions.put("createUser", args -> {
    String name = (String) args[0];
    int age = (Integer) args[1];
    
    Map<String, Object> user = new HashMap<>();
    user.put("Name", name);
    user.put("Age", age);
    return user;
});

template.parse(
    "{{with createUser \"Alice\" 30}}" +
    "{{.Name}} is {{.Age}}" +
    "{{end}}"
);
// Output: Alice is 30
```

### Function in Pipeline

```java
functions.put("greet", args -> {
    String name = (String) args[0];
    return "Hello, " + name + "!";
});

template.parse("{{.Name | greet}}");
// With data: {"Name": "Alice"}
// Output: Hello, Alice!
```

### Function with Context Access

Functions can't directly access template context, but you can pass it:

```java
functions.put("fullName", args -> {
    Map<String, Object> user = (Map<String, Object>) args[0];
    String first = (String) user.get("FirstName");
    String last = (String) user.get("LastName");
    return first + " " + last;
});

template.parse("{{fullName .User}}");
```

## Common Patterns

### Pattern 1: Conditional Display

```gotemplate
{{if .Name}}{{.Name | upper}}{{else}}ANONYMOUS{{end}}
```

### Pattern 2: Default Values

```gotemplate
{{.Title | default "Untitled"}}
```

### Pattern 3: Formatting Numbers

```gotemplate
Price: ${{printf "%.2f" .Price}}
```

### Pattern 4: String Manipulation

```gotemplate
{{.Text | trim | lower | replace " " "-"}}
```

### Pattern 5: Math Operations

```gotemplate
Total: ${{mul .Price .Quantity | printf "%.2f"}}
Discount: {{mul .Price .Quantity .Discount | div 100 | printf "%.2f"}}
```

### Pattern 6: List Operations

```gotemplate
{{range $i, $item := .Items}}
  {{if gt $i 0}}, {{end}}{{$item}}
{{end}}
```

Output: `item1, item2, item3`

### Pattern 7: Alternating Styles

```gotemplate
{{range $i, $item := .Items}}
  <div class="{{if mod $i 2 | eq 0}}even{{else}}odd{{end}}">
    {{$item}}
  </div>
{{end}}
```

## Best Practices

### 1. Use Built-in Functions When Possible

Built-in functions are optimized and well-tested.

✅ **Good:**
```gotemplate
{{upper .Name}}
```

❌ **Bad:** Creating custom function for simple operations

### 2. Keep Functions Simple

Functions should do one thing well.

✅ **Good:**
```java
functions.put("double", args -> {
    return ((Number) args[0]).intValue() * 2;
});
```

❌ **Bad:** Complex logic in functions

### 3. Handle Errors Gracefully

```java
functions.put("safeDivide", args -> {
    double a = ((Number) args[0]).doubleValue();
    double b = ((Number) args[1]).doubleValue();
    if (b == 0) {
        return 0;  // Or throw TemplateException
    }
    return a / b;
});
```

### 4. Document Custom Functions

```java
/**
 * Formats a phone number.
 * Expects: (String) phone number digits
 * Returns: Formatted phone number (xxx) xxx-xxxx
 */
functions.put("formatPhone", args -> {
    // Implementation...
});
```

### 5. Test Functions Thoroughly

```java
@Test
public void testDoubleFunction() throws Exception {
    Map<String, Function> functions = new HashMap<>();
    functions.put("double", args -> ((Number) args[0]).intValue() * 2);
    
    Template template = new Template("test", functions);
    template.parse("{{double 5}}");
    
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
    
    assertEquals("10", writer.toString().trim());
}
```

## Performance Considerations

### 1. Avoid Expensive Operations

Don't do heavy computation in functions called in loops.

❌ **Bad:**
```gotemplate
{{range .Items}}
  {{expensiveOperation .}}
{{end}}
```

✅ **Good:** Pre-compute in Java code

### 2. Cache Results

If function is called multiple times with same arguments, cache result.

### 3. Minimize Function Calls

Chain operations efficiently:

❌ **Bad:**
```gotemplate
{{upper (trim .Text)}}
```

✅ **Good:**
```gotemplate
{{.Text | trim | upper}}
```

## Troubleshooting

### Problem: Function Not Found

**Error:** `function "xyz" not defined`

**Solution:** 
1. Check function name spelling
2. Verify function is registered
3. Ensure correct function map is passed to template

### Problem: Wrong Number of Arguments

**Error:** `wrong number of args for xyz`

**Solution:** Check function signature and call

### Problem: Type Mismatch

**Error:** `can't handle type X for function Y`

**Solution:** Cast arguments to expected types

```java
functions.put("myFunc", args -> {
    // Safe casting
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("Expected String");
    }
    String value = (String) args[0];
    // ...
});
```

## Complete Example

```java
import io.github.verils.gotemplate.*;
import java.util.*;

public class FunctionExample {
    public static void main(String[] args) throws Exception {
        // Create custom functions
        Map<String, Function> functions = new HashMap<>();
        
        // Greeting function
        functions.put("greet", args -> {
            String name = (String) args[0];
            return "Hello, " + name + "!";
        });
        
        // Format currency
        functions.put("currency", args -> {
            double amount = ((Number) args[0]).doubleValue();
            return String.format("$%.2f", amount);
        });
        
        // Create template
        String templateText = 
            "{{greet .Name}}\n" +
            "Items: {{len .Items}}\n" +
            "Total: {{mul .Price .Quantity | currency}}\n" +
            "Discounted: {{mul .Price .Quantity .Discount | div 100 | currency}}";
        
        Template template = new Template("invoice", functions);
        template.parse(templateText);
        
        // Prepare data
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "Alice");
        data.put("Items", Arrays.asList("Widget", "Gadget"));
        data.put("Price", 19.99);
        data.put("Quantity", 5);
        data.put("Discount", 10);  // 10%
        
        // Execute
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        System.out.println(writer.toString());
    }
}
```

**Output:**
```
Hello, Alice!
Items: 2
Total: $99.95
Discounted: $10.00
```

---

**Next Steps:**
- 🎯 Learn about [Control Flow](control-flow.md) for conditionals and loops
- 📊 Understand [Data Models](data-models.md) for working with Java objects
- 📚 Review [Template Syntax](template-syntax.md) for complete reference
