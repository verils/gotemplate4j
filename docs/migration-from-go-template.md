# Migration from Go text/template

This guide maps common Go `text/template` usage to gotemplate4j.

## Data Models

Go structs usually map to JavaBeans:

```java
public class User {
    public String getName() {
        return "Bob";
    }
}
```

Templates can access this with `{{.Name}}`. Public fields are also supported, and maps can be used when the data shape is dynamic.

Enums render through `toString()` and expose public no-argument methods:

```gotemplate
{{.Status.name}}
```

## Null and Missing Values

By default, `null` and missing map keys are falsey and print as empty output:

```gotemplate
{{if .Name}}{{.Name}}{{else}}anonymous{{end}}
```

For stricter production checks, configure:

```java
Template template = new Template("demo").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

`MissingKeyPolicy.ZERO` is available for Go-style naming, but Java often cannot infer a zero value for absent map entries.

## Map Iteration

Go templates sort maps with basic ordered key types. gotemplate4j follows Java `Map` iteration order instead. Use `LinkedHashMap` when output order matters.

## Custom Functions

Go template functions map to gotemplate4j `Function` instances:

```java
Map<String, Function> functions = new HashMap<>();
functions.put("upper", args -> String.valueOf(args[0]).toUpperCase());

Template template = new Template("demo", functions);
```

Function failures are wrapped in `TemplateExecutionException` during execution.

## Template Sets

Use `define` and `template` in template text as usual. After parsing, inspect the set with:

```java
template.name();
template.definedTemplates();
template.hasTemplate("header");
template.lookup("header");
template.templates();
```

`lookup` and `templates` return independent template copies so callers cannot mutate the original parsed set by accident.

## Go-Only APIs to Avoid

- `ParseFS`: use Java streams, readers, or caller-managed file loading.
- Channels, Go iterators, and integer `range`: convert data to Java arrays, collections, or maps first.
- Function-valued fields and map entries for `call`: expose a gotemplate4j `Function`.
- General method calls with arguments: move that logic into a `Function` or prepare the value before execution.
