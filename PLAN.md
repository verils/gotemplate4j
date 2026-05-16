# gotemplate4j Development Plan

**Last Updated**: 2026-05-13  
**Current Version**: 0.8.0  
**Next Version**: 0.9.0 (future enhancements)  
**Future Major Release**: 0.10.0 (Java 11 upgrade planned)  
**Current Focus**: v0.8.0 Stage 2 (Quality & Testing) in progress  
**Status**: v0.8.0 Stage 1 complete ✅, Stage 2 in progress

---

## Direction

gotemplate4j should remain a small, Java-compatible implementation of Go's `text/template` semantics for Java applications. v0.8.0 focuses on quality improvements, performance optimization, and addressing identified limitations.

**Important Notice**: Starting from v0.10.0, gotemplate4j will require **Java 11 or higher**. This decision aligns with industry standards and enables the use of modern Java features for cleaner, more maintainable code. Java 8 support will end with v0.9.x.

## Working Constraints

- **Current**: Maintain Java 8 compatibility through v0.9.x
- **Future**: Upgrade to Java 11+ starting from v0.10.0
- Use `./mvnw`, not `mvn`.
- Avoid runtime dependencies beyond vanilla Java.
- Preserve backward compatibility unless a documented Go compatibility fix requires a behavior change.
- Keep Go-compatible behavior, Java-specific behavior, and unsupported Go APIs clearly separated.

## Current Progress

### ✅ Completed (v0.8.0 - In Progress)

- **@TemplateField Annotation Support**: Explicit field/method name control with annotation-based mapping
- **Improved Error Messages**: Enhanced field-chain error reporting with full path context
  - Shows complete path when segment fails (e.g., `nil pointer evaluating User.Address.City at 'City'`)
  - Maintains backward compatibility with existing error format
  - Added comprehensive tests for nested access error scenarios
  - Test coverage: 7 new test cases covering various nesting levels
  - All error paths now provide clear debugging information

---

## v0.8.0 Release Plan

Positioning: v0.8.0 is the quality improvement and performance optimization release. It addresses identified limitations, improves code quality, and establishes better testing infrastructure.

### Goals

- Address critical issues identified in code review (property name conversion, error messages)
- Maintain high test coverage quality (current: ~92% instruction, ~89% branch)
- Establish robust performance benchmarking infrastructure (JMH migration)
- Enhance developer experience with better tooling and static analysis
- Maintain Java 8 compatibility and dependency-light philosophy

### Non-Goals

- Do not implement complex Go-only features (channels, iterators, ParseFS)
- Do not add runtime dependencies beyond vanilla Java
- Do not break backward compatibility
- Do not implement general method invocation with arguments (requires separate security design)

### Stage 1: Critical Issue Resolution ✅ COMPLETE

All critical issues from code review have been resolved:

**✅ Priority 1: Field Name Annotation Support** - COMPLETED

Replaced heuristic property name conversion with explicit annotation-based mapping:
- Introduced `@TemplateField` annotation for explicit field/method name control in templates
- Supports annotation on both fields and getter methods (field takes precedence)
- Lookup priority: @TemplateField value > exact match > simple Go-style capitalization
- Maintains backward compatibility (no annotation = current behavior with simple capitalization)
- Added comprehensive tests for annotation usage patterns and edge cases
- Documented annotation usage in user guide with practical examples
- Deprecated complex heuristics in `toGoStylePropertyName` (kept only first-letter capitalization)

**✅ Priority 2: Improved Error Messages** - COMPLETED

Enhanced field-chain error reporting with full path context:
- Shows complete path when segment fails (e.g., `nil pointer evaluating User.Address.City at 'City'`)
- Maintains backward compatibility with existing error format
- Added 7 comprehensive tests for nested access error scenarios
- All error messages now include full context for easier debugging

**✅ Priority 3: Optional Unwrapping Optimization** - DEFERRED

Deferred pending JMH benchmark results:
- Will profile after JMH migration (Stage 2)
- Only implement if performance impact is measurable
- Current implementation is correct and maintainable

### Stage 2: Quality and Testing Improvements ⭐ IN PROGRESS

Improve test coverage, quality, and establish better testing infrastructure:

**Test Coverage Enhancement:**
- Maintain current coverage levels (~92% instruction, ~89% branch)
- Add tests for critical error paths and edge cases as needed
- Focus on test quality over quantity
- Verify Optional unwrapping in deep chains

**Testing Infrastructure:**
- ✅ Migrate TemplateBenchmark to JMH for accurate measurements - COMPLETED
  - Created TemplateJmhBenchmark with comprehensive benchmark scenarios
  - Configured JMH dependencies and exec-maven-plugin in pom.xml
  - Established baseline performance numbers (see results below)
  - Baseline results (ops/sec):
    * parseBenchmark: ~223,427 ops/sec
    * executeBenchmark: ~987,607 ops/sec
    * beanAccessBenchmark: ~961,966 ops/sec
    * mapAccessBenchmark: ~2,290,887 ops/sec
    * rangeHeavyBenchmark (100 items): ~25,141 ops/sec
    * functionHeavyBenchmark: ~843,460 ops/sec
  - Usage: `./mvnw test-compile exec:java "-Dexec.mainClass=io.github.verils.gotemplate.TemplateJmhBenchmark" "-Dexec.classpathScope=test"`
- Add mutation testing to verify test quality
- Consider adding property-based testing for edge cases

**Static Analysis:**
- Integrate SpotBugs or PMD for code quality checks
- Review deprecated APIs and magic strings
- Strengthen input validation boundaries
- Add code quality gates to CI/CD pipeline

### Stage 3: Performance Optimization

Profile and optimize based on real usage patterns:

**Performance Profiling:**
- Profile hot paths in Executor (field access, command execution)
- Identify bottlenecks in reflection-heavy operations
- Measure impact of Optional unwrapping on performance
- Establish performance regression checks

**Optimization Opportunities:**
- Consider reflection caching for frequently-accessed methods/fields
- Evaluate AST caching for pre-compiled templates
- Optimize unwrapOptional calls if profiling shows bottleneck
- Add performance notes to frequently-called methods

**Documentation:**
- Add performance profiling guides
- Document optimization strategies and trade-offs
- Provide benchmarking best practices

### Stage 4: API and Feature Evaluation

Evaluate potential API improvements and feature additions:

**Method Invocation with Arguments:**
- Decide whether general Java method invocation with arguments is desirable
- Conduct security analysis for dynamic method invocation
- Evaluate compatibility trade-offs
- Design safe invocation patterns if proceeding

**Template Pre-compilation:**
- Evaluate support for template pre-compilation
- Assess performance benefits vs complexity
- Design API for compiled template caching

**Enhanced Template Inheritance:**
- Revisit broader Go API parity after stability
- Evaluate support for template inheritance beyond block/define
- Consider layout/template composition patterns

**File Helper Improvements:**
- Revisit Java-friendly file helpers based on user feedback
- Assess if caller-managed IO is too verbose in practice
- Consider convenience methods for common patterns

### Stage 5: Release Preparation

Prepare for v0.8.0 release:

**Quality Assurance:**
- Verify all critical issues resolved
- Confirm test coverage meets minimum thresholds (90%/85%)
- Validate performance improvements with JMH benchmarks
- Ensure static analysis passes without warnings

**Documentation Updates:**
- Add "Field Name Mapping" section explaining @TemplateField annotation
- Include troubleshooting guide for common issues
- Document annotation usage patterns and best practices
- Update performance tuning guide with new optimizations

**CHANGELOG and README:**
- Document all quality improvements and optimizations
- Highlight performance gains and coverage improvements
- Update feature list with new capabilities
- Refresh quick start examples if needed

**Final Verification:**
- `./mvnw clean verify "-Dgpg.skip=true"` succeeds
- All tests pass on Java 8
- Code coverage meets enhanced thresholds
- Performance benchmarks show improvements or no regression

### v0.8.0 Completion Gate

- All critical issues from code review resolved and tested
- Test coverage meets minimum thresholds (90% instruction / 85% branch)
- JMH benchmark infrastructure established with baseline numbers
- Static analysis integrated and passing
- Performance optimizations implemented and validated
- `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8
- No backward compatibility breaks
- Documentation updated with new features and best practices

### Suggested Next Session Order

**Current Position**: v0.8.0 Stage 1 complete ✅, Stage 2 in progress

**Completed:**
1. ~~Implement @TemplateField annotation support~~ ✅ DONE
2. ~~Improve error messages with full path context~~ ✅ DONE

**Next Priorities (Stage 2 - Quality & Testing):**
3. Maintain test coverage quality ⭐ HIGH PRIORITY
   - Add tests for critical error paths as discovered
   - Focus on meaningful test scenarios over coverage metrics
   - Verify Optional unwrapping in deep chains
4. Add comprehensive annotation and naming convention tests
5. Migrate TemplateBenchmark to JMH
6. Establish baseline performance numbers
7. Integrate SpotBugs/PMD static analysis

**Future (Stage 3-4 - Performance & Features):**
8. Profile Optional unwrapping performance (after JMH setup)
9. Profile and optimize hot paths
10. Consider reflection caching strategies
11. Add mutation testing
12. Evaluate method invocation with arguments (security analysis)
13. Update documentation with new features
14. Final review and verification

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.

---

## Future Roadmap: Java 11 Migration (v0.10.0)

### Overview

Starting from version 0.10.0, gotemplate4j will require **Java 11 or higher**. This strategic decision enables:
- Access to modern Java language features (var, records, pattern matching)
- Better performance with G1 GC and JIT improvements
- Alignment with industry standards (Java 8 reached end of public updates in 2019)
- Simplified codebase maintenance

### Migration Timeline

```
v0.8.0 (Current)  → Java 8, Quality improvements
v0.9.0 (Next)     → Java 8, Performance optimization + JMH + Deprecation warnings
v0.10.0 (Future)  → Java 11+, Modern features + Code cleanup
```

### What Changes in v0.10.0?

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

### Preparation in v0.9.0

In version 0.9.0, we will:
- Add deprecation notices in documentation
- Test compatibility with Java 11, 17, and 21
- Provide migration guide draft
- Collect user feedback

### Migration Guide for Users

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

### Rationale

**Why Java 11?**
- Long-term support until September 2026
- Mature ecosystem with widespread adoption
- Significant improvements over Java 8
- Industry standard for new projects

**Why not Java 17?**
- Java 11 provides sufficient modern features
- Wider current adoption in enterprise environments
- Can consider Java 17+ in future major releases (v1.0+)

### Support Policy

- **v0.9.x**: Last version supporting Java 8 (security fixes only after v0.10.0 release)
- **v0.10.0+**: Requires Java 11 or higher
- **Critical security patches**: May backport to v0.9.x for limited time

For questions or concerns about this migration, please open an issue on GitHub.
