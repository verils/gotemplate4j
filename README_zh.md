# Go 模板引擎 Java 版

[![Testing](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml/badge.svg)](https://github.com/verils/gotemplate4j/actions/workflows/maven.yml)

[English Documentation](./README.md)

一个用于 Java 的 Go 模板引擎实现，用于评估 Go 模板并生成文本输出。

> ✅ **生产就绪**：本项目已达到生产就绪状态，具有稳定的核心功能、80%以上的测试覆盖率以及全面的内置函数支持。

## 快速开始

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

## 环境要求

- Java 版本：>= **1.8**
- 无需额外依赖（纯 Java 实现）

## 贡献指南

欢迎贡献！请参阅 [CONTRIBUTING.md](./CONTRIBUTING.md) 获取详细指南。

### 贡献者快速开始

```bash
# 克隆仓库
git clone https://github.com/verils/gotemplate4j.git
cd gotemplate4j

# 构建和测试
./mvnw clean install
```

有关开发环境设置、编码指南和优先领域的更多信息，请参阅 [CONTRIBUTING.md](./CONTRIBUTING.md)。

## 当前状态

### ✅ 已完成功能 (v0.4.0)
- 实现了所有 18+ 个 Go 内置函数（`eq`, `ne`, `lt`, `le`, `gt`, `ge`, `and`, `or`, `len`, `index`, `slice`, `call`, `html`, `js`, `urlquery`, `deepEqual`, `typeof`, `kindOf`）
- 完整的管道支持，包含变量赋值
- 增强的错误诊断，带有行/列信息和上下文片段
- 测试覆盖率：82% 指令覆盖率，79% 分支覆盖率（JaCoCo 测量，超过 70% 分支覆盖率目标）
- 复数解析和格式化
- 公共 API 类的全面 Javadoc 文档
- 面向开发者的 CONTRIBUTING.md 指南

### 🚧 进行中
- 性能优化（缓存策略）
- 全面的 Javadoc 文档
- 高级模板功能（自定义分隔符、增强的空白控制）

## 参考资料

- [Go text/template 文档](https://pkg.go.dev/text/template)
- [Go template 源代码](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine（替代方案）](https://github.com/proninyaroslav/java-template-engine)
- [变更日志](./CHANGELOG)
- [开发计划](./docs/PLAN.md)

## 开发路线

请参阅 [docs/PLAN.md](./docs/PLAN.md) 获取详细的开发路线图。
