# Unit Testing Skill

## Overview

This skill defines best practices and organizational standards for unit testing in the gotemplate4j project. Following these guidelines ensures test code maintainability, readability, and coverage.

## Test Code Organization Principles

### 1. TestSupport Unified Usage Standard

**Important: All TestSupport classes must use static methods; inheritance is prohibited!**

The project defines three TestSupport classes to simplify test code:

#### Design Principles
- ✅ All TestSupport classes must be `final` classes
- ✅ Private constructors prevent instantiation
- ✅ All methods are `static` methods
- ✅ Test classes use these methods via static imports
- ❌ Inheritance (extends) is prohibited

The project defines three TestSupport classes to simplify test code:

#### TemplateTestSupport
Location: `src/test/java/io/github/verils/gotemplate/TemplateTestSupport.java`

**Provided Utility Methods:**
- `render(String source)` - Render template (without data)
- `render(String source, Object data)` - Render template (with data)
- `data(Object... entries)` - Quickly create test data Map
- `invoke(String name, Object... args)` - Call built-in functions

**Usage Guidelines:**
```java
// ✅ Recommended: Use static imports
import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;

class MyTest {
    @Test
    void testExample() throws Exception {
        String result = render("{{.Name}}", data("Name", "Bob"));
        assertEquals("Bob", result);
    }
}

// ❌ Avoid: Repetitive Template instance creation
Template template = new Template("test");
template.parse("{{.Name}}");
StringWriter writer = new StringWriter();
template.execute(writer, data);
```

#### LexerTestSupport
Location: `src/test/java/io/github/verils/gotemplate/internal/LexerTestSupport.java`

**Provided Utility Methods:**
- `lexDefault(String input)` - Lexical analysis with default delimiters
- `lexDelim(String input)` - Lexical analysis with custom delimiters
- `token(TokenType type, String value, int pos, int line, int column)` - Create expected Token
- `assertDefaultTokens(String input, Token... expectedTokens)` - Assert lexical analysis results

**Usage Guidelines:**
```java
import static io.github.verils.gotemplate.internal.LexerTestSupport.*;

class LexerTest {
    @Test
    void testSimpleText() {
        assertDefaultTokens("hello",
            token(TokenType.TEXT, "hello", 0, 1, 1),
            token(TokenType.EOF, "", 5, 1, 6));
    }
}
```

#### ParserTestSupport
Location: `src/test/java/io/github/verils/gotemplate/internal/ParserTestSupport.java`

**Provided Utility Methods:**
- `assertOK(String name, String input, String result)` - Assert parsing success
- `assertError(String name, String text)` - Assert parsing failure
- `createParser1()` / `createParser2()` - Create parsers with different functions

**Usage Guidelines:**
```java
import static io.github.verils.gotemplate.internal.ParserTestSupport.assertError;
import static io.github.verils.gotemplate.internal.ParserTestSupport.assertOK;
import static io.github.verils.gotemplate.internal.ParserTestSupport.createParser1;

class ParserTest {
    @Test
    void testValidSyntax() throws TemplateParseException {
        assertOK("test", "{{.Name}}", "{{.Name}}");
    }
    
    @Test
    void testInvalidSyntax() {
        assertError("test", "{{.Name");
    }
    
    @Test
    void testWithCustomParser() throws TemplateParseException {
        Parser parser = createParser1();
        // Use parser directly for testing
    }
}
```

**Incorrect Example (Inheritance Prohibited):**
```java
// ❌ Wrong: Do not use inheritance
class ParserTest extends ParserTestSupport {
    @Test
    void testExample() {
        assertError("test", "{{.Name");  // Works but doesn't follow standards
    }
}

// ✅ Correct: Use static imports
class ParserTest {
    @Test
    void testExample() {
        assertError("test", "{{.Name");
    }
}
```

### 2. Test Class Splitting Principles

#### Split by Functionality and Sub-domains

**Rules:**
- Single test class should not exceed 300 lines
- One test class tests only one sub-domain of a component
- Test class naming format: `{Component}{Feature}Test.java`

**Examples:**
```
❌ Large test class (283 lines):
TemplateExecutorTest.java

✅ Split test classes:
TemplateExecutionBasicTest.java          - Basic execution tests
TemplateExecutionConditionalTest.java    - Conditional statement tests
TemplateExecutionRangeTest.java          - Range loop tests
TemplateExecutionFieldAccessTest.java    - Field access tests
TemplateExecutionTemplateTest.java       - Template definition and call tests
```

#### Test Class Organization Structure

```
src/test/java/io/github/verils/gotemplate/
├── TemplateTestSupport.java              # Shared test utilities
├── TemplateExecution*Test.java           # Execution-related tests (split by feature)
├── Functions*Test.java                   # Function-related tests (split by type)
├── internal/
│   ├── LexerTestSupport.java            # Lexer test utilities
│   ├── Lexer*Test.java                  # Lexer tests (split by feature)
│   ├── ParserTestSupport.java           # Parser test utilities
│   ├── Parser*Test.java                 # Parser tests (split by feature)
│   └── ast/
│       └── ASTNodeTest.java             # AST node tests
```

### 3. Avoid Duplicate Tests

**Methods to Identify Duplicate Tests:**
- Use grep to search for similar test method names
- Check if test scenarios cover the same code paths
- Verify if assertion conditions are identical

**Handling Duplicate Tests:**
```java
// ❌ Duplicate: Both tests verify unclosed action
class ParserCoverageErrorTest {
    @Test
    void testParserWithUnclosedAction() {
        assertThrows(Exception.class, () -> parser.parse("test", "{{ .Name"));
    }
}

class ParserLegacyErrorTest {
    @Test
    void testUnclosedAction() {
        assertError("unclosed action", "hello{{range");
    }
}

// ✅ Merged: Keep more comprehensive test, delete duplicate
class ParserLegacyErrorTest {
    @Test
    void testUnclosedAction() {
        assertError("unclosed action", "hello{{range");
    }
    
    @Test
    void testUnclosedIf() {
        assertError("unclosed if", "{{if .Name}}hello");
    }
}
```

### 4. No CoverageTest

**Principles:**
- Do not create test classes named `*CoverageTest`
- Move coverage test cases to corresponding functional test classes
- Monitor coverage through JaCoCo reports instead of dedicated test classes

**Reasons:**
- CoverageTest usually contains scattered test cases that are hard to maintain
- Functionally related tests should be centralized
- JaCoCo automatically detects uncovered code lines

## Test Writing Best Practices

### 1. Test Naming Conventions

```java
// ✅ Clearly describe test scenario and expected behavior
@Test
void testExecuteWithNullValue() { ... }

@Test
void testRangeBreakCoversArrayCollectionAndMapPaths() { ... }

// ❌ Vague naming
@Test
void test1() { ... }

@Test
void testCase1() { ... }
```

### 2. Test Structure (AAA Pattern)

```java
@Test
void testIndexReadsMapsArraysAndStrings() throws Exception {
    // Arrange - Prepare test data
    Map<String, String> map = new HashMap<>();
    map.put("key", "value");
    
    // Act - Execute test
    String result = render("{{index .Map \"key\"}}", data("Map", map));
    
    // Assert - Verify result
    assertEquals("value", result);
}
```

### 3. Parameterized Tests

For multiple input/output scenarios, use `@ParameterizedTest`:

```java
@ParameterizedTest
@MethodSource("lengthCases")
void lenReturnsSizeForSupportedTypes(Object value, Object expected) {
    assertEquals(expected, TemplateTestSupport.invoke("len", value));
}

private static Stream<Arguments> lengthCases() {
    return Stream.of(
        arguments("hello", 5),
        arguments("", 0),
        arguments(new String[]{"a", "b", "c"}, 3),
        arguments(null, 0)
    );
}
```

### 4. Test Isolation

```java
// ✅ Each test is independent, does not depend on state from other tests
@Test
void testExample1() {
    Template template = new Template("test");
    // ...
}

@Test
void testExample2() {
    Template template = new Template("test");  // Recreate
    // ...
}

// ❌ Avoid: Sharing state between tests
private Template sharedTemplate;

@BeforeEach
void setUp() {
    sharedTemplate = new Template("test");
}
```

### 5. Exception Testing

```java
// ✅ Use assertThrows to verify exceptions
@Test
void testUnparsedTemplateThrowsException() {
    Template template = new Template("test");
    assertThrows(Exception.class, () -> {
        template.execute(new StringWriter(), null);
    });
}

// ✅ Verify exception messages
@Test
void testMissingFieldShowsPath() {
    TemplateExecutionException ex = assertThrows(TemplateExecutionException.class,
        () -> template.execute(writer, data));
    assertTrue(ex.getMessage().contains("can't evaluate field"));
}
```

## Test Coverage Requirements

### Coverage Thresholds
- **Instruction Coverage**: ≥ 90%
- **Branch Coverage**: ≥ 85%

### Checking and Improving Coverage

```bash
# Run tests and generate coverage report
./mvnw clean test jacoco:report

# View HTML report
open target/site/jacoco/index.html

# Full build (includes coverage check)
./mvnw clean verify "-Dgpg.skip=true"
```

### Coverage Focus Areas

Reasons why template engines require high coverage:
1. **Complex parsing logic** - 20+ AST node types
2. **Security-critical** - Prevent template injection attacks
3. **Many edge cases** - Various data types and nested structures
4. **Backward compatibility** - Library stability and reliability

## Refactoring Guidelines

### When to Refactor Tests

1. **Test class exceeds 300 lines** - Split by functionality
2. **Duplicate tests discovered** - Merge or delete
3. **Inconsistent TestSupport usage** - Unify usage patterns
4. **Unclear test names** - Rename to descriptive names
5. **Complex test logic** - Extract helper methods or use parameterized tests

### Refactoring Steps

1. **Analyze existing tests** - Identify duplicates and mergeable tests
2. **Create new test classes** - Split large test classes by functionality
3. **Migrate test cases** - Move to new classes and use TestSupport
4. **Delete old files** - Ensure all tests have been migrated
5. **Run tests** - Verify all tests pass
6. **Check coverage** - Ensure coverage has not decreased

## Example: Complete Test Class

```java
package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Tests for collection-related built-in functions (len, index, slice).
 */
class FunctionsCollectionTest {

    @ParameterizedTest
    @MethodSource("lengthCases")
    void lenReturnsSizeForSupportedTypes(Object value, Object expected) {
        assertEquals(expected, TemplateTestSupport.invoke("len", value));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> lengthCases() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        
        return Stream.of(
            arguments("hello", 5),
            arguments("", 0),
            arguments(new String[]{"a", "b", "c"}, 3),
            arguments(map, 2),
            arguments(null, 0)
        );
    }

    @Test
    void lenRejectsInvalidArgumentCountsAndTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("len"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("len", 1, 2));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("len", 42));
    }

    @Test
    void indexReadsMapsArraysAndStrings() throws IOException, TemplateException {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        assertEquals("value", render("{{index .Map \"key\"}}", data("Map", map)));
        assertEquals("b", render("{{index .Items 1}}", data("Items", new String[]{"a", "b", "c"})));
        assertEquals("h", render("{{index .Text 0}}", data("Text", "hello")));
    }
}
```

## Summary

Following this skill's guidelines ensures:
- ✅ Test code is easy to understand and maintain
- ✅ Test coverage is comprehensive without duplication
- ✅ New developers can quickly start writing tests
- ✅ Maintain 90%+ code coverage
- ✅ Test classes have single responsibilities and clear structure

Remember: **High-quality tests are the cornerstone of template engine reliability**.
