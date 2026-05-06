# Go Template Compatibility

gotemplate4j implements a Java 8-compatible subset of Go's `text/template` package. The goal is practical compatibility for Java applications that need to evaluate Go-style templates, while keeping Java-specific behavior explicit.

## Covered Core Behavior

- Actions and pipelines: text actions, field chains, variables, parenthesized pipeline arguments, and multi-command pipelines.
- Control flow: `if`, `else`, `else if`, `range`, `range ... else`, `with`, `else with`, `break`, and `continue`.
- Template sets: `define`, `template`, block-style overrides, repeated parsing, and execution of named templates.
- Built-in functions: comparison, logical, collection, formatting, escaping, `call`, and gotemplate4j introspection helpers.
- Errors: parse errors include source context, function runtime failures are wrapped in `TemplateExecutionException`, and writer `IOException` values propagate to callers.

## Java-Specific Behavior

- JavaBean getters and public fields are exposed through Go-style field-chain access.
- Public no-argument methods are available in field chains. Methods with arguments are intentionally not called from templates.
- `Optional` values are unwrapped automatically; empty optionals behave like `null`.
- Enum values render with `toString()` and expose public no-argument methods such as `name` and `ordinal`.
- `null` values are falsey and print as empty output.
- Java `Map` iteration follows the map implementation order. gotemplate4j does not sort basic keys like Go does.
- `default`, `deepEqual`, `typeof`, and `kindOf` are gotemplate4j extensions, not Go predefined functions.

## Missing Keys

The default behavior preserves v0.5.0 compatibility: missing map keys evaluate to `null` and print as empty output.

```java
Template template = new Template("demo");
template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

Supported policies:

- `DEFAULT`: missing map keys evaluate to `null`.
- `ZERO`: returns a Java zero-like value when the target type is knowable; missing map keys usually have no knowable value type, so they currently fall back to `null`.
- `ERROR`: throws `TemplateExecutionException` for missing map keys and missing field-chain segments after a `null` value.

The Go-style string entrypoint is also supported:

```java
template.option("missingkey=error");
```

## Unsupported or Deferred Go APIs

- `ParseFS` and Go filesystem abstractions.
- `range` over channels, `iter.Seq`, `iter.Seq2`, or integer sequences.
- General method calls with arguments from templates.
- Go function-valued fields or map entries for `call`; gotemplate4j `call` accepts `Function` instances.
- Full Go numeric and complex formatting parity.

## Java Alternatives

- Use `Template.parse(InputStream)` or `Template.parse(Reader)` for caller-managed IO.
- Use JavaBeans, public fields, maps, and enums as template data models.
- Use `Function` implementations for custom functions.
- Use `Template.name()`, `definedTemplates()`, `hasTemplate(String)`, `lookup(String)`, and `templates()` to inspect parsed template sets before execution.
