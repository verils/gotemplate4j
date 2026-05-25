# 最佳实践指南

本指南提供了有效使用 gotemplate4j 的全面最佳实践，涵盖设计模式、代码组织、错误处理和可维护性。

---

## 目录

- [设计原则](#设计原则)
- [模板组织](#模板组织)
- [数据建模](#数据建模)
- [函数设计](#函数设计)
- [错误处理](#错误处理)
- [测试策略](#测试策略)
- [代码组织](#代码组织)
- [可维护性](#可维护性)
- [常用模式](#常用模式)

---

## 设计原则

### 1. 保持模板简洁

模板应专注于展示，而非业务逻辑。

```gotemplate
<!-- 不好：模板中包含复杂逻辑 -->
{{if and (gt .score 80) (lt .score 90)}}
  Grade: B
{{else if and (ge .score 90) (le .score 100)}}
  Grade: A
{{else if and (ge .score 70) (lt .score 80)}}
  Grade: C
{{end}}
```

```java
// 推荐：逻辑放在 Java 代码中
public class GradeCalculator {
    public static String calculateGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "F";
    }
}

String grade = GradeCalculator.calculateGrade(score);
data.put("grade", grade);
```

```gotemplate
<!-- 模板保持简洁 -->
Grade: {{.grade}}
```

**理由：** 业务逻辑放在 Java 中更易于测试、调试和维护。

---

### 2. 单一职责

每个模板应有一个明确的用途。

```gotemplate
<!-- 不好：一个模板包含所有内容 -->
{{define "page"}}
  <html>
    <head>{{template "header" .}}</head>
    <body>
      {{template "navigation" .}}
      {{template "content" .}}
      {{template "sidebar" .}}
      {{template "footer" .}}
    </body>
  </html>
{{end}}

<!-- 推荐：不同关注点使用不同模板 -->
<!-- layout.html -->
<html>
  <head>{{block "head" .}}{{end}}</head>
  <body>{{block "body" .}}{{end}}</body>
</html>

<!-- page.html -->
{{define "head"}}<title>{{.title}}</title>{{end}}
{{define "body"}}
  {{template "navigation" .}}
  <main>{{.content}}</main>
{{end}}
```

---

### 3. DRY（不要重复自己）

使用模板定义来避免重复。

```gotemplate
<!-- 不好：重复的 HTML 结构 -->
<div class="card">
  <h3>{{.item1.name}}</h3>
  <p>{{.item1.description}}</p>
</div>
<div class="card">
  <h3>{{.item2.name}}</h3>
  <p>{{.item2.description}}</p>
</div>

<!-- 推荐：使用 range 配合模板 -->
{{define "card"}}
<div class="card">
  <h3>{{.name}}</h3>
  <p>{{.description}}</p>
</div>
{{end}}

{{range .items}}
  {{template "card" .}}
{{end}}
```

---

## 模板组织

### 目录结构

按逻辑组织模板：

```
templates/
├── layouts/
│   ├── base.html
│   ├── admin.html
│   └── email.html
├── partials/
│   ├── header.html
│   ├── footer.html
│   ├── navigation.html
│   └── sidebar.html
├── pages/
│   ├── home.html
│   ├── profile.html
│   └── settings.html
└── emails/
    ├── welcome.html
    └── notification.html
```

### 命名规范

使用一致的命名方式：

```java
// 推荐：清晰、描述性的名称
Template userDashboard = new Template("user-dashboard");
Template welcomeEmail = new Template("email-welcome");
Template navigationPartial = new Template("partial-navigation");

// 不好：不清晰的名称
Template t1 = new Template("t1");
Template temp = new Template("temp");
```

### 模板继承层次

设计清晰的继承链：

```gotemplate
<!-- base.html - 根模板 -->
<!DOCTYPE html>
<html>
<head>
  {{block "head" .}}
    <title>{{.title}}</title>
  {{end}}
</head>
<body>
  {{block "body" .}}
    {{.content}}
  {{end}}
</body>
</html>

<!-- page.html - 继承 base -->
{{define "head"}}
  <title>{{.pageTitle}} - My Site</title>
  <link rel="stylesheet" href="/css/page.css">
{{end}}

{{define "body"}}
  {{template "header" .}}
  <main>{{.pageContent}}</main>
  {{template "footer" .}}
{{end}}
```

---

## 数据建模

### 为模板使用 DTO

为模板创建专门的数据传输对象：

```java
// 推荐：专用的 DTO
public class UserProfileView {
    private String displayName;
    private String email;
    private String avatarUrl;
    private int followerCount;

    // Getters 和 setters
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    // ...
}

// 使用方式
UserProfileView view = new UserProfileView();
view.setDisplayName(user.getName());
view.setEmail(user.getEmail());
view.setAvatarUrl(user.getAvatarUrl());
view.setFollowerCount(user.getFollowerCount());

template.execute(writer, view);
```

```java
// 不好：直接传递领域实体
template.execute(writer, userEntity); // 可能暴露敏感字段
```

### 扁平化嵌套结构

避免模板中过深的数据访问嵌套：

```java
// 不好：深层嵌套
Map<String, Object> data = new HashMap<>();
data.put("user", Map.of(
    "profile", Map.of(
        "settings", Map.of(
            "theme", "dark"
        )
    )
));
// 模板：{{.user.profile.settings.theme}}
```

```java
// 推荐：扁平结构
Map<String, Object> data = new HashMap<>();
data.put("theme", "dark");
// 模板：{{.theme}}
```

### 显式处理 Null 值

为可选值提供默认值：

```java
Map<String, Object> data = new HashMap<>();
data.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
data.put("bio", user.getBio() != null ? user.getBio() : "No bio yet");
```

或使用自定义函数：

```java
Function defaultIfNull = args -> {
    return args[0] != null ? args[0] : args[1];
};

// 模板：{{defaultIfNull .nickname .username}}
```

---

## 函数设计

### 小而专注的函数

每个函数应做好一件事：

```java
// 推荐：单一职责
Function upperCase = args -> ((String) args[0]).toUpperCase();
Function lowerCase = args -> ((String) args[0]).toLowerCase();
Function titleCase = args -> StringUtils.capitalize((String) args[0]);

// 不好：多功能函数
Function stringTransform = args -> {
    String operation = (String) args[0];
    String value = (String) args[1];
    switch (operation) {
        case "upper": return value.toUpperCase();
        case "lower": return value.toLowerCase();
        case "title": return StringUtils.capitalize(value);
        default: throw new IllegalArgumentException("Unknown operation");
    }
};
```

### 为函数编写文档

为自定义函数提供清晰的文档：

```java
/**
 * 将数字格式化为货币形式。
 *
 * 用法：{{currency .amount}} 或 {{currency .amount "USD"}}
 *
 * 参数：
 *   - amount (Number)：要格式化的金额
 *   - currencyCode (String, 可选)：ISO 货币代码（默认：USD）
 *
 * 返回：格式化后的字符串（例如，"$1,234.56"）
 */
Function currency = args -> {
    if (args.length < 1 || args.length > 2) {
        throw new IllegalArgumentException("currency requires 1-2 arguments");
    }

    Number amount = (Number) args[0];
    String currencyCode = args.length == 2 ? (String) args[1] : "USD";

    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
    return formatter.format(amount.doubleValue());
};
```

### 验证参数

始终验证函数输入：

```java
Function safeSubstring = args -> {
    // 检查参数数量
    if (args.length < 2 || args.length > 3) {
        throw new IllegalArgumentException(
            "substring requires 2-3 arguments: string, start, [length]"
        );
    }

    // 检查类型
    if (!(args[0] instanceof String)) {
        throw new IllegalArgumentException("First argument must be a string");
    }
    if (!(args[1] instanceof Number)) {
        throw new IllegalArgumentException("Second argument must be a number");
    }

    String str = (String) args[0];
    int start = ((Number) args[1]).intValue();

    // 验证范围
    if (start < 0 || start > str.length()) {
        throw new IllegalArgumentException("Start index out of bounds");
    }

    if (args.length == 3) {
        if (!(args[2] instanceof Number)) {
            throw new IllegalArgumentException("Third argument must be a number");
        }
        int length = ((Number) args[2]).intValue();
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be negative");
        }
        return str.substring(start, Math.min(start + length, str.length()));
    }

    return str.substring(start);
};
```

### 纯函数

优先使用纯函数（无副作用）：

```java
// 推荐：纯函数
Function add = args -> {
    double sum = 0;
    for (Object arg : args) {
        sum += ((Number) arg).doubleValue();
    }
    return sum;
};

// 不好：有副作用的函数
Function logAndReturn = args -> {
    System.out.println("Function called: " + Arrays.toString(args)); // 副作用！
    return args[0];
};
```

---

## 错误处理

### 使用适当的 MissingKeyPolicy

不同环境使用不同策略：

```java
public class TemplateFactory {

    public static Template createDevelopmentTemplate(String name) {
        // 严格策略可尽早捕获错误
        return new Template(name)
            .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
    }

    public static Template createProductionTemplate(String name) {
        // 宽松策略可防止崩溃
        return new Template(name)
            .withMissingKeyPolicy(MissingKeyPolicy.INVALID);
    }
}
```

### 优雅降级

在生产环境中优雅处理错误：

```java
public String safeRender(Template template, Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (TemplateException e) {
        logger.error("Template rendering failed", e);
        return "<div class='error'>Content temporarily unavailable</div>";
    }
}
```

### 开发环境中提供详细错误信息

在开发过程中提供有用的错误信息：

```java
try {
    template.execute(writer, data);
} catch (TemplateException e) {
    if (isDevelopmentMode()) {
        String errorMessage = String.format(
            "Template Error:\n" +
            "Type: %s\n" +
            "Message: %s\n" +
            "Template: %s\n" +
            "Data keys: %s",
            e.getClass().getSimpleName(),
            e.getMessage(),
            template.name(),
            data.keySet()
        );
        throw new RuntimeException(errorMessage, e);
    } else {
        logger.error("Template execution failed", e);
        throw new RuntimeException("Rendering failed", e);
    }
}
```

---

## 测试策略

### 对模板进行单元测试

使用各种输入测试模板：

```java
class UserProfileTemplateTest {

    private Template template;

    @BeforeEach
    void setUp() throws TemplateParseException {
        template = new Template("user-profile");
        template.parse("Hello, {{.name}}! You have {{.messageCount}} messages.");
    }

    @Test
    void testBasicRendering() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        data.put("messageCount", 5);

        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Hello, Alice! You have 5 messages.", writer.toString());
    }

    @Test
    void testZeroMessages() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Bob");
        data.put("messageCount", 0);

        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Hello, Bob! You have 0 messages.", writer.toString());
    }

    @Test
    void testMissingField() throws Exception {
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Charlie");
        // messageCount 缺失

        assertThrows(TemplateExecutionException.class, () -> {
            template.execute(new StringWriter(), data);
        });
    }
}
```

### 测试自定义函数

彻底测试自定义函数：

```java
class CurrencyFunctionTest {

    private Function currency;

    @BeforeEach
    void setUp() {
        currency = args -> {
            Number amount = (Number) args[0];
            String code = args.length == 2 ? (String) args[1] : "USD";
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            return formatter.format(amount.doubleValue());
        };
    }

    @Test
    void testDefaultCurrency() {
        assertEquals("$1,234.56", currency.invoke(1234.56));
    }

    @Test
    void testZeroAmount() {
        assertEquals("$0.00", currency.invoke(0));
    }

    @Test
    void testNegativeAmount() {
        assertEquals("-$100.00", currency.invoke(-100));
    }

    @Test
    void testInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> currency.invoke());
        assertThrows(IllegalArgumentException.class, () -> currency.invoke("not a number"));
    }
}
```

### 集成测试

测试完整的模板工作流：

```java
class EmailTemplateIntegrationTest {

    @Test
    void testWelcomeEmailFlow() throws Exception {
        // 设置
        Template emailTemplate = new Template("welcome-email");
        emailTemplate.parseFile(Paths.get("templates/emails/welcome.html"));

        User newUser = new User("alice", "alice@example.com");

        // 执行
        Map<String, Object> data = new HashMap<>();
        data.put("username", newUser.getUsername());
        data.put("email", newUser.getEmail());

        StringWriter writer = new StringWriter();
        emailTemplate.execute(writer, data);

        // 验证
        String result = writer.toString();
        assertTrue(result.contains("Welcome, alice!"));
        assertTrue(result.contains("alice@example.com"));
        assertTrue(result.contains("<html>"));
    }
}
```

---

## 代码组织

### 模板工厂模式

集中管理模板创建：

```java
public class TemplateFactory {

    private static final Map<String, Function> SHARED_FUNCTIONS = new HashMap<>();

    static {
        SHARED_FUNCTIONS.put("formatDate", new FormatDateFunction());
        SHARED_FUNCTIONS.put("currency", new CurrencyFunction());
        SHARED_FUNCTIONS.put("truncate", new TruncateFunction());
    }

    public static Template createWebTemplate(String name) {
        return new Template(name, SHARED_FUNCTIONS)
            .withMissingKeyPolicy(getPolicyForEnvironment());
    }

    public static Template createEmailTemplate(String name) {
        Map<String, Function> emailFunctions = new HashMap<>(SHARED_FUNCTIONS);
        emailFunctions.put("unsubscribeLink", new UnsubscribeLinkFunction());

        return new Template(name, emailFunctions)
            .withMissingKeyPolicy(getPolicyForEnvironment());
    }

    private static MissingKeyPolicy getPolicyForEnvironment() {
        return isProduction() ? MissingKeyPolicy.INVALID : MissingKeyPolicy.ERROR;
    }
}
```

### 模板服务层

封装模板操作：

```java
@Service
public class TemplateRenderingService {

    private final TemplateCache templateCache;

    public TemplateRenderingService(TemplateCache templateCache) {
        this.templateCache = templateCache;
    }

    public String renderUserProfile(User user) {
        Template template = templateCache.get("user-profile");

        Map<String, Object> data = new HashMap<>();
        data.put("displayName", user.getDisplayName());
        data.put("joinDate", formatDate(user.getJoinDate()));
        data.put("stats", getUserStats(user));

        try {
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        } catch (Exception e) {
            throw new RenderingException("Failed to render user profile", e);
        }
    }

    public String renderWelcomeEmail(User user) {
        Template template = templateCache.get("email-welcome");

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("verificationLink", generateVerificationLink(user));

        try {
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            return writer.toString();
        } catch (Exception e) {
            throw new RenderingException("Failed to render welcome email", e);
        }
    }
}
```

---

## 可维护性

### 对模板进行版本控制

将模板视为代码：

```bash
# 将模板与代码一起提交
git add templates/
git commit -m "Update user profile template layout"
```

### 记录模板变更

为模板修改保留变更日志：

```markdown
## 模板变更

### 2024-01-15
- 更新 `user-profile.html` 以显示关注者数量
- 新增 `email-welcome.html` 用于新用户引导

### 2024-01-10
- 重构 `base.html`，使用 blocks 替代 includes
- 修复 `comment-list.html` 中的 XSS 漏洞
```

### 使用一致的格式化

保持模板格式一致：

```gotemplate
<!-- 推荐：一致的缩进 -->
<div class="user-card">
  <h2>{{.name}}</h2>
  <p>{{.bio}}</p>
  {{if .isActive}}
    <span class="badge">Active</span>
  {{end}}
</div>

<!-- 不好：不一致的格式化 -->
<div class="user-card">
<h2>{{.name}}</h2>
  <p>{{.bio}}</p>
    {{if .isActive}}
<span class="badge">Active</span>
  {{end}}
    </div>
```

### 为复杂模板添加注释

添加注释以提高清晰度：

```gotemplate
{{/* 主页面布局 - 继承 base.html */}}
{{define "head"}}
  {{/* 页面特定的样式表 */}}
  <link rel="stylesheet" href="/css/page.css">
{{end}}

{{define "body"}}
  {{/* 导航栏 */}}
  {{template "navigation" .}}

  {{/* 主内容区域 */}}
  <main>
    {{range .articles}}
      {{/* 文章卡片组件 */}}
      {{template "article-card" .}}
    {{end}}
  </main>

  {{/* 页脚部分 */}}
  {{template "footer" .}}
{{end}}
```

---

## 常用模式

### 模式 1：使用 Block 的布局

```gotemplate
<!-- base.html -->
<!DOCTYPE html>
<html>
<head>
  {{block "head" .}}
    <title>{{.title}}</title>
  {{end}}
</head>
<body>
  {{block "body" .}}
    {{.content}}
  {{end}}
</body>
</html>

<!-- page.html -->
{{define "head"}}
  <title>{{.pageTitle}} - My Site</title>
  <link rel="stylesheet" href="/css/page.css">
{{end}}

{{define "body"}}
  <h1>{{.heading}}</h1>
  <p>{{.text}}</p>
{{end}}
```

### 模式 2：组件组合

```gotemplate
{{define "button"}}
<button class="btn btn-{{.style|default "primary"}}">
  {{.label}}
</button>
{{end}}

{{define "card"}}
<div class="card">
  <div class="card-header">{{.title}}</div>
  <div class="card-body">{{.content}}</div>
  {{if .showFooter}}
    <div class="card-footer">{{.footer}}</div>
  {{end}}
</div>
{{end}}

<!-- 使用方式 -->
{{template "card" (dict "title" "My Card" "content" "Content here" "showFooter" true)}}
```

### 模式 3：条件区块

```gotemplate
{{if .isAdmin}}
  {{template "admin-controls" .}}
{{else if .isModerator}}
  {{template "moderator-controls" .}}
{{else}}
  {{template "user-controls" .}}
{{end}}
```

### 模式 4：带空状态的列表

```gotemplate
{{if .items}}
  <ul>
    {{range .items}}
      <li>{{.name}}</li>
    {{end}}
  </ul>
{{else}}
  <p class="empty-state">No items found.</p>
{{end}}
```

### 模式 5：分页

```gotemplate
<div class="pagination">
  {{if .hasPrevious}}
    <a href="?page={{.previousPage}}">Previous</a>
  {{end}}

  <span>Page {{.currentPage}} of {{.totalPages}}</span>

  {{if .hasNext}}
    <a href="?page={{.nextPage}}">Next</a>
  {{end}}
</div>
```

---

## 性能最佳实践

详见[性能调优指南](performance.md)了解详细的性能优化策略。

快速提示：
- 模板只解析一次，多次执行
- 缓存已解析的模板
- 保持模板简洁
- 尽量减少传递给模板的数据
- 避免在循环中进行昂贵的操作

---

## 安全最佳实践

详见[安全注意事项](security.md)了解全面的安全指南。

快速提示：
- 永远不要将用户输入解析为模板
- 适当地转义输出
- 验证所有函数参数
- 限制资源使用
- 仅使用白名单模板

---

## 总结

gotemplate4j 的最佳实践：

1. **保持模板简洁** - 仅包含展示逻辑
2. **系统化组织** - 清晰的目录结构和命名方式
3. **精心设计数据模型** - 使用 DTO，扁平化结构
4. **设计专注的函数** - 单一职责，验证输入
5. **适当地处理错误** - 不同环境使用不同策略
6. **彻底测试** - 单元测试、集成测试
7. **良好地组织代码** - 工厂模式、服务层
8. **勤于维护** - 版本控制、文档、格式化
9. **遵循常用模式** - 布局、组件、条件判断
10. **优先考虑性能和安全** - 缓存、验证、转义

更多信息：
- 优化策略请参见[性能调优](performance.md)
- 安全指南请参见[安全注意事项](security.md)
- 详细 API 文档请参见[API 参考](../api-reference/)
- 实际使用模式请参见[示例](../examples/)
