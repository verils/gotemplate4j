# 基本概念

本指南解释 gotemplate4j 的基本概念，帮助你理解模板的工作原理。

## 什么是模板？

模板是一个嵌入了表达式的文本文档，其中的表达式在执行时被求值并替换为实际值。模板将展现逻辑与数据分离，使生成动态内容变得更加容易。

### 模板结构

模板由两种类型的内容组成：

1. **静态文本**：原样出现在输出中的普通文本
2. **动作**：用 `{{` 和 `}}` 括起来的、需要求值的表达式

```gotemplate
Hello, {{.Name}}!  <!-- "Hello, " 是静态文本，"{{.Name}}" 是一个动作 -->
```

## 数据上下文（点号）

点号（`.`）代表当前的数据上下文。这是 gotemplate4j 中最重要的概念。

### 访问字段

使用点号后跟字段名来访问数据：

```java
// Java 代码
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");
data.put("Age", 30);

Template template = new Template("demo");
template.parse("姓名：{{.Name}}，年龄：{{.Age}}");
```

**输出：**
```
姓名：Alice，年龄：30
```

### 嵌套数据

使用点号记法访问嵌套字段：

```java
Map<String, Object> user = new HashMap<>();
user.put("Name", "Bob");

Map<String, Object> address = new HashMap<>();
address.put("City", "北京");
user.put("Address", address);

template.parse("{{.Name}} 住在 {{.Address.City}}");
```

**输出：**
```
Bob 住在 北京
```

### 点号在不同上下文中的变化

`.` 的值根据上下文的不同而变化：

```gotemplate
{{range .Items}}
  {{.}}  <!-- 这里，. 代表循环中的当前项 -->
{{end}}
```

## 动作

动作是用 `{{` 和 `}}` 括起来的表达式，它们控制输出中出现的内容。

### 变量替换

最简单的动作输出一个值：

```gotemplate
{{.Name}}
{{.Age}}
{{.Price}}
```

### 条件动作

使用 `if`/`else`/`end` 进行条件逻辑：

```gotemplate
{{if .IsActive}}
  用户处于活跃状态
{{else}}
  用户处于非活跃状态
{{end}}
```

### 循环动作

使用 `range` 来迭代集合：

```gotemplate
{{range .Items}}
  - {{.}}
{{end}}
```

你也可以同时获取索引和值：

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

**整数范围迭代（v0.9.0+）**：

你可以使用整数范围来迭代数字序列：

```gotemplate
{{range $i := 5}}
  索引：{{$i}}  <!-- 输出：0, 1, 2, 3, 4 -->
{{end}}
```

这对于生成重复内容或创建编号列表非常有用。

### With 动作

`with` 动作改变点号上下文：

```gotemplate
{{with .User}}
  姓名：{{.Name}}
  年龄：{{.Age}}
{{end}}
```

在 `with` 块内部，`.` 指的是 `.User` 而不是根数据。

## 管道

管道使用管道操作符（`|`）将操作串联起来：

```gotemplate
{{.Name | upper}}           <!-- 转换为大写 -->
{{.Text | lower | trim}}    <!-- 串联多个操作 -->
{{printf "%.2f" .Price}}    <!-- 格式化数字 -->
```

### 管道的工作原理

1. 值从左到右传递
2. 每个函数接收前一步的输出
3. 最终结果被渲染

```gotemplate
{{"hello" | upper | printf "问候：%s"}}
<!-- 输出：问候：HELLO -->
```

## 函数

函数对数据执行操作。gotemplate4j 包含许多内置函数。

### 使用函数

调用带参数的函数：

```gotemplate
{{len .Items}}              <!-- 获取长度 -->
{{upper .Name}}             <!-- 转换为大写 -->
{{add 1 2}}                 <!-- 数字相加 -->
```

### 常用内置函数

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `len` | 获取长度 | `{{len .Items}}` |
| `upper` | 转大写 | `{{upper .Text}}` |
| `lower` | 转小写 | `{{lower .Text}}` |
| `trim` | 去除空白 | `{{trim .Text}}` |
| `add` | 加法 | `{{add 1 2}}` |
| `sub` | 减法 | `{{sub 5 3}}` |
| `printf` | 格式化字符串 | `{{printf "%d" .Num}}` |
| `eq` | 等于 | `{{eq .A .B}}` |
| `ne` | 不等于 | `{{ne .A .B}}` |
| `and` | 逻辑与 | `{{and .A .B}}` |
| `or` | 逻辑或 | `{{or .A .B}}` |
| `not` | 逻辑非 | `{{not .Flag}}` |

参阅[函数指南](../user-guide/functions.md)获取完整参考。

## 模板集

模板可以包含多个相互引用的命名模板。

### 定义模板

使用 `define` 创建命名模板：

```gotemplate
{{define "header"}}
<html>
<head><title>{{.Title}}</title></head>
<body>
{{end}}

{{define "footer"}}
</body>
</html>
{{end}}
```

### 调用模板

使用 `template` 调用已定义的模板：

```gotemplate
{{template "header" .}}
<h1>你好，{{.Name}}！</h1>
{{template "footer" .}}
```

### Block 模板

使用 `block` 定义并内联执行（与 Go 兼容）：

```gotemplate
{{block "content" .}}默认内容{{end}}
```

参阅[模板集指南](../user-guide/template-sets.md)获取详情。

## 注释

使用 `{{/*` 和 `*/}}` 添加注释：

```gotemplate
{{/* 这是一个注释 */}}
Hello, {{.Name}}! {{/* 行内注释 */}}
```

注释不会出现在输出中。

## 空白控制

默认情况下，动作保留周围的空白。使用 `-` 来修剪空白：

```gotemplate
{{- if .Show -}}
  可见
{{- end -}}
```

`-` 会移除动作那侧的空白。

## 错误处理

模板在解析或执行过程中可能会失败。

### 解析错误

当模板语法无效时发生：

```java
try {
    template.parse("{{invalid syntax");
} catch (TemplateParseException e) {
    System.err.println("解析错误：" + e.getMessage());
}
```

### 执行错误

当模板以有问题数据运行时发生：

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("执行错误：" + e.getMessage());
}
```

参阅[错误处理指南](../user-guide/error-handling.md)获取最佳实践。

## 数据类型

gotemplate4j 可操作各种 Java 数据类型：

### 支持的类型

- **字符串**：`"hello"`
- **数字**：`int`、`long`、`float`、`double`、`BigInteger`、`BigDecimal`
- **布尔值**：`true`、`false`
- **集合**：`List`、`Set`、`Array`、`Map`
- **JavaBean**：带有 getter 方法的对象
- **枚举**：枚举常量
- **Optional**：`Optional<T>` 值
- **Null**：根据 MissingKeyPolicy 处理

### JavaBean

公共字段和 getter 方法是可访问的：

```java
public class User {
    private String name;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// 在模板中：{{.Name}} 调用 getName()
```

### Map

Map 的键像字段一样访问：

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");

// 在模板中：{{.Name}} 访问 map 的键 "Name"
```

## 执行流程

理解模板的执行方式有助于调试问题：

1. **解析阶段**：模板文本被解析为抽象语法树（AST）
2. **执行阶段**：使用数据上下文遍历 AST
3. **渲染阶段**：动作产生输出文本

```java
Template template = new Template("demo");
template.parse("Hello, {{.Name}}!");  // 解析阶段

StringWriter writer = new StringWriter();
template.execute(writer, data);       // 执行 + 渲染阶段
```

## 最佳实践

### 1. 保持模板简单

避免在模板中使用复杂逻辑，将业务逻辑放在 Java 代码中。

❌ **不好的做法：**
```gotemplate
{{if and (gt .Age 18) (eq .Status "active") (or .Premium .VIP)}}
```

✅ **好的做法：**
```java
data.put("CanAccess", user.isEligible());
```
```gotemplate
{{if .CanAccess}}
```

### 2. 使用有意义的名称

清晰地命名变量和模板：

❌ **不好的做法：**
```gotemplate
{{range .X}}...{{end}}
```

✅ **好的做法：**
```gotemplate
{{range .Users}}...{{end}}
```

### 3. 处理缺失值

配置如何处理缺失的值：

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);  // 缺失键时报错
```

### 4. 用各种数据进行测试

用以下数据测试模板：
- 空集合
- Null 值
- 边界情况（零、负数）
- 大数据集

### 5. 复用模板

定义可复用的组件：

```gotemplate
{{define "user-card"}}
<div class="card">
  <h3>{{.Name}}</h3>
  <p>{{.Email}}</p>
</div>
{{end}}

{{range .Users}}
  {{template "user-card" .}}
{{end}}
```

## 下一步

现在你已经理解了基本概念：

- 📚 阅读[模板语法指南](../user-guide/template-syntax.md)获取详细的语法参考
- 🔧 探索[函数](../user-guide/functions.md)了解所有可用的函数
- 💡 查看[示例](../examples/basic-examples.md)获取实用用例
- 🔍 学习[数据模型](../user-guide/data-models.md)操作 Java 对象

## 快速参考

### 动作类型

| 类型 | 语法 | 示例 |
|------|--------|---------|
| 变量 | `{{.Field}}` | `{{.Name}}` |
| If | `{{if ...}}{{end}}` | `{{if .Active}}是{{end}}` |
| Range | `{{range ...}}{{end}}` | `{{range .Items}}{{.}}{{end}}` |
| With | `{{with ...}}{{end}}` | `{{with .User}}{{.Name}}{{end}}` |
| Template | `{{template ...}}` | `{{template "header" .}}` |
| Define | `{{define ...}}{{end}}` | `{{define "foo"}}...{{end}}` |
| Block | `{{block ...}}{{end}}` | `{{block "foo" .}}...{{end}}` |

### 管道示例

```gotemplate
{{.Name | upper}}
{{printf "你好，%s！" .Name}}
{{len .Items | printf "数量：%d"}}
```

### 常见模式

```gotemplate
<!-- 默认值 -->
{{if .Name}}{{.Name}}{{else}}匿名{{end}}

<!-- 带索引的循环 -->
{{range $i, $v := .Items}}{{$i}}: {{$v}}{{end}}

<!-- 嵌套条件 -->
{{if .User}}{{if .User.Name}}{{.User.Name}}{{end}}{{end}}
```

---

**需要更多细节？** 继续阅读[用户指南](../user-guide/template-syntax.md)获取全面的文档。
