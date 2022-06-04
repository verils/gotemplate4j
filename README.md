# Go Template for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

For now, it‘s just a hand-write parser simply translated from Go. Introducing a Parser Generator is a fine way to do the
template work, this is comming soon.

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
// Prepare your data. For Golang it uses UpperCamelCase naming, but this is in Java,
// you can use any kind of naming that you want.
Map<String, Object> data = new HashMap<>();
data.put("Name", "World");

// Create a template factory, you can make it singleton, but it is better to use individually in each context
GoTemplateFactory goTemplateFactory = new GoTemplateFactory();

// Parse your template
goTemplateFactory.parse("example", "Hello {{.Name}}!");

// Get template from factory
GoTemplate goTemplate = goTemplateFactory.getTemplate("example");

// Execute and output
StringWriter writer = new OutputStreamWriter(System.out);
goTemplate.execute(data, writer);

// Then it prints "Hello World!" on console
```
