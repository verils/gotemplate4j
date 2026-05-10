# Go 模板引擎 Java 版

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[English Documentation](./README.md)

一个用于 Java 的 Go 模板引擎实现，用于评估 Go 模板并生成文本输出。

> **项目目的**：本项目并非旨在替代 Go 模板，而是为了填补 Java 生态系统中对 Go 模板兼容性的需求空白。它使 Java 应用程序能够利用 Go 强大的模板语法，特别是在与基于 Go 的系统协作时。

> ✅ **生产就绪**：本项目已达到生产就绪状态，具有稳定的核心功能、强制 90% 以上指令覆盖率以及全面的内置函数支持。

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
    <version>0.6.0</version>
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

## 兼容性与迁移

v0.6.0 将详细兼容性说明移入专门文档：

- [Go Template Compatibility](./docs/go-template-compatibility.md)
- [Migration from Go text/template](./docs/migration-from-go-template.md)

简要说明：gotemplate4j 覆盖核心 Go `text/template` 控制流、pipeline、模板定义、内置函数和执行错误。JavaBean 访问、public field、`Optional`、enum、null、缺失 key 和 Map 迭代顺序等 Java 特定行为会与 Go 兼容性声明分开记录。

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

## 开发路线

请参阅 [PLAN.md](./PLAN.md) 获取详细的开发路线图。

## 参考资料

- [Go text/template 文档](https://pkg.go.dev/text/template)
- [Go template 源代码](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine（替代方案）](https://github.com/proninyaroslav/java-template-engine)
- [变更日志](./CHANGELOG)
- [开发计划](./PLAN.md)

## 贡献指南

欢迎贡献！请参阅 [CONTRIBUTING.md](./CONTRIBUTING.md) 获取详细指南。
