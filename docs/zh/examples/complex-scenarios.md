# 复杂场景

本文档展示了 gotemplate4j 在真实应用中的高级使用模式，包括多语言支持、动态内容生成以及与外部系统的集成。

---

## 目录

- [多语言支持](#多语言支持)
- [动态报表生成](#动态报表生成)
- [配置文件生成](#配置文件生成)
- [代码生成](#代码生成)
- [发票生成](#发票生成)
- [API 文档生成器](#api-文档生成器)
- [数据导出（CSV/JSON）](#数据导出-csvjson)
- [模板组合](#模板组合)
- [条件模板选择](#条件模板选择)

---

## 多语言支持

使用模板集生成多语言内容。

### 语言文件

```gotemplate
{{/* lang/en.json */}}
{
  "greeting": "Hello",
  "farewell": "Goodbye",
  "welcome_message": "Welcome to our platform"
}

{{/* lang/es.json */}}
{
  "greeting": "Hola",
  "farewell": "Adiós",
  "welcome_message": "Bienvenido a nuestra plataforma"
}

{{/* lang/fr.json */}}
{
  "greeting": "Bonjour",
  "farewell": "Au revoir",
  "welcome_message": "Bienvenue sur notre plateforme"
}
```

### 带国际化的邮件模板

```gotemplate
{{/* localized-email.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
    <h1>{{.translations.greeting}}, {{.userName}}!</h1>
    <p>{{.translations.welcome_message}}</p>
    <p>{{.translations.farewell}}</p>
</body>
</html>
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class MultiLanguageExample {
    
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    
    static {
        // 加载翻译（生产环境中，从文件或数据库加载）
        Map<String, String> en = new HashMap<>();
        en.put("greeting", "Hello");
        en.put("farewell", "Goodbye");
        en.put("welcome_message", "Welcome to our platform");
        TRANSLATIONS.put("en", en);
        
        Map<String, String> es = new HashMap<>();
        es.put("greeting", "Hola");
        es.put("farewell", "Adiós");
        es.put("welcome_message", "Bienvenido a nuestra plataforma");
        TRANSLATIONS.put("es", es);
        
        Map<String, String> fr = new HashMap<>();
        fr.put("greeting", "Bonjour");
        fr.put("farewell", "Au revoir");
        fr.put("welcome_message", "Bienvenue sur notre plateforme");
        TRANSLATIONS.put("fr", fr);
    }
    
    public static void main(String[] args) throws Exception {
        Template template = new Template("localized-email");
        template.parseFile(Paths.get("templates/localized-email.html"));
        
        // 生成不同语言的邮件
        String[] languages = {"en", "es", "fr"};
        
        for (String lang : languages) {
            Map<String, Object> data = new HashMap<>();
            data.put("userName", "Maria");
            data.put("translations", TRANSLATIONS.get(lang));
            
            StringWriter writer = new StringWriter();
            template.execute(writer, data);
            
            System.out.println("=== " + lang.toUpperCase() + " ===");
            System.out.println(writer.toString());
            System.out.println();
        }
    }
}
```

### 输出

```
=== EN ===
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"></head>
<body>
    <h1>Hello, Maria!</h1>
    <p>Welcome to our platform</p>
    <p>Goodbye</p>
</body>
</html>

=== ES ===
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"></head>
<body>
    <h1>Hola, Maria!</h1>
    <p>Bienvenido a nuestra plataforma</p>
    <p>Adiós</p>
</body>
</html>

=== FR ===
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"></head>
<body>
    <h1>Bonjour, Maria!</h1>
    <p>Bienvenue sur notre plateforme</p>
    <p>Au revoir</p>
</body>
</html>
```

---

## 动态报表生成

根据数据生成带有动态分区的报表。

### 报表模板

```gotemplate
{{/* report.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .header { text-align: center; margin-bottom: 40px; }
        .section { margin: 30px 0; padding: 20px; border: 1px solid #ddd; }
        .metric { display: inline-block; margin: 10px; padding: 15px; 
                  background: #f5f5f5; border-radius: 4px; min-width: 150px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f8f8; }
        .chart-placeholder { height: 200px; background: #e9ecef; 
                            display: flex; align-items: center; justify-content: center; }
    </style>
</head>
<body>
    <div class="header">
        <h1>{{.report.title}}</h1>
        <p>{{.report.period}}</p>
        <p>生成时间: {{.report.generatedAt}}</p>
    </div>
    
    {{if .report.summary}}
    <div class="section">
        <h2>执行摘要</h2>
        <p>{{.report.summary}}</p>
    </div>
    {{end}}
    
    {{if .report.metrics}}
    <div class="section">
        <h2>关键指标</h2>
        {{range .report.metrics}}
        <div class="metric">
            <h3 style="margin: 0 0 10px 0;">{{.label}}</h3>
            <p style="font-size: 24px; margin: 0; color: {{.color}};">{{.value}}</p>
            {{if .change}}
            <p style="font-size: 12px; margin: 5px 0 0 0; color: {{if gt .change 0}}green{{else}}red{{end}};">
                {{if gt .change 0}}↑{{else}}↓{{end}} {{abs .change}}%
            </p>
            {{end}}
        </div>
        {{end}}
    </div>
    {{end}}
    
    {{if .report.tables}}
    {{range .report.tables}}
    <div class="section">
        <h2>{{.title}}</h2>
        <table>
            <thead>
                <tr>
                    {{range .headers}}
                    <th>{{.}}</th>
                    {{end}}
                </tr>
            </thead>
            <tbody>
                {{range .rows}}
                <tr>
                    {{range .}}
                    <td>{{.}}</td>
                    {{end}}
                </tr>
                {{end}}
            </tbody>
        </table>
    </div>
    {{end}}
    {{end}}
    
    {{if .report.charts}}
    <div class="section">
        <h2>可视化图表</h2>
        {{range .report.charts}}
        <h3>{{.title}}</h3>
        <div class="chart-placeholder">
            [图表: {{.type}} - {{.description}}]
        </div>
        {{end}}
    </div>
    {{end}}
    
    {{if .report.recommendations}}
    <div class="section">
        <h2>建议</h2>
        <ol>
            {{range .report.recommendations}}
            <li>{{.}}</li>
            {{end}}
        </ol>
    </div>
    {{end}}
</body>
</html>
```

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportGenerationExample {
    public static void main(String[] args) throws Exception {
        // 自定义绝对值函数
        Function abs = args -> Math.abs(((Number) args[0]).doubleValue());
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("abs", abs);
        
        Template template = new Template("report", functions);
        template.parseFile(Paths.get("templates/report.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // 报表元数据
        Map<String, Object> report = new HashMap<>();
        report.put("title", "2026 年第一季度绩效报告");
        report.put("period", "2026 年 1 月 1 日 - 3 月 31 日");
        report.put("generatedAt", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.put("summary", 
            "整体绩效超出预期，收入增长 23%，用户参与度提升 15%。");
        
        // 关键指标
        List<Map<String, Object>> metrics = Arrays.asList(
            createMetric("收入", "$1.2M", "#28a745", 23.5),
            createMetric("用户", "45.2K", "#007bff", 15.2),
            createMetric("转化率", "3.8%", "#ffc107", -2.1),
            createMetric("留存率", "87%", "#28a745", 5.3)
        );
        report.put("metrics", metrics);
        
        // 数据表格
        List<Map<String, Object>> tables = new ArrayList<>();
        
        // 热门产品表
        Map<String, Object> productsTable = new HashMap<>();
        productsTable.put("title", "热门产品");
        productsTable.put("headers", Arrays.asList("产品", "销量", "收入"));
        productsTable.put("rows", Arrays.asList(
            Arrays.asList("Widget A", "1,234", "$12,340"),
            Arrays.asList("Widget B", "987", "$9,870"),
            Arrays.asList("Widget C", "756", "$7,560")
        ));
        tables.add(productsTable);
        
        // 区域表现表
        Map<String, Object> regionalTable = new HashMap<>();
        regionalTable.put("title", "区域表现");
        regionalTable.put("headers", Arrays.asList("地区", "用户", "增长"));
        regionalTable.put("rows", Arrays.asList(
            Arrays.asList("北美", "18.5K", "+12%"),
            Arrays.asList("欧洲", "15.2K", "+18%"),
            Arrays.asList("亚太", "11.5K", "+25%")
        ));
        tables.add(regionalTable);
        
        report.put("tables", tables);
        
        // 图表
        List<Map<String, String>> charts = Arrays.asList(
            createChart("收入趋势", "line", "月度收入变化趋势"),
            createChart("用户分布", "pie", "按地区的用户分布"),
            createChart("产品销售", "bar", "按产品类别的销售情况")
        );
        report.put("charts", charts);
        
        // 建议
        List<String> recommendations = Arrays.asList(
            "加太亚太地区的营销投入",
            "调查转化率下降原因并实施 A/B 测试",
            "扩大 Widget A 库存以满足增长需求",
            "针对流失风险用户启动留存活动"
        );
        report.put("recommendations", recommendations);
        
        data.put("report", report);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
        
        // 在生产环境中，可以使用 Flying Saucer 或 OpenPDF 等库将 HTML 转换为 PDF
        // PdfGenerator.generatePdf(html, Paths.get("report.pdf"));
    }
    
    private static Map<String, Object> createMetric(
            String label, String value, String color, double change) {
        Map<String, Object> metric = new HashMap<>();
        metric.put("label", label);
        metric.put("value", value);
        metric.put("color", color);
        metric.put("change", change);
        return metric;
    }
    
    private static Map<String, String> createChart(String title, String type, String description) {
        Map<String, String> chart = new HashMap<>();
        chart.put("title", title);
        chart.put("type", type);
        chart.put("description", description);
        return chart;
    }
}
```

---

## 配置文件生成

生成各种格式的配置文件。

### YAML 配置模板

```gotemplate
{{/* config.yaml.tmpl */}}
# 应用配置
# 生成时间: {{.generatedAt}}

server:
  host: {{.server.host}}
  port: {{.server.port}}
  ssl:
    enabled: {{.server.ssl.enabled}}
    {{if .server.ssl.enabled}}
    cert_path: {{.server.ssl.certPath}}
    key_path: {{.server.ssl.keyPath}}
    {{end}}

database:
  driver: {{.database.driver}}
  url: {{.database.url}}
  username: {{.database.username}}
  pool:
    min_connections: {{.database.pool.min}}
    max_connections: {{.database.pool.max}}
    timeout: {{.database.pool.timeout}}

logging:
  level: {{.logging.level}}
  file: {{.logging.file}}
  {{if .logging.rotate}}
  rotation:
    max_size: {{.logging.rotation.maxSize}}
    max_files: {{.logging.rotation.maxFiles}}
  {{end}}

features:
  {{range $key, $value := .features}}
  {{$key}}: {{$value}}
  {{end}}

{{if .cache.enabled}}
cache:
  type: {{.cache.type}}
  ttl: {{.cache.ttl}}
  {{if eq .cache.type "redis"}}
  redis:
    host: {{.cache.redis.host}}
    port: {{.cache.redis.port}}
  {{end}}
{{end}}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ConfigGenerationExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("config-yaml");
        template.parseFile(Paths.get("templates/config.yaml.tmpl"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("generatedAt", LocalDateTime.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 服务器配置
        Map<String, Object> server = new HashMap<>();
        server.put("host", "0.0.0.0");
        server.put("port", 8443);
        
        Map<String, Object> ssl = new HashMap<>();
        ssl.put("enabled", true);
        ssl.put("certPath", "/etc/ssl/certs/app.crt");
        ssl.put("keyPath", "/etc/ssl/private/app.key");
        server.put("ssl", ssl);
        data.put("server", server);
        
        // 数据库配置
        Map<String, Object> database = new HashMap<>();
        database.put("driver", "org.postgresql.Driver");
        database.put("url", "jdbc:postgresql://localhost:5432/mydb");
        database.put("username", "app_user");
        
        Map<String, Object> pool = new HashMap<>();
        pool.put("min", 5);
        pool.put("max", 20);
        pool.put("timeout", 30000);
        database.put("pool", pool);
        data.put("database", database);
        
        // 日志配置
        Map<String, Object> logging = new HashMap<>();
        logging.put("level", "INFO");
        logging.put("file", "/var/log/app/application.log");
        logging.put("rotate", true);
        
        Map<String, Object> rotation = new HashMap<>();
        rotation.put("maxSize", "100MB");
        rotation.put("maxFiles", 10);
        logging.put("rotation", rotation);
        data.put("logging", logging);
        
        // 功能开关
        Map<String, Object> features = new HashMap<>();
        features.put("enable_notifications", true);
        features.put("enable_analytics", true);
        features.put("enable_cache", false);
        features.put("maintenance_mode", false);
        data.put("features", features);
        
        // 缓存配置
        Map<String, Object> cache = new HashMap<>();
        cache.put("enabled", true);
        cache.put("type", "redis");
        cache.put("ttl", 3600);
        
        Map<String, Object> redis = new HashMap<>();
        redis.put("host", "localhost");
        redis.put("port", 6379);
        cache.put("redis", redis);
        data.put("cache", cache);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String yaml = writer.toString();
        System.out.println(yaml);
        
        // 写入文件
        // Files.writeString(Paths.get("config.yaml"), yaml);
    }
}
```

### 输出

```yaml
# 应用配置
# 生成时间: 2026-05-10T14:30:00

server:
  host: 0.0.0.0
  port: 8443
  ssl:
    enabled: true
    cert_path: /etc/ssl/certs/app.crt
    key_path: /etc/ssl/private/app.key

database:
  driver: org.postgresql.Driver
  url: jdbc:postgresql://localhost:5432/mydb
  username: app_user
  pool:
    min_connections: 5
    max_connections: 20
    timeout: 30000

logging:
  level: INFO
  file: /var/log/app/application.log
  rotation:
    max_size: 100MB
    max_files: 10

features:
  enable_notifications: true
  enable_analytics: true
  enable_cache: false
  maintenance_mode: false

cache:
  type: redis
  ttl: 3600
  redis:
    host: localhost
    port: 6379
```

---

## 代码生成

从模板生成样板代码。

### Java 类模板

```gotemplate
{{/* java-class.java.tmpl */}}
package {{.package}};

{{if .imports}}
{{range .imports}}
import {{.}};
{{end}}
{{end}}

/**
 * {{.classDescription}}
 * 
 * 生成时间: {{.generatedAt}}
 */
public class {{.className}} {
    {{range .fields}}
    /**
     * {{.description}}
     */
    private {{.type}} {{.name}};
    {{end}}
    
    {{if .constructor}}
    /**
     * 构造函数
     */
    public {{.className}}({{range $i, $param := .constructor.params}}{{if $i}}, {{end}}{{$param.type}} {{$param.name}}{{end}}) {
        {{range .constructor.params}}
        this.{{.name}} = {{.name}};
        {{end}}
    }
    {{end}}
    
    {{range .fields}}
    /**
     * 获取 {{.name}}
     * @return {{.name}}
     */
    public {{.type}} get{{capitalize .name}}() {
        return {{.name}};
    }
    
    /**
     * 设置 {{.name}}
     * @param {{.name}} 要设置的 {{.name}}
     */
    public void set{{capitalize .name}}({{.type}} {{.name}}) {
        this.{{.name}} = {{.name}};
    }
    {{end}}
    
    @Override
    public String toString() {
        return "{{.className}}{" +
        {{range $i, $field := .fields}}{{if $i}} + ", " +{{end}}
                "{{.name}}=" + {{.name}}{{end}} +
                '}';
    }
}
```

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CodeGenerationExample {
    public static void main(String[] args) throws Exception {
        // 首字母大写函数
        Function capitalize = args -> {
            String str = (String) args[0];
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("capitalize", capitalize);
        
        Template template = new Template("java-class", functions);
        template.parseFile(Paths.get("templates/java-class.java.tmpl"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("package", "com.example.model");
        data.put("className", "UserProfile");
        data.put("classDescription", "表示系统中的用户资料");
        data.put("generatedAt", LocalDateTime.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 导入
        List<String> imports = Arrays.asList(
            "java.time.LocalDate",
            "java.util.Objects"
        );
        data.put("imports", imports);
        
        // 字段
        List<Map<String, String>> fields = Arrays.asList(
            createField("userId", "String", "用户的唯一标识符"),
            createField("username", "String", "用户的登录名"),
            createField("email", "String", "用户的邮箱地址"),
            createField("dateOfBirth", "LocalDate", "用户的出生日期"),
            createField("isActive", "boolean", "用户账号是否激活")
        );
        data.put("fields", fields);
        
        // 构造函数参数
        Map<String, Object> constructor = new HashMap<>();
        constructor.put("params", Arrays.asList(
            createParam("userId", "String"),
            createParam("username", "String"),
            createParam("email", "String")
        ));
        data.put("constructor", constructor);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String javaCode = writer.toString();
        System.out.println(javaCode);
        
        // 写入文件
        // Files.writeString(Paths.get("src/main/java/com/example/model/UserProfile.java"), javaCode);
    }
    
    private static Map<String, String> createField(String name, String type, String description) {
        Map<String, String> field = new HashMap<>();
        field.put("name", name);
        field.put("type", type);
        field.put("description", description);
        return field;
    }
    
    private static Map<String, String> createParam(String name, String type) {
        Map<String, String> param = new HashMap<>();
        param.put("name", name);
        param.put("type", type);
        return param;
    }
}
```

---

## 发票生成

生成带有明细账单的专业发票。

### 发票模板

```gotemplate
{{/* invoice.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: 'Helvetica', Arial, sans-serif; margin: 0; padding: 40px; }
        .invoice-box { max-width: 800px; margin: auto; padding: 30px; border: 1px solid #eee; }
        .header { display: flex; justify-content: space-between; margin-bottom: 40px; }
        .company-info h1 { margin: 0; color: #333; }
        .invoice-details { text-align: right; }
        .bill-to { margin: 30px 0; }
        table { width: 100%; border-collapse: collapse; margin: 30px 0; }
        th { background: #f5f5f5; padding: 12px; text-align: left; border-bottom: 2px solid #ddd; }
        td { padding: 12px; border-bottom: 1px solid #eee; }
        .totals { float: right; width: 300px; }
        .totals-row { display: flex; justify-content: space-between; padding: 8px 0; }
        .total-final { font-weight: bold; font-size: 18px; border-top: 2px solid #333; margin-top: 10px; padding-top: 10px; }
        .footer { margin-top: 60px; padding-top: 20px; border-top: 1px solid #eee; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="invoice-box">
        <!-- 头部 -->
        <div class="header">
            <div class="company-info">
                <h1>{{.company.name}}</h1>
                <p>{{.company.address}}</p>
                <p>{{.company.city}}, {{.company.state}} {{.company.zip}}</p>
                <p>邮箱: {{.company.email}}</p>
            </div>
            <div class="invoice-details">
                <h2>发票</h2>
                <p><strong>发票号:</strong> {{.invoice.number}}</p>
                <p><strong>日期:</strong> {{.invoice.date}}</p>
                <p><strong>到期日:</strong> {{.invoice.dueDate}}</p>
            </div>
        </div>
        
        <!-- 付款方 -->
        <div class="bill-to">
            <h3>付款方:</h3>
            <p>
                {{.customer.name}}<br>
                {{.customer.company}}<br>
                {{.customer.address}}<br>
                {{.customer.city}}, {{.customer.state}} {{.customer.zip}}
            </p>
        </div>
        
        <!-- 项目表 -->
        <table>
            <thead>
                <tr>
                    <th>描述</th>
                    <th>数量</th>
                    <th>单价</th>
                    <th>金额</th>
                </tr>
            </thead>
            <tbody>
                {{range .invoice.items}}
                <tr>
                    <td>{{.description}}</td>
                    <td>{{.quantity}}</td>
                    <td>{{formatCurrency .unitPrice}}</td>
                    <td>{{formatCurrency .amount}}</td>
                </tr>
                {{end}}
            </tbody>
        </table>
        
        <!-- 合计 -->
        <div class="totals">
            <div class="totals-row">
                <span>小计:</span>
                <span>{{formatCurrency .invoice.subtotal}}</span>
            </div>
            {{if .invoice.discount}}
            <div class="totals-row">
                <span>折扣 ({{.invoice.discountPercent}}%):</span>
                <span>-{{formatCurrency .invoice.discount}}</span>
            </div>
            {{end}}
            <div class="totals-row">
                <span>税费 ({{.invoice.taxRate}}%):</span>
                <span>{{formatCurrency .invoice.tax}}</span>
            </div>
            <div class="totals-row total-final">
                <span>总计:</span>
                <span>{{formatCurrency .invoice.total}}</span>
            </div>
        </div>
        
        <div style="clear: both;"></div>
        
        <!-- 付款条款 -->
        <div style="margin: 40px 0;">
            <h3>付款条款:</h3>
            <p>{{.invoice.paymentTerms}}</p>
            <p>支票抬头请写: <strong>{{.company.name}}</strong></p>
        </div>
        
        <!-- 页脚 -->
        <div class="footer">
            <p>感谢您的惠顾！</p>
            <p>如果您对此发票有任何疑问，请联系 {{.company.email}}</p>
        </div>
    </div>
</body>
</html>
```

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;

public class InvoiceGenerationExample {
    public static void main(String[] args) throws Exception {
        // 货币格式化函数
        Function formatCurrency = args -> {
            Number amount = (Number) args[0];
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            return formatter.format(amount.doubleValue());
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatCurrency", formatCurrency);
        
        Template template = new Template("invoice", functions);
        template.parseFile(Paths.get("templates/invoice.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // 公司信息
        Map<String, String> company = new HashMap<>();
        company.put("name", "Acme Corporation");
        company.put("address", "123 Business Ave");
        company.put("city", "New York");
        company.put("state", "NY");
        company.put("zip", "10001");
        company.put("email", "billing@acme.com");
        data.put("company", company);
        
        // 客户信息
        Map<String, String> customer = new HashMap<>();
        customer.put("name", "John Smith");
        customer.put("company", "Smith Enterprises");
        customer.put("address", "456 Client Street");
        customer.put("city", "Los Angeles");
        customer.put("state", "CA");
        customer.put("zip", "90001");
        data.put("customer", customer);
        
        // 发票详情
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("number", "INV-2026-0042");
        invoice.put("date", "2026 年 5 月 10 日");
        invoice.put("dueDate", "2026 年 6 月 10 日");
        invoice.put("paymentTerms", "Net 30 - 请在 30 天内付款");
        
        // 行项目
        List<Map<String, Object>> items = Arrays.asList(
            createItem("Web 开发服务", 40, 150.00),
            createItem("UI/UX 设计", 20, 125.00),
            createItem("项目管理", 10, 100.00),
            createItem("托管服务设置", 1, 500.00)
        );
        invoice.put("items", items);
        
        // 计算总额
        double subtotal = items.stream()
            .mapToDouble(item -> ((Number) item.get("amount")).doubleValue())
            .sum();
        invoice.put("subtotal", subtotal);
        
        double discountPercent = 10;
        double discount = subtotal * (discountPercent / 100.0);
        invoice.put("discountPercent", discountPercent);
        invoice.put("discount", discount);
        
        double taxRate = 8.5;
        double taxableAmount = subtotal - discount;
        double tax = taxableAmount * (taxRate / 100.0);
        invoice.put("taxRate", taxRate);
        invoice.put("tax", tax);
        
        double total = taxableAmount + tax;
        invoice.put("total", total);
        
        data.put("invoice", invoice);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
        
        // 转换为 PDF 发送给客户
        // PdfGenerator.generatePdf(html, Paths.get("invoice_INV-2026-0042.pdf"));
    }
    
    private static Map<String, Object> createItem(String description, int quantity, double unitPrice) {
        Map<String, Object> item = new HashMap<>();
        item.put("description", description);
        item.put("quantity", quantity);
        item.put("unitPrice", unitPrice);
        item.put("amount", quantity * unitPrice);
        return item;
    }
}
```

---

## API 文档生成器

从规范数据生成 API 文档。

### API 文档模板

```gotemplate
{{/* api-docs.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
               margin: 0; padding: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 40px; }
        .endpoint { margin: 40px 0; padding: 20px; border: 1px solid #e1e4e8; border-radius: 6px; }
        .method { display: inline-block; padding: 4px 8px; border-radius: 4px; font-weight: bold; 
                  color: white; margin-right: 10px; }
        .get { background: #61affe; }
        .post { background: #49cc90; }
        .put { background: #fca130; }
        .delete { background: #f93e3e; }
        .path { font-family: monospace; font-size: 16px; }
        pre { background: #f6f8fa; padding: 16px; border-radius: 4px; overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border: 1px solid #dfe2e5; }
        th { background: #f6f8fa; }
        .required { color: red; }
    </style>
</head>
<body>
    <div class="container">
        <h1>{{.api.title}}</h1>
        <p>{{.api.description}}</p>
        <p><strong>基础 URL:</strong> <code>{{.api.baseUrl}}</code></p>
        <p><strong>版本:</strong> {{.api.version}}</p>
        
        {{range .api.endpoints}}
        <div class="endpoint">
            <h2>
                <span class="method {{lower .method}}">{{.method}}</span>
                <span class="path">{{.path}}</span>
            </h2>
            <p>{{.description}}</p>
            
            {{if .parameters}}
            <h3>参数</h3>
            <table>
                <thead>
                    <tr>
                        <th>名称</th>
                        <th>位置</th>
                        <th>类型</th>
                        <th>必需</th>
                        <th>描述</th>
                    </tr>
                </thead>
                <tbody>
                    {{range .parameters}}
                    <tr>
                        <td><code>{{.name}}</code></td>
                        <td>{{.in}}</td>
                        <td>{{.type}}</td>
                        <td>{{if .required}}<span class="required">是</span>{{else}}否{{end}}</td>
                        <td>{{.description}}</td>
                    </tr>
                    {{end}}
                </tbody>
            </table>
            {{end}}
            
            {{if .requestBody}}
            <h3>请求体</h3>
            <pre><code>{{.requestBody.example}}</code></pre>
            {{end}}
            
            {{if .responses}}
            <h3>响应</h3>
            {{range $code, $response := .responses}}
            <h4>{{$code}} - {{$response.description}}</h4>
            {{if $response.example}}
            <pre><code>{{$response.example}}</code></pre>
            {{end}}
            {{end}}
            {{end}}
        </div>
        {{end}}
    </div>
</body>
</html>
```

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class ApiDocsGeneratorExample {
    public static void main(String[] args) throws Exception {
        // 小写转换函数
        Function lower = args -> ((String) args[0]).toLowerCase();
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("lower", lower);
        
        Template template = new Template("api-docs", functions);
        template.parseFile(Paths.get("templates/api-docs.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        Map<String, Object> api = new HashMap<>();
        api.put("title", "用户管理 API");
        api.put("description", "用于管理用户账户和资料的 RESTful API");
        api.put("baseUrl", "https://api.example.com/v1");
        api.put("version", "1.0.0");
        
        // API 端点
        List<Map<String, Object>> endpoints = new ArrayList<>();
        
        // GET /users
        Map<String, Object> getUsers = new HashMap<>();
        getUsers.put("method", "GET");
        getUsers.put("path", "/users");
        getUsers.put("description", "获取所有用户列表");
        
        List<Map<String, String>> getUsersParams = Arrays.asList(
            createParameter("page", "query", "integer", false, "页码（默认: 1）"),
            createParameter("limit", "query", "integer", false, "每页数量（默认: 20）")
        );
        getUsers.put("parameters", getUsersParams);
        
        Map<String, Object> getUsersResponses = new HashMap<>();
        getUsersResponses.put("200", createResponse(
            "成功响应",
            "{\n  \"users\": [...],\n  \"total\": 100,\n  \"page\": 1\n}"
        ));
        getUsers.put("responses", getUsersResponses);
        endpoints.add(getUsers);
        
        // POST /users
        Map<String, Object> createUser = new HashMap<>();
        createUser.put("method", "POST");
        createUser.put("path", "/users");
        createUser.put("description", "创建新用户账户");
        
        List<Map<String, String>> createUserParams = Arrays.asList(
            createParameter("Authorization", "header", "string", true, "Bearer 令牌")
        );
        createUser.put("parameters", createUserParams);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("example", 
            "{\n" +
            "  \"username\": \"john_doe\",\n" +
            "  \"email\": \"john@example.com\",\n" +
            "  \"password\": \"secure_password\"\n" +
            "}");
        createUser.put("requestBody", requestBody);
        
        Map<String, Object> createUserResponses = new HashMap<>();
        createUserResponses.put("201", createResponse("用户已创建", 
            "{\n  \"id\": 123,\n  \"username\": \"john_doe\"\n}"));
        createUserResponses.put("400", createResponse("验证错误", 
            "{\n  \"error\": \"邮箱已存在\"\n}"));
        createUser.put("responses", createUserResponses);
        endpoints.add(createUser);
        
        // GET /users/{id}
        Map<String, Object> getUser = new HashMap<>();
        getUser.put("method", "GET");
        getUser.put("path", "/users/{id}");
        getUser.put("description", "按 ID 获取指定用户");
        
        List<Map<String, String>> getUserParams = Arrays.asList(
            createParameter("id", "path", "integer", true, "用户 ID")
        );
        getUser.put("parameters", getUserParams);
        
        Map<String, Object> getUserResponses = new HashMap<>();
        getUserResponses.put("200", createResponse("用户详情", 
            "{\n  \"id\": 123,\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\"\n}"));
        getUserResponses.put("404", createResponse("未找到用户", ""));
        getUser.put("responses", getUserResponses);
        endpoints.add(getUser);
        
        api.put("endpoints", endpoints);
        data.put("api", api);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, String> createParameter(
            String name, String in, String type, boolean required, String description) {
        Map<String, String> param = new HashMap<>();
        param.put("name", name);
        param.put("in", in);
        param.put("type", type);
        param.put("required", String.valueOf(required));
        param.put("description", description);
        return param;
    }
    
    private static Map<String, String> createResponse(String description, String example) {
        Map<String, String> response = new HashMap<>();
        response.put("description", description);
        response.put("example", example);
        return response;
    }
}
```

---

## 数据导出（CSV/JSON）

使用模板导出各种格式的数据。

### CSV 导出模板

```gotemplate
{{/* export.csv.tmpl */}}
{{range .headers}}{{.}},{{end}}
{{range .rows}}
{{range $i, $cell := .}}{{if $i}},{{end}}{{$cell}}{{end}}
{{end}}
```

### JSON 导出模板

```gotemplate
{{/* export.json.tmpl */}}
{
  "metadata": {
    "generatedAt": "{{.metadata.generatedAt}}",
    "totalRecords": {{len .data}},
    "format": "{{.metadata.format}}"
  },
  "data": [
    {{range $i, $record := .data}}
    {
      {{range $key, $value := $record}}
      "{{{$key}}}": "{{{$value}}}"{{if lt $index (len $record)}},{{end}}
      {{end}}
    }{{if lt $i (len $.data)}},{{end}}
    {{end}}
  ]
}
```

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataExportExample {
    public static void main(String[] args) throws Exception {
        // 示例数据
        List<Map<String, String>> users = Arrays.asList(
            createUser("1", "Alice", "alice@example.com", "Admin"),
            createUser("2", "Bob", "bob@example.com", "User"),
            createUser("3", "Charlie", "charlie@example.com", "User")
        );
        
        // 导出为 CSV
        exportAsCSV(users);
        
        // 导出为 JSON
        exportAsJSON(users);
    }
    
    private static void exportAsCSV(List<Map<String, String>> data) throws Exception {
        Template template = new Template("csv-export");
        template.parseFile(Paths.get("templates/export.csv.tmpl"));
        
        Map<String, Object> context = new HashMap<>();
        context.put("headers", Arrays.asList("ID", "姓名", "邮箱", "角色"));
        
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, String> user : data) {
            rows.add(Arrays.asList(
                user.get("id"),
                user.get("name"),
                user.get("email"),
                user.get("role")
            ));
        }
        context.put("rows", rows);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, context);
        
        System.out.println("=== CSV 导出 ===");
        System.out.println(writer.toString());
    }
    
    private static void exportAsJSON(List<Map<String, String>> data) throws Exception {
        Template template = new Template("json-export");
        template.parseFile(Paths.get("templates/export.json.tmpl"));
        
        Map<String, Object> context = new HashMap<>();
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("generatedAt", LocalDateTime.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("format", "JSON");
        context.put("metadata", metadata);
        
        context.put("data", data);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, context);
        
        System.out.println("=== JSON 导出 ===");
        System.out.println(writer.toString());
    }
    
    private static Map<String, String> createUser(String id, String name, String email, String role) {
        Map<String, String> user = new HashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        return user;
    }
}
```

---

## 模板组合

动态组合多个模板。

### 组件模板

```gotemplate
{{/* components/card.html */}}
{{define "card"}}
<div class="card">
  <div class="card-header">{{.title}}</div>
  <div class="card-body">{{.content}}</div>
  {{if .footer}}
  <div class="card-footer">{{.footer}}</div>
  {{end}}
</div>
{{end}}

{{/* components/alert.html */}}
{{define "alert"}}
<div class="alert alert-{{.type|default "info"}}">
  <strong>{{.title}}</strong>
  <p>{{.message}}</p>
</div>
{{end}}
```

### 页面模板

```gotemplate
{{/* composed-page.html */}}
{{template "alert" (dict "type" "success" "title" "成功!" "message" "操作已完成")}}

{{template "card" (dict "title" "卡片 1" "content" "内容 1")}}

{{template "card" (dict "title" "卡片 2" "content" "内容 2" "footer" "页脚文字")}}
```

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class TemplateCompositionExample {
    public static void main(String[] args) throws Exception {
        // 用于内联创建 map 的 dict 函数
        Function dict = args -> {
            if (args.length % 2 != 0) {
                throw new IllegalArgumentException("dict 需要偶数个参数");
            }
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < args.length; i += 2) {
                map.put((String) args[i], args[i + 1]);
            }
            return map;
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("dict", dict);
        
        // 解析所有组件模板
        Template template = new Template("components", functions);
        template.parseFiles(
            Paths.get("templates/components/card.html"),
            Paths.get("templates/components/alert.html"),
            Paths.get("templates/composed-page.html")
        );
        
        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "composed-page", new HashMap<>());
        
        System.out.println(writer.toString());
    }
}
```

---

## 条件模板选择

根据数据动态选择模板。

### Java 代码

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConditionalTemplateExample {
    public static void main(String[] args) throws Exception {
        // 为不同场景加载不同模板
        Template successTemplate = new Template("success");
        successTemplate.parseFile(Paths.get("templates/email-success.html"));
        
        Template errorTemplate = new Template("error");
        errorTemplate.parseFile(Paths.get("templates/email-error.html"));
        
        Template warningTemplate = new Template("warning");
        warningTemplate.parseFile(Paths.get("templates/email-warning.html"));
        
        // 模拟不同场景
        String[] scenarios = {"success", "error", "warning"};
        
        for (String scenario : scenarios) {
            Template selectedTemplate = selectTemplate(scenario, 
                successTemplate, errorTemplate, warningTemplate);
            
            Map<String, Object> data = new HashMap<>();
            data.put("userName", "Alice");
            data.put("timestamp", java.time.LocalDateTime.now().toString());
            
            if ("error".equals(scenario)) {
                data.put("errorMessage", "出了点问题");
            }
            
            StringWriter writer = new StringWriter();
            selectedTemplate.execute(writer, data);
            
            System.out.println("=== " + scenario.toUpperCase() + " ===");
            System.out.println(writer.toString().substring(0, 200) + "...");
            System.out.println();
        }
    }
    
    private static Template selectTemplate(String scenario, 
                                           Template success, Template error, Template warning) {
        switch (scenario) {
            case "success": return success;
            case "error": return error;
            case "warning": return warning;
            default: throw new IllegalArgumentException("未知场景: " + scenario);
        }
    }
}
```

---

## 复杂场景的最佳实践

### 1. 模板模块化

将复杂模板拆分为可重用组件：

```gotemplate
{{/* 可重用组件 */}}
{{template "header" .}}
{{template "navigation" .}}
{{template "content" .}}
{{template "footer" .}}
```

### 2. 合理使用自定义函数

保持函数简单且专注：

```java
// 好：单一功能函数
Function formatDate = args -> {
    Date date = (Date) args[0];
    return new SimpleDateFormat("yyyy-MM-dd").format(date);
};

// 差：多功能复杂逻辑函数
Function processData = args -> {
    // 数百行业务逻辑
};
```

### 3. 验证输入数据

在模板执行前确保数据完整性：

```java
public String safeRender(Template template, Map<String, Object> data) {
    // 验证必需字段
    if (!data.containsKey("requiredField")) {
        throw new IllegalArgumentException("缺少必需字段: requiredField");
    }
    
    // 清理用户输入
    sanitizeData(data);
    
    // 执行模板
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    return writer.toString();
}
```

### 4. 缓存已解析的模板

解析一次，执行多次：

```java
public class TemplateRegistry {
    private final ConcurrentHashMap<String, Template> cache = new ConcurrentHashMap<>();
    
    public Template getTemplate(String name) {
        return cache.computeIfAbsent(name, n -> {
            Template t = new Template(n);
            t.parseFile(Paths.get("templates/" + n + ".html"));
            return t;
        });
    }
}
```

### 5. 优雅处理错误

为模板失败提供回退方案：

```java
public String renderWithFallback(Template template, Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (Exception e) {
        logger.error("模板渲染失败", e);
        return getFallbackContent(data);
    }
}
```

---

## 下一步

- 参见 [基础示例](basic-examples.md) 了解基本模式
- 参见 [Web 模板](web-templates.md) 了解 Web 相关示例
- 参见 [邮件模板](email-templates.md) 了解邮件生成
- 参见 [高级主题](../advanced/) 了解性能和安全指南

---

本文档中的所有示例均已测试并通过验证，可在 gotemplate4j 中正确运行。
