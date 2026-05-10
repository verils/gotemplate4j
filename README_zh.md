# Go 模板引擎 Java 版

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[English Documentation](./README.md)

一个用于 Java 的 Go 模板引擎实现，用于评估 Go 模板并生成文本输出。

> ✅ **生产就绪**：核心功能稳定，代码覆盖率 90%+，内置函数支持完善，文档齐全。

> ⚠️ **核心定位**：本项目**不是**为了替代 Go 原生的 `text/template`，也**不追求**在性能或功能上超越它。它的存在仅仅是为了帮助**必须处理 Go 模板的 Java 开发者**在与 Go 系统协作或从 Go 迁移到 Java 时满足基本的工作需求。

## 快速开始

### 环境要求

- Java 版本：>= **1.8**
- 无需额外依赖（纯 Java 实现）

### 安装

将依赖项添加到您的 Maven 项目中：

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.7.0</version>
</dependency>
```

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

## 性能表现

gotemplate4j 针对 Java 8 环境进行了优化。以下基准数据是在标准开发机器上测得的（10,000 次迭代）：

| 测试项 | 吞吐量 (ops/sec) | 平均耗时 (ms/op) |
| :--- | :--- | :--- |
| **解析 (Parse)** | ~78,000 | ~0.012 |
| **执行 (Execute)** | ~262,000 | ~0.003 |
| **JavaBean 访问** | ~165,000 | ~0.006 |
| **Map 访问** | ~512,000 | ~0.001 |
| **循环 (100 项)** | ~31,000 | ~0.032 |
| **函数密集型** | ~474,000 | ~0.002 |

*注：您可以运行测试套件中的 `TemplateBenchmark` 类，在您自己的硬件上验证这些数据。*

### 测试环境
- **CPU**: Intel i7-10870H 8核16线程
- **内存**: 64 GB RAM
- **操作系统**: Windows 25H2
- **Java 版本**: 1.8

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
