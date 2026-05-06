# Golang Template Engine for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[中文文档](./README_zh.md)

A Go template engine implementation for Java that evaluates Go templates and generates textual output.

> **Purpose**: This project was created not to replace Go templates, but to fill a gap in the Java ecosystem where Go template compatibility was needed. It enables Java applications to leverage Go's powerful template syntax when working with Go-based systems.

> ✅ **Production Ready**: This project has reached production-ready status with stable core functionality, enforced 90%+ instruction coverage, and comprehensive built-in function support.

## Quick Start

### Requirements

- Java Version: >= **1.8**
- No additional dependencies required (pure Java)

### Installation

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.5.0</version>
</dependency>
```

### Basic Usage

```java
// Create a user as the input data
User user = new User();
user.setName("Bob");

// Prepare your template
Template template = new Template("demo");
template.parse("Hello, {{ .Name }}!");

// Execute and print out the result text
StringWriter writer = new StringWriter();
template.execute(writer, user);
System.out.print(writer.toString());  // "Hello Bob!"
```

## Go Compatibility Scope

v0.5.0 is the compatibility-audit release for core Go `text/template` behavior. The following areas are covered by focused tests:

- Control flow: `if`, `else`, `else if`, `range`, `range ... else`, `with`, `else with`, `break`, and `continue`
- Template definitions, override order, and `template "name"` execution with omitted pipeline data
- Pipeline variables, declaration with `:=`, assignment with `=`, parenthesized pipeline arguments, and branch/range scoping
- Built-in functions including comparison, logical, collection, formatting, escaping, and `call`
- Execution error handling, including function failures and writer `IOException` propagation

## Java-Specific Behavior

Some behavior intentionally maps Go template concepts onto Java:

- Java getters and public fields are exposed through field-chain access.
- Public no-argument methods can be accessed in field chains, such as enum `name` and `ordinal`.
- Java `Optional` values are unwrapped automatically.
- Null and missing values are falsey and print as empty output.
- Java `Map` iteration follows the map implementation order; keys are not sorted by the engine.
- `default`, `deepEqual`, `typeof`, and `kindOf` are gotemplate4j extensions, not Go predefined functions.

## Unsupported Go APIs in v0.5.0

The following Go APIs or semantics are intentionally deferred:

- `Option("missingkey=default/zero/error")`
- `ParseFiles`, `ParseGlob`, and `ParseFS`
- Template introspection APIs such as `Lookup`, `DefinedTemplates`, `Templates`, `Name`, and associated `New`
- `range` over channels, Go iterators, or integer sequences
- General Java method calls with arguments from templates
- Go function-valued fields or map entries for `call`; gotemplate4j `call` accepts `Function` instances

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for detailed guidelines.

## Roadmap

See [PLAN.md](./PLAN.md) for the detailed development roadmap.

## References

- [Go text/template Documentation](https://pkg.go.dev/text/template)
- [Go template Source Code](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine (Alternative)](https://github.com/proninyaroslav/java-template-engine)
- [Changelog](./CHANGELOG)
- [Development Plan](./PLAN.md)

