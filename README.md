# Golang Template Engine for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[中文文档](./README_zh.md)

A Go template engine implementation for Java that evaluates Go templates and generates textual output.

> **Purpose**: This project was created not to replace Go templates, but to fill a gap in the Java ecosystem where Go template compatibility was needed. It enables Java applications to leverage Go's powerful template syntax when working with Go-based systems.

> ✅ **Production Ready**: This project has reached production-ready status with stable core functionality, 80%+ test coverage, and comprehensive built-in function support.

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
    <version>0.4.0</version>
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


