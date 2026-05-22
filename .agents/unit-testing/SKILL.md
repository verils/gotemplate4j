---
name: unit-testing
description: Unit testing workflow and standards for gotemplate4j. Use when adding, modifying, reviewing, or refactoring tests in this Java 8 Maven project, especially for Template, Lexer, Parser, Executor, AST, built-in function, coverage, or TestSupport-related work.
---

# Unit Testing

Follow this skill when working on gotemplate4j tests. Keep tests focused, behavior-driven, and aligned with the existing Java 8/JUnit 5/Maven setup.

## Core Rules

- Use `./mvnw`, never `mvn` directly.
- Preserve Java 8 compatibility.
- Add or update focused tests for every behavior change.
- Prefer modifying existing test classes when that keeps related behavior together.
- Do not create `*CoverageTest` classes.
- Keep coverage at or above project thresholds: instruction coverage 90%, branch coverage 85%.
- Avoid external dependencies.
- Use existing test helpers through static imports; do not inherit from support classes.

## TestSupport Usage

All TestSupport classes must be utility classes:

- Declare the class `final`.
- Provide a private constructor.
- Expose only `static` helper methods.
- Use helpers through static imports in tests.
- Do not use `extends` with any TestSupport class.

### TemplateTestSupport

Location: `src/test/java/io/github/verils/gotemplate/TemplateTestSupport.java`

Use for public template execution and built-in function tests:

- `render(String source)`
- `render(String source, Object data)`
- `data(Object... entries)`
- `invoke(String name, Object... args)`

```java
import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateExecutionFieldAccessTest {

    @Test
    void testFieldAccessReadsMapValue() throws Exception {
        assertEquals("Bob", render("{{.Name}}", data("Name", "Bob")));
    }
}
```

Avoid repeated low-level setup when a TestSupport helper already expresses the behavior:

```java
Template template = new Template("test");
template.parse("{{.Name}}");
StringWriter writer = new StringWriter();
template.execute(writer, data);
```

### LexerTestSupport

Location: `src/test/java/io/github/verils/gotemplate/internal/LexerTestSupport.java`

Use for lexer token assertions:

- `lexDefault(String input)`
- `lexDelim(String input)`
- `token(TokenType type, String value, int pos, int line, int column)`
- `assertDefaultTokens(String input, Token... expectedTokens)`

```java
import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.token;

class LexerTextTest {

    @Test
    void testSimpleTextEmitsTextThenEof() {
        assertDefaultTokens("hello",
            token(TokenType.TEXT, "hello", 0, 1, 1),
            token(TokenType.EOF, "", 5, 1, 6));
    }
}
```

### ParserTestSupport

Location: `src/test/java/io/github/verils/gotemplate/internal/ParserTestSupport.java`

Use for parser success and failure assertions:

- `assertOK(String name, String input, String result)`
- `assertError(String name, String text)`
- `createParser1()`
- `createParser2()`

```java
import static io.github.verils.gotemplate.internal.ParserTestSupport.assertError;
import static io.github.verils.gotemplate.internal.ParserTestSupport.assertOK;

class ParserActionTest {

    @Test
    void testFieldActionParses() throws TemplateParseException {
        assertOK("field", "{{.Name}}", "{{.Name}}");
    }

    @Test
    void testUnclosedActionFails() {
        assertError("unclosed action", "{{.Name");
    }
}
```

Do not write tests like this:

```java
class ParserActionTest extends ParserTestSupport {
    @Test
    void testUnclosedActionFails() {
        assertError("unclosed action", "{{.Name");
    }
}
```

## Test Organization

Place tests under `src/test/java/io/github/verils/gotemplate/`.

Use this organization:

```text
src/test/java/io/github/verils/gotemplate/
├── TemplateTestSupport.java
├── TemplateExecution*Test.java
├── Functions*Test.java
├── internal/
│   ├── LexerTestSupport.java
│   ├── Lexer*Test.java
│   ├── ParserTestSupport.java
│   ├── Parser*Test.java
│   └── ast/
│       └── *Test.java
```

Split tests by component and sub-domain:

- Use names like `{Component}{Feature}Test.java`.
- Keep one test class focused on one behavior area.
- Split classes that approach or exceed 300 lines.
- Prefer `TemplateExecutionConditionalTest`, `TemplateExecutionRangeTest`, and `TemplateExecutionFieldAccessTest` over one large `TemplateExecutorTest`.

## Duplicate Test Control

Before adding tests:

- Search for similar method names and scenarios with `rg`.
- Check whether an existing test already covers the same code path.
- Extend the most relevant existing test when it improves locality.
- Delete or merge duplicate tests during refactors.

Do not keep scattered tests only to satisfy coverage. Move coverage-motivated cases into the functional test class for the behavior they exercise.

## Writing Tests

Use descriptive method names that state behavior:

```java
@Test
void testExecuteWithNullValue() {
}

@Test
void testRangeBreakCoversArrayCollectionAndMapPaths() {
}
```

Avoid vague names:

```java
@Test
void test1() {
}
```

Prefer Arrange/Act/Assert structure when a test has setup:

```java
@Test
void testIndexReadsMapValue() throws Exception {
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");

    String result = render("{{index .Map \"key\"}}", data("Map", map));

    assertEquals("value", result);
}
```

Use parameterized tests for compact input/output matrices:

```java
@ParameterizedTest
@MethodSource("lengthCases")
void testLenReturnsSizeForSupportedTypes(Object value, Object expected) {
    assertEquals(expected, TemplateTestSupport.invoke("len", value));
}

private static Stream<Arguments> lengthCases() {
    return Stream.of(
        arguments("hello", 5),
        arguments("", 0),
        arguments(new String[] {"a", "b", "c"}, 3),
        arguments(null, 0)
    );
}
```

Keep tests isolated. Do not rely on execution order or shared mutable state between tests.

For exception tests:

```java
@Test
void testUnparsedTemplateThrowsException() {
    Template template = new Template("test");

    assertThrows(Exception.class, () -> template.execute(new StringWriter(), null));
}

@Test
void testMissingFieldShowsPath() {
    TemplateExecutionException ex = assertThrows(TemplateExecutionException.class,
        () -> template.execute(writer, data));

    assertTrue(ex.getMessage().contains("can't evaluate field"));
}
```

Prefer specific exception types when the code exposes them consistently.

## Refactoring Workflow

Use this workflow when reorganizing tests:

1. Identify the component and behavior area for each test.
2. Search for duplicates and overlapping assertions.
3. Move tests into the closest functional test class.
4. Introduce a new test class only when an existing class would become unfocused or too large.
5. Replace repeated setup with existing TestSupport helpers.
6. Remove obsolete duplicate files after migration.
7. Run the relevant test subset, then the full test suite when practical.

## Coverage Verification

Use Java 8 for verification. If the default JDK is not Java 8, set `JAVA_HOME` before running Maven.

Common commands:

```bash
./mvnw test
./mvnw clean test jacoco:report
./mvnw clean verify "-Dgpg.skip=true"
```

Use JaCoCo reports to find missing branches and lines:

- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`
- CSV: `target/site/jacoco/jacoco.csv`

Focus coverage improvements on parser branches, executor paths, built-in function edge cases, AST node behavior, and error handling.
