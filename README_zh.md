# Go 模板引擎 Java 版

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[English Documentation](./README.md)

一个用于 Java 的 Go 模板引擎实现，用于评估 Go 模板并生成文本输出。

> **项目目的**：本项目并非旨在替代 Go 模板，而是为了填补 Java 生态系统中对 Go 模板兼容性的需求空白。它使 Java 应用程序能够利用 Go 强大的模板语法，特别是在与基于 Go 的系统协作时。

> ✅ **生产就绪**：本项目已达到生产就绪状态，具有稳定的核心功能、80%以上的测试覆盖率以及全面的内置函数支持。

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
    <version>0.4.0</version>
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

## 贡献指南

欢迎贡献！请参阅 [CONTRIBUTING.md](./CONTRIBUTING.md) 获取详细指南。

## 开发路线

请参阅 [PLAN.md](./PLAN.md) 获取详细的开发路线图。

## 参考资料

- [Go text/template 文档](https://pkg.go.dev/text/template)
- [Go template 源代码](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine（替代方案）](https://github.com/proninyaroslav/java-template-engine)
- [变更日志](./CHANGELOG)
- [开发计划](./PLAN.md)
