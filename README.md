# Golang Template Engine for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

Evaluate go templates and make textual output.

Before then, there is another fine library with a name not so intuitive: [Java Template Engine](https://github.com/proninyaroslav/java-template-engine), you can choose that if you like.

This project is for experimental purpose, please **DON'T USE IN PRODUCTION**, for just now. Wish I can complete it ASAP.


## Requirements

Java Version: >=**1.8**

## Installation

For Maven, you can simply add dependency:

```xml

<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.2.0</version>
</dependency>
```

## User Guide

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

## Waht is Next?

- [ ] Support complex number format
- [ ] Support standard *built-in* functions in Golang
- [ ] Use JavaCC to deal with the syntax
