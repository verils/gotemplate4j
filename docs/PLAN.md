# gotemplate4j Long-Term Development Plan

**Last Updated**: 2026-04-30  
**Current Version**: 0.4.0-SNAPSHOT  
**Status**: Experimental - NOT FOR PRODUCTION USE

---

## 🎯 Vision

Transform gotemplate4j from an experimental Go template engine for Java into a production-ready, feature-complete implementation that faithfully replicates Go's `text/template` package while leveraging Java's ecosystem strengths.

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
- ~~**Built-in Functions**: Only 4/18 fully implemented (`print`, `printf`, `println`, `not`)~~ **COMPLETED v0.4.0**
- **PipeNode**: Variable handling incomplete, weak multi-stage pipeline support
- ~~**Error Messages**: Generic, lack context (line/column numbers)~~ **PARTIALLY COMPLETED - Parser errors include line/column**
- ~~**Test Coverage**: Unknown, likely <50%~~ **COMPLETED - 82% instruction coverage, 80% branch coverage**
- **Performance**: No caching or optimization strategies
- **Documentation**: Minimal Javadoc, no advanced usage guides

### 🔴 Production Blockers
1. ~~Incomplete built-in function implementations~~ **RESOLVED v0.4.0**
2. Weak PipeNode processing
3. ~~Insufficient test coverage~~ **RESOLVED - 82% coverage achieved**
4. ~~Missing error diagnostics~~ **PARTIALLY RESOLVED - Parser includes line/column info**

---

## 🗺️ Strategic Roadmap

### Phase 1: Foundation & Production Readiness (Q2-Q3 2026)
**Goal**: Remove "DON'T USE IN PRODUCTION" warning

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
**Priority**: CRITICAL | **Effort**: 2 weeks

**Tasks**:
- Implement variable assignment in pipes: `{{$var := .Value | upper}}`
- Support multi-stage pipeline transformations
- Handle type conversions between pipeline stages
- Add proper error propagation

**Current Issue** (Executor.java:178-179):
```java
for (VariableNode variable : pipeNode.getVariables()) {
    // EMPTY - not implemented!
}
```

**Acceptance Criteria**:
- Variable declarations work in all contexts
- Pipeline chains execute correctly
- Type errors produce clear messages

---

#### 1.3 Enhance Error Diagnostics
**Priority**: HIGH | **Effort**: 1 week | **Status**: ✅ PARTIALLY COMPLETED

~~**Improvements**:~~
- ~~Add line/column numbers to all parse errors~~ **DONE - Parser.java includes line/column info**
- Include context snippets in error messages
- Provide suggestions for common mistakes
- Create error code system for documentation

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
**Priority**: HIGH | **Effort**: Ongoing | **Status**: ✅ COMPLETED

~~**Target**: >80% code coverage~~ **ACHIEVED - 82% instruction, 80% branch**

~~**Test Categories**:~~
- ~~Unit tests for all built-in functions~~ (74 tests)
- ~~Integration tests for complex templates~~
- ~~Edge case testing (null values, empty collections)~~
- ~~Error path validation~~
- Unicode/internationalization tests
- Performance regression tests

~~**Tools**:~~
- ~~Configure JaCoCo for coverage reporting~~ **DONE - Configured in pom.xml**
- Add mutation testing (PITest)

---

### Phase 2: Feature Completeness (Q4 2026 - Q1 2027)
**Goal**: Full Go template specification compliance

#### 2.1 Advanced Template Features
**Priority**: HIGH | **Effort**: 3-4 weeks

**Missing Features**:
- [ ] Full `{{block}}` action with proper overriding semantics
- [ ] Template inheritance patterns
- [ ] Custom delimiters (currently hardcoded `{{` `}}`)
- [ ] Enhanced whitespace control (`-` trim marker edge cases)
- [ ] Nested template execution with proper context passing
- [ ] Template cloning for thread safety

---

#### 2.2 Type System Enhancements
**Priority**: MEDIUM | **Effort**: 2-3 weeks

**Improvements**:
- Support public fields (not just getter methods)
- Better array/collection iteration with index tracking
- Custom type adapters/converter registry
- Improved null-safety with default values
- Support for Java 8+ Optional types
- Enum handling improvements

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

## 🚀 Quick Wins (< 1 Week Each)

These can be implemented immediately for high impact:

1. **Implement `len` function** - Simple, frequently used
2. **Implement `eq` function** - Most common comparison
3. **Add line numbers to errors** - Easy UX improvement
4. **Update README version** - Currently shows 0.3.1, should be 0.3.2
5. **Add basic Javadoc** - Start with public API classes
6. **Configure JaCoCo** - Visibility into test coverage
7. **Add CONTRIBUTING.md** - Lower barrier for contributors

---

## 📈 Success Metrics

### Technical Metrics
- [x] All 18+ built-in functions fully implemented
- [x] Test coverage >80% (measured by JaCoCo)
- [ ] Zero critical/high severity bugs
- [ ] Performance within 2x of Go's native implementation
- [ ] Mutation testing score >70%

### Documentation Metrics
- [ ] 100% public API documented with Javadoc
- [ ] At least 10 comprehensive usage examples
- [ ] Migration guide complete
- [ ] Architecture diagram published

### Release Milestones
- [x] Version 0.4.0: All comparison/logical operators
- [x] Version 0.5.0: All collection functions
- [x] Version 0.6.0: Complete built-in functions
- [ ] Version 0.7.0: Performance optimizations
- [ ] Version 0.8.0: Documentation complete
- [ ] Version 1.0.0: Production ready (remove experimental warning)

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
