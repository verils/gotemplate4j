# 安全注意事项

本指南涵盖使用 gotemplate4j 的安全最佳实践，包括模板注入防护、安全的函数实现和数据验证。

---

## 目录

- [安全概述](#安全概述)
- [模板注入攻击](#模板注入攻击)
- [输入验证](#输入验证)
- [安全的函数实现](#安全的函数实现)
- [转义与清理](#转义与清理)
- [访问控制](#访问控制)
- [拒绝服务攻击防护](#拒绝服务攻击防护)
- [安全检查清单](#安全检查清单)

---

## 安全概述

模板引擎可能是应用程序中安全敏感的组件。不当使用可能导致：

- **模板注入**：攻击者注入恶意模板代码
- **信息泄露**：模板暴露敏感数据
- **拒绝服务（DoS）**：恶意模板导致资源耗尽
- **跨站脚本攻击（XSS）**：Web 上下文中的未转义输出

gotemplate4j 提供了减轻这些风险的功能，但安全使用取决于正确的实现。

---

## 模板注入攻击

### 什么是模板注入？

当用户控制的输入在没有适当验证的情况下被用作模板的一部分时，就会发生模板注入：

```java
// 存在漏洞：用户输入被直接用于模板
String userInput = request.getParameter("template");
Template template = new Template("user-template");
template.parse(userInput); // 危险！
template.execute(writer, data);
```

攻击者可以提供：
```gotemplate
{{.secretPassword}}
{{index .privateData "creditCard"}}
```

### 防护策略

#### 1. 永远不要将用户输入解析为模板

```java
// 不好：允许模板注入
String userTemplate = getUserInput();
template.parse(userTemplate);

// 推荐：将用户输入作为数据，而非模板
Map<String, Object> data = new HashMap<>();
data.put("userContent", getUserInput());
template.execute(writer, data);
```

#### 2. 使用白名单模板

仅允许预定义的模板：

```java
public class SafeTemplateEngine {
    private final Map<String, Template> allowedTemplates;

    public SafeTemplateEngine() {
        allowedTemplates = new HashMap<>();

        // 仅注册已批准的模板
        Template welcome = new Template("welcome");
        welcome.parse("Hello, {{.name}}!");
        allowedTemplates.put("welcome", welcome);

        Template goodbye = new Template("goodbye");
        goodbye.parse("Goodbye, {{.name}}!");
        allowedTemplates.put("goodbye", goodbye);
    }

    public String render(String templateName, Map<String, Object> data) throws Exception {
        Template template = allowedTemplates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not allowed: " + templateName);
        }

        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    }
}
```

#### 3. 验证模板名称

如果必须动态加载模板，请严格验证名称：

```java
public String render(String templateName, Map<String, Object> data) throws Exception {
    // 仅允许字母数字和下划线
    if (!templateName.matches("^[a-zA-Z0-9_]+$")) {
        throw new IllegalArgumentException("Invalid template name");
    }

    // 防止路径遍历
    if (templateName.contains("..") || templateName.contains("/")) {
        throw new IllegalArgumentException("Invalid template name");
    }

    Path templatePath = Paths.get("templates").resolve(templateName + ".tmpl");
    Template template = new Template(templateName);
    template.parseFile(templatePath);

    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    return writer.toString();
}
```

---

## 输入验证

### 在传递给模板之前验证数据

始终在将数据传递给模板之前进行验证和清理：

```java
public void renderUserProfile(User user) throws Exception {
    // 验证输入
    if (user.getName() == null || user.getName().isEmpty()) {
        throw new IllegalArgumentException("Name is required");
    }

    if (user.getName().length() > 100) {
        throw new IllegalArgumentException("Name too long");
    }

    // 必要时进行清理
    String sanitizedName = HtmlUtils.htmlEscape(user.getName());

    Map<String, Object> data = new HashMap<>();
    data.put("name", sanitizedName);

    template.execute(writer, data);
}
```

### 限制数据暴露

仅向模板传递必要的数据：

```java
// 不好：传递包含敏感字段的整个用户对象
template.execute(writer, userObject); // 可能包含密码、社保号等

// 推荐：仅提取需要的字段
Map<String, Object> data = new HashMap<>();
data.put("displayName", user.getDisplayName());
data.put("email", user.getEmail());
template.execute(writer, data);
```

### 在开发环境中使用 MissingKeyPolicy.ERROR

尽早发现意外的数据暴露：

```java
Template template = new Template("profile")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);

// 如果模板引用了未定义的字段，将抛出异常
template.execute(writer, data);
```

---

## 安全的函数实现

如果实现不够仔细，自定义函数可能引入安全漏洞。

### 验证函数参数

始终验证参数类型和值：

```java
// 不安全：没有验证
Function unsafeReadFile = args -> {
    String filename = (String) args[0]; // 没有验证！
    return Files.readString(Paths.get(filename)); // 可以读取任何文件！
};

// 安全：已验证并受限
Function safeReadFile = args -> {
    if (args.length != 1 || !(args[0] instanceof String)) {
        throw new IllegalArgumentException("Expected one string argument");
    }

    String filename = (String) args[0];

    // 验证文件名
    if (filename.contains("..") || filename.startsWith("/")) {
        throw new IllegalArgumentException("Invalid filename");
    }

    // 限制到特定目录
    Path basePath = Paths.get("/safe/templates");
    Path filePath = basePath.resolve(filename).normalize();

    if (!filePath.startsWith(basePath)) {
        throw new IllegalArgumentException("Access denied");
    }

    return Files.readString(filePath);
};
```

### 避免危险操作

不要实现可以执行任意代码或访问敏感资源的函数：

```java
// 危险：允许任意代码执行
Function exec = args -> {
    String command = (String) args[0];
    Process process = Runtime.getRuntime().exec(command);
    // ...
};

// 危险：允许读取任意文件
Function readFile = args -> {
    String path = (String) args[0];
    return Files.readString(Paths.get(path));
};

// 危险：允许网络访问
Function fetchUrl = args -> {
    String url = (String) args[0];
    return new URL(url).openStream();
};
```

### 限制资源使用

通过限制资源消耗来防止拒绝服务：

```java
// 安全：有限制的字符串重复
Function safeRepeat = args -> {
    String str = (String) args[0];
    int count = ((Number) args[1]).intValue();

    // 限制最大重复次数
    if (count > 1000) {
        throw new IllegalArgumentException("Count too large (max 1000)");
    }

    if (str.length() * count > 100000) {
        throw new IllegalArgumentException("Result too large");
    }

    return str.repeat(count);
};
```

### 为函数设置时间限制

对于可能较慢的操作，添加超时：

```java
Function safeComputation = args -> {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Object> future = executor.submit(() -> {
        // 可能较慢的操作
        return performCalculation(args);
    });

    try {
        return future.get(5, TimeUnit.SECONDS); // 5 秒超时
    } catch (TimeoutException e) {
        future.cancel(true);
        throw new RuntimeException("Operation timed out");
    } finally {
        executor.shutdownNow();
    }
};
```

---

## 转义与清理

### HTML 转义

在生成 HTML 时，转义用户输入以防止 XSS：

```java
// 使用内置的 html 函数
template.parse("<div>{{html .userInput}}</div>");

// 或在 Java 代码中转义
import org.owasp.encoder.Encode;

String safeInput = Encode.forHtml(userInput);
data.put("userInput", safeInput);
```

### JavaScript 转义

在 JavaScript 中嵌入数据时：

```gotemplate
<script>
  var userName = "{{js .userName}}";
</script>
```

或在 Java 中：
```java
import org.owasp.encoder.Encode;

String safeJs = Encode.forJavaScript(userInput);
data.put("userName", safeJs);
```

### URL 转义

在构建 URL 时：

```gotemplate
<a href="/search?q={{urlquery .searchTerm}}">Search</a>
```

或在 Java 中：
```java
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
data.put("searchTerm", encoded);
```

### 上下文感知转义

不同的上下文需要不同的转义方式：

```java
public class ContextAwareEscaper {

    public static String escapeForHtml(String input) {
        return input == null ? "" :
            input.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;")
                 .replace("'", "&#x27;");
    }

    public static String escapeForHtmlAttribute(String input) {
        return escapeForHtml(input);
    }

    public static String escapeForJavaScript(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    public static String escapeForUrl(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 自动转义策略

考虑实现基于上下文的自动转义：

```java
public class AutoEscapingTemplate {
    private final Template template;

    public AutoEscapingTemplate(Template template) {
        this.template = template;
    }

    public void execute(Writer writer, Map<String, Object> data, String context)
            throws Exception {
        // 根据上下文自动转义
        Map<String, Object> escapedData = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                switch (context) {
                    case "html":
                        escapedData.put(entry.getKey(), escapeForHtml(value));
                        break;
                    case "javascript":
                        escapedData.put(entry.getKey(), escapeForJavaScript(value));
                        break;
                    case "url":
                        escapedData.put(entry.getKey(), escapeForUrl(value));
                        break;
                    default:
                        escapedData.put(entry.getKey(), value);
                }
            } else {
                escapedData.put(entry.getKey(), entry.getValue());
            }
        }

        template.execute(writer, escapedData);
    }
}
```

---

## 访问控制

### 限制模板能力

使用有限的函数集创建模板：

```java
// 安全的函数集 - 无危险操作
Map<String, Function> safeFunctions = new HashMap<>();
safeFunctions.put("upper", args -> ((String) args[0]).toUpperCase());
safeFunctions.put("lower", args -> ((String) args[0]).toLowerCase());
safeFunctions.put("trim", args -> ((String) args[0]).trim());
// 注意：没有文件 I/O、没有网络访问、没有代码执行

Template safeTemplate = new Template("safe", safeFunctions);
```

### 基于角色的模板访问

限制用户可以访问的模板：

```java
public class RoleBasedTemplateEngine {
    private final Map<String, Set<String>> rolePermissions;
    private final Map<String, Template> templates;

    public RoleBasedTemplateEngine() {
        // 定义权限
        rolePermissions = new HashMap<>();
        rolePermissions.put("admin", Set.of("admin-dashboard", "user-list", "settings"));
        rolePermissions.put("user", Set.of("profile", "dashboard"));
        rolePermissions.put("guest", Set.of("welcome", "login"));

        // 加载模板...
    }

    public String render(String userRole, String templateName, Map<String, Object> data)
            throws Exception {
        Set<String> allowedTemplates = rolePermissions.get(userRole);
        if (allowedTemplates == null || !allowedTemplates.contains(templateName)) {
            throw new SecurityException("Access denied to template: " + templateName);
        }

        Template template = templates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    }
}
```

### 模板沙箱化

使用 Java SecurityManager 以受限权限运行模板（高级用法）：

```java
// 高级：使用 SecurityManager 限制模板执行
// 这需要仔细配置，超出了基本用法的范围
```

---

## 拒绝服务攻击防护

### 限制模板复杂度

防止复杂模板消耗过多资源：

```java
public class ResourceLimitedTemplate {
    private static final int MAX_TEMPLATE_SIZE = 10000; // 10KB
    private static final int MAX_EXECUTION_TIME_MS = 5000; // 5 秒

    public String render(String templateText, Map<String, Object> data) throws Exception {
        // 检查模板大小
        if (templateText.length() > MAX_TEMPLATE_SIZE) {
            throw new IllegalArgumentException("Template too large");
        }

        // 带超时的执行
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            Template template = new Template("limited");
            template.parse(templateText);
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        });

        try {
            return future.get(MAX_EXECUTION_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Template execution timed out");
        } finally {
            executor.shutdownNow();
        }
    }
}
```

### 限制迭代次数

防止无限或过长的循环：

```java
// 在自定义 range 函数中
Function safeRange = args -> {
    int count = ((Number) args[0]).intValue();

    // 限制最大迭代次数
    if (count > 10000) {
        throw new IllegalArgumentException("Range too large (max 10000)");
    }

    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
        result.add(i);
    }
    return result;
};
```

### 监控资源使用

跟踪并限制资源消耗：

```java
public class MonitoredTemplate {
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private static final long MAX_TOTAL_TIME_MS = 60000; // 1 分钟

    public void execute(Template template, Writer writer, Object data) throws Exception {
        // 检查是否超出时间预算
        if (totalExecutionTime.get() > MAX_TOTAL_TIME_MS) {
            throw new RuntimeException("Resource limit exceeded");
        }

        long start = System.currentTimeMillis();
        try {
            template.execute(writer, data);
            executionCount.incrementAndGet();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            totalExecutionTime.addAndGet(elapsed);
        }
    }

    public void resetBudget() {
        totalExecutionTime.set(0);
    }
}
```

---

## 安全检查清单

使用此清单确保模板的安全使用：

### 模板输入
- [ ] 永远不要将用户输入解析为模板代码
- [ ] 仅使用白名单模板
- [ ] 严格验证模板名称
- [ ] 防止路径遍历攻击
- [ ] 限制模板大小

### 数据处理
- [ ] 在传递给模板之前验证所有数据
- [ ] 仅传递必要的数据（最小化暴露）
- [ ] 清理用户提供的数据
- [ ] 在开发环境中使用 MissingKeyPolicy.ERROR
- [ ] 不要传递敏感数据（密码、令牌等）

### 函数安全
- [ ] 验证所有函数参数
- [ ] 不要实现危险操作（文件 I/O、代码执行）
- [ ] 限制函数中的资源使用
- [ ] 为慢速操作添加超时
- [ ] 使用最小权限原则

### 输出转义
- [ ] 适当地转义 HTML 输出
- [ ] 转义 JavaScript 上下文
- [ ] 转义 URL 参数
- [ ] 使用上下文感知转义
- [ ] 考虑自动转义策略

### 访问控制
- [ ] 按用例限制可用函数
- [ ] 实现基于角色的模板访问
- [ ] 验证用户权限
- [ ] 不要向普通用户暴露管理模板

### DoS 防护
- [ ] 限制模板复杂度
- [ ] 设置执行超时
- [ ] 限制迭代次数
- [ ] 监控资源使用
- [ ] 实现速率限制

### 监控与日志
- [ ] 记录模板执行错误
- [ ] 监控异常模式
- [ ] 跟踪资源消耗
- [ ] 对安全违规发出告警
- [ ] 定期安全审计

### 测试
- [ ] 测试模板注入漏洞
- [ ] 验证所有上下文中的转义
- [ ] 使用恶意输入进行测试
- [ ] 负载下的性能测试
- [ ] 安全渗透测试

---

## 常见安全反模式

### 反模式 1：信任用户输入

```java
// 不好：信任用户输入
String userTemplate = request.getParameter("tpl");
template.parse(userTemplate);

// 推荐：验证并限制
if (!ALLOWED_TEMPLATES.contains(userTemplate)) {
    throw new SecurityException("Invalid template");
}
```

### 反模式 2：暴露敏感数据

```java
// 不好：传递整个对象
template.execute(writer, userWithPassword);

// 推荐：提取安全字段
Map<String, Object> safeData = new HashMap<>();
safeData.put("username", user.getUsername());
safeData.put("email", user.getEmail());
template.execute(writer, safeData);
```

### 反模式 3：缺少转义

```gotemplate
<!-- 不好：未转义的用户输入 -->
<div>{{.userComment}}</div>

<!-- 推荐：已转义的输出 -->
<div>{{html .userComment}}</div>
```

### 反模式 4：危险的自定义函数

```java
// 不好：允许任意文件读取
Function readFile = args -> Files.readString(Paths.get((String) args[0]));

// 推荐：受限的文件访问
Function readTemplate = args -> {
    String name = (String) args[0];
    if (!name.matches("^[a-zA-Z0-9_-]+$")) {
        throw new IllegalArgumentException("Invalid name");
    }
    return Files.readString(TEMPLATES_DIR.resolve(name + ".tmpl"));
};
```

---

## 安全资源

### OWASP 指南

遵循 OWASP 的模板安全建议：
- [OWASP 模板注入](https://cheatsheetseries.owasp.org/cheatsheets/Injection_Prevention_Cheat_Sheet.html)
- [OWASP XSS 防护](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)

### 推荐库

增强安全性：
- **OWASP Java Encoder**：`org.owasp.encoder:encoder`
- **JSoup**：HTML 清理 `org.jsoup:jsoup`
- **Apache Commons Text**：字符串转义 `org.apache.commons:commons-text`

---

## 总结

gotemplate4j 的安全最佳实践：

1. **永远不要将用户输入解析为模板** - 最关键的规则
2. **验证和清理所有数据** - 在传递给模板之前
3. **适当地转义输出** - 根据上下文（HTML、JS、URL）
4. **实现安全的自定义函数** - 验证参数、限制资源
5. **控制访问** - 按角色限制模板和函数
6. **防止 DoS** - 限制复杂度、设置超时
7. **监控和审计** - 跟踪使用情况、检测异常

请记住：安全是一个过程，而非一个产品。定期审查和更新你的安全措施。

更多信息：
- 通用指南请参见[最佳实践](best-practices.md)
- 安全的错误报告请参见[错误处理](../user-guide/error-handling.md)
- 安全的使用模式请参见[示例](../examples/)
