# 控制流指南

本指南解释 gotemplate4j 中的控制流结构：条件语句、循环和上下文管理。

## 概述

控制流允许你为模板添加逻辑：
- **条件语句**：使用 `if`/`else` 做出决策
- **循环**：使用 `range` 遍历集合
- **上下文**：使用 `with` 更改数据上下文
- **循环控制**：使用 `break` 和 `continue`

## 条件动作

### 基本 If

`if` 动作对条件求值，当结果为真时渲染内容。

```gotemplate
{{if .IsActive}}
  用户已激活
{{end}}
```

### If/Else

```gotemplate
{{if .IsActive}}
  活跃用户
{{else}}
  非活跃用户
{{end}}
```

### If/Else If/Else

串联多个条件：

```gotemplate
{{if eq .Status "active"}}
  已激活
{{else if eq .Status "pending"}}
  待处理
{{else if eq .Status "suspended"}}
  已暂停
{{else}}
  未知状态
{{end}}
```

### 嵌套条件

```gotemplate
{{if .User}}
  {{if .User.Name}}
    你好, {{.User.Name}}!
  {{else}}
    你好, 匿名用户!
  {{end}}
{{else}}
  没有用户登录
{{end}}
```

## 真值判断

条件会对值的"真值性"进行求值。

### 假值

以下值被视为 **false**：
- 布尔值 `false`
- 数字零（`0`、`0.0` 等）
- `null` 或缺失值
- 空字符串 `""`
- 空集合（长度为 0）

### 真值

所有其他值视为 **true**：
- 布尔值 `true`
- 非零数字
- 非空字符串
- 非空集合
- 任何对象（即使字段为 null）

### 示例

```gotemplate
{{if 0}}零是假{{end}}        <!-- 不会输出 -->
{{if 1}}非零是真{{end}}     <!-- 会输出 -->

{{if ""}}空字符串是假{{end}}      <!-- 不会输出 -->
{{if "x"}}非空是真{{end}}         <!-- 会输出 -->

{{if nil}}Nil 是假{{end}}              <!-- 不会输出 -->

{{if .EmptyList}}有项目{{end}}          <!-- 如果为空则不输出 -->
{{if .NonEmptyList}}有项目{{end}}       <!-- 会输出 -->
```

## 比较运算符

在条件中使用比较函数。

### 等于（eq）

```gotemplate
{{if eq .Status "active"}}已激活{{end}}
{{if eq .Age 18}}正好18岁{{end}}
{{if eq .Count 0}}无项目{{end}}
```

比较多个值（所有值必须相等）：

```gotemplate
{{if eq .A .B .C}}全部相等{{end}}
```

### 不等于（ne）

```gotemplate
{{if ne .Status "inactive"}}非不活跃{{end}}
{{if ne .Password .ConfirmPassword}}密码不匹配{{end}}
```

### 小于（lt）

```gotemplate
{{if lt .Age 18}}未成年人{{end}}
{{if lt .Score 60}}不及格{{end}}
```

### 小于等于（lte）

```gotemplate
{{if lte .Age 18}}18岁或以下{{end}}
{{if lte .Items 100}}在限制内{{end}}
```

### 大于（gt）

```gotemplate
{{if gt .Age 18}}成年人{{end}}
{{if gt .Score 90}}优秀{{end}}
```

### 大于等于（gte）

```gotemplate
{{if gte .Age 18}}18岁或以上{{end}}
{{if gte .Balance 1000}}高级客户{{end}}
```

## 逻辑运算符

### AND

短路求值：如果第一个参数为 false，第二个不会被求值。

```gotemplate
{{if and .IsActive .IsVerified}}已激活且已验证{{end}}

<!-- 多个条件 -->
{{if and .IsActive .IsVerified .HasPermission}}已授予访问权限{{end}}
```

### OR

短路求值：如果第一个参数为 true，第二个不会被求值。

```gotemplate
{{if or .IsAdmin .IsModerator}}有权限{{end}}

<!-- 多个条件 -->
{{if or .IsAdmin .IsModerator .IsOwner}}完全访问权限{{end}}
```

### NOT

```gotemplate
{{if not .IsDeleted}}未删除{{end}}
{{if not .IsEmpty}}有内容{{end}}
```

### 复杂逻辑

组合运算符：

```gotemplate
{{if and (or .IsAdmin .IsModerator) (not .IsSuspended)}}
  可以管理
{{end}}
```

对于复杂逻辑，考虑在 Java 中计算：

```java
data.put("CanModerate", user.hasPermission() && !user.isSuspended());
```
```gotemplate
{{if .CanModerate}}可以管理{{end}}
```

## Range 动作

遍历数组、列表、映射和整数。

### 基本 Range

```gotemplate
{{range .Items}}
  {{.}}
{{end}}
```

在 range 块内，`.` 指向当前项目。

### 带索引的 Range

```gotemplate
{{range $index, $item := .Items}}
  {{$index}}: {{$item}}
{{end}}
```

输出：
```
0: Apple
1: Banana
2: Cherry
```

### 遍历 Map

```gotemplate
{{range $key, $value := .Config}}
  {{$key}}: {{$value}}
{{end}}
```

#### Map 迭代顺序

**默认情况下，map 键会进行排序以提供确定性输出**，与 Go 模板行为一致。

**Go 模板规范**："如果值是 map 且键是定义了顺序的基本类型，则元素将按键的排序顺序访问。"

**排序行为**：
- **字符串键**：按字母顺序排序（字典序）
- **整数键**：按数值排序
- **可比较键**：使用自然顺序
- **其他类型**：退回到 `toString()` 比较

**示例 - 字符串键**：

```java
Map<String, Object> data = new HashMap<>();
Map<String, String> config = new LinkedHashMap<>();
config.put("zebra", "z");      // 最先插入
config.put("apple", "a");      // 第二个插入
config.put("mango", "m");      // 第三个插入
data.put("Config", config);

Template template = new Template("demo");
template.parse("{{range $k, $v := .Config}}{{$k}}={{$v}},{{end}}");
template.execute(writer, data);
// 输出: apple=a,mango=m,zebra=z,  （按字母顺序排序）
```

**示例 - 整数键**：

```java
Map<Integer, String> scores = new LinkedHashMap<>();
scores.put(3, "third");   // 最先插入
scores.put(1, "first");   // 第二个插入
scores.put(2, "second");  // 第三个插入
data.put("Scores", scores);

template.parse("{{range $k, $v := .Scores}}{{$k}}:{{$v}},{{end}}");
// 输出: 1:first,2:second,3:third  （按数值排序）
```

#### 禁用 Map 键排序

如果需要保留插入顺序（例如使用 `LinkedHashMap` 时），可以禁用排序：

```java
Template template = new Template("demo")
    .withMapKeySorting(false);  // 禁用排序
```

或通过选项字符串：

```java
Template template = new Template("demo");
template.option("mapkeysorting=false");
```

**何时禁用排序**：
- 依赖 `LinkedHashMap` 的特定插入顺序时
- 对非常大 map 的性能关键场景
- 需要与依赖插入顺序的现有模板保持兼容时

**注意**：对于 Go 模板兼容性，建议保持排序启用（默认）。

### 遍历整数（Go 兼容）

从 0 迭代到 n-1：

```gotemplate
{{range $i := 5}}
  迭代 {{$i}}
{{end}}
```

输出：
```
迭代 0
迭代 1
迭代 2
迭代 3
迭代 4
```

用于生成重复元素：

```gotemplate
<select name="year">
{{range $i := 10}}
  <option value="{{add 2020 $i}}">{{add 2020 $i}}</option>
{{end}}
</select>
```

### Range Else

当集合为空时执行替代内容：

```gotemplate
{{range .Items}}
  <li>{{.}}</li>
{{else}}
  <li>暂无可用项目</li>
{{end}}
```

### 嵌套 Range

```gotemplate
{{range .Categories}}
  <h2>{{.Name}}</h2>
  <ul>
  {{range .Items}}
    <li>{{.}}</li>
  {{end}}
  </ul>
{{end}}
```

### 跳出 Range

使用 `break` 提前退出循环：

```gotemplate
{{range .Items}}
  {{if eq . "stop"}}
    {{break}}
  {{end}}
  {{.}}
{{end}}
```

### 跳过迭代

使用 `continue` 跳到下一次迭代：

```gotemplate
{{range .Items}}
  {{if eq . "skip"}}
    {{continue}}
  {{end}}
  {{.}}
{{end}}
```

### Break 和 Continue 示例

```gotemplate
{{range $i, $item := .Items}}
  {{if gt $i 10}}
    {{break}}  <!-- 10 个项目后停止 -->
  {{end}}
  {{if eq $item "hidden"}}
    {{continue}}  <!-- 跳过隐藏项目 -->
  {{end}}
  <div>{{$item}}</div>
{{end}}
```

## With 动作

更改点号上下文以简化对嵌套数据的访问。

### 基本 With

```gotemplate
{{with .User}}
  姓名: {{.Name}}
  年龄: {{.Age}}
  邮箱: {{.Email}}
{{end}}
```

等价于：

```gotemplate
{{if .User}}
  姓名: {{.User.Name}}
  年龄: {{.User.Age}}
  邮箱: {{.User.Email}}
{{end}}
```

### With/Else

当值为假时执行 else 块：

```gotemplate
{{with .User}}
  <div class="user">
    <h3>{{.Name}}</h3>
    <p>{{.Email}}</p>
  </div>
{{else}}
  <p>没有用户登录</p>
{{end}}
```

### 嵌套 With

```gotemplate
{{with .User}}
  {{with .Address}}
    {{.Street}}, {{.City}}
  {{end}}
{{end}}
```

### Range 中使用 With

```gotemplate
{{range .Users}}
  {{with .Profile}}
    {{.Name}} - {{.Bio}}
  {{end}}
{{end}}
```

## 组合控制流

实际模板通常组合多种结构。

### 示例 1：筛选列表

```gotemplate
<h2>活跃用户</h2>
<ul>
{{range .Users}}
  {{if .IsActive}}
    <li>{{.Name}} ({{.Age}})</li>
  {{end}}
{{end}}
</ul>
```

### 示例 2：分组显示

```gotemplate
{{range $category, $items := .GroupedItems}}
  <h3>{{$category}}</h3>
  {{if $items}}
    <ul>
    {{range $items}}
      <li>{{.}}</li>
    {{end}}
    </ul>
  {{else}}
    <p>此分类中没有项目</p>
  {{end}}
{{end}}
```

### 示例 3：分页列表

```gotemplate
{{range $i, $item := .Items}}
  {{if and (gte $i .StartIndex) (lt $i .EndIndex)}}
    <div class="item">{{$item}}</div>
  {{end}}
{{end}}

{{if gt (len .Items) .EndIndex}}
  <a href="?page={{add .Page 1}}">下一页</a>
{{end}}
```

### 示例 4：带验证的表单

```gotemplate
<form>
  {{with .User}}
    <div class="form-group">
      <label>姓名:</label>
      <input type="text" name="name" value="{{.Name}}" />
      {{if .NameError}}
        <span class="error">{{.NameError}}</span>
      {{end}}
    </div>
    
    <div class="form-group">
      <label>邮箱:</label>
      <input type="email" name="email" value="{{.Email}}" />
      {{if .EmailError}}
        <span class="error">{{.EmailError}}</span>
      {{end}}
    </div>
  {{end}}
  
  <button type="submit">保存</button>
</form>
```

## 常见模式

### 模式 1：默认值

```gotemplate
{{if .Title}}{{.Title}}{{else}}默认标题{{end}}
```

或使用管道：

```gotemplate
{{.Title | default "默认标题"}}
```

### 模式 2：交替样式

```gotemplate
{{range $i, $item := .Items}}
  <div class="{{if mod $i 2 | eq 0}}even{{else}}odd{{end}}">
    {{$item}}
  </div>
{{end}}
```

### 模式 3：逗号分隔列表

```gotemplate
{{range $i, $item := .Items}}
  {{if gt $i 0}}, {{end}}{{$item}}
{{end}}
```

输出：`item1, item2, item3`

### 模式 4：首/末项

```gotemplate
{{range $i, $item := .Items}}
  {{if eq $i 0}}第一个: {{end}}
  {{$item}}
  {{if eq $i (sub (len $.Items) 1)}}（最后一个）{{end}}
{{end}}
```

注意：在 range 内部使用 `$` 访问根上下文。

### 模式 5：条件包装

```gotemplate
{{if .ShowContainer}}
  <div class="container">
{{end}}

内容在这里

{{if .ShowContainer}}
  </div>
{{end}}
```

### 模式 6：状态徽章

```gotemplate
{{if eq .Status "active"}}
  <span class="badge badge-success">活跃</span>
{{else if eq .Status "pending"}}
  <span class="badge badge-warning">待处理</span>
{{else}}
  <span class="badge badge-danger">不活跃</span>
{{end}}
```

### 模式 7：空状态

```gotemplate
{{if .Items}}
  <table>
  {{range .Items}}
    <tr><td>{{.}}</td></tr>
  {{end}}
  </table>
{{else}}
  <div class="empty-state">
    <p>未找到项目</p>
    <a href="/create">创建一个</a>
  </div>
{{end}}
```

## 最佳实践

### 1. 保持逻辑简单

将复杂逻辑移到 Java 代码中。

❌ **不好：**
```gotemplate
{{if and (or (and .A .B) (and .C .D)) (not (or .E .F))}}...{{end}}
```

✅ **好：**
```java
data.put("ShouldDisplay", calculateCondition());
```
```gotemplate
{{if .ShouldDisplay}}...{{end}}
```

### 2. 避免深度嵌套

为了可读性，保持嵌套层级较浅。

❌ **不好：**
```gotemplate
{{if .A}}
  {{if .B}}
    {{if .C}}
      {{if .D}}
        ...
      {{end}}
    {{end}}
  {{end}}
{{end}}
```

✅ **好：**
```java
data.put("IsValid", a && b && c && d);
```
```gotemplate
{{if .IsValid}}...{{end}}
```

### 3. 处理空集合

始终考虑空状态。

```gotemplate
{{if .Items}}
  {{range .Items}}...{{end}}
{{else}}
  <p>无项目</p>
{{end}}
```

### 4. 使用有意义的变量名

```gotemplate
{{range $user := .Users}}
  {{$user.Name}}
{{end}}
```

而不是：

```gotemplate
{{range $x := .Users}}
  {{$x.Name}}
{{end}}
```

### 5. 为复杂逻辑添加注释

```gotemplate
{{/* 仅显示未过期且活跃的高级用户 */}}
{{range .Users}}
  {{if and .IsActive .IsPremium (not .IsExpired)}}
    {{.Name}}
  {{end}}
{{end}}
```

## 性能提示

### 1. 减少循环中的条件判断

尽可能在 Java 中预先筛选数据。

❌ **不好：**
```gotemplate
{{range .AllItems}}
  {{if .IsActive}}
    {{.}}
  {{end}}
{{end}}
```

✅ **好：**
```java
data.put("ActiveItems", items.stream()
    .filter(Item::isActive)
    .collect(Collectors.toList()));
```
```gotemplate
{{range .ActiveItems}}{{.}}{{end}}
```

### 2. 避免重复检查

❌ **不好：**
```gotemplate
{{if .User}}
  {{if .User.Name}}
    {{.User.Name}}
  {{end}}
{{end}}
```

✅ **好：**
```gotemplate
{{with .User}}
  {{if .Name}}{{.Name}}{{end}}
{{end}}
```

### 3. 尽早使用 Break

尽快退出循环：

```gotemplate
{{range .Items}}
  {{if eq . "target"}}
    找到了!
    {{break}}
  {{end}}
{{end}}
```

## 问题排查

### 问题：条件始终为假

**问题：** 条件永远不会求值为 true。

**解决方案：** 
1. 检查真值判断规则（0、""、null 为 false）
2. 验证数据是否正确传递
3. 使用 `typeof` 检查值类型

```gotemplate
值: {{.Value}}, 类型: {{typeof .Value}}
```

### 问题：Range 不迭代

**问题：** Range 块不执行。

**解决方案：**
1. 检查集合是否为空或 null
2. 验证集合类型（必须是数组、列表、map 或整数）

```gotemplate
{{if .Items}}
  长度: {{len .Items}}
  {{range .Items}}...{{end}}
{{else}}
  无项目或 items 为 null
{{end}}
```

### 问题：Range 中上下文错误

**问题：** 在 range 内无法访问根数据。

**解决方案：** 使用 `$` 访问根上下文：

```gotemplate
{{range .Items}}
  项目: {{.}}, 总计: {{len $.Items}}
{{end}}
```

### 问题：无限循环

**问题：** 模板执行挂起。

**解决方案：** 
1. 检查 break/continue 中的逻辑错误
2. 验证 range 集合是有限的
3. 查找递归模板调用

## 完整示例

```gotemplate
{{/* 用户仪表盘模板 */}}
<html>
<head><title>{{.Title}}</title></head>
<body>
  <h1>欢迎, {{.User.Name}}</h1>
  
  {{/* 通知 */}}
  {{if .Notifications}}
    <div class="notifications">
      <h2>通知 ({{len .Notifications}})</h2>
      {{range $i, $note := .Notifications}}
        {{if lt $i 5}}  {{/* 仅显示前5条 */}}
          <div class="notification {{if not $note.Read}}unread{{end}}">
            {{$note.Message}}
            {{if $note.Link}}
              <a href="{{$note.Link}}">查看</a>
            {{end}}
          </div>
        {{end}}
      {{end}}
      {{if gt (len .Notifications) 5}}
        <a href="/notifications">查看全部</a>
      {{end}}
    </div>
  {{else}}
    <p>没有新通知</p>
  {{end}}
  
  {{/* 最近活动 */}}
  <h2>最近活动</h2>
  {{with .Activity}}
    {{if .}}
      <ul>
      {{range .}}
        <li>{{.Description}} ({{.Timestamp}})</li>
      {{end}}
      </ul>
    {{else}}
      <p>没有最近活动</p>
    {{end}}
  {{end}}
  
  {{/* 快速统计 */}}
  <div class="stats">
    <div>订单: {{.Stats.OrderCount}}</div>
    <div>收入: ${{printf "%.2f" .Stats.Revenue}}</div>
    <div>评分: {{.Stats.Rating}}/5</div>
  </div>
</body>
</html>
```

---

**下一步：**
- 📚 了解[模板集](template-sets.md)以创建可复用组件
- 🔧 探索[函数](functions.md)以进行数据操作
- 📊 理解[数据模型](data-models.md)以使用 Java 对象
