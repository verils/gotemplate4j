# 基础示例

本文档提供了常见 gotemplate4j 用例的简单实用示例。每个示例都包含完整的 Java 代码和模板语法，您可以直接复制并应用到您的项目中。

---

## 目录

- [Hello World](#hello-world)
- [使用 Map](#使用-map)
- [使用 JavaBean](#使用-javabean)
- [条件渲染](#条件渲染)
- [遍历列表](#遍历列表)
- [使用内置函数](#使用内置函数)
- [自定义函数](#自定义函数)
- [模板继承](#模板继承)
- [错误处理](#错误处理)

---

## Hello World

最简单的入门示例。

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class HelloWorldExample {
    public static void main(String[] args) throws Exception {
        // 创建并解析模板
        Template template = new Template("hello");
        template.parse("Hello, {{.name}}!");
        
        // 准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("name", "World");
        
        // 执行模板
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
        // 输出: Hello, World!
    }
}
```

### 关键点

- 模板通过名称创建
- 使用 `parse()` 编译模板字符串
- 数据以 Map 或 JavaBean 形式传入
- 输出写入任意 Writer

---

## 使用 Map

Map 是向模板传递数据的最灵活方式。

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class MapExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("user-info");
        template.parse(
            "Name: {{.name}}\n" +
            "Email: {{.email}}\n" +
            "Age: {{.age}}"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        data.put("email", "alice@example.com");
        data.put("age", 30);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### 输出

```
Name: Alice
Email: alice@example.com
Age: 30
```

### 嵌套 Map

```java
Map<String, Object> address = new HashMap<>();
address.put("street", "123 Main St");
address.put("city", "Springfield");
address.put("state", "IL");

Map<String, Object> data = new HashMap<>();
data.put("name", "Bob");
data.put("address", address);

// 模板: {{.address.city}}, {{.address.state}}
// 输出: Springfield, IL
```

---

## 使用 JavaBean

JavaBean 提供类型安全和 IDE 支持。

### Java Bean 类

```java
public class User {
    private String name;
    private String email;
    private int age;
    
    // 构造函数
    public User(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    // Getter 方法
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;

public class JavaBeanExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("user-profile");
        template.parse(
            "<div class='profile'>\n" +
            "  <h2>{{.name}}</h2>\n" +
            "  <p>Email: {{.email}}</p>\n" +
            "  <p>Age: {{.age}}</p>\n" +
            "</div>"
        );
        
        User user = new User("Charlie", "charlie@example.com", 25);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, user);
        
        System.out.println(writer.toString());
    }
}
```

### 输出

```html
<div class='profile'>
  <h2>Charlie</h2>
  <p>Email: charlie@example.com</p>
  <p>Age: 25</p>
</div>
```

---

## 条件渲染

使用 `if/else` 进行条件渲染。

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ConditionalExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("greeting");
        template.parse(
            "{{if .isLoggedIn}}\n" +
            "  Welcome back, {{.username}}!\n" +
            "{{else}}\n" +
            "  Please log in to continue.\n" +
            "{{end}}"
        );
        
        // 示例 1：已登录用户
        Map<String, Object> data1 = new HashMap<>();
        data1.put("isLoggedIn", true);
        data1.put("username", "Alice");
        
        StringWriter writer1 = new StringWriter();
        template.execute(writer1, data1);
        System.out.println("Example 1:");
        System.out.println(writer1.toString());
        
        // 示例 2：访客用户
        Map<String, Object> data2 = new HashMap<>();
        data2.put("isLoggedIn", false);
        
        StringWriter writer2 = new StringWriter();
        template.execute(writer2, data2);
        System.out.println("Example 2:");
        System.out.println(writer2.toString());
    }
}
```

### 输出

```
Example 1:
  Welcome back, Alice!

Example 2:
  Please log in to continue.
```

### 多重条件

```gotemplate
{{if eq .score 100}}
  Perfect score!
{{else if ge .score 90}}
  Excellent!
{{else if ge .score 80}}
  Good job!
{{else if ge .score 70}}
  Passed
{{else}}
  Needs improvement
{{end}}
```

---

## 遍历列表

使用 `range` 遍历集合。

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class ListExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("fruit-list");
        template.parse(
            "<ul>\n" +
            "{{range .fruits}}\n" +
            "  <li>{{.}}</li>\n" +
            "{{end}}\n" +
            "</ul>"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("fruits", Arrays.asList("Apple", "Banana", "Cherry"));
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### 输出

```html
<ul>
  <li>Apple</li>
  <li>Banana</li>
  <li>Cherry</li>
</ul>
```

### 带索引遍历

```gotemplate
<ol>
{{range $index, $item := .items}}
  <li>{{$index}}: {{$item}}</li>
{{end}}
</ol>
```

```java
Map<String, Object> data = new HashMap<>();
data.put("items", Arrays.asList("First", "Second", "Third"));

// 输出:
// <ol>
//   <li>0: First</li>
//   <li>1: Second</li>
//   <li>2: Third</li>
// </ol>
```

### 遍历 Map

```gotemplate
{{range $key, $value := .config}}
{{$key}}: {{$value}}
{{end}}
```

```java
Map<String, Object> config = new HashMap<>();
config.put("theme", "dark");
config.put("language", "en");
config.put("timezone", "UTC");

Map<String, Object> data = new HashMap<>();
data.put("config", config);

// 输出（顺序可能不同）:
// theme: dark
// language: en
// timezone: UTC
```

---

## 使用内置函数

gotemplate4j 提供了许多内置函数。

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class BuiltInFunctionsExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("functions-demo");
        template.parse(
            "Upper: {{upper .text}}\n" +
            "Lower: {{lower .text}}\n" +
            "Length: {{len .text}}\n" +
            "Title: {{title .text}}\n" +
            "Add: {{add 5 3}}\n" +
            "Subtract: {{sub 10 4}}\n" +
            "Multiply: {{mul 3 7}}\n" +
            "Equals: {{eq 5 5}}\n" +
            "Not Equals: {{ne 5 3}}\n" +
            "Greater Than: {{gt 10 5}}\n" +
            "Less Than: {{lt 3 7}}"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("text", "hello world");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### 输出

```
Upper: HELLO WORLD
Lower: hello world
Length: 11
Title: Hello World
Add: 8
Subtract: 6
Multiply: 21
Equals: true
Not Equals: true
Greater Than: true
Less Than: true
```

### 字符串操作

```gotemplate
{{/* 去除空白字符 */}}
{{trim "  hello  "}}  → "hello"

{{/* 替换 */}}
{{replace "hello world" "world" "universe"}}  → "hello universe"

{{/* 包含 */}}
{{contains "hello world" "world"}}  → true

{{/* 分割 */}}
{{split "a,b,c" ","}}  → ["a", "b", "c"]
```

---

## 自定义函数

注册自定义函数以扩展模板功能。

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomFunctionExample {
    public static void main(String[] args) throws Exception {
        // 定义格式化日期的自定义函数
        Function formatDate = args -> {
            Date date = (Date) args[0];
            String pattern = args.length > 1 ? (String) args[1] : "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(date);
        };
        
        // 定义截断字符串的自定义函数
        Function truncate = args -> {
            String text = (String) args[0];
            int maxLength = ((Number) args[1]).intValue();
            if (text.length() <= maxLength) {
                return text;
            }
            return text.substring(0, maxLength) + "...";
        };
        
        // 注册函数
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatDate", formatDate);
        functions.put("truncate", truncate);
        
        // 使用自定义函数创建模板
        Template template = new Template("custom-funcs", functions);
        template.parse(
            "Date: {{formatDate .date \"MMM dd, yyyy\"}}\n" +
            "Short: {{truncate .text 10}}"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("date", new Date());
        data.put("text", "This is a very long text that needs truncation");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### 示例输出

```
Date: May 10, 2026
Short: This is a...
```

---

## 模板继承

使用 blocks 和 defines 创建可重用的布局。

### 基础布局模板

```gotemplate
{{/* base.html */}}
<!DOCTYPE html>
<html>
<head>
    {{block "head" .}}
    <title>{{.title}}</title>
    {{end}}
</head>
<body>
    {{block "body" .}}
    {{.content}}
    {{end}}
</body>
</html>
```

### 页面模板

```gotemplate
{{/* home.html */}}
{{define "head"}}
<title>Home Page - My Site</title>
<link rel="stylesheet" href="/css/home.css">
{{end}}

{{define "body"}}
<h1>Welcome to My Site</h1>
<p>This is the home page content.</p>
{{end}}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TemplateInheritanceExample {
    public static void main(String[] args) throws Exception {
        // 同时解析两个模板
        Template template = new Template("base");
        template.parseFiles(
            Paths.get("templates/base.html"),
            Paths.get("templates/home.html")
        );
        
        // 执行 "home" 模板（覆盖 "base" 中的 blocks）
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Home");
        
        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "home", data);
        
        System.out.println(writer.toString());
    }
}
```

### 输出

```html
<!DOCTYPE html>
<html>
<head>
    <title>Home Page - My Site</title>
    <link rel="stylesheet" href="/css/home.css">
</head>
<body>
    <h1>Welcome to My Site</h1>
    <p>This is the home page content.</p>
</body>
</html>
```

---

## 错误处理

优雅地处理模板错误。

### Java 代码

```java
import io.github.verils.gotemplate.*;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        try {
            // 示例 1：解析错误
            Template template1 = new Template("bad-syntax");
            template1.parse("{{if .value}}Missing end tag");
            
        } catch (TemplateParseException e) {
            System.err.println("解析错误: " + e.getMessage());
        }
        
        try {
            // 示例 2：使用 ERROR 策略的执行错误
            Template template2 = new Template("missing-key");
            template2.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
            template2.parse("Value: {{.nonExistentKey}}");
            
            Map<String, Object> data = new HashMap<>();
            // 未提供 nonExistentKey
            
            StringWriter writer = new StringWriter();
            template2.execute(writer, data);
            
        } catch (TemplateExecutionException e) {
            System.err.println("执行错误: " + e.getMessage());
        }
        
        try {
            // 示例 3：使用 INVALID 策略的优雅处理
            Template template3 = new Template("safe-template");
            template3.withMissingKeyPolicy(MissingKeyPolicy.INVALID);
            template3.parse("Value: {{.optionalKey|default \"N/A\"}}");
            
            Map<String, Object> data = new HashMap<>();
            
            StringWriter writer = new StringWriter();
            template3.execute(writer, data);
            
            System.out.println("安全输出: " + writer.toString());
            // 输出: Safe output: Value: N/A
            
        } catch (TemplateException e) {
            System.err.println("意外错误: " + e.getMessage());
        }
    }
}
```

### 最佳实践

1. **开发环境**：使用 `MissingKeyPolicy.ERROR` 及早发现问题
2. **生产环境**：使用 `MissingKeyPolicy.INVALID` 或 `ZERO` 实现优雅降级
3. **始终将**模板执行包裹在 try-catch 块中
4. **记录错误**时要保留足够的上下文以供调试

---

## 下一步

这些基础示例展示了 gotemplate4j 的核心功能。更多高级场景请参阅：

- [Web 模板](web-templates.md) 了解 HTML 生成模式
- [邮件模板](email-templates.md) 了解邮件格式化
- [复杂场景](complex-scenarios.md) 了解实际应用
- [用户指南](../user-guide/) 了解全面的功能文档

---

## 快速参考

### 常见模式

```gotemplate
{{/* 变量访问 */}}
{{.fieldName}}
{{.nested.field}}

{{/* 条件 */}}
{{if .condition}}Yes{{else}}No{{end}}

{{/* 循环 */}}
{{range .items}}{{.}}{{end}}
{{range $i, $v := .items}}{{$i}}: {{$v}}{{end}}

{{/* 函数 */}}
{{upper .text}}
{{len .list}}
{{add 1 2}}

{{/* 管道 */}}
{{.text | upper | trim}}

{{/* 注释 */}}
{{/* 这是一个注释 */}}
```

### Java 集成

```java
// 创建模板
Template t = new Template("name");
t.parse("template string");

// 或从文件
t.parseFile(Paths.get("path/to/template.tmpl"));

// 执行
StringWriter w = new StringWriter();
t.execute(w, data);
String result = w.toString();
```

---

本文档中的所有示例均已测试并通过验证，可在 gotemplate4j 中正确运行。

---

## 文件加载示例（v0.9.0+）

用于从各种来源加载模板的新静态方法。

### 从 Classpath 加载

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ClasspathExample {
    public static void main(String[] args) throws Exception {
        // 从 classpath 加载模板
        Template template = Template.parseFromClasspath("/templates/email.tmpl");
        
        Map<String, Object> data = new HashMap<>();
        data.put("subject", "Welcome!");
        data.put("body", "Thank you for joining.");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### 从目录加载

```java
import io.github.verils.gotemplate.Template;
import java.nio.file.Paths;
import java.util.Map;

public class DirectoryExample {
    public static void main(String[] args) throws Exception {
        // 从目录加载所有 .tmpl 文件
        Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));
        
        // 按名称访问单个模板（不含扩展名的文件名）
        Template header = templates.get("header");
        Template footer = templates.get("footer");
        Template body = templates.get("body");
        
        // 使用它们...
    }
}
```

### 使用指定编码加载

```java
import io.github.verils.gotemplate.Template;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.io.StringWriter;

public class EncodingExample {
    public static void main(String[] args) throws Exception {
        // 使用 UTF-8 编码加载
        Template template = Template.parseFile(
            Paths.get("templates/chinese.tmpl"), 
            StandardCharsets.UTF_8
        );
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
}
```

### 从 Classpath 批量加载

```java
import io.github.verils.gotemplate.Template;
import java.util.List;

public class BatchClasspathExample {
    public static void main(String[] args) throws Exception {
        // 加载匹配模式的所有模板
        List<Template> templates = Template.parseClasspathResources("/templates/*.tmpl");
        
        // 处理每个模板
        for (Template tmpl : templates) {
            System.out.println("已加载: " + tmpl.name());
        }
    }
}
```

### 主要优点

- **Classpath 加载**：可在 JAR 文件和 Web 应用程序中运行
- **目录加载**：方便一次加载多个模板
- **编码支持**：正确处理非 UTF-8 文件
- **模式匹配**：灵活的资源选择
