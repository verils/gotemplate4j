# Go Template for Java

For now, it‘s just a hand-write parser simply translated from Go. Introducing a Parser Generator is a fine way to do the
template work, coming soon.

## How To Use?

The coding is unfinished yet...

```
Map<String, Object> data = new HashMap();
data.put("Greeting", "Good day!");

GoTemplate goTemplate = new GoTemplate("{{.Greeting}}");
String text = goTemplate.exec(data);
System.out.println(text); // text = "Good day!"
```

Or you can use object rather than map:

```
Data data = new Data();
data.setGreeting("Good day!");

GoTemplate goTemplate = new GoTemplate("{{.Greeting}}");
String text = goTemplate.exec(data);
System.out.println(text); // text = "Good day!"
```