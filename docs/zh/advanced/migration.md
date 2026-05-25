# 从 Go text/template 迁移

本指南提供了常见的 Go `text/template` 使用模式到 gotemplate4j 等价实现的全面映射。旨在帮助开发者以最小的障碍将现有 Go 模板迁移到 Java 应用程序。

---

## 目录

- [数据模型](#数据模型)
- [Null 和缺失值](#null-和缺失值)
- [映射迭代](#映射迭代)
- [自定义函数](#自定义函数)
- [模板集](#模板集)
- [流程控制](#流程控制)
- [内置函数](#内置函数)
- [应避免的 Go 专属 API](#应避免的-go-专属-api)
- [常见迁移模式](#常见迁移模式)
- [测试你的迁移](#测试你的迁移)

---

## 数据模型

Go 结构体通常映射为 gotemplate4j 中的 JavaBean。

### Go 结构体 → JavaBean

**Go：**
```go
type User struct {
    Name string
    Age  int
}
```

**Java：**
```java
public class User {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```

**模板（两者相同）：**
```gotemplate
{{.Name}} is {{.Age}} years old
```

模板可以通过 `{{.Name}}` 访问此属性。公有字段同样受支持，当数据形态动态变化时可以使用映射。

### 枚举支持

枚举通过 `toString()` 渲染，并暴露公有无参方法：

```java
public enum Status {
    ACTIVE,
    INACTIVE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
```

```gotemplate
{{.Status}}           // → "active"（通过 toString）
{{.Status.name}}      // → "ACTIVE"（通过 name() 方法）
{{.Status.ordinal}}   // → 0（通过 ordinal() 方法）
```

## Null 和缺失值

默认情况下，`null` 和缺失的映射键为假值，打印为空输出：

```gotemplate
{{if .Name}}{{.Name}}{{else}}anonymous{{end}}
```

### Go 与 Java 行为对比

**Go：**
- 默认：缺失键会导致错误
- 可通过 `.Option("missingkey=error/invalid/zero")` 配置
- Null 值显示为 `<no value>`

**gotemplate4j：**
- 默认：缺失键返回 `null`（打印为空）
- 可通过 `withMissingKeyPolicy()` 或 `.option("missingkey=...")` 配置
- Null 值打印为空字符串

### 配置严格模式

对于更严格的生产检查，进行如下配置：

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

或使用 Go 风格的选项字符串：

```java
template.option("missingkey=error");
```

`MissingKeyPolicy.ZERO` 可用于 Go 风格的命名方式，但 Java 通常无法为缺失的映射条推断零值。

## 映射迭代

Go 模板对具有基本有序键类型的映射进行排序。gotemplate4j 则遵循 Java `Map` 的迭代顺序。

### Go 行为

```go
// Go 自动对映射键排序以实现确定性输出
{{range $key, $value := .MyMap}}
  {{$key}}: {{$value}}
{{end}}
// 输出：键按字母顺序排列
```

### gotemplate4j 行为

```java
// Java 保留插入顺序（对于 LinkedHashMap）
Map<String, String> map = new LinkedHashMap<>();
map.put("z", "last");
map.put("a", "first");
data.put("MyMap", map);
```

```gotemplate
{{range $key, $value := .MyMap}}
  {{$key}}: {{$value}}
{{end}}
// 输出：z: last, a: first（插入顺序）
```

**建议**：当输出顺序重要时，请使用 `LinkedHashMap`，或在传递给模板之前在 Java 中对键进行排序。

---

## 流程控制

流程控制语法在 Go 和 gotemplate4j 中完全相同。

### 条件判断

```gotemplate
{{if .Condition}}
  True branch
{{else if .OtherCondition}}
  Other branch
{{else}}
  False branch
{{end}}
```

### 循环

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{else}}
  No items
{{end}}
```

### 上下文切换

```gotemplate
{{with .User}}
  Name: {{.Name}}
  Email: {{.Email}}
{{else}}
  No user logged in
{{end}}
```

### 循环控制

```gotemplate
{{range .Items}}
  {{if .Skip}}{{continue}}{{end}}
  {{if .Stop}}{{break}}{{end}}
  {{.Value}}
{{end}}
```

---

## 内置函数

所有标准 Go template 函数在 gotemplate4j 中的工作方式相同。

### 比较函数

```gotemplate
{{eq .Value 10}}    // 等于
{{ne .Value 10}}    // 不等于
{{lt .Value 10}}    // 小于
{{le .Value 10}}    // 小于等于
{{gt .Value 10}}    // 大于
{{ge .Value 10}}    // 大于等于
```

### 逻辑函数

```gotemplate
{{and .A .B}}       // 逻辑与
{{or .A .B}}        // 逻辑或
{{not .A}}          // 逻辑非
```

### 集合函数

```gotemplate
{{len .Items}}      // 数组/切片/映射/字符串的长度
{{index .Items 0}}  // 获取指定索引的元素
```

### 格式化函数

```gotemplate
{{printf "%s: %d" .Name .Age}}  // 格式化字符串
{{print .Name .Age}}            // 用空格连接
{{println .Name .Age}}          // 用换行符连接
```

### 字符串转义

```gotemplate
{{html .Content}}     // HTML 转义
{{js .Content}}       // JavaScript 转义
{{urlquery .Content}} // URL 查询转义
```

### gotemplate4j 扩展

以下是不属于 Go 标准库的附加函数：

```gotemplate
{{default .Value "fallback"}}  // 提供默认值
{{deepEqual .A .B}}            // 深度相等检查
{{typeof .Value}}              // 获取 Java 类型名称
{{kindOf .Value}}              // 获取值的类别/种类
```

## 自定义函数

Go template 函数映射为 gotemplate4j 的 `Function` 实例。

### Go 自定义函数

```go
funcMap := template.FuncMap{
    "upper": strings.ToUpper,
    "add": func(a, b int) int { return a + b },
}
tmpl := template.New("demo").Funcs(funcMap)
```

### gotemplate4j 自定义函数

```java
import io.github.verils.gotemplate.Function;

Map<String, Function> functions = new HashMap<>();
functions.put("upper", args -> String.valueOf(args[0]).toUpperCase());
functions.put("add", args -> {
    double a = ((Number) args[0]).doubleValue();
    double b = ((Number) args[1]).doubleValue();
    return a + b;
});

Template template = new Template("demo", functions);
```

函数执行失败在执行期间包装在 `TemplateExecutionException` 中。

## 模板集

照常在模板文本中使用 `define` 和 `template`。解析后，通过内省 API 检查模板集。

### 定义模板

```gotemplate
{{define "header"}}
<header>
  <h1>{{.Title}}</h1>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>&copy; 2026</p>
</footer>
{{end}}
```

### 使用模板

```gotemplate
{{template "header" .}}
<main>Content here</main>
{{template "footer" .}}
```

### 内省 API

解析后，检查模板集：

```java
Template template = new Template("base");
template.parseFiles(Paths.get("base.tmpl"), Paths.get("partials.tmpl"));

// 获取模板名称
String name = template.name();  // "base"

// 列出所有已定义的模板
Set<String> names = template.definedTemplates();  // ["base", "header", "footer"]

// 检查模板是否存在
boolean hasHeader = template.hasTemplate("header");  // true

// 查找特定模板（返回独立副本）
Template headerTemplate = template.lookup("header");

// 将所有模板作为 Map 获取
Map<String, Template> all = template.templates();
```

`lookup` 和 `templates` 返回独立的模板副本，因此调用者不会意外修改原始的已解析集。

---

## 应避免的 Go 专属 API

这些 Go 功能在 gotemplate4j 中没有直接等价物。

### 文件系统操作

**Go：**
```go
tmpl, err := template.ParseFS(os.DirFS("templates"), "*.tmpl")
```

**gotemplate4j：**
```java
// 改用 Java IO
Template template = new Template("demo");
template.parseFile(Paths.get("templates/demo.tmpl"));
template.parseFiles(Paths.get("templates/base.tmpl"), Paths.get("templates/page.tmpl"));
template.parseGlob(Paths.get("templates/*.tmpl"));
```

### 通道和迭代器

**Go：**
```go
// 在通道上 range
{{range $item := .Channel}}
  {{$item}}
{{end}}

// 在整数上 range
{{range $i := 5}}
  {{$i}}
{{end}}
```

**gotemplate4j：**
```java
// 先转换为集合
List<Integer> numbers = Arrays.asList(0, 1, 2, 3, 4);
data.put("numbers", numbers);
```

```gotemplate
{{range $i := .numbers}}
  {{$i}}
{{end}}
```

### 带参方法调用

**Go：**
```go
// 可以调用带参数的方法
type User struct { Name string }
func (u User) Greet(greeting string) string {
    return greeting + ", " + u.Name
}
```

```gotemplate
{{.User.Greet "Hello"}}  // 在 Go 中有效
```

**gotemplate4j：**
```java
// 不支持 - 改用自定义函数
Function greet = args -> {
    User user = (User) args[0];
    String greeting = (String) args[1];
    return greeting + ", " + user.getName();
};

Map<String, Function> functions = new HashMap<>();
functions.put("greet", greet);
```

```gotemplate
{{greet .User "Hello"}}  // 改用函数
```

### 函数值

**Go：**
```go
// 可以在结构体字段或映射中存储函数
type Data struct {
    Transform func(string) string
}
```

```gotemplate
{{call .Transform "input"}}
```

**gotemplate4j：**
```java
// 使用 Function 接口
Function transform = args -> ((String) args[0]).toUpperCase();
data.put("transform", transform);
```

```gotemplate
{{call .transform "input"}}  // 适用于 Function 实例
```

---

## 常见迁移模式

### 模式 1：简单模板

**Go：**
```go
tmpl, _ := template.New("demo").Parse("Hello, {{.Name}}!")
var buf bytes.Buffer
tmpl.Execute(&buf, map[string]string{"Name": "Alice"})
```

**gotemplate4j：**
```java
Template template = new Template("demo");
template.parse("Hello, {{.Name}}!");

StringWriter writer = new StringWriter();
Map<String, String> data = new HashMap<>();
data.put("Name", "Alice");
template.execute(writer, data);
```

### 模式 2：带函数的模板

**Go：**
```go
funcMap := template.FuncMap{
    "upper": strings.ToUpper,
}
tmpl := template.New("demo").Funcs(funcMap)
tmpl.Parse("{{upper .Name}}")
```

**gotemplate4j：**
```java
Map<String, Function> functions = new HashMap<>();
functions.put("upper", args -> ((String) args[0]).toUpperCase());

Template template = new Template("demo", functions);
template.parse("{{upper .Name}}");
```

### 模式 3：模板文件

**Go：**
```go
tmpl, _ := template.ParseFiles("base.tmpl", "page.tmpl")
```

**gotemplate4j：**
```java
Template template = new Template("base");
template.parseFiles(
    Paths.get("base.tmpl"),
    Paths.get("page.tmpl")
);
```

### 模式 4：错误处理

**Go：**
```go
tmpl, err := template.New("demo").Parse(text)
if err != nil {
    log.Fatal(err)
}

err = tmpl.Execute(&buf, data)
if err != nil {
    log.Fatal(err)
}
```

**gotemplate4j：**
```java
try {
    Template template = new Template("demo");
    template.parse(text);

    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateParseException e) {
    // 处理解析错误
    e.printStackTrace();
} catch (TemplateExecutionException e) {
    // 处理执行错误
    e.printStackTrace();
}
```

---

## 测试你的迁移

### 步骤 1：验证语法

测试所有模板是否正确解析：

```java
@Test
void testTemplateParsing() {
    Template template = new Template("test");
    assertDoesNotThrow(() -> template.parse(templateText));
}
```

### 步骤 2：比较输出

将相同的数据通过 Go 和 Java 模板运行，并比较输出：

```java
@Test
void testOutputMatches() throws Exception {
    Map<String, Object> data = createTestData();

    // 执行 gotemplate4j
    Template template = new Template("test");
    template.parse(templateText);
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    String javaOutput = writer.toString();

    // 与预期输出（来自 Go）比较
    assertEquals(expectedGoOutput, javaOutput);
}
```

### 步骤 3：测试边缘情况

测试 null 值、空集合、缺失键：

```java
@Test
void testNullHandling() throws Exception {
    Template template = new Template("test");
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    template.parse("{{if .Value}}{{.Value}}{{else}}empty{{end}}");

    Map<String, Object> data = new HashMap<>();
    // Value 缺失

    assertThrows(TemplateExecutionException.class, () -> {
        template.execute(new StringWriter(), data);
    });
}
```

### 步骤 4：性能测试

对模板执行进行基准测试：

```java
@Benchmark
public void benchmarkTemplateExecution() throws Exception {
    Template template = new Template("test");
    template.parse(templateText);

    for (int i = 0; i < 1000; i++) {
        StringWriter writer = new StringWriter();
        template.execute(writer, testData);
    }
}
```

---

## 参见

- [兼容性指南](compatibility.md) - 详细的兼容性信息
- [用户指南](user-guide/) - 全面的使用文档
- [示例](examples/) - 实际代码示例
- [API 参考](api-reference/) - 详细的 API 文档
