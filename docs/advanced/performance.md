# Performance Tuning Guide

This guide covers performance optimization strategies for gotemplate4j, including benchmarking, caching, and best practices for high-throughput scenarios.

---

## Table of Contents

- [Performance Overview](#performance-overview)
- [Benchmarking](#benchmarking)
- [Template Reuse](#template-reuse)
- [Thread Safety and Cloning](#thread-safety-and-cloning)
- [Data Model Optimization](#data-model-optimization)
- [Template Design Best Practices](#template-design-best-practices)
- [Profiling and Monitoring](#profiling-and-monitoring)
- [Common Performance Pitfalls](#common-performance-pitfalls)

---

## Performance Overview

gotemplate4j is designed for simplicity and Go template compatibility rather than maximum performance. However, with proper usage patterns, it can handle most application workloads efficiently.

### Key Performance Characteristics

- **Parsing**: One-time cost per template (can be cached)
- **Execution**: Fast for simple templates, scales with data complexity
- **Reflection**: Used for field access (minimal overhead with JVM optimizations)
- **Memory**: Low footprint, suitable for long-running applications

### Typical Performance

On modern hardware:
- Simple template execution: ~10-100 microseconds
- Complex template with loops: ~100-1000 microseconds
- Template parsing: ~1-10 milliseconds (depending on size)

**Note:** Actual performance depends on template complexity, data size, and JVM warmup.

---

## Benchmarking

### Built-in Benchmark

gotemplate4j includes a basic benchmark in `TemplateBenchmark.java`:

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

### Running Benchmarks

```bash
# Compile
./mvnw compile

# Run benchmark
./mvnw exec:java -Dexec.mainClass="io.github.verils.gotemplate.TemplateBenchmark"
```

### JMH Recommendation

For production-grade benchmarking, consider migrating to JMH (Java Microbenchmark Harness):

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

Example JMH benchmark:

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

---

## Template Reuse

### Parse Once, Execute Many Times

The most important performance optimization is to parse templates once and reuse them:

```java
// BAD: Parsing on every request
public String render(String templateText, Map<String, Object> data) {
    Template template = new Template("render");
    template.parse(templateText); // Expensive!
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    return writer.toString();
}

// GOOD: Parse once, cache the template
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

### Template Caching Strategies

#### Simple In-Memory Cache

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

#### Cache with Expiration

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
        cache.remove(name); // Remove expired entry
        return null;
    }
    
    public void put(String name, Template template) {
        cache.put(name, new CachedTemplate(template));
    }
}
```

#### Loading from Files

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

## Thread Safety and Cloning

### Understanding Thread Safety

**Parsing Phase:** NOT thread-safe
```java
// BAD: Concurrent parsing
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        template.parse("{{.Data}}"); // Race condition!
    });
}
```

**Execution Phase:** Thread-safe with separate Writers
```java
// GOOD: Concurrent execution
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    final int taskId = i;
    executor.submit(() -> {
        StringWriter writer = new StringWriter(); // Separate writer per thread
        template.execute(writer, data); // Safe
    });
}
```

### Using Template Cloning for Safety

When you need to modify templates concurrently:

```java
// Create base template once
Template baseTemplate = new Template("master");
baseTemplate.parse("{{define \"header\"}}Header{{end}}{{.content}}");

// Clone for each modification
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 10; i++) {
    final int version = i;
    executor.submit(() -> {
        // Each thread gets its own copy
        Template threadTemplate = new Template(baseTemplate);
        
        // Safe to modify without affecting other threads
        threadTemplate.parse("{{define \"footer\"}}Footer v" + version + "{{end}}");
        
        // Execute safely
        StringWriter writer = new StringWriter();
        threadTemplate.execute(writer, createData(version));
    });
}
executor.shutdown();
```

### Performance Impact of Cloning

Cloning is lightweight:
- Copies references to parsed AST nodes (no deep copy)
- Copies function maps (shallow copy)
- Creates new configuration state

Typical cloning overhead: < 1 microsecond

---

## Data Model Optimization

### Use Flat Data Structures

Deeply nested data requires more reflection calls:

```java
// BAD: Deep nesting
Map<String, Object> data = new HashMap<>();
Map<String, Object> user = new HashMap<>();
Map<String, Object> profile = new HashMap<>();
profile.put("name", "John");
user.put("profile", profile);
data.put("user", user);

// Template: {{.user.profile.name}} - 3 reflection calls
```

```java
// GOOD: Flatter structure
Map<String, Object> data = new HashMap<>();
data.put("userName", "John");

// Template: {{.userName}} - 1 reflection call
```

### Prefer JavaBeans Over Maps

JavaBeans with getter methods are faster than HashMap lookups:

```java
// GOOD: JavaBean
public class User {
    private String name;
    private int age;
    
    public String getName() { return name; }
    public int getAge() { return age; }
}

User user = new User();
user.setName("John");
user.setAge(30);

// Template: {{.name}} - Direct method call (faster after JIT compilation)
```

```java
// OK: HashMap
Map<String, Object> user = new HashMap<>();
user.put("name", "John");
user.put("age", 30);

// Template: {{.name}} - Map lookup (slightly slower)
```

### Minimize Data Size

Only pass data that the template actually needs:

```java
// BAD: Passing entire object graph
template.execute(writer, hugeObjectWithLotsOfFields);

// GOOD: Extract only needed fields
Map<String, Object> data = new HashMap<>();
data.put("name", user.getName());
data.put("email", user.getEmail());
template.execute(writer, data);
```

### Avoid Large Collections in Loops

Iterating over large collections in templates is slow:

```gotemplate
{{range .Items}}
  {{.Name}}
{{end}}
```

If `.Items` has 10,000 elements, this will be slow. Consider:
- Paginating results
- Pre-processing in Java code
- Limiting iteration count

---

## Template Design Best Practices

### 1. Minimize Template Complexity

Keep templates simple and move complex logic to Java code:

```gotemplate
<!-- BAD: Complex logic in template -->
{{if and (gt .Score 80) (lt .Score 90)}}
  Grade: B
{{else if and (ge .Score 90) (lt .Score 100)}}
  Grade: A
{{end}}
```

```java
// GOOD: Logic in Java code
String grade = calculateGrade(score);
data.put("grade", grade);
```

```gotemplate
<!-- Template becomes simple -->
Grade: {{.grade}}
```

### 2. Avoid Deep Nesting

Limit nesting depth to improve readability and performance:

```gotemplate
<!-- BAD: Deep nesting -->
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
<!-- GOOD: Flat access with defaults -->
Theme: {{defaultIfEmpty .theme "light"}}
```

### 3. Use Template Definitions Wisely

Define reusable sections but avoid excessive fragmentation:

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

### 4. Minimize Function Calls in Loops

Function calls inside loops add up:

```gotemplate
<!-- BAD: Function called N times -->
{{range .Items}}
  {{uppercase .Name}}
{{end}}
```

```java
// GOOD: Pre-process in Java
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

### 5. Cache Expensive Computations

If you must use functions, cache their results:

```gotemplate
<!-- BAD: Expensive computation repeated -->
<div>{{computeExpensiveValue .id}}</div>
<div>{{computeExpensiveValue .id}}</div>
```

```java
// GOOD: Compute once, use multiple times
Object value = computeExpensiveValue(id);
data.put("cachedValue", value);
```

```gotemplate
<div>{{.cachedValue}}</div>
<div>{{.cachedValue}}</div>
```

---

## Profiling and Monitoring

### Enable JVM Profiling

Use JVM flags to monitor performance:

```bash
java -Xprof -jar your-app.jar
```

### Use VisualVM

VisualVM provides real-time monitoring:

1. Start your application
2. Launch VisualVM (`jvisualvm`)
3. Connect to your application
4. Monitor CPU, memory, and threads

### Profile Template Execution

Add timing instrumentation:

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

### Log Slow Executions

```java
public void executeWithLogging(Template template, Writer writer, Object data, String context) 
        throws Exception {
    long start = System.currentTimeMillis();
    template.execute(writer, data);
    long elapsed = System.currentTimeMillis() - start;
    
    if (elapsed > 100) { // Log if slower than 100ms
        logger.warn("Slow template execution: {} took {}ms", context, elapsed);
    }
}
```

---

## Common Performance Pitfalls

### Pitfall 1: Parsing in Hot Paths

```java
// BAD: Parsing on every request
@RequestMapping("/page")
public String renderPage() {
    Template template = new Template("page");
    template.parse(loadTemplateText()); // Expensive!
    // ...
}

// GOOD: Parse once at startup
@PostConstruct
public void init() {
    template = new Template("page");
    template.parse(loadTemplateText());
}
```

### Pitfall 2: Creating Templates Per Request

```java
// BAD: New template instance per request
public String render(Map<String, Object> data) {
    Template template = new Template("render", customFunctions);
    // ...
}

// GOOD: Reuse template instance
private final Template template = new Template("render", customFunctions);

public String render(Map<String, Object> data) {
    // Just execute, don't recreate
}
```

### Pitfall 3: Excessive Logging in Functions

```java
// BAD: Logging in hot function
Function logFunc = args -> {
    logger.debug("Function called with: {}", args); // Slow!
    return process(args);
};

// GOOD: Conditional logging
Function logFunc = args -> {
    if (logger.isDebugEnabled()) {
        logger.debug("Function called with: {}", Arrays.toString(args));
    }
    return process(args);
};
```

### Pitfall 4: String Concatenation in Loops

```gotemplate
<!-- BAD: Inefficient string building -->
{{range .Items}}
  {{$result := print $result .Name ", "}}
{{end}}
```

```java
// GOOD: Build string in Java
String result = items.stream()
    .map(Item::getName)
    .collect(Collectors.joining(", "));
data.put("result", result);
```

### Pitfall 5: Ignoring MissingKeyPolicy

```java
// BAD: Silent failures make debugging hard
Template template = new Template("demo");
// Uses default INVALID policy - missing keys produce no output

// GOOD: Explicit policy based on environment
Template template = new Template("demo")
    .withMissingKeyPolicy(isDev() ? MissingKeyPolicy.ERROR : MissingKeyPolicy.INVALID);
```

---

## Performance Checklist

Use this checklist to optimize your template performance:

### Template Design
- [ ] Templates are simple and focused
- [ ] Complex logic is in Java code, not templates
- [ ] Nesting depth is minimal (< 3 levels)
- [ ] No expensive computations in loops
- [ ] Template definitions are reused appropriately

### Caching
- [ ] Templates are parsed once and cached
- [ ] Cache implementation is thread-safe
- [ ] Cache expiration strategy is appropriate
- [ ] File-based templates are loaded efficiently

### Data Models
- [ ] Data structures are flat where possible
- [ ] Only necessary data is passed to templates
- [ ] Large collections are paginated or limited
- [ ] JavaBeans are used instead of Maps when possible

### Thread Safety
- [ ] Parsing happens outside hot paths
- [ ] Execution uses separate Writers per thread
- [ ] Template cloning is used for concurrent modifications
- [ ] No race conditions in shared state

### Monitoring
- [ ] Execution times are monitored
- [ ] Slow executions are logged
- [ ] Cache hit rates are tracked
- [ ] Memory usage is reasonable

### Testing
- [ ] Performance tests exist for critical paths
- [ ] Benchmarks are run regularly
- [ ] Regression detection is automated
- [ ] Load testing validates scalability

---

## Future Optimizations

These optimizations are planned for future versions:

### AST Caching

Cache parsed AST trees to avoid reparsing:

```java
// Future API (not yet implemented)
Template template = Template.compile("{{.name}}"); // Pre-compiled AST
```

### Reflection Caching

Cache reflection lookups for frequently accessed fields:

```java
// Future optimization (internal)
// Automatically cache Method objects for getter methods
```

### Template Pre-compilation

Compile templates to bytecode for maximum performance:

```java
// Future feature (not planned for v0.7.0)
CompiledTemplate compiled = template.compile();
compiled.execute(writer, data); // Faster execution
```

---

## Summary

Key performance optimization strategies:

1. **Parse once, execute many times** - Most important optimization
2. **Cache templates** - Use ConcurrentHashMap or similar
3. **Keep templates simple** - Move logic to Java code
4. **Optimize data models** - Flat structures, JavaBeans
5. **Use thread safety correctly** - Clone when modifying
6. **Monitor and profile** - Measure before optimizing
7. **Avoid common pitfalls** - Don't parse in hot paths

For more information:
- See [Template API](../api-reference/template-api.md) for cloning and configuration
- See [Best Practices](best-practices.md) for general guidelines
- See [Examples](../examples/) for real-world usage patterns
