# Golang Template Engine for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[中文文档](./README_zh.md)

A Go template engine implementation for Java that evaluates Go templates and generates textual output.

> ⚠️ **Experimental Status**: This project is currently experimental. **DO NOT USE IN PRODUCTION** yet, as support for complex numbers and built-in functions is incomplete.

## Quick Start

### Installation

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.3.1</version>
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

## Requirements

- Java Version: >= **1.8**
- No additional dependencies required (pure Java)

## Current Limitations

- The format of **print** functions in Java differs from Golang
- Only a subset of built-in functions are implemented
- PipeNode processing has limited capabilities

## Roadmap

- [x] Support complex number format
- [ ] Support all *built-in* functions in Golang
- [ ] Complete PipeNode for all types of identifiers

See [docs/PLAN.md](./docs/PLAN.md) for the detailed development roadmap.

## References

- [Go text/template Documentation](https://pkg.go.dev/text/template)
- [Go template Source Code](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine (Alternative)](https://github.com/proninyaroslav/java-template-engine)
- [Changelog](./CHANGELOG)
- [Development Plan](./docs/PLAN.md)

## Contributing

Contributions are welcome! Here's how you can help:

### Priority Areas

1. **Built-in function implementations** - Help complete the remaining Go built-in functions
2. **Test case development** - Improve test coverage to >80%
3. **Documentation improvements** - Add Javadoc, examples, and guides
4. **Performance profiling** - Identify and optimize bottlenecks

### Development Setup

```bash
# Clone the repository
git clone https://github.com/verils/gotemplate4j.git
cd gotemplate4j

# Build the project
./mvnw clean install

# Run tests
./mvnw test
```

### Guidelines

- Java Version: >= 1.8
- Build Tool: Maven (use `./mvnw` wrapper)
- No additional dependencies except Vanilla Java
- Follow standard Java naming conventions
- Add unit tests for all new features
- Maintain backward compatibility

For detailed development plan, see [docs/PLAN.md](./docs/PLAN.md).
