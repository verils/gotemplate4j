# gotemplate4j Long-Term Development Plan

**Last Updated**: 2026-05-05  
**Current Version**: 0.4.0  
**Next Version**: 0.5.0 (dev branch)  
**Status**: Production Ready; v0.5.0 feature development in progress

---

## 🎯 Vision

Evolve gotemplate4j from a production-ready core into a feature-complete implementation that faithfully replicates Go's `text/template` package while leveraging Java's ecosystem strengths.

---

## 📊 Current State Assessment

### ✅ What Works
- Lexer: Complete tokenization of template syntax
- Parser: AST generation for most template constructs
- Executor: Basic template execution with JavaBeans introspection
- Core Actions: `if`, `range`, `with`, `template`, `block/define`
- Complex Numbers: Parsing and formatting support (since v0.3.0)
- Pipeline: Basic pipe operator support

### ⚠️ Known Limitations
- **Performance**: No caching or optimization strategies
- **Documentation**: Javadoc added for public APIs, but advanced usage guides still needed
- **Error Diagnostics**: Line/column info and context snippets implemented, suggestions and error codes pending

### 🔴 Production Blockers
1. ~~Incomplete built-in function implementations~~ **RESOLVED v0.4.0**
2. ~~Weak PipeNode processing~~ **RESOLVED v0.4.0**
3. ~~Missing error diagnostics~~ **RESOLVED - Parser includes line/column info and context snippets**

---

## 🗺️ Strategic Roadmap

### Phase 1: Foundation & Production Readiness (Q2-Q3 2026)
**Goal**: Achieve production-ready status ✅ COMPLETED

#### 1.1 Complete Critical Built-in Functions
**Priority**: CRITICAL | **Effort**: 2-3 weeks | **Status**: ✅ COMPLETED v0.4.0

~~Implement in order of usage frequency:~~

~~**Comparison Operators** (Week 1):~~
- ~~`eq` - Equality comparison~~
- ~~`ne` - Not equal comparison~~
- ~~`lt` - Less than~~
- ~~`le` - Less than or equal~~
- ~~`gt` - Greater than~~
- ~~`ge` - Greater than or equal~~

~~**Logical Operators** (Week 2):~~
- ~~`and` - Short-circuit logical AND~~
- ~~`or` - Short-circuit logical OR~~

~~**Collection Functions** (Week 3):~~
- ~~`len` - Length of arrays/slices/maps/strings~~
- ~~`index` - Access array/map elements by index/key~~
- ~~`slice` - Slice arrays/strings~~

~~**Utility Functions** (Week 4):~~
- ~~`call` - Call functions dynamically~~
- ~~`html` - HTML escaping~~
- ~~`js` - JavaScript escaping~~
- ~~`urlquery` - URL query escaping~~

**Additional Functions Implemented (Phase 2.3):**
- ~~`deepEqual` - Deep equality comparison~~
- ~~`typeof` - Type inspection~~
- ~~`kindOf` - Kind inspection~~

**Acceptance Criteria**:
- ~~All functions pass Go template compatibility tests~~
- ~~Comprehensive unit tests for edge cases~~ (74 tests in BuiltInFunctionsTest)
- ~~Performance benchmarks established~~

---

#### 1.2 Fix PipeNode Processing
**Priority**: CRITICAL | **Effort**: 2 weeks | **Status**: ✅ COMPLETED v0.4.0

~~**Tasks**:~~
- ~~Implement variable assignment in pipes: `{{$var := .Value | upper}}`~~
- ~~Support multi-stage pipeline transformations~~
- ~~Handle type conversions between pipeline stages~~
- ~~Add proper error propagation~~

**Implementation Details**:
- Added variable storage mechanism using `Map<String, Object>` in Executor
- Implemented variable assignment in `executePipe()` method
- Added `executeVariable()` method for variable lookup at runtime
- Maintained Go template's parse-time variable validation (Parser.findVariable)
- Updated all execution methods to pass variable context through call chain

**Acceptance Criteria**:
- ✅ Variable declarations work in all contexts
- ✅ Pipeline chains execute correctly
- ✅ Type errors produce clear messages
- ✅ 16 comprehensive tests added (PipeNodeVariableTest)
- ✅ All 245 tests pass with coverage maintained above the v0.4.0 quality target

---

#### 1.3 Enhance Error Diagnostics
**Priority**: HIGH | **Effort**: 1 week | **Status**: ✅ COMPLETED

~~**Improvements**:~~
- ~~Add line/column numbers to all parse errors~~ **DONE - Parser.java includes line/column info**
- ~~Include context snippets in error messages~~ **DONE - Added buildErrorMessage() method with template context**
- Provide suggestions for common mistakes
- Create error code system for documentation

**Implementation Details**:
- Added `buildErrorMessage()` method in Parser.java to generate detailed error messages
- Added `throwUnexpectErrorWithContext()` for context-aware error reporting
- Error messages now include line/column numbers and template snippet with pointer
- Updated critical error locations to use context-aware error throwing

**Known Issues**:
- Additional tests needed to cover all error context paths
- Target: Add tests in v0.5.0 to improve coverage headroom above the configured JaCoCo thresholds

**Example**:
```
Before: "unexpected token"
After:  "Parse error at line 5, column 12: unexpected 'else' without matching 'if'"
        "  {{if .Condition}}"
        "    Hello"
        "  {{else}}  ← Error here"
```

---

#### 1.4 Increase Test Coverage
**Priority**: MANDATORY (Enforced by CI/CD) | **Effort**: Ongoing | **Status**: Mostly Complete

**Coverage Requirements** (Enforced by JaCoCo in pom.xml):
- Instruction Coverage: ≥ 85%
- Branch Coverage: ≥ 75%

**Note**: Code coverage is a mandatory quality gate enforced automatically during builds, not a version-specific goal. All changes must maintain these thresholds.

**Rationale**: Template engines require higher coverage than typical applications due to:
- Complex parsing logic with numerous edge cases
- Security implications (template injection prevention)
- 20+ AST node types requiring thorough testing
- Critical execution paths that must be bulletproof
- Backward compatibility requirements for a library

**Test Categories**:
- Unit tests for all built-in functions
- Integration tests for complex templates
- Edge case testing (null values, empty collections, malformed syntax)
- Error path validation (all error branches tested)
- Unicode/internationalization tests
- Performance regression tests
- Security-focused tests (injection attempts, resource exhaustion)

**Tools**:
- JaCoCo for coverage reporting (configured in pom.xml)
- Mutation testing (PITest) - planned
- Coverage thresholds enforced in CI/CD pipeline

**Current Status**: v0.4.0 release notes report 82% instruction and 79% branch coverage; run `./mvnw clean verify "-Dgpg.skip=true"` to validate the current workspace against the configured gate.
**Strategy**: Add tests incrementally when implementing new features or fixing bugs, ensuring coverage never drops below thresholds.

---

### Phase 2: Feature Completeness (Q4 2026 - Q1 2027)
**Goal**: Full Go template specification compliance

#### 2.1 Advanced Template Features (v0.5.0 Focus)
**Priority**: HIGH | **Effort**: 3-4 weeks | **Status**: PLANNED for v0.5.0

**Missing Features**:
- [ ] Full `{{block}}` action with proper overriding semantics
- [ ] Template inheritance patterns
- [ ] Custom delimiters (currently hardcoded `{{` `}}`)
- [ ] Enhanced whitespace control (`-` trim marker edge cases)
- [ ] Nested template execution with proper context passing
- [ ] Template cloning for thread safety

---

#### 2.2 Type System Enhancements (v0.5.0 Focus)
**Priority**: MEDIUM | **Effort**: 2-3 weeks | **Status**: PLANNED for v0.5.0

**Improvements**:
- [ ] Support public fields (not just getter methods)
- [ ] Better array/collection iteration with index tracking
- [ ] Custom type adapters/converter registry
- [ ] Improved null-safety with default values
- [ ] Support for Java 8+ Optional types
- [ ] Enum handling improvements

---

#### 2.3 Remaining Built-in Functions
**Priority**: MEDIUM | **Effort**: 1-2 weeks | **Status**: ✅ COMPLETED v0.4.0

~~**Functions to Implement**:~~
- ~~`deepEqual` - Deep equality comparison~~ **DONE**
- `indir` - Indirect reference through pointers (Java equivalent) - *Not applicable in Java*
- ~~`typeof` - Type inspection~~ **DONE**
- ~~`kindOf` - Kind inspection~~ **DONE**

---

### Phase 3: Performance & Optimization (Q2 2027)
**Goal**: Production-grade performance characteristics

#### 3.1 Caching Strategies
**Priority**: MEDIUM | **Effort**: 2 weeks

**Optimizations**:
- AST caching for repeated template parsing
- Method reflection caching (cache `Method` objects)
- StringBuilder pooling for string operations
- Template compilation to bytecode (advanced)

**Expected Impact**: 5-10x performance improvement for repeated executions

---

#### 3.2 Memory Optimization
**Priority**: LOW | **Effort**: 1-2 weeks

**Areas**:
- Reduce object allocation in hot paths
- Lazy evaluation for unused branches
- Stream-based execution for large datasets
- GC pressure reduction

---

#### 3.3 Benchmarking Suite
**Priority**: MEDIUM | **Effort**: 1 week

**Deliverables**:
- JMH benchmarks for core operations
- Comparison with Go's native implementation
- Performance regression detection in CI
- Public benchmark results documentation

---

### Phase 4: Documentation & Developer Experience (Q3 2027)
**Goal**: Professional-grade documentation and tooling

#### 4.1 Comprehensive Documentation
**Priority**: HIGH | **Effort**: 2-3 weeks

**Documentation Needed**:
- [ ] Complete Javadoc for all public APIs
- [ ] Usage examples for all features
- [ ] Migration guide from Go templates
- [ ] API reference with parameter descriptions
- [ ] Troubleshooting guide
- [ ] Architecture overview with diagrams
- [ ] Contributing guidelines
- [ ] FAQ section

---

#### 4.2 Code Quality Improvements
**Priority**: MEDIUM | **Effort**: 1-2 weeks

**Tasks**:
- Resolve `@Deprecated` annotations (migrate or remove)
- Extract magic strings to constants
- Improve method naming consistency
- Add input validation at API boundaries
- Consider modularization (JPMS)

---

#### 4.3 Developer Tooling
**Priority**: LOW | **Effort**: 1 week

**Additions**:
- Maven dependency-check plugin (security scanning)
- SpotBugs static analysis
- PMD code quality checks
- Automated CHANGELOG generation
- Release automation

---

### Phase 5: Ecosystem & Community (Q4 2027+)
**Goal**: Build community and ecosystem around the project

#### 5.1 Framework Integrations
**Priority**: LOW | **Effort**: VARIABLE

**Potential Integrations**:
- Spring Boot starter module
- Jakarta EE integration
- Micronaut/Quarkus support
- Android compatibility verification

---

#### 5.2 Example Repository
**Priority**: MEDIUM | **Effort**: 1-2 weeks

**Examples to Create**:
- Basic usage scenarios
- Email template generation
- Code generation use cases
- Configuration file templating
- Internationalization examples
- Custom function development

---

#### 5.3 Community Building
**Priority**: LOW | **Effort**: ONGOING

**Activities**:
- Seek feedback on missing features
- Participate in Java template engine discussions
- Compare with alternatives (Thymeleaf, Freemarker, etc.)
- Publish articles/tutorials
- Conference presentations

---

## 📈 Success Metrics

### Technical Metrics
- [x] All 18+ built-in functions fully implemented
- [ ] Test coverage headroom improved above configured thresholds (≥85% instruction, ≥75% branch - enforced by JaCoCo)
- [x] Zero critical/high severity bugs
- [ ] Performance within 2x of Go's native implementation
- [ ] Mutation testing score >70%

### Documentation Metrics
- [ ] 100% public API documented with Javadoc
- [ ] At least 10 comprehensive usage examples
- [ ] Migration guide complete
- [ ] Architecture diagram published

### Release Milestones
- [x] Version 0.4.0: All comparison/logical operators + collection functions + complete built-in functions
- [ ] Version 0.5.0: Advanced template features and type system enhancements
- [ ] Version 0.6.0: Performance optimizations and caching strategies
- [ ] Version 0.7.0: Documentation complete with examples and migration guide
- [ ] Version 1.0.0: Stable API and compatibility guarantee

---

## ⚠️ Risk Assessment

### High Risk Areas

**1. PipeNode Refactoring**
- **Risk**: Breaking existing functionality
- **Mitigation**: Comprehensive regression testing, incremental rollout

**2. Feature Creep**
- **Risk**: Over-complicating the simple design
- **Mitigation**: Stick to Go template spec, resist non-standard additions

**3. Performance vs. Compatibility**
- **Risk**: Optimizations may break Go template semantics
- **Mitigation**: Extensive compatibility testing after each optimization

**4. Java 8 Compatibility**
- **Risk**: Modern optimizations may require newer Java
- **Mitigation**: Maintain Java 8 baseline, create Java 11+ variant if needed

---

## 🔄 Maintenance Strategy

### Ongoing Tasks
- Monthly dependency security scans
- Quarterly performance benchmarks
- Bi-annual review against Go's template updates
- Continuous test coverage monitoring

### Release Cadence
- Bug fixes: As needed (patch releases)
- Minor features: Monthly (minor releases)
- Major features: Quarterly (minor releases)
- Breaking changes: Rarely, with migration guides (major releases)

---

*This is a living document. Update it as the project evolves and priorities shift.*
