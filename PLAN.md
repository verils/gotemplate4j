# gotemplate4j Development Plan

**Last Updated**: 2026-05-23  
**Current Version**: 0.9.1 (Released)  
**Next Version**: 0.10.0 (Java 11 upgrade planned)  
**Current Focus**: v0.10.0 - Java 11 migration, modernization, and compatibility polish

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

## Version Plans

### 📋 Upcoming: v0.10.0 Release Plan

**Positioning**: v0.10.0 is the Java 11 baseline release. It should keep API compatibility where practical while using the runtime upgrade to clean up Java 8-era code and finish a focused set of Go `text/template` compatibility improvements.

**Important Scope Note**: gotemplate4j implements Go's `text/template` package only, NOT `html/template`. Features like contextual auto-escaping, XSS prevention, and HTML-specific security mechanisms are intentionally out of scope. Users needing HTML safety should use the existing `{{html .value}}` function or pre-process data in Java code.

#### Goals

- Migrate to Java 11 minimum requirement
- Modernize codebase with low-risk Java 11 APIs and remove Java 8 compatibility workarounds
- Preserve public API compatibility unless a breaking change is required by the Java 11 baseline
- Improve Go `text/template` compatibility in small, well-tested areas
- Improve developer diagnostics for parse and execution failures
- Keep documentation synchronized across README.md and README_zh.md

#### Non-Goals

- Do not break API backward compatibility
- Do not add runtime dependencies beyond vanilla Java
- Do not implement method invocation with arguments (requires extensive security design)
- Do not start template pre-compilation (deferred to v1.0+)
- Do not migrate to Java 17+ in this release
- Do not perform a broad parser, executor, or AST rewrite unless required for a scoped compatibility fix

#### v0.10.0 Work Scope

**Required: Java 11 Baseline**
- Update Maven compiler settings and build plugins for Java 11
- Update local development, CI, and release documentation to use Java 11+
- Document that v0.9.x is the final Java 8-compatible release line
- Run full verification with `./mvnw clean verify "-Dgpg.skip=true"`

**Required: Documentation and Release Hygiene**
- Update README.md and README_zh.md together for Java 11 requirements
- Keep version-specific release notes out of README files
- Document any known Go `text/template` differences that remain after v0.10.0
- Keep production/experimental warnings consistent across translated docs

**Recommended: Compatibility Polish**
- Audit and complete common built-in function behavior where gaps are small and testable
- Add focused pipeline compatibility tests for function chaining, variable assignment, and parenthesized pipelines
- Improve parse/execution error context, especially missing function and invalid field/key diagnostics
- Clarify or tighten complex number support; if not completed, document the remaining limitation

**Recommended: Java 11 Modernization**
- Prefer targeted use of Java 11 APIs such as `String.isBlank()`, `strip()`, and immutable collection factories where they reduce code complexity
- Avoid broad stylistic rewrites such as converting large areas to `var`
- Avoid introducing Java 17-only language features such as records or pattern matching
- Preserve readable, explicit code in parser and executor hot paths

**Optional: Template Definition Compatibility**
- Review `define`, `template`, and `block` behavior against Go `text/template`
- Fix only well-bounded discrepancies with focused tests
- Defer larger template inheritance or composition redesign to a later version

---

### Next Version: v0.10.0 - Java 11 Migration and Compatibility Polish

#### Overview

Starting from version 0.10.0, gotemplate4j will require **Java 11 or higher**. This strategic decision enables:
- Access to modern Java 11 language and library features
- Better runtime behavior with modern JVM improvements
- Alignment with industry standards (Java 8 reached end of public updates in 2019)
- Simplified codebase maintenance

#### Migration Timeline

```
v0.8.0 (Released) → Java 8, Quality improvements + Performance optimization
v0.9.1 (Current)  → Java 8, bug fixes + Compatibility testing
v0.10.0 (Next)    → Java 11+, Modernization + Compatibility polish
```

#### What Changes in v0.10.0?

**Breaking Changes:**
- Minimum Java version: 8 → 11
- No API changes planned (backward compatible at API level)
- Runtime requirement only

**Code Improvements:**
- Use Java 11 APIs where they simplify existing code
- Leverage `List.of()`, `Map.of()` for small immutable collections where mutation is not required
- Use modern String methods such as `isBlank()`, `strip()`, and `repeat()`
- Remove Java 8 compatibility workarounds
- Avoid broad style-only rewrites that increase review risk

**Performance Benefits:**
- G1 Garbage Collector (default in Java 11)
- Improved JIT compilation
- Better memory management
- Potential runtime improvements depending on workload and JVM configuration

**Compatibility Improvements:**
- Focused built-in function behavior fixes
- Pipeline execution compatibility tests and small fixes
- Better parser and executor diagnostics
- Clear documentation for any remaining Go `text/template` differences

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

### v0.9.0 Release - Incomplete Tasks

The following tasks from v0.9.0 release planning were not completed and are moved to backlog:

#### Backward Compatibility Verification 🔴 HIGH PRIORITY
**Priority**: High
**Status**: Not started

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

**Success Criteria**:
- [ ] All 823+ tests pass
- [ ] Code coverage ≥90%
- [ ] No breaking changes detected

**Estimated Time**: 0.5 day

---

#### Documentation Updates 🟡 MEDIUM PRIORITY
**Priority**: Medium
**Status**: Not started

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

**Estimated Time**: 1 day

---

#### Error Diagnostics Enhancements 🔴 HIGH PRIORITY
**Priority**: High
**Status**: Partially complete

**Remaining Items from Stage 3**:

1. **Function Call Error Details** 🔴 MISSING
   - [ ] Show expected function signature
   - [ ] Display actual arguments provided
   - [ ] List available functions when calling undefined function

2. **Execution Error Context** - OPTIONAL
   - [ ] Show data type information for better understanding

3. **Intelligent Suggestions** - PARTIAL
   - [ ] Provide helpful hints for common mistakes

**Estimated Time**: 1-2 days

---

#### Final Release Preparation 🔴 HIGH PRIORITY
**Priority**: High
**Status**: Not started

**Objective**: Prepare v0.9.0 for release.

**Checklist**:
- [ ] Update version in pom.xml: `0.8.0` → `0.9.0`
- [ ] Update CHANGELOG with v0.9.0 release notes
- [ ] Update PLAN.md "Current Progress" section
- [ ] Create git tag: `v0.9.0`
- [ ] Prepare GitHub release notes
- [ ] Verify Maven Central deployment process

**Estimated Time**: 0.5 day

---

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
