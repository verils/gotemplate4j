# 变更日志

[English](./CHANGELOG_en.md) | **中文**

v0.11.0 (2026-10-27)

========

**兼容性、安全性与诊断修复** - 本版本使转义、比较、集合、格式化和模板加载函数以及
模板定义语义与 Go `text/template` 对齐；修复管道处理和模板真值判断，并增加更强的
诊断和回归覆盖。

### 🐛 缺陷修复

- `js` 现在会中和 `<`、`>`、`&` 和 `=`，防止可关闭脚本的载荷突破 JavaScript 字符串
  上下文。
- `js` 现在遵循 Go 的 `JSEscapeString`：保留可打印 Unicode，对 ASCII 控制字符和不可打印
  码点使用大写 Unicode 转义，并转义所有 JavaScript 敏感字符。
- `html` 现在使用 Go 的数字引号实体，并将 NUL 替换为 Unicode 替换字符。
- `urlquery` 现在遵循 Go 的 `QueryEscape`，包括 UTF-8 字节编码以及对 `*`、`~` 等字符的
  处理。
- `html`、`js` 和 `urlquery` 现在接受零个或多个参数，并按 Go 风格的空格规则连接结果。
  Java `null` 继续贡献空字符串。
- `default` 现在将管道值解释为待检查值，因此当 `.Value` 存在或为 null 时，
  `{{.Value | default "fallback"}}` 都能正常工作。
- 管道执行现在能区分上游值缺失与实际 `null` 结果。
- `if`、`with`、`and`、`or`、`not` 和 `default` 现在共享同一套真值规则，覆盖数组、集合、
  Map、字符序列、Optional 值和精确的数值零判断。
- 比较函数现在遵循 Go 的类型分组和参数个数规则。`eq` 将第一个值与后续任一候选值比较，
  而 `ne`、`lt`、`le`、`gt` 和 `ge` 必须恰好接收两个参数。
- 整数比较不再通过 `double` 转换而丢失精度。整数和小数字面量保持各自独立的比较分组。
- 不兼容类型以及直接比较数组、集合或 Map 现在会抛出清晰错误；聚合值应使用
  `deepEqual`。
- 带引号字符串现在会解码 Go 风格的简单转义、转义引号、八进制、`\x`、`\u` 和 `\U`
  序列。无效转义会报告源代码行号和列号。
- 字符常量现在会解码为单个 Unicode 码点，包括补充字符，而不是返回反斜杠值。
- Map 迭代不再通过 `TreeMap` 强制转换 Map，从而避免 null、混合类型、自定义和不可比较
  key 引发异常。
- Map key 排序现在要求所有 key 属于同一具体基础类型；其他 Map 保持快照迭代顺序，不进行
  部分排序或 `toString()` 排序。
- `index` 现在支持通过嵌套 Map、数组、列表、集合和字符串进行链式多维访问，并严格检查
  整数索引和清晰的越界错误。
- 嵌套 `index` Map 查找现在在每一层应用 `MissingKeyPolicy`，并在层之间解包 `Optional`
  值。
- `slice` 现在支持零到三个索引，校验全部边界，并拒绝负数、反向、越界和过量索引，而不是
  截断处理。
- `-1` 等有符号整数字面量现在会保留符号，不再在执行期间被解释为正数。
- `print` 现在使用 Go 的 `Sprint` 空格规则：仅当相邻操作数都不是字符串时插入空格。
  `println` 始终用空格分隔操作数。
- `printf` 现在支持 `%v`、`%T`、`%t`、`%q`、`%d`、`%s`、`%x`、`%X`、`%f`、常见标志、
  宽度、精度和 `%%`，且不再直接依赖未校验的 Java 格式字符串。
- 无效 `printf` 动词、格式错误、参数数量不匹配和不兼容值类型现在会产生清晰的执行错误。
- 字符串、原始字符串、URL 和动作中间出现的注释标记不再被误判为注释。只有动作开头的
  注释（包括可选 trim 标记位置）才会被识别。
- 模板定义现在在一次解析中遵循 Go 的注册规则：非空重名定义会被拒绝，而不是静默采用
  最后一个定义体；空定义不会替换已有内容。
- 重复检测同样覆盖 `block` 定义以及与根模板名称的冲突。
- 文件和 classpath 批量加载根模板时现在使用完整文件名，防止独立模板互相覆盖。
- `parseDirectory`、`parseGlob` 和 `parseClasspathResources` 现在在无匹配时失败，而不是
  返回空模板；结果使用稳定路径顺序，并拒绝重复文件名。
- Classpath 加载现在支持普通 classpath 目录和 JAR 条目，无需显式 JAR 目录条目。
- 新增静态便捷方法 `fromFile`、`fromClasspath`、`fromDirectory` 和
  `fromClasspathResources`；现有实例方法及其根名称语义保持不变。
- `option` 现在校验 `key=value` 语法并支持 `mapkeysorting=true|false`；不支持的 key 和值
  会抛出 `IllegalArgumentException`。
- `MissingKeyPolicy.ZERO` 现在明确记录其 Java 契约：无法确定 Map 元素类型时解析为
  `null`，并渲染为 `<no value>`。
- `deepEqual` 现在递归比较数组、Map、集合、列表和 Set，处理循环容器，并拒绝具体运行时
  类型不同的值。
- 复数字面量现在会在解析期间抛出清晰的 `TemplateParseException`，而不是静默渲染为零。
- 调用方提供的 InputStream 和 Reader 仍由调用方持有，解析过程不会关闭它们。
- 缺失命名模板现在会一致抛出 `TemplateNotFoundException`，包括来自 `executeTemplate`
  和 `{{template}}` 动作的查找。
- `Functions.BUILTIN` 现在是不可变注册表视图；每个模板的自定义函数仍可覆盖内置函数。
- 函数执行错误现在包含简短的参数类型摘要，但不暴露参数值。

### 🔧 构建

- 编译现在使用 `maven.compiler.release=11`，防止意外使用 Java 11 基线之后新增的 API。
- 文档、AGENTS、CONTRIBUTING 和 CI 工作流现在一致要求 JDK 11，并在 CI 中加入 Go 以运行
  模板差分测试。

### 🧪 测试

- 已使用 JDK 11 验证：999 个测试全部通过，无失败或跳过，指令覆盖率为 91.44%，
  分支覆盖率为 87.39%。
- 增加脚本关闭注入、控制字符、可打印与不可打印 Unicode、HTML 实体、URL 查询编码、
  零/多参数和 null 参数的回归覆盖。
- 增加直接/管道 `default` 组合矩阵、自定义覆盖行为，以及所有逻辑模板构造共享真值判断的
  覆盖。
- 增加精确整数、混合数值分组、NaN/Infinity、聚合值拒绝、枚举比较和比较参数个数覆盖。
- 增加字面量解码、原始字符串、Unicode、无效转义位置和字符码点覆盖。
- 增加混合数值类型、混合基础类型、null key、自定义对象、自定义 Comparable key 和嵌套
  Map 的 Map 排序回退覆盖。
- 增加嵌套索引、缺失 key 策略、Optional、严格索引类型、负数字面量和 index/slice 边界
  覆盖。
- 增加 Go 打印空格规则、核心 `printf` 动词、宽度、精度、标志、引号、数组、null、管道
  用法和无效输入的格式化覆盖。
- 增加引号字符串和原始字符串中的类注释文本、真实尾随注释、自定义注释分隔符和严格注释
  起始空白行为的词法与执行覆盖。
- 增加重复 `define`/`block` 名称、空定义和纯注释重定义、根名称冲突、仅定义根模板以及
  解析失败隔离的回归覆盖。
- 增加文件、目录、完整 glob、classpath、缺失资源、重复文件名和临时 JAR 覆盖，包括没有
  目录条目的 JAR。
- 增加 Go `text/template` 差分探针，覆盖核心控制流、集合、格式化、转义、Map 排序、
  模板定义和错误行为。
- 增加 option 校验、Map 排序选项、不可变注册表、调用方持有流、并发执行、递归/循环
  `deepEqual`、复数拒绝和安全错误摘要覆盖。

### 📚 文档

- 关闭剩余延后路线图决策：测试包重组、带参数方法调用、字节码模板编译、非 Go 继承语法和
  额外质量工具采用现在都是明确贡献边界，而不再是未启用待办。
- 修正中英文性能指南，说明解析一次后复用，且不再宣传不存在的 `Template.compile` 或
  `CompiledTemplate` API。
- 明确 `block` 和 `define` 是唯一支持的继承机制。
- 使中英文函数、安全、API 和兼容性文档与已实现的转义行为同步。
- 记录多维索引、零到三索引切片、严格边界和 Java 特定字符串/列表行为。
- 记录支持的 `printf` 子集、Java 类型/聚合表示、打印空格规则和显式格式化错误行为。
- 记录注释必须出现的位置，以及字符串内容中注释标记的处理方式。
- 在中英文指南中明确模板覆盖发生在不同解析调用或文件之间，而不是同一次调用中的重复定义
  之间。
- 修正 README、FAQ、API 参考、兼容性说明和示例代码，使其使用实际实例 API 和新的静态
  加载工厂。
- 根据真实内置注册表重写中英文函数指南，并清晰分离自定义函数示例。
- 对齐 `ZERO`、`mapkeysorting`、流所有权、缺失模板异常、线程安全、整数范围和不受支持
  复数字面量的文档。

v0.10.0 (2026-05-23)

========

**Java 11 基线版本** - 本版本将最低 Java 要求提升到 Java 11，保持公共 API 稳定，并聚焦
兼容性打磨、诊断和发布文档清理。

### ⚠️ 破坏性变更

- **现在要求 Java 11**：v0.9.x 是最后一个支持 Java 8 的发布线。
- **构建基线更新**：Maven 编译器 source 和 target 现在设置为 Java 11。

---

### ✨ 改进

- **列表切片支持**：内置 `slice` 函数现在除字符串和数组外还支持 Java `List`。
- **函数诊断**：内置 `index` 函数的运行时失败现在使用与其他函数相同的增强错误格式，
  包括参数数量和原始原因详情。
- **Java 11 清理**：在行为等价处，用 `String.isBlank()` 替换基于 trim 的空白文本检测。

---

### 🧪 测试

- 增加链式管道、基于管道的变量赋值和作为函数参数使用的括号管道执行兼容性覆盖。
- 增加嵌套模板覆盖，验证 `$` 重置为传给 `{{template "name" pipeline}}` 的上下文。
- 使用 JDK 11 和完整 Maven 测试套件验证本版本。

---

### 📚 文档

- 根据 Java 11 要求更新 README、翻译版 README 和入门文档。
- 精简开发计划中的过时待办，使 v0.10.0 规划保持可执行。
- 明确 v0.9.x 的 Java 8 支持边界。

---

### 📦 升级说明

- 使用 Java 8 的用户必须停留在 v0.9.x，或升级到 Java 11 及更高版本。
- 对现有 Java 11+ 用户不计划修改公共 API。
- 本地构建应继续使用 Maven Wrapper：`./mvnw`。

v0.9.1 (2026-05-23)

========

**关键缺陷修复和测试覆盖版本** - 本补丁版本修复综合测试中发现的高影响模板引擎问题，
扩展语法覆盖，并改进项目测试维护指南。

### 🐛 缺陷修复

- **修复词法分析器 trim 标记崩溃**：模板开头的 `{{-` 等左 trim 标记不再触发
  `StringIndexOutOfBoundsException`。
- **修复集合索引**：内置 `index` 函数现在除 Map、数组和字符串外还支持 Java `List` 和
  `Collection`。
- **修复括号管道链式调用**：`(pipeline).field` 等表达式现在可在综合模板测试覆盖的执行
  路径中工作。
- **修复原始字符串字面量处理**：原始字符串字面量保留转义序列，不再像解释型字符串一样
  处理。
- **修复整数 range 循环控制**：`{{break}}` 和 `{{continue}}` 现在可在整数 `range` 循环中
  正常工作。
- **验证变量重新赋值行为**：增加 `{{$var = value}}` 重新赋值兼容性覆盖。

---

### 🧪 测试

- 增加高风险 Go `text/template` 兼容行为的综合模板语法和执行覆盖。
- 重组词法分析器测试，并删除过时测试结构。
- 改进关键解析器、执行器、函数和 range 边界覆盖。

---

### 📚 文档

- 增加 v0.9.0 之后发现的关键问题缺陷跟踪文档。
- 更新 v0.9.1 缺陷修复版本的开发计划说明。
- 将单元测试代理技能整理为标准技能包结构。

---

### 📦 升级说明

- 与 v0.9.0 完全向后兼容。
- 修复关键运行时和兼容性缺陷，建议所有 v0.9.0 用户升级。
- v0.9.x 线继续支持 Java 8。从 v0.10.0 开始要求 Java 11+。

v0.9.0 (2026-05-20)

========

**开发者体验增强版本** - 本版本通过增强文件加载 API、Go 风格整数 range 迭代以及带拼写
建议的智能错误诊断，提高开发者效率。

### ✨ 新功能

- **增强文件加载 API**：增加完整的文件加载方法以灵活管理模板。
  - `parseFromClasspath(String)`：从 classpath 资源加载模板。
  - `parseFromResource(String)`：另一种 classpath 加载方法。
  - `parseFile(Path, Charset)`：加载单个文件并支持指定编码。
  - `parseDirectory(Path)`：从目录加载所有 `.tmpl` 文件。
  - `parseClasspathResources(String)`：使用模式匹配批量加载。
  - 所有方法默认使用 UTF-8，也可指定字符集。
  - 测试覆盖：`TemplateFileHelperTest` 中 18 个综合测试用例。

  示例：
  ```java
  // 从 classpath 加载
  Template tmpl = Template.parseFromClasspath("templates/email.tmpl");

  // 从目录加载
  Map<String, Template> templates = Template.parseDirectory(Paths.get("templates"));

  // 使用指定编码加载
  Template tmpl = Template.parseFile(Paths.get("template.tmpl"), StandardCharsets.UTF_8);
  ```

- **整数 range 支持**：增加 Go 风格整数 range 迭代。
  - `{{range $i := 5}}` 从 0 迭代到 4（与 Go 兼容）。
  - 同时支持整数字面量和来自数据的值。
  - 正确处理零和负数（不迭代）。
  - 支持单变量形式：`{{range $i := .Count}}`。
  - 完全兼容 Go `text/template` 规范。
  - 测试覆盖：`IntegerRangeTest` 中 8 个综合测试用例。

---

### 📈 改进

#### 增强错误诊断（重大）

- **智能字段名建议**：改进字段访问错误消息。
  - 访问不存在字段时显示可用字段。
  - 使用 Levenshtein 距离算法提供 “Did you mean?” 建议。
  - 从建议中过滤 Object 类方法（`hashCode`、`toString` 等）。
  - 在模糊匹配前执行不区分大小写的精确匹配检查。
  - 示例：`can't evaluate field User.FristName. Available fields: [age, firstName, getAge]. Did you mean 'firstName'?`
  - 测试覆盖：`EnhancedFieldErrorTest` 中 6 个测试用例。

- **智能 Map key 错误消息**：增强 Map key 访问错误。
  - key 未找到时列出可用 key。
  - 为 Map key 拼写错误提供修正建议。
  - 同时适用于字段路径访问和 `index` 函数。
  - 处理多种 key 类型（String、Integer 等）。
  - 示例：`missing map key 'FristName'. Available keys: [Age, FirstName, LastName]. Did you mean 'FirstName'?`
  - 测试覆盖：`EnhancedMapKeyErrorTest` 中 11 个测试用例。

- **综合函数错误诊断**：改进函数调用错误报告。
  - 解析期：遇到未定义函数时显示可用函数。
  - 运行期：函数执行失败时显示实际参数数量。
  - 为函数名集成拼写建议。
  - 列出所有可用的内置和自定义函数。
  - 示例（解析）：`undefined function: leng. Available functions: [and, call, len, ...]. Did you mean 'len'?`
  - 示例（运行）：`function 'divide' failed with 1 argument(s): Requires exactly 2 arguments`
  - 测试覆盖：`EnhancedFunctionErrorTest` 中 5 个测试用例。

- **统一解析器异常格式**：标准化所有解析器异常消息。
  - 一致格式：`<error-type>: <description> [in <context>]`
  - 删除错误消息中不必要的引号。
  - 删除冗余行/列信息（由异常对象提供）。
  - 标准化前缀：`unexpected`、`undefined`、`invalid`、`missing`。
  - 改进术语清晰度（例如 `unexpected dot after term`）。
  - 更新全部 823 个测试以匹配新消息格式。

#### 基础设施增强

- **ErrorUtils 工具类**：创建内部错误诊断工具。
  - 实现 Levenshtein 距离算法计算相似度。
  - 提供相似度评分和匹配函数。
  - 为拼写错误生成智能建议。
  - 综合测试覆盖（`ErrorUtilsTest` 中 9 个测试用例）。

- **增强 TemplateParseException**：增加程序化错误位置访问。
  - 为异常类增加行号和列号字段。
  - 调用方可以程序化访问错误位置详情。
  - 与现有构造函数保持向后兼容。

---

### 🧪 测试

- **823 个测试在 Java 8 上全部通过**（各阶段新增 74+ 个测试）。
- **代码覆盖率：约 91% 指令覆盖、约 89% 分支覆盖**（超过 90%/85% 门槛）。
- **无向后兼容性破坏**。
- **JMH 基准显示无性能回退**。
- `./mvnw clean verify "-Dgpg.skip=true"` 成功。

---

### 📦 升级说明

- **完全向后兼容**：无 API 修改或破坏性变更。
- **增强错误消息自动生效**：所有改进均为内部实现，无需配置。
- **文件加载 API 为新增能力**：现有模板解析方法保持不变。
- **最后一个支持 Java 8 的版本**：从 v0.10.0 开始要求 Java 11+。
- **建议所有用户升级**：开发期间尤其可受益于更好的错误消息。

v0.8.0 (2026-05-17)

========

**性能、安全与质量增强版本** - 本版本带来最高 +267% 的重大性能改进，通过
`@TemplateField` 注解提供显式字段名控制以增强安全性，并建立稳健的 JMH 基准测试基础。

### ✨ 新功能

- **@TemplateField 注解支持**：增加 `@TemplateField`，显式控制字段/方法名到模板名称的
  映射。
  - 注解字段或方法以指定确切模板标识符。
  - 查找优先级：`@TemplateField` > 精确匹配 > Go 风格首字母大写。
  - 支持私有字段访问，无需公共 getter。
  - 同名字段注解优先于方法注解。
  - 支持跨类层次结构（继承）。
  - 包含缓存机制以优化性能。
  - 测试覆盖：14 个覆盖全部场景的综合测试用例。

  示例：
  ```java
  public class User {
      @TemplateField("UserName")
      private String userName = "Alice";

      @TemplateField("FullName")
      public String getFullName() {
          return firstName + " " + lastName;
      }
  }
  // 模板：{{.UserName}}, {{.FullName}}
  ```

- **带完整路径上下文的增强错误消息**：改进字段链错误报告。
  - 某段失败时显示完整路径（例如 `nil pointer evaluating User.Address.City at 'City'`）。
  - 与现有错误格式保持向后兼容。
  - 为嵌套访问错误场景增加 7 个综合测试。
  - 所有错误路径现在提供清晰的调试信息。

---

### 📈 改进

#### 性能优化（重大）

- **ClassMetadata 统一缓存**：用统一元数据缓存替换三个独立缓存。
  - 修复 PropertyDescriptor 索引导致的性能回退（-51.5% → +267%）。
  - `beanAccessBenchmark`：467K → 1,714K ops/sec（**+267%**）。
  - `executeBenchmark`：987K → 1,790K ops/sec（**+81.3%**）。
  - 所有基准均有改进：parse（+45%）、range（+53.7%）、function（+23.2%）、
    map（+20.6%）。
  - 消除冗余反射扫描并启用快速失败优化。
  - 私有字段配合公共 getter 方法（保持正确封装）。

- **BeanInfo 缓存**：BeanInfo 自省的实例级缓存。
  - `beanAccessBenchmark`：提升 32.2%。
  - `executeBenchmark`：提升 20.5%。
  - 随 Template 生命周期自动 GC（无内存泄漏）。

- **PropertyDescriptor 索引**：带回退的名称索引查找。
  - 将 O(n) 线性搜索减少为 O(1) 索引查找。
  - 优雅处理不兼容的方法调用。
  - 为完整兼容性回退到线性搜索。

- **注解缓存内存泄漏修复**：从静态缓存改为实例级缓存。
  - 防止长期运行应用中的内存泄漏。
  - 共享 Template 生命周期以自动清理。

#### 安全增强

- **显式字段名控制**：`@TemplateField` 注解替代启发式属性名转换。
  - 避免意外方法调用和字段访问。
  - 精确控制模板可见成员。
  - 废弃 `toGoStylePropertyName` 中的复杂启发式。

- **输入校验强化**：完整的构造函数参数校验。
  - 校验模板名、分隔符和注释分隔符。
  - 为输入校验场景增加 15 个新测试。
  - 防止无效配置导致运行时错误。

#### 测试基础设施

- **JMH 基准迁移**：从简单 TemplateBenchmark 迁移到 JMH。
  - 配置 jmh-core 1.37 和 jmh-generator-annprocess 1.37。
  - 创建包含 6 个综合场景的 TemplateJmhBenchmark。
  - 为后续比较建立基准性能数字。
  - 使用方式：`./mvnw test-compile exec:java -Dexec.mainClass="io.github.verils.gotemplate.TemplateJmhBenchmark"`

- **Optional 深度链验证**：为嵌套 Optional 场景增加 6 个综合测试。
  - 验证多层解包（User → Optional<Address> → Optional<City>）。
  - 测试链中不同位置的空 Optional。
  - 验证三层 Optional 嵌套。

---

### 🧪 测试

- **778 个测试在 Java 8 上全部通过**（各阶段新增 75+ 个测试）。
- **代码覆盖率：约 92% 指令覆盖、约 89% 分支覆盖**（超过 90%/85% 门槛）。
- **无向后兼容性破坏**。
- **线程安全实现已验证**。
- `./mvnw clean verify "-Dgpg.skip=true"` 成功。

---

### 📦 升级说明

- **完全向后兼容**：无 API 修改或破坏性变更。
- **性能改进自动生效**：所有优化均为内部实现，无需配置。
- **@TemplateField 为可选功能**：现有模板无需注解即可继续工作。
- **提供 JMH 基准**：运行
  `./mvnw test-compile exec:java -Dexec.mainClass="io.github.verils.gotemplate.TemplateJmhBenchmark"`
  以测量性能。
- **建议所有用户升级**：高频模板执行场景尤其受益。

v0.7.0 (2026-05-11)

========

**文档与兼容性增强版本** - 本版本将 gotemplate4j 从可用库提升为生产可用且文档完善的
方案，并降低从 Go 模板迁移的阻力。

### 新功能

- **整数 range 支持**：支持在 range 表达式中迭代整数。
  - `{{range $i := 5}}` 从 0 迭代到 4（与 Go 兼容）。
  - 正确处理零和负数（不迭代）。
  - 同时支持整数字面量和来自数据的值。
  - 测试覆盖：8 个综合测试用例。

- **确定性输出的 Map key 排序**：增加可配置 Map key 排序以匹配 Go 模板行为。
  - 默认启用：`{{range $k, $v := .Map}}` 产生排序输出。
  - 支持 Comparable 类型（String、Integer 等）的自然排序。
  - 对非 Comparable 类型回退到 `toString()` 比较。
  - 需要插入顺序时可通过 `withMapKeySorting(false)` 禁用。
  - 完全兼容 Go `text/template` 规范。
  - 测试覆盖：11 个综合测试用例，包括嵌套 Map 和自定义 key 类型。

- **Block 动作增强**：完整支持 Go block 动作及正确覆盖语义。
  - `{{block "name" .}}Default content{{end}}` 定义并内联执行。
  - 后续 `{{define "name"}}...{{end}}` 可覆盖 block。
  - 支持嵌套 block 和复杂模板继承模式。
  - 相比 define+template 模式减少模板冗余。
  - 测试覆盖：9 个综合测试用例。

### 文档

- **完整文档结构**：创建包含 20+ 文档的完整文档中心。
  - 入门指南（安装、快速开始、基本概念）。
  - 用户指南（语法、数据模型、函数、控制流、模板集、错误处理）。
  - API 参考（Template、Function、Exception API）。
  - 高级主题（性能、安全、最佳实践）。
  - 真实场景示例（基础、Web、邮件、复杂场景）。
  - 包含 30+ 常见问题的 FAQ。

- **增强兼容性文档**：
  - 扩展 [Go 模板兼容性指南](./docs/zh/advanced/compatibility.md)，增加详细行为对比表。
  - 提供带 Go 与 Java 并排示例的完整[迁移指南](./docs/zh/advanced/migration.md)。
  - 增加常见迁移模式和测试策略。
  - 更新[控制流指南](./docs/zh/user-guide/control-flow.md)中的 Map 迭代顺序细节。

- **生产级示例**：
  - 覆盖全部核心功能的基础示例。
  - Web 模板模式（布局、表单、表格、分页、认证界面）。
  - 邮件模板（欢迎、密码重置、订单确认、新闻通讯）。
  - 复杂场景（多语言、报表、代码生成、发票、API 文档）。

### 改进

- 所有文档采用一致结构，便于未来静态站点生成。
- 验证并测试代码示例。
- 文档间交叉引用准确。
- 为未来 i18n 支持采用易翻译结构。
- 增强 Executor，在命令执行中直接处理 NumberNode 和 BoolNode。

### 测试

- 699 个测试在 Java 8 上全部通过。
- 代码覆盖率保持高于 90% 门槛。
- 无向后兼容性破坏。
- `./mvnw clean verify "-Dgpg.skip=true"` 成功。

v0.6.0 (2026-05-10)

========

**审计后 API 易用性与迁移版本** - 本版本基于 v0.5.0 兼容性审计，将高价值运行时行为
设为可配置，增加文件系统辅助方法，并提供性能基线。

### 新功能

- **可配置缺失 key 策略**：增加 `MissingKeyPolicy`，提供 `DEFAULT`、`ZERO` 和 `ERROR`
  模式。
  - 可通过 `Template.withMissingKeyPolicy(...)` 或 Go 风格
    `Template.option("missingkey=...")` 配置。
- **模板自省 API**：增加检查已解析模板集的方法：`name()`、`hasTemplate(...)`、
  `definedTemplates()`、`lookup(...)` 和 `templates()`。
- **文件系统辅助方法**：增加从磁盘加载模板的便捷方法：`parseFile(Path)`、
  `parseFiles(Path...)` 和 `parseGlob(Path, String)`。
- **性能基准**：增加轻量级工具 `TemplateBenchmark` 以监控解析和执行吞吐量。

### 文档

- 增加详细[兼容性指南](./docs/zh/advanced/compatibility.md)。
- 增加从 Go `text/template` 迁移的[迁移指南](./docs/zh/advanced/migration.md)。
- 增加聚焦的兼容性和 Java 差异 fixture 测试。

### 实现说明

- `DEFAULT` 策略保留 v0.5.0 行为：缺失 Map key 求值为 `null`，打印为空输出。
- 由于 Java 类型推断限制，`ZERO` 策略目前对缺失 Map key 回退为 `null`。
- `lookup(...)` 和 `templates()` 返回独立模板副本，防止意外修改原模板集。
- 文件辅助方法使用 UTF-8 和标准 `java.nio.file` API。

v0.5.0 (2026-05-06)

========

**兼容性审计版本** - 本版本完成 v0.5.0 对核心执行语义的 Go `text/template` 兼容性审计，
并记录有意的 Java 差异。

### 兼容性改进

- 增加高风险 Go 模板行为的聚焦兼容覆盖：
  - 空列表、数组、Map 和 null 值上的 `range ... else`。
  - `with ... else with`。
  - 省略管道数据的模板调用。
  - 变量声明、重新赋值以及分支/range 作用域。
  - 逻辑 `and` / `or` 短路求值。
  - 函数错误包装和 Writer `IOException` 传播。

- 改进执行语义：
  - null range 值现在执行 `else` 分支，不再抛出 `NullPointerException`。
  - `and` 和 `or` 现在会在求值后续参数前短路。
  - 模板函数的运行时失败会包装为 `TemplateExecutionException`。
  - 可以在字段链中访问公共无参 Java 方法。
  - 枚举 `name` 和 `ordinal` 访问不再依赖 JDK 私有字段反射。

### Java 兼容性决策

- Java getter 和公共字段映射为 Go 风格导出字段访问。
- Java `Optional` 值自动解包。
- null 和缺失值判定为假，并打印为空输出。
- Java `Map` 迭代遵循 Map 实现顺序；引擎不排序 key（该版本行为）。
- `default`、`deepEqual`、`typeof` 和 `kindOf` 记录为 gotemplate4j 扩展。

### 延后的 Go API

以下 Go API 有意不包含在 v0.5.0 中：

- `Option("missingkey=default/zero/error")`
- `ParseFiles`、`ParseGlob` 和 `ParseFS`
- `Lookup`、`DefinedTemplates`、`Templates`、`Name` 及相关 `New` 等模板自省 API
- 对 channel、Go iterator 或整数序列执行 `range`
- 模板中带参数的通用 Java 方法调用
- 用于 `call` 的 Go 函数值字段或 Map 条目

### 测试

- 增加 `GoCompatibilityAuditTest`。
- 更新旧版 null range 行为覆盖。
- 总计 620+ 测试，零失败。
- Java 8 验证目标：`./mvnw verify "-Dgpg.skip=true"`。

v0.4.0 (2026-05-01)

========

**生产可用版本** - 本版本标志项目从实验状态过渡到生产可用状态。

### 新功能

- **完整内置函数**：全部 18+ Go 模板内置函数现已完整实现。
  - 比较运算符：`eq`、`ne`、`lt`、`le`、`gt`、`ge`
  - 逻辑运算符：`and`、`or`、`not`
  - 集合函数：`len`、`index`、`slice`
  - 工具函数：`call`、`html`、`js`、`urlquery`
  - 类型检查：`deepEqual`、`typeof`、`kindOf`

- **增强管道支持**：支持变量赋值的完整管道处理。
  - 管道中的变量声明：`{{$var := .Value | upper}}`
  - 多阶段管道转换
  - 管道阶段之间正确的类型转换

- **改进错误诊断**：带上下文的增强错误消息。
  - 所有解析错误包含行号和列号
  - 显示错误位置的模板上下文片段
  - 使用可视指针标出准确错误位置

### 改进

- 测试覆盖率提升至 82% 指令覆盖、79% 分支覆盖（JaCoCo 测量）。
- 为所有公共 API 类提供完整 JavaDoc。
- 增加面向开发者的 CONTRIBUTING.md。
- 项目状态从实验性更新为生产可用。

### 测试

- 为内置函数增加 74 个综合测试。
- 为管道变量支持增加 16 个测试。
- 增加错误上下文校验测试。
- 总计 260+ 测试，零失败。

v0.3.2 (2024-02-08)

========

- 支持新的 `Template` 实现，更接近 Golang 模板 API。`GoTemplate` 思路来自某些 Java
  模板引擎，但保留原始 Go 模板 API 风格更易理解。
- 修复解析定义块时的一些问题。

v0.3.1 (2023-11-30)

========

- 修复管道值处理。

v0.3.0 (2023-03-30)

========

- 重构内部组件，将其包名从 `runtime` 改为 `internal`。
- 改进词法分析器和解析器，支持复数与普通数，并完善测试。

v0.2.1 (2023-03-28)

========

功能

- 增加新的 Golang 风格 API。

修复一些执行器问题

- 正确打印数值。
- 正确处理 Map 对象。

========

v0.2.0 (2022-06-04)

========

修复一些问题并调整包结构。

v0.1.0 (2021-12-21)

========

创建词法分析器和解析器。
