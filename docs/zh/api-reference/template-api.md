# Template API 参考

`Template` 类是 Java 中解析和执行 Go 风格模板的主要入口。本文档记录了所有公共构造函数、方法和配置选项。

---

## 目录

- [构造函数](#构造函数)
- [解析方法](#解析方法)
- [执行方法](#执行方法)
- [模板自省](#模板自省)
- [配置方法](#配置方法)
- [文件系统辅助方法](#文件系统辅助方法)
- [线程安全](#线程安全)

---

## 构造函数

### `Template(String name)`

使用指定名称和默认分隔符（`{{` 和 `}}`）创建新模板。

```java
Template template = new Template("my-template");
```

**参数：**
- `name` - 模板名称，用于标识和模板引用

**抛出异常：**
- 如果 name 为 null 或空字符串，抛出 `IllegalArgumentException`

**Since:** 0.1.0

---

### `Template(String name, String leftDelimiter, String rightDelimiter)`

使用自定义分隔符创建新模板。

```java
// 使用 <% %> 替代 {{ }}
Template template = new Template("my-template", "<%", "%>");
template.parse("<% .Name %>");
```

**参数：**
- `name` - 模板名称
- `leftDelimiter` - 左分隔符（默认："{{"）
- `rightDelimiter` - 右分隔符（默认："}}"）

**抛出异常：**
- 如果 name 为 null 或空字符串，抛出 `IllegalArgumentException`

**Since:** 0.5.0

---

### `Template(String name, String leftDelimiter, String rightDelimiter, String leftComment, String rightComment)`

使用自定义分隔符和注释标记创建新模板。

```java
// 自定义分隔符和注释风格
Template template = new Template("my-template", "<%", "%>", "<!--", "-->");
template.parse("<% /* 这是一个注释 */ .Name %>");
```

**参数：**
- `name` - 模板名称
- `leftDelimiter` - 左分隔符
- `rightDelimiter` - 右分隔符
- `leftComment` - 左注释分隔符（默认："/*"）
- `rightComment` - 右注释分隔符（默认："*/"）

**抛出异常：**
- 如果 name 为 null 或空字符串，抛出 `IllegalArgumentException`

**Since:** 0.5.0

---

### `Template(String name, Map<String, Function> functions)`

使用自定义函数创建新模板。

```java
Map<String, Function> customFunctions = new HashMap<>();
customFunctions.put("upper", args -> ((String) args[0]).toUpperCase());

Template template = new Template("my-template", customFunctions);
template.parse("{{.Name | upper}}");
```

**参数：**
- `name` - 模板名称
- `functions` - 自定义函数映射，键为函数名，值为 Function 实现

**抛出异常：**
- 如果 name 为 null 或空字符串，抛出 `IllegalArgumentException`

**参见：**
- [Function 接口](function-api.md) 了解如何实现自定义函数

**Since:** 0.1.0

---

### `Template(String name, Map<String, Function> functions, String leftDelimiter, String rightDelimiter)`

使用自定义函数和分隔符创建新模板。

```java
Map<String, Function> customFunctions = new HashMap<>();
customFunctions.put("lower", args -> ((String) args[0]).toLowerCase());

Template template = new Template("my-template", customFunctions, "[[", "]]");
template.parse("[[.Name | lower]]");
```

**参数：**
- `name` - 模板名称
- `functions` - 自定义函数映射
- `leftDelimiter` - 左分隔符
- `rightDelimiter` - 右分隔符

**抛出异常：**
- 如果 name 为 null 或空字符串，抛出 `IllegalArgumentException`

**Since:** 0.5.0

---

### `Template(Template other)`

复制构造函数，创建现有模板的深拷贝。适用于线程安全的执行场景。

```java
// 创建并解析模板一次
Template baseTemplate = new Template("master");
baseTemplate.parse("{{.message}}");

// 在每个线程中使用前创建副本
Template threadSafe = new Template(baseTemplate);
StringWriter writer = new StringWriter();
threadSafe.execute(writer, data);
```

**参数：**
- `other` - 要复制的模板

**Since:** 0.5.0

---

## 解析方法

### `void parse(String template)`

将给定文本解析为模板主体。

```java
Template template = new Template("greeting");
template.parse("Hello, {{.Name}}!");
```

**参数：**
- `template` - 要解析的模板文本内容

**抛出异常：**
- 如果模板包含语法错误、未定义变量或其他解析问题，抛出 `TemplateParseException`

**模板定义示例：**

```java
template.parse(
    "{{define \"header\"}}Header: {{.Title}}{{end}}" +
    "{{template \"header\" .}}"
);
```

**Since:** 0.1.0

---

### `void parse(InputStream in)`

从 InputStream 解析模板内容，使用 UTF-8 编码。

```java
try (InputStream in = Files.newInputStream(Paths.get("template.tmpl"))) {
    template.parse(in);
}
```

**参数：**
- `in` - 提供模板内容的 InputStream

**抛出异常：**
- 如果解析失败，抛出 `TemplateParseException`
- 如果无法读取流，抛出 `IOException`

**Since:** 0.1.0

---

### `void parse(Reader reader)`

从 Reader 解析模板内容。

```java
try (Reader reader = new FileReader("template.tmpl")) {
    template.parse(reader);
}
```

**参数：**
- `reader` - 提供模板内容的 Reader

**抛出异常：**
- 如果解析失败，抛出 `TemplateParseException`
- 如果无法读取 Reader，抛出 `IOException`

**Since:** 0.1.0

---

## 执行方法

### `void execute(Writer writer, Object data)`

使用提供的数据执行模板，并将结果写入 Writer。

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "World");

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString()); // 输出: Hello, World!
```

**参数：**
- `writer` - 接收模板输出的 Writer
- `data` - 用于模板变量替换的数据对象

**抛出异常：**
- 如果模板执行失败，抛出 `TemplateException`
- 如果写入 Writer 失败，抛出 `IOException`
- 如果模板尚未解析，抛出 `TemplateNotFoundException`

**数据访问模式：**
- `{{.FieldName}}` - 调用数据对象的 `getFieldName()` 方法
- `{{.Field.SubField}}` - 链式调用 getter：`getField().getSubField()`
- `{{index .ArrayOrMap 0}}` - 访问数组/映射元素

**Since:** 0.1.0

---

### `void execute(OutputStream out, Object data)`

执行模板并将结果写入 OutputStream，使用 UTF-8 编码。

```java
ByteArrayOutputStream out = new ByteArrayOutputStream();
template.execute(out, data);
String result = out.toString("UTF-8");
```

**参数：**
- `out` - 用于写入模板输出的 OutputStream
- `data` - 数据对象

**抛出异常：**
- 如果执行失败，抛出 `TemplateException`
- 如果写入失败，抛出 `IOException`
- 如果未找到模板，抛出 `TemplateNotFoundException`

**Since:** 0.1.0

---

### `void executeTemplate(Writer writer, String name, Object data)`

使用提供的数据执行一个命名模板。

```java
// 解析多个模板定义
template.parse(
    "{{define \"header\"}}<h1>{{.Title}}</h1>{{end}}" +
    "{{define \"footer\"}}<footer>{{.Copyright}}</footer>{{end}}"
);

// 执行指定模板
StringWriter writer = new StringWriter();
template.executeTemplate(writer, "header", data);
```

**参数：**
- `writer` - 接收输出的 Writer
- `name` - 要执行的模板名称
- `data` - 数据对象

**抛出异常：**
- 如果执行失败，抛出 `TemplateException`
- 如果写入失败，抛出 `IOException`
- 如果不存在具有给定名称的模板，抛出 `TemplateNotFoundException`

**Since:** 0.1.0

---

## 模板自省

### `String name()`

返回根模板名称。

```java
Template template = new Template("my-template");
System.out.println(template.name()); // 输出: my-template
```

**返回：** 根模板名称

**Since:** 0.6.0

---

### `boolean hasTemplate(String name)`

检查此模板集中是否存在具有给定名称的已解析模板。

```java
template.parse("{{define \"header\"}}Header{{end}}");
if (template.hasTemplate("header")) {
    System.out.println("Header 模板已定义");
}
```

**参数：**
- `name` - 要检查的模板名称

**返回：** 如果模板已定义，返回 `true`

**Since:** 0.6.0

---

### `Set<String> definedTemplates()`

按稳定解析顺序返回已解析模板的名称。

```java
template.parse(
    "{{define \"header\"}}Header{{end}}" +
    "{{define \"footer\"}}Footer{{end}}"
);

Set<String> templates = template.definedTemplates();
// 包含: ["main-template", "header", "footer"]
```

**返回：** 不可变的模板名称集合

**Since:** 0.6.0

---

### `Template lookup(String name)`

按名称查找已解析的模板并返回一个独立副本。

```java
Template headerTemplate = template.lookup("header");
if (headerTemplate != null) {
    StringWriter writer = new StringWriter();
    headerTemplate.execute(writer, data);
}
```

**参数：**
- `name` - 要查找的模板名称

**返回：** 独立的模板副本，如果不存在该名称的模板则返回 `null`

**Since:** 0.6.0

---

### `List<Template> templates()`

按稳定解析顺序返回所有已解析模板的独立副本。

```java
List<Template> allTemplates = template.templates();
for (Template t : allTemplates) {
    System.out.println("Template: " + t.name());
}
```

**返回：** 不可变的模板副本列表

**Since:** 0.6.0

---

## 配置方法

### `Template withMissingKeyPolicy(MissingKeyPolicy policy)`

配置模板在执行过程中遇到缺失映射键时的处理方式。

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

**可用策略：**
- `INVALID`（默认） - 不执行任何操作，继续执行
- `ZERO` - 返回映射类型元素的零值
- `ERROR` - 停止执行并抛出 TemplateExecutionException

**参数：**
- `policy` - 缺失键策略；传入 `null` 重置为 `INVALID`

**返回：** 当前模板（支持方法链式调用）

**参见：**
- [MissingKeyPolicy 枚举](exception-api.md#missingkeypolicy)

**Since:** 0.6.0

---

### `Template option(String option)`

应用 Go 风格的字符串选项。

```java
// 通过选项字符串设置缺失键策略
template.option("missingkey=error");
template.option("missingkey=zero");
template.option("missingkey=default");
```

**支持的选项：**
- `missingkey=default` 或 `missingkey=invalid` - 设置策略为 INVALID
- `missingkey=zero` - 设置策略为 ZERO
- `missingkey=error` - 设置策略为 ERROR

**参数：**
- `option` - 格式为 "key=value" 的选项字符串

**返回：** 当前模板（支持方法链式调用）

**抛出异常：**
- 如果选项格式无效或不支持，抛出 `IllegalArgumentException`

**Since:** 0.6.0

---

### `MissingKeyPolicy missingKeyPolicy()`

返回当前配置的缺失键策略。

```java
MissingKeyPolicy policy = template.missingKeyPolicy();
System.out.println("当前策略: " + policy);
```

**返回：** 当前缺失键策略

**Since:** 0.6.0

---

## 文件系统辅助方法

### `void parseFile(Path path)`

从文件解析模板内容，使用 UTF-8 编码。

```java
Path templatePath = Paths.get("templates/greeting.tmpl");
template.parseFile(templatePath);
```

**参数：**
- `path` - 文件路径

**抛出异常：**
- 如果解析失败，抛出 `TemplateParseException`
- 如果无法读取文件，抛出 `IOException`

**Since:** 0.6.0

---

### `static Template parseFile(Path path, Charset charset)`

🆕 **v0.9.0+**：使用指定编码从文件解析模板内容，并返回一个新的 Template 实例。

```java
// 使用 UTF-8 编码解析
Template template = Template.parseFile(Paths.get("template.tmpl"), StandardCharsets.UTF_8);

// 使用 ISO-8859-1 编码解析
Template template = Template.parseFile(Paths.get("template.tmpl"), StandardCharsets.ISO_8859_1);
```

**参数：**
- `path` - 文件路径
- `charset` - 要使用的字符编码

**返回：** 一个新的已解析 Template 实例

**抛出异常：**
- 如果解析失败，抛出 `TemplateParseException`
- 如果无法读取文件，抛出 `IOException`

**Since:** 0.9.0

---

### `static Template parseFromClasspath(String resourcePath)`

🆕 **v0.9.0+**：从 classpath 资源解析模板内容，并返回一个新的 Template 实例。

```java
// 从 classpath 根目录加载
Template template = Template.parseFromClasspath("/templates/email.tmpl");

// 从包中加载
Template template = Template.parseFromClasspath("/com/example/templates/greeting.tmpl");
```

**参数：**
- `resourcePath` - classpath 资源路径（以 `/` 开头）

**返回：** 一个新的已解析 Template 实例

**抛出异常：**
- 如果解析失败，抛出 `TemplateParseException`
- 如果找不到资源，抛出 `TemplateNotFoundException`
- 如果无法读取资源，抛出 `IOException`

**Since:** 0.9.0

---

### `static Map<String, Template> parseDirectory(Path directory)`

🆕 **v0.9.0+**：解析目录中所有 `.tmpl` 文件，并返回模板名称到 Template 实例的映射。

```java
// 解析目录中所有 .tmpl 文件
Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));

// 访问单个模板
Template header = templates.get("header");
Template footer = templates.get("footer");
```

**参数：**
- `directory` - 包含模板文件的目录

**返回：** 一个映射，键为模板名称（不含扩展名的文件名），值为已解析的 Template 实例

**抛出异常：**
- 如果任何模板解析失败，抛出 `TemplateParseException`
- 如果无法访问目录，抛出 `IOException`

**注意：** 仅处理扩展名为 `.tmpl` 的文件，不递归处理子目录。

**Since:** 0.9.0

---

### `static List<Template> parseClasspathResources(String pattern)`

🆕 **v0.9.0+**：解析匹配模式的所有 classpath 资源，并返回 Template 实例列表。

```java
// 从包中加载所有模板
List<Template> templates = Template.parseClasspathResources("/templates/*.tmpl");

// 处理每个模板
for (Template tmpl : templates) {
    System.out.println("已加载: " + tmpl.name());
}
```

**参数：**
- `pattern` - classpath 模式（支持 `*` 通配符）

**返回：** 已解析的 Template 实例列表

**抛出异常：**
- 如果任何模板解析失败，抛出 `TemplateParseException`
- 如果无法访问资源，抛出 `IOException`

**Since:** 0.9.0

---

### `void parseFiles(Path... paths)`

从多个文件解析模板内容。

```java
template.parseFiles(
    Paths.get("templates/header.tmpl"),
    Paths.get("templates/footer.tmpl"),
    Paths.get("templates/body.tmpl")
);
```

**参数：**
- `paths` - 文件路径列表

**抛出异常：**
- 如果任何模板解析失败，抛出 `TemplateParseException`
- 如果任何文件无法读取，抛出 `IOException`

**注意：** 如果多个文件定义了相同的模板名称，最后解析的那个会生效（除非为空）。

**Since:** 0.6.0

---

### `void parseGlob(Path directory, String glob)`

解析目录中匹配 glob 模式的模板文件。

```java
// 解析 templates 目录中所有 .tmpl 文件
template.parseGlob(Paths.get("templates"), "*.tmpl");

// 解析所有 .html 文件
template.parseGlob(Paths.get("views"), "*.html");
```

**参数：**
- `directory` - 要搜索的目录
- `glob` - glob 模式（例如 "*.tmpl"、"*.html"）

**抛出异常：**
- 如果任何模板解析失败，抛出 `TemplateParseException`
- 如果无法访问目录，抛出 `IOException`

**Since:** 0.6.0

---

## 线程安全

`Template` 类具有特定的线程安全特征：

### 解析期间
**非线程安全。** 解析会修改内部状态，应仅由单个线程执行。

### 解析完成后
**执行是线程安全的**，条件是为每次执行使用不同的 `Writer` 实例。多个线程可以安全地对同一个已解析模板并发调用 `execute()`。

### 并发修改时
如果需要在其他线程执行模板时修改模板（添加定义、更改配置），请使用复制构造函数：

```java
// 创建并解析模板一次
Template baseTemplate = new Template("master");
baseTemplate.parse("{{.message}}");

// 在每个线程中，使用前创建副本
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    final int taskId = i;
    executor.submit(() -> {
        Template threadSafe = new Template(baseTemplate);
        StringWriter writer = new StringWriter();
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Task " + taskId);
        
        try {
            threadSafe.execute(writer, data);
            System.out.println(writer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
}
executor.shutdown();
```

这确保每个线程拥有自己的独立副本，可以在不影响其他线程的情况下进行修改。

---

## 摘要

`Template` 类提供完整的 API 用于：
- 使用自定义分隔符和函数创建模板
- 从字符串、流、Reader 或文件解析模板文本
- **从 classpath、目录加载模板，或使用指定编码加载模板（v0.9.0+）**
- 使用任意 Java 对象作为数据执行模板
- 自省模板集以发现已定义的模板
- 配置错误处理策略
- 通过克隆管理线程安全

更多信息：
- 参见 [Function API](function-api.md) 了解自定义函数实现
- 参见 [Exception API](exception-api.md) 了解错误处理详情
- 参见 [用户指南](../user-guide/template-syntax.md) 了解模板语法参考
