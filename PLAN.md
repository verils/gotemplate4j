# gotemplate4j Development Plan

**Last Updated**: 2026-05-12  
**Current Version**: 0.7.0  
**Next Version**: 0.8.0 (future enhancements)  
**Current Focus**: Planning v0.8.0 features and improvements  
**Status**: v0.7.0 released ✅ - All stages complete

---

## Direction

gotemplate4j should remain a small, Java 8-compatible implementation of Go's `text/template` semantics for Java applications. v0.7.0 completed production-grade documentation and enhanced Go compatibility. v0.8.0 focuses on quality improvements, performance optimization, and addressing identified limitations.

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
- **Enhanced Compatibility**: Reduced migration friction from Go templates
- **CHANGELOG Updated**: Comprehensive release notes for v0.7.0

### ✅ Completed (v0.6.0)

- MissingKeyPolicy configuration (DEFAULT/ZERO/ERROR)
- Template introspection API (name(), hasTemplate(), definedTemplates(), lookup(), templates())
- File system helpers (parseFile(), parseFiles(), parseGlob())
- Performance benchmarking (TemplateBenchmark)
- Initial compatibility and migration documentation

---

## v0.8.0 Release Plan

Positioning: v0.8.0 is the quality improvement and performance optimization release. It addresses identified limitations, improves code quality, and establishes better testing infrastructure.

### Goals

- Address critical issues identified in code review (property name conversion, error messages)
- Improve test coverage and quality (target 95%/90% instruction/branch coverage)
- Establish robust performance benchmarking infrastructure (JMH migration)
- Enhance developer experience with better tooling and static analysis
- Maintain Java 8 compatibility and dependency-light philosophy

### Non-Goals

- Do not implement complex Go-only features (channels, iterators, ParseFS)
- Do not add runtime dependencies beyond vanilla Java
- Do not break backward compatibility
- Do not implement general method invocation with arguments (requires separate security design)

### Stage 1: Critical Issue Resolution

Address issues identified during v0.7.0 code review:

**Priority 1: Enhanced Property Name Conversion**

Fix `toGoStylePropertyName` to handle all-caps abbreviations (URL, ID, XML):
- Try multiple matching variations (exact, first-letter caps, case-insensitive)
- Add comprehensive tests for naming conventions
- Document supported patterns in user guide

**Priority 2: Improved Error Messages**

Enhance field-chain error reporting with full path context:
- Show complete path when segment fails (e.g., `.User.Address.City` at `Address`)
- Maintain backward compatibility with existing error format
- Add tests for nested access error scenarios

**Priority 3: Optional Unwrapping Optimization**

Profile and optimize if benchmarks show bottleneck:
- Consider caching for frequently-accessed Optional fields
- Only implement if performance impact is measurable

### Stage 2: Quality and Testing Improvements

Improve test coverage, quality, and establish better testing infrastructure:

**Test Coverage Enhancement:**
- Target 95% instruction coverage and 90% branch coverage
- Add tests for currently uncovered error paths
- Improve nested field access test coverage
- Test all naming convention variations (camelCase, PascalCase, ALL_CAPS)
- Verify Optional unwrapping in deep chains

**Testing Infrastructure:**
- Migrate TemplateBenchmark to JMH for accurate measurements
- Establish baseline performance numbers
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
- Confirm test coverage targets met (95%/90%)
- Validate performance improvements with JMH benchmarks
- Ensure static analysis passes without warnings

**Documentation Updates:**
- Add "Naming Conventions" section for property access
- Include troubleshooting guide for common issues
- Document supported naming patterns (camelCase, PascalCase, ALL_CAPS)
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
- Test coverage reaches 95% instruction / 90% branch targets
- JMH benchmark infrastructure established with baseline numbers
- Static analysis integrated and passing
- Performance optimizations implemented and validated
- `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8
- No backward compatibility breaks
- Documentation updated with new features and best practices

### Suggested Next Session Order

**Current Position**: v0.7.0 released ✅, planning v0.8.0

1. Implement enhanced property name conversion (handle URL, ID, XML)
2. Improve error messages with full path context
3. Profile Optional unwrapping performance
4. Add comprehensive naming convention tests
5. Migrate TemplateBenchmark to JMH
6. Establish baseline performance numbers
7. Integrate SpotBugs/PMD static analysis
8. Increase test coverage to 95%/90%
9. Add mutation testing
10. Profile and optimize hot paths
11. Consider reflection caching strategies
12. Evaluate method invocation with arguments (security analysis)
13. Update documentation with new features
14. Final review and verification

---

## v0.7.0 Release Summary

**Release Date**: 2026-05-11  
**Status**: ✅ COMPLETE

### Key Achievements

1. **Production-Grade Documentation**
   - Created 20+ comprehensive documentation files
   - Organized structure ready for static site generation
   - Covered all aspects: getting started, user guide, API reference, advanced topics, examples, FAQ

2. **Enhanced Go Compatibility**
   - Integer Range Support: `{{range $i := 5}}` iterates 0-4
   - Map Key Sorting: Deterministic output with configurable sorting
   - Block Action: Inline template definition with `{{block "name" .}}`
   - Total: 28 new test cases added

3. **Quality Assurance**
   - All 689 tests pass on Java 8
   - Code coverage remains above 90% threshold
   - No backward compatibility breaks

### Test Results

```
Tests run: 689, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
All coverage checks have been met.
```

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
