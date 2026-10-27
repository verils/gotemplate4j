# gotemplate4j AI 代理指南

## 项目概览

- 本项目是 Go 模板引擎的 Java 实现。
- 项目基线为 Java 11，不引入运行时外部依赖。
- 兼容性优先于性能；已知的 Go 行为差异必须在用户文档中明确记录。

### 文件结构

- 主代码：`src/main/java/io/github/verils/gotemplate/`
- 测试代码：`src/test/java/io/github/verils/gotemplate/`
- 模板资源：`src/test/resources/*.tmpl`
- 核心组件：词法分析器、解析器、执行器和 AST 节点

## 语言与文档规则

- 中文是项目的主要开发和审阅语言。
- `README.md`、`AGENTS.md` 和 `CONTRIBUTING.md` 以中文编写；英文入口分别使用
  `README_en.md` 和 `CONTRIBUTING_en.md`。
- 用户文档保留 `docs/zh/` 和 `docs/en/` 两套完整版本，中文入口为默认。
- 新增或修改代码时，所触及的注释和 JavaDoc 必须使用中文。不要为了统一语言而批量
  改写未涉及的旧注释。
- 标识符、公共 API 名称、代码字面量、异常文本、注解、许可证原文和技术标签保持原样。
- 不要把模板语法关键字、函数名或 Java 类型翻译成中文。
- 修改中文主文档时，必须同步更新对应的英文文档，并保持章节、版本号、命令和示例一致。
- 变更日志使用 `CHANGELOG.md`（中文主版）和 `CHANGELOG_en.md`（英文版）；两个文件
  必须记录相同的版本、日期和变更范围。

## 开发环境

- Java 版本：11（项目基线；编译、测试和覆盖率验证均使用 JDK 11）
- 构建工具：Maven，必须使用 `./mvnw` 包装器，不要直接调用 `mvn`
- 禁止引入额外运行时依赖，只使用 Vanilla Java
- 使用 `./mvnw` 可避免本机 `JAVA_HOME` 配置影响构建

### Java 版本管理

**重要**：项目要求使用 Java 11 进行编译和权威测试/覆盖率验证。如果默认 Java 版本不是
11：

1. **检查当前 Java 版本：**
   ```bash
   java -version
   ```

2. **查找可用的 Java 11 安装：**
   - Windows：检查 `C:\Program Files\Eclipse Adoptium\` 或 `C:\Program Files\Java\`
   - 目录名通常类似 `jdk-11.0.xxx.x-hotspot`

3. **仅为当前项目临时设置 `JAVA_HOME`：**
   ```bash
   # Windows PowerShell（请替换为实际安装路径）
   $env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-11.0.xxx.x-hotspot"
   $env:PATH="$env:JAVA_HOME\bin;$env:PATH"
   ```

   ```bat
   rem Windows CMD（请替换为实际安装路径）
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.xxx.x-hotspot
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```

   ```bash
   # Linux/macOS
   export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

4. **确认 Java 11 已生效：**
   ```bash
   java -version
   # 应显示：openjdk version "11.x.x"
   ```

5. **可选：配置 Maven 工具链**
   - 多 JDK 环境可创建 `.mvn/toolchains.xml`，明确指定 JDK 11。
   - JaCoCo 的权威覆盖率结果必须在 Java 11 下生成，较新的 JDK 不能替代这一要求。

## 开发原则

### 核心原则

- 每个行为变更都必须有聚焦测试。
- 每个已知的 Go 行为差异都必须记录在中英文用户文档中。
- 兼容性与性能冲突时，优先保证兼容性。
- 不引入外部依赖，只使用 Vanilla Java。
- 尽可能保持向后兼容。

### 代码规范

- 遵循标准 Java 命名约定：变量和方法使用 `camelCase`，类使用 `PascalCase`。
- 所有文件使用 UTF-8 编码。
- 方法应简洁并遵循单一职责。
- 公共 API 必须提供 JavaDoc。
- 变量名应清晰表达用途。
- 新增或修改的注释与 JavaDoc 使用中文；代码示例中的说明性注释也应使用中文。

### 质量标准

- 尽可能保持向后兼容。
- 遵循现有的 `TemplateException` 异常体系。
- 在文档中保留实验性功能的警告。
- 未经明确要求，不修改 `LICENSE` 或核心项目元数据。

## 测试要求

- 所有新功能都必须包含单元测试。
- 使用 `./mvnw test` 运行测试。
- 测试位于 `src/test/java/io/github/verils/gotemplate/`。
- 使用 JUnit 5（Jupiter）。
- 遵循现有测试模式和结构；详细规则见
  [CONTRIBUTING.md](./CONTRIBUTING.md#测试放置位置)。
- 所有现有测试和新增测试都必须通过，不允许任何测试失败。
- 指令覆盖率不得低于 90%，分支覆盖率不得低于 85%。
- 不要无必要地创建新测试类；优先扩展最相关的现有测试。

### 覆盖率验证

项目使用 JaCoCo 检查并自动执行覆盖率门槛：

1. **运行测试并生成报告：**
   ```bash
   ./mvnw clean test jacoco:report
   ```

2. **执行完整构建和覆盖率检查：**
   ```bash
   # 本地无 GPG 密钥时跳过签名
   ./mvnw clean verify "-Dgpg.skip=true"
   ```

3. **查看覆盖率报告：**
   - HTML：`target/site/jacoco/index.html`
   - XML：`target/site/jacoco/jacoco.xml`
   - CSV：`target/site/jacoco/jacoco.csv`

4. **覆盖率门槛：**
   - 指令覆盖率：≥ 90%
   - 分支覆盖率：≥ 85%
   - 未达到门槛时构建失败

   模板引擎包含复杂的解析分支、安全敏感路径和大量 AST 节点，因此需要高于普通应用的
   覆盖率和更充分的错误路径验证。

5. **改进覆盖率：**
   - 优先处理 CSV 报告中分支覆盖率较低的类。
   - 为边界条件和异常路径增加测试。
   - 使用 HTML 报告定位未覆盖代码（红色标记）。

## 构建与部署

- 编译：`./mvnw compile`
- 运行测试：`./mvnw test`
- 生成覆盖率报告：`./mvnw test jacoco:report`
- 完整验证：`./mvnw verify "-Dgpg.skip=true"`
- 打包：`./mvnw package`
- 带签名完整构建：`./mvnw verify`
- 部署到 Nexus：`./mvnw deploy`
- 清理构建：`./mvnw clean`

**注意**：本地没有 GPG 密钥时，应添加 `"-Dgpg.skip=true"` 跳过制品签名。

## 工作流规则

### 提交策略

- **禁止自动提交。** 必须由人工审阅并批准暂存区差异后才能创建提交。
- Conventional Commits 的类型和可选 scope 使用英文，例如 `feat`、`fix`、`docs`、
  `test`、`refactor`、`chore`。
- 提交主题和正文使用中文。
- 每条提交都必须包含以下 trailer：
  ```
  Co-Authored-By: <name> <email>
  Signed-off-by: <name> <email>
  ```
- 使用维护者确认或 Git 中已配置的身份，不得编造占位身份。
- 提交正文应说明修改内容和原因。

示例：

```
docs(项目): 将中文设为默认开发语言

统一根文档和贡献指南的语言入口，并保留同步维护的英文版本。
```

### 任务管理

- 超过 200 行的修改必须先制定详细计划。
- 将复杂任务拆分为可独立验证的小步骤。
- 每一步验证通过后再继续下一步。
- 优先参考现有实现和测试模式。

## 文档职责

- **README.md / README_en.md**：面向用户的项目概览、安装、基本用法和 API 文档入口。
- **AGENTS.md**：AI 代理的开发和维护技术规范。
- **CONTRIBUTING.md / CONTRIBUTING_en.md**：面向贡献者的流程、边界和审查标准。
- **PLAN.md**：开发路线、优先级、任务拆分和当前状态，使用中文维护。
- **CHANGELOG.md / CHANGELOG_en.md**：已发布版本和未发布变更的中英文记录。

### 文档分离原则

- README 只保留稳定、与具体版本无关的入门信息。
- CHANGELOG 记录所有版本相关变更和发布说明。
- 不要在 README 中新增“vX.Y.Z 新功能”章节或版本专属功能列表。
- PLAN 跟踪未完成工作，不重复已完成版本的详细说明。

### 文档创建政策

- 除非用户明确要求，不创建新的文档文件。
- 优先更新现有文档。
- 遵循现有文档结构和命名约定。

### 文档同步

- **关键要求**：修改 `README.md` 时同步修改 `README_en.md`。
- 修改 `CONTRIBUTING.md` 时同步修改 `CONTRIBUTING_en.md`。
- 修改 `CHANGELOG.md` 时同步修改 `CHANGELOG_en.md`。
- 修改 `docs/zh/` 中的页面时，同步更新 `docs/en/` 中的对应页面；反之亦然。
- 所有语言版本中的代码示例、版本号、命令、链接和功能描述必须一致。
- 如果无法完成特定翻译，必须明确标注并交由人工处理，不能静默留下不同步内容。

## 交接协议

结束会话前应：

1. 总结已完成的修改。
2. 列出剩余 TODO 或未完成任务。
3. 说明与标准模式的任何偏离。
4. 提供具体的验证命令和实际结果。

## 特别说明

- 本项目是实验性软件，应保留适当的生产使用警告。
- 复数支持尚不完整，修改相关内容时注意限制。
- 部分内置函数尚未完整实现。
- PipeNode 功能仍较弱，处理时需谨慎。
