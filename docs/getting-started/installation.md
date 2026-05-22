# Installation

This guide shows you how to add gotemplate4j to your Java project.

## Requirements

- **Java Version**: Java 8 or later
- **Build Tool**: Maven, Gradle, or manual JAR inclusion
- **Dependencies**: None (pure Java implementation)

## Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.9.1</version>
</dependency>
```

## Gradle

Add the following to your `build.gradle`:

```groovy
dependencies {
    implementation 'io.github.verils:gotemplate4j:0.9.1'
}
```

For Gradle Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("io.github.verils:gotemplate4j:0.9.1")
}
```

## Manual Installation

1. Download the JAR file from [Maven Central](https://central.sonatype.com/artifact/io.github.verils/gotemplate4j)
2. Add the JAR to your project's classpath

### Download Links

- **JAR**: `gotemplate4j-0.9.1.jar`
- **Sources**: `gotemplate4j-0.9.1-sources.jar`
- **Javadoc**: `gotemplate4j-0.9.1-javadoc.jar`

## Verify Installation

Create a simple test to verify that gotemplate4j is correctly installed:

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;

public class InstallationTest {
    public static void main(String[] args) throws Exception {
        // Create a template
        Template template = new Template("test");
        template.parse("Hello, {{.Name}}!");
        
        // Prepare data
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("Name", "World");
        
        // Execute template
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        // Print result
        System.out.println(writer.toString());
        // Output: Hello, World!
    }
}
```

If this compiles and runs successfully, gotemplate4j is properly installed!

## Next Steps

- 📖 Read the [Quick Start Guide](quick-start.md) to learn the basics
- 💡 Explore [Basic Concepts](basic-concepts.md) to understand core features
- 🔍 Check out [Examples](../examples/basic-examples.md) for more use cases

## Troubleshooting

### Compilation Errors

If you get compilation errors:

1. Ensure Java 8+ is installed: `java -version`
2. Verify the dependency is correctly added to your build file
3. Run a clean build: `mvn clean compile` or `gradle clean build`

### Runtime Errors

If you get `ClassNotFoundException` or `NoClassDefFoundError`:

1. Verify the JAR is in your classpath
2. Check for dependency conflicts
3. Ensure you're using a compatible Java version

### Need Help?

- 📚 Browse the [Documentation Index](index.md)
- ❓ Check the [FAQ](../faq.md)
- 🐛 Report issues on [GitHub](https://github.com/verils/gotemplate4j/issues)
