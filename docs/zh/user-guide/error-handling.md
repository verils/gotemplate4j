# 错误处理指南

本指南解释错误类型、处理策略以及稳健模板执行的最佳实践。

## 概述

gotemplate4j 有定义良好的异常层次结构：
- **TemplateException**：所有模板错误的基类
- **TemplateParseException**：模板解析期间的错误
- **TemplateExecutionException**：模板执行期间的错误
- **TemplateNotFoundException**：未找到模板文件

理解这些异常有助于你优雅地处理错误。

> 🆕 **v0.9.0 增强**：智能错误诊断，包含拼写建议、字段路径上下文和可用选项列表。

## 异常层次结构

```
TemplateException（基类）
├── TemplateParseException
│   ├── 词法分析器错误
│   └── 解析器错误
├── TemplateExecutionException
│   ├── 缺失字段错误
│   ├── 函数错误
│   └── 运行时错误
└── TemplateNotFoundException
    └── 文件未找到错误
```

## 解析错误

当模板语法无效时发生解析错误。

### 常见解析错误

#### 未闭合的动作

```java
try {
    template.parse("Hello {{.Name");
} catch (TemplateParseException e) {
    System.err.println("解析错误: " + e.getMessage());
    // 输出: unclosed action
}
```

#### 缺少 End

```java
try {
    template.parse("{{if .Active}}Active");
} catch (TemplateParseException e) {
    System.err.println("解析错误: " + e.getMessage());
    // 输出: unexpected EOF, expected end
}
```

#### 无效的管道

```java
try {
    template.parse("{{.Name | | upper}}");
} catch (TemplateParseException e) {
    System.err.println("解析错误: " + e.getMessage());
    // 输出: missing pipeline operand
}
```

#### 未定义函数

```java
try {
    template.parse("{{undefinedFunc .Name}}");
} catch (TemplateParseException e) {
    System.err.println("解析错误: " + e.getMessage());
}
```

### 处理解析错误

始终将解析调用包装在 try-catch 中：

```java
Template template = new Template("demo");
try {
    template.parse(templateText);
} catch (TemplateParseException e) {
    System.err.println("解析模板失败: " + e.getMessage());
    // 记录错误、显示用户友好的消息等
    return;
}
```

## 执行错误

当模板运行遇到有问题的数据或遇到运行时问题时发生执行错误。

### 常见执行错误

#### 缺失字段

```java
Map<String, Object> data = new HashMap<>();
// Name 键缺失

template.parse("你好, {{.Name}}!");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("执行错误: " + e.getMessage());
}
```

行为取决于 MissingKeyPolicy（见下文）。

#### 空指针

```java
Map<String, Object> data = new HashMap<>();
data.put("User", null);

template.parse("{{.User.Name}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("执行错误: " + e.getMessage());
}
```

#### 类型不匹配

```java
Map<String, Object> data = new HashMap<>();
data.put("Count", "not a number");

template.parse("{{add .Count 1}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("执行错误: " + e.getMessage());
}
```

#### 函数错误

```java
Map<String, Function> functions = new HashMap<>();
functions.put("divide", args -> {
    int a = (Integer) args[0];
    int b = (Integer) args[1];
    return a / b;  // 可能抛出 ArithmeticException
});

template.parse("{{divide 10 0}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
} catch (TemplateExecutionException e) {
    System.err.println("执行错误: " + e.getMessage());
}
```

### 处理执行错误

将 execute 调用包装在 try-catch 中：

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    System.err.println("模板执行失败: " + e.getMessage());
    // 适当处理错误
} catch (IOException e) {
    System.err.println("IO 错误: " + e.getMessage());
}
```

## MissingKeyPolicy

控制执行期间缺失键的处理方式。

### 策略选项

#### DEFAULT

缺失键渲染为空字符串。

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);

template.parse("姓名: '{{.Name}}'");

Map<String, Object> data = new HashMap<>();
// Name 缺失

StringWriter writer = new StringWriter();
template.execute(writer, data);
System.out.println(writer.toString());
// 输出: 姓名: ''
```

#### ZERO

缺失键渲染为该类型的零值。

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ZERO);

template.parse("计数: {{.Count}}, 姓名: '{{.Name}}'");

StringWriter writer = new StringWriter();
template.execute(writer, new HashMap<>());
System.out.println(writer.toString());
// 输出: 计数: 0, 姓名: ''
```

#### ERROR

缺失键抛出 TemplateExecutionException。

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

template.parse("姓名: {{.Name}}");

try {
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
} catch (TemplateExecutionException e) {
    System.err.println("错误: " + e.getMessage());
    // 输出: can't evaluate field Name
}
```

### 选择策略

- **DEFAULT**：最适合生产环境，宽容处理
- **ZERO**：适用于数字密集型模板
- **ERROR**：最适合开发环境，及早发现问题

## 模板未找到

尝试加载不存在的模板文件时发生。

```java
try {
    template.parseFile(Paths.get("nonexistent.tmpl"));
} catch (TemplateNotFoundException e) {
    System.err.println("未找到模板: " + e.getMessage());
} catch (IOException e) {
    System.err.println("IO 错误: " + e.getMessage());
}
```

## 最佳实践

### 1. 始终处理异常

永远不要让模板异常未经检查地传播。

❌ **不好：**
```java
public void render() throws Exception {
    template.execute(writer, data);
}
```

✅ **好：**
```java
public String render() {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        log.error("模板错误", e);
        return "<error>渲染模板失败</error>";
    }
}
```

### 2. 使用前验证模板

在开发期间测试模板：

```java
public void validateTemplate(String templateText) {
    try {
        Template template = new Template("validation");
        template.parse(templateText);
        System.out.println("模板有效");
    } catch (TemplateParseException e) {
        System.err.println("无效模板: " + e.getMessage());
    }
}
```

### 3. 开发环境中使用 ERROR 策略

及早发现缺失键：

```java
Template template = new Template("demo");
if (isDevelopment()) {
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
} else {
    template.withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);
}
```

### 4. 执行前检查数据

验证数据是否符合模板期望：

```java
public void executeWithData(Map<String, Object> data) {
    // 验证必需字段
    if (!data.containsKey("Name")) {
        throw new IllegalArgumentException("缺少必需字段: Name");
    }
    
    try {
        template.execute(writer, data);
    } catch (TemplateExecutionException e) {
        log.error("执行失败", e);
    }
}
```

### 5. 提供回退内容

使用回退优雅地处理错误：

```java
public String renderWithFallback(Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        log.warn("模板失败，使用回退", e);
        return getDefaultContent();
    }
}
```

### 6. 记录详细的错误信息

在错误日志中包含上下文：

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    log.error("模板执行失败\n" +
              "模板: {}\n" +
              "数据键: {}\n" +
              "错误: {}", 
              template.name(),
              data.keySet(),
              e.getMessage(),
              e);
}
```

### 7. 测试边缘情况

使用以下情况测试模板：
- 空数据
- Null 值
- 缺失字段
- 错误类型
- 空集合

```java
@Test
public void testWithEmptyData() {
    Map<String, Object> emptyData = new HashMap<>();
    
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, emptyData);
        // 验证输出是可接受的
    } catch (TemplateExecutionException e) {
        fail("应该能处理空数据: " + e.getMessage());
    }
}
```

## 错误恢复策略

### 策略 1：默认值

为缺失数据提供默认值：

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", Optional.ofNullable(userData.getName())
                         .orElse("匿名"));
data.put("Age", Optional.ofNullable(userData.getAge())
                        .orElse(0));
```

### 策略 2：条件渲染

在访问前检查：

```gotemplate
{{if .User}}
  {{if .User.Name}}
    {{.User.Name}}
  {{else}}
    匿名
  {{end}}
{{else}}
  无用户
{{end}}
```

### 策略 3：安全函数

创建处理错误的函数：

```java
functions.put("safeGet", args -> {
    Map<String, Object> map = (Map<String, Object>) args[0];
    String key = (String) args[1];
    Object defaultValue = args[2];
    
    return map.getOrDefault(key, defaultValue);
});
```
```gotemplate
{{safeGet .Data "key" "默认值"}}
```

### 策略 4：Java 中的 Try-Catch

在应用程序级别处理：

```java
public class TemplateRenderer {
    public String render(String templateName, Map<String, Object> data) {
        try {
            Template template = getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        } catch (TemplateNotFoundException e) {
            log.error("未找到模板: {}", templateName);
            return "<error>模板不可用</error>";
        } catch (TemplateParseException e) {
            log.error("{} 中的解析错误: {}", templateName, e.getMessage());
            return "<error>模板语法错误</error>";
        } catch (TemplateExecutionException e) {
            log.error("{} 中的执行错误: {}", templateName, e.getMessage());
            return "<error>渲染模板失败</error>";
        }
    }
}
```

## 增强的错误诊断（v0.9.0+）

gotemplate4j v0.9.0 引入了智能错误诊断，帮助你快速识别和修复模板问题。

### 带建议的字段访问错误

访问不存在的字段时，错误消息现在包含：
- 完整字段路径上下文（例如 `User.Address.City`）
- 该级别的可用字段列表
- 使用模糊匹配的拼写建议（Levenshtein 距离）

**示例**：
```java
// 模板: {{.FristName}}
// 数据: 带有字段 [firstName, lastName, age] 的 User 对象

// v0.9.0 之前:
can't evaluate field FristName

// v0.9.0 之后:
can't evaluate field User.FristName. 可用字段: [age, firstName, getAge, getFirstName, getName, lastName]. 你是想用 'firstName' 吗?
```

建议算法计算拼写错误名称与可用选项之间的编辑距离，建议最接近的匹配。

### Map 键错误

对 map 键访问的类似增强：

```java
// 模板: {{index .Data "FristName"}}
// 数据: 带有键 ["FirstName", "LastName", "Age"] 的 Map

// v0.9.0 之前:
missing map key 'FristName'

// v0.9.0 之后:
missing map key 'FristName'. 可用键: [Age, FirstName, LastName]. 你是想用 'FirstName' 吗?
```

### 函数错误

解析时和运行时函数错误现在提供全面的信息：

#### 解析时错误

引用未定义的函数时：

```java
// 模板: {{leng .Name}}

// v0.9.0 之前:
function leng not defined

// v0.9.0 之后:
undefined function: leng. 可用函数: [and, call, deepEqual, default, eq, ge, gt, html, index, js, kindOf, len, lt, ne, not, or, printf, sprintf, typeOf, urlquery]. 你是想用 'len' 吗?
```

错误消息列出所有可用的内置函数并建议最接近的匹配。

#### 运行时错误

函数调用在运行时失败时：

```java
// 自定义函数期望 2 个参数
template.addFunction("divide", args -> {
    if (args.length != 2) {
        throw new IllegalArgumentException("需要恰好 2 个参数");
    }
    // ...
});

// 模板: {{divide 10}}

// v0.9.0 之前:
function 'divide' failed

// v0.9.0 之后:
function 'divide' failed with 1 argument(s): 需要恰好 2 个参数
```

错误消息包含实际参数数量以帮助调试签名不匹配。

### 统一的解析器异常格式

所有解析器异常现在遵循一致的格式：
```
<error-type>: <description> [in <context>]
```

**示例**：
- `undefined function: functionName`
- `invalid number syntax: 0xGG`
- `unexpected dot after term true`
- `non-executable command in pipeline stage 2`
- `missing value: command`

这种一致性使错误消息更容易以编程方式解析并一目了然地理解。

### 使用增强诊断

增强诊断自动生效 -- 无需配置。只需升级到 v0.9.0+，即可立即获得更好的错误消息。

**利用增强诊断的提示**：

1. **阅读完整的错误消息**：它包含可操作的信息
2. **检查建议**："你是想用?" 提示通常是正确的
3. **查看可用选项**：列表显示实际可访问的内容
4. **在开发中使用 ERROR 策略**：通过详细消息及早发现问题

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);  // 获取详细错误信息

try {
    template.parse(templateText);
} catch (TemplateParseException e) {
    System.err.println(e.getMessage());  // 阅读增强的消息
}
```

---

## 调试技巧

### 1. 启用详细的错误消息

某些异常包含位置信息：

```
TemplateParseException: unexpected token "}" at line 3, column 10
```

使用此信息快速定位错误。

### 2. 测试小块

隔离有问题的模板部分：

```java
// 仅测试有问题的部分
Template test = new Template("test");
test.parse("{{.ProblematicField}}");
test.execute(writer, data);
```

### 3. 打印数据结构

验证数据是否符合期望：

```java
System.out.println("数据键: " + data.keySet());
System.out.println("用户: " + data.get("User"));
```

### 4. 使用更简单的模板

从简单开始，逐步添加复杂度：

```java
// 从这里开始
template.parse("{{.Name}}");

// 然后添加更多
template.parse("{{if .Name}}{{.Name}}{{else}}无姓名{{end}}");
```

### 5. 检查模板语法

如果可用，使用在线验证器或 linter。

## 常见错误场景

### 场景 1：生产环境错误处理

```java
@RestController
public class PageController {
    
    @Autowired
    private TemplateService templateService;
    
    @GetMapping("/page/{name}")
    public ResponseEntity<String> renderPage(@PathVariable String name) {
        try {
            Map<String, Object> data = buildPageData(name);
            String html = templateService.render(name, data);
            return ResponseEntity.ok(html);
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (TemplateException e) {
            log.error("页面渲染失败: {}", name, e);
            return ResponseEntity.status(500)
                                 .body("<h1>内部服务器错误</h1>");
        }
    }
}
```

### 场景 2：带回退的邮件模板

```java
public String generateEmail(Map<String, Object> data) {
    try {
        Template template = loadEmailTemplate();
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (Exception e) {
        log.error("邮件生成失败，使用纯文本", e);
        return generatePlainTextEmail(data);
    }
}
```

### 场景 3：批处理

```java
public void processBatch(List<Map<String, Object>> items) {
    for (Map<String, Object> item : items) {
        try {
            String result = templateService.render("item", item);
            saveResult(result);
        } catch (TemplateException e) {
            log.error("处理项目失败: {}", item.get("id"), e);
            // 继续处理下一个项目
        }
    }
}
```

## 性能注意事项

### 1. 解析一次，执行多次

不要重复解析模板：

❌ **不好：**
```java
for (Map<String, Object> data : dataList) {
    Template template = new Template("demo");
    template.parse(templateText);  // 每次迭代都解析
    template.execute(writer, data);
}
```

✅ **好：**
```java
Template template = new Template("demo");
template.parse(templateText);  // 只解析一次

for (Map<String, Object> data : dataList) {
    StringWriter writer = new StringWriter();
    template.execute(writer, data);  // 多次执行
}
```

### 2. 缓存已编译的模板

```java
private Map<String, Template> templateCache = new ConcurrentHashMap<>();

public Template getTemplate(String name) {
    return templateCache.computeIfAbsent(name, n -> {
        Template template = new Template(n);
        template.parseFile(Paths.get("templates/" + n + ".tmpl"));
        return template;
    });
}
```

### 3. 避免昂贵的错误处理

不要使用异常进行流程控制：

❌ **不好：**
```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    // 预期情况，并非真正的错误
    useDefaultOutput();
}
```

✅ **好：**
```java
if (isValidData(data)) {
    template.execute(writer, data);
} else {
    useDefaultOutput();
}
```

## 测试错误处理

### 单元测试

```java
@Test
public void testMissingKeyWithErrorPolicy() {
    Template template = new Template("test")
        .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    template.parse("{{.Missing}}");
    
    assertThrows(TemplateExecutionException.class, () -> {
        template.execute(new StringWriter(), new HashMap<>());
    });
}

@Test
public void testMissingKeyWithDefaultPolicy() {
    Template template = new Template("test")
        .withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);
    template.parse("{{.Missing}}");
    
    StringWriter writer = new StringWriter();
    template.execute(writer, new HashMap<>());
    
    assertEquals("", writer.toString());
}

@Test
public void testParseError() {
    Template template = new Template("test");
    
    assertThrows(TemplateParseException.class, () -> {
        template.parse("{{invalid syntax");
    });
}
```

### 集成测试

```java
@Test
public void testFullRenderWithErrorHandling() {
    Map<String, Object> data = new HashMap<>();
    data.put("Name", "Alice");
    // 缺少其他字段
    
    String result = renderer.render("profile", data);
    
    // 不应该抛出异常，应该有回退内容
    assertNotNull(result);
    assertTrue(result.contains("匿名") || result.contains("error"));
}
```

---

**下一步：**
- 🔍 查看[Go 兼容性](../advanced/compatibility.md)了解已知差异
- 📚 了解[性能调优](../advanced/performance.md)以进行优化
- 💡 探索[最佳实践](../advanced/best-practices.md)获取全面指南
