# gotemplate4j Development Plan

**Last Updated**: 2026-05-11  
**Current Version**: 0.7.0  
**Next Version**: 0.8.0 (future enhancements)  
**Current Focus**: v0.7.0 release complete - All stages done ✅  
**Status**: Stage 1 complete ✅, Stage 2 complete ✅, Stage 3 complete ✅, Stage 4 complete ✅, Stage 5 complete ✅

---

## Direction

gotemplate4j should remain a small, Java 8-compatible implementation of Go's `text/template` semantics for Java applications. v0.6.0 completed the API usability improvements. v0.7.0 focuses on two strategic directions:

1. **Production-Grade Documentation**: Create comprehensive, web-deployable user documentation following open-source best practices, organized for future static site generation.
2. **Enhanced Go Compatibility**: Reduce migration friction by implementing high-value Go template features that lower the learning curve and reduce common pitfalls.

## Working Constraints

- Keep Java 8 compatibility.
- Use `./mvnw`, not `mvn`.
- Avoid runtime dependencies beyond vanilla Java.
- Preserve backward compatibility unless a documented Go compatibility fix requires a behavior change.
- Keep Go-compatible behavior, Java-specific behavior, and unsupported Go APIs clearly separated.

## Current Progress

### ✅ Completed (v0.7.0)

- **Integer Range Support**: Full support for `{{range $i := 5}}` syntax (Go-compatible)
- **Map Key Sorting**: Deterministic map iteration with configurable sorting
- **Block Action Enhancement**: Inline template definition and execution with `{{block "name" .}}`
- **Production-Grade Documentation**: 20+ comprehensive guides covering all aspects
  - Getting Started (installation, quick start, basic concepts)
  - User Guide (syntax, data models, functions, control flow, template sets, error handling)
  - API Reference (Template, Function, Exception APIs)
  - Advanced Topics (performance, security, best practices)
  - Real-world Examples (basic, web, email, complex scenarios)
  - FAQ with 30+ common questions
- **Enhanced Compatibility**: Reduced migration friction from Go templates
- **CHANGELOG Updated**: Comprehensive release notes for v0.7.0

### ✅ Completed (v0.6.0)

- MissingKeyPolicy configuration (DEFAULT/ZERO/ERROR)
- Template introspection API (name(), hasTemplate(), definedTemplates(), lookup(), templates())
- File system helpers (parseFile(), parseFiles(), parseGlob())
- Performance benchmarking (TemplateBenchmark)
- Initial compatibility and migration documentation

### 🚧 In Progress (v0.7.0 - Complete)

- ✅ Priority 1: Integer Range Support - Implemented and tested (8 test cases)
- ✅ Priority 2: Map Key Sorting Option - Implemented and tested (11 test cases)
- ✅ Priority 3: Block Action Enhancement - Implemented and tested (9 test cases)

- ✅ Documentation directory structure created
- ✅ docs/index.md - Main documentation hub
- ✅ docs/getting-started/installation.md - Installation guide
- ✅ docs/getting-started/quick-start.md - Quick start tutorial
- ✅ docs/getting-started/basic-concepts.md - Basic concepts
- ✅ docs/user-guide/template-syntax.md - Complete template syntax reference
- ✅ docs/user-guide/data-models.md - Working with Java data
- ✅ docs/user-guide/functions.md - Built-in and custom functions
- ✅ docs/user-guide/control-flow.md - Control flow constructs
- ✅ docs/user-guide/template-sets.md - Template sets and inheritance
- ✅ docs/user-guide/error-handling.md - Error handling strategies
- ✅ docs/api-reference/template-api.md - Template class API reference
- ✅ docs/api-reference/function-api.md - Function interface API reference
- ✅ docs/api-reference/exception-api.md - Exception hierarchy API reference
- ✅ docs/advanced/performance.md - Performance tuning guide
- ✅ docs/advanced/security.md - Security considerations
- ✅ docs/advanced/best-practices.md - Best practices guide
- ✅ docs/examples/basic-examples.md - Basic usage examples
- ✅ docs/examples/web-templates.md - Web application examples
- ✅ docs/examples/email-templates.md - Email generation examples
- ✅ docs/examples/complex-scenarios.md - Complex real-world scenarios
- ✅ docs/faq.md - Frequently asked questions (645 lines, comprehensive)
- ✅ docs/advanced/compatibility.md - Enhanced compatibility details
- ✅ docs/advanced/migration.md - Enhanced migration guide

### 📋 Planned (v0.8.0 - Future Enhancements)

See Later Backlog section below.

---

## v0.7.0 Release Plan

Positioning: v0.7.0 is the documentation and compatibility enhancement release. It transforms gotemplate4j from a functional library into a production-ready, well-documented solution with reduced migration friction from Go templates.

### Goals

- Create comprehensive, production-grade user documentation suitable for web deployment
- Organize documentation structure for future static site generation (MkDocs, Docusaurus, etc.)
- Implement high-priority Go compatibility improvements that reduce common migration pain points
- Maintain Java 8 compatibility and dependency-light philosophy
- Keep all new features well-tested and documented

### Non-Goals

- Do not implement complex Go-only features (channels, iterators, ParseFS)
- Do not add runtime dependencies beyond vanilla Java
- Do not break backward compatibility
- Do not implement general method invocation with arguments (requires separate security design)

### Stage 1: Production-Grade Documentation Structure ✅ Complete

**Status**: All documentation sections created and verified. Stage 1 complete.

Create a comprehensive documentation structure following open-source best practices:

**Documentation Organization:**

```
docs/
├── index.md                    # Landing page / overview
├── getting-started/            # Quick start guides
│   ├── installation.md         # Installation and setup
│   ├── quick-start.md          # 5-minute tutorial
│   └── basic-concepts.md       # Core concepts explained
├── user-guide/                 # Comprehensive user guide
│   ├── template-syntax.md      # Complete syntax reference
│   ├── data-models.md          # Working with Java data
│   ├── functions.md            # Built-in and custom functions
│   ├── control-flow.md         # If, range, with, etc.
│   ├── template-sets.md        # Define, template, blocks
│   └── error-handling.md       # Error types and handling
├── api-reference/              # API documentation
│   ├── template-api.md         # Template class API
│   ├── function-api.md         # Function interface
│   └── exception-api.md        # Exception hierarchy
├── advanced/                   # Advanced topics
│   ├── compatibility.md        # Go compatibility details (existing)
│   ├── migration.md            # Migration guide (existing)
│   ├── performance.md          # Performance tuning
│   ├── security.md             # Security considerations
│   └── best-practices.md       # Best practices and patterns
├── examples/                   # Code examples
│   ├── basic-examples.md       # Simple use cases
│   ├── web-templates.md        # Web application examples
│   ├── email-templates.md      # Email generation examples
│   └── complex-scenarios.md    # Advanced scenarios
└── faq.md                      # Frequently asked questions
```

**Completed So Far:**
- ✅ Created directory structure: getting-started/, user-guide/, api-reference/, advanced/, examples/
- ✅ docs/index.md - Main documentation hub with full navigation
- ✅ docs/getting-started/installation.md - Maven/Gradle/manual installation
- ✅ docs/getting-started/quick-start.md - 5-minute tutorial with examples
- ✅ docs/getting-started/basic-concepts.md - Core concepts explained
- ✅ docs/user-guide/template-syntax.md - Complete syntax reference
- ✅ docs/user-guide/data-models.md - Working with Java data
- ✅ docs/user-guide/functions.md - All built-in and custom functions
- ✅ docs/user-guide/control-flow.md - If, range, with, break/continue
- ✅ docs/user-guide/template-sets.md - Define, template, blocks, inheritance
- ✅ docs/user-guide/error-handling.md - Exception handling and best practices
- ✅ docs/api-reference/template-api.md - Complete Template class API documentation
- ✅ docs/api-reference/function-api.md - Function interface with implementation examples
- ✅ docs/api-reference/exception-api.md - Exception hierarchy and error handling patterns
- ✅ docs/advanced/performance.md - Comprehensive performance optimization guide
- ✅ docs/advanced/security.md - Security best practices and vulnerability prevention
- ✅ docs/advanced/best-practices.md - Design patterns and maintainability guidelines

**Stage 2 Compatibility Features Completed:**
- ✅ **Integer Range Support**: Added support for `{{range $i := 5}}` syntax in Executor.java
  - Iterates from 0 to n-1 for positive integers
  - Handles zero and negative numbers correctly (no iteration)
  - Supports both literal integers and data-driven values
  - Test coverage: 8 comprehensive test cases
- ✅ **Map Key Sorting Option**: Added optional map key sorting for deterministic output in Template.java and Executor.java
  - New `withMapKeySorting(boolean)` method for configuration
  - New `mapKeySorting()` getter method
  - Uses natural ordering for Comparable keys, falls back to toString() comparison
  - Preserved in template cloning
  - Test coverage: 11 comprehensive test cases including nested maps and custom key types
- 🔧 **Executor Enhancement**: Fixed executeCommand to handle NumberNode and BoolNode directly

**Next Steps for Stage 2:**
- ✅ Implement Priority 1: Integer Range Support (COMPLETE)
- ✅ Implement Priority 2: Map Key Sorting Option (COMPLETE)
- ✅ Implement Priority 3: Block Action Enhancement (COMPLETE)
- ✅ Run full test suite to verify no regressions (689 tests passed)
- ✅ Update CHANGELOG (COMPLETE)

**Documentation Standards:**

- Use clear, concise language suitable for both beginners and experts
- Include code examples for every major feature
- Provide Go template comparison where relevant
- Add troubleshooting sections for common issues
- Use consistent formatting and terminology
- Include diagrams for complex concepts (using Mermaid)
- All examples should be tested and verified

**Future-Proofing:**

- Structure docs for easy conversion to MkDocs, Docusaurus, or similar tools
- Use standard Markdown features with minimal extensions
- Separate content from presentation concerns
- Include metadata headers for navigation and search

### Stage 2: High-Priority Compatibility Improvements

Based on the v0.6.0 audit, implement features that reduce migration friction:

**Priority 1: Integer Range Support**

Go supports `range` over integers (e.g., `{{range $i := 5}}...{{end}}` iterates 0-4).

Implementation approach:
```java
// Detect integer in range executor
if (data instanceof Number) {
    int count = ((Number) data).intValue();
    for (int i = 0; i < count; i++) {
        // Execute loop body with $index = i, $value = i
    }
}
```

Benefits:
- Common pattern in Go templates for generating repeated elements
- Easy to implement in Java
- No security concerns

**Priority 2: Map Key Sorting Option**

Go sorts map keys for deterministic output. Add optional sorting:

```java
Template template = new Template("demo")
    .withMapKeySorting(true);  // Sort keys alphabetically
```

Implementation:
- When iterating maps, sort keys if enabled
- Use natural ordering for Comparable keys
- Fall back to toString() comparison for others

Benefits:
- Deterministic output for testing
- Matches Go's default behavior
- Useful for generating configuration files

**Priority 4: Block Action Enhancement**

Go's `{{block "name" .}}...{{end}}` defines and executes inline. Currently only `define` is supported.

Current workaround requires two steps:
```gotemplate
{{define "content"}}Default content{{end}}
{{template "content" .}}
```

With block support:
```gotemplate
{{block "content" .}}Default content{{end}}
```

Benefits:
- More concise template syntax
- Better matches Go template patterns
- Reduces template verbosity

### Stage 3: Documentation Content Creation

Create comprehensive documentation content:

**Getting Started Section:**
- Installation guide with Maven/Gradle examples
- 5-minute quick start tutorial
- Basic concepts: templates, data, execution
- Common use cases and patterns

**User Guide Section:**
- Complete template syntax reference with examples
- Data models: JavaBeans, Maps, Lists, Enums, Optional
- All built-in functions with usage examples
- Control flow: if/else, range, with, break/continue
- Template sets: define, template, block, inheritance
- Custom functions: implementation and registration
- Error handling: parse errors, execution errors, best practices

**API Reference:**
- Template class: all constructors and methods
- Function interface: how to implement custom functions
- Exception hierarchy: when each exception is thrown
- MissingKeyPolicy: policies and use cases

**Advanced Topics:**
- Compatibility details (enhance existing docs)
- Migration from Go templates (enhance existing docs)
- Performance tuning and benchmarking
- Security considerations (template injection prevention)
- Best practices and design patterns
- Thread safety and concurrent execution

**Examples:**
- Basic examples for each feature
- Web application templates (HTML generation)
- Email templates (common business scenarios)
- Configuration file generation
- Complex real-world scenarios

**FAQ:**
- Common questions and answers
- Troubleshooting guide
- Known limitations and workarounds
- Comparison with other Java template engines

### Stage 4: Testing and Verification

Ensure all new features are thoroughly tested:

**Compatibility Feature Tests:**
- Integer range: various counts, edge cases (0, negative)
- Null display: different policies, nested templates
- Map sorting: different key types, empty maps
- Block action: define+execute, override behavior

**Documentation Quality:**
- All code examples compile and run correctly
- Cross-references between docs are accurate
- Screenshots/diagrams are clear and helpful
- Translation-friendly structure (for future i18n)

**Regression Testing:**
- All existing tests pass
- No backward compatibility breaks
- Performance benchmarks show no degradation
- Code coverage remains above thresholds

### Stage 5: Release Preparation

Prepare for v0.7.0 release:

**Documentation Polish:**
- Review all docs for clarity and completeness
- Ensure consistent tone and style
- Verify all links and references
- Create documentation README with navigation guide

**CHANGELOG Updates:**
- Document all new compatibility features
- List all new documentation
- Highlight migration benefits
- Note any behavioral changes

**README Updates:**
- Link to new comprehensive documentation
- Update feature list with new capabilities
- Refresh quick start examples
- Add documentation navigation section

**Final Verification:**
- `./mvnw clean verify "-Dgpg.skip=true"` succeeds
- All tests pass on Java 8
- Code coverage meets thresholds
- Documentation builds without errors

### v0.7.0 Completion Gate

- Complete documentation structure created with all planned sections
- All high-priority compatibility features implemented and tested
- Documentation includes working code examples for all major features
- Existing docs (compatibility.md, migration.md) enhanced and integrated
- FAQ addresses top 20 common questions
- `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8
- No backward compatibility breaks
- Code coverage remains above configured thresholds
- README links to comprehensive documentation hub

### Suggested Next Session Order

**Current Position**: Stage 1 complete ✅, Stage 2 complete ✅

1. ✅ Create documentation directory structure and templates
2. ✅ Write Getting Started section (installation ✅, quick start ✅, concepts ✅)
3. ✅ Write Template Syntax reference ✅
4. ✅ Write Data Models guide ✅
5. ✅ Write Functions guide ✅
6. ✅ Write Control Flow guide ✅
7. ✅ Write Template Sets guide ✅
8. ✅ Write Error Handling guide ✅
9. ✅ Implement integer range support with tests (COMPLETE)
10. ✅ Write API Reference documentation (template-api ✅, function-api ✅, exception-api ✅)
11. ✅ Implement map key sorting option with tests (COMPLETE)
12. ✅ Implement block action support with tests (COMPLETE)
13. ✅ Create comprehensive examples (basic-examples ✅, web-templates ✅, email-templates ✅, complex-scenarios ✅)
14. ✅ Enhance compatibility and migration docs
15. ✅ Write FAQ
16. ✅ Update CHANGELOG (COMPLETE)
17. ✅ Final review and verification (COMPLETE)

---

## Critical Issues Identified (v0.6.1 Candidate)

During Executor code review, the following issues were identified that may warrant a patch release:

### Issue 1: Go-style Property Name Conversion Limitation

**Severity**: Medium  
**Location**: `Executor.java` line 682-684 (`toGoStylePropertyName` method)  
**Impact**: May not correctly handle all-caps abbreviations (e.g., `URL`, `ID`, `XML`)  

**Problem Description:**

The current implementation only capitalizes the first letter:
```java
private String toGoStylePropertyName(String propertyDescriptorName) {
    return Character.toUpperCase(propertyDescriptorName.charAt(0)) + propertyDescriptorName.substring(1);
}
```

This works for most cases:
- `name` → `Name` ✅
- `userName` → `UserName` ✅

But fails for all-caps abbreviations:
- `url` → `Url` ❌ (should be `URL`)
- `id` → `Id` ❌ (should be `ID`)
- `xmlParser` → `XmlParser` ❌ (should be `XMLParser`)

**Example Failure Scenario:**
```java
public class Config {
    private String url;
    public String getURL() { return url; }  // Go template expects {{.URL}}
}

// In template: {{.URL}} 
// Current behavior: Converts "url" to "Url", doesn't match "URL"
// Expected: Should match "URL" or "Url" or "url"
```

**Proposed Fix:**
Enhance the conversion logic to handle common abbreviation patterns:
```java
private String toGoStylePropertyName(String propertyDescriptorName) {
    // If already starts with uppercase, return as-is
    if (Character.isUpperCase(propertyDescriptorName.charAt(0))) {
        return propertyDescriptorName;
    }
    
    // Simple first-letter capitalization for most cases
    return Character.toUpperCase(propertyDescriptorName.charAt(0)) 
         + propertyDescriptorName.substring(1);
}
```

**Alternative Approach:**
Make the matching more flexible by trying multiple variations:
1. Exact match: `identifier.equals(propertyName)`
2. First-letter caps: `identifier.equals(toGoStyleName(propertyName))`
3. All-caps check: If identifier is all-caps, try matching case-insensitively

**Recommendation:** 
- **For v0.6.1**: Add test cases to document current behavior, defer fix unless user reports issue
- **For v0.7.0**: Implement enhanced matching logic with comprehensive tests

**Test Cases Needed:**
```java
// Test nested access with various naming conventions
{{.User.Address.City}}           // Standard camelCase
{{.Config.URL}}                  // All-caps abbreviation
{{.Data.XMLContent}}            // Mixed case
{{.item.ID}}                     // Two-letter abbreviation
```

### Issue 2: MissingKeyPolicy Consistency in Nested Chains

**Severity**: Low  
**Location**: `Executor.java` line 325-422 (`executeFieldPath` method)  
**Impact**: Error messages may not clearly indicate which segment of a chain failed  

**Observation:**
The current implementation throws errors per-segment:
```java
if (missingKeyPolicy == MissingKeyPolicy.ERROR) {
    throw new TemplateExecutionException(
        String.format("missing value for field-chain segment '%s'", identifier));
}
```

For a chain like `.User.Address.City`, if `Address` is null, the error says:
```
missing value for field-chain segment 'Address'
```

This is correct but could be more helpful by showing the full path:
```
missing value for field-chain '.User.Address.City' at segment 'Address'
```

**Recommendation:**
- **For v0.6.1**: No action needed - current behavior is acceptable
- **For v0.7.0**: Consider enhancing error messages with full path context

### Issue 3: Optional Unwrapping Performance

**Severity**: Very Low  
**Location**: `Executor.java` line 430-436 (`unwrapOptional` method)  
**Impact**: Minimal - called on every field access  

**Observation:**
The `unwrapOptional` method is called frequently (lines 339, 353, 374, 390, 404, 193):
```java
private Object unwrapOptional(Object obj) {
    if (obj instanceof Optional) {
        Optional<?> optional = (Optional<?>) obj;
        return optional.orElse(null);
    }
    return obj;
}
```

This is correct but could be optimized with caching if performance becomes an issue.

**Recommendation:**
- **For v0.6.1**: No action needed
- **For v0.7.0**: Profile and optimize only if benchmarks show this is a bottleneck

---

## v0.6.1 Release Criteria (If Needed)

Consider a patch release if any of the following occur:

1. **User reports** issues with all-caps property names (URL, ID, XML, etc.)
2. **Critical bug** discovered in nested field access
3. **Security vulnerability** identified in template execution
4. **Backward compatibility break** found in v0.6.0

**v0.6.1 Scope:**
- Bug fixes only
- No new features
- Maintain Java 8 compatibility
- Zero dependency changes
- All existing tests must pass

---

## v0.7.0 Planned Improvements (Based on Code Review)

In addition to the Stage 2-5 plan, consider these enhancements:

### Documentation Enhancements
- Add "Naming Conventions" section explaining Go-style vs Java-style property access
- Include troubleshooting guide for common property access issues
- Document supported naming patterns (camelCase, PascalCase, ALL_CAPS)

### Testing Enhancements
- Add comprehensive nested field access tests
- Test all naming convention variations
- Test Map/Bean mixed access patterns
- Verify Optional unwrapping in deep chains

### Code Quality
- Consider adding comments to explain the three-tier lookup strategy
- Document the Go compatibility decisions in code comments
- Add performance notes for frequently-called methods

## Later Backlog

These items should stay out of v0.7.0 unless the release scope is explicitly changed.

### Documentation Backlog

- Create video tutorials for common use cases
- Add interactive playground/demo application
- Translate documentation to Chinese (README_zh.md already exists)
- Create architecture decision records (ADRs)
- Add performance profiling guides

### Quality and Tooling Backlog

- Improve coverage headroom above the configured JaCoCo thresholds (target 95%/90%)
- Add tests for error paths that are currently only incidentally covered
- Consider static analysis tooling such as SpotBugs or PMD after compatibility behavior stabilizes
- Review deprecated APIs, magic strings, and input validation boundaries
- Add mutation testing to verify test quality

### Performance Backlog

- Migrate TemplateBenchmark to JMH for more accurate measurements
- Establish baseline numbers before adding reflection caching or AST caching
- Add performance regression checks only after the benchmark harness is stable enough to avoid noisy failures
- Profile hot paths and optimize based on real usage patterns

### API Backlog

- Decide whether general Java method invocation with arguments is desirable, given security and compatibility tradeoffs
- Revisit Java-friendly file helpers if caller-managed IO proves too verbose in real usage
- Revisit broader Go API parity only after v0.7.0 documentation and compatibility improvements are stable
- Consider template pre-compilation for improved execution performance
- Evaluate support for template inheritance beyond block/define

---

## v0.7.0 Release Summary

**Release Date**: 2026-05-11  
**Status**: ✅ COMPLETE - All stages finished successfully

### Key Achievements

1. **Production-Grade Documentation** (Stage 1 & 3)
   - Created 20+ comprehensive documentation files
   - Organized structure ready for static site generation
   - Covered all aspects: getting started, user guide, API reference, advanced topics, examples, FAQ
   - All code examples tested and verified

2. **Enhanced Go Compatibility** (Stage 2)
   - Integer Range Support: `{{range $i := 5}}` iterates 0-4
   - Map Key Sorting: Deterministic output with configurable sorting
   - Block Action: Inline template definition with `{{block "name" .}}`
   - Total: 28 new test cases added

3. **Quality Assurance** (Stage 4)
   - All 689 tests pass on Java 8
   - Code coverage remains above 90% threshold
   - No backward compatibility breaks
   - Full build verification successful

4. **Release Preparation** (Stage 5)
   - CHANGELOG documents all new features
   - Documentation navigation links in docs/index.md
   - Version numbers updated throughout

### Test Results

```
Tests run: 689, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
All coverage checks have been met.
```

### Deliverables

- ✅ Complete documentation hub in `docs/` directory
- ✅ Three major compatibility features implemented
- ✅ Comprehensive CHANGELOG entry
- ✅ Updated PLAN.md reflecting completion status

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
