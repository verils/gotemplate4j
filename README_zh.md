# Go 模板引擎 Java 版

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

评估 Go 模板并生成文本输出。

在此之前，还有一个名称不太直观的优秀库：[Java Template Engine](https://github.com/proninyaroslav/java-template-engine)，如果您喜欢可以选择它。

本项目目前处于实验阶段，**请勿在生产环境中使用**，因为对复数和内置函数的支持尚不完整。希望能尽快完成这些功能。

您可以查看 [变更日志](./CHANGELOG) 了解最新动态。

## 要求

Java 版本：>= **1.8**

除了 *Vanilla Java* 外，无需其他依赖

## 安装

对于 Maven，您只需添加以下依赖：

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.3.1</version>
</dependency>
```

## 使用方法

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

## 限制

- Java 中 **print** 函数的格式与 Golang 不同
- 仅实现了少数内置函数
- PipeNode 的处理能力较弱

## 下一步计划？

- [x] 支持复数格式
- [ ] 支持 Golang 中的所有 *内置* 函数
- [ ] 完善 PipeNode 以支持所有类型的标识符