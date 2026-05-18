# gotemplate4j Development Plan

**Last Updated**: 2026-05-18  
**Current Version**: 0.9.0 (in development)  
**Next Version**: 0.10.0 (Java 11 upgrade planned)  
**Current Focus**: v0.9.0 - Java 8 final release with deprecation notices and compatibility testing

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

**Positioning**: v0.9.0 is the final Java 8 release, focusing on deprecation notices, compatibility testing, and preparing for Java 11 migration in v0.10.0.

#### Goals

- Add Java 11 migration deprecation notices in documentation
- Test compatibility with Java 11, 17, and 21
- Provide migration guide draft for users
- Collect user feedback on Java version requirements
- Maintain Java 8 compatibility and stability
- Prepare codebase for smooth transition to Java 11

#### Non-Goals

- Do not implement new major features (focus on preparation)
- Do not break Java 8 compatibility
- Do not add runtime dependencies beyond vanilla Java
- Do not start Java 11-specific code changes yet

#### Stage 1: Deprecation Notices and Documentation ✅ PENDING

Prepare users for Java 11 migration:

**Documentation Updates:**
- Add deprecation notices for Java 8 support ending with v0.9.x
- Create Java 11 migration guide draft
- Update README with version support timeline
- Document expected breaking changes in v0.10.0

**Code Comments:**
- Add @Deprecated annotations where appropriate (if any APIs will change)
- Include migration hints in Javadoc comments

**Estimated Effort**: 1 day

#### Stage 2: Compatibility Testing ✅ PENDING

Verify gotemplate4j works correctly on newer Java versions:

**Testing Matrix:**
- Test on Java 8 (baseline - current support)
- Test on Java 11 LTS (target for v0.10.0)
- Test on Java 17 LTS (widely adopted)
- Test on Java 21 LTS (latest LTS)

**Test Scenarios:**
- Run full test suite on each Java version
- Verify JMH benchmarks work correctly
- Check for any version-specific behaviors
- Validate reflection-based features work as expected
- Test concurrent access patterns

**Validation Criteria:**
- All 778+ tests pass on all Java versions
- Performance benchmarks show no significant regression
- No version-specific bugs or incompatibilities

**Estimated Effort**: 2-3 days

#### Stage 3: User Feedback Collection ✅ PENDING

Gather community input on Java 11 migration:

**Feedback Channels:**
- GitHub discussions or issues
- Survey existing users about Java version usage
- Identify any blockers for Java 11 adoption
- Understand timeline constraints for enterprise users

**Key Questions:**
- Are users ready to migrate to Java 11?
- What concerns exist about dropping Java 8 support?
- Any specific features needed before migration?
- Preferred timeline for v0.10.0 release?

**Deliverables:**
- Summary of user feedback
- Risk assessment for Java 11 migration
- Adjusted timeline if needed

**Estimated Effort**: 1-2 days (ongoing during v0.9.0 development)

#### v0.9.0 Completion Gate

- ✅ Deprecation notices added to documentation
- ✅ Compatibility tested on Java 8, 11, 17, and 21
- ✅ Migration guide draft provided
- ✅ User feedback collected and reviewed
- ✅ All tests pass on all supported Java versions
- ✅ Performance benchmarks show no regression
- ✅ `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8
- ✅ No backward compatibility breaks
- ✅ Ready for Java 11 migration in v0.10.0

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

### Performance Optimization (Low Priority)

#### Performance Optimization Fine-tuning 🟡 LOW PRIORITY
**Status**: Data-driven decisions needed

**Deferred Optimizations** (low ROI after ClassMetadata):
- **Optional Unwrapping Optimization**: Profile first, optimize only if bottleneck confirmed (<5% expected gain)
    - Potential approaches: inline the check, remove from known non-Optional paths
- **String Building Optimization**: Use StringBuilder in `buildFullPath()` for error messages (<5% gain)
- **Executor Lifecycle Management**: Evaluate ThreadLocal or pooled Executor pattern
    - Profile first: Is Executor creation a bottleneck?
    - Must maintain thread safety and zero behavioral changes
    - Alternative: ThreadLocal<Executor> per Template or object pool

**Note**: These optimizations have low expected ROI after ClassMetadata unified cache (+267% bean access). Implement only if profiling shows measurable bottlenecks.

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

#### File Helper Improvements 🟡 MEDIUM PRIORITY
**Priority**: Medium (Practical value, low risk)
**Status**: Not started

**Description**: Enhance file loading APIs for better developer experience.

**Current APIs** (v0.6.0+):
- ✅ `parseFile(Path)` - Load single file
- ✅ `parseFiles(Path...)` - Load multiple files
- ✅ `parseGlob(Path, String)` - Load files matching pattern

**Potential Improvements**:
1. **Classpath Loading**
   ```java
   // New API proposal
   template.parseFromClasspath("/templates/base.tmpl");
   template.parseFromResource("base.tmpl");
   ```

2. **Better Error Diagnostics**
    - Show search paths when file not found
    - List available templates in directory
    - Suggest similar filenames on typo

3. **Built-in Caching**
    - Optional simple cache for development mode
    - Auto-reload on file changes (watch mode)
    - Cache invalidation strategies

4. **Convenience Methods**
   ```java
   // Parse entire directory structure
   template.parseDirectory(Paths.get("templates"));
   
   // Load with encoding specification
   template.parseFile(path, StandardCharsets.UTF_8);
   ```

**Rationale**:
- Most practical improvement with immediate user value
- Low implementation risk (extends existing APIs)
- Aligns with common usage patterns

**Decision**: Likely candidate for next release after collecting user feedback

**Estimated Effort**: 1-2 days

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
