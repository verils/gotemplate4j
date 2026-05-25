# Go Template 兼容性

gotemplate4j 实现了 Go 的 `text/template` 包的一个 Java 兼容子集。v0.10.0 开发线要求 Java 11 或更高版本。其目标是为需要执行 Go 风格模板的 Java 应用程序提供实用兼容性，同时明确保留 Java 特有的行为。

本文档详细说明了哪些功能与 Go 行为一致、哪些存在差异以及这些差异存在的原因。

---

## 目录

- [已覆盖的核心行为](#已覆盖的核心行为)
- [Java 特有行为](#java-特有行为)
- [缺失键处理](#缺失键处理)
- [不支持或暂缓的 Go API](#不支持或暂缓的-go-api)
- [行为差异总结](#行为差异总结)
- [Java 替代方案](#java-替代方案)

---

## 已覆盖的核心行为

gotemplate4j 支持实际使用中最常见的 Go template 功能。

### 模板语法

- **动作和管道**：文本动作、字段链、变量、带括号的管道参数以及多命令管道。
- **注释**：`{{/* 注释 */}}` 风格的注释，会从输出中移除。
- **管道符**：使用 `|` 操作符链接函数（例如 `{{.name | upper | trim}}`）。
- **变量赋值**：`$var := value` 语法用于局部变量。

### 流程控制

- **条件判断**：`if`、`else`、`else if`，支持布尔求值。
- **循环**：`range` 遍历数组、列表、映射和字符串。
- **上下文切换**：`with` 和 `else with` 用于改变点上下文。
- **循环控制**：`break` 和 `continue` 语句，可在 range 循环内使用。
- **Else 子句**：`range ... else` 和 `with ... else` 用于处理空/null 情况。

### 模板集

- **定义**：`define` 用于创建命名模板块。
- **调用**：`template` 用于引入其他模板。
- **块**：Block 风格的覆盖，支持模板继承。
- **重复解析**：模板可以多次解析以添加定义。
- **按名称执行**：从模板集中按名称执行特定模板。

### 内置函数

支持所有标准 Go template 函数：

- **比较**：`eq`、`ne`、`lt`、`le`、`gt`、`ge`
- **逻辑**：`and`、`or`、`not`
- **集合**：`len`、`index`
- **格式化**：`printf`、`print`、`println`
- **字符串转义**：`html`、`js`、`urlquery`
- **函数调用**：`call` 用于调用 Function 实例
- **gotemplate4j 扩展**：`default`、`deepEqual`、`typeof`、`kindOf`

### 错误处理

- **解析错误**：包含源上下文信息，显示解析失败的位置。
- **执行错误**：函数运行时失败包装在 `TemplateExecutionException` 中。
- **IO 错误**：Writer 的 `IOException` 值传播给调用者。

## Java 特有行为

以下行为由于 Java 语言特性而与 Go template 有所不同。

### 数据访问

- **JavaBean getter**：通过 getter 方法访问属性（例如 `getName()` → `{{.Name}}`）。
- **公有字段**：支持对公有字段的直接字段访问。
- **无参方法**：公有无参方法可以在字段链中调用。
- **带参方法**：出于安全原因，有意不支持在模板中调用带参数的方法。

示例：
```java
public class User {
    private String name;

    public String getName() { return name; }  // 可通过 {{.Name}} 访问
    public int getAge() { return 30; }        // 可通过 {{.Age}} 访问
}
```

### Optional 处理

- **自动解包**：`Optional` 值会自动解包。
- **空 Optional**：行为类似 `null`（假值，打印为空）。

```java
Optional<String> name = Optional.of("Alice");
// 模板：{{.name}} → "Alice"

Optional<String> empty = Optional.empty();
// 模板：{{.empty}} → ""（空字符串）
```

### 枚举支持

- **toString() 渲染**：枚举使用其 `toString()` 方法渲染。
- **方法访问**：可以访问诸如 `name()` 和 `ordinal()` 之类的公有无参方法。

```java
public enum Status { ACTIVE, INACTIVE }

// 模板示例：
{{.Status}}           // → "ACTIVE"（通过 toString）
{{.Status.name}}      // → "ACTIVE"（通过 name() 方法）
{{.Status.ordinal}}   // → 0（通过 ordinal() 方法）
```

### Null 处理

- **假值求值**：`null` 值在条件判断中求值为 false。
- **空输出**：`null` 值打印为空字符串。
- **安全导航**：字段链在遇到 `null` 时停止，不会抛出异常（默认行为）。

```gotemplate
{{if .Name}}Name exists{{else}}No name{{end}}
{{.Name}}  <!-- 如果为 null，则什么都不打印 -->
```

### 映射迭代顺序

- **Java Map 顺序**：迭代遵循映射实现的顺序。
- **不自动排序**：与 Go 不同，gotemplate4j 不会对映射键进行排序。
- **确定性顺序**：当输出顺序重要时，请使用 `LinkedHashMap`。

```java
// Go 行为：键按字母顺序排序
// gotemplate4j 行为：键遵循插入顺序
Map<String, String> map = new LinkedHashMap<>();
map.put("z", "last");
map.put("a", "first");
// Range 将产生：z, a（插入顺序）
```

### 自定义扩展

以下函数是 gotemplate4j 的附加功能，不属于 Go 标准库：

- **`default`**：提供回退值（`{{.value | default "fallback"}}`）
- **`deepEqual`**：复杂对象的深度比较
- **`typeof`**：获取值的 Java 类型名称
- **`kindOf`**：获取值的类别/种类

## 缺失键处理

默认行为保持向后兼容：缺失的映射键求值为 `null`，打印为空输出。

### 配置

```java
Template template = new Template("demo");
template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

或使用 Go 风格的字符串选项：

```java
template.option("missingkey=error");
```

### 支持的策略

- **`DEFAULT`**：缺失的映射键求值为 `null`。这是默认行为。
- **`ZERO`**：当目标类型可知时，返回 Java 的零值。对于不可知类型的缺失映射键，回退为 `null`。
- **`ERROR`**：对于缺失的映射键和 `null` 值之后的缺失字段链段落，抛出 `TemplateExecutionException`。

### 使用场景

- **开发环境**：使用 `ERROR` 策略在开发过程中尽早发现问题。
- **生产环境**：使用 `DEFAULT` 或 `ZERO` 在生产环境中进行优雅降级。
- **严格验证**：当所有必填字段必须存在时，使用 `ERROR`。

## 不支持或暂缓的 Go API

以下 Go template 功能目前不受 gotemplate4j 支持。

### 文件系统和 IO

- **`ParseFS`**：Go 的文件系统抽象不可用。请改用 Java 流、Reader 或调用者管理的文件加载。
  ```java
  // 替代 ParseFS 的做法：
  template.parseFile(Paths.get("template.tmpl"));
  template.parseFiles(Paths.get("file1.tmpl"), Paths.get("file2.tmpl"));
  template.parseGlob(Paths.get("templates/*.tmpl"));
  ```

### Range 限制

- **整数 range**：`range $i := 5`（遍历整数）尚未支持。
- **通道**：Go 通道无法从 Java 中使用。
- **迭代器**：`iter.Seq` 和 `iter.Seq2` 是 Go 特有的，不可用。

解决方案：在传递给模板之前将数据转换为 Java 数组、集合或映射。

### 方法调用

- **带参方法**：出于安全原因，有意禁用在模板中调用带参数的通用方法。
  ```java
  // 模板中不支持：
  // {{.object.method "arg1" "arg2"}}

  // 应改用自定义函数：
  Function myFunc = args -> { /* 逻辑 */ };
  template.addFunction("myFunc", myFunc);
  // 模板：{{myFunc .object "arg1" "arg2"}}
  ```

### 函数值

- **Go 函数值字段**：Go 在结构体字段或映射条目中存储函数的能力不受支持。
- **`call` 函数**：在 gotemplate4j 中，`call` 仅接受 `Function` 接口实例，不接受任意方法。

### 数字格式化

- **复数支持**：未实现完整的 Go 数字和复数格式化兼容。
- **格式化动词**：与 Go 相比，部分高级 `printf` 格式化动词的支持可能有限。

---

## 行为差异总结

下表总结了 Go template 与 gotemplate4j 之间的关键差异：

| 功能 | Go `text/template` | gotemplate4j | 原因 |
|---------|-------------------|--------------|--------|
| 映射迭代顺序 | 键已排序 | 插入顺序 | Java Map 行为 |
| 缺失键 | 可配置（默认：error） | 可配置（默认：null） | 向后兼容 |
| Null 显示 | `<no value>` | 空字符串 | Java 惯例 |
| 方法调用 | 任意公有方法 | 仅无参方法 | 安全性 |
| 整数 range | 支持 | 尚未支持 | 实现优先级 |
| 通道 | 支持 | 不适用 | Java 没有通道 |
| Optional | 不适用 | 自动解包 | Java 特有功能 |
| 枚举 | 不适用 | toString() + 方法 | Java 特有功能 |
| 自定义函数 | Go 函数 | `Function` 接口 | Java 类型系统 |
| ParseFS | 支持 | 不支持 | Java IO 模型不同 |

---

## Java 替代方案

以下是 gotemplate4j 中常见 Go template 模式的推荐做法。

### 模板加载

替代 Go 的文件系统抽象，使用 Java IO：

```java
// 从文件加载
Template template = new Template("mytemplate");
template.parseFile(Paths.get("templates/mytemplate.tmpl"));

// 从多个文件加载
template.parseFiles(
    Paths.get("base.tmpl"),
    Paths.get("partials/header.tmpl"),
    Paths.get("partials/footer.tmpl")
);

// 从 glob 模式加载
template.parseGlob(Paths.get("templates/*.tmpl"));

// 从 InputStream 或 Reader 加载（调用者管理的 IO）
try (InputStream is = getClass().getResourceAsStream("/template.tmpl")) {
    template.parse(is);
}
```

### 数据模型

使用 JavaBean、公有字段、映射和枚举作为模板数据模型：

```java
// JavaBean（推荐用于结构化数据）
public class User {
    private String name;
    private int age;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

// Map（用于动态数据）
Map<String, Object> data = new HashMap<>();
data.put("name", "Alice");
data.put("age", 30);

// 枚举
public enum Status { ACTIVE, INACTIVE }
```

### 自定义函数

实现 `Function` 接口以实现自定义逻辑：

```java
import io.github.verils.gotemplate.Function;

// 简单函数
Function upperCase = args -> {
    return ((String) args[0]).toUpperCase();
};

// 带验证的函数
Function divide = args -> {
    if (args.length != 2) {
        throw new IllegalArgumentException("divide requires 2 arguments");
    }
    double a = ((Number) args[0]).doubleValue();
    double b = ((Number) args[1]).doubleValue();
    if (b == 0) {
        throw new ArithmeticException("Division by zero");
    }
    return a / b;
};

// 注册函数
Map<String, Function> functions = new HashMap<>();
functions.put("upper", upperCase);
functions.put("divide", divide);

Template template = new Template("demo", functions);
```

### 模板内省

在执行前检查已解析的模板集：

```java
Template template = new Template("base");
template.parseFiles(Paths.get("base.tmpl"), Paths.get("page.tmpl"));

// 获取模板名称
String name = template.name();  // "base"

// 列出所有已定义的模板
Set<String> names = template.definedTemplates();  // ["base", "page", "header", ...]

// 检查模板是否存在
boolean hasHeader = template.hasTemplate("header");  // true 或 false

// 查找特定模板（返回独立副本）
Template headerTemplate = template.lookup("header");

// 将所有模板作为 Map 获取
Map<String, Template> all = template.templates();
```

### 错误处理

适当地处理不同类型的错误：

```java
try {
    // 解析阶段
    Template template = new Template("demo");
    template.parse(templateText);

    // 执行阶段
    StringWriter writer = new StringWriter();
    template.execute(writer, data);

} catch (TemplateParseException e) {
    // 模板中的语法错误
    System.err.println("Parse error: " + e.getMessage());

} catch (TemplateExecutionException e) {
    // 执行期间的运行时错误
    System.err.println("Execution error: " + e.getMessage());

} catch (TemplateNotFoundException e) {
    // 引用的模板未找到
    System.err.println("Template not found: " + e.getMessage());
}
```

---

## 参见

- [迁移指南](migration.md) - 从 Go template 迁移的逐步指南
- [用户指南](user-guide/) - 全面的使用文档
- [示例](examples/) - 实际代码示例
- [API 参考](api-reference/) - 详细的 API 文档
