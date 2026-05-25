# 性能调优指南

本指南涵盖 gotemplate4j 的性能优化策略，包括基准测试、缓存以及高吞吐量场景的最佳实践。

---

## 目录

- [性能概述](#性能概述)
- [基准测试](#基准测试)
- [模板复用](#模板复用)
- [线程安全与克隆](#线程安全与克隆)
- [数据模型优化](#数据模型优化)
- [模板设计最佳实践](#模板设计最佳实践)
- [性能分析与监控](#性能分析与监控)
- [常见性能陷阱](#常见性能陷阱)

---

## 性能概述

gotemplate4j 是为简洁性和 Go template 兼容性而设计的，而非追求极致性能。然而，通过正确的使用模式，它可以高效地处理大多数应用程序工作负载。

### 关键性能特征

- **解析**：每个模板的一次性成本（可以缓存）
- **执行**：简单模板执行速度快，性能随数据复杂度而变化
- **反射**：用于字段访问（JVM 优化后开销最小）
- **内存**：占用低，适合长时间运行的应用程序

### 典型性能

在现代硬件上：
- 简单模板执行：约 10-100 微秒
- 带循环的复杂模板：约 100-1000 微秒
- 模板解析：约 1-10 毫秒（取决于大小）

**注意：** 实际性能取决于模板复杂度、数据大小和 JVM 预热情况。

---

## 基准测试

### 内置基准测试

gotemplate4j 在 `TemplateBenchmark.java` 中包含了一个基础基准测试：

```java
public class TemplateBenchmark {
    public static void main(String[] args) throws Exception {
        Template template = new Template("benchmark");
        template.parse("Hello, {{.Name}}! You have {{.Count}} messages.");

        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        data.put("Count", 42);

        int iterations = 100000;
        long start = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println(iterations + " executions in " + elapsed + "ms");
        System.out.println("Average: " + (elapsed / (double) iterations * 1000) + "μs per execution");
    }
}
```

### 运行基准测试

```bash
# 编译
./mvnw compile

# 运行基准测试
./mvnw exec:java -Dexec.mainClass="io.github.verils.gotemplate.TemplateBenchmark"
```

### JMH 推荐

对于生产级基准测试，建议迁移到 JMH（Java Microbenchmark Harness）：

```xml
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.35</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.35</version>
    <scope>test</scope>
</dependency>
```

JMH 基准测试示例：

```java
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class TemplateJMHTest {

    private Template template;
    private Map<String, Object> data;

    @Setup
    public void setup() throws TemplateParseException {
        template = new Template("benchmark");
        template.parse("Hello, {{.Name}}!");

        data = new HashMap<>();
        data.put("Name", "World");
    }

    @Benchmark
    public String executeTemplate(Blackhole blackhole) throws Exception {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        blackhole.consume(writer.toString());
        return writer.toString();
    }
}
```

### 性能基线（v0.8.0）

使用 JMH 在 JDK 1.8 上测量的参考性能数据：

| 基准 | 吞吐量 (ops/s) | 描述 |
|-----------|-------------------|-------------|
| **mapAccessBenchmark** | ~2,562,000 | 简单 Map 字段访问 |
| **executeBenchmark** | ~1,570,000 | 基本模板执行 |
| **beanAccessBenchmark** | ~1,514,000 | JavaBean 字段访问 |
| **functionHeavyBenchmark** | ~956,000 | 使用 upper/lower/len 函数 |
| **parseBenchmark** | ~296,000 | 模板解析（一次性成本） |
| **rangeHeavyBenchmark** | ~34,700 | 迭代 100 个项目 |

**测试环境：**
- **JMH 版本**：1.37
- **JDK**：1.8.0_482（OpenJDK 64-Bit Server VM）
- **CPU**：Intel i7-10870H 8C16T
- **内存**：64 GB RAM
- **操作系统**：Windows 25H2
- **预热**：3 次迭代 × 2s
- **测量**：5 次迭代 × 3s
- **线程**：1

**复现方式：**
```bash
./mvnw test-compile exec:java -Dexec.mainClass=io.github.verils.gotemplate.TemplateJmhBenchmark
```

**注意：** 这些数据作为未来性能比较的基线。实际性能可能因硬件和 JVM 配置而异。更多详情请参见 [TemplateJmhBenchmark.java](../../src/test/java/io/github/verils/gotemplate/TemplateJmhBenchmark.java)。

---

## 模板复用

### 解析一次，多次执行

最重要的性能优化是解析模板一次并复用它们：

```java
// 不好：每次请求都解析
public String render(String templateText, Map<String, Object> data) {
    Template template = new Template("render");
    template.parse(templateText); // 昂贵！
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    return writer.toString();
}

// 推荐：解析一次，缓存模板
private final Map<String, Template> templateCache = new ConcurrentHashMap<>();

public Template getOrParseTemplate(String name, String templateText) {
    return templateCache.computeIfAbsent(name, key -> {
        Template t = new Template(key);
        t.parse(templateText);
        return t;
    });
}

public String render(String templateName, String templateText, Map<String, Object> data) {
    Template template = getOrParseTemplate(templateName, templateText);
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    return writer.toString();
}
```

### 模板缓存策略

#### 简单内存缓存

```java
import java.util.concurrent.ConcurrentHashMap;

public class TemplateCache {
    private final ConcurrentHashMap<String, Template> cache = new ConcurrentHashMap<>();

    public Template get(String name) {
        return cache.get(name);
    }

    public void put(String name, Template template) {
        cache.put(name, template);
    }

    public Template computeIfAbsent(String name, Function<String, Template> parser) {
        return cache.computeIfAbsent(name, parser);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
```

#### 带过期时间的缓存

```java
import java.util.concurrent.*;

public class ExpiringTemplateCache {
    private final ConcurrentHashMap<String, CachedTemplate> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;

    private static class CachedTemplate {
        final Template template;
        final long createdAt;

        CachedTemplate(Template template) {
            this.template = template;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMillis) {
            return System.currentTimeMillis() - createdAt > ttlMillis;
        }
    }

    public ExpiringTemplateCache(long ttlSeconds) {
        this.ttlMillis = ttlSeconds * 1000L;
    }

    public Template get(String name) {
        CachedTemplate cached = cache.get(name);
        if (cached != null && !cached.isExpired(ttlMillis)) {
            return cached.template;
        }
        cache.remove(name); // 移除已过期条目
        return null;
    }

    public void put(String name, Template template) {
        cache.put(name, new CachedTemplate(template));
    }
}
```

#### 从文件加载

```java
public class FileBasedTemplateCache {
    private final Path templateDirectory;
    private final ConcurrentHashMap<String, Template> cache = new ConcurrentHashMap<>();

    public FileBasedTemplateCache(Path templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public Template loadTemplate(String name) throws IOException, TemplateParseException {
        return cache.computeIfAbsent(name, n -> {
            try {
                Path path = templateDirectory.resolve(n + ".tmpl");
                Template template = new Template(n);
                template.parseFile(path);
                return template;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load template: " + n, e);
            }
        });
    }

    public void invalidate(String name) {
        cache.remove(name);
    }

    public void reloadAll() throws IOException, TemplateParseException {
        cache.clear();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(templateDirectory, "*.tmpl")) {
            for (Path path : stream) {
                String name = path.getFileName().toString().replace(".tmpl", "");
                loadTemplate(name);
            }
        }
    }
}
```

---

## 线程安全与克隆

### 理解线程安全

**解析阶段：** 非线程安全
```java
// 不好：并发解析
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        template.parse("{{.Data}}"); // 竞态条件！
    });
}
```

**执行阶段：** 使用独立的 Writer 时是线程安全的
```java
// 推荐：并发执行
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    final int taskId = i;
    executor.submit(() -> {
        StringWriter writer = new StringWriter(); // 每个线程使用独立的 writer
        template.execute(writer, data); // 安全
    });
}
```

### 使用模板克隆以保证安全

当需要并发修改模板时：

```java
// 一次性创建基础模板
Template baseTemplate = new Template("master");
baseTemplate.parse("{{define \"header\"}}Header{{end}}{{.content}}");

// 为每次修改创建克隆
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    final int version = i;
    executor.submit(() -> {
        // 每个线程获得自己的副本
        Template threadTemplate = new Template(baseTemplate);

        // 可以安全修改而不影响其他线程
        threadTemplate.parse("{{define \"footer\"}}Footer v" + version + "{{end}}");

        // 安全执行
        StringWriter writer = new StringWriter();
        threadTemplate.execute(writer, createData(version));
    });
}
executor.shutdown();
```

### 克隆的性能影响

克隆操作是轻量级的：
- 复制对已解析 AST 节点的引用（而非深拷贝）
- 复制函数映射（浅拷贝）
- 创建新的配置状态

典型克隆开销：< 1 微秒

---

## 数据模型优化

### 使用扁平数据结构

深层嵌套的数据需要更多反射调用：

```java
// 不好：深层嵌套
Map<String, Object> data = new HashMap<>();
Map<String, Object> user = new HashMap<>();
Map<String, Object> profile = new HashMap<>();
profile.put("name", "John");
user.put("profile", profile);
data.put("user", user);

// 模板：{{.user.profile.name}} - 3 次反射调用
```

```java
// 推荐：扁平结构
Map<String, Object> data = new HashMap<>();
data.put("userName", "John");

// 模板：{{.userName}} - 1 次反射调用
```

### 优先使用 JavaBean 而非 Map

使用 getter 方法的 JavaBean 比 HashMap 查找更快：

```java
// 推荐：JavaBean
public class User {
    private String name;
    private int age;

    public String getName() { return name; }
    public int getAge() { return age; }
}

User user = new User();
user.setName("John");
user.setAge(30);

// 模板：{{.name}} - 直接方法调用（JIT 编译后更快）
```

```java
// 可以：HashMap
Map<String, Object> user = new HashMap<>();
user.put("name", "John");
user.put("age", 30);

// 模板：{{.name}} - Map 查找（稍慢）
```

### 最小化数据大小

仅传递模板实际需要的数据：

```java
// 不好：传递整个对象图
template.execute(writer, hugeObjectWithLotsOfFields);

// 推荐：仅提取需要的字段
Map<String, Object> data = new HashMap<>();
data.put("name", user.getName());
data.put("email", user.getEmail());
template.execute(writer, data);
```

### 避免在循环中使用大型集合

在模板中迭代大型集合很慢：

```gotemplate
{{range .Items}}
  {{.Name}}
{{end}}
```

如果 `.Items` 有 10,000 个元素，这会很慢。考虑：
- 分页结果
- 在 Java 代码中预处理
- 限制迭代次数

---

## 模板设计最佳实践

### 1. 最小化模板复杂度

保持模板简洁，将复杂逻辑移至 Java 代码：

```gotemplate
<!-- 不好：模板中包含复杂逻辑 -->
{{if and (gt .Score 80) (lt .Score 90)}}
  Grade: B
{{else if and (ge .Score 90) (lt .Score 100)}}
  Grade: A
{{end}}
```

```java
// 推荐：逻辑放在 Java 代码中
String grade = calculateGrade(score);
data.put("grade", grade);
```

```gotemplate
<!-- 模板变得简洁 -->
Grade: {{.grade}}
```

### 2. 避免深层嵌套

限制嵌套深度以提高可读性和性能：

```gotemplate
<!-- 不好：深层嵌套 -->
{{if .user}}
  {{if .user.profile}}
    {{if .user.profile.settings}}
      {{if .user.profile.settings.theme}}
        Theme: {{.user.profile.settings.theme}}
      {{end}}
    {{end}}
  {{end}}
{{end}}
```

```gotemplate
<!-- 推荐：扁平访问并带默认值 -->
Theme: {{defaultIfEmpty .theme "light"}}
```

### 3. 明智地使用模板定义

定义可复用的部分，但避免过度分割：

```gotemplate
{{define "header"}}
  <header>{{.title}}</header>
{{end}}

{{define "footer"}}
  <footer>{{.copyright}}</footer>
{{end}}

{{template "header" .}}
<main>{{.content}}</main>
{{template "footer" .}}
```

### 4. 最小化循环内的函数调用

在循环内的函数调用会累加：

```gotemplate
<!-- 不好：函数被调用 N 次 -->
{{range .Items}}
  {{uppercase .Name}}
{{end}}
```

```java
// 推荐：在 Java 中预处理
List<String> upperNames = items.stream()
    .map(item -> item.getName().toUpperCase())
    .collect(Collectors.toList());
data.put("upperNames", upperNames);
```

```gotemplate
{{range .upperNames}}
  {{.}}
{{end}}
```

### 5. 缓存昂贵的计算

如果必须使用函数，请缓存其结果：

```gotemplate
<!-- 不好：昂贵的计算被重复执行 -->
<div>{{computeExpensiveValue .id}}</div>
<div>{{computeExpensiveValue .id}}</div>
```

```java
// 推荐：计算一次，多次使用
Object value = computeExpensiveValue(id);
data.put("cachedValue", value);
```

```gotemplate
<div>{{.cachedValue}}</div>
<div>{{.cachedValue}}</div>
```

---

## 性能分析与监控

### 启用 JVM 性能分析

使用 JVM 标志监控性能：

```bash
java -Xprof -jar your-app.jar
```

### 使用 VisualVM

VisualVM 提供实时监控：

1. 启动你的应用程序
2. 启动 VisualVM（`jvisualvm`）
3. 连接到你的应用程序
4. 监控 CPU、内存和线程

### 分析模板执行

添加计时工具：

```java
public class TimedTemplate {
    private final Template template;
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalTimeNanos = new AtomicLong(0);

    public TimedTemplate(Template template) {
        this.template = template;
    }

    public void execute(Writer writer, Object data) throws Exception {
        long start = System.nanoTime();
        try {
            template.execute(writer, data);
        } finally {
            long elapsed = System.nanoTime() - start;
            totalExecutions.incrementAndGet();
            totalTimeNanos.addAndGet(elapsed);
        }
    }

    public double getAverageExecutionTimeMicros() {
        long executions = totalExecutions.get();
        if (executions == 0) return 0;
        return (totalTimeNanos.get() / (double) executions) / 1000.0;
    }

    public long getTotalExecutions() {
        return totalExecutions.get();
    }
}
```

### 记录慢执行

```java
public void executeWithLogging(Template template, Writer writer, Object data, String context)
        throws Exception {
    long start = System.currentTimeMillis();
    template.execute(writer, data);
    long elapsed = System.currentTimeMillis() - start;

    if (elapsed > 100) { // 如果慢于 100ms 则记录
        logger.warn("Slow template execution: {} took {}ms", context, elapsed);
    }
}
```

---

## 常见性能陷阱

### 陷阱 1：在热路径中解析

```java
// 不好：每次请求都解析
@RequestMapping("/page")
public String renderPage() {
    Template template = new Template("page");
    template.parse(loadTemplateText()); // 昂贵！
    // ...
}

// 推荐：在启动时解析一次
@PostConstruct
public void init() {
    template = new Template("page");
    template.parse(loadTemplateText());
}
```

### 陷阱 2：每次请求都创建模板

```java
// 不好：每次请求都创建新模板实例
public String render(Map<String, Object> data) {
    Template template = new Template("render", customFunctions);
    // ...
}

// 推荐：复用模板实例
private final Template template = new Template("render", customFunctions);

public String render(Map<String, Object> data) {
    // 仅执行，不重新创建
}
```

### 陷阱 3：在函数中过度记录日志

```java
// 不好：在热函数中记录日志
Function logFunc = args -> {
    logger.debug("Function called with: {}", args); // 慢！
    return process(args);
};

// 推荐：条件日志记录
Function logFunc = args -> {
    if (logger.isDebugEnabled()) {
        logger.debug("Function called with: {}", Arrays.toString(args));
    }
    return process(args);
};
```

### 陷阱 4：在循环中进行字符串拼接

```gotemplate
<!-- 不好：低效的字符串拼接 -->
{{range .Items}}
  {{$result := print $result .Name ", "}}
{{end}}
```

```java
// 推荐：在 Java 中拼接字符串
String result = items.stream()
    .map(Item::getName)
    .collect(Collectors.joining(", "));
data.put("result", result);
```

### 陷阱 5：忽略 MissingKeyPolicy

```java
// 不好：静默失败使调试困难
Template template = new Template("demo");
// 使用默认的 INVALID 策略 - 缺失键不产生任何输出

// 推荐：根据环境明确指定策略
Template template = new Template("demo")
    .withMissingKeyPolicy(isDev() ? MissingKeyPolicy.ERROR : MissingKeyPolicy.INVALID);
```

---

## 性能检查清单

使用此清单优化你的模板性能：

### 模板设计
- [ ] 模板简洁且专注
- [ ] 复杂逻辑放在 Java 代码中，而非模板中
- [ ] 嵌套深度最小化（< 3 层）
- [ ] 循环中无昂贵的计算
- [ ] 适当复用模板定义

### 缓存
- [ ] 模板只解析一次并被缓存
- [ ] 缓存实现是线程安全的
- [ ] 缓存过期策略适当
- [ ] 基于文件的模板加载高效

### 数据模型
- [ ] 数据结构尽可能扁平
- [ ] 仅向模板传递必要的数据
- [ ] 大型集合进行分页或限制
- [ ] 尽可能使用 JavaBean 而非 Map

### 线程安全
- [ ] 解析发生在热路径之外
- [ ] 执行时每个线程使用独立的 Writer
- [ ] 并发修改时使用模板克隆
- [ ] 共享状态中无竞态条件

### 监控
- [ ] 监控执行时间
- [ ] 记录慢执行
- [ ] 跟踪缓存命中率
- [ ] 内存使用合理

### 测试
- [ ] 关键路径存在性能测试
- [ ] 定期运行基准测试
- [ ] 自动化回归检测
- [ ] 负载测试验证可扩展性

---

## 未来优化

以下优化计划在未来的版本中实现：

### AST 缓存

缓存已解析的 AST 树以避免重复解析：

```java
// 未来 API（尚未实现）
Template template = Template.compile("{{.name}}"); // 预编译的 AST
```

### 反射缓存

缓存频繁访问的字段的反射查找：

```java
// 未来优化（内部）
// 自动为 getter 方法缓存 Method 对象
```

### 模板预编译

将模板编译为字节码以获得最大性能：

```java
// 未来功能（未计划在 v0.7.0 中实现）
CompiledTemplate compiled = template.compile();
compiled.execute(writer, data); // 更快的执行
```

---

## 总结

关键性能优化策略：

1. **解析一次，多次执行** - 最重要的优化
2. **缓存模板** - 使用 ConcurrentHashMap 或类似工具
3. **保持模板简洁** - 将逻辑移至 Java 代码
4. **优化数据模型** - 扁平结构、JavaBean
5. **正确使用线程安全** - 修改时使用克隆
6. **监控和分析** - 先测量再优化
7. **避免常见陷阱** - 不在热路径中解析

更多信息：
- 克隆和配置请参见[模板 API](../api-reference/template-api.md)
- 通用指南请参见[最佳实践](best-practices.md)
- 实际使用模式请参见[示例](../examples/)
