# 模板集与继承

本指南解释如何使用多个模板、模板继承和可复用组件。

## 概述

gotemplate4j 支持：
- **命名模板**：定义可复用的模板块
- **模板调用**：从其他模板中调用模板
- **Block**：定义并内联执行，支持覆盖能力
- **模板继承**：从基础模板构建复杂布局

## 定义模板

使用 `define` 创建命名模板。

### 基本定义

```gotemplate
{{define "header"}}
<html>
<head>
  <title>{{.Title}}</title>
</head>
<body>
{{end}}
```

定义的模板在被调用之前不会产生输出。

### 多个定义

单个模板可以包含多个定义：

```gotemplate
{{define "header"}}
<header>
  <h1>{{.SiteName}}</h1>
  <nav>
    {{range .MenuItems}}
      <a href="{{.URL}}">{{.Label}}</a>
    {{end}}
  </nav>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>&copy; {{.Year}} {{.CompanyName}}</p>
</footer>
{{end}}

{{define "sidebar"}}
<aside>
  <h3>分类</h3>
  <ul>
  {{range .Categories}}
    <li><a href="{{.URL}}">{{.Name}}</a></li>
  {{end}}
  </ul>
</aside>
{{end}}
```

## 调用模板

使用 `template` 动作调用已定义的模板。

### 基本调用

```gotemplate
{{template "header" .}}
<main>内容在这里</main>
{{template "footer" .}}
```

第二个参数将数据上下文传递给被调用的模板。

### 无上下文调用

```gotemplate
{{template "header"}}
```

使用当前上下文（点号）。

### 使用不同上下文调用

```gotemplate
{{template "user-card" .User}}
{{template "product-list" .Products}}
```

向模板传递特定数据。

### 完整示例

```gotemplate
{{define "page"}}
<!DOCTYPE html>
<html>
<head>
  <title>{{.Title}}</title>
</head>
<body>
  {{template "header" .}}
  
  <main>
    {{template "content" .}}
  </main>
  
  {{template "footer" .}}
</body>
</html>
{{end}}

{{define "header"}}
<header>
  <h1>{{.SiteName}}</h1>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>版权所有 {{.Year}}</p>
</footer>
{{end}}

{{define "content"}}
<p>主要内容在这里</p>
{{end}}

<!-- 执行页面模板 -->
{{template "page" .}}
```

## Block 动作

`block` 动作定义并内联执行模板。它与 Go 兼容，并允许后续覆盖。

### 基本 Block

```gotemplate
{{block "content" .}}默认内容{{end}}
```

等价于：

```gotemplate
{{define "content"}}默认内容{{end}}
{{template "content" .}}
```

### Block 覆盖

后续定义覆盖先前的定义：

```gotemplate
{{/* 基础模板 */}}
{{block "title" .}}默认标题{{end}}

{{/* 稍后在模板集中 */}}
{{define "title"}}自定义标题{{end}}

<!-- 执行时输出: 自定义标题 -->
```

### 布局中的 Block

```gotemplate
{{/* layout.tmpl */}}
<html>
<head>
  <title>{{block "title" .}}我的网站{{end}}</title>
</head>
<body>
  {{block "content" .}}默认内容{{end}}
</body>
</html>

{{/* page.tmpl */}}
{{define "title"}}首页{{end}}

{{define "content"}}
<h1>欢迎!</h1>
<p>这是首页。</p>
{{end}}
```

当你解析两个模板并执行 "content" 时，它会使用被覆盖的 "title"。

## 模板继承模式

使用类似继承的模式构建复杂页面。

### 基础布局

```gotemplate
{{define "base"}}
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>{{block "title" .}}默认标题{{end}}</title>
  {{block "styles" .}}
  <link rel="stylesheet" href="/css/main.css">
  {{end}}
</head>
<body>
  {{block "header" .}}
  <header>
    <h1>{{.SiteName}}</h1>
  </header>
  {{end}}
  
  <main>
    {{block "content" .}}默认内容{{end}}
  </main>
  
  {{block "footer" .}}
  <footer>
    <p>&copy; {{.Year}}</p>
  </footer>
  {{end}}
  
  {{block "scripts" .}}
  <script src="/js/main.js"></script>
  {{end}}
</body>
</html>
{{end}}
```

### 首页

```gotemplate
{{define "title"}}首页 - 我的网站{{end}}

{{define "content"}}
<h2>欢迎来到我们的网站</h2>
<p>这是首页内容。</p>
{{end}}
```

### 关于页面

```gotemplate
{{define "title"}}关于我们 - 我的网站{{end}}

{{define "content"}}
<h2>关于我们</h2>
<p>了解更多关于我们公司的信息。</p>
{{end}}

{{define "styles"}}
<link rel="stylesheet" href="/css/main.css">
<link rel="stylesheet" href="/css/about.css">
{{end}}
```

### 渲染页面

```java
// 解析所有模板
Template template = new Template("base");
template.parseFile(Paths.get("templates/base.tmpl"));
template.parseFile(Paths.get("templates/home.tmpl"));

// 执行
Map<String, Object> data = new HashMap<>();
data.put("SiteName", "我的网站");
data.put("Year", 2026);

StringWriter writer = new StringWriter();
template.execute(writer, data);
```

## 解析多个模板

将多个模板文件加载到单个模板集中。

### 从文件

```java
Template template = new Template("base");
template.parseFile(Paths.get("templates/layout.tmpl"));
template.parseFile(Paths.get("templates/home.tmpl"));
template.parseFile(Paths.get("templates/about.tmpl"));
```

### 使用 parseFiles

```java
Template template = new Template("base");
template.parseFiles(
    Paths.get("templates/layout.tmpl"),
    Paths.get("templates/home.tmpl"),
    Paths.get("templates/about.tmpl")
);
```

### 使用 parseGlob

解析所有匹配的文件：

```java
Template template = new Template("base");
template.parseGlob("templates/*.tmpl");
```

或使用模式：

```java
template.parseGlob("templates/pages/*.tmpl");
```

## 模板查找

检查模板集中是否存在某个模板。

### hasTemplate

```java
if (template.hasTemplate("header")) {
    System.out.println("Header 模板存在");
}
```

### lookup

按名称获取模板：

```java
Template headerTemplate = template.lookup("header");
if (headerTemplate != null) {
    // 使用该模板
}
```

### definedTemplates

列出所有已定义的模板：

```java
Set<String> names = template.definedTemplates();
for (String name : names) {
    System.out.println("模板: " + name);
}
```

### name

获取主模板名称：

```java
String primaryName = template.name();
```

## 执行特定模板

从模板集中执行特定模板。

### 按名称

```java
// 专门执行 "home" 模板
template.executeTemplate("home", writer, data);
```

### 默认行为

如果不指定模板而调用 `execute()`，则执行第一个解析的模板。

## 嵌套模板执行

模板可以递归地调用其他模板。

### 示例

```gotemplate
{{define "page"}}
{{template "header" .}}
{{template "body" .}}
{{template "footer" .}}
{{end}}

{{define "body"}}
<article>
  {{template "article-content" .}}
  {{template "comments" .Comments}}
</article>
{{end}}

{{define "comments"}}
{{if .}}
  <h3>评论</h3>
  {{range .}}
    {{template "comment" .}}
  {{end}}
{{end}}
{{end}}

{{define "comment"}}
<div class="comment">
  <strong>{{.Author}}</strong>
  <p>{{.Text}}</p>
</div>
{{end}}
```

## 上下文管理

控制不同模板中可用的数据。

### 传递完整上下文

```gotemplate
{{template "partial" .}}
```

局部模板接收所有数据。

### 传递子集

```gotemplate
{{template "user-info" .User}}
```

局部模板只接收用户数据。

### 创建新上下文

```gotemplate
{{with .User}}
  {{template "profile" .}}
{{end}}
```

Profile 模板将 User 视为根上下文。

## 最佳实践

### 1. 逻辑组织模板

将相关模板分组：

```
templates/
├── layout/
│   ├── base.tmpl
│   ├── header.tmpl
│   └── footer.tmpl
├── pages/
│   ├── home.tmpl
│   ├── about.tmpl
│   └── contact.tmpl
└── partials/
    ├── user-card.tmpl
    ├── product-item.tmpl
    └── navigation.tmpl
```

### 2. 使用描述性名称

✅ **好：**
```gotemplate
{{define "user-profile-card"}}...{{end}}
{{define "navigation-menu"}}...{{end}}
```

❌ **不好：**
```gotemplate
{{define "tpl1"}}...{{end}}
{{define "partial"}}...{{end}}
```

### 3. 保持模板专注

每个模板应该做好一件事：

```gotemplate
{{define "user-name"}}
  {{.FirstName}} {{.LastName}}
{{end}}

{{define "user-email"}}
  <a href="mailto:{{.Email}}">{{.Email}}</a>
{{end}}
```

### 4. 文档化模板依赖

```java
/**
 * 模板: home-page
 * 需要: base layout, navigation, footer
 * 期望数据: SiteName, Year, MenuItems, FeaturedProducts
 */
```

### 5. 对可覆盖段落使用 Block

```gotemplate
{{define "base"}}
<html>
<head>
  <title>{{block "title" .}}默认{{end}}</title>
  {{block "head-extra" .}}{{end}}  <!-- 默认为空 -->
</head>
<body>
  {{block "content" .}}默认{{end}}
</body>
</html>
{{end}}
```

页面可以覆盖 `title` 和 `head-extra`。

## 常见模式

### 模式 1：页面布局

```gotemplate
{{define "page"}}
<!DOCTYPE html>
<html>
<head>
  <title>{{.Title}}</title>
</head>
<body>
  {{template "navbar" .}}
  
  <div class="container">
    {{template "content" .}}
  </div>
  
  {{template "footer" .}}
</body>
</html>
{{end}}
```

### 模式 2：组件库

```gotemplate
{{define "button"}}
<button class="btn btn-{{.Style | default "default"}}">
  {{.Label}}
</button>
{{end}}

{{define "alert"}}
<div class="alert alert-{{.Type | default "info"}}">
  {{.Message}}
</div>
{{end}}

{{define "card"}}
<div class="card">
  {{if .Title}}
    <div class="card-header">{{.Title}}</div>
  {{end}}
  <div class="card-body">
    {{.Content}}
  </div>
</div>
{{end}}
```

用法：

```gotemplate
{{template "button" (dict "Label" "点击我" "Style" "primary")}}
{{template "alert" (dict "Type" "success" "Message" "已保存!")}}
```

注意：你可能需要一个 `dict` 函数来在模板中创建 map。

### 模式 3：条件段落

```gotemplate
{{define "sidebar"}}
{{if .ShowSidebar}}
  <aside>
    {{block "sidebar-content" .}}
      默认侧边栏内容
    {{end}}
  </aside>
{{end}}
{{end}}
```

### 模式 4：重复元素

```gotemplate
{{define "list-item"}}
<li class="item {{if .IsActive}}active{{end}}">
  {{.Name}}
</li>
{{end}}

<ul>
{{range .Items}}
  {{template "list-item" .}}
{{end}}
</ul>
```

## 高级主题

### 模板克隆

克隆模板集以进行线程安全执行：

```java
Template clone = template.clone();
clone.execute(writer, data);
```

### 缺失模板

优雅地处理缺失的模板调用：

```gotemplate
{{if hasTemplate "optional-partial"}}
  {{template "optional-partial" .}}
{{end}}
```

注意：`hasTemplate` 可能需要作为自定义函数实现。

### 递归模板

模板可以调用自身（谨慎使用）：

```gotemplate
{{define "tree-node"}}
<li>
  {{.Name}}
  {{if .Children}}
    <ul>
    {{range .Children}}
      {{template "tree-node" .}}
    {{end}}
    </ul>
  {{end}}
</li>
{{end}}

<ul>
{{range .TreeRoots}}
  {{template "tree-node" .}}
{{end}}
</ul>
```

## 问题排查

### 问题：模板未找到

**错误：** `template "xyz" not defined`

**解决方案：**
1. 验证模板在使用前已定义
2. 检查模板名称的拼写
3. 确保所有文件都已解析

```java
System.out.println(template.definedTemplates());
```

### 问题：模板中数据错误

**问题：** 模板显示意外的数据。

**解决方案：** 检查传递了什么上下文：

```gotemplate
{{template "partial" .}}    <!-- 传递完整上下文 -->
{{template "partial" .User}} <!-- 仅传递 User -->
```

### 问题：循环引用

**问题：** 无限循环或栈溢出。

**解决方案：** 避免模板在没有基本情况的条件下相互递归调用。

### 问题：Block 未被覆盖

**问题：** Block 显示默认内容而非覆盖内容。

**解决方案：** 
1. 确保覆盖在执行前定义
2. 检查两个模板是否在同一模板集中
3. 验证模板名称完全匹配

## 完整示例：博客模板

```gotemplate
{{/* base.tmpl */}}
{{define "base"}}
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>{{block "title" .}}我的博客{{end}}</title>
  {{block "styles" .}}
  <link rel="stylesheet" href="/css/blog.css">
  {{end}}
</head>
<body>
  {{template "header" .}}
  
  <div class="container">
    <main>
      {{block "content" .}}默认内容{{end}}
    </main>
    
    <aside>
      {{template "sidebar" .}}
    </aside>
  </div>
  
  {{template "footer" .}}
  
  {{block "scripts" .}}
  <script src="/js/blog.js"></script>
  {{end}}
</body>
</html>
{{end}}

{{define "header"}}
<header>
  <h1><a href="/">{{.BlogName}}</a></h1>
  <nav>
    <a href="/">首页</a>
    <a href="/about">关于</a>
    <a href="/contact">联系</a>
  </nav>
</header>
{{end}}

{{define "footer"}}
<footer>
  <p>&copy; {{.Year}} {{.BlogName}}</p>
</footer>
{{end}}

{{define "sidebar"}}
<div class="widget">
  <h3>最近文章</h3>
  <ul>
  {{range .RecentPosts}}
    <li><a href="{{.URL}}">{{.Title}}</a></li>
  {{end}}
  </ul>
</div>

<div class="widget">
  <h3>分类</h3>
  <ul>
  {{range .Categories}}
    <li><a href="{{.URL}}">{{.Name}}</a></li>
  {{end}}
  </ul>
</div>
{{end}}

{{/* index.tmpl */}}
{{define "title"}}首页 - {{.BlogName}}{{end}}

{{define "content"}}
<h2>最新文章</h2>

{{range .Posts}}
<article class="post-preview">
  <h3><a href="{{.URL}}">{{.Title}}</a></h3>
  <p class="meta">作者 {{.Author}} 于 {{.Date}}</p>
  <p>{{.Excerpt}}</p>
  <a href="{{.URL}}" class="read-more">阅读更多 →</a>
</article>
{{end}}

{{if .HasMorePosts}}
  <a href="/page/2" class="older-posts">更早的文章</a>
{{end}}
{{end}}

{{/* post.tmpl */}}
{{define "title"}}{{.Post.Title}} - {{.BlogName}}{{end}}

{{define "styles"}}
<link rel="stylesheet" href="/css/blog.css">
<link rel="stylesheet" href="/css/post.css">
{{end}}

{{define "content"}}
<article class="full-post">
  <h1>{{.Post.Title}}</h1>
  <p class="meta">作者 {{.Post.Author}} 于 {{.Post.Date}}</p>
  
  <div class="content">
    {{.Post.Content}}
  </div>
  
  {{if .Post.Tags}}
    <div class="tags">
      标签:
      {{range .Post.Tags}}
        <span class="tag">{{.}}</span>
      {{end}}
    </div>
  {{end}}
</article>

<section class="comments">
  <h3>评论</h3>
  {{if .Post.Comments}}
    {{range .Post.Comments}}
      {{template "comment" .}}
    {{end}}
  {{else}}
    <p>暂无评论。</p>
  {{end}}
</section>
{{end}}

{{define "comment"}}
<div class="comment">
  <strong>{{.Author}}</strong>
  <span class="date">{{.Date}}</span>
  <p>{{.Text}}</p>
</div>
{{end}}
```

---

**下一步：**
- 🐛 了解[错误处理](error-handling.md)以构建稳健的模板
- 🔧 探索[函数](functions.md)以创建如 `dict` 的辅助函数
- 📚 查阅[模板语法](template-syntax.md)获取完整参考
