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
    <version>0.5.0</version>
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

## Go 兼容范围

v0.5.0 是核心 Go `text/template` 行为的兼容性审计版本。以下行为已有 focused tests 覆盖：

- 控制流：`if`、`else`、`else if`、`range`、`range ... else`、`with`、`else with`、`break` 和 `continue`
- 模板定义、覆盖顺序，以及省略 pipeline data 的 `template "name"` 执行
- pipeline 变量、`:=` 声明、`=` 赋值、带括号的 pipeline 参数，以及 branch/range 作用域
- 内置函数，包括比较、逻辑、集合、格式化、转义和 `call`
- 执行错误处理，包括函数失败和 writer `IOException` 传播

## Java 特定行为

部分行为是 Go 模板概念在 Java 类型系统上的有意映射：

- Java getter 和 public field 可通过字段链访问。
- public 无参方法可在字段链中访问，例如 enum 的 `name` 和 `ordinal`。
- Java `Optional` 会自动解包。
- null 和缺失值为 falsey，输出时表现为空字符串。
- Java `Map` 迭代遵循具体 Map 实现顺序；引擎不会像 Go 那样对基础类型 key 排序。
- `default`、`deepEqual`、`typeof` 和 `kindOf` 是 gotemplate4j 扩展，不是 Go 预定义函数。

## v0.5.0 暂不支持的 Go API

以下 Go API 或语义已明确延期：

- `Option("missingkey=default/zero/error")`
- `ParseFiles`、`ParseGlob` 和 `ParseFS`
- 模板 introspection API，例如 `Lookup`、`DefinedTemplates`、`Templates`、`Name` 和 associated `New`
- 对 channel、Go iterator 或 integer sequence 执行 `range`
- 在模板中调用带参数的通用 Java method
- 用 `call` 调用 Go 风格的 function-valued field 或 map entry；gotemplate4j 的 `call` 接受 `Function` 实例

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
