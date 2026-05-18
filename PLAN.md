# gotemplate4j Development Plan

**Last Updated**: 2026-05-18  
**Current Version**: 0.9.0 (in development)  
**Next Version**: 0.10.0 (Java 11 upgrade planned)  
**Current Focus**: v0.9.0 - Java 8 final release with practical enhancements and Go compatibility improvements

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

4. **Intelligent Suggestions** 🟡 PARTIAL
   - [x] Detect typos in field names and suggest corrections (**COMPLETED - P1**)
   - [ ] Show similar map keys when a key is not found (**MISSING - P2**)
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
| **P2** | Enhance Map Key Errors | Update `Executor.handleMissingMapKey` to list available keys and suggest corrections. | 0.5 day | ⏳ NEXT |
| **P3** | Enhance Function Errors | Improve `Executor.executeFunction` to show argument mismatches and list defined functions. | 1 day | 🔲 PENDING |
| **P4** | Testing & Polish | Add comprehensive tests for new error formats and ensure backward compatibility. | 1 day | 🔄 IN PROGRESS |

**Completed Work (P1):**
- ✅ Enhanced `Executor.executeFieldPath` to display available fields on error
- ✅ Integrated `ErrorUtils` for intelligent typo suggestions
- ✅ Filtered out Object class methods from suggestions (hashCode, toString, etc.)
- ✅ Lowered similarity threshold to 0.5 for better typo detection
- ✅ Added case-insensitive exact match check before fuzzy matching
- ✅ Created comprehensive test suite (6 test cases in `EnhancedFieldErrorTest`)
- ✅ All 807 tests passing, code coverage maintained >90%

**Example Output:**
```
Before: can't evaluate field FristName
After:  can't evaluate field User.FristName. Available fields: [age, firstName, getAge, getFirstName, getName, name] Did you mean 'firstName'?
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

**Estimated Effort**: 3-4 days total  
**Progress**: ~50% complete (P0, P0.5, P1 done; P2, P3 pending)

#### Stage 4: Testing & Documentation 🔲 PENDING

Comprehensive testing and documentation for all new features:

**Testing Requirements:**
- File loading API tests (classpath, directory, encoding)
- Error diagnostics accuracy tests
- Backward compatibility tests
- Cross-platform file path tests

**Documentation Updates:**
- Add new API usage examples for file loading
- Document classpath resource loading best practices
- Enhance User Guide with file loading patterns
- Update FAQ with integer range syntax examples
- Update API reference documentation

**Performance Validation:**
- Run JMH benchmarks
- Ensure no performance regression from new features
- Validate file loading performance

**Completion Criteria:**
- All new features have complete test coverage
- Code coverage maintains ≥90%
- All existing tests pass
- JMH benchmarks show no degradation
- Documentation fully updated
- `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8
- No backward compatibility breaks

**Estimated Effort**: 1-2 days

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

#### Test Case Reorganization 🟡 MEDIUM PRIORITY
**Priority**: Medium (Improve maintainability and clarity)
**Status**: Planning phase

**Description**: Reorganize and restructure the test suite to improve maintainability, clarity, and coverage tracking.

**Current Issues**:
- Tests are scattered across multiple files with inconsistent naming patterns
- Some test classes mix different concerns (e.g., `TemplateExecutionBasicTest`, `TemplateExecutionConditionalTest`, etc.)
- Lack of clear categorization between unit tests, integration tests, and compatibility tests
- Inconsistent test data setup patterns
- Difficulty in identifying which Go template features are covered

**Proposed Organization**:

1. **By Component**:
   ```
   src/test/java/io/github/verils/gotemplate/
   ├── lexer/           # Lexer-specific tests
   ├── parser/          # Parser-specific tests  
   ├── executor/        # Executor-specific tests
   ├── ast/             # AST node tests
   ├── functions/       # Built-in function tests
   └── api/             # Public API tests
   ```

2. **By Feature**:
   ```
   src/test/java/io/github/verils/gotemplate/features/
   ├── control-flow/    # if, range, with, block tests
   ├── variables/       # Variable access and pipeline tests
   ├── templates/       # Template definition and inclusion tests
   ├── inheritance/     # Block override and inheritance tests
   └── data-types/      # Different data type handling tests
   ```

3. **By Compatibility**:
   ```
   src/test/java/io/github/verils/gotemplate/compatibility/
   ├── go-text-template/    # Go text/template compatibility tests
   ├── java-deviations/     # Intentional Java-specific deviations
   └── edge-cases/          # Edge cases and boundary conditions
   ```

**Naming Convention Standards**:
- Unit tests: `{Component}Test.java` (e.g., `LexerTest.java`, `ParserTest.java`)
- Feature tests: `{Feature}FeatureTest.java` (e.g., `RangeFeatureTest.java`, `IfFeatureTest.java`)
- Integration tests: `{Scenario}IntegrationTest.java` (e.g., `EmailTemplateIntegrationTest.java`)
- Compatibility tests: `{Standard}CompatibilityTest.java` (e.g., `GoTextTemplateCompatibilityTest.java`)

**Implementation Plan**:

| Phase | Task | Description | Estimated Effort |
| :--- | :--- | :--- | :--- |
| **Phase 1** | Audit existing tests | Catalog all current tests, identify overlaps and gaps | 1 day |
| **Phase 2** | Design new structure | Define package structure and migration strategy | 0.5 day |
| **Phase 3** | Create new packages | Set up new directory structure and base test classes | 0.5 day |
| **Phase 4** | Migrate lexer tests | Move and reorganize lexer-related tests | 0.5 day |
| **Phase 5** | Migrate parser tests | Move and reorganize parser-related tests | 1 day |
| **Phase 6** | Migrate executor tests | Move and reorganize executor-related tests | 1 day |
| **Phase 7** | Migrate feature tests | Reorganize feature-specific tests by category | 1.5 days |
| **Phase 8** | Migrate compatibility tests | Organize Go compatibility and deviation tests | 1 day |
| **Phase 9** | Update test utilities | Refactor `TemplateTestSupport` and helper classes | 0.5 day |
| **Phase 10** | Verify and validate | Run all tests, ensure coverage maintained, fix issues | 1 day |

**Benefits**:
- Easier to locate tests for specific components or features
- Better visibility into test coverage by category
- Simplified maintenance when modifying specific components
- Clearer separation of concerns (unit vs integration vs compatibility)
- Improved onboarding experience for new contributors
- Easier to identify missing test coverage areas

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
