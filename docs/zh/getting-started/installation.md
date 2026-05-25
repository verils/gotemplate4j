# 安装

本指南将展示如何将 gotemplate4j 添加到你的 Java 项目中。

## 要求

- **Java 版本**：Java 11 或更高版本
- **构建工具**：Maven、Gradle 或手动引入 JAR
- **依赖**：无（纯 Java 实现）

## Maven

将以下依赖添加到你的 `pom.xml` 中：

```xml
<dependency>
    <groupId>io.github.verils</groupId>
    <artifactId>gotemplate4j</artifactId>
    <version>0.10.0</version>
</dependency>
```

## Gradle

将以下内容添加到你的 `build.gradle` 中：

```groovy
dependencies {
    implementation 'io.github.verils:gotemplate4j:0.10.0'
}
```

Gradle Kotlin DSL（`build.gradle.kts`）：

```kotlin
dependencies {
    implementation("io.github.verils:gotemplate4j:0.10.0")
}
```

## 手动安装

1. 从 [Maven Central](https://central.sonatype.com/artifact/io.github.verils/gotemplate4j) 下载 JAR 文件
2. 将 JAR 添加到项目的 classpath 中

### 下载链接

- **JAR**：`gotemplate4j-0.10.0.jar`
- **源码**：`gotemplate4j-0.10.0-sources.jar`
- **Javadoc**：`gotemplate4j-0.10.0-javadoc.jar`

## 验证安装

创建一个简单的测试来验证 gotemplate4j 是否已正确安装：

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;

public class InstallationTest {
    public static void main(String[] args) throws Exception {
        // 创建模板
        Template template = new Template("test");
        template.parse("Hello, {{.Name}}!");
        
        // 准备数据
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("Name", "World");
        
        // 执行模板
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        // 打印结果
        System.out.println(writer.toString());
        // 输出：Hello, World!
    }
}
```

如果这段代码能够成功编译和运行，说明 gotemplate4j 已正确安装！

## 下一步

- 📖 阅读[快速开始指南](quick-start.md)学习基础知识
- 💡 探索[基本概念](basic-concepts.md)理解核心功能
- 🔍 查看[示例](../examples/basic-examples.md)了解更多用例

## 故障排除

### 编译错误

如果遇到编译错误：

1. 确保已安装 Java 11+：`java -version`
2. 验证依赖项已正确添加到构建文件中
3. 执行干净构建：`mvn clean compile` 或 `gradle clean build`

### 运行时错误

如果遇到 `ClassNotFoundException` 或 `NoClassDefFoundError`：

1. 验证 JAR 是否在 classpath 中
2. 检查是否存在依赖冲突
3. 确保使用的 Java 版本兼容

### 需要帮助？

- 📚 浏览[文档索引](index.md)
- ❓ 查看[常见问题](../faq.md)
- 🐛 在 [GitHub](https://github.com/verils/gotemplate4j/issues) 上报告问题
