# Go 模板引擎 Java 版

[![Test and Verify](https://github.com/verils/gotemplate4j/actions/workflows/test.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/test.yml)

[English Documentation](./README.md)

一个用于 Java 的 Go 模板引擎实现，用于评估 Go 模板并生成文本输出。

> ✅ **生产就绪**：核心功能稳定，代码覆盖率 90%+，内置函数支持完善，文档齐全。

> ⚠️ **核心定位**：本项目**不是**为了替代 Go 原生的 `text/template`，也**不追求**在性能或功能上超越它。它的存在仅仅是为了帮助**必须处理 Go 模板的 Java 开发者**在与 Go 系统协作或从 Go 迁移到 Java 时满足基本的工作需求。

> 🆕 **最新版本 (v0.9.1)**：关键模板引擎修复、扩展的语法覆盖，以及改进的测试组织。

## 快速开始

### 环境要求

- Java 版本：>= **11**
- 无需额外依赖（纯 Java 实现）

> ⚠️ **Java 8 支持**：v0.9.x 是最后一个支持 Java 8 的发布线。v0.10.0 开发线需要 **Java 11 或更高版本**。详情请参阅 [开发计划](./PLAN.md)。

### 安装

#### Maven

将依赖项添加到您的 `pom.xml`：

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.9.1</version>
</dependency>
```

#### Gradle

将依赖项添加到您的 `build.gradle`：

```groovy
dependencies {
    implementation 'io.github.verils:gotemplate4j:0.9.1'
}
```

对于 Gradle Kotlin DSL (`build.gradle.kts`)：

```kotlin
dependencies {
    implementation("io.github.verils:gotemplate4j:0.9.1")
}
```

更多安装选项，请参阅 [安装指南](./docs/getting-started/installation.md)。

### 基本用法

```java
// 创建用户作为输入数据
User user = new User();
user.setName("Bob");

// 准备您的模板
Template template = new Template("demo");
template.parse("Hello, {{ .Name }}!");

// 执行并打印结果文本
StringWriter writer = new StringWriter();
template.execute(writer, user);
System.out.print(writer.toString());  // "Hello Bob!"
```

### 增强的文件加载 (v0.9.0+)

从 classpath、目录或指定编码加载模板：

```java
// 从 classpath 加载
Template tmpl = Template.parseFromClasspath("templates/email.tmpl");

// 从目录加载（所有 .tmpl 文件）
Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));

// 使用指定编码加载
Template tmpl = Template.parseFile(Paths.get("template.tmpl"), StandardCharsets.UTF_8);

// 批量从 classpath 加载（支持模式匹配）
List<Template> templates = Template.parseClasspathResources("templates/*.tmpl");
```

更多示例，请参阅 [基础示例](./docs/examples/basic-examples.md)。

## 文档

完整的文档可在 `docs/` 目录中找到：

### 🚀 入门指南
- [安装指南](./docs/getting-started/installation.md) - 将 gotemplate4j 添加到您的项目
- [快速入门教程](./docs/getting-started/quick-start.md) - 5分钟内创建第一个模板
- [基本概念](./docs/getting-started/basic-concepts.md) - 理解核心概念

### 📖 用户指南
- [模板语法参考](./docs/user-guide/template-syntax.md) - 完整语法指南
- [使用 Java 数据](./docs/user-guide/data-models.md) - JavaBeans、Maps、Lists、Enums
- [内置和自定义函数](./docs/user-guide/functions.md) - 函数参考
- [控制流](./docs/user-guide/control-flow.md) - If、range、with、break/continue
- [模板集和继承](./docs/user-guide/template-sets.md) - Define、template、blocks
- [错误处理](./docs/user-guide/error-handling.md) - 优雅地处理错误

### 🔧 高级主题
- [Go 模板兼容性](./docs/advanced/compatibility.md) - 详细的兼容性指南
- [从 Go 模板迁移](./docs/advanced/migration.md) - 逐步迁移指南
- [性能优化](./docs/advanced/performance.md) - 优化模板执行
- [安全最佳实践](./docs/advanced/security.md) - 安全注意事项
- [设计模式](./docs/advanced/best-practices.md) - 最佳实践和模式

### 📚 API 参考
- [Template API](./docs/api-reference/template-api.md) - Template 类参考
- [Function API](./docs/api-reference/function-api.md) - Function 接口指南
- [Exception API](./docs/api-reference/exception-api.md) - 异常层次结构

### 💡 示例
- [基础示例](./docs/examples/basic-examples.md) - 简单用例
- [Web 模板](./docs/examples/web-templates.md) - HTML 生成模式
- [邮件模板](./docs/examples/email-templates.md) - 邮件生成示例
- [复杂场景](./docs/examples/complex-scenarios.md) - 高级实际场景

### ❓ FAQ
- [常见问题解答](./docs/faq.md) - 常见问题和答案

**从这里开始：** [文档中心](./docs/index.md)

## 参考资料

- [Go text/template 文档](https://pkg.go.dev/text/template)
- [Go template 源代码](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine（替代方案）](https://github.com/proninyaroslav/java-template-engine)
- [变更日志](./CHANGELOG)
- [开发计划](./PLAN.md)

## 开发路线

请参阅 [PLAN.md](./PLAN.md) 获取详细的开发路线图。

## 贡献指南

欢迎贡献！请参阅 [CONTRIBUTING.md](./CONTRIBUTING.md) 获取详细指南。
