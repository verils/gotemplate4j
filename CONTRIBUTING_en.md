# Contributing to gotemplate4j

**English** | [中文](./CONTRIBUTING.md)

Thank you for your interest in contributing to gotemplate4j! This document describes the contribution workflow, development standards, and review criteria.

## 🎯 Project Goals

gotemplate4j aims to be a faithful Java implementation of Go's `text/template` package. Our goals are:

1. **Compatibility**: Stay close to Go template semantics.
2. **Performance**: Provide efficient template execution for Java applications.
3. **Simplicity**: Keep the codebase clean and maintainable.
4. **Quality**: Maintain test coverage above the JaCoCo thresholds.

## 🌐 Language Policy

Chinese is the primary development and review language:

- `README.md`, `AGENTS.md`, and this guide's primary entry use Chinese.
- English entry points are `README_en.md`, `CONTRIBUTING_en.md`, and `docs/en/`.
- User documentation maintains both `docs/zh/` and `docs/en/`, with Chinese as the default.
- New or modified comments and Javadoc use Chinese. Do not batch-rewrite unrelated legacy comments.
- Identifiers, API names, code literals, exception text, annotations, and license text remain unchanged.

## 📋 How to Contribute

### Reporting Issues

Before creating an issue:

- Check existing issues to avoid duplicates.
- Provide a minimal reproducible example.
- Include expected and actual behavior.
- Specify your Java version and environment.

### Submitting Pull Requests

1. **Fork** the repository.
2. **Create a branch**:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```
3. **Make your changes** according to the coding guidelines.
4. **Add tests** for new functionality.
5. **Run all tests**:
   ```bash
   ./mvnw clean test
   ```
6. **Check coverage**:
   ```bash
   ./mvnw test jacoco:report
   ```
7. **Commit your changes** with a clear message (see the commit message guidelines).
8. **Push** to your fork.
9. **Submit a Pull Request** with a clear description.

## 💻 Development Setup

For prerequisites and Java version management, see
[README_en.md](./README_en.md#requirements).

### Quick Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/gotemplate4j.git
cd gotemplate4j

# Build and test
./mvnw clean install

# Run tests only
./mvnw test

# Generate a coverage report
./mvnw test jacoco:report
# Open target/site/jacoco/index.html in a browser
```

The project requires Java 11 and the Maven Wrapper. Do not invoke `mvn` directly.

## 📝 Coding Guidelines

### Code Style

- Follow standard Java naming conventions:
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- Use UTF-8 encoding for all files.
- Keep methods concise, preferably under 50 lines.
- Add Javadoc for public APIs.
- Use meaningful variable names.
- Use Chinese for new or modified comments and Javadoc.

### Testing Requirements

- **All new features must include unit tests.**
- **Code coverage must meet the JaCoCo thresholds.**
- Test edge cases and error conditions.
- Use JUnit 5 (Jupiter).
- Follow existing test patterns.

### Where to Put Tests

The project currently keeps all automated tests under `src/test/java` and runs them with Maven Surefire via `./mvnw test`. There is no separate `src/integrationTest` source set and no Maven Failsafe configuration.

Use the existing test structure to make the intent of a new test clear:

- `src/test/java/io/github/verils/gotemplate/internal/*Test.java`: lexer, parser, AST, token, and internal utility behavior.
- `Functions*Test.java`: built-in template function behavior.
- `Template*Test.java`: public `Template` API behavior, template sets, cloning, introspection, and user-facing template operations.
- `ExecutorTest.java` or a focused top-level feature test: execution semantics that do not fit a narrower existing class.
- `GoCompatibility*Test.java`: behavior that should match Go `text/template` semantics.
- `JavaDeviationFixtureTest.java` or a focused feature test: intentional Java-specific behavior or documented deviations from Go.
- Feature-specific tests such as `RangeIndexTest`, `NullSafetyTest`, or `OptionalSupportTest`: focused execution behavior that deserves its own readable test class.

Prefer extending an existing test class when it remains a good fit. Create a new focused `*Test.java` class only when the behavior would make an existing class too broad or hard to scan.

Do not add `*IT.java`, `src/integrationTest`, or Maven Failsafe configuration unless the test needs a genuinely separate lifecycle, such as slow compatibility suites, external processes, large filesystem fixtures, or environment-dependent setup.

Example test structure:

```java
@Test
void testYourFeature() throws TemplateException {
    // Arrange
    Template template = new Template("test");
    template.parse("{{your template}}");

    // Act
    StringWriter writer = new StringWriter();
    template.execute(writer, testData);

    // Assert
    assertEquals("expected output", writer.toString());
}
```

### Commit Message Guidelines

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types:**

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation change
- `style`: Code style change (formatting with no logic change)
- `refactor`: Code refactoring
- `test`: New or updated tests
- `chore`: Build or tooling change

Keep the type and optional scope in English. Write the subject and body in Chinese.

**Examples:**

```text
feat(functions): 实现 deepEqual 内置函数

fix(parser): 正确处理嵌套变量引用

docs(readme): 将中文设为默认入口

test(parser): 增加错误诊断测试
```

**Important:** Every commit must include both trailers:

```
Co-Authored-By: <name> <email>
Signed-off-by: <name> <email>
```

A human reviewer must review and approve the staged diff before a commit is created. Never commit automatically.

## 🎯 Contribution Boundaries

See [PLAN.md](./PLAN.md) for active work. Contributions should prioritize:

1. **Go compatibility and correctness**
   - Focused regression tests for documented behavior.
   - Go differential coverage for compatibility changes.
   - Clear diagnostics for unsupported syntax and values.

2. **Documentation and test quality**
   - Javadoc and examples that match the public API.
   - Synchronized Chinese and English user documentation, with Chinese as the default entry.
   - Tests that cover behavior rather than implementation details.

3. **Evidence-based performance work**
   - Profile a concrete bottleneck before changing hot paths.
   - Reproduce improvements with the existing JMH benchmark.
   - Preserve compatibility and error behavior.

### Template Engine Non-Goals

- **Method calls with arguments**: Templates invoke public no-argument methods only. Register a `Function` for operations that need parameters.
- **Non-Go inheritance syntax**: `block` and `define` are the only supported inheritance mechanisms. A separate `extends`, layout, or enhanced inheritance language is not planned.
- **Bytecode template compilation**: Reuse and cache a parsed `Template` where appropriate. A public `Template.compile` API or generated template classes are not planned.

### Quality Tooling Decisions

- Keep the documented test layout: `internal/*` for lexer, parser, AST, and internal utilities, with focused feature tests at the public package level. A repository-wide package reorganization is not planned.
- JaCoCo, the Go differential suite, and JMH are the supported quality and performance checks. Mutation testing, property testing, and additional static analysis plugins are not part of the roadmap; propose them separately only when a concrete quality gap cannot be covered by the current suite.

## 🔍 Code Review Process

All pull requests undergo review:

1. **Automated Checks (CI):**
   - All tests must pass.
   - Coverage must remain above the JaCoCo thresholds.
   - The build must succeed on Java 11.

2. **Manual Review:**
   - Code quality and style.
   - Test coverage adequacy.
   - Compatibility with Go templates.
   - Performance implications.

3. **Merge Criteria:**
   - At least one maintainer approval.
   - All CI checks pass.
   - Breaking changes include a migration plan.

## 🚫 What Not to Do

- ❌ Do not add external dependencies; the project uses vanilla Java only.
- ❌ Do not break backward compatibility without discussion.
- ❌ Do not submit code without tests.
- ❌ Do not modify `LICENSE` or core metadata without an explicit request.
- ❌ Do not add non-standard features that deviate from the Go template specification.

## 📊 Quality Standards

### Test Coverage

- **Minimum**: 90% instruction coverage and 85% branch coverage.
- **Enforcement**: The build fails if coverage drops below the thresholds.
- **Tool**: JaCoCo, configured in `pom.xml`.

### Performance

- No significant performance regressions.
- Benchmark critical paths when making changes.
- Consider memory allocation impact.

### Compatibility

- Maintain Go template semantic compatibility.
- Document intentional deviations.
- Provide migration guides for breaking changes.

## 🤝 Getting Help

- **Questions**: Start a GitHub Discussion.
- **Bugs**: Create an issue with reproduction steps.
- **Features**: Discuss the proposal in an issue first.
- **Code review**: Ask maintainers for guidance.

## 📜 Code of Conduct

- Be respectful and inclusive.
- Welcome newcomers and help them learn.
- Focus on constructive feedback.
- Assume good faith in discussions.

## 🎉 Recognition

Contributors are recognized in:

- `CHANGELOG.md` and `CHANGELOG_en.md`
- Release notes
- The README contributors section (for significant contributions)

## 📄 License

By contributing, you agree that your contributions will be licensed under the project's MIT License.

---

Thank you for contributing to gotemplate4j! 🚀

For detailed development plans, see [PLAN.md](./PLAN.md).
