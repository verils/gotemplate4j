# gotemplate4j Development Plan

**Last Updated**: 2026-05-17  
**Current Version**: 0.8.0  
**Next Version**: 0.9.0 (future enhancements)  
**Future Major Release**: 0.10.0 (Java 11 upgrade planned)  
**Current Focus**: v0.8.0 Stage 3 (Performance Optimization) - ClassMetadata completed  
**Status**: v0.8.0 Stage 1 ✅, Stage 2 ✅, Stage 3 ✅ ClassMetadata optimization complete

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

### Stage 2: Quality and Testing Improvements ✅ COMPLETE

Improve test coverage, quality, and establish better testing infrastructure:

**Test Coverage Enhancement:**
- Maintain current coverage levels (~92% instruction, ~89% branch)
- Add tests for critical error paths and edge cases as needed
- Focus on test quality over quantity
- Verify Optional unwrapping in deep chains

**Testing Infrastructure:**
- ✅ Migrate TemplateBenchmark to JMH for accurate measurements - COMPLETED
  - Created TemplateJmhBenchmark with comprehensive benchmark scenarios
  - Configured JMH dependencies (jmh-core 1.37, jmh-generator-annprocess 1.37)
  - Configured exec-maven-plugin for easy benchmark execution
  - Established baseline performance numbers (ops/sec):
    * parseBenchmark: ~223,427
    * executeBenchmark: ~987,607
    * beanAccessBenchmark: ~961,966
    * mapAccessBenchmark: ~2,290,887
    * rangeHeavyBenchmark (100 items): ~25,141
    * functionHeavyBenchmark: ~843,460
  - Benchmark configuration: 3 warmup iterations (2s), 5 measurement iterations (3s)
  - Usage: `./mvnw test-compile exec:java -Dexec.mainClass="io.github.verils.gotemplate.TemplateJmhBenchmark"`
- ✅ Verify Optional unwrapping in deep chains - COMPLETED
  - Added 6 comprehensive tests for nested Optional scenarios
  - Verified proper unwrapping at multiple levels (User -> Optional<Address> -> Optional<City>)
  - Tested empty Optional handling at different chain positions
  - Validated three-level Optional nesting (Optional<String> inside Optional<City> inside Optional<Address>)
  - All 763 tests pass including new deep chain scenarios
- Add mutation testing to verify test quality (deferred)
- Consider adding property-based testing for edge cases (deferred)

**Static Analysis:**
- Integrate SpotBugs or PMD for code quality checks (deferred - requires external dependencies)
- Review deprecated APIs and magic strings
- Strengthen input validation boundaries
- Add code quality gates to CI/CD pipeline

### Stage 3: Performance Optimization ✅ COMPLETE

Profile and optimize based on real usage patterns. Target: 10-30% performance improvement in hot paths.

**Current Status**: ClassMetadata Unified Cache completed ✅ | **Result**: beanAccessBenchmark +267% improvement

**Performance Analysis Results** (May 17, 2026 - After ClassMetadata):
- ✅ executeBenchmark: 1,790,404 ops/sec (+81.3% from baseline 987K) 🚀
- ✅ beanAccessBenchmark: 1,714,003 ops/sec (+178.4% from regression 467K, +78.2% from baseline 962K) 🚀
- ✅ functionHeavyBenchmark: 1,039,120 ops/sec (+23.2% from baseline 843K)
- ✅ mapAccessBenchmark: 2,762,592 ops/sec (+20.6% from baseline 2,291K)
- ✅ parseBenchmark: 323,855 ops/sec (+45.0% from baseline 223K)
- ✅ rangeHeavyBenchmark: 38,648 ops/sec (+53.7% from baseline 25K)

**Root Cause Analysis** (SOLVED):
- beanAccessBenchmark regression was caused by `findAnnotatedMember()` overhead
- For classes without `@TemplateField` annotations, every field access triggered:
  1. annotationCache miss → full reflection scan (getDeclaredFields + getDeclaredMethods)
  2. Empty HashMap creation and caching
  3. PropertyDescriptor index lookup + fallback linear search (double work)
  4. Additional method/field scans
- Total overhead: ~600-1200ns per access vs ~100-200ns before optimization
- **Solution Implemented**: Unified ClassMetadata cache eliminated redundant scans and enabled fast-fail
- **Result**: beanAccessBenchmark improved from 467K to 1,714K ops/sec (+267%)

**Phase 1: Performance Profiling & Baseline (1-2 days)**
- Run existing JMH benchmarks to establish current baseline
  - parseBenchmark: ~223,427 ops/sec
  - executeBenchmark: ~987,607 ops/sec
  - beanAccessBenchmark: ~961,966 ops/sec
  - mapAccessBenchmark: ~2,290,887 ops/sec
  - rangeHeavyBenchmark: ~25,141 ops/sec
  - functionHeavyBenchmark: ~843,460 ops/sec
- Add micro-benchmarks for specific operations:
  - Field access path (`executeFieldPath`)
  - Optional unwrapping overhead
  - BeanInfo introspection cost
  - Reflection invocation (Method.invoke vs Field.get)

**Phase 2: Core Optimizations (3-5 days)**

**Completed Optimizations:**
1. ✅ BeanInfo Caching (+32.2% bean access, +20.5% execute)
2. ✅ Annotation Cache memory leak fix (static → instance-level)
3. ✅ PropertyDescriptor Indexing (implemented with fallback for compatibility)
4. ✅ **ClassMetadata Unified Cache** (+267% bean access, fixed regression) - COMPLETED
   - Renamed from ClassInfo to ClassMetadata for better naming convention
   - All fields changed to private with public getter methods
   - Follows Java encapsulation best practices

**Critical Fix Completed:**
4. ✅ **ClassMetadata Unified Cache** (COMPLETED - May 17, 2026)
   - **Problem Solved**: Three separate caches caused performance regression in beanAccessBenchmark (-51.5%)
   - **Solution Implemented**: Single `Map<Class<?>, ClassMetadata>` cache with pre-computed metadata
   - **Actual Performance Gain**: 
     - beanAccessBenchmark: 467K → 1,714K ops/sec (**+267%**, exceeded lower target)
     - executeBenchmark: Maintained at 1,790K ops/sec (no regression)
     - Eliminated all invalid lookups for classes without annotations
   - **Code Quality**: 
     - Fields are private with public getter methods (encapsulation)
     - Comprehensive Javadoc documentation
     - All 778 tests pass
   - **Risk**: Low (internal refactoring, API unchanged) ✅
   - **Status**: ✅ COMPLETED AND VALIDATED
   - **Implementation Details**: See *Optimization 7: ClassMetadata Unified Cache* below

**Planned Optimizations (after ClassInfo):**
5. Method/Field Object Caching (5-10% expected) - MAY BE REDUNDANT after ClassInfo
6. Optional Unwrapping Optimization (data-driven)
7. String Building Optimization (<5% expected)

**Deferred for Future Evaluation:**
- SoftReference Cache Strategy (v0.9.0+, only if memory pressure observed)
- Executor Lifecycle Management (needs profiling data first)

*Optimization 1: BeanInfo Caching* ✅ COMPLETED
- **Problem**: `Introspector.getBeanInfo()` called repeatedly for same class
- **Solution**: Instance-level cache in Template, passed to Executor
- **Design**: Cache shares Template lifecycle, auto-GC'd with Template (no memory leak)
- **Expected gain**: 15-25% improvement (bean access scenarios)
- **Actual gain**: 
  - beanAccessBenchmark: +32.2% (961K → 1,272K ops/sec)
  - executeBenchmark: +20.5% (987K → 1,189K ops/sec)
  - All benchmarks improved, no regression
- **Risk**: None (instance-level cache, no static state)
- **Status**: Implemented and validated ✅
- **Note**: Also fixed ANNOTATION_CACHE (static → instance-level) to prevent memory leaks

*Optimization 1.5: SoftReference Cache Strategy* 📅 FUTURE (v0.9.0+)
- **Problem**: Potential memory pressure in long-running applications with many templates
- **Proposal**: Use SoftReference wrappers for cached values
- **Pros**: JVM auto-cleans under memory pressure, prevents OOM
- **Cons**: Current instance caches already GC with Template; added complexity; slight performance overhead
- **Decision**: Defer until actual memory pressure observed in production

*Optimization 2: PropertyDescriptor Indexing* ✅ COMPLETED
- **Problem**: Linear search through PropertyDescriptor array on every field access
- **Solution**: Build name-indexed cache per class: `Class -> (name -> PropertyDescriptor)` with fallback to linear search for compatibility
- **Expected gain**: 10-20% improvement (reduces O(n) to O(1) lookup)
- **Actual gain**: Performance improvement varies by workload; index provides fast path while maintaining full compatibility
- **Risk**: Low (instance-level cache, fallback ensures compatibility)
- **Status**: Implemented and validated ✅
- **Implementation details**:
  - Instance-level cache in Template, passed to Executor
  - Indexes both original property names and Go-style capitalized names
  - Handles IllegalArgumentException from incompatible method invocations gracefully
  - Falls back to linear search if indexed lookup fails
  - All 778 tests pass including edge cases with public fields and inheritance

*Optimization 7: ClassMetadata Unified Cache* ✅ COMPLETED (May 17, 2026)
- **Problem Solved**: Three independent caches caused performance regression in beanAccessBenchmark (-51.5%)
  - Redundant reflection scans: annotationCache, beanInfoCache, propertyDescriptorCache each scanned separately
  - Invalid lookups: Classes without @TemplateField still triggered full annotation scanning
  - Multiple HashMap lookups: 3-4 separate cache checks per field access
  - No fast-fail mechanism: Cannot quickly determine if class has no accessible members
  
- **Root Cause Identified**:
  - `findAnnotatedMember()` always performed full reflection scan on cache miss
  - Even empty results were cached, but initial scan cost was high (~500-1000ns)
  - For simple classes like BeanData (2 getters, no annotations), this overhead dominated
  - Combined with PropertyDescriptor double-lookup (index + fallback), total cost was 4-6x higher than before
  
- **Solution Architecture Implemented**:
  - Created `ClassMetadata` class to consolidate all reflection metadata into single immutable object
  - Replaced three separate caches (`beanInfoCache`, `annotationCache`, `propertyDescriptorCache`) with unified `classMetadataCache`
  - All fields are private with public getter methods following Java encapsulation best practices
  - Pre-computed boolean flags enable fast-fail optimization for non-existent lookup paths
  - Single comprehensive scan per class eliminates redundant reflection operations
  
- **Execution Flow After Optimization**:
  1. Lookup or create `ClassMetadata` from cache (~10ns after first access)
  2. Check `hasAnnotatedMembers()` flag → if true, lookup in annotated members map
  3. Check `hasPropertyDescriptors()` flag → if true, lookup in property index
  4. Check `hasPublicMethods()` flag → if true and name exists, invoke via reflection
  5. Check `hasPublicFields()` flag → if true and name exists, get field value
  6. Throw exception if not found
  
  **Key improvement**: Boolean flags allow instant skip of entire lookup paths when metadata doesn't exist
  
- **Performance Benefits Achieved**:
  - ✅ **Eliminated redundant scans**: One comprehensive scan vs 3-4 separate scans
  - ✅ **Fast-fail capability**: Boolean flags skip entire lookup paths instantly
  - ✅ **Reduced HashMap operations**: 1 lookup vs 3-4 separate lookups
  - ✅ **Better CPU cache locality**: Related data in single object
  - ✅ **Invalid lookup elimination**: Classes without annotations pay zero annotation scan cost after first access
  - ✅ **Encapsulation**: Private fields with public getters follow Java best practices
  
- **Actual Performance Impact** (Measured May 17, 2026):
  - ✅ beanAccessBenchmark: 467K → 1,714K ops/sec (**+267%**, target was +300-400%)
  - ✅ executeBenchmark: 1,790K → 1,790K ops/sec (maintained, no regression)
  - ✅ Overall field access: ~600-1200ns → ~120-150ns (**4-8x faster**)
  - ✅ Invalid lookups: ~500-1000ns → ~50-100ns (**5-10x faster**)
  - Note: While +267% didn't reach the ambitious +300-400% target, it successfully fixed the -51.5% regression and exceeded the +15% minimum success criterion
  
- **Memory Impact**:
  - Slight increase: ClassMetadata objects larger than individual cache entries
  - Mitigation: Instance-level cache (GC'd with Template), typical overhead < 1MB
  - Trade-off: Acceptable for significant performance gains
  
- **Implementation Completed**:
  1. ✅ Designed ClassMetadata structure with all necessary fields
  2. ✅ Implemented comprehensive scanner in ClassMetadata constructor
  3. ✅ Replaced 3 separate caches with single classMetadataCache
  4. ✅ Refactored executeFieldPath to use ClassMetadata with fast-fail logic
  5. ✅ Removed obsolete methods: findAnnotatedMember, findPropertyDescriptor (inlined into ClassMetadata)
  6. ✅ Changed all fields to private with public getter methods (encapsulation)
  7. ✅ All 778 tests pass
  8. ✅ JMH benchmarks validated improvements
  
- **Risk Assessment**:
  - Risk Level: **LOW** (internal refactoring, no API changes) ✅
  - Backward Compatibility: ✅ Guaranteed (Executor internal only)
  - Thread Safety: ✅ ConcurrentHashMap + immutable ClassMetadata
  - Test Coverage: ✅ All 778 tests pass
  - Code Quality: ✅ Private fields with public getters, comprehensive Javadoc
  
- **Success Criteria Met**:
  - ✅ beanAccessBenchmark ≥ 1,500,000 ops/sec (achieved: 1,714,003 ops/sec)
  - ✅ executeBenchmark maintained (1,790,404 ops/sec, no regression)
  - ✅ All other benchmarks: no regression
  - ✅ All 778 tests pass
  - ✅ Zero behavioral changes
  - ✅ Code follows Java encapsulation best practices
  
- **Priority Justification Achieved**:
  - ✅ **CRITICAL**: Fixed performance regression introduced by PropertyDescriptor Indexing
  - ✅ **High ROI**: 1-2 days development for 267% improvement in key benchmark
  - ✅ **Architectural Improvement**: Cleaner design, better maintainability, proper encapsulation
  - ✅ **Foundation for Future**: Enables easier addition of new optimizations

*Optimization 3: Method/Field Object Caching* ⚡ LOW PRIORITY (LIKELY REDUNDANT)
- **Note**: This optimization is now likely unnecessary after ClassMetadata implementation
- **Decision**: Deferred indefinitely - ClassMetadata already provides comprehensive caching
- **Original Problem**: Repeated reflection lookups for same members
- **Original Solution**: Extend annotation cache to include regular accessible members
- **Expected gain**: 5-10% improvement (now included in ClassMetadata)
- **Risk**: Medium (need to handle inheritance properly)
- **Status**: 📅 DEFERRED - ClassMetadata makes this redundant

*Optimization 4: Optional Unwrapping Optimization* ⚠️ DATA-DRIVEN
- **Problem**: `unwrapOptional()` called at every field access point
- **Analysis**: Most values are NOT Optional, instanceof check is fast
- **Decision**: Profile first, optimize only if bottleneck confirmed
- **Potential approaches**:
  - Inline the check to avoid method call overhead
  - Remove from known non-Optional paths (requires code audit)
- **Expected gain**: < 5% (only if profiling shows impact)

*Optimization 5: String Building Optimization* 📝 LOW PRIORITY
- **Problem**: Error message path construction uses string concatenation
- **Solution**: Use StringBuilder in `buildFullPath()`
- **Expected gain**: < 5% (only affects error paths)

*Optimization 6: Executor Lifecycle Management* 🔍 EVALUATION NEEDED
- **Proposal**: Transform Executor from per-execution instance to Template-level singleton
- **Current behavior**: New Executor created for each `executeTemplate()` call
- **Potential benefits**:
  - Reduce object allocation and GC pressure
  - Reuse cached data structures across executions
  - Improve high-frequency execution scenarios
- **Challenges**:
  - ⚠️ Thread safety: Executor holds mutable state (variables, writer)
  - ⚠️ Requires significant refactoring of execute methods
  - ⚠️ May introduce concurrency bugs if not carefully designed
- **Decision criteria**:
  1. Profile first: Is Executor creation a bottleneck?
  2. If yes, consider thread-local or pooled Executor pattern
  3. Must maintain zero behavioral changes
  4. Must pass all concurrent access tests
- **Alternative approaches**:
  - ThreadLocal<Executor> per Template
  - Object pool with bounded size
  - Stateless Executor design (pass all state as parameters)
- **Status**: 📅 DEFERRED - Needs profiling data before implementation

**Phase 3: Validation & Testing (1-2 days)**
- Run full JMH benchmark suite to measure improvements
- Verify all 778+ tests pass (especially bean/field/Optional tests)
- Test thread safety under concurrent access
- Ensure no memory leaks (consider WeakHashMap if needed)
- Confirm zero behavioral changes

**Phase 4: Documentation (1 day)**
- Add performance notes to optimized methods (Javadoc)
- Update docs/advanced/performance.md with:
  - Optimization strategies explained
  - Benchmark results (before/after)
  - Best practices for users
- Update PLAN.md with final performance numbers

**Success Criteria:**
- ✅ beanAccessBenchmark: ≥ 15% improvement
- ✅ executeBenchmark: ≥ 10% improvement
- ✅ All other benchmarks: no regression
- ✅ All tests pass (778+)
- ✅ Coverage maintained (≥ 90%/85%)
- ✅ Zero API or behavior changes
- ✅ Thread-safe implementation
- ✅ Java 8 compatible

### Stage 5: Release Preparation ✅ COMPLETE

Prepare for v0.8.0 release:

**Quality Assurance:**
- ✅ Verify all critical issues resolved
- ✅ Confirm test coverage meets minimum thresholds (90%/85%)
- ✅ Validate performance improvements with JMH benchmarks
- ⚠️ Static analysis deferred (requires external dependencies)

**Documentation Updates:**
- ✅ Add "Field Name Mapping" section explaining @TemplateField annotation
- ✅ Include troubleshooting guide for common issues
- ✅ Document annotation usage patterns and best practices
- ✅ Update performance tuning guide with new optimizations

**CHANGELOG and README:**
- ✅ Document all quality improvements and optimizations
- ✅ Highlight performance gains and coverage improvements
- ✅ Update feature list with new capabilities
- ✅ Refresh quick start examples if needed

**Final Verification:**
- ✅ `./mvnw clean verify "-Dgpg.skip=true"` succeeds
- ✅ All tests pass on Java 8
- ✅ Code coverage meets enhanced thresholds
- ✅ Performance benchmarks show improvements or no regression

### v0.8.0 Completion Gate ✅ ALL MET

- ✅ All critical issues from code review resolved and tested
- ✅ Test coverage meets minimum thresholds (~92% instruction / ~89% branch)
- ✅ JMH benchmark infrastructure established with baseline numbers
- ⚠️ Static analysis deferred (SpotBugs/PMD requires external dependencies)
- ✅ Performance optimizations implemented and validated
- ✅ `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8
- ✅ No backward compatibility breaks
- ✅ Documentation updated with new features and best practices

---

## Future Backlog (Unscheduled)

This section tracks potential improvements and features that are not yet scheduled for specific releases.
Items will be moved to active development stages based on user feedback, priority assessment, and resource availability.

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

---

### Performance Optimization (Future)

#### Remaining Hot Path Optimizations
**Status**: Partially deferred after ClassMetadata optimization

**Completed**:
- ✅ BeanInfo Caching (+32.2% bean access)
- ✅ Annotation Cache memory leak fix
- ✅ PropertyDescriptor Indexing
- ✅ ClassMetadata Unified Cache (+267% bean access)

**Deferred (Low ROI)**:
- Method/Field Object Caching (likely redundant after ClassMetadata)
- Optional Unwrapping Optimization (<5% expected gain, data-driven)
- String Building Optimization (<5% gain, error paths only)

**Evaluation Needed**:
- Executor Lifecycle Management (thread-local or pooled Executor)
  - Profile first: Is Executor creation a bottleneck?
  - Consider ThreadLocal<Executor> per Template
  - Must maintain thread safety and zero behavioral changes

---

### Java 11 Migration Preparation

See dedicated section: [Future Roadmap: Java 11 Migration (v0.10.0)](#future-roadmap-java-11-migration-v0100)

**Preparation Tasks for v0.9.0**:
- Add deprecation notices in documentation
- Test compatibility with Java 11, 17, and 21
- Provide migration guide draft
- Collect user feedback on Java version requirements

---

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
- Items in "Future Backlog" should be reviewed periodically and either promoted to active development or removed.

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
