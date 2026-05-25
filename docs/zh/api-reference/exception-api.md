# Exception API 参考

gotemplate4j 使用分层异常模型来处理模板解析和执行过程中的错误。本文档记录了所有异常类型及其用法。

---

## 目录

- [异常层次结构](#异常层次结构)
- [TemplateException](#templateexception)
- [TemplateParseException](#templateparseexception)
- [TemplateExecutionException](#templateexecutionexception)
- [TemplateNotFoundException](#templatenotfoundexception)
- [MissingKeyPolicy](#missingkeypolicy)
- [错误处理模式](#错误处理模式)
- [最佳实践](#最佳实践)

---

## 异常层次结构

```
java.lang.Exception
    └── TemplateException（基类）
        ├── TemplateParseException（解析错误）
        ├── TemplateExecutionException（运行时错误）
        └── TemplateNotFoundException（缺失模板）
```

所有与模板相关的异常都继承自 `TemplateException`，这样可以方便地使用单个 catch 块捕获所有模板错误。

---

## TemplateException

**包名：** `io.github.verils.gotemplate`

**类型：** 受检异常

所有模板相关异常的基类。

### 构造函数

```java
public TemplateException(String message)
public TemplateException(String message, Throwable cause)
```

### 使用场景

`TemplateException` 本身很少被直接抛出，而是使用其子类来处理具体的错误场景。不过，您可以通过捕获 `TemplateException` 来统一处理所有模板错误：

```java
try {
    template.parse("{{.Name}}");
    template.execute(writer, data);
} catch (TemplateException e) {
    // 捕获 ParseException、ExecutionException 或 NotFoundException
    System.err.println("模板错误: " + e.getMessage());
}
```

### 常见用例

- 在统一错误处理器中捕获所有模板错误
- 将模板操作包裹在 try-catch 块中
- 记录模板失败信息

---

## TemplateParseException

**包名：** `io.github.verils.gotemplate`

**继承自：** `TemplateException`

当模板包含语法错误或无法解析时抛出。

### 构造函数

```java
public TemplateParseException(String message)
public TemplateParseException(String message, Throwable cause)
```

### 抛出时机

此异常在**解析阶段**抛出，当遇到以下情况时：

1. **语法错误：**
   ```java
   template.parse("{{if .Condition}}"); // 缺少 {{end}}
   // 抛出: TemplateParseException
   ```

2. **未定义变量：**
   ```java
   template.parse("{{$undefinedVar}}");
   // 抛出: TemplateParseException
   ```

3. **无效语法：**
   ```java
   template.parse("{{.Field invalid syntax}}");
   // 抛出: TemplateParseException
   ```

4. **分隔符不匹配：**
   ```java
   template.parse("{{if .Condition}}...{{else}}"); // 缺少 {{end}}
   // 抛出: TemplateParseException
   ```

5. **无效的函数调用：**
   ```java
   template.parse("{{nonexistentFunc .Arg}}");
   // 可能抛出: TemplateParseException
   ```

### 错误消息格式

错误消息通常包含：
- 错误描述
- 行号和列号（如果可用）
- 错误上下文

示例：
```
TemplateParseException: unexpected EOF in action at line 3, column 15
```

### 处理解析错误

```java
try {
    template.parse(templateText);
} catch (TemplateParseException e) {
    // 记录错误及上下文
    logger.error("模板解析失败: {}", e.getMessage(), e);
    
    // 提供用户友好的反馈
    showError("模板语法错误: " + e.getMessage());
}
```

### 预防建议

- 在部署前验证模板语法
- 使用模板检查工具
- 使用各种输入测试模板
- 保持模板简洁且结构良好

---

## TemplateExecutionException

**包名：** `io.github.verils.gotemplate`

**继承自：** `TemplateException`

当模板在运行时执行失败时抛出。

### 构造函数

```java
public TemplateExecutionException(String message)
public TemplateExecutionException(String message, Throwable cause)
```

### 抛出时机

此异常在**执行阶段**（成功解析后）抛出，当遇到以下情况时：

1. **访问不存在的字段：**
   ```java
   template.parse("{{.NonExistentField}}");
   
   Map<String, Object> data = new HashMap<>();
   // 'NonExistentField' 不在 map 中
   
   template.execute(writer, data);
   // 抛出: TemplateExecutionException（如果策略为 ERROR）
   ```

2. **函数调用错误：**
   ```java
   template.parse("{{len .Value}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("Value", 123); // len 期望 String/Collection，不是 Integer
   
   template.execute(writer, data);
   // 抛出: TemplateExecutionException
   ```

3. **类型转换失败：**
   ```java
   template.parse("{{if gt .Value 10}}...{{end}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("Value", "不是数字");
   
   template.execute(writer, data);
   // 抛出: TemplateExecutionException
   ```

4. **空指针访问：**
   ```java
   template.parse("{{.User.Name}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("User", null);
   
   template.execute(writer, data);
   // 抛出: TemplateExecutionException
   ```

5. **自定义函数错误：**
   ```java
   Function customFunc = args -> {
       throw new RuntimeException("出了点问题");
   };
   
   template.parse("{{customFunc}}");
   template.execute(writer, data);
   // 抛出: TemplateExecutionException（包装了 RuntimeException）
   ```

6. **缺失键策略违规：**
   ```java
   Template template = new Template("demo")
       .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
   
   template.parse("{{index .Map \"missing\"}}");
   
   Map<String, Object> data = new HashMap<>();
   data.put("Map", new HashMap<>());
   
   template.execute(writer, data);
   // 抛出: TemplateExecutionException
   ```

### 错误消息格式

错误消息描述运行时失败：

```
TemplateExecutionException: field NonExistentField not found in type java.util.HashMap
TemplateExecutionException: calling len on invalid type java.lang.Integer
TemplateExecutionException: nil pointer evaluating interface {}.Name
```

### 处理执行错误

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    // 记录详细错误信息
    logger.error("模板执行失败: {}", e.getMessage(), e);
    
    // 检查是否为缺失字段错误
    if (e.getMessage().contains("not found")) {
        showWarning("某些数据字段缺失");
    }
    
    // 提供回退内容
    writer.write("Error rendering content");
}
```

### 调试技巧

1. **启用详细错误消息：**
   ```java
   try {
       template.execute(writer, data);
   } catch (TemplateExecutionException e) {
       e.printStackTrace(); // 完整堆栈跟踪
       logger.debug("数据: {}", data); // 记录输入数据
   }
   ```

2. **使用示例数据测试：**
   ```java
   // 创建覆盖所有模板路径的测试数据
   Map<String, Object> testData = createTestData();
   template.execute(new StringWriter(), testData);
   ```

3. **在开发期间使用 MissingKeyPolicy.ERROR：**
   ```java
   Template template = new Template("demo")
       .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
   // 提前捕获缺失字段
   ```

---

## TemplateNotFoundException

**包名：** `io.github.verils.gotemplate`

**继承自：** `TemplateException`

当引用的模板无法找到时抛出。

### 构造函数

```java
public TemplateNotFoundException(String message)
public TemplateNotFoundException(String message, Throwable cause)
```

### 抛出时机

此异常在以下情况下发生：

1. **执行未解析的模板：**
   ```java
   Template template = new Template("my-template");
   // 从未调用 template.parse()
   
   template.execute(writer, data);
   // 抛出: TemplateNotFoundException
   ```

2. **包含未定义的模板：**
   ```java
   template.parse("{{template \"undefined-template\" .}}");
   
   template.execute(writer, data);
   // 抛出: TemplateNotFoundException
   ```

3. **使用不存在的名称调用 executeTemplate：**
   ```java
   template.parse("{{define \"header\"}}Header{{end}}");
   
   template.executeTemplate(writer, "footer", data); // "footer" 未定义
   // 抛出: TemplateNotFoundException
   ```

### 错误消息格式

```
TemplateNotFoundException: Template 'undefined-template' not found.
```

### 处理未找到错误

```java
try {
    template.executeTemplate(writer, "optional-section", data);
} catch (TemplateNotFoundException e) {
    // 可选模板未找到 - 使用默认内容
    writer.write("<div>默认内容</div>");
}
```

### 预防

在执行前检查模板是否存在：

```java
if (template.hasTemplate("section-name")) {
    template.executeTemplate(writer, "section-name", data);
} else {
    writer.write("<div>默认内容</div>");
}
```

或使用 lookup：

```java
Template sectionTemplate = template.lookup("section-name");
if (sectionTemplate != null) {
    sectionTemplate.execute(writer, data);
} else {
    // 处理缺失模板
}
```

---

## MissingKeyPolicy

**包名：** `io.github.verils.gotemplate`

**类型：** 枚举

控制执行期间访问缺失映射键时的行为。

### 枚举值

#### `INVALID`（默认）

不执行任何操作，继续执行。缺失的键不产生输出。

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.INVALID);

template.parse("{{.MissingKey}}");
template.execute(writer, new HashMap<>());
// 输出: （空字符串）
```

**用例：** 生产环境，缺失值应被静默忽略。

---

#### `ZERO`

返回映射类型元素的零值。

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ZERO);

template.parse("{{.MissingKey}}");
template.execute(writer, new HashMap<>());
// 输出: 取决于预期类型（null、0、false 等）
```

**用例：** 当需要可预测的默认值而不是空字符串时。

---

#### `ERROR`

立即停止执行并抛出 `TemplateExecutionException`。

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

template.parse("{{.MissingKey}}");

try {
    template.execute(writer, new HashMap<>());
} catch (TemplateExecutionException e) {
    System.out.println(e.getMessage());
    // 输出: map has no entry for key "MissingKey"
}
```

**用例：** 开发和测试阶段，提前捕获缺失数据。

---

### 配置 MissingKeyPolicy

#### 使用 `withMissingKeyPolicy()`

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

#### 使用 `option()` 方法

```java
// Go 风格选项字符串
template.option("missingkey=error");
template.option("missingkey=zero");
template.option("missingkey=default"); // 等同于 INVALID
```

#### 检查当前策略

```java
MissingKeyPolicy policy = template.missingKeyPolicy();
System.out.println("当前策略: " + policy);
```

### 策略对比

| 策略 | 行为 | 用例 |
|--------|----------|----------|
| `INVALID` | 静默，无输出 | 生产环境，可选字段 |
| `ZERO` | 返回零值 | 可预测的默认值 |
| `ERROR` | 抛出异常 | 开发环境，必需字段 |

### 最佳实践

为不同环境使用不同的策略：

```java
Template template = new Template("demo");

if (isDevelopment()) {
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
} else {
    template.withMissingKeyPolicy(MissingKeyPolicy.INVALID);
}
```

---

## 错误处理模式

### 模式 1：全面错误处理

```java
public String renderTemplate(String templateText, Map<String, Object> data) {
    Template template = new Template("render");
    
    try {
        // 解析
        template.parse(templateText);
        
        // 执行
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
        
    } catch (TemplateParseException e) {
        // 模板语法错误
        logger.error("模板语法错误: {}", e.getMessage());
        throw new RenderingException("无效的模板语法", e);
        
    } catch (TemplateNotFoundException e) {
        // 引用的模板未找到
        logger.error("模板未找到: {}", e.getMessage());
        throw new RenderingException("缺失模板定义", e);
        
    } catch (TemplateExecutionException e) {
        // 执行期间运行时错误
        logger.error("模板执行错误: {}", e.getMessage());
        logger.debug("数据: {}", data);
        throw new RenderingException("渲染模板失败", e);
        
    } catch (IOException e) {
        // 写入输出时 IO 错误
        logger.error("渲染期间 IO 错误", e);
        throw new RenderingException("IO 错误", e);
    }
}
```

---

### 模式 2：优雅降级

```java
public String renderWithFallback(String templateText, Map<String, Object> data) {
    Template template = new Template("render");
    
    try {
        template.parse(templateText);
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
        
    } catch (TemplateException e) {
        // 任何模板错误 - 返回回退内容
        logger.warn("模板渲染失败，使用回退内容: {}", e.getMessage());
        return "<div>内容暂时不可用</div>";
    }
}
```

---

### 模式 3：执行前验证

```java
public void validateTemplate(String templateText) throws TemplateParseException {
    Template template = new Template("validation");
    template.parse(templateText);
    // 如果执行到这里，模板语法有效
}

// 用法
try {
    validateTemplate(userProvidedTemplate);
    // 可以安全使用模板
} catch (TemplateParseException e) {
    showError("无效模板: " + e.getMessage());
}
```

---

### 模式 4：详细错误报告

```java
public class TemplateErrorReporter {
    
    public static String formatError(TemplateException e, String templateName) {
        StringBuilder sb = new StringBuilder();
        sb.append("模板错误在 '").append(templateName).append("':\n");
        sb.append("类型: ").append(e.getClass().getSimpleName()).append("\n");
        sb.append("消息: ").append(e.getMessage()).append("\n");
        
        if (e.getCause() != null) {
            sb.append("原因: ").append(e.getCause().getMessage()).append("\n");
        }
        
        return sb.toString();
    }
}

// 用法
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    String errorReport = TemplateErrorReporter.formatError(e, "user-profile");
    logger.error(errorReport, e);
}
```

---

### 模式 5：带回退数据的重试

```java
public void executeWithRetry(Template template, Map<String, Object> data, Writer writer) 
        throws TemplateException, IOException {
    try {
        template.execute(writer, data);
    } catch (TemplateExecutionException e) {
        if (e.getMessage().contains("nil pointer")) {
            // 尝试使用默认数据
            logger.warn("执行失败，使用默认数据重试");
            Map<String, Object> defaultData = createDefaultData();
            writer.getBuffer().setLength(0); // 清除之前的输出
            template.execute(writer, defaultData);
        } else {
            throw e; // 重新抛出其他错误
        }
    }
}
```

---

## 最佳实践

### 1. 捕获特定异常

捕获您需要的最具体异常类型：

```java
// 好：特定异常处理
try {
    template.parse(text);
} catch (TemplateParseException e) {
    handleSyntaxError(e);
}

// 差：捕获通用 Exception
try {
    template.parse(text);
} catch (Exception e) {
    // 捕获一切，包括不相关的错误
}
```

---

### 2. 根据环境使用适当策略

```java
Template template = new Template("app");

if (environment.isDevelopment()) {
    // 在开发期间尽早捕获错误
    template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
} else {
    // 在生产环境中宽容处理
    template.withMissingKeyPolicy(MissingKeyPolicy.INVALID);
}
```

---

### 3. 记录上下文信息

记录错误时，包含相关上下文：

```java
try {
    template.execute(writer, data);
} catch (TemplateExecutionException e) {
    logger.error("模板执行失败\n" +
                 "模板: {}\n" +
                 "数据键: {}\n" +
                 "错误: {}", 
                 template.name(),
                 data.keySet(),
                 e.getMessage(),
                 e);
}
```

---

### 4. 提供用户友好的消息

将技术错误转化为用户友好的消息：

```java
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    if (e instanceof TemplateParseException) {
        showUserError("模板存在语法错误。请检查模板代码。");
    } else if (e instanceof TemplateNotFoundException) {
        showUserError("缺少必需的模板。请联系支持。");
    } else {
        showUserError("渲染内容时发生错误。请重试。");
    }
}
```

---

### 5. 开发环境快速失败

在开发期间配置严格的错误检查：

```java
Template template = new Template("dev")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

// 带验证的解析
try {
    template.parse(templateText);
} catch (TemplateParseException e) {
    // 向开发者显示详细错误
    throw new DevelopmentException("修复模板语法: " + e.getMessage(), e);
}
```

---

### 6. 生产环境优雅处理错误

在生产环境中具有韧性：

```java
public String safeRender(Template template, Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        logger.error("渲染失败，使用回退内容", e);
        return getDefaultContent();
    }
}
```

---

### 7. 文档化预期异常

记录哪些方法抛出哪些异常：

```java
/**
 * 解析并执行模板。
 *
 * @param templateText 模板源
 * @param data 数据模型
 * @return 渲染后的输出
 * @throws TemplateParseException 如果模板语法无效
 * @throws TemplateExecutionException 如果执行失败（例如缺失字段）
 * @throws IOException 如果写入输出失败
 */
public String render(String templateText, Map<String, Object> data) 
        throws TemplateParseException, TemplateExecutionException, IOException {
    // 实现...
}
```

---

### 8. 测试错误场景

为错误条件编写测试：

```java
@Test
void testMissingFieldError() {
    Template template = new Template("test")
        .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    template.parse("{{.MissingField}}");
    
    assertThrows(TemplateExecutionException.class, () -> {
        template.execute(new StringWriter(), new HashMap<>());
    });
}

@Test
void testSyntaxError() {
    Template template = new Template("test");
    
    assertThrows(TemplateParseException.class, () -> {
        template.parse("{{if .Condition}}"); // 缺少 {{end}}
    });
}
```

---

## 摘要

gotemplate4j 提供了全面的异常层次结构：

- **TemplateException**：所有模板错误的基类
- **TemplateParseException**：解析期间的语法错误
- **TemplateExecutionException**：执行期间的运行时错误
- **TemplateNotFoundException**：缺失的模板引用
- **MissingKeyPolicy**：控制缺失映射键的行为

最佳实践：
- 捕获特定异常类型
- 根据环境使用适当策略
- 记录上下文信息
- 提供用户友好的错误消息
- 开发环境快速失败，生产环境保持韧性
- 充分测试错误场景

更多信息：
- 参见 [Template API](template-api.md) 了解配置选项
- 参见 [错误处理指南](../user-guide/error-handling.md) 了解全面策略
- 参见 [Functions API](function-api.md) 了解自定义函数错误处理
