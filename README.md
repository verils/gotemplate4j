# Golang Template Engine for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[中文文档](./README_zh.md)

A Go template engine implementation for Java that evaluates Go templates and generates textual output.

> ⚠️ **Experimental Status**: This project is currently experimental. Core functionality is stable with 80%+ test coverage, but performance optimizations and complete documentation are still in progress.

## Quick Start

### Installation

Add the dependency to your Maven project:

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.4.0-SNAPSHOT</version>
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

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for detailed guidelines.

### Quick Start for Contributors

```bash
# Clone the repository
git clone https://github.com/verils/gotemplate4j.git
cd gotemplate4j

# Build and test
./mvnw clean install
```

For more information on development setup, coding guidelines, and priority areas, see [CONTRIBUTING.md](./CONTRIBUTING.md).

## Current Status

### ✅ Completed Features (v0.4.0)
- All 18+ Go built-in functions implemented (`eq`, `ne`, `lt`, `le`, `gt`, `ge`, `and`, `or`, `len`, `index`, `slice`, `call`, `html`, `js`, `urlquery`, `deepEqual`, `typeof`, `kindOf`)
- Complete pipeline support with variable assignment
- Enhanced error diagnostics with line/column information and context snippets
- Test coverage: 80% instruction, 79% branch (JaCoCo measured)
- Complex number parsing and formatting
- Comprehensive Javadoc for public API classes
- CONTRIBUTING.md guide for developers

### 🚧 In Progress
- Performance optimizations (caching strategies)
- Comprehensive Javadoc documentation
- Advanced template features (custom delimiters, enhanced whitespace control)

## Roadmap

See [docs/PLAN.md](./docs/PLAN.md) for the detailed development roadmap.

**Upcoming Milestones:**
- v0.5.0: Performance optimizations and caching
- v0.6.0: Complete documentation and production readiness
- v1.0.0: Stable release

## References

- [Go text/template Documentation](https://pkg.go.dev/text/template)
- [Go template Source Code](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine (Alternative)](https://github.com/proninyaroslav/java-template-engine)
- [Changelog](./CHANGELOG)
- [Development Plan](./docs/PLAN.md)


