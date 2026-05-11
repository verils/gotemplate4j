# Control Flow Guide

This guide explains control flow constructs in gotemplate4j: conditionals, loops, and context management.

## Overview

Control flow allows you to add logic to templates:
- **Conditionals**: Make decisions with `if`/`else`
- **Loops**: Iterate over collections with `range`
- **Context**: Change data context with `with`
- **Loop Control**: Use `break` and `continue`

## Conditional Actions

### Basic If

The `if` action evaluates a condition and renders content if true.

```gotemplate
{{if .IsActive}}
  User is active
{{end}}
```

### If/Else

```gotemplate
{{if .IsActive}}
  Active user
{{else}}
  Inactive user
{{end}}
```

### If/Else If/Else

Chain multiple conditions:

```gotemplate
{{if eq .Status "active"}}
  Active
{{else if eq .Status "pending"}}
  Pending
{{else if eq .Status "suspended"}}
  Suspended
{{else}}
  Unknown status
{{end}}
```

### Nested Conditionals

```gotemplate
{{if .User}}
  {{if .User.Name}}
    Hello, {{.User.Name}}!
  {{else}}
    Hello, Anonymous!
  {{end}}
{{else}}
  No user logged in
{{end}}
```

## Truthiness

Conditions evaluate values for "truthiness".

### False Values

These values are considered **false**:
- Boolean `false`
- Numeric zero (`0`, `0.0`, etc.)
- `null` or missing values
- Empty string `""`
- Empty collections (length 0)

### True Values

All other values are **true**:
- Boolean `true`
- Non-zero numbers
- Non-empty strings
- Non-empty collections
- Any object (even if fields are null)

### Examples

```gotemplate
{{if 0}}Zero is false{{end}}        <!-- Won't print -->
{{if 1}}Non-zero is true{{end}}     <!-- Will print -->

{{if ""}}Empty string is false{{end}}      <!-- Won't print -->
{{if "x"}}Non-empty is true{{end}}         <!-- Will print -->

{{if nil}}Nil is false{{end}}              <!-- Won't print -->

{{if .EmptyList}}Has items{{end}}          <!-- Won't print if empty -->
{{if .NonEmptyList}}Has items{{end}}       <!-- Will print -->
```

## Comparison Operators

Use comparison functions in conditions.

### Equal (eq)

```gotemplate
{{if eq .Status "active"}}Active{{end}}
{{if eq .Age 18}}Exactly 18{{end}}
{{if eq .Count 0}}No items{{end}}
```

Compare multiple values (all must be equal):

```gotemplate
{{if eq .A .B .C}}All equal{{end}}
```

### Not Equal (ne)

```gotemplate
{{if ne .Status "inactive"}}Not inactive{{end}}
{{if ne .Password .ConfirmPassword}}Passwords don't match{{end}}
```

### Less Than (lt)

```gotemplate
{{if lt .Age 18}}Minor{{end}}
{{if lt .Score 60}}Failing{{end}}
```

### Less Than or Equal (lte)

```gotemplate
{{if lte .Age 18}}18 or younger{{end}}
{{if lte .Items 100}}Within limit{{end}}
```

### Greater Than (gt)

```gotemplate
{{if gt .Age 18}}Adult{{end}}
{{if gt .Score 90}}Excellent{{end}}
```

### Greater Than or Equal (gte)

```gotemplate
{{if gte .Age 18}}18 or older{{end}}
{{if gte .Balance 1000}}Premium customer{{end}}
```

## Logical Operators

### AND

Short-circuit evaluation: if first argument is false, second is not evaluated.

```gotemplate
{{if and .IsActive .IsVerified}}Active and verified{{end}}

<!-- Multiple conditions -->
{{if and .IsActive .IsVerified .HasPermission}}Access granted{{end}}
```

### OR

Short-circuit evaluation: if first argument is true, second is not evaluated.

```gotemplate
{{if or .IsAdmin .IsModerator}}Has permissions{{end}}

<!-- Multiple conditions -->
{{if or .IsAdmin .IsModerator .IsOwner}}Full access{{end}}
```

### NOT

```gotemplate
{{if not .IsDeleted}}Not deleted{{end}}
{{if not .IsEmpty}}Has content{{end}}
```

### Complex Logic

Combine operators:

```gotemplate
{{if and (or .IsAdmin .IsModerator) (not .IsSuspended)}}
  Can moderate
{{end}}
```

For complex logic, consider computing in Java:

```java
data.put("CanModerate", user.hasPermission() && !user.isSuspended());
```
```gotemplate
{{if .CanModerate}}Can moderate{{end}}
```

## Range Action

Iterate over arrays, lists, maps, and integers.

### Basic Range

```gotemplate
{{range .Items}}
  {{.}}
{{end}}
```

Inside the range block, `.` refers to the current item.

### Range with Index

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

Output:
```
0: Apple
1: Banana
2: Cherry
```

### Range Over Map

```gotemplate
{{range $key, $value := .Config}}
  {{$key}}: {{$value}}
{{end}}
```

#### Map Iteration Order

**By default, map keys are sorted to provide deterministic output**, matching Go template behavior.

**Go Template Specification**: "If the value is a map and the keys are of basic type with a defined order, the elements will be visited in sorted key order."

**Sorting Behavior**:
- **String keys**: Sorted alphabetically (lexicographic order)
- **Integer keys**: Sorted numerically
- **Comparable keys**: Use natural ordering
- **Other types**: Fall back to `toString()` comparison

**Example - String Keys**:

```java
Map<String, Object> data = new HashMap<>();
Map<String, String> config = new LinkedHashMap<>();
config.put("zebra", "z");      // Inserted first
config.put("apple", "a");      // Inserted second
config.put("mango", "m");      // Inserted third
data.put("Config", config);

Template template = new Template("demo");
template.parse("{{range $k, $v := .Config}}{{$k}}={{$v}},{{end}}");
template.execute(writer, data);
// Output: apple=a,mango=m,zebra=z,  (sorted alphabetically)
```

**Example - Integer Keys**:

```java
Map<Integer, String> scores = new LinkedHashMap<>();
scores.put(3, "third");   // Inserted first
scores.put(1, "first");   // Inserted second
scores.put(2, "second");  // Inserted third
data.put("Scores", scores);

template.parse("{{range $k, $v := .Scores}}{{$k}}:{{$v}},{{end}}");
// Output: 1:first,2:second,3:third  (sorted numerically)
```

#### Disabling Map Key Sorting

If you need to preserve insertion order (e.g., when using `LinkedHashMap`), you can disable sorting:

```java
Template template = new Template("demo")
    .withMapKeySorting(false);  // Disable sorting
```

Or via option string:

```java
Template template = new Template("demo");
template.option("mapkeysorting=false");
```

**When to disable sorting**:
- You rely on specific insertion order from `LinkedHashMap`
- Performance-critical scenarios with very large maps
- Maintaining compatibility with existing templates that depend on insertion order

**Note**: For Go template compatibility, it's recommended to keep sorting enabled (default).

### Range Over Integer (Go-compatible)

Iterate from 0 to n-1:

```gotemplate
{{range $i := 5}}
  Iteration {{$i}}
{{end}}
```

Output:
```
Iteration 0
Iteration 1
Iteration 2
Iteration 3
Iteration 4
```

Useful for generating repeated elements:

```gotemplate
<select name="year">
{{range $i := 10}}
  <option value="{{add 2020 $i}}">{{add 2020 $i}}</option>
{{end}}
</select>
```

### Range Else

Execute alternative content if collection is empty:

```gotemplate
{{range .Items}}
  <li>{{.}}</li>
{{else}}
  <li>No items available</li>
{{end}}
```

### Nested Ranges

```gotemplate
{{range .Categories}}
  <h2>{{.Name}}</h2>
  <ul>
  {{range .Items}}
    <li>{{.}}</li>
  {{end}}
  </ul>
{{end}}
```

### Breaking Out of Range

Use `break` to exit loop early:

```gotemplate
{{range .Items}}
  {{if eq . "stop"}}
    {{break}}
  {{end}}
  {{.}}
{{end}}
```

### Skipping Iterations

Use `continue` to skip to next iteration:

```gotemplate
{{range .Items}}
  {{if eq . "skip"}}
    {{continue}}
  {{end}}
  {{.}}
{{end}}
```

### Break and Continue Example

```gotemplate
{{range $i, $item := .Items}}
  {{if gt $i 10}}
    {{break}}  <!-- Stop after 10 items -->
  {{end}}
  {{if eq $item "hidden"}}
    {{continue}}  <!-- Skip hidden items -->
  {{end}}
  <div>{{$item}}</div>
{{end}}
```

## With Action

Change the dot context to simplify access to nested data.

### Basic With

```gotemplate
{{with .User}}
  Name: {{.Name}}
  Age: {{.Age}}
  Email: {{.Email}}
{{end}}
```

Equivalent to:

```gotemplate
{{if .User}}
  Name: {{.User.Name}}
  Age: {{.User.Age}}
  Email: {{.User.Email}}
{{end}}
```

### With/Else

Execute else block if value is falsy:

```gotemplate
{{with .User}}
  <div class="user">
    <h3>{{.Name}}</h3>
    <p>{{.Email}}</p>
  </div>
{{else}}
  <p>No user logged in</p>
{{end}}
```

### Nested With

```gotemplate
{{with .User}}
  {{with .Address}}
    {{.Street}}, {{.City}}
  {{end}}
{{end}}
```

### With in Range

```gotemplate
{{range .Users}}
  {{with .Profile}}
    {{.Name}} - {{.Bio}}
  {{end}}
{{end}}
```

## Combining Control Flow

Real templates often combine multiple constructs.

### Example 1: Filtered List

```gotemplate
<h2>Active Users</h2>
<ul>
{{range .Users}}
  {{if .IsActive}}
    <li>{{.Name}} ({{.Age}})</li>
  {{end}}
{{end}}
</ul>
```

### Example 2: Grouped Display

```gotemplate
{{range $category, $items := .GroupedItems}}
  <h3>{{$category}}</h3>
  {{if $items}}
    <ul>
    {{range $items}}
      <li>{{.}}</li>
    {{end}}
    </ul>
  {{else}}
    <p>No items in this category</p>
  {{end}}
{{end}}
```

### Example 3: Paginated List

```gotemplate
{{range $i, $item := .Items}}
  {{if and (gte $i .StartIndex) (lt $i .EndIndex)}}
    <div class="item">{{$item}}</div>
  {{end}}
{{end}}

{{if gt (len .Items) .EndIndex}}
  <a href="?page={{add .Page 1}}">Next Page</a>
{{end}}
```

### Example 4: Form with Validation

```gotemplate
<form>
  {{with .User}}
    <div class="form-group">
      <label>Name:</label>
      <input type="text" name="name" value="{{.Name}}" />
      {{if .NameError}}
        <span class="error">{{.NameError}}</span>
      {{end}}
    </div>
    
    <div class="form-group">
      <label>Email:</label>
      <input type="email" name="email" value="{{.Email}}" />
      {{if .EmailError}}
        <span class="error">{{.EmailError}}</span>
      {{end}}
    </div>
  {{end}}
  
  <button type="submit">Save</button>
</form>
```

## Common Patterns

### Pattern 1: Default Value

```gotemplate
{{if .Title}}{{.Title}}{{else}}Default Title{{end}}
```

Or using pipeline:

```gotemplate
{{.Title | default "Default Title"}}
```

### Pattern 2: Alternating Styles

```gotemplate
{{range $i, $item := .Items}}
  <div class="{{if mod $i 2 | eq 0}}even{{else}}odd{{end}}">
    {{$item}}
  </div>
{{end}}
```

### Pattern 3: Comma-Separated List

```gotemplate
{{range $i, $item := .Items}}
  {{if gt $i 0}}, {{end}}{{$item}}
{{end}}
```

Output: `item1, item2, item3`

### Pattern 4: First/Last Item

```gotemplate
{{range $i, $item := .Items}}
  {{if eq $i 0}}First: {{end}}
  {{$item}}
  {{if eq $i (sub (len $.Items) 1)}} (Last){{end}}
{{end}}
```

Note: Use `$` to access root context inside range.

### Pattern 5: Conditional Wrapper

```gotemplate
{{if .ShowContainer}}
  <div class="container">
{{end}}

Content here

{{if .ShowContainer}}
  </div>
{{end}}
```

### Pattern 6: Status Badge

```gotemplate
{{if eq .Status "active"}}
  <span class="badge badge-success">Active</span>
{{else if eq .Status "pending"}}
  <span class="badge badge-warning">Pending</span>
{{else}}
  <span class="badge badge-danger">Inactive</span>
{{end}}
```

### Pattern 7: Empty State

```gotemplate
{{if .Items}}
  <table>
  {{range .Items}}
    <tr><td>{{.}}</td></tr>
  {{end}}
  </table>
{{else}}
  <div class="empty-state">
    <p>No items found</p>
    <a href="/create">Create one</a>
  </div>
{{end}}
```

## Best Practices

### 1. Keep Logic Simple

Move complex logic to Java code.

❌ **Bad:**
```gotemplate
{{if and (or (and .A .B) (and .C .D)) (not (or .E .F))}}...{{end}}
```

✅ **Good:**
```java
data.put("ShouldDisplay", calculateCondition());
```
```gotemplate
{{if .ShouldDisplay}}...{{end}}
```

### 2. Avoid Deep Nesting

Keep nesting levels shallow for readability.

❌ **Bad:**
```gotemplate
{{if .A}}
  {{if .B}}
    {{if .C}}
      {{if .D}}
        ...
      {{end}}
    {{end}}
  {{end}}
{{end}}
```

✅ **Good:**
```java
data.put("IsValid", a && b && c && d);
```
```gotemplate
{{if .IsValid}}...{{end}}
```

### 3. Handle Empty Collections

Always consider empty state.

```gotemplate
{{if .Items}}
  {{range .Items}}...{{end}}
{{else}}
  <p>No items</p>
{{end}}
```

### 4. Use Meaningful Variable Names

```gotemplate
{{range $user := .Users}}
  {{$user.Name}}
{{end}}
```

Instead of:

```gotemplate
{{range $x := .Users}}
  {{$x.Name}}
{{end}}
```

### 5. Comment Complex Logic

```gotemplate
{{/* Show only active premium users who haven't expired */}}
{{range .Users}}
  {{if and .IsActive .IsPremium (not .IsExpired)}}
    {{.Name}}
  {{end}}
{{end}}
```

## Performance Tips

### 1. Minimize Conditionals in Loops

Pre-filter data in Java when possible.

❌ **Bad:**
```gotemplate
{{range .AllItems}}
  {{if .IsActive}}
    {{.}}
  {{end}}
{{end}}
```

✅ **Good:**
```java
data.put("ActiveItems", items.stream()
    .filter(Item::isActive)
    .collect(Collectors.toList()));
```
```gotemplate
{{range .ActiveItems}}{{.}}{{end}}
```

### 2. Avoid Redundant Checks

❌ **Bad:**
```gotemplate
{{if .User}}
  {{if .User.Name}}
    {{.User.Name}}
  {{end}}
{{end}}
```

✅ **Good:**
```gotemplate
{{with .User}}
  {{if .Name}}{{.Name}}{{end}}
{{end}}
```

### 3. Use Break Early

Exit loops as soon as possible:

```gotemplate
{{range .Items}}
  {{if eq . "target"}}
    Found it!
    {{break}}
  {{end}}
{{end}}
```

## Troubleshooting

### Problem: Condition Always False

**Issue:** Condition never evaluates to true.

**Solution:** 
1. Check truthiness rules (0, "", null are false)
2. Verify data is being passed correctly
3. Use `typeof` to check value type

```gotemplate
Value: {{.Value}}, Type: {{typeof .Value}}
```

### Problem: Range Not Iterating

**Issue:** Range block doesn't execute.

**Solution:**
1. Check if collection is empty or null
2. Verify collection type (must be array, list, map, or integer)

```gotemplate
{{if .Items}}
  Length: {{len .Items}}
  {{range .Items}}...{{end}}
{{else}}
  No items or items is null
{{end}}
```

### Problem: Wrong Context in Range

**Issue:** Can't access root data inside range.

**Solution:** Use `$` to access root context:

```gotemplate
{{range .Items}}
  Item: {{.}}, Total: {{len $.Items}}
{{end}}
```

### Problem: Infinite Loop

**Issue:** Template execution hangs.

**Solution:** 
1. Check for logical errors in break/continue
2. Verify range collection is finite
3. Look for recursive template calls

## Complete Example

```gotemplate
{{/* User Dashboard Template */}}
<html>
<head><title>{{.Title}}</title></head>
<body>
  <h1>Welcome, {{.User.Name}}</h1>
  
  {{/* Notifications */}}
  {{if .Notifications}}
    <div class="notifications">
      <h2>Notifications ({{len .Notifications}})</h2>
      {{range $i, $note := .Notifications}}
        {{if lt $i 5}}  {{/* Show only first 5 */}}
          <div class="notification {{if not $note.Read}}unread{{end}}">
            {{$note.Message}}
            {{if $note.Link}}
              <a href="{{$note.Link}}">View</a>
            {{end}}
          </div>
        {{end}}
      {{end}}
      {{if gt (len .Notifications) 5}}
        <a href="/notifications">See all</a>
      {{end}}
    </div>
  {{else}}
    <p>No new notifications</p>
  {{end}}
  
  {{/* Recent Activity */}}
  <h2>Recent Activity</h2>
  {{with .Activity}}
    {{if .}}
      <ul>
      {{range .}}
        <li>{{.Description}} ({{.Timestamp}})</li>
      {{end}}
      </ul>
    {{else}}
      <p>No recent activity</p>
    {{end}}
  {{end}}
  
  {{/* Quick Stats */}}
  <div class="stats">
    <div>Orders: {{.Stats.OrderCount}}</div>
    <div>Revenue: ${{printf "%.2f" .Stats.Revenue}}</div>
    <div>Rating: {{.Stats.Rating}}/5</div>
  </div>
</body>
</html>
```

---

**Next Steps:**
- 📚 Learn about [Template Sets](template-sets.md) for reusable components
- 🔧 Explore [Functions](functions.md) for data manipulation
- 📊 Understand [Data Models](data-models.md) for working with Java objects
