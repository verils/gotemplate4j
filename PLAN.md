# gotemplate4j Development Plan

**Last Updated**: 2026-05-05  
**Current Version**: 0.4.0  
**Next Version**: 0.5.0 (dev branch)  
**Current Focus**: Go `text/template` compatibility audit and v0.5.0 readiness

---

## Direction

gotemplate4j should remain a small, Java 8-compatible implementation of Go's `text/template` semantics for Java applications. New work should prioritize compatibility evidence, clear Java-specific deviations, and regression tests over broad feature expansion.

## Working Constraints

- Keep Java 8 compatibility.
- Use `./mvnw`, not `mvn`.
- Avoid new runtime dependencies unless a task cannot be completed well with vanilla Java.
- Preserve backward compatibility unless a documented Go compatibility fix requires a behavior change.
- Treat compatibility status as behavior-driven: a row is only resolved when covered by focused tests, explicitly documented as a Java deviation, or deliberately deferred.

## Short Term

Target: finish the v0.5.0 compatibility audit and resolve high-risk semantic gaps.

### Go Compatibility Audit

Reference: Go `text/template` documentation: https://pkg.go.dev/text/template

Status meanings:
- **Mostly Compatible**: implemented and covered, but needs direct Go comparison or edge-case review.
- **Needs Audit**: implementation likely exists, but behavior has not been reviewed closely enough.
- **Known Gap**: Go feature is not implemented or behavior is observably different.
- **Java Deviation**: intentionally different because of Java type system or public API constraints.
- **Not Applicable**: Go feature has no useful Java equivalent.

#### Actions and Control Flow

| Go behavior | Status | Evidence | Next action |
| --- | --- | --- | --- |
| `if`, `else`, `else if` | Mostly Compatible | `ExecutorEdgeCaseTest`, `ParserBranchScopeTest`, `ParserCanonicalTest` | Compare truthiness and `else if` parse tree against Go cases. |
| `range` over arrays and Java collections | Mostly Compatible | `RangeIndexTest`, `ExecutorEdgeCaseTest` | Add canonical empty `range ... else` tests for list, array, map. |
| `range` over maps | Mostly Compatible | `RangeIndexTest` | Verify key/value binding and document map ordering policy. Go sorts basic ordered keys; Java currently follows map implementation order. |
| `range` over integer, channel, `iter.Seq`, `iter.Seq2` | Known Gap | No parser/executor support | Decide whether to defer as non-v0.5 Java compatibility work. |
| `with`, `else`, `else with` | Mostly Compatible | `ExecutorEdgeCaseTest`, `NestedTemplateContextTest` | Add direct `else with` canonical tests. |
| `template "name"` with nil data | Needs Audit | `ParserTemplateDefinitionTest`, `TemplateTest` | Add execution tests for omitted pipeline behavior. |
| Nested template definitions | Needs Audit | `ParserTemplateDefinitionTest` | Add cases for definitions inside definitions and override order. |

#### Pipelines and Variables

| Go behavior | Status | Evidence | Next action |
| --- | --- | --- | --- |
| Parenthesized pipeline arguments | Mostly Compatible | `ParserPipelineCommandTest`, executor support for nested `PipeNode` | Add execution tests with nested functions and template invocation. |
| Variable assignment `$x = pipeline` | Needs Audit | Parser accepts `ASSIGN`; executor stores final value | Add tests distinguishing declaration from reassignment and scope behavior. |
| Variable scope ends at matching `end` | Mostly Compatible | `RangeIndexTest`, `ParserBranchScopeTest` | Strengthen execution tests for `if`, `with`, `range`, and root scope. |

#### Data Access and Type Semantics

| Go behavior | Status | Evidence | Next action |
| --- | --- | --- | --- |
| Struct/public field access | Java Deviation | `PublicFieldTest` | Document mapping: Java getters and public fields correspond to Go exported field-style access. |
| Method invocation with no args in chains | Needs Audit | Some enum method tests | Review JavaBean getters versus arbitrary public methods. |
| Method invocation with arguments in pipelines | Known Gap | No general method-call executor path | Decide whether to implement Java method calls or document function-only model. |
| Map key access via `.Key` | Mostly Compatible | `ExecutorEdgeCaseTest`, `OptionalSupportTest` | Add missing-key behavior tests. |
| Missing map key option `missingkey=default/zero/error` | Known Gap | No `Option` API | Decide whether v0.5 needs `Template.option(...)` or a documented default. |
| Nil/null behavior | Mostly Compatible | `NullSafetyTest`, `ExecutorEdgeCaseTest` | Compare printed null/missing values against Go `<no value>` behavior. |
| Java `Optional` unwrapping | Java Deviation | `OptionalSupportTest`, `NullSafetyTest` | Document as Java-specific convenience. |
| Enum rendering and enum methods | Java Deviation | `EnumHandlingTest` | Document as Java-specific behavior. |
| Complex number constants | Mostly Compatible | `ParserNumberTest`, `ComplexTest` | Compare formatting and overflow behavior with Go fixtures. |

#### Built-in Functions

| Go behavior | Status | Evidence | Next action |
| --- | --- | --- | --- |
| `and`, `or`, `not` | Mostly Compatible | `FunctionsLogicalTest` | Confirm short-circuit behavior, not just returned value. |
| `eq`, `ne`, `lt`, `le`, `gt`, `ge` | Mostly Compatible | `FunctionsComparisonTest` | Audit Go comparison rules for signed, unsigned, and float mixed numeric cases. |
| `index` | Mostly Compatible | `FunctionsCollectionTest` | Audit multi-level indexing and missing-key behavior. |
| `slice` | Mostly Compatible | `FunctionsCollectionTest` | Audit one-arg, two-arg, and three-index slice forms. |
| `call` | Known Gap | `FunctionsFormattingAndCallTest` | Current implementation calls `Function`; Go supports function-valued fields/map entries with error returns. |
| `html`, `js`, `urlquery` | Mostly Compatible | `FunctionsEscapingTest` | Compare exact escaping output with Go fixtures. |
| `print`, `printf`, `println` | Mostly Compatible | `FunctionsFormattingAndCallTest`, broad usage | Compare exact spacing and newline behavior. |
| `deepEqual`, `typeof`, `kindOf` | Java Deviation | `FunctionsIntrospectionTest` | Document as Java/library extensions, not Go predefined functions. |
| `default` | Java Deviation | `NullSafetyTest` | Document as a library extension, not a Go predefined function. |

#### Template Set and API Behavior

| Go behavior | Status | Evidence | Next action |
| --- | --- | --- | --- |
| Custom delimiters apply to subsequent parse calls | Mostly Compatible | `CustomDelimiterTest`, `LexerTrimDelimiterTest` | Add parse-after-parse delimiter inheritance case if needed. |
| `Funcs` before parse | Java Deviation | Constructor-based custom functions | Document Java API difference from Go's fluent `Funcs`. |
| `Delims` fluent API | Java Deviation | Constructor-based delimiters | Document constructor-based delimiter support. |
| `Option("missingkey=...")` | Known Gap | No API | Decide whether v0.5 requires this for compatibility. |
| `ParseFiles`, `ParseGlob`, `ParseFS` | Known Gap | Reader/InputStream parsing exists | Decide whether file/glob helpers are in scope. |
| `Lookup`, `DefinedTemplates`, `Templates`, `Name`, associated `New` | Known Gap | `executeTemplate` exists; no introspection API | Decide whether public API parity is v0.5 scope or later. |
| Parallel execution safety | Mostly Compatible | `TemplateCloningTest` | Add direct parallel execution on one parsed template with separate writers. |

#### Error Behavior

| Go behavior | Status | Evidence | Next action |
| --- | --- | --- | --- |
| Parse errors include useful location/context | Java Deviation | `ParserErrorContextTest`, `ParserCoverageErrorTest` | Compare parse failure categories with Go, not exact strings. |
| Execution stops on function errors | Needs Audit | Function tests cover wrong arity | Add tests for partial output and wrapped `TemplateExecutionException`. |
| Writer errors propagate | Needs Audit | No focused tests found | Add failing `Writer` test. |

### Short-Term Completion Gate

- All `Needs Audit` rows above have focused tests or a documented decision.
- All `Known Gap` rows are either implemented, explicitly deferred, or documented as Java-specific deviations.
- Java-specific extensions are clearly separated from Go compatibility claims.
- A small canonical fixture set compares high-risk behavior against Go `text/template` output.
- `./mvnw test` succeeds on Java 8.
- `./mvnw verify "-Dgpg.skip=true"` succeeds before release tagging.

## Medium Term

Target: improve confidence, maintainability, and runtime behavior after v0.5.0 compatibility decisions are settled.

### Compatibility Fixture Suite

- Build a compact canonical fixture set with expected Go outputs for high-risk behavior:
  `if`, `range`, `with`, template invocation, variables, missing keys, escaping, formatting, and errors.
- Add a repeatable process for refreshing expected outputs from Go when Go `text/template` changes.
- Keep Java deviations in separate fixtures so compatibility gaps are not hidden by Java-specific convenience behavior.

### API and Documentation

- Write a migration guide from Go templates to gotemplate4j, including Java object access rules.
- Document Java-specific extensions: `default`, `deepEqual`, `typeof`, `kindOf`, Optional unwrapping, enum handling, constructor-based funcs/delims.
- Document unsupported or deferred Go APIs: `Option`, file/glob helpers, template introspection, function-valued fields for `call`, channels and iterators.
- Improve Javadoc for public APIs that are part of the intended stable surface.

### Quality and Tooling

- Improve coverage headroom above the configured JaCoCo thresholds.
- Add tests for error paths that are currently only incidentally covered.
- Consider static analysis tooling such as SpotBugs or PMD after compatibility behavior stabilizes.
- Review deprecated APIs, magic strings, and input validation boundaries.

### Performance Baseline

- Add JMH or a lightweight benchmark harness for parse, execute, field access, function calls, and range-heavy templates.
- Establish baseline numbers before adding reflection caching or AST caching.
- Add performance regression checks only after the benchmark harness is stable enough to avoid noisy failures.

## Long Term

Target: make the library easier to operate at scale and clearer for downstream users.

### Runtime Optimization

- Add reflection metadata caching for JavaBean methods and public fields.
- Evaluate AST caching for repeated template parsing.
- Reduce avoidable allocations in hot execution paths.
- Consider StringBuilder pooling only if benchmarks show meaningful pressure.
- Defer bytecode generation unless benchmarks show the simpler optimizations are insufficient.

### API Parity Decisions

- Decide whether Go-style `Option("missingkey=...")` belongs in the public API.
- Decide whether `Lookup`, `DefinedTemplates`, `Templates`, `Name`, and associated `New` are needed for v1.0 API stability.
- Decide whether file helpers such as `ParseFiles` and `ParseGlob` fit the Java API or should remain caller-managed IO.
- Decide whether general Java method invocation in templates is desirable, given security and compatibility tradeoffs.

### Ecosystem

- Publish focused examples for common use cases: configuration generation, emails, code generation, custom functions, and JavaBeans/maps.
- Add a troubleshooting guide for parse errors, missing fields, null values, and function failures.
- Review framework integrations only after the core API and compatibility story are stable.

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
