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
    <version>0.6.0</version>
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

## Compatibility and Migration

v0.6.0 moves detailed compatibility notes into focused docs:

- [Go Template Compatibility](./docs/go-template-compatibility.md)
- [Migration from Go text/template](./docs/migration-from-go-template.md)

The short version: gotemplate4j covers core Go `text/template` control flow, pipelines, template definitions, built-in functions, and execution errors. Java-specific behavior such as JavaBean access, public fields, `Optional`, enums, nulls, missing keys, and map iteration order is documented separately from Go compatibility claims.

## Performance

gotemplate4j is optimized for Java 8 environments. The following baseline metrics were measured on a standard development machine (10,000 iterations):

| Benchmark | Throughput (ops/sec) | Avg Time (ms/op) |
| :--- | :--- | :--- |
| **Parse** | ~78,000 | ~0.012 |
| **Execute** | ~262,000 | ~0.003 |
| **JavaBean Access** | ~165,000 | ~0.006 |
| **Map Access** | ~512,000 | ~0.001 |
| **Range (100 items)** | ~31,000 | ~0.032 |
| **Function Heavy** | ~474,000 | ~0.002 |

*Note: You can run the `TemplateBenchmark` class in the test suite to verify these numbers on your own hardware.*

### Test Environment
- **CPU**: Intel i7-10870H 8C16T
- **Memory**: 64 GB RAM
- **OS**: Windows 25H2
- **Java Version**: 1.8

## Roadmap

See [PLAN.md](./PLAN.md) for the detailed development roadmap.

## References

- [Go text/template Documentation](https://pkg.go.dev/text/template)
- [Go template Source Code](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine (Alternative)](https://github.com/proninyaroslav/java-template-engine)
- [Changelog](./CHANGELOG)
- [Development Plan](./PLAN.md)

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for detailed guidelines.
