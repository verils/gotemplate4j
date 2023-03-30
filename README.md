# Golang Template Engine for Java



[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

Evaluate go templates and make textual output.

Before then, there is another fine library with a name not so intuitive: [Java Template Engine](https://github.com/proninyaroslav/java-template-engine), you can choose that if you like.

This project is for experimental purpose, please **DON'T USE IN PRODUCTION** for now because the support for complex number and builtin functions is incomplete. Wish I can finish them ASAP.


## Requirements

Java Version: >= **1.8**

## Installation

For Maven, you can simply add dependency:

```xml

<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.2.1</version>
</dependency>
```

## Usage

```java
// Prepare your data. In Golang it uses UpperCamelCase naming method, in Java we should use camelCase.
Map<String, Object> data = new HashMap<>();
data.put("name", "World");

// Create a template factory, you can make it singleton, but it is better to use individually in each context
GoTemplateFactory goTemplateFactory = new GoTemplateFactory();

// Parse your template, don't forget the template name: "example"
goTemplateFactory.parse("example", "Hello {{.name}}!");

// Get "example" template from factory
GoTemplate goTemplate = goTemplateFactory.getTemplate("example");

// Execute and do output
StringWriter writer = new OutputStreamWriter(System.out);
goTemplate.execute(data, writer);

// Then it prints "Hello World!" on console
```


## Limitations

- No built-in support for **complex** number in Java. Golang supports complex as a basic data type
- The format of **print** functions in Java are different from Golang

## Waht is Next?

- [ ] Support complex number format
- [ ] Support standard *built-in* functions in Golang
