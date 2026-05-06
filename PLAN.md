# gotemplate4j Development Plan

**Last Updated**: 2026-05-06  
**Current Version**: 0.5.0 release candidate  
**Current Focus**: complete the v0.5.0 Go `text/template` compatibility audit and release readiness

---

## Direction

gotemplate4j should remain a small, Java 8-compatible implementation of Go's `text/template` semantics for Java applications. The v0.5.0 line is the compatibility-audit release: behavior must be covered by focused tests, documented as a Java-specific deviation, or explicitly deferred.

## Working Constraints

- Keep Java 8 compatibility.
- Use `./mvnw`, not `mvn`.
- Avoid runtime dependencies beyond vanilla Java.
- Preserve backward compatibility unless a documented Go compatibility fix requires a behavior change.
- Keep Go-compatible behavior, Java-specific behavior, and unsupported Go APIs clearly separated.

## v0.5.0 Release Scope

Target: ship a compatibility-audit release with no unresolved audit rows. v0.5.0 does not need to implement every Go API, but every gap must be visible and intentional.

Status meanings:
- **Covered**: implemented and protected by focused tests.
- **Java Deviation**: intentionally different because of Java type system or public API constraints; must be documented.
- **Deferred**: not in v0.5.0 scope; must be visible in release notes and user documentation.
- **Not Applicable**: Go feature has no useful Java equivalent.

### Actions and Control Flow

| Go behavior | v0.5.0 status | Evidence | Decision |
| --- | --- | --- | --- |
| `if`, `else`, `else if` | Covered | `ParserBranchScopeTest`, `ParserCanonicalTest`, `GoCompatibilityAuditTest` | Keep supported. |
| `range` over arrays and Java collections | Covered | `RangeIndexTest`, `ExecutorTest`, `GoCompatibilityAuditTest` | Empty and null ranges execute `else`. |
| `range` over maps | Covered with Java ordering | `RangeIndexTest`, `GoCompatibilityAuditTest` | Java `Map` iteration order is preserved; unlike Go, basic keys are not sorted by the engine. |
| `range` over integer, channel, `iter.Seq`, `iter.Seq2` | Deferred | No Java executor model | Defer beyond v0.5.0. Channels and Go iterators are not Java concepts; integer range can be revisited later. |
| `break` and `continue` in `range` | Covered | `BreakContinueTest` | Keep supported. |
| `with`, `else`, `else with` | Covered | `NestedTemplateContextTest`, `GoCompatibilityAuditTest` | Keep supported. |
| `template "name"` with omitted pipeline | Covered | `GoCompatibilityAuditTest` | Executes named template with null dot. |
| Template definitions and override order | Covered | `ParserTemplateDefinitionTest`, `BlockOverrideTest`, `GoCompatibilityAuditTest` | Later non-empty definitions replace earlier ones. |

### Pipelines and Variables

| Go behavior | v0.5.0 status | Evidence | Decision |
| --- | --- | --- | --- |
| Parenthesized pipeline arguments | Covered | `ParserPipelineCommandTest`, `GoCompatibilityAuditTest` | Keep supported. |
| Variable declaration `$x := pipeline` | Covered | `PipeNodeVariableTest`, `GoCompatibilityAuditTest` | Declarations are available within their parse scope. |
| Variable assignment `$x = pipeline` | Covered | `GoCompatibilityAuditTest` | Assignment token is accepted and updates the current variable binding. |
| Variable scope ends at matching `end` | Covered | `ParserBranchScopeTest`, `GoCompatibilityAuditTest` | Parser rejects references outside branch/range scope. |

### Data Access and Type Semantics

| Go behavior | v0.5.0 status | Evidence | Decision |
| --- | --- | --- | --- |
| Struct/exported field access | Java Deviation | `PublicFieldTest` | Java getters and public fields correspond to Go exported field-style access. |
| Public no-arg method access in chains | Java Deviation | `EnumHandlingTest`, `GoCompatibilityAuditTest` | Public no-arg methods can be accessed as field-chain segments; methods with arguments are not supported. |
| Method invocation with arguments in pipelines | Deferred | No general method-call executor path | Keep the safer function-only model for v0.5.0. |
| Map key access via `.Key` | Covered with Java default | `OptionalSupportTest`, `GoCompatibilityAuditTest` | Missing keys evaluate to null and print as empty output. |
| `Option("missingkey=default/zero/error")` | Deferred | No `Option` API | Document unsupported in v0.5.0. |
| Nil/null behavior | Covered with Java default | `NullSafetyTest`, `ExecutorTest`, `GoCompatibilityAuditTest` | Null values are falsey and print as empty output, not Go's visible `<no value>` marker. |
| Java `Optional` unwrapping | Java Deviation | `OptionalSupportTest`, `NullSafetyTest` | Keep as Java-specific convenience. |
| Enum rendering and enum methods | Java Deviation | `EnumHandlingTest`, `GoCompatibilityAuditTest` | Enums render via `toString()` and expose public no-arg methods such as `name` and `ordinal`. |
| Complex number constants | Covered parser support | `ParserNumberTest`, `ComplexTest` | Parser support remains; full Go formatting parity is deferred. |

### Built-in Functions

| Go behavior | v0.5.0 status | Evidence | Decision |
| --- | --- | --- | --- |
| `and`, `or`, `not` | Covered | `FunctionsLogicalTest`, `GoCompatibilityAuditTest` | `and` and `or` short-circuit during execution. |
| `eq`, `ne`, `lt`, `le`, `gt`, `ge` | Covered | `FunctionsComparisonTest` | Keep current Java numeric/string comparison behavior; deeper mixed numeric parity can be refined later. |
| `index` | Covered | `FunctionsCollectionTest` | Supports Java maps, arrays, strings, and lists as implemented. |
| `slice` | Covered | `FunctionsCollectionTest` | Supports Java strings and arrays. |
| `call` | Java Deviation | `FunctionsFormattingAndCallTest` | Calls gotemplate4j `Function` instances only; Go function-valued fields/map entries are deferred. |
| `html`, `js`, `urlquery` | Covered | `FunctionsEscapingTest` | Keep current escaping behavior. |
| `print`, `printf`, `println` | Covered | `FunctionsFormattingAndCallTest`, `FormattingFunctionTest` | Keep current Java formatting behavior. |
| `deepEqual`, `typeof`, `kindOf` | Java Deviation | `FunctionsIntrospectionTest` | Document as library extensions, not Go predefined functions. |
| `default` | Java Deviation | `NullSafetyTest` | Document as a library extension, not a Go predefined function. |

### Template Set and API Behavior

| Go behavior | v0.5.0 status | Evidence | Decision |
| --- | --- | --- | --- |
| Custom delimiters | Covered | `CustomDelimiterTest`, `LexerTrimDelimiterTest` | Java constructor-based delimiter API remains. |
| `Funcs` before parse | Java Deviation | Constructor-based custom functions | Java API uses constructors instead of Go's fluent `Funcs`. |
| `Delims` fluent API | Java Deviation | Constructor-based delimiters | Java API uses constructors instead of Go's fluent `Delims`. |
| `Option("missingkey=...")` | Deferred | No API | Defer beyond v0.5.0. |
| `ParseFiles`, `ParseGlob`, `ParseFS` | Deferred | `Template.parse(InputStream)`, `Template.parse(Reader)` | Caller-managed IO remains the v0.5.0 Java API. |
| `Lookup`, `DefinedTemplates`, `Templates`, `Name`, associated `New` | Deferred | `executeTemplate` exists | Defer public introspection API decisions beyond v0.5.0. |
| Parallel execution safety | Covered | `TemplateCloningTest` | Parsed templates can be copied for concurrent execution with separate writers. |

### Error Behavior

| Go behavior | v0.5.0 status | Evidence | Decision |
| --- | --- | --- | --- |
| Parse errors include useful location/context | Java Deviation | `ParserErrorContextTest`, `ParserCoverageErrorTest` | Error wording is Java-specific; location/context is covered. |
| Execution stops on function errors | Covered | `GoCompatibilityAuditTest` | Runtime function failures are wrapped in `TemplateExecutionException`. |
| Writer errors propagate | Covered | `GoCompatibilityAuditTest` | Writer `IOException` is propagated to callers. |

## v0.5.0 Completion Gate

- No `Needs Audit` or undecided compatibility rows remain.
- All deferred Go APIs are documented in README and CHANGELOG.
- Java-specific extensions are clearly separated from Go compatibility claims.
- Focused compatibility tests cover high-risk behavior in `GoCompatibilityAuditTest`.
- `./mvnw test` succeeds on Java 8.
- `./mvnw verify "-Dgpg.skip=true"` succeeds before release tagging.

## Post-v0.5.0 Work

### Compatibility Fixture Suite

- Build a compact fixture set with expected Go outputs for `if`, `range`, `with`, template invocation, variables, missing keys, escaping, formatting, and errors.
- Add a repeatable process for refreshing expected outputs from Go when Go `text/template` changes.
- Keep Java deviations in separate fixtures so compatibility gaps are not hidden by Java-specific convenience behavior.

### API and Documentation

- Write a migration guide from Go templates to gotemplate4j, including Java object access rules.
- Decide whether Go-style `Option("missingkey=...")` belongs in the public API.
- Decide whether `Lookup`, `DefinedTemplates`, `Templates`, `Name`, and associated `New` are needed for v1.0 API stability.
- Decide whether file helpers such as `ParseFiles` and `ParseGlob` fit the Java API or should remain caller-managed IO.
- Decide whether general Java method invocation with arguments is desirable, given security and compatibility tradeoffs.

### Quality and Tooling

- Improve coverage headroom above the configured JaCoCo thresholds.
- Add tests for error paths that are currently only incidentally covered.
- Consider static analysis tooling such as SpotBugs or PMD after compatibility behavior stabilizes.
- Review deprecated APIs, magic strings, and input validation boundaries.

### Performance Baseline

- Add JMH or a lightweight benchmark harness for parse, execute, field access, function calls, and range-heavy templates.
- Establish baseline numbers before adding reflection caching or AST caching.
- Add performance regression checks only after the benchmark harness is stable enough to avoid noisy failures.

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
