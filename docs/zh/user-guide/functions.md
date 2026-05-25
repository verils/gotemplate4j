# 函数指南

本指南涵盖 gotemplate4j 中的所有内置函数以及如何创建自定义函数。

## 概述

函数在模板中对数据执行操作。gotemplate4j 包含许多内置函数，并允许你注册自定义函数。

### 函数调用语法

```gotemplate
{{functionName arg1 arg2 arg3}}
```

或使用管道：

```gotemplate
{{arg1 | functionName}}
{{arg1 | functionName arg2}}
```

## 内置函数

### 字符串函数

#### upper

将字符串转换为大写。

```gotemplate
{{upper "hello"}}
<!-- 输出: HELLO -->

{{.Name | upper}}
```

#### lower

将字符串转换为小写。

```gotemplate
{{lower "HELLO"}}
<!-- 输出: hello -->

{{.Name | lower}}
```

#### title

将字符串转换为标题大小写（每个单词首字母大写）。

```gotemplate
{{title "hello world"}}
<!-- 输出: Hello World -->
```

#### trim

移除前导和尾部空白。

```gotemplate
{{trim "  hello  "}}
<!-- 输出: hello -->

{{.Text | trim}}
```

#### trimLeft

移除前导空白。

```gotemplate
{{trimLeft "  hello  "}}
<!-- 输出: hello   -->
```

#### trimRight

移除尾部空白。

```gotemplate
{{trimRight "  hello  "}}
<!-- 输出:   hello -->
```

#### trimPrefix

从字符串中移除前缀。

```gotemplate
{{trimPrefix "Hello" "He"}}
<!-- 输出: llo -->
```

#### trimSuffix

从字符串中移除后缀。

```gotemplate
{{trimSuffix "Hello" "lo"}}
<!-- 输出: Hel -->
```

#### replace

替换子字符串的所有出现。

```gotemplate
{{replace "hello world" "world" "go"}}
<!-- 输出: hello go -->

{{.Text | replace "old" "new"}}
```

#### repeat

将字符串重复 n 次。

```gotemplate
{{repeat "ab" 3}}
<!-- 输出: ababab -->
```

#### len

获取字符串、数组、列表、map 或信道的长度。

```gotemplate
{{len "hello"}}
<!-- 输出: 5 -->

{{len .Items}}
<!-- 输出: 项目数量 -->
```

#### substr

提取子字符串（Go 兼容）。

```gotemplate
{{substr "hello" 1 4}}
<!-- 输出: ell -->
```

### 数学函数

#### add

数字相加。

```gotemplate
{{add 1 2}}
<!-- 输出: 3 -->

{{add 1 2 3 4}}
<!-- 输出: 10 -->

{{add .Count 1}}
```

#### sub

数字相减。

```gotemplate
{{sub 10 3}}
<!-- 输出: 7 -->

{{sub .Total .Used}}
```

#### mul

数字相乘。

```gotemplate
{{mul 3 4}}
<!-- 输出: 12 -->

{{mul .Price .Quantity}}
```

#### div

数字相除。

```gotemplate
{{div 10 2}}
<!-- 输出: 5 -->

{{div .Total .Count}}
```

注意：整数除法会截断。使用浮点数获得小数结果。

#### mod

取模（求余）操作。

```gotemplate
{{mod 10 3}}
<!-- 输出: 1 -->

{{if mod .Index 2 | eq 0}}偶数{{else}}奇数{{end}}
```

#### pow

幂函数（如果可用）。

```gotemplate
{{pow 2 3}}
<!-- 输出: 8 -->
```

### 比较函数

#### eq

等于比较。

```gotemplate
{{eq 1 1}}
<!-- 输出: true -->

{{eq .Status "active"}}
{{if eq .Age 18}}正好18岁{{end}}
```

#### ne

不等于比较。

```gotemplate
{{ne 1 2}}
<!-- 输出: true -->

{{if ne .Status "inactive"}}非不活跃{{end}}
```

#### lt

小于。

```gotemplate
{{lt 1 2}}
<!-- 输出: true -->

{{if lt .Age 18}}未成年人{{end}}
```

#### lte

小于等于。

```gotemplate
{{lte 2 2}}
<!-- 输出: true -->

{{if lte .Score 100}}有效分数{{end}}
```

#### gt

大于。

```gotemplate
{{gt 2 1}}
<!-- 输出: true -->

{{if gt .Age 18}}成年人{{end}}
```

#### gte

大于等于。

```gotemplate
{{gte 2 2}}
<!-- 输出: true -->

{{if gte .Score 60}}及格{{end}}
```

### 逻辑函数

#### and

逻辑与，带短路求值。

```gotemplate
{{and true true}}
<!-- 输出: true -->

{{and true false}}
<!-- 输出: false -->

{{if and .IsActive .IsVerified}}已激活且已验证{{end}}
```

返回第一个假值，如果全部为真则返回最后一个值。

#### or

逻辑或，带短路求值。

```gotemplate
{{or true false}}
<!-- 输出: true -->

{{or false false}}
<!-- 输出: false -->

{{if or .IsAdmin .IsModerator}}有权限{{end}}
```

返回第一个真值，如果全部为假则返回最后一个值。

#### not

逻辑非。

```gotemplate
{{not true}}
<!-- 输出: false -->

{{not false}}
<!-- 输出: true -->

{{if not .IsDeleted}}未删除{{end}}
```

### 格式化函数

#### printf

使用 Go 风格格式说明符格式化字符串。

```gotemplate
{{printf "Hello, %s!" "World"}}
<!-- 输出: Hello, World! -->

{{printf "%d 个项目" 5}}
<!-- 输出: 5 个项目 -->

{{printf "%.2f" 3.14159}}
<!-- 输出: 3.14 -->

{{printf "%s 今年 %d 岁" .Name .Age}}
```

常用格式说明符：
- `%s` - 字符串
- `%d` - 整数
- `%f` - 浮点数
- `%.2f` - 保留2位小数的浮点数
- `%t` - 布尔值
- `%v` - 任意值

#### print

将参数转换为字符串并连接。

```gotemplate
{{print "Hello" " " "World"}}
<!-- 输出: Hello World -->

{{print .Name " (" .Age ")"}}
```

#### println

类似于 print，但在末尾添加换行符。

```gotemplate
{{println "Hello" "World"}}
<!-- 输出: Hello World\n -->
```

### 类型函数

#### typeof

获取值的类型名。

```gotemplate
{{typeof .Name}}
<!-- 输出: java.lang.String（或类似） -->
```

#### kindof

获取值的种类（Go 兼容）。

```gotemplate
{{kindof .Name}}
<!-- 输出: String -->

{{kindof .Items}}
<!-- 输出: Slice, Map 等 -->
```

### 默认值和验证函数

#### default

如果输入为空或 null，返回默认值。

```gotemplate
{{default .Name "匿名"}}
{{.Name | default "匿名"}}

{{if eq (.Name | default "") ""}}无姓名{{end}}
```

#### index

按索引访问元素（用于数组、列表、map）。

```gotemplate
{{index .Items 0}}
<!-- 第一个项目 -->

{{index .MapData "key"}}
<!-- Map 值 -->

{{index .Matrix 1 2}}
<!-- 多维索引 -->
```

#### slice

从数组或字符串中提取切片。

```gotemplate
{{slice "hello" 1 4}}
<!-- 输出: ell -->

{{slice .Items 0 5}}
<!-- 前5个项目 -->
```

### URL 和 HTML 函数

#### urlquery

对字符串进行 URL 编码。

```gotemplate
{{urlquery "hello world"}}
<!-- 输出: hello+world -->

<a href="/search?q={{urlquery .Query}}">搜索</a>
```

#### html

对字符串进行 HTML 转义。

```gotemplate
{{html "<script>alert('xss')</script>"}}
<!-- 输出: &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt; -->
```

#### js

对字符串进行 JavaScript 转义。

```gotemplate
{{js "hello\nworld"}}
<!-- 输出: hello\nworld（已转义） -->
```

### 日期和时间函数

#### now

获取当前时间。

```gotemplate
{{now}}
<!-- 输出: 当前时间戳 -->
```

#### date

格式化日期/时间（如果可用）。

```gotemplate
{{date "2006-01-02" .Timestamp}}
<!-- 输出: 格式化后的日期 -->
```

注意：日期格式化使用 Go 的参考时间：`Mon Jan 2 15:04:05 MST 2006`

### JSON 函数

#### json

将值编码为 JSON。

```gotemplate
{{json .Data}}
<!-- 输出: JSON 表示 -->

<script>
var data = {{json .Data}};
</script>
```

#### jsonify

json 的别名（Go 兼容）。

```gotemplate
{{jsonify .Data}}
```

## 自定义函数

在创建模板时注册自定义函数。

### 基本自定义函数

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.util.HashMap;
import java.util.Map;

// 创建函数 map
Map<String, Function> functions = new HashMap<>();

// 注册自定义函数
functions.put("double", args -> {
    int value = (Integer) args[0];
    return value * 2;
});

// 使用自定义函数创建模板
Template template = new Template("demo", functions);
template.parse("5 的两倍是 {{double 5}}");

StringWriter writer = new StringWriter();
template.execute(writer, new HashMap<>());
System.out.println(writer.toString());
// 输出: 5 的两倍是 10
```

### 多参数函数

```java
functions.put("multiply", args -> {
    int a = (Integer) args[0];
    int b = (Integer) args[1];
    return a * b;
});

template.parse("{{multiply 3 4}}");
// 输出: 12
```

### 可变参数函数

```java
functions.put("sum", args -> {
    int total = 0;
    for (Object arg : args) {
        total += ((Number) arg).intValue();
    }
    return total;
});

template.parse("{{sum 1 2 3 4 5}}");
// 输出: 15
```

### 返回复杂类型的函数

```java
functions.put("createUser", args -> {
    String name = (String) args[0];
    int age = (Integer) args[1];
    
    Map<String, Object> user = new HashMap<>();
    user.put("Name", name);
    user.put("Age", age);
    return user;
});

template.parse(
    "{{with createUser \"Alice\" 30}}" +
    "{{.Name}} 今年 {{.Age}} 岁" +
    "{{end}}"
);
// 输出: Alice 今年 30 岁
```

### 管道中的函数

```java
functions.put("greet", args -> {
    String name = (String) args[0];
    return "你好, " + name + "!";
});

template.parse("{{.Name | greet}}");
// 数据: {"Name": "Alice"}
// 输出: 你好, Alice!
```

### 访问上下文的函数

函数不能直接访问模板上下文，但可以传递它：

```java
functions.put("fullName", args -> {
    Map<String, Object> user = (Map<String, Object>) args[0];
    String first = (String) user.get("FirstName");
    String last = (String) user.get("LastName");
    return first + " " + last;
});

template.parse("{{fullName .User}}");
```

## 常见模式

### 模式 1：条件显示

```gotemplate
{{if .Name}}{{.Name | upper}}{{else}}匿名{{end}}
```

### 模式 2：默认值

```gotemplate
{{.Title | default "无标题"}}
```

### 模式 3：格式化数字

```gotemplate
价格: ¥{{printf "%.2f" .Price}}
```

### 模式 4：字符串操作

```gotemplate
{{.Text | trim | lower | replace " " "-"}}
```

### 模式 5：数学运算

```gotemplate
总计: ¥{{mul .Price .Quantity | printf "%.2f"}}
折扣: {{mul .Price .Quantity .Discount | div 100 | printf "%.2f"}}
```

### 模式 6：列表操作

```gotemplate
{{range $i, $item := .Items}}
  {{if gt $i 0}}, {{end}}{{$item}}
{{end}}
```

输出：`item1, item2, item3`

### 模式 7：交替样式

```gotemplate
{{range $i, $item := .Items}}
  <div class="{{if mod $i 2 | eq 0}}even{{else}}odd{{end}}">
    {{$item}}
  </div>
{{end}}
```

## 最佳实践

### 1. 尽可能使用内置函数

内置函数经过优化且经过了充分测试。

✅ **好：**
```gotemplate
{{upper .Name}}
```

❌ **不好：** 为简单操作创建自定义函数

### 2. 保持函数简单

函数应该做好一件事。

✅ **好：**
```java
functions.put("double", args -> {
    return ((Number) args[0]).intValue() * 2;
});
```

❌ **不好：** 函数中有复杂逻辑

### 3. 优雅地处理错误

```java
functions.put("safeDivide", args -> {
    double a = ((Number) args[0]).doubleValue();
    double b = ((Number) args[1]).doubleValue();
    if (b == 0) {
        return 0;  // 或抛出 TemplateException
    }
    return a / b;
});
```

### 4. 文档化自定义函数

```java
/**
 * 格式化电话号码。
 * 期望: (String) 电话号码数字
 * 返回: 格式化的电话号码 (xxx) xxx-xxxx
 */
functions.put("formatPhone", args -> {
    // 实现...
});
```

### 5. 充分测试函数

```java
@Test
public void testDoubleFunction() throws Exception {
    Map<String, Function> functions = new HashMap<>();
    functions.put("double", args -> ((Number) args[0]).intValue() * 2);
    
    Template template = new Template("test", functions);
    template.parse("{{double 5}}");
    
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
    
    assertEquals("10", writer.toString().trim());
}
```

## 性能注意事项

### 1. 避免昂贵的操作

不要在循环中调用的函数中进行大量计算。

❌ **不好：**
```gotemplate
{{range .Items}}
  {{expensiveOperation .}}
{{end}}
```

✅ **好：** 在 Java 代码中预先计算

### 2. 缓存结果

如果函数使用相同参数被多次调用，缓存结果。

### 3. 减少函数调用

高效地串联操作：

❌ **不好：**
```gotemplate
{{upper (trim .Text)}}
```

✅ **好：**
```gotemplate
{{.Text | trim | upper}}
```

## 问题排查

### 问题：函数未找到

**错误：** `function "xyz" not defined`

**解决方案：** 
1. 检查函数名拼写
2. 验证函数是否已注册
3. 确保正确的函数 map 已传递给模板

### 问题：参数数量错误

**错误：** `wrong number of args for xyz`

**解决方案：** 检查函数签名和调用

### 问题：类型不匹配

**错误：** `can't handle type X for function Y`

**解决方案：** 将参数转换为期望的类型

```java
functions.put("myFunc", args -> {
    // 安全转换
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("期望 String 类型");
    }
    String value = (String) args[0];
    // ...
});
```

## 完整示例

```java
import io.github.verils.gotemplate.*;
import java.util.*;

public class FunctionExample {
    public static void main(String[] args) throws Exception {
        // 创建自定义函数
        Map<String, Function> functions = new HashMap<>();
        
        // 问候函数
        functions.put("greet", args -> {
            String name = (String) args[0];
            return "你好, " + name + "!";
        });
        
        // 格式化货币
        functions.put("currency", args -> {
            double amount = ((Number) args[0]).doubleValue();
            return String.format("¥%.2f", amount);
        });
        
        // 创建模板
        String templateText = 
            "{{greet .Name}}\n" +
            "项目数: {{len .Items}}\n" +
            "总计: {{mul .Price .Quantity | currency}}\n" +
            "折后: {{mul .Price .Quantity .Discount | div 100 | currency}}";
        
        Template template = new Template("invoice", functions);
        template.parse(templateText);
        
        // 准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "Alice");
        data.put("Items", Arrays.asList("Widget", "Gadget"));
        data.put("Price", 19.99);
        data.put("Quantity", 5);
        data.put("Discount", 10);  // 10% 折扣
        
        // 执行
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        System.out.println(writer.toString());
    }
}
```

**输出：**
```
你好, Alice!
项目数: 2
总计: ¥99.95
折后: ¥10.00
```

---

**下一步：**
- 🎯 了解[控制流](control-flow.md)以进行条件和循环处理
- 📊 理解[数据模型](data-models.md)以使用 Java 对象
- 📚 查阅[模板语法](template-syntax.md)获取完整参考
