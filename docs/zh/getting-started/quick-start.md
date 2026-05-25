# 快速开始指南

5 分钟快速上手 gotemplate4j！本指南将带你创建你的第一个模板。

## 前置条件

- 已安装 Java 11 或更高版本
- 一个已设置好的 Java 项目（Maven、Gradle 或原生 Java）
- 已将 gotemplate4j 添加到你的项目（参阅[安装](installation.md)）

## 第 1 步：创建一个简单模板

让我们从经典的 "Hello, World!" 示例开始。

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        // 第 1 步：创建模板
        Template template = new Template("greeting");
        template.parse("Hello, {{.Name}}!");
        
        // 第 2 步：准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        
        // 第 3 步：执行模板
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        // 第 4 步：获取结果
        String result = writer.toString();
        System.out.println(result);  // 输出：Hello, World!
    }
}
```

**关键概念：**
- `{{.Name}}` - 访问数据中的 "Name" 字段
- 点号（`.`）代表当前数据上下文
- 模板执行将变量替换为实际值

## 第 2 步：使用条件语句

使用 `if`/`else` 为模板添加逻辑：

```java
Template template = new Template("conditional");
template.parse(
    "{{if .IsActive}}" +
    "用户处于活跃状态" +
    "{{else}}" +
    "用户处于非活跃状态" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("IsActive", true);

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());  // 输出：用户处于活跃状态
```

## 第 3 步：迭代集合

使用 `range` 来循环遍历数组、列表或 Map：

```java
Template template = new Template("loop");
template.parse(
    "项目：\n" +
    "{{range .Items}}" +
    "- {{.}}\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Items", new String[]{"Apple", "Banana", "Cherry"});

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());
// 输出：
// 项目：
// - Apple
// - Banana
// - Cherry
```

## 第 4 步：使用内置函数

gotemplate4j 包含许多实用的函数：

```java
Template template = new Template("functions");
template.parse(
    "大写：{{upper .Text}}\n" +
    "长度：{{len .Items}}\n" +
    "格式化：{{printf \"%.2f\" .Price}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Text", "hello");
data.put("Items", new String[]{"a", "b", "c"});
data.put("Price", 19.99);

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());
// 输出：
// 大写：HELLO
// 长度：3
// 格式化：19.99
```

## 第 5 步：操作 Java 对象

模板可以无缝操作 JavaBean：

```java
// 定义一个 Java 类
public class User {
    private String name;
    private int age;
    
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
}

// 在模板中使用
Template template = new Template("user");
template.parse("姓名：{{.Name}}，年龄：{{.Age}}");

User user = new User("Alice", 30);

StringWriter writer = new StringWriter();
template.execute(writer, user);
System.out.println(writer.toString());  // 输出：姓名：Alice，年龄：30
```

## 完整示例

以下是一个结合了多种功能的更贴近实际的示例：

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class CompleteExample {
    public static void main(String[] args) throws Exception {
        // 包含多种功能的模板
        String templateText = 
            "报告：{{.Title}}\n" +
            "====================\n" +
            "\n" +
            "{{if .ShowSummary}}" +
            "摘要：{{.Summary}}\n" +
            "\n" +
            "{{end}}" +
            "项目（共 {{len .Items}} 项）：\n" +
            "{{range $index, $item := .Items}}" +
            "{{add $index 1}}. {{upper $item}}\n" +
            "{{end}}" +
            "\n" +
            "生成时间：{{.Timestamp}}";
        
        Template template = new Template("report");
        template.parse(templateText);
        
        // 准备复杂数据
        Map<String, Object> data = new HashMap<>();
        data.put("Title", "月度销售报告");
        data.put("ShowSummary", true);
        data.put("Summary", "销售额增长了 15%");
        data.put("Items", Arrays.asList("小工具", "精密设备", "特殊器械"));
        data.put("Timestamp", new Date().toString());
        
        // 执行
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        System.out.println(writer.toString());
    }
}
```

**输出：**
```
报告：月度销售报告
====================

摘要：销售额增长了 15%

项目（共 3 项）：
1. 小工具
2. 精密设备
3. 特殊器械

生成时间：Sun May 10 16:00:00 CST 2026
```

## 接下来呢？

现在你已经掌握了基础知识，可以探索以下主题：

- 📖 [基本概念](basic-concepts.md) - 深入了解核心概念
- 📚 [模板语法](../user-guide/template-syntax.md) - 完整语法参考
- 🔧 [函数](../user-guide/functions.md) - 所有内置函数
- 💡 [示例](../examples/basic-examples.md) - 更多实用示例

## 常见模式

### 从文件加载模板

```java
import java.nio.file.Paths;

Template template = new Template("mytemplate");
template.parseFile(Paths.get("templates/mytemplate.tmpl"));
```

### 使用自定义函数

```java
import io.github.verils.gotemplate.Function;

Map<String, Function> functions = new HashMap<>();
functions.put("double", args -> {
    int value = (Integer) args[0];
    return value * 2;
});

Template template = new Template("custom", functions);
template.parse("5 的两倍是 {{double 5}}");
```

### 错误处理

```java
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    System.err.println("模板错误：" + e.getMessage());
} catch (IOException e) {
    System.err.println("IO 错误：" + e.getMessage());
}
```

## 成功技巧

1. **从简单开始**：先掌握基本的变量替换，再逐步增加复杂度
2. **测试模板**：用不同的数据输入验证模板
3. **使用有意义的名称**：清晰地命名模板和变量
4. **处理错误**：始终处理 `TemplateException` 和 `IOException`
5. **复用模板**：使用 `define` 和 `template` 创建可复用的组件

## 需要帮助？

- 📚 浏览完整的[文档](../index.md)
- ❓ 查看[常见问题](../faq.md)
- 🐛 在 [GitHub](https://github.com/verils/gotemplate4j/issues) 上报告问题
