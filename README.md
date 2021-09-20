# Go Template for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

For now, it‘s just a hand-write parser simply translated from Go. Introducing a Parser Generator is a fine way to do the
template work, this is comming soon.

## How To Use?

This repo has not been published to maven central yet, you may clone this repo or download the code directly then introduce in to you project.

### Use data object

```
Data data = new Data();
data.setGreeting("Good day!");

// You can also use java style name, the execution will check golang style name first.
data.put("greeting", "Good day!");

GoTemplate goTemplate = new GoTemplate("{{.Greeting}}");
String text = goTemplate.exec(data);
System.out.println(text); // text = "Good day!"
```

### Use data map
You can use data map either

```
Map<String, Object> data = new HashMap();
data.put("Greeting", "Good day!");

// You can also use java style name, the execution will check golang style name first.
data.put("greeting", "Good day!");

GoTemplate goTemplate = new GoTemplate("{{.Greeting}}");
String text = goTemplate.exec(data);
System.out.println(text); // text = "Good day!"
```
