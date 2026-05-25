# 常见问题解答（FAQ）

本文档回答关于 gotemplate4j 最常见的问题。如果你在这里找不到答案，请查看[用户指南](../user-guide/)或在 [GitHub](https://github.com/verils/gotemplate4j/issues) 上提交问题。

## 目录

- [一般问题](#一般问题)
- [安装与设置](#安装与设置)
- [模板语法](#模板语法)
- [数据处理](#数据处理)
- [函数](#函数)
- [错误处理](#错误处理)
- [性能](#性能)
- [Go 兼容性](#go-兼容性)
- [故障排除](#故障排除)

---

## 常见问题

### 什么是 gotemplate4j？

Gotemplate4j 是 Go 的 `text/template` 包的 Java 实现。它允许你在 Java 应用中使用 Go 模板语法，从模板和数据生成动态文本输出。

### gotemplate4j 的用途是什么？

Gotemplate4j 是为**需要操作 Go 模板的 Java 开发者**设计的。它不是为了替代 Go 原生 `text/template` 或与其他 Java 模板引擎竞争。

**适用场景：**
- 你是一名在 Go 生态中工作的 Java 开发者
- 你需要在 Java 应用中处理现有的 Go 模板
- 你希望在跨语言项目中使用熟悉的 Go 模板语法
- 你正在从 Go 迁移到 Java，并希望复用模板

**不适用场景：**
- 你在构建纯 Go 应用（请使用 Go 原生的 `text/template`）
- 你需要高级的 Java 模板功能（请使用 Thymeleaf、FreeMarker 等）
- 你追求最高的性能（Go 原生实现更快）

Gotemplate4j 优先考虑**兼容性和简洁性**，而非功能丰富度或性能。

### gotemplate4j 适合生产环境吗？

是的，gotemplate4j 是稳定的且经过充分测试的，拥有 90%+ 代码覆盖率。然而，它的设计优先考虑简洁性和 Go 兼容性，而非功能丰富度。对于复杂的企业需求，请评估功能集是否满足你的要求。

### 当前版本和发布周期是什么？

当前版本是 **0.10.0**。项目遵循语义化版本管理。查看 [CHANGELOG](../CHANGELOG) 了解发布历史。

---

## 安装与设置

详细的安装说明请参阅[安装指南](getting-started/installation.md)。

### 快速链接

- **Maven/Gradle 设置**：参阅[安装指南 - Maven 和 Gradle](getting-started/installation.md#maven)
- **手动安装**：参阅[安装指南 - 手动安装](getting-started/installation.md#manual-installation)
- **验证安装**：参阅[安装指南 - 验证安装](getting-started/installation.md#verify-installation)
- **故障排除**：参阅[安装指南 - 故障排除](getting-started/installation.md#troubleshooting)

---

## 模板语法

### 如何在模板中访问对象属性？

使用点号表示法。Gotemplate4j 支持：
- JavaBean getter 方法：`{{.name}}` 调用 `getName()`
- 公共字段：`{{.field}}` 直接访问公共字段
- Map 键：`{{.key}}` 访问 `map.get("key")`

示例：
```java
public class User {
    private String name;
    public String getName() { return name; }
}

// 在模板中：{{.name}}
```

### 如何检查值是否为 null 或空？

使用 `if` 动作配合真值规则：

```gotemplate
{{if .value}}值存在且不为空{{end}}
{{if not .value}}值缺失或为空{{end}}
```

**真值规则：**
- `null` → false
- `Boolean.FALSE` → false
- 空字符串 `""` → false
- 空集合/数组 → false
- 零值数字 → false
- 其他所有值 → true

### 如何遍历列表？

使用 `range` 动作：

```gotemplate
{{range $index, $item := .items}}
第 {{$index}} 项：{{$item}}
{{end}}
```

简单迭代（不带索引）：
```gotemplate
{{range .items}}
项：{{.}}
{{end}}
```

### 可以嵌套模板吗？

可以，使用 `template` 动作：

```gotemplate
{{define "header"}}<h1>{{.title}}</h1>{{end}}

{{template "header" .}}
<p>主要内容</p>
```

### 如何包含外部模板文件？

使用文件解析辅助方法：

```java
// 解析单个文件
Template tmpl = Template.parseFile("path/to/template.tmpl");

// 解析多个文件
Template tmpl = Template.parseFiles(
    "header.tmpl",
    "footer.tmpl", 
    "content.tmpl"
);

// 使用 glob 模式解析
Template tmpl = Template.parseGlob("templates/*.tmpl");
```

---

## 数据处理

### 可以向模板传递哪些 Java 类型？

Gotemplate4j 支持：
- **JavaBean**（带有 getter 方法的对象）
- **Map**（`Map<String, Object>`）
- **List/数组**（`List<?>`、数组）
- **枚举**
- **Optional**（`Optional<T>`）
- **基本类型**及其包装类
- **字符串**、数字、日期

### 如何向模板传递多个值？

将它们包装在 Map 或自定义对象中：

**使用 Map：**
```java
Map<String, Object> data = new HashMap<>();
data.put("name", "John");
data.put("age", 30);
template.execute(data, writer);
```

**使用对象：**
```java
public class UserData {
    private String name;
    private int age;
    // getters...
}

UserData data = new UserData("John", 30);
template.execute(data, writer);
```

### 如何处理 null 值？

默认情况下，null 值渲染为空字符串。你可以配置此行为：

```java
// 缺失键时报错
template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);

// 缺失键时显示零值
template.withMissingKeyPolicy(MissingKeyPolicy.ZERO);

// 默认：显示空字符串
template.withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);
```

### 可以使用 Java 8 Optional 吗？

可以，gotemplate4j 会自动解包 `Optional`：

```java
Optional<String> name = Optional.of("John");
// 在模板中：{{.name}} 渲染为 "John"

Optional<String> empty = Optional.empty();
// 在模板中：{{.name}} 渲染为 ""（空）
```

### 如何使用嵌套对象？

使用点号链：

```java
public class Order {
    private Customer customer;
    public Customer getCustomer() { return customer; }
}

public class Customer {
    private String name;
    public String getName() { return name; }
}

// 在模板中：{{.customer.name}}
```

---

## 函数

### 有哪些内置函数可用？

Gotemplate4j 提供 18+ 内置函数：

**比较：** `eq`、`ne`、`lt`、`le`、`gt`、`ge`  
**逻辑：** `and`、`or`、`not`  
**字符串：** `len`、`print`、`printf`、`println`  
**类型转换：** `js`、`html`、`urlquery`  
**内省：** `index`、`call`

请参阅[函数指南](user-guide/functions.md)获取完整文档。

### 如何创建自定义函数？

实现 `Function` 接口：

```java
public class UpperCaseFunction implements Function {
    @Override
    public String name() {
        return "upper";
    }
    
    @Override
    public Object apply(Object... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("upper 需要 1 个参数");
        }
        return args[0].toString().toUpperCase();
    }
}

// 注册函数
Template template = new Template("demo")
    .func(new UpperCaseFunction());
```

在模板中使用：
```gotemplate
{{upper "hello"}}  <!-- 输出：HELLO -->
```

### 可以覆盖内置函数吗？

可以，注册一个同名的自定义函数：

```java
template.func(new CustomPrintFunction()); // 覆盖 "print"
```

⚠️ **警告：** 覆盖内置函数可能会破坏模板兼容性。

### 如何向函数传递参数？

函数接受可变参数：

```gotemplate
{{printf "%s 今年 %d 岁" .name .age}}
{{add 1 2 3}}  <!-- 所有参数求和 -->
```

在自定义函数中，通过 `args` 数组访问：
```java
@Override
public Object apply(Object... args) {
    // args[0], args[1], 等等
}
```

---

## 错误处理

### 可能会发生哪些类型的错误？

三种主要的异常类型：

1. **TemplateParseException** - 解析期间的语法错误
2. **TemplateNotFoundException** - 模板文件未找到
3. **TemplateExecutionException** - 执行期间的运行时错误

所有这些都继承自 `TemplateException`。

### 如何处理解析错误？

在创建模板时捕获异常：

```java
try {
    Template template = new Template("demo")
        .parse("{{if .name}}Hello {{.name}}{{end"); // 缺少 }}
} catch (TemplateParseException e) {
    System.err.println("解析错误：" + e.getMessage());
}
```

### 如何处理执行错误？

在模板执行时捕获异常：

```java
try {
    template.execute(data, writer);
} catch (TemplateExecutionException e) {
    System.err.println("执行错误：" + e.getMessage());
}
```

### 如何获取详细的错误信息？

异常包含上下文信息：

```java
catch (TemplateException e) {
    System.err.println("错误：" + e.getMessage());
    System.err.println("模板：" + e.getTemplateName());
    System.err.println("行号：" + e.getLineNumber());
    e.printStackTrace();
}
```

### 应该在生产环境中使用 MissingKeyPolicy.ERROR 吗？

视情况而定：

**使用 ERROR 的情况：**
- 开发/测试阶段，尽早发现缺失的数据
- 数据完整性至关重要
- 你希望采用快速失败的行为

**使用 DEFAULT 或 ZERO 的情况：**
- 某些字段确实是可选的
- 你偏好优雅降级
- 模板使用 `if` 检查来处理缺失值

---

## 性能

### gotemplate4j 有多快？

Gotemplate4j 针对典型用例进行了优化：
- **解析**：中等模板约 1-5ms
- **执行**：每次执行约 0.1-1ms（取决于数据复杂度）

参阅 [TemplateBenchmark.java](../src/test/java/io/github/verils/gotemplate/TemplateBenchmark.java) 获取详细的基准测试。

### 如何提高性能？

**最佳实践：**

1. **复用已解析的模板** - 解析一次，执行多次
   ```java
   Template template = new Template("demo").parse("{{.name}}");
   // 多次执行时复用 'template'
   ```

2. **克隆模板以保证线程安全**
   ```java
   Template shared = new Template("demo").parse("{{.name}}");
   // 在每个线程中：
   Template clone = shared.clone();
   clone.execute(data, writer);
   ```

3. **避免在循环中使用复杂表达式**
   ```gotemplate
   {{/* 不好 */}}
   {{range .items}}
     {{call expensiveFunc .}}
   {{end}}
   
   {{/* 好 */}}
   在传递给模板之前，在 Java 中预先计算值
   ```

4. **对大型输出使用 StringBuilder**
   ```java
   StringWriter writer = new StringWriter();
   template.execute(data, writer);
   String result = writer.toString();
   ```

### gotemplate4j 是线程安全的吗？

**已解析的模板是不可变的，执行时是线程安全的。** 但是，模板配置（添加函数等）不是线程安全的。

**推荐模式：**
```java
// 一次性创建和配置（在启动期间）
Template baseTemplate = new Template("demo")
    .parse("{{.name}}")
    .func(myFunction);

// 为每个线程/请求克隆
Template threadSafe = baseTemplate.clone();
threadSafe.execute(data, writer);
```

### gotemplate4j 支持模板缓存吗？

不是内置的，但很容易实现：

```java
ConcurrentHashMap<String, Template> cache = new ConcurrentHashMap<>();

public Template getTemplate(String name) {
    return cache.computeIfAbsent(name, n -> {
        try {
            return Template.parseFile(n + ".tmpl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
```

---

## Go 兼容性

### gotemplate4j 与 Go 模板的兼容性如何？

Gotemplate4j 实现了**核心的 Go `text/template` 功能**：
- ✅ 所有控制结构（if、range、with、block）
- ✅ 点号表示法和变量赋值
- ✅ 内置函数（eq、len、printf 等）
- ✅ 模板定义和包含
- ✅ 管道语法

**已知差异：** 参阅 [Go 兼容性指南](advanced/compatibility.md)

### 可以使用我现有的 Go 模板吗？

大多数简单模板可以无需修改地工作。常见问题：

**可以直接使用：**
```gotemplate
{{.name}}
{{if .active}}活跃{{end}}
{{range .items}}{{.}}{{end}}
```

**可能需要调整：**
- Go 特有的函数（未实现）
- 通道操作（Java 不支持）
- 带参数的方法调用（支持有限）

参阅[迁移指南](advanced/migration.md)获取详情。

### 为什么带参数的方法调用不能工作？

Go 模板支持 `{{.Method arg1 arg2}}`，但 gotemplate4j 出于安全原因对此进行了限制。请改用自定义函数：

```gotemplate
{{/* Go 风格（不支持） */}}
{{.FormatDate "2006-01-02"}}

{{/* Java 风格（使用自定义函数） */}}
{{formatDate .date "yyyy-MM-dd"}}
```

### gotemplate4j 支持 Go 的 html/template 吗？

不支持，gotemplate4j 只实现了 `text/template`。如需 HTML 转义，请使用 `html` 函数：

```gotemplate
{{html .userInput}}
```

或在传递给模板之前在 Java 中预先转义。

---

## 故障排除

### 我的模板什么都没渲染。出了什么问题？

常见原因：

1. **空数据** - 检查你的数据对象是否有值
2. **属性名称错误** - 验证 getter 名称是否与模板匹配
3. **语法错误** - 检查是否有未闭合的 `{{` 或 `}}`
4. **静默失败** - 启用错误报告

调试方法：
```java
System.out.println("数据：" + data);
template.execute(data, new PrintWriter(System.out));
```

### 我收到 "unexpected EOF" 错误。这是什么意思？

你有一个未闭合的动作。检查：
- `if`、`range`、`with`、`block` 是否缺少 `{{end}}`
- 是否有未闭合的 `{{` 或 `}}`
- 是否有不匹配的大括号

示例：
```gotemplate
{{/* 错误 */}}
{{if .name}}
Hello {{.name}}

{{/* 正确 */}}
{{if .name}}
Hello {{.name}}
{{end}}
```

### 为什么我的数字比较失败了？

Go 模板按值比较数字，但类型很重要。确保类型一致：

```gotemplate
{{/* 类型不同时可能失败 */}}
{{eq .count 5}}

{{/* 更安全：转换为相同类型 */}}
{{eq (int .count) 5}}
```

### 如何调试模板变量？

使用 `printf` 检查值：

```gotemplate
{{printf "DEBUG: name=%v type=%T" .name .name}}
```

或者打印整个数据结构：
```gotemplate
{{printf "DEBUG: data=%v" .}}
```

### 我的自定义函数没有被调用。为什么？

检查：
1. 函数是否已注册：`.func(myFunction)`
2. 函数名称是否匹配：`name()` 返回正确的名称
3. 参数数量是否正确
4. `apply()` 中是否没有抛出异常

调试方法：
```java
System.out.println("已注册的函数：" + template.functions());
```

### 模板执行很慢。如何优化？

参阅上方的[性能](#性能)部分。要点：
- 复用已解析的模板
- 克隆以保证线程安全
- 在 Java 中预先计算复杂值
- 分析以定位瓶颈

### 可以在 Spring Boot 中使用 gotemplate4j 吗？

可以，手动集成：

```java
@Component
public class TemplateService {
    private final Map<String, Template> templates = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() throws IOException {
        templates.put("email", Template.parseFile("templates/email.tmpl"));
    }
    
    public String render(String name, Object data) {
        Template template = templates.get(name).clone();
        StringWriter writer = new StringWriter();
        template.execute(data, writer);
        return writer.toString();
    }
}
```

或者寻找社区的 Spring 集成库。

---

## v0.9.1 补丁版本

### gotemplate4j v0.9.1 有什么新变化？

版本 0.9.1 是 v0.9.x Java 8 系列的 bug 修复版本。它修复了关键的模板引擎问题，扩展了语法覆盖范围，并改进了测试组织。

### v0.9.1 是向后兼容的吗？

是的。v0.9.1 完全向后兼容 v0.9.0 和 v0.8.x。现有模板和代码无需更改即可继续使用。

### v0.9.1 需要什么 Java 版本？

v0.9.1 需要 **Java 8 或更高版本**。v0.9.x 系列是最后一个支持 Java 8 的系列。v0.10.0 开发系列需要 **Java 11 或更高版本**。

---

## v0.9.0 新功能

### gotemplate4j v0.9.0 有什么新变化？

版本 0.9.0 引入了三项重大增强：

1. **增强的文件加载 API** - 从 classpath、目录加载模板，或指定编码
2. **整数范围支持** - Go 风格的 `{{range $i := 5}}` 语法，用于迭代数字序列
3. **增强的错误诊断** - 智能的错误消息，包含拼写建议和可用选项列表

完整详情请参阅 [CHANGELOG](../CHANGELOG)。

### 如何从 classpath 加载模板？

使用新的静态方法：

```java
Template template = Template.parseFromClasspath("/templates/email.tmpl");
```

这在 Web 应用和 JAR 部署（模板打包在应用内部）中特别有用。

### 如何从目录加载所有模板？

使用 `parseDirectory()` 一次性加载所有 `.tmpl` 文件：

```java
Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));

// 通过文件名（不含扩展名）访问
Template header = templates.get("header");
```

### 加载模板时可以指定文件编码吗？

可以！使用带 Charset 参数的 `parseFile()`：

```java
Template template = Template.parseFile(
    Paths.get("template.tmpl"), 
    StandardCharsets.UTF_8
);
```

这确保模板中的非 ASCII 字符能正确处理。

### 整数范围迭代是如何工作的？

你现在可以迭代数字序列：

```gotemplate
{{range $i := 5}}
  索引：{{$i}}
{{end}}
```

输出：
```
索引：0
索引：1
索引：2
索引：3
索引：4
```

这与 Go 的 `text/template` 行为一致，对于生成重复内容非常有用。

### 什么是增强的错误诊断？

错误消息现在包含更多有用信息：

**v0.9.0 之前：**
```
can't evaluate field FristName
```

**v0.9.0 之后：**
```
can't evaluate field User.FristName. Available fields: [age, firstName, lastName]. Did you mean 'firstName'?
```

增强的诊断提供：
- 完整的字段路径上下文
- 可用字段/键/函数列表
- 使用模糊匹配的拼写建议
- 函数错误的参数数量信息

详情参阅[错误处理指南](user-guide/error-handling.md#enhanced-error-diagnostics-v090)。

### v0.9.0 是向后兼容的吗？

是的！v0.9.0 完全向后兼容 v0.8.x。所有现有模板和代码无需更改即可继续使用。

### v0.9.x 需要什么 Java 版本？

v0.9.x 需要 **Java 8 或更高版本**。这是最后一个支持 Java 8 的发布系列。v0.10.0 开发系列需要 **Java 11 或更高版本**。

---

## 还有问题？

- 📖 浏览[用户指南](user-guide/)获取详细文档
- 💡 查看[示例](examples/)获取真实场景用例
- 🐛 在 [GitHub Issues](https://github.com/verils/gotemplate4j/issues) 上报告 bug 或提问
- 📝 阅读 [CHANGELOG](../CHANGELOG) 了解最近的更新

---

*最后更新：2026-05-23（gotemplate4j v0.10.0）*
