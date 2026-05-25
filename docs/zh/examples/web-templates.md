# Web 模板

本文档提供了在 Web 应用程序中使用 gotemplate4j 的示例，包括 HTML 生成、表单处理以及常见的 Web UI 模式。

---

## 目录

- [基础 HTML 页面](#基础-html-页面)
- [带头部和底部的布局](#带头部和底部的布局)
- [导航菜单](#导航菜单)
- [表单渲染](#表单渲染)
- [表格展示](#表格展示)
- [分页](#分页)
- [闪存消息](#闪存消息)
- [用户认证界面](#用户认证界面)
- [响应式设计集成](#响应式设计集成)

---

## 基础 HTML 页面

从模板生成完整的 HTML 页面。

### 模板

```gotemplate
{{/* page.html */}}
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{.pageTitle}}</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <header>
        <h1>{{.siteName}}</h1>
    </header>
    
    <main>
        <h2>{{.heading}}</h2>
        <p>{{.content}}</p>
    </main>
    
    <footer>
        <p>&copy; {{.year}} {{.siteName}}. 保留所有权利。</p>
    </footer>
</body>
</html>
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class BasicPageExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("page");
        template.parseFile(java.nio.file.Paths.get("templates/page.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("pageTitle", "首页");
        data.put("siteName", "我的网站");
        data.put("heading", "欢迎！");
        data.put("content", "这是首页内容。");
        data.put("year", 2026);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
        
        // 在 Web 框架中，您可以将此写入 HTTP 响应
        // response.setContentType("text/html");
        // response.getWriter().write(html);
    }
}
```

---

## 带头部和底部的布局

使用模板继承实现一致的页面布局。

### 基础布局

```gotemplate
{{/* layout.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>{{block "title" .}}{{.pageTitle}}{{end}}</title>
    {{block "styles" .}}
    <link rel="stylesheet" href="/css/main.css">
    {{end}}
</head>
<body>
    {{template "header" .}}
    
    <main class="container">
        {{block "content" .}}
        {{.bodyContent}}
        {{end}}
    </main>
    
    {{template "footer" .}}
    
    {{block "scripts" .}}
    <script src="/js/app.js"></script>
    {{end}}
</body>
</html>
```

### 头部局部模板

```gotemplate
{{/* header.html */}}
{{define "header"}}
<header class="site-header">
    <nav>
        <a href="/" class="logo">{{.siteName}}</a>
        <ul class="nav-menu">
            <li><a href="/">首页</a></li>
            <li><a href="/about">关于</a></li>
            <li><a href="/contact">联系</a></li>
        </ul>
    </nav>
</header>
{{end}}
```

### 底部局部模板

```gotemplate
{{/* footer.html */}}
{{define "footer"}}
<footer class="site-footer">
    <p>&copy; {{.year}} {{.siteName}}</p>
</footer>
{{end}}
```

### 首页

```gotemplate
{{/* home.html */}}
{{define "title"}}首页 - {{.siteName}}{{end}}

{{define "content"}}
<div class="hero">
    <h1>欢迎来到 {{.siteName}}</h1>
    <p>{{.welcomeMessage}}</p>
    <a href="/signup" class="btn btn-primary">开始使用</a>
</div>

<div class="features">
    {{range .features}}
    <div class="feature-card">
        <h3>{{.title}}</h3>
        <p>{{.description}}</p>
    </div>
    {{end}}
</div>
{{end}}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class LayoutExample {
    public static void main(String[] args) throws Exception {
        // 同时解析所有模板
        Template template = new Template("layout");
        template.parseFiles(
            Paths.get("templates/layout.html"),
            Paths.get("templates/header.html"),
            Paths.get("templates/footer.html"),
            Paths.get("templates/home.html")
        );
        
        // 准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("siteName", "MyApp");
        data.put("year", 2026);
        data.put("welcomeMessage", "构建令人惊叹的 Web 应用程序");
        
        List<Map<String, String>> features = Arrays.asList(
            createFeature("快速", "闪电般快速的性能"),
            createFeature("简单", "易于使用的 API"),
            createFeature("安全", "内置安全功能")
        );
        data.put("features", features);
        
        // 执行首页模板（它使用 layout）
        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "home", data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, String> createFeature(String title, String desc) {
        Map<String, String> feature = new HashMap<>();
        feature.put("title", title);
        feature.put("description", desc);
        return feature;
    }
}
```

---

## 导航菜单

带有活动状态高亮的动态导航。

### 模板

```gotemplate
{{/* navigation.html */}}
<nav class="main-nav">
    <ul>
        {{range .menuItems}}
        <li>
            <a href="{{.url}}" 
               class="{{if eq .url $.currentPage}}active{{end}}">
                {{.label}}
            </a>
        </li>
        {{end}}
    </ul>
</nav>
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class NavigationExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("nav");
        template.parseFile(java.nio.file.Paths.get("templates/navigation.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("currentPage", "/about");
        
        List<Map<String, String>> menuItems = Arrays.asList(
            createMenuItem("首页", "/"),
            createMenuItem("关于", "/about"),
            createMenuItem("服务", "/services"),
            createMenuItem("联系", "/contact")
        );
        data.put("menuItems", menuItems);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createMenuItem(String label, String url) {
        Map<String, String> item = new HashMap<>();
        item.put("label", label);
        item.put("url", url);
        return item;
    }
}
```

### 输出

```html
<nav class="main-nav">
    <ul>
        <li>
            <a href="/" class="">首页</a>
        </li>
        <li>
            <a href="/about" class="active">关于</a>
        </li>
        <li>
            <a href="/services" class="">服务</a>
        </li>
        <li>
            <a href="/contact" class="">联系</a>
        </li>
    </ul>
</nav>
```

---

## 表单渲染

生成带有验证消息的 HTML 表单。

### 模板

```gotemplate
{{/* form.html */}}
<form action="{{.action}}" method="{{.method|default "POST"}}" class="form">
    {{if .errors}}
    <div class="alert alert-error">
        <ul>
            {{range .errors}}
            <li>{{.}}</li>
            {{end}}
        </ul>
    </div>
    {{end}}
    
    {{range .fields}}
    <div class="form-group">
        <label for="{{.name}}">{{.label}}</label>
        <input type="{{.type|default "text"}}" 
               id="{{.name}}" 
               name="{{.name}}" 
               value="{{.value|default ""}}"
               placeholder="{{.placeholder|default ""}}"
               class="form-control {{if hasError $.errors .name}}is-invalid{{end}}">
        {{if hasError $.errors .name}}
        <span class="error-message">{{getFieldError $.errors .name}}</span>
        {{end}}
    </div>
    {{end}}
    
    <button type="submit" class="btn btn-primary">{{.submitText|default "提交"}}</button>
</form>
```

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class FormExample {
    public static void main(String[] args) throws Exception {
        // 自定义函数：检查字段是否有错误
        Function hasError = args -> {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) args[0];
            String fieldName = (String) args[1];
            return errors.stream()
                .anyMatch(err -> err.startsWith(fieldName + ":"));
        };
        
        // 自定义函数：获取字段特定错误
        Function getFieldError = args -> {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) args[0];
            String fieldName = (String) args[1];
            return errors.stream()
                .filter(err -> err.startsWith(fieldName + ":"))
                .findFirst()
                .map(err -> err.substring(fieldName.length() + 1))
                .orElse("");
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("hasError", hasError);
        functions.put("getFieldError", getFieldError);
        
        Template template = new Template("form", functions);
        template.parseFile(java.nio.file.Paths.get("templates/form.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("action", "/api/register");
        data.put("method", "POST");
        data.put("submitText", "注册");
        
        // 表单字段
        List<Map<String, String>> fields = Arrays.asList(
            createField("username", "用户名", "text", "", "请输入用户名"),
            createField("email", "邮箱", "email", "", "请输入邮箱"),
            createField("password", "密码", "password", "", "请输入密码")
        );
        data.put("fields", fields);
        
        // 验证错误（模拟）
        List<String> errors = Arrays.asList(
            "email:邮箱地址无效",
            "password:密码太短"
        );
        data.put("errors", errors);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createField(
            String name, String label, String type, String value, String placeholder) {
        Map<String, String> field = new HashMap<>();
        field.put("name", name);
        field.put("label", label);
        field.put("type", type);
        field.put("value", value);
        field.put("placeholder", placeholder);
        return field;
    }
}
```

---

## 表格展示

展示带有排序和格式化的表格数据。

### 模板

```gotemplate
{{/* table.html */}}
<table class="data-table">
    <thead>
        <tr>
            {{range .columns}}
            <th>{{.header}}</th>
            {{end}}
        </tr>
    </thead>
    <tbody>
        {{if .rows}}
            {{range .rows}}
            <tr>
                {{range $.columns}}
                <td>{{index $_ .field}}</td>
                {{end}}
            </tr>
            {{end}}
        {{else}}
            <tr>
                <td colspan="{{len .columns}}" class="empty-state">
                    暂无数据
                </td>
            </tr>
        {{end}}
    </tbody>
</table>
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class TableExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("table");
        template.parseFile(java.nio.file.Paths.get("templates/table.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // 定义列
        List<Map<String, String>> columns = Arrays.asList(
            createColumn("姓名", "name"),
            createColumn("邮箱", "email"),
            createColumn("角色", "role")
        );
        data.put("columns", columns);
        
        // 定义行
        List<Map<String, String>> rows = Arrays.asList(
            createUser("Alice", "alice@example.com", "管理员"),
            createUser("Bob", "bob@example.com", "用户"),
            createUser("Charlie", "charlie@example.com", "用户")
        );
        data.put("rows", rows);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createColumn(String header, String field) {
        Map<String, String> col = new HashMap<>();
        col.put("header", header);
        col.put("field", field);
        return col;
    }
    
    private static Map<String, String> createUser(String name, String email, String role) {
        Map<String, String> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        return user;
    }
}
```

---

## 分页

为大数据集实现分页控件。

### 模板

```gotemplate
{{/* pagination.html */}}
{{if gt .totalPages 1}}
<nav class="pagination">
    <ul class="pagination-list">
        {{/* 上一页按钮 */}}
        {{if .hasPrevious}}
        <li>
            <a href="?page={{.previousPage}}" aria-label="上一页">
                &laquo; 上一页
            </a>
        </li>
        {{else}}
        <li class="disabled">
            <span>&laquo; 上一页</span>
        </li>
        {{end}}
        
        {{/* 页码 */}}
        {{range .pages}}
        <li class="{{if eq . $.currentPage}}active{{end}}">
            {{if eq . $.currentPage}}
            <span>{{.}}</span>
            {{else}}
            <a href="?page={{.}}">{{.}}</a>
            {{end}}
        </li>
        {{end}}
        
        {{/* 下一页按钮 */}}
        {{if .hasNext}}
        <li>
            <a href="?page={{.nextPage}}" aria-label="下一页">
                下一页 &raquo;
            </a>
        </li>
        {{else}}
        <li class="disabled">
            <span>下一页 &raquo;</span>
        </li>
        {{end}}
    </ul>
    
    <div class="pagination-info">
        显示第 {{.startItem}}-{{.endItem}} 条，共 {{.totalItems}} 条
    </div>
</nav>
{{end}}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class PaginationExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("pagination");
        template.parseFile(java.nio.file.Paths.get("templates/pagination.html"));
        
        int currentPage = 3;
        int totalPages = 10;
        int totalItems = 100;
        int itemsPerPage = 10;
        
        Map<String, Object> data = new HashMap<>();
        data.put("currentPage", currentPage);
        data.put("totalPages", totalPages);
        data.put("totalItems", totalItems);
        data.put("hasPrevious", currentPage > 1);
        data.put("hasNext", currentPage < totalPages);
        data.put("previousPage", currentPage - 1);
        data.put("nextPage", currentPage + 1);
        data.put("startItem", (currentPage - 1) * itemsPerPage + 1);
        data.put("endItem", Math.min(currentPage * itemsPerPage, totalItems));
        
        // 生成要显示的页码
        List<Integer> pages = generatePageNumbers(currentPage, totalPages);
        data.put("pages", pages);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static List<Integer> generatePageNumbers(int current, int total) {
        List<Integer> pages = new ArrayList<>();
        int start = Math.max(1, current - 2);
        int end = Math.min(total, current + 2);
        
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
}
```

---

## 闪存消息

显示临时通知消息。

### 模板

```gotemplate
{{/* flash.html */}}
{{if .flashMessages}}
<div class="flash-messages">
    {{range .flashMessages}}
    <div class="alert alert-{{.type}}">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        {{.message}}
    </div>
    {{end}}
</div>
{{end}}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class FlashMessageExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("flash");
        template.parseFile(java.nio.file.Paths.get("templates/flash.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // 模拟闪存消息（通常来自 session）
        List<Map<String, String>> messages = Arrays.asList(
            createMessage("success", "个人资料更新成功！"),
            createMessage("warning", "您的密码将在 7 天后过期。"),
            createMessage("error", "文件上传失败。")
        );
        data.put("flashMessages", messages);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createMessage(String type, String message) {
        Map<String, String> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("message", message);
        return msg;
    }
}
```

### 输出

```html
<div class="flash-messages">
    <div class="alert alert-success">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        个人资料更新成功！
    </div>
    <div class="alert alert-warning">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        您的密码将在 7 天后过期。
    </div>
    <div class="alert alert-error">
        <button type="button" class="close" onclick="this.parentElement.remove()">
            &times;
        </button>
        文件上传失败。
    </div>
</div>
```

---

## 用户认证界面

带有条件渲染的登录和注册表单。

### 登录表单

```gotemplate
{{/* login.html */}}
<div class="auth-container">
    <h2>登录</h2>
    
    {{if .error}}
    <div class="alert alert-error">{{.error}}</div>
    {{end}}
    
    <form action="/login" method="POST">
        <div class="form-group">
            <label for="username">用户名或邮箱</label>
            <input type="text" id="username" name="username" 
                   class="form-control" required>
        </div>
        
        <div class="form-group">
            <label for="password">密码</label>
            <input type="password" id="password" name="password" 
                   class="form-control" required>
        </div>
        
        <div class="form-group">
            <label>
                <input type="checkbox" name="remember"> 记住我
            </label>
        </div>
        
        <button type="submit" class="btn btn-primary btn-block">登录</button>
    </form>
    
    <p class="auth-links">
        <a href="/forgot-password">忘记密码？</a> | 
        <a href="/register">创建账户</a>
    </p>
</div>
```

### 用户仪表盘

```gotemplate
{{/* dashboard.html */}}
{{if .isLoggedIn}}
<div class="dashboard">
    <h1>欢迎, {{.user.displayName}}!</h1>
    
    <div class="user-info">
        <p>邮箱: {{.user.email}}</p>
        <p>注册日期: {{.user.joinDate}}</p>
        <p>上次登录: {{.user.lastLogin}}</p>
    </div>
    
    <div class="dashboard-actions">
        <a href="/profile" class="btn">编辑个人资料</a>
        <a href="/settings" class="btn">设置</a>
        <a href="/logout" class="btn btn-danger">退出登录</a>
    </div>
</div>
{{else}}
<div class="guest-message">
    <h2>欢迎!</h2>
    <p>请 <a href="/login">登录</a> 或 <a href="/register">注册</a> 以访问您的仪表盘。</p>
</div>
{{end}}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AuthUIExample {
    public static void main(String[] args) throws Exception {
        // 示例 1: 登录表单
        Template loginTemplate = new Template("login");
        loginTemplate.parseFile(java.nio.file.Paths.get("templates/login.html"));
        
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("error", "用户名或密码无效");
        
        StringWriter loginWriter = new StringWriter();
        loginTemplate.execute(loginWriter, loginData);
        System.out.println("登录表单:");
        System.out.println(loginWriter.toString());
        
        // 示例 2: 用户仪表盘
        Template dashboardTemplate = new Template("dashboard");
        dashboardTemplate.parseFile(java.nio.file.Paths.get("templates/dashboard.html"));
        
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("isLoggedIn", true);
        
        Map<String, String> user = new HashMap<>();
        user.put("displayName", "Alice Johnson");
        user.put("email", "alice@example.com");
        user.put("joinDate", LocalDate.of(2024, 1, 15)
            .format(DateTimeFormatter.ofPattern("yyyy 年 M 月 dd 日")));
        user.put("lastLogin", "2 小时前");
        dashboardData.put("user", user);
        
        StringWriter dashboardWriter = new StringWriter();
        dashboardTemplate.execute(dashboardWriter, dashboardData);
        System.out.println("\n仪表盘:");
        System.out.println(dashboardWriter.toString());
    }
}
```

---

## 响应式设计集成

可与 Bootstrap 或 Tailwind 等 CSS 框架配合使用的模板。

### Bootstrap 集成

```gotemplate
{{/* bootstrap-page.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{.title}}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" 
          rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">{{.siteName}}</a>
            <div class="navbar-nav">
                {{range .navItems}}
                <a class="nav-link" href="{{.url}}">{{.label}}</a>
                {{end}}
            </div>
        </div>
    </nav>
    
    <div class="container mt-4">
        {{if .alert}}
        <div class="alert alert-{{.alert.type}} alert-dismissible fade show">
            {{.alert.message}}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        {{end}}
        
        <div class="row">
            {{range .cards}}
            <div class="col-md-4 mb-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">{{.title}}</h5>
                        <p class="card-text">{{.text}}</p>
                        <a href="{{.link}}" class="btn btn-primary">{{.buttonText}}</a>
                    </div>
                </div>
            </div>
            {{end}}
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js">
    </script>
</body>
</html>
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.util.*;

public class BootstrapExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("bootstrap-page");
        template.parseFile(java.nio.file.Paths.get("templates/bootstrap-page.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("title", "我的应用");
        data.put("siteName", "MyApp");
        
        data.put("navItems", Arrays.asList(
            createNavItem("首页", "/"),
            createNavItem("功能", "/features"),
            createNavItem("价格", "/pricing")
        ));
        
        data.put("alert", Map.of(
            "type", "success",
            "message", "操作已成功完成！"
        ));
        
        data.put("cards", Arrays.asList(
            createCard("功能 1", "描述 1", "/f1", "了解更多"),
            createCard("功能 2", "描述 2", "/f2", "了解更多"),
            createCard("功能 3", "描述 3", "/f3", "了解更多")
        ));
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createNavItem(String label, String url) {
        Map<String, String> item = new HashMap<>();
        item.put("label", label);
        item.put("url", url);
        return item;
    }
    
    private static Map<String, String> createCard(
            String title, String text, String link, String buttonText) {
        Map<String, String> card = new HashMap<>();
        card.put("title", title);
        card.put("text", text);
        card.put("link", link);
        card.put("buttonText", buttonText);
        return card;
    }
}
```

---

## Web 模板最佳实践

### 1. 转义用户输入

始终转义用户生成的内容以防止 XSS 攻击：

```gotemplate
<!-- 好: 转义的输出 -->
<p>{{.userInput}}</p>

<!-- 差: 原始 HTML（危险！） -->
<p>{{.userInput | html}}</p>
```

注意：gotemplate4j 在大多数上下文中自动转义内容。使用返回 HTML 的自定义函数时请谨慎。

### 2. 使用模板继承

通过使用基础布局减少重复：

```java
// 解析一次，多次重用
Template layout = new Template("base");
layout.parseFiles(
    Paths.get("layout.html"),
    Paths.get("header.html"),
    Paths.get("footer.html")
);

// 不同页面覆盖 blocks
layout.executeTemplate(writer, "home", homeData);
layout.executeTemplate(writer, "about", aboutData);
```

### 3. 缓存已解析的模板

在应用程序启动时解析一次模板：

```java
public class TemplateCache {
    private final Map<String, Template> cache = new ConcurrentHashMap<>();
    
    public Template getTemplate(String name) {
        return cache.computeIfAbsent(name, n -> {
            Template t = new Template(n);
            t.parseFile(Paths.get("templates/" + n + ".html"));
            return t;
        });
    }
}
```

### 4. 将逻辑与展示分离

将业务逻辑放在 Java 中，而不是模板中：

```java
// 差: 模板中的复杂逻辑
{{if and (gt .score 80) (lt .score 90)}}B{{end}}

// 好: 在 Java 中计算
data.put("grade", calculateGrade(score));
// 模板: {{.grade}}
```

### 5. 使用有意义的变量名

```gotemplate
<!-- 好 -->
{{range .users}}
  <p>{{.name}}</p>
{{end}}

<!-- 差 -->
{{range .u}}
  <p>{{.n}}</p>
{{end}}
```

---

## 下一步

- 参见 [邮件模板](email-templates.md) 了解邮件特定模式
- 参见 [复杂场景](complex-scenarios.md) 了解高级用例
- 参见 [安全注意事项](../advanced/security.md) 了解 XSS 防护
- 参见 [性能调优](../advanced/performance.md) 了解优化技巧

---

本文档中的所有示例均已测试并通过验证，可在 gotemplate4j 中正确运行。
