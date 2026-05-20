# gotemplate4j Development Plan

**Last Updated**: 2026-05-20  
**Current Version**: 0.9.0 (in development)  
**Next Version**: 0.10.0 (Java 11 upgrade planned)  
**Current Focus**: v0.9.0 - Final stage: Testing & Documentation before release

---

## Plan Documentation and Rules

### Project Direction

gotemplate4j should remain a small, Java-compatible implementation of Go's `text/template` semantics for Java applications.

**Important Notice**: Starting from v0.10.0, gotemplate4j will require **Java 11 or higher**. This decision aligns with industry standards and enables the use of modern Java features for cleaner, more maintainable code. Java 8 support will end with v0.9.x.

### Document Maintenance Rules

- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
- Items in "Future Backlog" should be reviewed periodically and either promoted to active development or removed.
- Completed version releases should be moved to "Current Progress" section, not kept in detailed planning sections.

---

## Current Progress

### ✅ Completed Versions (v0.8.0 - Released)

v0.8.0 has been released with the following improvements:

- **@TemplateField Annotation Support**: Explicit field/method name control with annotation-based mapping
- **Improved Error Messages**: Enhanced field-chain error reporting with full path context
- **Performance Optimizations**: Significant performance improvements across all benchmarks
    - beanAccessBenchmark: +267% improvement (ClassMetadata unified cache)
    - executeBenchmark: +81.3% improvement
    - parseBenchmark: +45.0% improvement
    - All benchmarks show substantial gains with no regressions
- **JMH Benchmark Infrastructure**: Established comprehensive performance testing framework
- **Test Coverage**: Maintained high quality (~92% instruction, ~89% branch)

---

## Version Plans

### In Development: v0.9.0 Release Plan

**Positioning**: v0.9.0 is the final Java 8 release, focusing on practical developer experience improvements and Go `text/template` compatibility enhancements.

**Important Scope Note**: gotemplate4j implements Go's `text/template` package only, NOT `html/template`. Features like contextual auto-escaping, XSS prevention, and HTML-specific security mechanisms are intentionally out of scope. Users needing HTML safety should use the existing `{{html .value}}` function or pre-process data in Java code.

#### Goals

- Enhance file loading APIs for better developer experience (classpath, directory, encoding support)
- Complete integer range support implementation and testing (already implemented in v0.8.0)
- Improve error diagnostics with detailed context information
- Maintain Java 8 compatibility and stability
- Prepare codebase for smooth transition to Java 11 in v0.10.0

#### Non-Goals

- Do not implement Java 11-specific features (deferred to v0.10.0)
- Do not break Java 8 compatibility
- Do not add runtime dependencies beyond vanilla Java
- Do not implement method invocation with arguments (requires extensive security design)
- Do not start template pre-compilation (deferred to v1.0+)

#### Stage 1: File Helper Improvements ✅ COMPLETED

Enhanced file loading APIs for better developer experience.

**Completed Features:**

- **Classpath Loading API**
  - `parseFromClasspath(String resourcePath)` - Load templates from classpath
  - `parseFromResource(String resourceName)` - Convenient alias method
  - Support for multiple ClassLoaders (context, class, system)
  - Clear error messages when resources not found

- **Encoding Support**
  - `parseFile(Path path, Charset charset)` - Type-safe charset specification
  - `parse(InputStream in, String charsetName)` - Stream parsing with encoding
  - `parse(InputStream in, Charset charset)` - Type-safe stream parsing

- **Directory Operations**
  - `parseDirectory(Path directory)` - Parse all .tmpl files in directory
  - Directory existence and type validation
  - Helpful error messages for invalid paths

- **Batch Loading**
  - `parseClasspathResources(String pattern)` - Static method for bulk loading
  - Glob pattern support for resource matching

- **Error Diagnostics**
  - Show absolute paths when files not found
  - Display ClassLoader information for classpath errors
  - Provide actionable tips in error messages

**Testing:**
- 20 comprehensive test cases covering all new APIs
- All tests passing with no regressions
- Code coverage maintained at >90%

**Status**: Complete and ready for use

#### Stage 2: Integer Range Support ✅ COMPLETED

Go-style integer range iteration is already implemented in v0.8.0:

**Syntax Support:**
```gotemplate
{{range $i := 5}}
  Index: {{$i}}  // 0, 1, 2, 3, 4
{{end}}

{{range $i, $j := 3}}
  Pair: {{$i}}, {{$j}}  // (0,0), (1,1), (2,2)
{{end}}
```

**Implementation Status:**
- ✅ Parser: Recognizes integer range syntax
- ✅ Executor: Generates integer sequences (Executor.java lines 154-167)
- ✅ Tests: Comprehensive test coverage (IntegerRangeTest.java)
- ✅ Documentation: User guide updated (control-flow.md)

**Compatibility:**
- Matches Go `text/template` behavior
- Supports both single-variable and two-variable forms
- Handles edge cases (zero, negative numbers)

**Remaining Work**: None - feature complete

#### Stage 3: Enhanced Error Diagnostics 🔄 IN PROGRESS

Improve error messages and provide better debugging information to help developers quickly identify and fix template issues.

**Goals:**
- Make parse errors more actionable with precise location information
- Provide complete context for execution errors (full field paths, available fields)
- Offer intelligent suggestions for common mistakes (typos, missing keys)
- Maintain consistent error message formatting across the codebase

**Current Status & Gaps Analysis:**

1. **Parse Error Enhancements** ✅ COMPLETED
   - [x] Include line and column numbers in all parse errors
   - [x] Show the problematic template line with visual indicator (`buildErrorMessage` in `Parser.java`)
   - [x] Provide context about what was expected vs what was found
   - [x] Add line/column fields to `TemplateParseException` for programmatic access

2. **Error Utility Infrastructure** ✅ COMPLETED
   - [x] Created `ErrorUtils` with Levenshtein distance algorithm
   - [x] Implemented similarity scoring and matching functions
   - [x] Added suggestion generation for typos
   - [x] Comprehensive test coverage (9 test cases)

3. **Execution Error Context** ✅ COMPLETED
   - [x] Display full field path in evaluation errors (e.g., "can't evaluate field Address.City")
   - [x] List available fields when accessing non-existent fields (**COMPLETED - P1**)
   - [ ] Show data type information for better understanding (**OPTIONAL**)

4. **Intelligent Suggestions** ✅ COMPLETED
   - [x] Detect typos in field names and suggest corrections (**COMPLETED - P1**)
   - [x] Show similar map keys when a key is not found (**COMPLETED - P2**)
   - [ ] Provide helpful hints for common mistakes (**PARTIAL**)

5. **Function Call Error Details** 🔴 MISSING
   - [ ] Show expected function signature (**MISSING - P3**)
   - [ ] Display actual arguments provided (**MISSING - P3**)
   - [ ] List available functions when calling undefined function (**MISSING - P3**)

**Prioritized Action Plan:**

| Priority | Task | Description | Estimated Effort | Status |
| :--- | :--- | :--- | :--- | :--- |
| **P0** | Implement `ErrorUtils` ✅ | Create utility for Levenshtein distance calculation and similarity matching. | 0.5 day | ✅ DONE |
| **P0.5** | Enhance `TemplateParseException` ✅ | Add line/column fields to the exception class so callers can programmatically access error location details. | 0.5 day | ✅ DONE |
| **P1** | Enhance Field Errors ✅ | Update `Executor.executeFieldPath` to show available fields and typo suggestions using `ClassMetadata`. | 1 day | ✅ DONE |
| **P2** | Enhance Map Key Errors ✅ | Update `Executor.handleMissingMapKey` to list available keys and suggest corrections. | 0.5 day | ✅ DONE |
| **P3** | Enhance Function Errors ✅ | Improve `Executor.executeFunction` to show argument mismatches and list defined functions. | 1 day | ✅ DONE |
| **P3.5** | Unify Parser Exception Messages ✅ | Standardize all TemplateParseException messages in Parser.java with consistent format and ensure all include line/column info. | 1 day | ✅ DONE |
| **P4** | Testing & Polish ✅ | Add comprehensive tests for new error formats and ensure backward compatibility. | 1 day | ✅ DONE |

**Completed Work (P1):**
- ✅ Enhanced `Executor.executeFieldPath` to display available fields on error
- ✅ Integrated `ErrorUtils` for intelligent typo suggestions
- ✅ Filtered out Object class methods from suggestions (hashCode, toString, etc.)
- ✅ Lowered similarity threshold to 0.5 for better typo detection
- ✅ Added case-insensitive exact match check before fuzzy matching
- ✅ Created comprehensive test suite (6 test cases in `EnhancedFieldErrorTest`)
- ✅ All 807 tests passing, code coverage maintained >90%

**Completed Work (P2):**
- ✅ Enhanced `Executor.handleMissingMapKey` to display available keys on error
- ✅ Integrated `ErrorUtils` for map key typo suggestions
- ✅ Supports both field path access and `index` function scenarios
- ✅ Handles various key types (String, Integer, etc.) with proper string conversion
- ✅ Created comprehensive test suite (11 test cases in `EnhancedMapKeyErrorTest`)
- ✅ All 818 tests passing, code coverage maintained at 91% instructions / 89% branches

**Completed Work (P3):**
- ✅ Enhanced `Executor.executeFunction` to display argument count on function execution errors
- ✅ Enhanced `Parser.parseCommand` to show available functions when undefined function is encountered
- ✅ Integrated `ErrorUtils` for function name typo suggestions in both parse and execution phases
- ✅ Shows top 5 most similar function names to help users identify typos
- ✅ Lists all available functions (built-in + custom) with count indication
- ✅ Provides clear error messages for both parse-time and runtime function errors
- ✅ Created comprehensive test suite (5 test cases in `EnhancedFunctionErrorTest`)
- ✅ All tests passing, maintaining high code coverage

**Pending Work (P3.5): Parser Exception Message Unification**

**Problem**: Current exception messages in Parser.java have inconsistent formats:
- Mixed styles: some use quotes, some don't (`"unexpected '%s'"` vs `"missing token"`)
- Redundant information: line/column repeated in message text when already in exception object
- Inconsistent separators: colon, space, or no separator
- Vague messages: "missing token" doesn't indicate what kind of token was expected

**Proposed Solution**: Adopt unified format following Go template conventions:
```
<error-type>: <description> [in <context>]
```

**Format Rules**:
1. Use standard prefixes: `unexpected`, `undefined`, `invalid`, `missing`
2. Use colon `:` as primary separator between type and description
3. Remove redundant line/column from message text (provided by exception object)
4. Remove unnecessary quotes around values
5. Keep messages concise but informative

**Examples**:

| Current Format | Proposed Format | Rationale |
|---------------|----------------|-----------|
| `"missing token"` | `"unexpected EOF"` | More specific |
| `"unexpected '%s' in %s"` | `"unexpected %s in %s"` | Remove extra quotes |
| `"undefined variable %s"` | `"undefined variable: %s"` | Consistent separator |
| `"function 'unknow' is not defined"` | `"undefined function: unknow"` | Unified prefix |
| `"illegal number syntax: %s, line: %d, column: %d"` | `"invalid number syntax: %s"` | Remove redundant location |
| `"non executable command in pipeline stage %d"` | `"non-executable command in pipeline stage %d"` | Use hyphen |

**Implementation Plan**:
1. Audit all `throwUnexpectError()` and `throw new TemplateParseException()` calls in Parser.java
2. Categorize messages by type (unexpected, undefined, invalid, missing, etc.)
3. Apply unified format to each category
4. Ensure all exceptions include line/column via token parameter
5. Update tests to match new message formats
6. Verify no regressions in error handling

**Estimated Effort**: 1 day

**Example Output:**
```
Field Error:
Before: can't evaluate field FristName
After:  can't evaluate field User.FristName. Available fields: [age, firstName, getAge, getFirstName, getName, name] Did you mean 'firstName'?

Map Key Error:
Before: missing map key 'FristName'
After:  missing map key 'FristName'. Available keys: [Age, FirstName, LastName]. Did you mean 'FirstName'?

Function Error (Parse Time):
Before: function leng not defined
After:  function 'leng' is not defined. Available functions: [default, deepEqual, index, kindOf, len, ...] Did you mean 'len'?

Function Error (Execution Time):
Before: function 'customFunc' failed
After:  function 'customFunc' failed with 1 argument(s): customFunc requires exactly 2 arguments
```

**Implementation Approach:**
- Use existing `ClassMetadata` cache to efficiently retrieve available fields/methods without performance penalty.
- Implement Levenshtein distance algorithm in a new internal utility class.
- Ensure error messages remain concise but informative (avoid overwhelming users with too much data).
- Maintain backward compatibility with existing exception constructors.

**Testing Strategy:**
- Add tests for parse error location accuracy (verify existing logic).
- Verify field path completeness in execution errors.
- Test suggestion quality for common typos (e.g., `FristName` -> `firstName`).
- Ensure error messages are clear and actionable.

**Completed Work (P3.5):**
- ✅ Unified all Parser exception messages with `<type>: <description>` format
- ✅ Removed unnecessary quotes from error messages (e.g., `unexpected '%s'` → `unexpected %s`)
- ✅ Eliminated redundant line/column information from message text (provided by exception object)
- ✅ Standardized prefixes: `unexpected`, `undefined`, `invalid`, `missing`
- ✅ Improved terminology clarity (e.g., `unexpected . after term` → `unexpected dot after term`)
- ✅ Added hyphens to compound adjectives (e.g., `non-executable command`)
- ✅ Updated all related test assertions to match new message formats
- ✅ All 823 tests passing, code coverage maintained >90%

**Completed Work (P4):**
- ✅ Verified all error diagnostic features work correctly together
- ✅ Confirmed backward compatibility with existing exception handling
- ✅ Validated error message consistency across Parser and Executor
- ✅ Full build verification passed (`./mvnw verify "-Dgpg.skip=true"`)

**Estimated Effort**: 3-4 days total  
**Progress**: ✅ **100% COMPLETE** - Stage 3 fully implemented and tested

#### Stage 4: Testing & Documentation 🔄 IN PROGRESS

Final validation and documentation updates before v0.9.0 release.

**Status**: Ready to begin - All core features complete

**Testing Requirements:**
- ✅ File loading API tests (classpath, directory, encoding) - Complete
- ✅ Error diagnostics accuracy tests - Complete
- ⏳ Backward compatibility tests - Pending verification
- ⏳ Cross-platform file path tests - Pending verification
- ⏳ Performance regression tests - Pending JMH benchmarks

**Documentation Updates:**
- ⏳ Add new API usage examples for file loading
- ⏳ Document classpath resource loading best practices
- ⏳ Enhance User Guide with file loading patterns
- ⏳ Update FAQ with integer range syntax examples
- ⏳ Update API reference documentation
- ⏳ Document enhanced error messages with examples

**Performance Validation:**
- ✅ Run JMH benchmarks - **COMPLETED** (2026-05-20)
- ✅ Ensure no performance regression from new features
- ⏳ Validate file loading performance

**Completion Criteria:**
- [ ] All new features have complete test coverage
- [ ] Code coverage maintains ≥90%
- [ ] All existing tests pass
- [ ] JMH benchmarks show no degradation
- [ ] Documentation fully updated
- [ ] `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8
- [ ] No backward compatibility breaks

**Estimated Effort**: 1-2 days

---

## v0.9.0 Release - Next Steps Detailed Plan

**Current Status**: Stage 3 complete, Stage 4 in progress  
**Last Updated**: 2026-05-20  
**Target Release**: End of current week

### ✅ Completed: JMH Benchmark Validation

**Date**: 2026-05-20  
**Status**: PASSED

**Results**:
```
Benchmark                                     Mode  Cnt        Score        Error  Units
TemplateJmhBenchmark.beanAccessBenchmark     thrpt    5  1626883.985 ± 186742.249  ops/s
TemplateJmhBenchmark.executeBenchmark        thrpt    5  1761225.693 ± 125373.880  ops/s
TemplateJmhBenchmark.functionHeavyBenchmark  thrpt    5  1019492.665 ±  48493.388  ops/s
TemplateJmhBenchmark.mapAccessBenchmark      thrpt    5  2671596.117 ± 163699.932  ops/s
TemplateJmhBenchmark.parseBenchmark          thrpt    5   293991.248 ±  67107.734  ops/s
TemplateJmhBenchmark.rangeHeavyBenchmark     thrpt    5    38906.306 ±    940.566  ops/s
```

**Analysis**:
- ✅ No performance regression detected (all within expected ranges)
- ✅ v0.9.0 maintains v0.8.0 performance improvements
- ✅ Enhanced error diagnostics have negligible performance impact
- ✅ All benchmarks completed successfully

**Conclusion**: Performance validation PASSED - Ready for next steps

---

### Step 2: Backward Compatibility Verification (Priority: High)

**Objective**: Ensure v0.9.0 maintains full backward compatibility with v0.8.x.

**Test Scenarios**:
1. **API Compatibility**
   - Test existing public API methods unchanged
   - Verify exception hierarchy preserved
   - Confirm Template class behavior consistent

2. **Template Syntax Compatibility**
   - Run Go compatibility fixture tests
   - Verify all v0.8.x templates parse correctly
   - Test edge cases (empty templates, special characters)

3. **Error Message Changes Impact**
   - Review error message format changes
   - Ensure message content still actionable
   - Verify line/column information accurate

**Actions**:
```bash
# Run full test suite
./mvnw clean test

# Run Go compatibility tests specifically
./mvnw test -Dtest=GoCompatibilityFixtureTest,JavaDeviationFixtureTest

# Verify coverage threshold
./mvnw verify "-Dgpg.skip=true"
```

**Success Criteria**:
- [ ] All 823+ tests pass
- [ ] Code coverage ≥90%
- [ ] No breaking changes detected

**Estimated Time**: 0.5 day

---

### Step 3: Documentation Updates (Priority: Medium)

**Objective**: Update all user-facing documentation for v0.9.0 features.

**Files to Update**:

1. **README.md & README_zh.md** (同步更新)
   - Add file loading API examples
   - Document enhanced error messages
   - Update version number to 0.9.0

2. **docs/getting-started/basic-concepts.md**
   - Add integer range syntax examples
   - Include ErrorUtils mention in error handling section

3. **docs/user-guide/error-handling.md** (New or Enhanced)
   - Document enhanced field error messages
   - Show map key error suggestions
   - Explain function error diagnostics
   - Provide troubleshooting guide with examples

4. **docs/user-guide/template-syntax.md**
   - Add integer range iteration examples
   - Document both single and two-variable forms

5. **docs/api-reference/template-api.md**
   - Document new file loading methods:
     - `parseFromClasspath(String)`
     - `parseFromResource(String)`
     - `parseFile(Path, Charset)`
     - `parseDirectory(Path)`
     - `parseClasspathResources(String)`

6. **docs/examples/basic-examples.md**
   - Add classpath loading example
   - Add directory loading example
   - Add encoding specification example

7. **docs/faq.md**
   - Add Q&A about integer range syntax
   - Add Q&A about enhanced error messages
   - Add Q&A about file loading best practices

**Documentation Guidelines**:
- Keep examples concise and practical
- Use consistent formatting across all docs
- Ensure Chinese translation matches English version
- Include before/after examples for error messages

**Estimated Time**: 1 day

---

### Step 4: Final Release Preparation (Priority: High)

**Objective**: Prepare v0.9.0 for release.

**Checklist**:
- [ ] Update version in pom.xml: `0.8.0` → `0.9.0`
- [ ] Update CHANGELOG with v0.9.0 release notes
- [ ] Update PLAN.md "Current Progress" section
- [ ] Create git tag: `v0.9.0`
- [ ] Prepare GitHub release notes
- [ ] Verify Maven Central deployment process

**Release Notes Content**:
```markdown
## v0.9.0 - Enhanced Developer Experience

### New Features
- **Enhanced File Loading APIs**: Classpath, directory, and encoding support
- **Integer Range Support**: Go-style `{{range $i := 5}}` syntax
- **Enhanced Error Diagnostics**: Intelligent suggestions and detailed context

### Improvements
- Unified parser exception message format
- Improved error messages with typo suggestions
- Better field path context in execution errors
- Function name suggestions for undefined functions

### Performance
- Maintained v0.8.0 performance improvements
- No regressions in benchmarks

### Compatibility
- Full backward compatible with v0.8.x
- Last Java 8 supported version
```

**Estimated Time**: 0.5 day

---

### Summary Timeline

| Task | Priority | Estimated Time | Dependencies |
|------|----------|----------------|--------------|
| ✅ JMH Benchmarks | High | 0.5 day | None - DONE |
| Backward Compatibility | High | 0.5 day | Benchmarks pass |
| Documentation Updates | Medium | 1 day | Features complete |
| Release Preparation | High | 0.5 day | All above complete |
| **Remaining Total** | | **2 days** | |

**Target Completion**: End of current week

---

### Next Version: v0.10.0 - Java 11 Migration

#### Overview

Starting from version 0.10.0, gotemplate4j will require **Java 11 or higher**. This strategic decision enables:
- Access to modern Java language features (var, records, pattern matching)
- Better performance with G1 GC and JIT improvements
- Alignment with industry standards (Java 8 reached end of public updates in 2019)
- Simplified codebase maintenance

#### Migration Timeline

```
v0.8.0 (Released) → Java 8, Quality improvements + Performance optimization
v0.9.0 (Current)  → Java 8, Deprecation notices + Compatibility testing
v0.10.0 (Next)    → Java 11+, Modern features + Code cleanup
```

#### What Changes in v0.10.0?

**Breaking Changes:**
- Minimum Java version: 8 → 11
- No API changes planned (backward compatible at API level)
- Runtime requirement only

**Code Improvements:**
- Use `var` for cleaner variable declarations
- Leverage `List.of()`, `Map.of()` for immutable collections
- Use modern String methods (`isBlank()`, `strip()`, `repeat()`)
- Enhanced Optional API usage
- Remove Java 8 compatibility workarounds

**Performance Benefits:**
- G1 Garbage Collector (default in Java 11)
- Improved JIT compilation
- Better memory management
- Potential 10-20% performance improvement

#### Migration Guide for Users

**If you're currently using Java 8:**

1. **Upgrade to Java 11 LTS** (recommended) or Java 17 LTS
    - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
    - Most systems can run multiple Java versions side-by-side

2. **Update your build configuration:**
   ```xml
   <!-- Maven pom.xml -->
   <properties>
       <maven.compiler.source>11</maven.compiler.source>
       <maven.compiler.target>11</maven.compiler.target>
   </properties>
   ```

3. **No code changes required** (unless you're extending internal classes)

**Expected Impact:**
- ✅ Most users: Zero code changes needed
- ✅ Library consumers: Just update Java runtime
- ⚠️ Android developers: May need to stay on v0.9.x (last Java 8 version)
- ⚠️ Legacy systems: Plan Java upgrade alongside library upgrade

#### Rationale

**Why Java 11?**
- Long-term support until September 2026
- Mature ecosystem with widespread adoption
- Significant improvements over Java 8
- Industry standard for new projects

**Why not Java 17?**
- Java 11 provides sufficient modern features
- Wider current adoption in enterprise environments
- Can consider Java 17+ in future major releases (v1.0+)

#### Support Policy

- **v0.9.x**: Last version supporting Java 8 (security fixes only after v0.10.0 release)
- **v0.10.0+**: Requires Java 11 or higher
- **Critical security patches**: May backport to v0.9.x for limited time

For questions or concerns about this migration, please open an issue on GitHub.

---

## Future Version Plans (Backlog)

This section tracks potential improvements and features that are not yet scheduled for specific releases.
Items will be moved to active development stages based on user feedback, priority assessment, and resource availability.

### Testing & Quality

#### Test Suite Reorganization 🟡 MEDIUM PRIORITY
**Priority**: Medium (Improve maintainability and clarity)
**Status**: Planning phase - Ready for execution

**Description**: Reorganize the test suite by component and feature to improve maintainability, discoverability, and coverage tracking. This is a refactoring task that restructures test files without changing functionality.

**Current State Analysis** (as of 2026-05-20):

The test suite currently contains **70 test files** with the following issues:

1. **Flat Structure**: All tests in root package, making navigation difficult
   - 46 test files in `io.github.verils.gotemplate` root
   - 20 test files in `io.github.verils.gotemplate.internal`
   - No sub-package organization by component or feature

2. **Inconsistent Naming Patterns**:
   - Component-based: `LexerErrorTest`, `ParserCanonicalTest`, `ExecutorTest`
   - Feature-based: `BreakContinueTest`, `IntegerRangeTest`, `TemplateInheritanceTest`
   - Mixed concerns: `TemplateExecutionBasicTest`, `TemplateExecutionConditionalTest`, etc. (6 files)
   - Error diagnostics: `EnhancedFieldErrorTest`, `EnhancedFunctionErrorTest`, `EnhancedMapKeyErrorTest`

3. **Scattered Related Tests**:
   - Template execution tests split across 6 files (`TemplateExecution*Test.java`)
   - Function tests split across 6 files (`Functions*Test.java`)
   - Parser tests already well-organized in `internal` package
   - Lexer tests already well-organized in `internal` package

4. **Helper Classes Mixed with Tests**:
   - `TemplateTestSupport.java` - base test class
   - `Recipient.java` - test data model
   - Located alongside test files instead of dedicated test utilities package

**Target Organization Structure**:

```
src/test/java/io/github/verils/gotemplate/
├── api/                          # Public API tests
│   ├── TemplateApiTest.java
│   ├── TemplateCloningTest.java
│   ├── TemplateIntrospectionTest.java
│   └── TemplateFileHelperTest.java
│
├── features/                     # Feature-based tests (Go template semantics)
│   ├── ControlFlowFeatureTest.java          # if, else, end
│   ├── RangeFeatureTest.java                # range loops
│   ├── WithFeatureTest.java                 # with blocks
│   ├── BlockFeatureTest.java                # block definitions
│   ├── BreakContinueFeatureTest.java        # break/continue
│   ├── TemplateInclusionFeatureTest.java    # template/block inclusion
│   ├── TemplateInheritanceFeatureTest.java  # define/extends
│   ├── VariablePipelineFeatureTest.java     # variables and pipelines
│   └── TruthinessFeatureTest.java           # boolean evaluation
│
├── functions/                    # Built-in function tests
│   ├── CollectionFunctionsTest.java         # index, len, etc.
│   ├── ComparisonFunctionsTest.java         # eq, ne, lt, gt, etc.
│   ├── LogicalFunctionsTest.java            # and, or, not
│   ├── FormattingFunctionsTest.java         # printf, sprintf
│   ├── EscapingFunctionsTest.java           # html, js, urlquery
│   ├── IntrospectionFunctionsTest.java      # kindOf, typeOf
│   └── CallFunctionTest.java                # call
│
├── errorhandling/                # Error handling and diagnostics
│   ├── ParseErrorTest.java                  # TemplateParseException
│   ├── ExecutionErrorTest.java              # TemplateExecutionException
│   ├── EnhancedFieldErrorTest.java          # Field access errors
│   ├── EnhancedMapKeyErrorTest.java         # Map key errors
│   ├── EnhancedFunctionErrorTest.java       # Function errors
│   └── MissingKeyPolicyTest.java            # Missing key policies
│
├── datahandling/                 # Data type handling
│   ├── NullSafetyTest.java                  # Null value handling
│   ├── OptionalSupportTest.java             # Optional support
│   ├── EnumHandlingTest.java                # Enum support
│   ├── PublicFieldTest.java                 # Public field access
│   ├── GoStyleFieldNameTest.java            # Go-style field names
│   ├── TemplateFieldAnnotationTest.java     # @TemplateField
│   └── MapKeySortingTest.java               # Map iteration order
│
├── compatibility/                # Go compatibility and deviations
│   ├── GoCompatibilityAuditTest.java        # Compatibility audit
│   ├── GoCompatibilityFixtureTest.java      # Fixture tests
│   ├── JavaDeviationFixtureTest.java        # Java-specific deviations
│   └── RootVariableCompatibilityTest.java   # $ variable compatibility
│
├── advanced/                     # Advanced features
│   ├── CustomDelimiterTest.java             # Custom delimiters
│   ├── NestedTemplateContextTest.java       # Nested contexts
│   ├── IntegerRangeTest.java                # Integer range syntax
│   ├── RangeIndexTest.java                  # Range with index
│   ├── PipeNodeVariableTest.java            # Pipe node variables
│   └── TemplateInputValidationTest.java     # Input validation
│
├── internal/                     # Internal component tests (keep existing structure)
│   ├── lexer/                                 # Lexer tests
│   │   ├── LexerLiteralTest.java
│   │   ├── LexerTextAndActionTest.java
│   │   ├── LexerVariablePipelineTest.java
│   │   ├── LexerTrimDelimiterTest.java
│   │   ├── LexerPositionTest.java
│   │   ├── LexerErrorTest.java
│   │   └── LexerTestSupport.java
│   │
│   ├── parser/                                # Parser tests
│   │   ├── ParserCanonicalTest.java
│   │   ├── ParserBranchScopeTest.java
│   │   ├── ParserTemplateDefinitionTest.java
│   │   ├── ParserPipelineCommandTest.java
│   │   ├── ParserNumberTest.java
│   │   ├── ParserErrorContextTest.java
│   │   ├── ParserLegacyErrorTest.java
│   │   └── ParserTestSupport.java
│   │
│   ├── executor/                              # Executor tests
│   │   └── ExecutorTest.java
│   │
│   ├── ast/                                   # AST node tests
│   │   └── ASTNodeTest.java
│   │
│   └── lang/                                  # Language utility tests
│       ├── CharUtilsTest.java
│       ├── ComplexTest.java
│       ├── StringUtilsTest.java
│       ├── ErrorUtilsTest.java
│       └── StringEscapeUtilsTest.java
│
└── util/                         # Test utilities
    ├── TemplateTestSupport.java               # Base test class
    ├── Recipient.java                         # Test data model
    └── TemplateJmhBenchmark.java              # Performance benchmarks
```

**Naming Convention Standards**:

1. **Component Tests** (internal implementation):
   - Pattern: `{Component}Test.java`
   - Examples: `LexerTest.java`, `ParserTest.java`, `ExecutorTest.java`
   - Location: `internal/{component}/`

2. **Feature Tests** (Go template features):
   - Pattern: `{Feature}FeatureTest.java`
   - Examples: `RangeFeatureTest.java`, `IfFeatureTest.java`, `BlockFeatureTest.java`
   - Location: `features/`

3. **Function Tests** (built-in functions):
   - Pattern: `{Category}FunctionsTest.java`
   - Examples: `CollectionFunctionsTest.java`, `ComparisonFunctionsTest.java`
   - Location: `functions/`

4. **API Tests** (public API surface):
   - Pattern: `{Component}ApiTest.java` or `{Scenario}Test.java`
   - Examples: `TemplateApiTest.java`, `TemplateFileHelperTest.java`
   - Location: `api/`

5. **Error Handling Tests**:
   - Pattern: `{ErrorType}ErrorTest.java` or `{Scenario}ErrorTest.java`
   - Examples: `ParseErrorTest.java`, `EnhancedFieldErrorTest.java`
   - Location: `errorhandling/`

6. **Compatibility Tests**:
   - Pattern: `{Standard}CompatibilityTest.java` or `{Scenario}Test.java`
   - Examples: `GoCompatibilityAuditTest.java`, `JavaDeviationFixtureTest.java`
   - Location: `compatibility/`

7. **Test Utilities**:
   - Pattern: `{Name}TestSupport.java` or `{Name}Fixture.java`
   - Examples: `TemplateTestSupport.java`, `ParserTestSupport.java`
   - Location: `util/` or alongside component tests

**Migration Strategy**:

The migration will be performed in phases to minimize risk and allow continuous verification:

| Phase | Task | Files Affected | Effort | Verification |
| :--- | :--- | :--- | :--- | :--- |
| **Phase 1** | Audit & Inventory | All 70 test files | 0.5 day | Document current state |
| **Phase 2** | Create Package Structure | New directories | 0.5 day | Verify directory creation |
| **Phase 3** | Move Internal Tests | `internal/*` (20 files) | 0.5 day | Run tests, verify no regressions |
| **Phase 4** | Consolidate TemplateExecution Tests | 6 → 1 file | 1 day | Merge tests, run all |
| **Phase 5** | Consolidate Functions Tests | 6 → 6 categorized files | 1 day | Reorganize by category |
| **Phase 6** | Organize Feature Tests | ~15 files | 1.5 days | Group by Go template feature |
| **Phase 7** | Organize Error Handling Tests | ~8 files | 0.5 day | Group by error type |
| **Phase 8** | Organize Data Handling Tests | ~7 files | 0.5 day | Group by data type |
| **Phase 9** | Organize Compatibility Tests | ~4 files | 0.5 day | Group by compatibility aspect |
| **Phase 10** | Move Test Utilities | 3 files | 0.5 day | Update imports |
| **Phase 11** | Final Verification | All tests | 1 day | Full test suite + coverage check |

**Total Estimated Effort**: 8-9 days (can be done incrementally over 2-3 weeks)

**Benefits**:

1. **Improved Discoverability**:
   - Easy to find tests for specific components or features
   - Clear separation between unit, feature, and integration tests
   - Logical grouping reduces cognitive load

2. **Better Coverage Tracking**:
   - JaCoCo reports show coverage by package
   - Easier to identify untested areas
   - Can track coverage trends by component

3. **Simplified Maintenance**:
   - Related tests co-located
   - Easier to update tests when modifying components
   - Reduced merge conflicts in large teams

4. **Clearer Separation of Concerns**:
   - Internal implementation tests isolated from public API tests
   - Feature tests independent of implementation details
   - Compatibility tests clearly marked

5. **Improved Onboarding**:
   - New contributors can quickly understand test structure
   - Easier to add tests for new features
   - Better documentation through organization

6. **CI/CD Optimization**:
   - Can run subset of tests by package
   - Faster feedback for specific changes
   - Parallel test execution by package

**Risks & Mitigation**:
- **Risk**: Breaking existing CI/CD pipelines during migration
  - **Mitigation**: Perform migration in small, incremental steps with continuous verification
- **Risk**: Losing test coverage during reorganization
  - **Mitigation**: Maintain >90% coverage threshold throughout migration
- **Risk**: Time investment without immediate feature benefits
  - **Mitigation**: Schedule during low-priority periods, emphasize long-term maintainability gains

**Success Criteria**:
- All existing tests pass after reorganization
- Code coverage remains ≥90%
- Clear package structure with logical grouping
- Consistent naming conventions applied throughout
- Documentation updated to reflect new test organization
- Migration completed without functionality changes

**Estimated Total Effort**: 8-10 days (can be done incrementally)

---

### API Enhancements

#### Method Invocation with Arguments 🔴 SECURITY REVIEW REQUIRED
**Priority**: Low (Deferred - requires comprehensive security design)
**Status**: Not started

**Description**: Allow templates to call Java methods with arguments (e.g., `{{.user.getName("full")}}`).

**Concerns**:
- ⚠️ **Security Risk**: Arbitrary method invocation could expose dangerous operations
- ⚠️ **Access Control**: Need whitelist/blacklist mechanism or annotation-based exposure
- ⚠️ **Type Safety**: Parameter type conversion and validation complexity
- ⚠️ **Backward Compatibility**: May change existing template behavior

**Proposed Approach**:
1. Conduct thorough security analysis
2. Design safe invocation patterns (e.g., `@TemplateCallable` annotation)
3. Implement access control mechanisms
4. Create RFC document for community review

**Alternative**: Continue using `Function` interface for controlled method exposure (current approach)

**Estimated Effort**: 5-7 days (including security design)

---

#### Template Pre-compilation 🔵 LOW PRIORITY
**Priority**: Low (Defer to v1.0+)
**Status**: Not started

**Description**: Compile templates to Java bytecode for maximum execution performance.

**Potential Benefits**:
- 2-5x performance improvement for hot paths
- Reduced memory footprint (no AST overhead)
- Faster startup for pre-compiled templates

**Challenges**:
- High implementation complexity (code generation)
- Additional build step required
- Debugging becomes harder
- Current performance already excellent (executeBenchmark: 1.79M ops/sec)

**Implementation Options**:
1. Generate Java source code from AST
2. Compile to bytecode at runtime or build time
3. Support serialization/deserialization of compiled templates

**Decision Criteria**:
- Wait for actual performance bottlenecks in production use
- Evaluate cost-benefit ratio after gathering real-world usage data

**Estimated Effort**: 5-10 days

---

### Feature Improvements

#### Enhanced Template Inheritance 🟡 MEDIUM PRIORITY
**Priority**: Medium (User feedback driven)
**Status**: Not started

**Description**: Extend current block/define/template mechanism with additional inheritance features.

**Current Capabilities**:
- ✅ `{{define "name"}}...{{end}}` - Template definition
- ✅ `{{template "name" .}}` - Template invocation
- ✅ `{{block "name" .}}Default{{end}}` - Inline definition with override
- ✅ Multi-level inheritance through parsing order

**Potential Enhancements**:
1. **Layout Composition Patterns**
    - Declarative extends syntax (e.g., `{{extends "base.tmpl"}}`)
    - Automatic file loading based on inheritance declarations
    - Namespace isolation for large projects

2. **Cross-file Inheritance Chains**
    - Simplify multi-file template organization
    - Reduce manual `parseFiles()` calls

3. **Block Nesting Improvements**
    - Better support for deeply nested overrides
    - Named slot system (similar to Vue/React slots)

**User Feedback Needed**:
- Are current mechanisms sufficient for most use cases?
- What pain points exist in large-scale template management?
- Would declarative inheritance simplify workflows?

**Estimated Effort**: 3-5 days

---

### Testing & Quality

#### Mutation Testing (PITest) 🔵 DEFERRED
**Priority**: Low
**Status**: Deferred

**Description**: Use mutation testing to verify test suite effectiveness.

**Benefits**:
- Identify untested code paths
- Improve test quality beyond coverage metrics
- Catch false positives in test assertions

**Challenges**:
- Requires external dependency (pitest-maven)
- May slow down CI/CD pipeline
- Complex to configure for optimal results

**Decision**: Defer until core functionality stabilizes

---

#### Property-Based Testing 🔵 DEFERRED
**Priority**: Low
**Status**: Deferred

**Description**: Use property-based testing frameworks (e.g., jqwik) for edge case discovery.

**Use Cases**:
- Template parsing with random inputs
- Edge case data models (null, empty, extreme values)
- Unicode and special character handling

**Decision**: Defer; current test coverage is sufficient

---

#### Static Analysis Integration (SpotBugs/PMD) 🔵 DEFERRED
**Priority**: Low
**Status**: Deferred

**Description**: Integrate static analysis tools into build pipeline.

**Benefits**:
- Catch potential bugs early
- Enforce code quality standards
- Identify performance anti-patterns

**Challenges**:
- Requires external dependencies
- May produce false positives
- Configuration complexity

**Current Alternative**: Manual code reviews and IDE inspections
