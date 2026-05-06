# Contributing to gotemplate4j

Thank you for your interest in contributing to gotemplate4j! This document provides guidelines and information for contributors.

## 🎯 Project Goals

gotemplate4j aims to be a faithful Java implementation of Go's `text/template` package. Our goals are:

1. **Compatibility**: Maintain close compatibility with Go template semantics
2. **Performance**: Provide efficient template execution for Java applications
3. **Simplicity**: Keep the codebase clean and maintainable
4. **Quality**: Maintain high test coverage above the JaCoCo thresholds

## 📋 How to Contribute

### Reporting Issues

Before creating an issue:
- Check existing issues to avoid duplicates
- Provide a minimal reproducible example
- Include expected vs actual behavior
- Specify your Java version and environment

### Submitting Pull Requests

1. **Fork** the repository
2. **Create a branch** for your feature/fix:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```
3. **Make your changes** following the coding guidelines below
4. **Add tests** for new functionality
5. **Run all tests** to ensure nothing breaks:
   ```bash
   ./mvnw clean test
   ```
6. **Check code coverage** (must maintain the JaCoCo thresholds):
   ```bash
   ./mvnw test jacoco:report
   ```
7. **Commit your changes** with a clear message (see Commit Message Guidelines)
8. **Push** to your fork
9. **Submit a Pull Request** with a clear description

## 💻 Development Setup

For detailed prerequisites and Java version management, see the [README.md](./README.md#requirements).

### Quick Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/gotemplate4j.git
cd gotemplate4j

# Build and test
./mvnw clean install

# Run tests only
./mvnw test

# Generate coverage report
./mvnw test jacoco:report
# Open target/site/jacoco/index.html in browser
```

## 📝 Coding Guidelines

### Code Style

- Follow standard Java naming conventions:
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- Use UTF-8 encoding for all files
- Keep methods concise (<50 lines preferred)
- Add Javadoc comments for public APIs
- Use meaningful variable names

### Testing Requirements

- **All new features must include unit tests**
- **Maintain code coverage above the JaCoCo thresholds** (enforced by CI)
- Test edge cases and error conditions
- Use JUnit 5 (Jupiter) framework
- Follow existing test patterns

### Where to Put Tests

The project currently keeps all automated tests under `src/test/java` and runs them with Maven Surefire via `./mvnw test`. There is no separate `src/integrationTest` source set and no Maven Failsafe configuration at this time.

Use the existing test structure to make the intent of a new test clear:

- `src/test/java/io/github/verils/gotemplate/internal/*Test.java`: lexer, parser, AST, token, and internal utility behavior.
- `Functions*Test.java`: built-in template function behavior.
- `Template*Test.java`: public `Template` API behavior, template sets, cloning, introspection, and user-facing template operations.
- `ExecutorTest.java` or a focused top-level feature test: execution semantics that do not fit a narrower existing class.
- `GoCompatibility*Test.java`: behavior that should match Go `text/template` semantics.
- `JavaDeviationFixtureTest.java` or a focused feature test: intentional Java-specific behavior or documented deviations from Go.
- Feature-specific top-level tests such as `RangeIndexTest`, `NullSafetyTest`, or `OptionalSupportTest`: focused execution behavior that deserves its own readable test class.

Prefer adding a test to the most specific existing class when it improves readability. Create a new focused `*Test.java` class when the behavior would make an existing class too broad or hard to scan.

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

Follow [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Build/tooling changes

**Examples:**
```
feat(functions): implement deepEqual built-in function

fix(parser): handle nested variable references correctly

docs(readme): update installation instructions

test(parser): add tests for error diagnostics
```

**Important:** All commit messages must be in **English**.

## 🎯 Priority Areas for Contribution

See [PLAN.md](./PLAN.md) for the complete development roadmap.

### High Priority
1. **Performance Optimizations**
   - AST caching strategies
   - Method reflection caching
   - Memory usage improvements

2. **Documentation**
   - Comprehensive Javadoc for public APIs
   - Usage examples and tutorials
   - Architecture documentation

3. **Advanced Features**
   - Custom delimiters support
   - Enhanced whitespace control
   - Template inheritance patterns

### Medium Priority
4. **Type System Enhancements**
   - Support for Java Optional types
   - Better enum handling
   - Public field access (not just getters)

5. **Developer Experience**
   - Better error messages with suggestions
   - Debugging utilities
   - IDE integration helpers

### Low Priority
6. **Framework Integrations**
   - Spring Boot starter module
   - Jakarta EE support
   - Micronaut/Quarkus modules

## 🔍 Code Review Process

All pull requests undergo review:

1. **Automated Checks** (CI):
   - All tests must pass
   - Code coverage must remain above the JaCoCo thresholds
   - Build must succeed on Java 8

2. **Manual Review**:
   - Code quality and style
   - Test coverage adequacy
   - Compatibility with Go templates
   - Performance implications

3. **Merge Criteria**:
   - At least one maintainer approval
   - All CI checks passing
   - No breaking changes without migration plan

## 🚫 What Not to Do

- ❌ Don't add external dependencies (project uses vanilla Java only)
- ❌ Don't break backward compatibility without discussion
- ❌ Don't submit code without tests
- ❌ Don't modify LICENSE or core metadata without explicit request
- ❌ Don't add non-standard features that deviate from Go template spec

## 📊 Quality Standards

### Test Coverage
- **Minimum**: 90% instruction coverage, 85% branch coverage
- **Enforcement**: Build fails if coverage drops below threshold
- **Tool**: JaCoCo (configured in pom.xml)

### Performance
- No significant performance regressions
- Benchmark critical paths when making changes
- Consider memory allocation impact

### Compatibility
- Maintain Go template semantic compatibility
- Document any intentional deviations
- Provide migration guides for breaking changes

## 🤝 Getting Help

- **Questions**: Open a GitHub Discussion
- **Bugs**: Create an Issue with reproduction steps
- **Features**: Propose in Issues for discussion first
- **Code Review**: Ask maintainers for guidance

## 📜 Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Assume good faith in discussions

## 🎉 Recognition

Contributors will be recognized in:
- CHANGELOG entries
- Release notes
- README contributors section (for significant contributions)

## 📄 License

By contributing, you agree that your contributions will be licensed under the project's MIT License.

---

Thank you for contributing to gotemplate4j! 🚀

For detailed development plans, see [PLAN.md](./PLAN.md).
