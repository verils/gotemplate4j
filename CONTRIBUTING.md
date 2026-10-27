# 为 gotemplate4j 贡献

[English](./CONTRIBUTING_en.md) | **中文**

感谢你对 gotemplate4j 的关注！本文档说明贡献流程、开发规范和审查标准。

## 🎯 项目目标

gotemplate4j 的目标是忠实实现 Go 的 `text/template` 包。项目目标包括：

1. **兼容性**：尽可能贴近 Go 模板语义。
2. **性能**：为 Java 应用提供高效的模板执行能力。
3. **简洁性**：保持代码库清晰、可维护。
4. **质量**：保持高于 JaCoCo 门槛的测试覆盖率。

## 🌐 语言规则

中文是主要开发和审阅语言：

- `README.md`、`AGENTS.md` 和本指南使用中文。
- 英文入口使用 `README_en.md`、`CONTRIBUTING_en.md` 和 `docs/en/`。
- 用户文档同时维护 `docs/zh/` 与 `docs/en/`，中文入口为默认。
- 新增或修改代码时，所触及的注释和 JavaDoc 使用中文；不要批量改写未涉及的旧注释。
- 标识符、API 名称、代码字面量、异常文本、注解和许可证原文保持原样。

## 📋 如何贡献

### 报告问题

创建 Issue 前：

- 检查是否已有重复问题。
- 提供最小可复现示例。
- 说明预期行为与实际行为。
- 注明 Java 版本和运行环境。

### 提交 Pull Request

1. **Fork** 仓库。
2. **创建分支**：
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/issue-description
   ```
3. **按照编码规范完成修改**。
4. **为新功能添加测试**。
5. **运行全部测试**：
   ```bash
   ./mvnw clean test
   ```
6. **检查覆盖率**：
   ```bash
   ./mvnw test jacoco:report
   ```
7. **使用清晰的提交信息提交修改**（参见提交信息规范）。
8. **推送**到你的 Fork。
9. **创建 Pull Request**，并提供清晰的变更说明。

## 💻 开发环境

详细前置条件和 Java 版本管理见
[README.md](./README.md#环境要求)。

### 快速设置

```bash
# 克隆你的 Fork
git clone https://github.com/YOUR_USERNAME/gotemplate4j.git
cd gotemplate4j

# 构建并运行测试
./mvnw clean install

# 仅运行测试
./mvnw test

# 生成覆盖率报告
./mvnw test jacoco:report
# 在浏览器中打开 target/site/jacoco/index.html
```

项目要求使用 Java 11 和 Maven Wrapper。不要直接调用 `mvn`。

## 📝 编码规范

### 代码风格

- 遵循标准 Java 命名约定：
  - 类：`PascalCase`
  - 方法/变量：`camelCase`
  - 常量：`UPPER_SNAKE_CASE`
- 所有文件使用 UTF-8 编码。
- 方法应简洁，优先控制在 50 行以内。
- 公共 API 必须提供 JavaDoc。
- 变量名应清晰表达用途。
- 新增或修改的注释和 JavaDoc 使用中文。

### 测试要求

- **所有新功能都必须包含单元测试。**
- **代码覆盖率必须达到 JaCoCo 门槛。**
- 测试边界条件和错误场景。
- 使用 JUnit 5（Jupiter）。
- 遵循现有测试模式。

### 测试放置位置

项目当前将所有自动化测试放在 `src/test/java`，通过 Maven Surefire 和
`./mvnw test` 执行。目前没有独立的 `src/integrationTest` 源集，也没有配置 Maven
Failsafe。

根据测试意图放置测试：

- `src/test/java/io/github/verils/gotemplate/internal/*Test.java`：词法分析器、解析器、
  AST、Token 和内部工具行为。
- `Functions*Test.java`：内置模板函数行为。
- `Template*Test.java`：公共 `Template` API、模板集、克隆、自省和面向用户的模板操作。
- `ExecutorTest.java` 或聚焦的顶层功能测试：不适合放入更具体测试类的执行语义。
- `GoCompatibility*Test.java`：应与 Go `text/template` 语义一致的行为。
- `JavaDeviationFixtureTest.java` 或聚焦功能测试：有意的 Java 特定行为或已记录的 Go 差异。
- `RangeIndexTest`、`NullSafetyTest`、`OptionalSupportTest` 等功能测试：需要独立可读测试类
  的聚焦执行行为。

当现有测试类更合适时，优先扩展现有测试。只有行为会使现有类过于宽泛或难以浏览时，
才创建新的 `*Test.java` 类。

除非测试确实需要独立生命周期（例如耗时的兼容性套件、外部进程、大型文件系统 fixture
或依赖环境的设置），否则不要添加 `*IT.java`、`src/integrationTest` 或 Maven Failsafe。

示例测试结构：

```java
@Test
void testYourFeature() throws TemplateException {
    // 准备
    Template template = new Template("test");
    template.parse("{{your template}}");

    // 执行
    StringWriter writer = new StringWriter();
    template.execute(writer, testData);

    // 断言
    assertEquals("expected output", writer.toString());
}
```

### 提交信息规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**类型：**

- `feat`：新功能
- `fix`：缺陷修复
- `docs`：文档修改
- `style`：代码风格修改（格式化，不改变逻辑）
- `refactor`：代码重构
- `test`：新增或更新测试
- `chore`：构建或工具修改

类型和可选 scope 使用英文；主题和正文使用中文。

**示例：**

```text
feat(functions): 实现 deepEqual 内置函数

fix(parser): 正确处理嵌套变量引用

docs(readme): 将中文设为默认入口

test(parser): 增加错误诊断测试
```

**重要：** 每条提交都必须包含以下 trailer：

```
Co-Authored-By: <name> <email>
Signed-off-by: <name> <email>
```

提交前必须由人工审阅并批准暂存区差异。禁止自动提交。

## 🎯 贡献边界

当前工作见 [PLAN.md](./PLAN.md)。贡献应优先关注：

1. **Go 兼容性和正确性**
   - 为已记录行为增加聚焦回归测试。
   - 为兼容性变更增加 Go 差分覆盖。
   - 为不支持的语法和值提供清晰诊断。

2. **文档和测试质量**
   - JavaDoc 和示例必须与公共 API 一致。
   - 中英文用户文档必须同步，中文为默认入口。
   - 测试应覆盖行为，而不是实现细节。

3. **有证据的性能优化**
   - 修改热路径前先分析具体瓶颈。
   - 使用现有 JMH 基准复现改进。
   - 保持兼容性和错误行为不变。

### 模板引擎非目标

- **带参数的方法调用**：模板只调用公共无参方法。需要参数的操作应注册为
  `Function`。
- **非 Go 继承语法**：`block` 和 `define` 是唯一支持的继承机制，不计划增加
  `extends`、布局或其他增强继承语言。
- **字节码模板编译**：应复用并缓存已解析的 `Template`。不计划提供公共
  `Template.compile` API 或生成模板类。

### 质量工具决策

- 保持现有测试布局：`internal/*` 用于词法分析器、解析器、AST 和内部工具，公共包顶层
  使用聚焦功能测试。不计划进行全仓库包重组。
- JaCoCo、Go 差分测试套件和 JMH 是受支持的质量与性能检查。变异测试、属性测试和额外
  静态分析插件不在当前路线图中；只有在现有套件无法覆盖具体质量缺口时才单独提出。

## 🔍 代码审查流程

所有 Pull Request 都必须经过审查：

1. **自动检查（CI）：**
   - 所有测试通过。
   - 覆盖率高于 JaCoCo 门槛。
   - Java 11 构建成功。

2. **人工审查：**
   - 代码质量和风格。
   - 测试覆盖是否充分。
   - 与 Go 模板的兼容性。
   - 性能影响。

3. **合并条件：**
   - 至少一名维护者批准。
   - 所有 CI 检查通过。
   - 破坏性变更必须提供迁移方案。

## 🚫 禁止事项

- ❌ 不要添加外部依赖；项目只使用 Vanilla Java。
- ❌ 未经讨论不要破坏向后兼容性。
- ❌ 不要提交没有测试的代码。
- ❌ 未经明确要求不要修改 `LICENSE` 或核心元数据。
- ❌ 不要添加偏离 Go 模板规范的非标准功能。

## 📊 质量标准

### 测试覆盖率

- **最低要求**：指令覆盖率 90%，分支覆盖率 85%。
- **执行方式**：覆盖率低于门槛时构建失败。
- **工具**：JaCoCo（在 `pom.xml` 中配置）。

### 性能

- 不允许出现明显性能回退。
- 修改关键路径时运行基准测试。
- 考虑内存分配影响。

### 兼容性

- 保持 Go 模板语义兼容。
- 记录所有有意差异。
- 为破坏性变更提供迁移指南。

## 🤝 获取帮助

- **问题**：发起 GitHub Discussion。
- **缺陷**：创建带复现步骤的 Issue。
- **功能**：先在 Issue 中讨论方案。
- **代码审查**：向维护者寻求指导。

## 📜 行为准则

- 尊重并包容所有参与者。
- 欢迎新贡献者并帮助其学习。
- 聚焦建设性反馈。
- 在讨论中保持善意。

## 🎉 贡献者认可

贡献者会在以下位置获得认可：

- `CHANGELOG.md` 和 `CHANGELOG_en.md`
- 发布说明
- README 贡献者章节（重要贡献）

## 📄 许可证

提交贡献即表示你同意贡献内容按项目的 MIT License 授权。

---

感谢你为 gotemplate4j 做出贡献！🚀

详细开发计划见 [PLAN.md](./PLAN.md)。
