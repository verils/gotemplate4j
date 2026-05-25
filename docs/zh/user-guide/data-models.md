# 使用 Java 数据模型

本指南解释如何在 gotemplate4j 模板中使用不同的 Java 数据类型。

## 概述

gotemplate4j 可以处理各种 Java 数据结构：
- Map
- JavaBean（带有 getter 的对象）
- List 和数组
- Enum 枚举
- Optional 值
- 原始类型

理解如何从这些结构中访问数据对于编写有效的模板至关重要。

## 数据上下文

执行模板时，将数据作为上下文传递：

```java
Template template = new Template("demo");
template.parse("你好, {{.Name}}!");

// 将数据作为 Map 传递
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");

StringWriter writer = new StringWriter();
template.execute(writer, data);
```

点号（`.`）在模板中表示此数据上下文。

## Map

Map 是模板中最灵活的数据结构。

### 基本 Map 访问

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");
data.put("Age", 30);
data.put("City", "北京");

template.parse("{{.Name}} 今年 {{.Age}} 岁，住在 {{.City}}");
```

**输出：**
```
Alice 今年 30 岁，住在 北京
```

### 嵌套 Map

```java
Map<String, Object> user = new HashMap<>();
user.put("Name", "Bob");

Map<String, Object> address = new HashMap<>();
address.put("Street", "123 Main St");
address.put("City", "上海");

user.put("Address", address);

template.parse("{{.Name}} 住在 {{.Address.Street}}, {{.Address.City}}");
```

**输出：**
```
Bob 住在 123 Main St, 上海
```

### Map 迭代

遍历 map 条目：

```java
Map<String, Object> config = new HashMap<>();
config.put("host", "localhost");
config.put("port", 8080);
config.put("debug", true);

template.parse(
    "{{range $key, $value := .Config}}" +
    "{{$key}}: {{$value}}\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Config", config);
```

**输出**（顺序可能不同）：
```
host: localhost
port: 8080
debug: true
```

### Map 键类型

使用 String 键的 Map 效果最好。其他键类型使用 `toString()`：

```java
Map<Integer, String> numberNames = new HashMap<>();
numberNames.put(1, "one");
numberNames.put(2, "two");

// 使用字符串表示访问
template.parse("{{.NumberNames.1}}");  // 输出: one
```

## JavaBean

JavaBean 是带有 getter 方法的对象。模板通过 getter 访问属性。

### 基本 JavaBean

```java
public class User {
    private String name;
    private int age;
    
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
}

// 在模板中使用
User user = new User("Alice", 30);
template.parse("姓名: {{.Name}}, 年龄: {{.Age}}");

StringWriter writer = new StringWriter();
template.execute(writer, user);
```

**输出：**
```
姓名: Alice, 年龄: 30
```

### Getter 方法解析

模板按以下顺序查找 getter：
1. `getFieldName()` - 标准 getter
2. `isFieldName()` - 布尔 getter
3. 公共字段 `fieldName`（如果没有 getter）

示例：

```java
public class Account {
    private boolean active;
    private String status;
    
    public boolean isActive() { return active; }
    public String getStatus() { return status; }
}

template.parse("活跃: {{.Active}}, 状态: {{.Status}}");
```

### 嵌套 JavaBean

```java
public class Address {
    private String street;
    private String city;
    
    public String getStreet() { return street; }
    public String getCity() { return city; }
}

public class User {
    private String name;
    private Address address;
    
    public String getName() { return name; }
    public Address getAddress() { return address; }
}

User user = new User("Bob", new Address("123 Main St", "北京"));
template.parse("{{.Name}} 住在 {{.Address.City}}");
```

### 混合 Map 和 Bean

可以混合使用 Map 和 JavaBean：

```java
Map<String, Object> data = new HashMap<>();
data.put("User", user);  // JavaBean
data.put("Settings", settingsMap);  // Map

template.parse("用户: {{.User.Name}}, 调试: {{.Settings.Debug}}");
```

### 使用 @TemplateField 进行字段名映射

从 0.8.0 版本开始，可以使用 `@TemplateField` 注解来显式控制 Java 字段和方法在模板中的访问方式。

#### 为什么使用 @TemplateField？

默认情况下，gotemplate4j 使用 Go 风格的名称转换（将首字母大写）来将 Java 属性名映射到模板标识符。`@TemplateField` 注解让你能显式控制此映射，在以下场景中非常有用：

- 想在模板中使用与 Java 字段名不同的名称
- 需要在重构期间支持旧的模板名称
- 想暴露私有字段而不创建公共 getter
- 需要精确控制模板 API

#### 基本用法

**在字段上：**

```java
import io.github.verils.gotemplate.TemplateField;

public class User {
    @TemplateField("UserName")
    private String userName = "Alice";
    
    @TemplateField("user_email")
    private String email = "alice@example.com";
}
```

**模板：**
```gotemplate
姓名: {{.UserName}}, 邮箱: {{.user_email}}
```

**输出：**
```
姓名: Alice, 邮箱: alice@example.com
```

**在方法上：**

```java
public class User {
    private String firstName = "John";
    private String lastName = "Doe";
    
    @TemplateField("FullName")
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

**模板：**
```gotemplate
{{.FullName}}
```

**输出：**
```
John Doe
```

#### 查找优先级

解析模板字段引用时，gotemplate4j 按以下顺序检查：

1. **@TemplateField 注解** - 精确匹配注解的字段/方法
2. **精确匹配** - 直接匹配 Java 属性/字段名
3. **Go 风格大写** - 首字母大写（例如 `userName` 变为 `UserName`）

演示优先级的示例：

```java
public class Product {
    // 可通过 {{.Price}} 访问（通过注解）
    @TemplateField("Price")
    private double price = 99.99;
    
    // 可通过 {{.name}}（精确匹配）或 {{.Name}}（Go 风格）访问
    public String name = "Widget";
}
```

#### 字段与方法优先级

如果字段和其 getter 方法都有相同的 `@TemplateField` 注解值，**字段优先**：

```java
public class Example {
    @TemplateField("value")
    public String fieldValue = "from-field";
    
    @TemplateField("value")
    public String getFieldValue() {
        return "from-method";
    }
}
```

**模板：**
```gotemplate
{{.value}}  // 输出: from-field
```

#### 私有字段访问

`@TemplateField` 注解允许模板直接访问私有字段，而无需公共 getter：

```java
public class Config {
    @TemplateField("ApiKey")
    private String apiKey = "secret-key";  // 私有但可访问
    
    // 不需要 getter!
}
```

**模板：**
```gotemplate
API 密钥: {{.ApiKey}}
```

这特别适用于：
- 封装数据同时保持模板简洁
- 避免编写样板 getter 方法
- 在重构期间保持向后兼容性

#### 继承支持

注解可以在类层次结构中工作：

```java
public class BaseEntity {
    @TemplateField("Id")
    private Long id;
    
    public Long getId() { return id; }
}

public class User extends BaseEntity {
    @TemplateField("UserName")
    private String userName;
    
    public String getUserName() { return userName; }
}
```

**模板：**
```gotemplate
ID: {{.Id}}, 姓名: {{.UserName}}
```

#### 最佳实践

1. **使用一致的命名**：选择一种命名约定（camelCase、PascalCase 或 snake_case）并保持一致
2. **文档化你的 API**：使用自定义名称时，为模板作者做好文档记录
3. **优先使用方法实现计算值**：在计算或格式化数据的方法上使用 `@TemplateField`
4. **将注解放在声明附近**：将注解直接放在字段/方法上以便清晰
5. **避免重复名称**：不要为多个字段/方法使用相同的模板名称

#### 迁移示例

重构字段名时，使用 `@TemplateField` 保持向后兼容性：

```java
public class LegacyUser {
    // 旧字段名 - 继续支持旧模板
    @TemplateField("usr_name")
    private String userName;  // 新的 Java 字段名
    
    // 可以逐步将模板从 {{.usr_name}} 迁移到 {{.UserName}}
}
```

这允许你重构 Java 代码而不会破坏现有模板。

## List 和数组

集合使用 `range` 或按索引访问。

### List 迭代

```java
List<String> items = Arrays.asList("Apple", "Banana", "Cherry");

template.parse(
    "项目:\n" +
    "{{range .Items}}" +
    "- {{.}}\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Items", items);
```

**输出：**
```
项目:
- Apple
- Banana
- Cherry
```

### 数组迭代

数组使用方式相同：

```java
String[] items = new String[]{"Apple", "Banana", "Cherry"};
data.put("Items", items);
// 相同的模板可以正常工作
```

### 带索引

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

**输出：**
```
0: Apple
1: Banana
2: Cherry
```

### 按索引访问

使用索引访问特定元素：

```gotemplate
第一个项目: {{index .Items 0}}
第二个项目: {{index .Items 1}}
```

注意：`index` 函数用于按位置访问元素。

### 空集合

检查集合是否为空：

```gotemplate
{{if .Items}}
  有 {{len .Items}} 个项目
{{else}}
  无项目
{{end}}
```

或显式检查长度：

```gotemplate
{{if eq (len .Items) 0}}
  空
{{end}}
```

### 对象列表

```java
List<User> users = Arrays.asList(
    new User("Alice", 30),
    new User("Bob", 25),
    new User("Charlie", 35)
);

template.parse(
    "用户:\n" +
    "{{range .Users}}" +
    "- {{.Name}} ({{.Age}})\n" +
    "{{end}}"
);

Map<String, Object> data = new HashMap<>();
data.put("Users", users);
```

**输出：**
```
用户:
- Alice (30)
- Bob (25)
- Charlie (35)
```

## 枚举

支持枚举并可以比较。

### 枚举定义

```java
public enum Status {
    ACTIVE, INACTIVE, PENDING
}

public class User {
    private String name;
    private Status status;
    
    public String getName() { return name; }
    public Status getStatus() { return status; }
}
```

### 在模板中使用枚举

```java
User user = new User("Alice", Status.ACTIVE);

template.parse(
    "{{if eq .Status \"ACTIVE\"}}" +
    "用户已激活" +
    "{{else}}" +
    "用户未激活" +
    "{{end}}"
);
```

### 枚举比较

比较枚举值：

```gotemplate
{{if eq .Status "ACTIVE"}}已激活{{end}}
{{if ne .Status "INACTIVE"}}非不活跃{{end}}
```

### 类 Switch 模式

```gotemplate
{{if eq .Status "ACTIVE"}}
  活跃用户
{{else if eq .Status "PENDING"}}
  等待审批
{{else}}
  不活跃用户
{{end}}
```

## Optional 值

支持 Java 8 的 `Optional`。

### 基本 Optional

```java
Optional<String> name = Optional.of("Alice");
Optional<String> email = Optional.empty();

Map<String, Object> data = new HashMap<>();
data.put("Name", name);
data.put("Email", email);

template.parse(
    "姓名: {{.Name}}\n" +
    "邮箱: {{.Email}}"
);
```

**输出：**
```
姓名: Alice
邮箱: 
```

空的 Optional 渲染为空字符串。

### 检查 Optional

```gotemplate
{{if .Email}}
  邮箱: {{.Email}}
{{else}}
  未提供邮箱
{{end}}
```

### 条件中的 Optional

Optional 有值时视为真，为空时视为假：

```gotemplate
{{if .Name}}有姓名{{else}}无姓名{{end}}
```

## 原始类型

支持所有 Java 原始类型及其包装类。

### 数字

```java
Map<String, Object> data = new HashMap<>();
data.put("Count", 42);
data.put("Price", 19.99);
data.put("Large", 1000000L);
data.put("Small", 0.001f);

template.parse(
    "计数: {{.Count}}\n" +
    "价格: {{.Price}}\n" +
    "大数: {{.Large}}\n" +
    "小数: {{.Small}}"
);
```

### 布尔值

```java
data.put("IsActive", true);
data.put("IsDeleted", false);

template.parse(
    "{{if .IsActive}}活跃{{end}}\n" +
    "{{if .IsDeleted}}已删除{{else}}未删除{{end}}"
);
```

### 字符串

```java
data.put("Name", "Alice");
data.put("Empty", "");
data.put("Null", null);

template.parse(
    "姓名: '{{.Name}}'\n" +
    "空: '{{.Empty}}'\n" +
    "Null: '{{.Null}}'"
);
```

## Null 处理

Null 值根据 MissingKeyPolicy 处理。

### 默认行为

默认情况下，null 值渲染为空字符串：

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", null);

template.parse("姓名: '{{.Name}}'");
// 输出: 姓名: ''
```

### MissingKeyPolicy 选项

配置缺失/null 键的处理方式：

```java
// 选项 1: DEFAULT - 渲染为空字符串
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.DEFAULT);

// 选项 2: ZERO - 渲染为零值
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ZERO);

// 选项 3: ERROR - 抛出异常
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

### 检查 Null

```gotemplate
{{if .Name}}
  姓名: {{.Name}}
{{else}}
  未提供姓名
{{end}}
```

## 复杂数据结构

实际数据通常组合多种类型。

### 示例：用户资料

```java
// 定义类
public class Address {
    private String street;
    private String city;
    private String country;
    
    // Getter...
}

public class User {
    private String name;
    private int age;
    private String email;
    private List<Address> addresses;
    private Map<String, String> preferences;
    
    // Getter...
}

// 创建数据
Address addr1 = new Address("123 Main St", "北京", "中国");
Address addr2 = new Address("456 Oak Ave", "上海", "中国");

User user = new User();
user.setName("Alice");
user.setAge(30);
user.setEmail("alice@example.com");
user.setAddresses(Arrays.asList(addr1, addr2));

Map<String, String> prefs = new HashMap<>();
prefs.put("theme", "dark");
prefs.put("language", "zh");
user.setPreferences(prefs);

// 模板
String templateText = 
    "用户: {{.Name}}\n" +
    "年龄: {{.Age}}\n" +
    "邮箱: {{.Email}}\n" +
    "\n" +
    "地址:\n" +
    "{{range .Addresses}}" +
    "- {{.Street}}, {{.City}}, {{.Country}}\n" +
    "{{end}}" +
    "\n" +
    "偏好:\n" +
    "{{range $key, $value := .Preferences}}" +
    "{{$key}}: {{$value}}\n" +
    "{{end}}";

Template template = new Template("profile");
template.parse(templateText);

StringWriter writer = new StringWriter();
template.execute(writer, user);
System.out.println(writer.toString());
```

**输出：**
```
用户: Alice
年龄: 30
邮箱: alice@example.com

地址:
- 123 Main St, 北京, 中国
- 456 Oak Ave, 上海, 中国

偏好:
theme: dark
language: zh
```

## 类型转换

gotemplate4j 在某些上下文中执行自动类型转换。

### 数字转换

数字可以跨类型比较：

```gotemplate
{{if eq .IntValue .LongValue}}相等{{end}}
```

### 字符串转换

值会被转换为字符串以显示：

```java
data.put("Number", 42);
template.parse("值: {{.Number}}");  // 输出: 值: 42
```

### 布尔转换

在条件中，值会根据真值性进行求值（参见[模板语法](template-syntax.md)）。

## 最佳实践

### 1. 简单数据使用 Map

对于简单模板，Map 最容易使用：

```java
Map<String, Object> data = new HashMap<>();
data.put("Name", "Alice");
data.put("Age", 30);
```

### 2. 复杂数据使用 JavaBean

对于带行为的结构化数据，使用 JavaBean：

```java
public class User {
    private String name;
    private int age;
    
    public String getDisplayName() {
        return name + " (年龄: " + age + ")";
    }
    
    // Getter...
}
```

### 3. 避免深度嵌套

尽量保持数据结构扁平：

❌ **不好：**
```java
data.put("User", Map.of(
    "Profile", Map.of(
        "Details", Map.of(
            "Name", "Alice"
        )
    )
));
```
```gotemplate
{{.User.Profile.Details.Name}}
```

✅ **好：**
```java
data.put("UserName", "Alice");
```
```gotemplate
{{.UserName}}
```

### 4. 处理缺失键

始终考虑数据缺失时会发生什么：

```gotemplate
{{if .Name}}{{.Name}}{{else}}匿名{{end}}
```

### 5. 文档化数据结构

记录模板期望的数据：

```java
/**
 * 模板期望:
 * - User: 带有 Name、Email 的 User 对象
 * - Items: Item 对象列表
 * - ShowFooter: 布尔值
 */
```

## 常见模式

### 模式 1：默认值

```gotemplate
{{if .Title}}{{.Title}}{{else}}默认标题{{end}}
```

或创建自定义 `default` 函数：

```java
functions.put("default", args -> {
    if (args[0] == null || args[0].equals("")) {
        return args[1];
    }
    return args[0];
});
```
```gotemplate
{{.Title | default "默认标题"}}
```

### 模式 2：条件段落

```gotemplate
{{if .ShowDetails}}
  <div class="details">
    {{.Details}}
  </div>
{{end}}
```

### 模式 3：带交替样式的循环

```gotemplate
{{range $index, $item := .Items}}
  <div class="{{if mod $index 2 | eq 0}}even{{else}}odd{{end}}">
    {{$item}}
  </div>
{{end}}
```

### 模式 4：带回退的嵌套数据

```gotemplate
{{if .User}}
  {{if .User.Name}}
    {{.User.Name}}
  {{else}}
    匿名用户
  {{end}}
{{else}}
  无用户
{{end}}
```

## 问题排查

### 问题：字段未找到

**错误：** `can't evaluate field X in type Y`

**解决方案：** 检查：
1. 字段名是否正确（区分大小写）
2. getter 方法是否存在（对于 JavaBean）
3. 键是否存在（对于 Map）

### 问题：空指针异常

**解决方案：** 在访问前检查是否为 null：

```gotemplate
{{if .User}}{{.User.Name}}{{end}}
```

### 问题：类型错误

**错误：** `unexpected type`

**解决方案：** 确保对类型使用正确的操作：

```gotemplate
<!-- 不能遍历字符串 -->
{{range .Name}}...{{end}}  <!-- 如果 .Name 是字符串则会出错 -->

<!-- 不能比较不兼容的类型 -->
{{if eq .StringField .IntField}}...{{end}}  <!-- 可能会失败 -->
```

---

**下一步：**
- 🔧 了解[函数](functions.md)以进行数据操作
- 🎯 探索[控制流](control-flow.md)以进行逻辑处理
- 📚 查阅[模板语法](template-syntax.md)获取完整参考
