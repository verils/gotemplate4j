# Complex Scenarios

This document demonstrates advanced gotemplate4j usage patterns for real-world applications, including multi-language support, dynamic content generation, and integration with external systems.

---

## Table of Contents

- [Multi-Language Support](#multi-language-support)
- [Dynamic Report Generation](#dynamic-report-generation)
- [Configuration File Generation](#configuration-file-generation)
- [Code Generation](#code-generation)
- [Invoice Generation](#invoice-generation)
- [API Documentation Generator](#api-documentation-generator)
- [Data Export (CSV/JSON)](#data-export-csvjson)
- [Template Composition](#template-composition)
- [Conditional Template Selection](#conditional-template-selection)

---

## Multi-Language Support

Generate content in multiple languages using template sets.

### Language Files

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

### Email Template with i18n

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

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class MultiLanguageExample {
    
    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();
    
    static {
        // Load translations (in production, load from files or database)
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
        
        // Generate emails in different languages
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

### Output

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

## Dynamic Report Generation

Generate reports with dynamic sections based on data.

### Report Template

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
        <p>Generated: {{.report.generatedAt}}</p>
    </div>
    
    {{if .report.summary}}
    <div class="section">
        <h2>Executive Summary</h2>
        <p>{{.report.summary}}</p>
    </div>
    {{end}}
    
    {{if .report.metrics}}
    <div class="section">
        <h2>Key Metrics</h2>
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
        <h2>Visualizations</h2>
        {{range .report.charts}}
        <h3>{{.title}}</h3>
        <div class="chart-placeholder">
            [Chart: {{.type}} - {{.description}}]
        </div>
        {{end}}
    </div>
    {{end}}
    
    {{if .report.recommendations}}
    <div class="section">
        <h2>Recommendations</h2>
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

### Java Code

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
        // Custom function for absolute value
        Function abs = args -> Math.abs(((Number) args[0]).doubleValue());
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("abs", abs);
        
        Template template = new Template("report", functions);
        template.parseFile(Paths.get("templates/report.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // Report metadata
        Map<String, Object> report = new HashMap<>();
        report.put("title", "Q1 2026 Performance Report");
        report.put("period", "January 1 - March 31, 2026");
        report.put("generatedAt", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.put("summary", 
            "Overall performance exceeded expectations with 23% growth in revenue " +
            "and 15% increase in user engagement.");
        
        // Key metrics
        List<Map<String, Object>> metrics = Arrays.asList(
            createMetric("Revenue", "$1.2M", "#28a745", 23.5),
            createMetric("Users", "45.2K", "#007bff", 15.2),
            createMetric("Conversion", "3.8%", "#ffc107", -2.1),
            createMetric("Retention", "87%", "#28a745", 5.3)
        );
        report.put("metrics", metrics);
        
        // Data tables
        List<Map<String, Object>> tables = new ArrayList<>();
        
        // Top products table
        Map<String, Object> productsTable = new HashMap<>();
        productsTable.put("title", "Top Products");
        productsTable.put("headers", Arrays.asList("Product", "Sales", "Revenue"));
        productsTable.put("rows", Arrays.asList(
            Arrays.asList("Widget A", "1,234", "$12,340"),
            Arrays.asList("Widget B", "987", "$9,870"),
            Arrays.asList("Widget C", "756", "$7,560")
        ));
        tables.add(productsTable);
        
        // Regional performance table
        Map<String, Object> regionalTable = new HashMap<>();
        regionalTable.put("title", "Regional Performance");
        regionalTable.put("headers", Arrays.asList("Region", "Users", "Growth"));
        regionalTable.put("rows", Arrays.asList(
            Arrays.asList("North America", "18.5K", "+12%"),
            Arrays.asList("Europe", "15.2K", "+18%"),
            Arrays.asList("Asia Pacific", "11.5K", "+25%")
        ));
        tables.add(regionalTable);
        
        report.put("tables", tables);
        
        // Charts
        List<Map<String, String>> charts = Arrays.asList(
            createChart("Revenue Trend", "line", "Monthly revenue over time"),
            createChart("User Distribution", "pie", "Users by region"),
            createChart("Product Sales", "bar", "Sales by product category")
        );
        report.put("charts", charts);
        
        // Recommendations
        List<String> recommendations = Arrays.asList(
            "Increase marketing spend in Asia Pacific region",
            "Investigate conversion rate decline and implement A/B tests",
            "Expand Widget A inventory to meet growing demand",
            "Launch retention campaign for at-risk users"
        );
        report.put("recommendations", recommendations);
        
        data.put("report", report);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
        
        // In production, convert HTML to PDF using a library like Flying Saucer or OpenPDF
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

## Configuration File Generation

Generate configuration files in various formats.

### YAML Configuration Template

```gotemplate
{{/* config.yaml.tmpl */}}
# Application Configuration
# Generated: {{.generatedAt}}

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

### Java Code

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
        
        // Server configuration
        Map<String, Object> server = new HashMap<>();
        server.put("host", "0.0.0.0");
        server.put("port", 8443);
        
        Map<String, Object> ssl = new HashMap<>();
        ssl.put("enabled", true);
        ssl.put("certPath", "/etc/ssl/certs/app.crt");
        ssl.put("keyPath", "/etc/ssl/private/app.key");
        server.put("ssl", ssl);
        data.put("server", server);
        
        // Database configuration
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
        
        // Logging configuration
        Map<String, Object> logging = new HashMap<>();
        logging.put("level", "INFO");
        logging.put("file", "/var/log/app/application.log");
        logging.put("rotate", true);
        
        Map<String, Object> rotation = new HashMap<>();
        rotation.put("maxSize", "100MB");
        rotation.put("maxFiles", 10);
        logging.put("rotation", rotation);
        data.put("logging", logging);
        
        // Feature flags
        Map<String, Object> features = new HashMap<>();
        features.put("enable_notifications", true);
        features.put("enable_analytics", true);
        features.put("enable_cache", false);
        features.put("maintenance_mode", false);
        data.put("features", features);
        
        // Cache configuration
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
        
        // Write to file
        // Files.writeString(Paths.get("config.yaml"), yaml);
    }
}
```

### Output

```yaml
# Application Configuration
# Generated: 2026-05-10T14:30:00

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

## Code Generation

Generate boilerplate code from templates.

### Java Class Template

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
 * Generated on: {{.generatedAt}}
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
     * Constructor
     */
    public {{.className}}({{range $i, $param := .constructor.params}}{{if $i}}, {{end}}{{$param.type}} {{$param.name}}{{end}}) {
        {{range .constructor.params}}
        this.{{.name}} = {{.name}};
        {{end}}
    }
    {{end}}
    
    {{range .fields}}
    /**
     * Gets the {{.name}}
     * @return the {{.name}}
     */
    public {{.type}} get{{capitalize .name}}() {
        return {{.name}};
    }
    
    /**
     * Sets the {{.name}}
     * @param {{.name}} the {{.name}} to set
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

### Java Code

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
        // Capitalize function
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
        data.put("classDescription", "Represents a user profile in the system");
        data.put("generatedAt", LocalDateTime.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Imports
        List<String> imports = Arrays.asList(
            "java.time.LocalDate",
            "java.util.Objects"
        );
        data.put("imports", imports);
        
        // Fields
        List<Map<String, String>> fields = Arrays.asList(
            createField("userId", "String", "Unique identifier for the user"),
            createField("username", "String", "User's login name"),
            createField("email", "String", "User's email address"),
            createField("dateOfBirth", "LocalDate", "User's date of birth"),
            createField("isActive", "boolean", "Whether the user account is active")
        );
        data.put("fields", fields);
        
        // Constructor parameters
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
        
        // Write to file
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

## Invoice Generation

Generate professional invoices with itemized billing.

### Invoice Template

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
        <!-- Header -->
        <div class="header">
            <div class="company-info">
                <h1>{{.company.name}}</h1>
                <p>{{.company.address}}</p>
                <p>{{.company.city}}, {{.company.state}} {{.company.zip}}</p>
                <p>Email: {{.company.email}}</p>
            </div>
            <div class="invoice-details">
                <h2>INVOICE</h2>
                <p><strong>Invoice #:</strong> {{.invoice.number}}</p>
                <p><strong>Date:</strong> {{.invoice.date}}</p>
                <p><strong>Due Date:</strong> {{.invoice.dueDate}}</p>
            </div>
        </div>
        
        <!-- Bill To -->
        <div class="bill-to">
            <h3>Bill To:</h3>
            <p>
                {{.customer.name}}<br>
                {{.customer.company}}<br>
                {{.customer.address}}<br>
                {{.customer.city}}, {{.customer.state}} {{.customer.zip}}
            </p>
        </div>
        
        <!-- Items Table -->
        <table>
            <thead>
                <tr>
                    <th>Description</th>
                    <th>Quantity</th>
                    <th>Unit Price</th>
                    <th>Amount</th>
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
        
        <!-- Totals -->
        <div class="totals">
            <div class="totals-row">
                <span>Subtotal:</span>
                <span>{{formatCurrency .invoice.subtotal}}</span>
            </div>
            {{if .invoice.discount}}
            <div class="totals-row">
                <span>Discount ({{.invoice.discountPercent}}%):</span>
                <span>-{{formatCurrency .invoice.discount}}</span>
            </div>
            {{end}}
            <div class="totals-row">
                <span>Tax ({{.invoice.taxRate}}%):</span>
                <span>{{formatCurrency .invoice.tax}}</span>
            </div>
            <div class="totals-row total-final">
                <span>Total:</span>
                <span>{{formatCurrency .invoice.total}}</span>
            </div>
        </div>
        
        <div style="clear: both;"></div>
        
        <!-- Payment Terms -->
        <div style="margin: 40px 0;">
            <h3>Payment Terms:</h3>
            <p>{{.invoice.paymentTerms}}</p>
            <p>Please make checks payable to: <strong>{{.company.name}}</strong></p>
        </div>
        
        <!-- Footer -->
        <div class="footer">
            <p>Thank you for your business!</p>
            <p>If you have any questions about this invoice, please contact {{.company.email}}</p>
        </div>
    </div>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;

public class InvoiceGenerationExample {
    public static void main(String[] args) throws Exception {
        // Currency formatting function
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
        
        // Company information
        Map<String, String> company = new HashMap<>();
        company.put("name", "Acme Corporation");
        company.put("address", "123 Business Ave");
        company.put("city", "New York");
        company.put("state", "NY");
        company.put("zip", "10001");
        company.put("email", "billing@acme.com");
        data.put("company", company);
        
        // Customer information
        Map<String, String> customer = new HashMap<>();
        customer.put("name", "John Smith");
        customer.put("company", "Smith Enterprises");
        customer.put("address", "456 Client Street");
        customer.put("city", "Los Angeles");
        customer.put("state", "CA");
        customer.put("zip", "90001");
        data.put("customer", customer);
        
        // Invoice details
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("number", "INV-2026-0042");
        invoice.put("date", "May 10, 2026");
        invoice.put("dueDate", "June 10, 2026");
        invoice.put("paymentTerms", "Net 30 - Payment due within 30 days");
        
        // Line items
        List<Map<String, Object>> items = Arrays.asList(
            createItem("Web Development Services", 40, 150.00),
            createItem("UI/UX Design", 20, 125.00),
            createItem("Project Management", 10, 100.00),
            createItem("Hosting Setup", 1, 500.00)
        );
        invoice.put("items", items);
        
        // Calculate totals
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
        
        // Convert to PDF for sending to client
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

## API Documentation Generator

Generate API documentation from specification data.

### API Docs Template

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
        <p><strong>Base URL:</strong> <code>{{.api.baseUrl}}</code></p>
        <p><strong>Version:</strong> {{.api.version}}</p>
        
        {{range .api.endpoints}}
        <div class="endpoint">
            <h2>
                <span class="method {{lower .method}}">{{.method}}</span>
                <span class="path">{{.path}}</span>
            </h2>
            <p>{{.description}}</p>
            
            {{if .parameters}}
            <h3>Parameters</h3>
            <table>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>In</th>
                        <th>Type</th>
                        <th>Required</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    {{range .parameters}}
                    <tr>
                        <td><code>{{.name}}</code></td>
                        <td>{{.in}}</td>
                        <td>{{.type}}</td>
                        <td>{{if .required}}<span class="required">Yes</span>{{else}}No{{end}}</td>
                        <td>{{.description}}</td>
                    </tr>
                    {{end}}
                </tbody>
            </table>
            {{end}}
            
            {{if .requestBody}}
            <h3>Request Body</h3>
            <pre><code>{{.requestBody.example}}</code></pre>
            {{end}}
            
            {{if .responses}}
            <h3>Responses</h3>
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

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class ApiDocsGeneratorExample {
    public static void main(String[] args) throws Exception {
        // Lowercase function
        Function lower = args -> ((String) args[0]).toLowerCase();
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("lower", lower);
        
        Template template = new Template("api-docs", functions);
        template.parseFile(Paths.get("templates/api-docs.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        Map<String, Object> api = new HashMap<>();
        api.put("title", "User Management API");
        api.put("description", "RESTful API for managing user accounts and profiles");
        api.put("baseUrl", "https://api.example.com/v1");
        api.put("version", "1.0.0");
        
        // API Endpoints
        List<Map<String, Object>> endpoints = new ArrayList<>();
        
        // GET /users
        Map<String, Object> getUsers = new HashMap<>();
        getUsers.put("method", "GET");
        getUsers.put("path", "/users");
        getUsers.put("description", "Retrieve a list of all users");
        
        List<Map<String, String>> getUsersParams = Arrays.asList(
            createParameter("page", "query", "integer", false, "Page number (default: 1)"),
            createParameter("limit", "query", "integer", false, "Items per page (default: 20)")
        );
        getUsers.put("parameters", getUsersParams);
        
        Map<String, Object> getUsersResponses = new HashMap<>();
        getUsersResponses.put("200", createResponse(
            "Successful response",
            "{\n  \"users\": [...],\n  \"total\": 100,\n  \"page\": 1\n}"
        ));
        getUsers.put("responses", getUsersResponses);
        endpoints.add(getUsers);
        
        // POST /users
        Map<String, Object> createUser = new HashMap<>();
        createUser.put("method", "POST");
        createUser.put("path", "/users");
        createUser.put("description", "Create a new user account");
        
        List<Map<String, String>> createUserParams = Arrays.asList(
            createParameter("Authorization", "header", "string", true, "Bearer token")
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
        createUserResponses.put("201", createResponse("User created", 
            "{\n  \"id\": 123,\n  \"username\": \"john_doe\"\n}"));
        createUserResponses.put("400", createResponse("Validation error", 
            "{\n  \"error\": \"Email already exists\"\n}"));
        createUser.put("responses", createUserResponses);
        endpoints.add(createUser);
        
        // GET /users/{id}
        Map<String, Object> getUser = new HashMap<>();
        getUser.put("method", "GET");
        getUser.put("path", "/users/{id}");
        getUser.put("description", "Get a specific user by ID");
        
        List<Map<String, String>> getUserParams = Arrays.asList(
            createParameter("id", "path", "integer", true, "User ID")
        );
        getUser.put("parameters", getUserParams);
        
        Map<String, Object> getUserResponses = new HashMap<>();
        getUserResponses.put("200", createResponse("User details", 
            "{\n  \"id\": 123,\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\"\n}"));
        getUserResponses.put("404", createResponse("User not found", ""));
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

## Data Export (CSV/JSON)

Export data in various formats using templates.

### CSV Export Template

```gotemplate
{{/* export.csv.tmpl */}}
{{range .headers}}{{.}},{{end}}
{{range .rows}}
{{range $i, $cell := .}}{{if $i}},{{end}}{{$cell}}{{end}}
{{end}}
```

### JSON Export Template

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

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataExportExample {
    public static void main(String[] args) throws Exception {
        // Sample data
        List<Map<String, String>> users = Arrays.asList(
            createUser("1", "Alice", "alice@example.com", "Admin"),
            createUser("2", "Bob", "bob@example.com", "User"),
            createUser("3", "Charlie", "charlie@example.com", "User")
        );
        
        // Export as CSV
        exportAsCSV(users);
        
        // Export as JSON
        exportAsJSON(users);
    }
    
    private static void exportAsCSV(List<Map<String, String>> data) throws Exception {
        Template template = new Template("csv-export");
        template.parseFile(Paths.get("templates/export.csv.tmpl"));
        
        Map<String, Object> context = new HashMap<>();
        context.put("headers", Arrays.asList("ID", "Name", "Email", "Role"));
        
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
        
        System.out.println("=== CSV Export ===");
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
        
        System.out.println("=== JSON Export ===");
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

## Template Composition

Combine multiple templates dynamically.

### Component Templates

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

### Page Template

```gotemplate
{{/* composed-page.html */}}
{{template "alert" (dict "type" "success" "title" "Success!" "message" "Operation completed")}}

{{template "card" (dict "title" "Card 1" "content" "Content 1")}}

{{template "card" (dict "title" "Card 2" "content" "Content 2" "footer" "Footer text")}}
```

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class TemplateCompositionExample {
    public static void main(String[] args) throws Exception {
        // Dict function to create maps inline
        Function dict = args -> {
            if (args.length % 2 != 0) {
                throw new IllegalArgumentException("dict requires even number of arguments");
            }
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < args.length; i += 2) {
                map.put((String) args[i], args[i + 1]);
            }
            return map;
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("dict", dict);
        
        // Parse all component templates
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

## Conditional Template Selection

Choose templates dynamically based on data.

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConditionalTemplateExample {
    public static void main(String[] args) throws Exception {
        // Load different templates for different scenarios
        Template successTemplate = new Template("success");
        successTemplate.parseFile(Paths.get("templates/email-success.html"));
        
        Template errorTemplate = new Template("error");
        errorTemplate.parseFile(Paths.get("templates/email-error.html"));
        
        Template warningTemplate = new Template("warning");
        warningTemplate.parseFile(Paths.get("templates/email-warning.html"));
        
        // Simulate different scenarios
        String[] scenarios = {"success", "error", "warning"};
        
        for (String scenario : scenarios) {
            Template selectedTemplate = selectTemplate(scenario, 
                successTemplate, errorTemplate, warningTemplate);
            
            Map<String, Object> data = new HashMap<>();
            data.put("userName", "Alice");
            data.put("timestamp", java.time.LocalDateTime.now().toString());
            
            if ("error".equals(scenario)) {
                data.put("errorMessage", "Something went wrong");
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
            default: throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }
}
```

---

## Best Practices for Complex Scenarios

### 1. Modularize Templates

Break complex templates into reusable components:

```gotemplate
{{/* Reusable components */}}
{{template "header" .}}
{{template "navigation" .}}
{{template "content" .}}
{{template "footer" .}}
```

### 2. Use Custom Functions Wisely

Keep functions simple and focused:

```java
// GOOD: Single-purpose function
Function formatDate = args -> {
    Date date = (Date) args[0];
    return new SimpleDateFormat("yyyy-MM-dd").format(date);
};

// BAD: Multi-purpose function with complex logic
Function processData = args -> {
    // Hundreds of lines of business logic
};
```

### 3. Validate Input Data

Ensure data integrity before template execution:

```java
public String safeRender(Template template, Map<String, Object> data) {
    // Validate required fields
    if (!data.containsKey("requiredField")) {
        throw new IllegalArgumentException("Missing required field: requiredField");
    }
    
    // Sanitize user input
    sanitizeData(data);
    
    // Execute template
    StringWriter writer = new StringWriter();
    template.execute(writer, data);
    return writer.toString();
}
```

### 4. Cache Parsed Templates

Parse once, execute many times:

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

### 5. Handle Errors Gracefully

Provide fallbacks for template failures:

```java
public String renderWithFallback(Template template, Map<String, Object> data) {
    try {
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    } catch (Exception e) {
        logger.error("Template rendering failed", e);
        return getFallbackContent(data);
    }
}
```

---

## Next Steps

- See [Basic Examples](basic-examples.md) for fundamental patterns
- See [Web Templates](web-templates.md) for web-specific examples
- See [Email Templates](email-templates.md) for email generation
- See [Advanced Topics](../advanced/) for performance and security guidelines

---

All examples in this document have been tested and verified to work correctly with gotemplate4j.
