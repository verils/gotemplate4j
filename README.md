# Golang Template Engine for Java

[![Test and Verify](https://github.com/verils/gotemplate4j/actions/workflows/test.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/test.yml)

[中文文档](./README_zh.md)

A Go template engine implementation for Java that evaluates Go templates and generates textual output.

> ✅ **Production Ready**: Stable core functionality with 90%+ code coverage, comprehensive built-in functions, and complete documentation.

> ⚠️ **Core Purpose**: This project is **NOT** a replacement for Go's native `text/template` and does **NOT** aim to surpass it in performance or features. It exists solely to help **Java developers who must work with Go templates** meet basic operational needs when dealing with Go-based systems or migrating from Go to Java.

> 🆕 **Latest Release (v0.10.0)**: Java 11 baseline, compatibility polish, and improved diagnostics.

## Quick Start

### Requirements

- Java Version: >= **11**
- No additional dependencies required (pure Java)

> ⚠️ **Java 8 Support**: v0.9.x is the final release line supporting Java 8. The v0.10.0 development line requires **Java 11 or higher**. See [Development Plan](./PLAN.md) for details.

### Installation

#### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.10.0</version>
</dependency>
```

#### Gradle

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'io.github.verils:gotemplate4j:0.10.0'
}
```

For Gradle Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("io.github.verils:gotemplate4j:0.10.0")
}
```

For more installation options, see the [Installation Guide](./docs/en/getting-started/installation.md).

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

### Enhanced File Loading (v0.9.0+)

Load templates from classpath, directories, or with specific encoding:

```java
// Load from classpath
Template tmpl = Template.parseFromClasspath("templates/email.tmpl");

// Load from directory (all .tmpl files)
Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));

// Load with specific encoding
Template tmpl = Template.parseFile(Paths.get("template.tmpl"), StandardCharsets.UTF_8);

// Batch load from classpath with pattern
List<Template> templates = Template.parseClasspathResources("templates/*.tmpl");
```

For more examples, see [Basic Examples](./docs/en/examples/basic-examples.md).

## Documentation

Comprehensive documentation is available in the `docs/` directory:

### 🚀 Getting Started
- [Installation Guide](./docs/en/getting-started/installation.md) - Add gotemplate4j to your project
- [Quick Start Tutorial](./docs/en/getting-started/quick-start.md) - Create your first template in 5 minutes
- [Basic Concepts](./docs/en/getting-started/basic-concepts.md) - Understand core concepts

### 📖 User Guide
- [Template Syntax Reference](./docs/en/user-guide/template-syntax.md) - Complete syntax guide
- [Working with Java Data](./docs/en/user-guide/data-models.md) - JavaBeans, Maps, Lists, Enums
- [Built-in and Custom Functions](./docs/en/user-guide/functions.md) - Function reference
- [Control Flow](./docs/en/user-guide/control-flow.md) - If, range, with, break/continue
- [Template Sets and Inheritance](./docs/en/user-guide/template-sets.md) - Define, template, blocks
- [Error Handling](./docs/en/user-guide/error-handling.md) - Handle errors gracefully

### 🔧 Advanced Topics
- [Go Template Compatibility](./docs/en/advanced/compatibility.md) - Detailed compatibility guide
- [Migration from Go Templates](./docs/en/advanced/migration.md) - Step-by-step migration
- [Performance Tuning](./docs/en/advanced/performance.md) - Optimize template execution
- [Security Best Practices](./docs/en/advanced/security.md) - Security considerations
- [Design Patterns](./docs/en/advanced/best-practices.md) - Best practices and patterns

### 📚 API Reference
- [Template API](./docs/en/api-reference/template-api.md) - Template class reference
- [Function API](./docs/en/api-reference/function-api.md) - Function interface guide
- [Exception API](./docs/en/api-reference/exception-api.md) - Exception hierarchy

### 💡 Examples
- [Basic Examples](./docs/en/examples/basic-examples.md) - Simple use cases
- [Web Templates](./docs/en/examples/web-templates.md) - HTML generation patterns
- [Email Templates](./docs/en/examples/email-templates.md) - Email generation examples
- [Complex Scenarios](./docs/en/examples/complex-scenarios.md) - Advanced real-world scenarios

### ❓ FAQ
- [Frequently Asked Questions](./docs/en/faq.md) - Common questions and answers

**Start here:** [Documentation Hub](./docs/en/index.md)

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
