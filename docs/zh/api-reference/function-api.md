# Function API 参考

`Function` 接口允许您实现可以在模板中调用的自定义函数。本文档记录了该接口并提供实现示例。

---

## 目录

- [Function 接口](#function-接口)
- [实现自定义函数](#实现自定义函数)
- [注册函数](#注册函数)
- [内置函数](#内置函数)
- [最佳实践](#最佳实践)
- [常见模式](#常见模式)

---

## Function 接口

### 接口定义

```java
package io.github.verils.gotemplate;

public interface Function {
    /**
     * 使用提供的参数调用函数。
     *
     * @param args 从模板传递给函数的参数
     * @return 结果值
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果函数执行失败
     */
    Object invoke(Object... args);
}
```

### 方法：`Object invoke(Object... args)`

使用提供的参数调用函数。

**参数：**
- `args` - 从模板传入的可变数量参数。可以是零个或多个任意类型的参数。

**返回：**
- 结果值，将转换为字符串用于输出，或作为管道中下一阶段的输入

**抛出异常：**
- 如果参数数量或类型无效，抛出 `IllegalArgumentException`
- 如果函数执行因任何原因失败，抛出 `RuntimeException`

**模板中的用法：**

函数可以通过两种方式调用：

1. **直接调用：**
   ```gotemplate
   {{myFunction arg1 arg2 arg3}}
   ```

2. **管道调用：**
   ```gotemplate
   {{arg1 | myFunction}}
   {{arg1 | myFunction arg2}}
   ```

---

## 实现自定义函数

### 基础示例：字符串转大写

```java
import io.github.verils.gotemplate.Function;

Function upperCase = new Function() {
    @Override
    public Object invoke(Object... args) {
        // 验证参数数量
        if (args.length != 1) {
            throw new IllegalArgumentException("upper 需要恰好一个参数");
        }
        
        // 验证参数类型
        if (!(args[0] instanceof String)) {
            throw new IllegalArgumentException("upper 需要字符串参数");
        }
        
        // 执行函数逻辑
        return ((String) args[0]).toUpperCase();
    }
};
```

**模板用法：**
```gotemplate
{{.Name | upper}}
<!-- 如果 .Name 为 "john"，输出: JOHN -->
```

---

### Lambda 表达式（Java 8+）

由于 `Function` 是函数式接口，您可以使用 lambda 表达式：

```java
Function lowerCase = args -> {
    if (args.length != 1 || !(args[0] instanceof String)) {
        throw new IllegalArgumentException("lower 需要一个字符串参数");
    }
    return ((String) args[0]).toLowerCase();
};
```

---

### 多参数示例：字符串重复

```java
Function repeat = args -> {
    if (args.length != 2) {
        throw new IllegalArgumentException("repeat 需要两个参数：字符串和次数");
    }
    
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("第一个参数必须是字符串");
    }
    
    if (!(args[1] instanceof Number)) {
        throw new IllegalArgumentException("第二个参数必须是数字");
    }
    
    String str = (String) args[0];
    int count = ((Number) args[1]).intValue();
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
        sb.append(str);
    }
    
    return sb.toString();
};
```

**模板用法：**
```gotemplate
{{repeat "-" 10}}
<!-- 输出: ---------- -->

{{.Separator | repeat .Count}}
```

---

### 复杂逻辑：日期格式化

```java
import java.text.SimpleDateFormat;
import java.util.Date;

Function formatDate = args -> {
    if (args.length < 1 || args.length > 2) {
        throw new IllegalArgumentException("formatDate 需要 1-2 个参数");
    }
    
    if (!(args[0] instanceof Date)) {
        throw new IllegalArgumentException("第一个参数必须是 Date");
    }
    
    Date date = (Date) args[0];
    String pattern = args.length == 2 ? (String) args[1] : "yyyy-MM-dd";
    
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    return sdf.format(date);
};
```

**模板用法：**
```gotemplate
{{formatDate .CreatedAt}}
<!-- 输出: 2024-01-15（默认格式） -->

{{formatDate .CreatedAt "MMM dd, yyyy"}}
<!-- 输出: Jan 15, 2024 -->
```

---

## 注册函数

### 单个函数

```java
Map<String, Function> functions = new HashMap<>();
functions.put("upper", upperCase);

Template template = new Template("demo", functions);
template.parse("{{.Name | upper}}");
```

### 多个函数

```java
Map<String, Function> functions = new HashMap<>();
functions.put("upper", args -> ((String) args[0]).toUpperCase());
functions.put("lower", args -> ((String) args[0]).toLowerCase());
functions.put("trim", args -> ((String) args[0]).trim());
functions.put("length", args -> ((String) args[0]).length());

Template template = new Template("demo", functions);
template.parse("{{.Text | trim | upper}}");
```

### 覆盖内置函数

自定义函数优先于同名的内置函数：

```java
Map<String, Function> functions = new HashMap<>();
// 覆盖内置的 'len' 函数
functions.put("len", args -> {
    // 自定义长度计算
    return ((String) args[0]).codePointCount(0, ((String) args[0]).length());
});

Template template = new Template("demo", functions);
```

**注意：** 覆盖内置函数时要谨慎，这可能会破坏现有模板。

---

## 内置函数

以下函数在所有模板中默认可用：

### 输出格式化

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `print` | 将参数转换为字符串 | `{{print .Name .Age}}` |
| `printf` | Go 风格格式化字符串 | `{{printf "%s 今年 %d 岁" .Name .Age}}` |
| `println` | 将参数转换为字符串并换行 | `{{println .Message}}` |

### 比较运算符

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `eq` | 等于（==） | `{{if eq .Status "active"}}...{{end}}` |
| `ne` | 不等于（!=） | `{{if ne .Count 0}}...{{end}}` |
| `lt` | 小于（<） | `{{if lt .Age 18}}...{{end}}` |
| `le` | 小于等于（<=） | `{{if le .Score 100}}...{{end}}` |
| `gt` | 大于（>） | `{{if gt .Price 100}}...{{end}}` |
| `ge` | 大于等于（>=） | `{{if ge .Rating 4}}...{{end}}` |

### 逻辑运算符

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `and` | 逻辑 AND | `{{if and .IsActive .IsAdmin}}...{{end}}` |
| `or` | 逻辑 OR | `{{if or .IsAdmin .IsModerator}}...{{end}}` |
| `not` | 逻辑 NOT | `{{if not .IsEmpty}}...{{end}}` |

### 集合操作

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `len` | 字符串/集合长度 | `{{len .Items}}` |
| `index` | 访问数组/映射元素 | `{{index .Array 0}}`、`{{index .Map "key"}}` |
| `slice` | 切片数组/字符串 | `{{slice .Array 1 3}}` |

### 类型检查

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `typeof` | 获取 Java 类名 | `{{typeof .Value}}` |
| `kindOf` | 获取类 Go 的 kind | `{{kindOf .Value}}` |
| `deepEqual` | 深度相等检查 | `{{if deepEqual .A .B}}...{{end}}` |

### 转义函数

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `html` | HTML 转义 | `{{html .UserInput}}` |
| `js` | JavaScript 转义 | `{{js .UserInput}}` |
| `urlquery` | URL 查询转义 | `{{urlquery .Param}}` |

### 其他函数

| 函数 | 描述 | 示例 |
|----------|-------------|---------|
| `call` | 动态调用函数 | `{{call .Func arg1 arg2}}` |

有关内置函数的详细文档，请参见 [函数指南](../user-guide/functions.md)。

---

## 最佳实践

### 1. 验证参数

务必验证参数的数量和类型：

```java
Function safeFunction = args -> {
    // 检查参数数量
    if (args.length != 2) {
        throw new IllegalArgumentException("需要 2 个参数，实际传入 " + args.length);
    }
    
    // 检查参数类型
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("第一个参数必须是 String");
    }
    if (!(args[1] instanceof Number)) {
        throw new IllegalArgumentException("第二个参数必须是 Number");
    }
    
    // 现在可以安全使用参数
    String str = (String) args[0];
    int num = ((Number) args[1]).intValue();
    
    return str + num;
};
```

### 2. 提供清晰的错误消息

错误消息应帮助模板作者了解出了什么问题：

```java
// 差的错误消息
throw new IllegalArgumentException("无效参数");

// 好的错误消息
throw new IllegalArgumentException(
    "join 需要一个列表和一个分隔符字符串，实际传入了 " + args.length + " 个参数"
);
```

### 3. 处理 Null 值

决定您的函数应如何处理 null 参数：

```java
Function nullSafe = args -> {
    if (args.length == 0 || args[0] == null) {
        return ""; // null 时返回空字符串
    }
    return args[0].toString().toUpperCase();
};
```

### 4. 保持函数简单

每个函数应该只做好一件事。复杂逻辑应拆分为多个函数，或在将数据传递给模板之前在 Java 代码中处理。

### 5. 为函数写文档

记录预期的参数、返回类型和行为：

```java
/**
 * 使用分隔符连接字符串列表。
 * 
 * 用法: {{join .Items ", "}}
 * 参数:
 *   - args[0]: List<String> 或 String[] - 要连接的项目
 *   - args[1]: String - 分隔符
 * 返回: String - 连接结果
 */
Function join = args -> {
    // 实现...
};
```

### 6. 考虑性能

对于在循环中频繁调用的函数，避免昂贵的操作：

```java
// 差：每次调用都创建新的 SimpleDateFormat
Function badDateFormat = args -> {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return sdf.format((Date) args[0]);
};

// 好：重用 SimpleDateFormat（通过同步确保线程安全）
private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = 
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

Function goodDateFormat = args -> {
    return DATE_FORMAT.get().format((Date) args[0]);
};
```

---

## 常见模式

### 模式 1：字符串操作

```java
// 去除空白字符
Function trim = args -> ((String) args[0]).trim();

// 替换子串
Function replace = args -> {
    String str = (String) args[0];
    String oldStr = (String) args[1];
    String newStr = (String) args[2];
    return str.replace(oldStr, newStr);
};

// 取子串
Function substr = args -> {
    String str = (String) args[0];
    int start = ((Number) args[1]).intValue();
    int end = args.length > 2 ? ((Number) args[2]).intValue() : str.length();
    return str.substring(start, end);
};
```

### 模式 2：数学运算

```java
// 加法
Function add = args -> {
    double sum = 0;
    for (Object arg : args) {
        sum += ((Number) arg).doubleValue();
    }
    return sum;
};

// 乘法
Function multiply = args -> {
    double product = 1;
    for (Object arg : args) {
        product *= ((Number) arg).doubleValue();
    }
    return product;
};

// 四舍五入
Function round = args -> {
    double value = ((Number) args[0]).doubleValue();
    int decimals = args.length > 1 ? ((Number) args[1]).intValue() : 0;
    double factor = Math.pow(10, decimals);
    return Math.round(value * factor) / factor;
};
```

### 模式 3：条件逻辑

```java
// 为空/null 时返回默认值
Function defaultIfEmpty = args -> {
    Object value = args[0];
    Object defaultValue = args[1];
    
    if (value == null) {
        return defaultValue;
    }
    if (value instanceof String && ((String) value).isEmpty()) {
        return defaultValue;
    }
    return value;
};
```

**模板用法：**
```gotemplate
{{defaultIfEmpty .Name "Anonymous"}}
<!-- 如果 .Name 为 null 或空，则使用 "Anonymous" -->
```

### 模式 4：数据转换

```java
// 将列表转换为逗号分隔的字符串
Function joinList = args -> {
    if (!(args[0] instanceof Collection)) {
        throw new IllegalArgumentException("需要集合类型参数");
    }
    
    String separator = args.length > 1 ? (String) args[1] : ", ";
    Collection<?> collection = (Collection<?>) args[0];
    
    return collection.stream()
        .map(Object::toString)
        .collect(Collectors.joining(separator));
};
```

**模板用法：**
```gotemplate
{{joinList .Tags ", "}}
<!-- 如果 .Tags 为 ["java", "template", "go"]，输出: java, template, go -->
```

### 模式 5：查找表

```java
// 将状态码映射为标签
Function statusLabel = args -> {
    int code = ((Number) args[0]).intValue();
    
    Map<Integer, String> labels = new HashMap<>();
    labels.put(0, "待处理");
    labels.put(1, "活跃");
    labels.put(2, "已暂停");
    labels.put(3, "已删除");
    
    return labels.getOrDefault(code, "未知");
};
```

**模板用法：**
```gotemplate
{{statusLabel .StatusCode}}
<!-- 如果 .StatusCode 为 1，输出: 活跃 -->
```

---

## 高级示例

### 示例 1：Markdown 渲染器

```java
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

Function markdown = args -> {
    if (args.length != 1 || !(args[0] instanceof String)) {
        throw new IllegalArgumentException("markdown 需要一个字符串参数");
    }
    
    Parser parser = Parser.builder().build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    
    String markdown = (String) args[0];
    var document = parser.parse(markdown);
    return renderer.render(document);
};
```

**注意：** 此示例需要外部 Markdown 库。

---

### 示例 2：JSON 格式化

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

Function toJson = args -> {
    if (args.length != 1) {
        throw new IllegalArgumentException("toJson 需要一个参数");
    }
    
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(args[0]);
};
```

---

### 示例 3：国际化

```java
import java.util.ResourceBundle;
import java.util.Locale;

class I18nFunction implements Function {
    private final ResourceBundle bundle;
    
    public I18nFunction(Locale locale) {
        this.bundle = ResourceBundle.getBundle("messages", locale);
    }
    
    @Override
    public Object invoke(Object... args) {
        if (args.length == 0 || !(args[0] instanceof String)) {
            throw new IllegalArgumentException("i18n 需要一个键字符串");
        }
        
        String key = (String) args[0];
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key; // 如果找不到翻译则返回键本身
        }
    }
}

// 用法
Function i18n = new I18nFunction(Locale.FRENCH);
Map<String, Function> functions = new HashMap<>();
functions.put("t", i18n);
Template template = new Template("demo", functions);
```

**模板用法：**
```gotemplate
{{t "greeting"}}
<!-- 根据区域输出本地化的问候语 -->
```

---

## 测试自定义函数

务必充分测试您的自定义函数：

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomFunctionTest {
    
    @Test
    void testUpperCase() {
        Function upper = args -> ((String) args[0]).toUpperCase();
        
        assertEquals("HELLO", upper.invoke("hello"));
        assertEquals("WORLD", upper.invoke("world"));
    }
    
    @Test
    void testUpperCaseWithInvalidArgs() {
        Function upper = args -> {
            if (args.length != 1 || !(args[0] instanceof String)) {
                throw new IllegalArgumentException("需要一个字符串参数");
            }
            return ((String) args[0]).toUpperCase();
        };
        
        assertThrows(IllegalArgumentException.class, () -> upper.invoke());
        assertThrows(IllegalArgumentException.class, () -> upper.invoke(123));
    }
    
    @Test
    void testUpperCaseInTemplate() throws Exception {
        Map<String, Function> functions = new HashMap<>();
        functions.put("upper", args -> ((String) args[0]).toUpperCase());
        
        Template template = new Template("test", functions);
        template.parse("{{.Name | upper}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "john");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("JOHN", writer.toString());
    }
}
```

---

## 摘要

`Function` 接口提供了一种强大的方式来扩展模板功能：

- 实现 `invoke(Object... args)` 方法
- 验证参数并提供清晰的错误消息
- 通过 `Template` 构造函数注册函数
- 对简单函数使用 lambda 表达式
- 遵循最佳实践以确保健壮性和性能

更多信息：
- 参见 [Template API](template-api.md) 了解模板配置
- 参见 [函数指南](../user-guide/functions.md) 了解用法示例
- 参见 [Exception API](exception-api.md) 了解错误处理
