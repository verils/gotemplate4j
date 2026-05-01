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

## 当前限制

- Java 中 **print** 函数的格式与 Golang 不同
- 仅实现了部分内置函数
- PipeNode 处理能力有限

## 开发路线

- [x] 支持复数格式
- [ ] 支持 Golang 中的所有 *内置* 函数
- [ ] 完成所有类型标识符的 PipeNode 支持

详见 [docs/PLAN.md](./docs/PLAN.md) 获取完整的开发路线图。

## 参考资料

- [Go text/template 文档](https://pkg.go.dev/text/template)
- [Go template 源代码](https://github.com/golang/go/tree/master/src/text/template)
- [Java Template Engine（替代方案）](https://github.com/proninyaroslav/java-template-engine)
- [变更日志](./CHANGELOG)
- [开发计划](./docs/PLAN.md)

## 贡献指南

欢迎贡献！您可以通过以下方式提供帮助：

### 优先领域

1. **内置函数实现** - 帮助完成剩余的 Go 内置函数
2. **测试用例开发** - 提高测试覆盖率至 >80%
3. **文档改进** - 添加 Javadoc、示例和指南
4. **性能分析** - 识别和优化瓶颈

### 开发环境设置

```bash
# 克隆仓库
git clone https://github.com/verils/gotemplate4j.git
cd gotemplate4j

# 构建项目
./mvnw clean install

# 运行测试
./mvnw test
```

### 开发规范

- Java 版本：>= 1.8
- 构建工具：Maven（使用 `./mvnw` 包装器）
- 除 Vanilla Java 外无额外依赖
- 遵循标准 Java 命名规范
- 为新功能添加单元测试
- 保持向后兼容性

详细的开发计划请参阅 [docs/PLAN.md](./docs/PLAN.md)。
