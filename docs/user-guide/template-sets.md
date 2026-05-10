# Template Sets and Inheritance

This guide explains how to work with multiple templates, template inheritance, and reusable components.

## Overview

gotemplate4j supports:
- **Named Templates**: Define reusable template blocks
- **Template Invocation**: Call templates from other templates
- **Blocks**: Define and execute inline with override capability
- **Template Inheritance**: Build complex layouts from base templates

## Defining Templates

Use `define` to create named templates.

### Basic Definition

```gotemplate
{{define "header"}}
<html>
<head>
  <title>{{.Title}}</title>
</head>
<body>
{{end}}
```

Defined templates don't produce output until invoked.

### Multiple Definitions

A single template can contain multiple definitions:

```gotemplate
{{define "header"}}
<header>
  <h1>{{.SiteName}}</h1>
  <nav>
    {{range .MenuItems}}
      <a href="{{.URL}}">{{.Label}}</a>
    {{end}}
  </nav>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>&copy; {{.Year}} {{.CompanyName}}</p>
</footer>
{{end}}

{{define "sidebar"}}
<aside>
  <h3>Categories</h3>
  <ul>
  {{range .Categories}}
    <li><a href="{{.URL}}">{{.Name}}</a></li>
  {{end}}
  </ul>
</aside>
{{end}}
```

## Invoking Templates

Use `template` action to invoke defined templates.

### Basic Invocation

```gotemplate
{{template "header" .}}
<main>Content here</main>
{{template "footer" .}}
```

The second argument passes data context to the invoked template.

### Invocation Without Context

```gotemplate
{{template "header"}}
```

Uses current context (dot).

### Invocation With Different Context

```gotemplate
{{template "user-card" .User}}
{{template "product-list" .Products}}
```

Pass specific data to the template.

### Complete Example

```gotemplate
{{define "page"}}
<!DOCTYPE html>
<html>
<head>
  <title>{{.Title}}</title>
</head>
<body>
  {{template "header" .}}
  
  <main>
    {{template "content" .}}
  </main>
  
  {{template "footer" .}}
</body>
</html>
{{end}}

{{define "header"}}
<header>
  <h1>{{.SiteName}}</h1>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>Copyright {{.Year}}</p>
</footer>
{{end}}

{{define "content"}}
<p>Main content goes here</p>
{{end}}

<!-- Execute the page template -->
{{template "page" .}}
```

## Block Action

The `block` action defines and executes a template inline. It's Go-compatible and allows later overrides.

### Basic Block

```gotemplate
{{block "content" .}}Default content{{end}}
```

Equivalent to:

```gotemplate
{{define "content"}}Default content{{end}}
{{template "content" .}}
```

### Block with Override

Later definitions override earlier ones:

```gotemplate
{{/* Base template */}}
{{block "title" .}}Default Title{{end}}

{{/* Later in template set */}}
{{define "title"}}Custom Title{{end}}

<!-- When executed, outputs: Custom Title -->
```

### Block in Layout

```gotemplate
{{/* layout.tmpl */}}
<html>
<head>
  <title>{{block "title" .}}My Site{{end}}</title>
</head>
<body>
  {{block "content" .}}Default content{{end}}
</body>
</html>

{{/* page.tmpl */}}
{{define "title"}}Home Page{{end}}

{{define "content"}}
<h1>Welcome!</h1>
<p>This is the home page.</p>
{{end}}
```

When you parse both templates and execute "content", it uses the overridden "title".

## Template Inheritance Pattern

Build complex pages using inheritance-like patterns.

### Base Layout

```gotemplate
{{define "base"}}
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>{{block "title" .}}Default Title{{end}}</title>
  {{block "styles" .}}
  <link rel="stylesheet" href="/css/main.css">
  {{end}}
</head>
<body>
  {{block "header" .}}
  <header>
    <h1>{{.SiteName}}</h1>
  </header>
  {{end}}
  
  <main>
    {{block "content" .}}Default content{{end}}
  </main>
  
  {{block "footer" .}}
  <footer>
    <p>&copy; {{.Year}}</p>
  </footer>
  {{end}}
  
  {{block "scripts" .}}
  <script src="/js/main.js"></script>
  {{end}}
</body>
</html>
{{end}}
```

### Home Page

```gotemplate
{{define "title"}}Home - My Site{{end}}

{{define "content"}}
<h2>Welcome to Our Site</h2>
<p>This is the home page content.</p>
{{end}}
```

### About Page

```gotemplate
{{define "title"}}About Us - My Site{{end}}

{{define "content"}}
<h2>About Us</h2>
<p>Learn more about our company.</p>
{{end}}

{{define "styles"}}
<link rel="stylesheet" href="/css/main.css">
<link rel="stylesheet" href="/css/about.css">
{{end}}
```

### Rendering Pages

```java
// Parse all templates
Template template = new Template("base");
template.parseFile(Paths.get("templates/base.tmpl"));
template.parseFile(Paths.get("templates/home.tmpl"));

// Execute
Map<String, Object> data = new HashMap<>();
data.put("SiteName", "My Site");
data.put("Year", 2026);

StringWriter writer = new StringWriter();
template.execute(writer, data);
```

## Parsing Multiple Templates

Load multiple template files into a single template set.

### From Files

```java
Template template = new Template("base");
template.parseFile(Paths.get("templates/layout.tmpl"));
template.parseFile(Paths.get("templates/home.tmpl"));
template.parseFile(Paths.get("templates/about.tmpl"));
```

### Using parseFiles

```java
Template template = new Template("base");
template.parseFiles(
    Paths.get("templates/layout.tmpl"),
    Paths.get("templates/home.tmpl"),
    Paths.get("templates/about.tmpl")
);
```

### Using parseGlob

Parse all matching files:

```java
Template template = new Template("base");
template.parseGlob("templates/*.tmpl");
```

Or with pattern:

```java
template.parseGlob("templates/pages/*.tmpl");
```

## Template Lookup

Check if a template exists in the set.

### hasTemplate

```java
if (template.hasTemplate("header")) {
    System.out.println("Header template exists");
}
```

### lookup

Get a template by name:

```java
Template headerTemplate = template.lookup("header");
if (headerTemplate != null) {
    // Use the template
}
```

### definedTemplates

List all defined templates:

```java
Set<String> names = template.definedTemplates();
for (String name : names) {
    System.out.println("Template: " + name);
}
```

### name

Get the primary template name:

```java
String primaryName = template.name();
```

## Executing Specific Templates

Execute a specific template from the set.

### By Name

```java
// Execute "home" template specifically
template.executeTemplate("home", writer, data);
```

### Default Behavior

If you call `execute()` without specifying a template, it executes the first parsed template.

## Nested Template Execution

Templates can invoke other templates recursively.

### Example

```gotemplate
{{define "page"}}
{{template "header" .}}
{{template "body" .}}
{{template "footer" .}}
{{end}}

{{define "body"}}
<article>
  {{template "article-content" .}}
  {{template "comments" .Comments}}
</article>
{{end}}

{{define "comments"}}
{{if .}}
  <h3>Comments</h3>
  {{range .}}
    {{template "comment" .}}
  {{end}}
{{end}}
{{end}}

{{define "comment"}}
<div class="comment">
  <strong>{{.Author}}</strong>
  <p>{{.Text}}</p>
</div>
{{end}}
```

## Context Management

Control what data is available in different templates.

### Passing Full Context

```gotemplate
{{template "partial" .}}
```

Partial receives all data.

### Passing Subset

```gotemplate
{{template "user-info" .User}}
```

Partial receives only user data.

### Creating New Context

```gotemplate
{{with .User}}
  {{template "profile" .}}
{{end}}
```

Profile template sees User as root context.

## Best Practices

### 1. Organize Templates Logically

Group related templates:

```
templates/
├── layout/
│   ├── base.tmpl
│   ├── header.tmpl
│   └── footer.tmpl
├── pages/
│   ├── home.tmpl
│   ├── about.tmpl
│   └── contact.tmpl
└── partials/
    ├── user-card.tmpl
    ├── product-item.tmpl
    └── navigation.tmpl
```

### 2. Use Descriptive Names

✅ **Good:**
```gotemplate
{{define "user-profile-card"}}...{{end}}
{{define "navigation-menu"}}...{{end}}
```

❌ **Bad:**
```gotemplate
{{define "tpl1"}}...{{end}}
{{define "partial"}}...{{end}}
```

### 3. Keep Templates Focused

Each template should do one thing:

```gotemplate
{{define "user-name"}}
  {{.FirstName}} {{.LastName}}
{{end}}

{{define "user-email"}}
  <a href="mailto:{{.Email}}">{{.Email}}</a>
{{end}}
```

### 4. Document Template Dependencies

```java
/**
 * Template: home-page
 * Requires: base layout, navigation, footer
 * Expects data: SiteName, Year, MenuItems, FeaturedProducts
 */
```

### 5. Use Blocks for Overridable Sections

```gotemplate
{{define "base"}}
<html>
<head>
  <title>{{block "title" .}}Default{{end}}</title>
  {{block "head-extra" .}}{{end}}  <!-- Empty by default -->
</head>
<body>
  {{block "content" .}}Default{{end}}
</body>
</html>
{{end}}
```

Pages can override `title` and `head-extra`.

## Common Patterns

### Pattern 1: Page Layout

```gotemplate
{{define "page"}}
<!DOCTYPE html>
<html>
<head>
  <title>{{.Title}}</title>
</head>
<body>
  {{template "navbar" .}}
  
  <div class="container">
    {{template "content" .}}
  </div>
  
  {{template "footer" .}}
</body>
</html>
{{end}}
```

### Pattern 2: Component Library

```gotemplate
{{define "button"}}
<button class="btn btn-{{.Style | default "default"}}">
  {{.Label}}
</button>
{{end}}

{{define "alert"}}
<div class="alert alert-{{.Type | default "info"}}">
  {{.Message}}
</div>
{{end}}

{{define "card"}}
<div class="card">
  {{if .Title}}
    <div class="card-header">{{.Title}}</div>
  {{end}}
  <div class="card-body">
    {{.Content}}
  </div>
</div>
{{end}}
```

Usage:

```gotemplate
{{template "button" (dict "Label" "Click Me" "Style" "primary")}}
{{template "alert" (dict "Type" "success" "Message" "Saved!")}}
```

Note: You may need a `dict` function to create maps in templates.

### Pattern 3: Conditional Sections

```gotemplate
{{define "sidebar"}}
{{if .ShowSidebar}}
  <aside>
    {{block "sidebar-content" .}}
      Default sidebar content
    {{end}}
  </aside>
{{end}}
{{end}}
```

### Pattern 4: Repeated Elements

```gotemplate
{{define "list-item"}}
<li class="item {{if .IsActive}}active{{end}}">
  {{.Name}}
</li>
{{end}}

<ul>
{{range .Items}}
  {{template "list-item" .}}
{{end}}
</ul>
```

## Advanced Topics

### Template Cloning

Clone a template set for thread-safe execution:

```java
Template clone = template.clone();
clone.execute(writer, data);
```

### Missing Templates

Handle missing template invocations gracefully:

```gotemplate
{{if hasTemplate "optional-partial"}}
  {{template "optional-partial" .}}
{{end}}
```

Note: `hasTemplate` may need to be implemented as a custom function.

### Recursive Templates

Templates can call themselves (use with caution):

```gotemplate
{{define "tree-node"}}
<li>
  {{.Name}}
  {{if .Children}}
    <ul>
    {{range .Children}}
      {{template "tree-node" .}}
    {{end}}
    </ul>
  {{end}}
</li>
{{end}}

<ul>
{{range .TreeRoots}}
  {{template "tree-node" .}}
{{end}}
</ul>
```

## Troubleshooting

### Problem: Template Not Found

**Error:** `template "xyz" not defined`

**Solution:**
1. Verify template is defined before use
2. Check spelling of template name
3. Ensure all files are parsed

```java
System.out.println(template.definedTemplates());
```

### Problem: Wrong Data in Template

**Issue:** Template shows unexpected data.

**Solution:** Check what context is passed:

```gotemplate
{{template "partial" .}}    <!-- Passes full context -->
{{template "partial" .User}} <!-- Passes only User -->
```

### Problem: Circular Reference

**Issue:** Infinite loop or stack overflow.

**Solution:** Avoid templates calling each other recursively without base case.

### Problem: Block Not Overridden

**Issue:** Block shows default content instead of override.

**Solution:** 
1. Ensure override is defined before execution
2. Check that both templates are in same template set
3. Verify template names match exactly

## Complete Example: Blog Template

```gotemplate
{{/* base.tmpl */}}
{{define "base"}}
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>{{block "title" .}}My Blog{{end}}</title>
  {{block "styles" .}}
  <link rel="stylesheet" href="/css/blog.css">
  {{end}}
</head>
<body>
  {{template "header" .}}
  
  <div class="container">
    <main>
      {{block "content" .}}Default content{{end}}
    </main>
    
    <aside>
      {{template "sidebar" .}}
    </aside>
  </div>
  
  {{template "footer" .}}
  
  {{block "scripts" .}}
  <script src="/js/blog.js"></script>
  {{end}}
</body>
</html>
{{end}}

{{define "header"}}
<header>
  <h1><a href="/">{{.BlogName}}</a></h1>
  <nav>
    <a href="/">Home</a>
    <a href="/about">About</a>
    <a href="/contact">Contact</a>
  </nav>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>&copy; {{.Year}} {{.BlogName}}</p>
</footer>
{{end}}

{{define "sidebar"}}
<div class="widget">
  <h3>Recent Posts</h3>
  <ul>
  {{range .RecentPosts}}
    <li><a href="{{.URL}}">{{.Title}}</a></li>
  {{end}}
  </ul>
</div>

<div class="widget">
  <h3>Categories</h3>
  <ul>
  {{range .Categories}}
    <li><a href="{{.URL}}">{{.Name}}</a></li>
  {{end}}
  </ul>
</div>
{{end}}

{{/* index.tmpl */}}
{{define "title"}}Home - {{.BlogName}}{{end}}

{{define "content"}}
<h2>Latest Posts</h2>

{{range .Posts}}
<article class="post-preview">
  <h3><a href="{{.URL}}">{{.Title}}</a></h3>
  <p class="meta">By {{.Author}} on {{.Date}}</p>
  <p>{{.Excerpt}}</p>
  <a href="{{.URL}}" class="read-more">Read more →</a>
</article>
{{end}}

{{if .HasMorePosts}}
  <a href="/page/2" class="older-posts">Older Posts</a>
{{end}}
{{end}}

{{/* post.tmpl */}}
{{define "title"}}{{.Post.Title}} - {{.BlogName}}{{end}}

{{define "styles"}}
<link rel="stylesheet" href="/css/blog.css">
<link rel="stylesheet" href="/css/post.css">
{{end}}

{{define "content"}}
<article class="full-post">
  <h1>{{.Post.Title}}</h1>
  <p class="meta">By {{.Post.Author}} on {{.Post.Date}}</p>
  
  <div class="content">
    {{.Post.Content}}
  </div>
  
  {{if .Post.Tags}}
    <div class="tags">
      Tags:
      {{range .Post.Tags}}
        <span class="tag">{{.}}</span>
      {{end}}
    </div>
  {{end}}
</article>

<section class="comments">
  <h3>Comments</h3>
  {{if .Post.Comments}}
    {{range .Post.Comments}}
      {{template "comment" .}}
    {{end}}
  {{else}}
    <p>No comments yet.</p>
  {{end}}
</section>
{{end}}

{{define "comment"}}
<div class="comment">
  <strong>{{.Author}}</strong>
  <span class="date">{{.Date}}</span>
  <p>{{.Text}}</p>
</div>
{{end}}
```

---

**Next Steps:**
- 🐛 Learn about [Error Handling](error-handling.md) for robust templates
- 🔧 Explore [Functions](functions.md) for creating helper functions like `dict`
- 📚 Review [Template Syntax](template-syntax.md) for complete reference
