# Go Template for Java

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

For now, it‘s just a hand-write parser simply translated from Go. Introducing a Parser Generator is a fine way to do the
template work, this is comming soon.

## How To Use?

This repo has not been published to maven central yet, you may clone this repo or download the code directly then introduce in to you project.

### Use data object

Class Data:
```
class Data {
    private String greeting;
    
    // Go style naming is supported, and it will be choose in prior, but we recommend using java style naming
    private String Greeting;
}
```

Usage example:
```java
GoTemplate goTemplate = new GoTemplate("greeting");
goTemplate.parse("{{.Greeting}}");

// Prapare data
Data data = new Data();
data.setGreeting("Good day!");

String text = goTemplate.execute(data);
System.out.println(text); // text = "Good day!"
```

### Use data map
You can use data map either

```
GoTemplate goTemplate = new GoTemplate("greeting");
goTemplate.parse("{{.Greeting}}");

// Prapare data
Map<String, Object> data = new HashMap();
data.put("Greeting", "Good day!");
// You can also use java style naming, it is recommended
data.put("greeting", "Good day!");

String text = goTemplate.execute(data);
System.out.println(text); // text = "Good day!"
```
