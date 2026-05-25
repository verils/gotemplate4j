# 模板语法参考

本指南提供 gotemplate4j 模板语法的完整参考，与 Go 的 `text/template` 包兼容。

## 概述

gotemplate4j 模板是带有嵌入动作的文本文件。动作以双花括号 `{{` 和 `}}` 包裹。

### 基本结构

```gotemplate
静态文本 {{action}} 更多静态文本 {{another action}}
```

动作在模板执行期间被求值并替换为结果。

## 分隔符

### 标准分隔符

默认分隔符为 `{{` 和 `}}`：

```gotemplate
{{.Name}}
```

### 自定义分隔符

创建模板时可以更改分隔符：

```java
Template template = new Template("demo", "{{%", "%}}");
template.parse("<% .Name %>");
```

### 空白裁剪

使用 `-` 来裁剪空白：

```gotemplate
{{- .Name -}}
```

- `{{-` 裁剪动作之前的空白
- `-}}` 裁剪动作之后的空白

示例：

```gotemplate
Hello   {{- .Name -}}   !
```

假设 `.Name = "Alice"`，输出为：`HelloAlice!`（空格被移除）

## 注释

注释不会出现在输出中：

```gotemplate
{{/* 这是一条注释 */}}
```

多行注释：

```gotemplate
{{/*
  这是一条
  多行注释
*/}}
```

行内注释：

```gotemplate
Hello {{.Name}} {{/* 显示名称 */}}
```

## 变量

### 点号（`.`）

点号表示当前数据上下文：

```gotemplate
{{.}}           <!-- 整个数据 -->
{{.Name}}       <!-- Name 字段 -->
{{.User.Age}}   <!-- 嵌套字段 -->
```

### 变量赋值

使用 `:=` 创建变量：

```gotemplate
{{$name := .Name}}
{{$count := len .Items}}
```

变量以 `$` 开头，作用域限定在其所属的块中：

```gotemplate
{{range .Items}}
  {{$item := .}}  <!-- $item 的作用域限定在此 range 块中 -->
{{end}}
<!-- $item 在此处不可访问 -->
```

### 多重赋值

一次分配多个变量：

```gotemplate
{{$index, $value := range .Items}}
```

## 管道

管道使用 `|` 串联操作：

```gotemplate
{{.Name | upper}}
{{.Text | lower | trim}}
{{printf "Hello, %s!" .Name}}
```

### 管道规则

1. 数据从左到右流动
2. 每个函数接收前一个输出作为最后一个参数
3. 最终结果被渲染

示例：

```gotemplate
<!-- 以下两种写法等价 -->
{{upper .Name}}
{{.Name | upper}}

<!-- 链式调用 -->
{{.Text | trim | upper | printf "Result: %s"}}
```

### 括号

使用括号来提高清晰度或覆盖优先级：

```gotemplate
{{printf "%s (%d)" (.Name | upper) (len .Items)}}
```

## 条件动作

### If 语句

基本 if：

```gotemplate
{{if .IsActive}}
  用户已激活
{{end}}
```

If/Else：

```gotemplate
{{if .IsActive}}
  已激活
{{else}}
  未激活
{{end}}
```

If/Else If/Else：

```gotemplate
{{if eq .Status "active"}}
  已激活
{{else if eq .Status "pending"}}
  待处理
{{else}}
  未知
{{end}}
```

### 真值判断

以下值被视为 **false**：
- `false`（布尔值）
- `0`（任何数值类型）
- `null` 或缺失
- 空字符串 `""`
- 空集合（长度为 0）

所有其他值视为 **true**。

示例：

```gotemplate
{{if 0}}假{{end}}        <!-- 不会输出 -->
{{if 1}}真{{end}}         <!-- 会输出 -->
{{if ""}}假{{end}}       <!-- 不会输出 -->
{{if "x"}}真{{end}}       <!-- 会输出 -->
{{if nil}}假{{end}}      <!-- 不会输出 -->
```

### 比较运算符

| 运算符 | 含义 | 示例 |
|--------|------|------|
| `eq` | 等于 | `{{eq .A .B}}` |
| `ne` | 不等于 | `{{ne .A .B}}` |
| `lt` | 小于 | `{{lt .A .B}}` |
| `lte` | 小于等于 | `{{lte .A .B}}` |
| `gt` | 大于 | `{{gt .A .B}}` |
| `gte` | 大于等于 | `{{gte .A .B}}` |

示例：

```gotemplate
{{if eq .Age 18}}正好18岁{{end}}
{{if gt .Age 18}}成年人{{end}}
{{if lte .Score 100}}有效分数{{end}}
```

### 逻辑运算符

| 运算符 | 含义 | 示例 |
|--------|------|------|
| `and` | 逻辑与 | `{{and .A .B}}` |
| `or` | 逻辑或 | `{{or .A .B}}` |
| `not` | 逻辑非 | `{{not .Flag}}` |

示例：

```gotemplate
{{if and .IsActive .IsVerified}}已激活且已验证{{end}}
{{if or .IsAdmin .IsModerator}}有权限{{end}}
{{if not .IsDeleted}}未删除{{end}}
```

短路求值：

```gotemplate
<!-- 如果 .A 为 false，则 .B 不会被求值 -->
{{if and .A .B}}...{{end}}

<!-- 如果 .A 为 true，则 .B 不会被求值 -->
{{if or .A .B}}...{{end}}
```

## Range 动作

遍历集合（数组、列表、映射、信道）。

### 基本 Range

```gotemplate
{{range .Items}}
  {{.}}
{{end}}
```

### 带索引

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

### 遍历 Map

```gotemplate
{{range $key, $value := .MapData}}
  {{$key}}: {{$value}}
{{end}}
```

注意：除非启用 map 键排序，否则 map 迭代顺序不保证。

### 遍历整数（Go 兼容）

从 0 迭代到 n-1：

**单变量形式**：
```gotemplate
{{range $i := 5}}
  迭代 {{$i}}
{{end}}
```

输出：
```
迭代 0
迭代 1
迭代 2
迭代 3
迭代 4
```

**双变量形式**（v0.9.0+）：

也可以使用双变量，此时索引和值相同：

```gotemplate
{{range $index, $value := 3}}
  索引: {{$index}}, 值: {{$value}}
{{end}}
```

输出：
```
索引: 0, 值: 0
索引: 1, 值: 1
索引: 2, 值: 2
```

这与 Go 的 `text/template` 对整数 range 的行为一致。

### 空集合

如果集合为空或 nil，range 体不会执行：

```gotemplate
{{range .Items}}
  有项目
{{else}}
  无项目
{{end}}
```

### Break 和 Continue

控制循环执行：

```gotemplate
{{range .Items}}
  {{if eq . "stop"}}
    {{break}}
  {{end}}
  {{.}}
{{end}}
```

```gotemplate
{{range .Items}}
  {{if eq . "skip"}}
    {{continue}}
  {{end}}
  {{.}}
{{end}}
```

## With 动作

更改点号上下文：

```gotemplate
{{with .User}}
  姓名: {{.Name}}
  年龄: {{.Age}}
  城市: {{.Address.City}}
{{end}}
```

等价于：

```gotemplate
{{if .User}}
  姓名: {{.User.Name}}
  年龄: {{.User.Age}}
  城市: {{.User.Address.City}}
{{end}}
```

### With/Else

```gotemplate
{{with .User}}
  {{.Name}}
{{else}}
  无用户
{{end}}
```

## 模板动作

### Define

定义命名模板：

```gotemplate
{{define "header"}}
<html>
<head><title>{{.Title}}</title></head>
{{end}}
```

定义的模板在被调用之前不会产生输出。

### Template

调用已定义的模板：

```gotemplate
{{template "header" .}}
```

语法：
- `{{template "name"}}` - 使用当前上下文调用
- `{{template "name" .}}` - 使用显式上下文调用
- `{{template "name" $data}}` - 使用变量调用

### Block

定义并内联执行（Go 兼容）：

```gotemplate
{{block "content" .}}默认内容{{end}}
```

等价于：

```gotemplate
{{define "content"}}默认内容{{end}}
{{template "content" .}}
```

Block 可以被后续的定义覆盖。

## 函数调用

使用参数调用函数：

```gotemplate
{{len .Items}}
{{upper .Name}}
{{add 1 2}}
{{printf "%s 今年 %d 岁" .Name .Age}}
```

### 内置函数

完整列表请参阅[函数指南](functions.md)。

常用函数：

| 类别 | 函数 |
|------|------|
| 字符串 | `upper`、`lower`、`trim`、`len`、`replace` |
| 数学 | `add`、`sub`、`mul`、`div`、`mod` |
| 逻辑 | `and`、`or`、`not`、`eq`、`ne`、`lt`、`gt` |
| 格式化 | `printf`、`print`、`println` |
| 类型 | `typeof`、`kindof` |

### 自定义函数

注册自定义函数：

```java
Map<String, Function> functions = new HashMap<>();
functions.put("double", args -> {
    int value = (Integer) args[0];
    return value * 2;
});

Template template = new Template("demo", functions);
template.parse("Double: {{double 5}}");
```

## 字符串字面量

使用引号表示字符串字面量：

```gotemplate
{{"hello"}}
{{'world'}}
```

转义字符：

```gotemplate
{{"line1\nline2"}}    <!-- 换行符 -->
{{"tab\there"}}       <!-- 制表符 -->
{{"quote: \"hi\""}}   <!-- 引号 -->
{{"backslash: \\"}}   <!-- 反斜杠 -->
```

## 数字字面量

整数：

```gotemplate
{{42}}
{{-10}}
{{0}}
```

浮点数：

```gotemplate
{{3.14}}
{{-0.5}}
{{1.0e10}}
```

## 布尔字面量

```gotemplate
{{true}}
{{false}}
```

## Nil

表示 null/nil：

```gotemplate
{{nil}}
```

## 点号节点

访问当前上下文：

```gotemplate
{{.}}
```

用于传递整个上下文：

```gotemplate
{{template "partial" .}}
```

## 字段访问

访问对象字段或 map 键：

```gotemplate
{{.Name}}
{{.User.Age}}
{{.Address.City}}
```

对于 JavaBean，会调用 getter 方法：
- `.Name` 对应 `getName()`
- `.IsActive` 对应 `isActive()` 或 `getIsActive()`

对于 Map，访问键值：
- `.Name` 对应 `map.get("Name")`

## 方法调用（受限）

调用零参数方法：

```gotemplate
{{.GetFullName}}
{{.ToString}}
```

注意：出于安全原因，不支持带参数的方法。

## 示例

### 完整模板

```gotemplate
{{/* 用户资料模板 */}}
<html>
<head>
  <title>{{.Title | default "资料"}}</title>
</head>
<body>
  <h1>{{.User.Name | upper}}</h1>
  
  {{with .User}}
  <div class="profile">
    <p>姓名: {{.Name}}</p>
    <p>年龄: {{.Age}}</p>
    <p>邮箱: {{.Email}}</p>
    
    {{if .Addresses}}
    <h2>地址</h2>
    <ul>
    {{range $i, $addr := .Addresses}}
      <li>
        {{$addr.Street}}, {{$addr.City}}
        {{if $addr.IsPrimary}}（主要）{{end}}
      </li>
    {{end}}
    </ul>
    {{else}}
    <p>没有保存的地址。</p>
    {{end}}
  </div>
  {{end}}
  
  {{template "footer" .}}
</body>
</html>
```

### 数据

```java
Map<String, Object> data = new HashMap<>();
data.put("Title", "用户资料");

Map<String, Object> user = new HashMap<>();
user.put("Name", "Alice");
user.put("Age", 30);
user.put("Email", "alice@example.com");

List<Map<String, Object>> addresses = new ArrayList<>();
Map<String, Object> addr1 = new HashMap<>();
addr1.put("Street", "123 Main St");
addr1.put("City", "北京");
addr1.put("IsPrimary", true);
addresses.add(addr1);

user.put("Addresses", addresses);
data.put("User", user);
```

## 语法错误

常见语法错误：

### 缺少 End

```gotemplate
{{if .Active}}
  已激活
<!-- 缺少 {{end}} -->
```

错误：`unexpected EOF, expected end`

### 未闭合的动作

```gotemplate
{{.Name
```

错误：`unclosed action`

### 无效的管道

```gotemplate
{{.Name | | upper}}
```

错误：`missing pipeline operand`

## 最佳实践

### 1. 使用描述性的变量名

❌ **不好：**
```gotemplate
{{range $x := .Items}}{{$x}}{{end}}
```

✅ **好：**
```gotemplate
{{range $item := .Items}}{{$item}}{{end}}
```

### 2. 保持条件简单

将复杂逻辑移到 Java 代码中。

❌ **不好：**
```gotemplate
{{if and (or .A .B) (and (not .C) .D)}}...{{end}}
```

✅ **好：**
```java
data.put("ShouldShow", calculateCondition());
```
```gotemplate
{{if .ShouldShow}}...{{end}}
```

### 3. 处理缺失值

```gotemplate
{{if .Name}}{{.Name}}{{else}}匿名{{end}}
```

或者使用 default 函数：

```gotemplate
{{.Name | default "匿名"}}
```

### 4. 明智地使用空白裁剪

只有在需要干净输出时才进行裁剪。

### 5. 为复杂逻辑添加注释

```gotemplate
{{/* 仅为活跃的高级用户显示高级徽章 */}}
{{if and .IsActive .IsPremium}}
  <span class="badge">高级</span>
{{end}}
```

## 速查卡

### 动作

| 动作 | 语法 |
|--------|--------|
| 变量 | `{{.Field}}` |
| If | `{{if COND}}...{{end}}` |
| If/Else | `{{if COND}}...{{else}}...{{end}}` |
| Range | `{{range COLLECTION}}...{{end}}` |
| With | `{{with VALUE}}...{{end}}` |
| Define | `{{define "NAME"}}...{{end}}` |
| Template | `{{template "NAME" DATA}}` |
| Block | `{{block "NAME" DATA}}...{{end}}` |
| 注释 | `{{/* COMMENT */}}` |

### 运算符

| 类型 | 运算符 |
|------|-----------|
| 比较 | `eq`、`ne`、`lt`、`lte`、`gt`、`gte` |
| 逻辑 | `and`、`or`、`not` |
| 算术 | `add`、`sub`、`mul`、`div`、`mod` |

### 特殊字符

| 字符 | 含义 |
|-----------|---------|
| `.` | 当前上下文 |
| `$` | 变量前缀 |
| `\|` | 管道运算符 |
| `:=` | 变量赋值 |
| `-` | 空白裁剪 |

---

**下一步：**
- 🔧 了解[内置函数](functions.md)
- 📊 理解[数据模型](data-models.md)
- 🎯 深入探索[控制流](control-flow.md)
