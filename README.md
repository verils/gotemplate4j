# Golang Template Engine for Java

[![Test and Verify](https://github.com/verils/gotemplate4j/actions/workflows/test.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/test.yml)

[中文文档](./README_zh.md)

A Go template engine implementation for Java that evaluates Go templates and generates textual output.

> ✅ **Production Ready**: Stable core functionality with 90%+ code coverage, comprehensive built-in functions, and complete documentation.

> ⚠️ **Core Purpose**: This project is **NOT** a replacement for Go's native `text/template` and does **NOT** aim to surpass it in performance or features. It exists solely to help **Java developers who must work with Go templates** meet basic operational needs when dealing with Go-based systems or migrating from Go to Java.

## Quick Start

### Requirements

- Java Version: >= **1.8** (Java 11+ required from v0.10.0)
- No additional dependencies required (pure Java)

> ⚠️ **Upcoming Change**: Starting from version 0.10.0, gotemplate4j will require **Java 11 or higher**. See [Future Roadmap](./PLAN.md#future-roadmap-java-11-migration-v0100) for details.

### Installation

#### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.8.0</version>
</dependency>
```

#### Gradle

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'io.github.verils:gotemplate4j:0.8.0'
}
```

For Gradle Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("io.github.verils:gotemplate4j:0.8.0")
}
```

For more installation options, see the [Installation Guide](./docs/getting-started/installation.md).

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

## Documentation

Comprehensive documentation is available in the `docs/` directory:

### 🚀 Getting Started
- [Installation Guide](./docs/getting-started/installation.md) - Add gotemplate4j to your project
- [Quick Start Tutorial](./docs/getting-started/quick-start.md) - Create your first template in 5 minutes
- [Basic Concepts](./docs/getting-started/basic-concepts.md) - Understand core concepts

### 📖 User Guide
- [Template Syntax Reference](./docs/user-guide/template-syntax.md) - Complete syntax guide
- [Working with Java Data](./docs/user-guide/data-models.md) - JavaBeans, Maps, Lists, Enums
- [Built-in and Custom Functions](./docs/user-guide/functions.md) - Function reference
- [Control Flow](./docs/user-guide/control-flow.md) - If, range, with, break/continue
- [Template Sets and Inheritance](./docs/user-guide/template-sets.md) - Define, template, blocks
- [Error Handling](./docs/user-guide/error-handling.md) - Handle errors gracefully

### 🔧 Advanced Topics
- [Go Template Compatibility](./docs/advanced/compatibility.md) - Detailed compatibility guide
- [Migration from Go Templates](./docs/advanced/migration.md) - Step-by-step migration
- [Performance Tuning](./docs/advanced/performance.md) - Optimize template execution
- [Security Best Practices](./docs/advanced/security.md) - Security considerations
- [Design Patterns](./docs/advanced/best-practices.md) - Best practices and patterns

### 📚 API Reference
- [Template API](./docs/api-reference/template-api.md) - Template class reference
- [Function API](./docs/api-reference/function-api.md) - Function interface guide
- [Exception API](./docs/api-reference/exception-api.md) - Exception hierarchy

### 💡 Examples
- [Basic Examples](./docs/examples/basic-examples.md) - Simple use cases
- [Web Templates](./docs/examples/web-templates.md) - HTML generation patterns
- [Email Templates](./docs/examples/email-templates.md) - Email generation examples
- [Complex Scenarios](./docs/examples/complex-scenarios.md) - Advanced real-world scenarios

### ❓ FAQ
- [Frequently Asked Questions](./docs/faq.md) - Common questions and answers

**Start here:** [Documentation Hub](./docs/index.md)

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

## References

- [Go text/template Documentation](https://pkg.go.dev/text/template)
- [Go template Source Code](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine (Alternative)](https://github.com/proninyaroslav/java-template-engine)
- [Changelog](./CHANGELOG)
- [Development Plan](./PLAN.md)

## Roadmap

See [PLAN.md](./PLAN.md) for the detailed development roadmap.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for detailed guidelines.
