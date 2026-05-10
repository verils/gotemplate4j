# gotemplate4j Development Plan

**Last Updated**: 2026-05-10  
**Current Version**: 0.6.0  
**Next Version**: 0.7.0  
**Current Focus**: production-grade documentation and enhanced Go template compatibility  
**Status**: Stage 1 in progress - Documentation structure created, core docs started

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

### ✅ Completed (v0.6.0)

- MissingKeyPolicy configuration (DEFAULT/ZERO/ERROR)
- Template introspection API (name(), hasTemplate(), definedTemplates(), lookup(), templates())
- File system helpers (parseFile(), parseFiles(), parseGlob())
- Performance benchmarking (TemplateBenchmark)
- Initial compatibility and migration documentation

### 🚧 In Progress (v0.7.0 - Stage 1)

- ✅ Documentation directory structure created
- ✅ docs/index.md - Main documentation hub
- ✅ docs/getting-started/installation.md - Installation guide
- ✅ docs/getting-started/quick-start.md - Quick start tutorial
- ⏳ docs/getting-started/basic-concepts.md - Next to create
- ⏳ Remaining documentation sections

### 📋 Planned (v0.7.0 - Stages 2-5)

See detailed plan below.

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

### Stage 1: Production-Grade Documentation Structure ✅ Partially Complete

**Status**: Directory structure created, core docs started. Continuing with remaining sections.

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

**Next Steps for Stage 1:**
- ⏳ Create docs/getting-started/basic-concepts.md
- ⏳ Create all User Guide sections
- ⏳ Create API Reference sections
- ⏳ Create Advanced Topics sections
- ⏳ Create Examples sections
- ⏳ Create FAQ

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

**Priority 2: Enhanced Null Value Display**

Go displays `<no value>` for missing/null values by default. Add optional policy:

```java
Template template = new Template("demo")
    .withMissingKeyPolicy(MissingKeyPolicy.ERROR)
    .withNullDisplay("<no value>");  // New method
```

Or via option:
```java
template.option("null_display=<no value>");
```

Benefits:
- Easier debugging of missing values
- Closer to Go's default behavior
- Helps identify data issues in production

**Priority 3: Map Key Sorting Option**

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

**Current Position**: Stage 1 - Step 2 (Creating Getting Started docs)

1. ✅ Create documentation directory structure and templates
2. 🚧 Write Getting Started section (installation ✅, quick start ✅, concepts ⏳)
3. ⏳ Implement integer range support with tests
4. ⏳ Implement null display policy with tests
5. ⏳ Write User Guide sections while implementing features
6. ⏳ Implement map key sorting option with tests
7. ⏳ Implement block action support with tests
8. ⏳ Write API Reference documentation
9. ⏳ Create comprehensive examples
10. ⏳ Write Advanced Topics and FAQ
11. ⏳ Enhance existing compatibility and migration docs
12. ⏳ Update README and CHANGELOG
13. ⏳ Final review and verification

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

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
