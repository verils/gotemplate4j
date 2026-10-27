# 函数指南

gotemplate4j 提供 Go 兼容的模板函数，并支持自定义 Java 函数。函数名称区分大小写。

## 函数调用语法

```gotemplate
{{functionName arg1 arg2}}
{{value | functionName arg}}
```

管道结果作为最后一个参数传入。内置 `default` 是例外：管道值是被检查的值。

## 内置函数注册表

默认注册表只包含以下函数：

| 类别 | 函数 |
| --- | --- |
| 比较 | `eq`、`ne`、`lt`、`le`、`gt`、`ge` |
| 逻辑 | `and`、`or`、`not` |
| 集合 | `len`、`index`、`slice` |
| 格式化 | `print`、`printf`、`println` |
| 转义 | `html`、`js`、`urlquery` |
| 调用 | `call` |
| gotemplate4j 扩展 | `deepEqual`、`typeof`、`kindOf`、`default` |

`Functions.BUILTIN` 以不可修改 map 暴露该注册表。向 `Template` 构造函数传入同名自定义
函数时，仅覆盖该模板实例中的内置函数。

## 比较函数

`eq` 至少接收两个参数，第一个值等于后续任意值时返回 true。`ne`、`lt`、`le`、`gt`、
`ge` 必须恰好接收两个参数。

```gotemplate
{{eq .Status "active" "pending"}}
{{if lt .Age 18}}未成年人{{else}}成年人{{end}}
```

整数与整数、小数与小数、字符串与字符串可以比较。布尔值支持 `eq` 和 `ne`。混用不兼容
类型会抛出模板执行异常。数组、集合和 map 不能直接比较，应使用 `deepEqual`。

## 逻辑函数

`and` 和 `or` 与 Go 一样短路并返回操作数，`not` 返回布尔值。

```gotemplate
{{and .Active .Verified}}
{{or .Admin .Moderator}}
{{not .Deleted}}
```

`if`、`with`、`and`、`or`、`not` 和 `default` 共享同一真值规则：`null`、`false`、
数值零、空字符串、空数组、空集合、空 map 和空 `Optional` 为假；其他值为真。

## 集合函数

### len

返回字符串、数组、集合或 map 的长度。

```gotemplate
{{len .Items}}
{{len "hello"}}
```

### index

索引数组、列表、有序集合、字符串和 map。后续参数继续索引嵌套容器。

```gotemplate
{{index .Items 0}}
{{index .Matrix 1 2}}
{{index .Users 0 "Name"}}
```

序列下标必须是整型且不能越界。map 缺键遵循当前 `MissingKeyPolicy`。

### slice

支持数组、列表和字符串的零到三个下标。

```gotemplate
{{slice .Items}}
{{slice .Items 1}}
{{slice "hello" 1 4}}
{{slice .Items 1 4 5}}
```

负数、反向和越界区间会抛错。字符串不支持三下标形式。

## 格式化

`print` 使用 Go `Sprint` 的空格规则。`println` 始终用空格分隔参数并追加换行。
`printf` 支持 `%v`、`%T`、`%t`、`%q`、`%d`、`%s`、`%x`、`%X`、`%f` 和 `%%`，
以及常用 flags、十进制宽度和精度。

```gotemplate
{{print "hello" "world"}}
{{println 1 2 3}}
{{printf "%.2f" .Price}}
{{printf "%05d" 42}}
```

`%T` 输出 Java 完全限定类名，这是有意的 Java 差异。动态宽度/精度和参数重排不受支持，
会抛出明确执行异常。

## 转义

`html`、`js` 和 `urlquery` 接受零个或多个参数，并使用 Go `Sprint` 空格规则拼接。

```gotemplate
{{html "<b>"}}
{{js "</script>"}}
{{urlquery "a b"}}
```

`js` 是字符串转义函数，不是上下文感知的 JavaScript 自动转义。应确保结果位于正确
引用的 JavaScript 字符串中。

## 自省与扩展函数

### deepEqual

递归比较数组、map、集合和嵌套容器。每一层都要求相同的具体运行时类型。List 和数组保持
顺序，Set 按元素匹配，Map 比较键和值，并支持循环容器。普通非容器对象使用 `equals`。

```gotemplate
{{if deepEqual .Expected .Actual}}相等{{else}}不同{{end}}
```

### typeof 和 kindOf

`typeof` 返回 Java 类名或 `nil`。`kindOf` 返回 `array`、`slice`、`map`、`struct`、
`bool`、`int`、`string` 或 `invalid` 等宽泛类型。

```gotemplate
{{typeof .Value}}
{{kindOf .Value}}
```

### default

被检查值按模板真值规则为假时返回回退值。

```gotemplate
{{default .Name "匿名"}}
{{.Name | default "匿名"}}
```

### call

调用模板数据中提供的 `Function` 值。

```gotemplate
{{call .Formatter .Value}}
```

## 自定义函数

通过 `Template` 构造函数注册函数。函数接收已求值参数组成的 `Object[]`，可返回任意对象。

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

Map<String, Function> functions = new HashMap<>();
functions.put("double", args -> ((Number) args[0]).longValue() * 2);
functions.put("greet", args -> "你好, " + args[0] + "!");

Template template = new Template("demo", functions);
template.parse("{{greet (.Name | default \"world\")}}; double={{double 5}}");

StringWriter writer = new StringWriter();
template.execute(writer, new HashMap<String, Object>());
```

自定义函数可以覆盖当前模板的内置函数：

```java
Map<String, Function> functions = new HashMap<>();
functions.put("len", args -> 42);
Template template = new Template("custom-len", functions);
```

自定义函数应校验参数数量和类型，错误信息应可操作，并避免在异常消息中返回敏感值。

## 问题排查

- `undefined function` 表示函数名拼写错误，或没有传入模板构造函数。
- 参数数量和类型错误包装为 `TemplateExecutionException`，并包含紧凑的参数类型摘要。
- 缺失命名模板使用 `TemplateNotFoundException`。
- 复杂数字面量暂不支持，会在解析阶段报错。
