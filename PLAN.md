# gotemplate4j Development Plan

**Last Updated**: 2026-05-06  
**Current Version**: 0.5.0  
**Next Version**: 0.6.0  
**Current Focus**: post-audit API usability, migration guidance, and high-value compatibility gaps

---

## Direction

gotemplate4j should remain a small, Java 8-compatible implementation of Go's `text/template` semantics for Java applications. v0.5.0 completed the core compatibility audit. v0.6.0 should turn that audit result into better user-facing API behavior, migration guidance, and a small number of high-value compatibility improvements.

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

| Go behavior                                            | v0.5.0 status              | Evidence                                                                        | Decision                                                                                                    |
|--------------------------------------------------------|----------------------------|---------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `if`, `else`, `else if`                                | Covered                    | `ParserBranchScopeTest`, `ParserCanonicalTest`, `GoCompatibilityAuditTest`      | Keep supported.                                                                                             |
| `range` over arrays and Java collections               | Covered                    | `RangeIndexTest`, `ExecutorTest`, `GoCompatibilityAuditTest`                    | Empty and null ranges execute `else`.                                                                       |
| `range` over maps                                      | Covered with Java ordering | `RangeIndexTest`, `GoCompatibilityAuditTest`                                    | Java `Map` iteration order is preserved; unlike Go, basic keys are not sorted by the engine.                |
| `range` over integer, channel, `iter.Seq`, `iter.Seq2` | Deferred                   | No Java executor model                                                          | Defer beyond v0.5.0. Channels and Go iterators are not Java concepts; integer range can be revisited later. |
| `break` and `continue` in `range`                      | Covered                    | `BreakContinueTest`                                                             | Keep supported.                                                                                             |
| `with`, `else`, `else with`                            | Covered                    | `NestedTemplateContextTest`, `GoCompatibilityAuditTest`                         | Keep supported.                                                                                             |
| `template "name"` with omitted pipeline                | Covered                    | `GoCompatibilityAuditTest`                                                      | Executes named template with null dot.                                                                      |
| Template definitions and override order                | Covered                    | `ParserTemplateDefinitionTest`, `BlockOverrideTest`, `GoCompatibilityAuditTest` | Later non-empty definitions replace earlier ones.                                                           |

### Pipelines and Variables

| Go behavior                           | v0.5.0 status | Evidence                                                | Decision                                                               |
|---------------------------------------|---------------|---------------------------------------------------------|------------------------------------------------------------------------|
| Parenthesized pipeline arguments      | Covered       | `ParserPipelineCommandTest`, `GoCompatibilityAuditTest` | Keep supported.                                                        |
| Variable declaration `$x := pipeline` | Covered       | `PipeNodeVariableTest`, `GoCompatibilityAuditTest`      | Declarations are available within their parse scope.                   |
| Variable assignment `$x = pipeline`   | Covered       | `GoCompatibilityAuditTest`                              | Assignment token is accepted and updates the current variable binding. |
| Variable scope ends at matching `end` | Covered       | `ParserBranchScopeTest`, `GoCompatibilityAuditTest`     | Parser rejects references outside branch/range scope.                  |

### Data Access and Type Semantics

| Go behavior                                   | v0.5.0 status             | Evidence                                                     | Decision                                                                                                 |
|-----------------------------------------------|---------------------------|--------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| Struct/exported field access                  | Java Deviation            | `PublicFieldTest`                                            | Java getters and public fields correspond to Go exported field-style access.                             |
| Public no-arg method access in chains         | Java Deviation            | `EnumHandlingTest`, `GoCompatibilityAuditTest`               | Public no-arg methods can be accessed as field-chain segments; methods with arguments are not supported. |
| Method invocation with arguments in pipelines | Deferred                  | No general method-call executor path                         | Keep the safer function-only model for v0.5.0.                                                           |
| Map key access via `.Key`                     | Covered with Java default | `OptionalSupportTest`, `GoCompatibilityAuditTest`            | Missing keys evaluate to null and print as empty output.                                                 |
| `Option("missingkey=default/zero/error")`     | Deferred                  | No `Option` API                                              | Document unsupported in v0.5.0.                                                                          |
| Nil/null behavior                             | Covered with Java default | `NullSafetyTest`, `ExecutorTest`, `GoCompatibilityAuditTest` | Null values are falsey and print as empty output, not Go's visible `<no value>` marker.                  |
| Java `Optional` unwrapping                    | Java Deviation            | `OptionalSupportTest`, `NullSafetyTest`                      | Keep as Java-specific convenience.                                                                       |
| Enum rendering and enum methods               | Java Deviation            | `EnumHandlingTest`, `GoCompatibilityAuditTest`               | Enums render via `toString()` and expose public no-arg methods such as `name` and `ordinal`.             |
| Complex number constants                      | Covered parser support    | `ParserNumberTest`, `ComplexTest`                            | Parser support remains; full Go formatting parity is deferred.                                           |

### Built-in Functions

| Go behavior                        | v0.5.0 status  | Evidence                                                   | Decision                                                                                                |
|------------------------------------|----------------|------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `and`, `or`, `not`                 | Covered        | `FunctionsLogicalTest`, `GoCompatibilityAuditTest`         | `and` and `or` short-circuit during execution.                                                          |
| `eq`, `ne`, `lt`, `le`, `gt`, `ge` | Covered        | `FunctionsComparisonTest`                                  | Keep current Java numeric/string comparison behavior; deeper mixed numeric parity can be refined later. |
| `index`                            | Covered        | `FunctionsCollectionTest`                                  | Supports Java maps, arrays, strings, and lists as implemented.                                          |
| `slice`                            | Covered        | `FunctionsCollectionTest`                                  | Supports Java strings and arrays.                                                                       |
| `call`                             | Java Deviation | `FunctionsFormattingAndCallTest`                           | Calls gotemplate4j `Function` instances only; Go function-valued fields/map entries are deferred.       |
| `html`, `js`, `urlquery`           | Covered        | `FunctionsEscapingTest`                                    | Keep current escaping behavior.                                                                         |
| `print`, `printf`, `println`       | Covered        | `FunctionsFormattingAndCallTest`, `FormattingFunctionTest` | Keep current Java formatting behavior.                                                                  |
| `deepEqual`, `typeof`, `kindOf`    | Java Deviation | `FunctionsIntrospectionTest`                               | Document as library extensions, not Go predefined functions.                                            |
| `default`                          | Java Deviation | `NullSafetyTest`                                           | Document as a library extension, not a Go predefined function.                                          |

### Template Set and API Behavior

| Go behavior                                                         | v0.5.0 status  | Evidence                                                | Decision                                                                       |
|---------------------------------------------------------------------|----------------|---------------------------------------------------------|--------------------------------------------------------------------------------|
| Custom delimiters                                                   | Covered        | `CustomDelimiterTest`, `LexerTrimDelimiterTest`         | Java constructor-based delimiter API remains.                                  |
| `Funcs` before parse                                                | Java Deviation | Constructor-based custom functions                      | Java API uses constructors instead of Go's fluent `Funcs`.                     |
| `Delims` fluent API                                                 | Java Deviation | Constructor-based delimiters                            | Java API uses constructors instead of Go's fluent `Delims`.                    |
| `Option("missingkey=...")`                                          | Deferred       | No API                                                  | Defer beyond v0.5.0.                                                           |
| `ParseFiles`, `ParseGlob`, `ParseFS`                                | Deferred       | `Template.parse(InputStream)`, `Template.parse(Reader)` | Caller-managed IO remains the v0.5.0 Java API.                                 |
| `Lookup`, `DefinedTemplates`, `Templates`, `Name`, associated `New` | Deferred       | `executeTemplate` exists                                | Defer public introspection API decisions beyond v0.5.0.                        |
| Parallel execution safety                                           | Covered        | `TemplateCloningTest`                                   | Parsed templates can be copied for concurrent execution with separate writers. |

### Error Behavior

| Go behavior                                  | v0.5.0 status  | Evidence                                            | Decision                                                               |
|----------------------------------------------|----------------|-----------------------------------------------------|------------------------------------------------------------------------|
| Parse errors include useful location/context | Java Deviation | `ParserErrorContextTest`, `ParserCoverageErrorTest` | Error wording is Java-specific; location/context is covered.           |
| Execution stops on function errors           | Covered        | `GoCompatibilityAuditTest`                          | Runtime function failures are wrapped in `TemplateExecutionException`. |
| Writer errors propagate                      | Covered        | `GoCompatibilityAuditTest`                          | Writer `IOException` is propagated to callers.                         |

## v0.5.0 Completion Gate

- No `Needs Audit` or undecided compatibility rows remain.
- All deferred Go APIs are documented in README and CHANGELOG.
- Java-specific extensions are clearly separated from Go compatibility claims.
- Focused compatibility tests cover high-risk behavior in `GoCompatibilityAuditTest`.
- `./mvnw test` succeeds on Java 8.
- `./mvnw verify "-Dgpg.skip=true"` succeeds before release tagging.

## v0.6.0 Release Plan

Positioning: v0.6.0 is the API usability and migration release after the v0.5.0 compatibility audit. It should not reopen the entire compatibility matrix. It should make the audited behavior easier to understand, configure, and validate in real Java projects.

### Goals

- Move detailed compatibility guidance out of README into focused docs.
- Add a configurable missing-key policy, because missing data is one of the most important production behaviors for template engines.
- Add template introspection APIs so callers can inspect parsed template sets before execution.
- Add a canonical compatibility fixture suite that preserves the v0.5.0 audit results and gives future changes a stable regression target.
- Keep the implementation Java 8-compatible and dependency-light.

### Non-Goals

- Do not implement generic Java method calls with arguments in v0.6.0. The security and invocation rules need a separate design.
- Do not implement Go channel, `iter.Seq`, `iter.Seq2`, or Go `ParseFS` equivalents.
- Do not start broad performance refactors before establishing a baseline.
- Do not add runtime dependencies unless a specific feature cannot be implemented cleanly with the JDK.

### Stage 1: Documentation and Fixture Skeleton

Create user-facing docs:

- `docs/go-template-compatibility.md`
- `docs/migration-from-go-template.md`

The compatibility doc should cover:

- Covered core Go `text/template` behavior.
- Java-specific behavior: getters, public fields, public no-arg methods, `Optional`, enum rendering, null and missing values.
- Unsupported or deferred Go APIs.
- Suggested Java alternatives for unsupported Go APIs.

The migration guide should cover:

- How to map Go structs to JavaBeans, public fields, maps, and enums.
- How null and missing keys behave.
- How map iteration order differs from Go's ordered-key behavior.
- How custom functions map to gotemplate4j `Function`.
- How to avoid relying on Go-only APIs such as `ParseFS`, `Option`, and function-valued fields.

Add fixture test structure:

- Keep Go-compatible fixtures separate from Java-deviation fixtures.
- Prefer small, canonical fixtures with explicit expected output.
- Cover `if`, `range`, `with`, template invocation, variables, missing keys, escaping, formatting, and errors.

### Stage 2: Missing Key Policy

Implement configurable missing-key behavior.

Preferred public API:

```java
Template template = new Template("name");
template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
```

Also consider a Go-compatible string entrypoint if it can be added without ambiguity:

```java
template.option("missingkey=error");
```

Policies:

- `DEFAULT`: current v0.5.0 behavior; missing keys evaluate to null and print as empty output.
- `ZERO`: return a Java zero-like value where the target type is knowable; otherwise fall back to null/empty output.
- `ERROR`: throw `TemplateExecutionException` when a map key or field-chain segment is missing.

Implementation constraints:

- Preserve v0.5.0 default behavior for backward compatibility.
- Make policy part of `Template` state and preserve it in the copy constructor.
- Keep exported API Java 8-compatible.
- Add focused tests for map key access, field access, nested chains, `index`, branch truthiness, and template execution errors.
- Document any Java-specific limits of `ZERO`, because Java often cannot infer the intended value type from a missing map entry.

### Stage 3: Template Introspection API

Add a small, Java-friendly template-set inspection API.

Candidate API:

```java
String name();
boolean hasTemplate(String name);
Set<String> definedTemplates();
List<Template> templates();
Template lookup(String name);
```

Refine the exact return types during implementation. Avoid exposing mutable internal AST structures.

Behavior expectations:

- `name()` returns the root template name.
- `definedTemplates()` returns stable names for parsed templates.
- `hasTemplate(name)` is the safest simple query.
- `lookup(name)` should be documented carefully. If returning a `Template`, it must not allow accidental mutation of the original template set.

Testing requirements:

- Root-only template.
- Multiple `define` blocks.
- Re-parse and override behavior.
- Empty definition handling.
- Copy constructor behavior.

### Stage 4: Optional File Helpers

This stage is optional for v0.6.0. Only include it if Stages 1-3 are complete and stable.

Candidate API:

```java
void parseFile(Path path);
void parseFiles(Path... paths);
void parseGlob(Path directory, String glob);
```

Constraints:

- Use `java.nio.file` only.
- Keep IO caller-visible through `IOException`.
- Do not try to model Go `ParseFS` in Java 8.
- Document file-name to template-name behavior before implementation.

### Stage 5: Lightweight Performance Baseline

This stage is optional for v0.6.0 and should not block release.

Add a lightweight benchmark or repeatable smoke harness for:

- parse throughput
- execute throughput
- JavaBean getter access
- public field access
- public no-arg method access
- range-heavy templates
- function-heavy pipelines

Do not add reflection caching or AST caching until baseline numbers show a concrete need.

### v0.6.0 Completion Gate

- `README.md` and `README_zh.md` link to the detailed compatibility and migration docs instead of carrying all details inline.
- Missing-key policy is implemented or explicitly moved out of v0.6.0 before coding begins.
- Template introspection API is implemented or explicitly moved out of v0.6.0 before coding begins.
- Compatibility fixtures and Java-deviation fixtures are separate.
- `CHANGELOG` describes v0.6.0 as a post-audit API usability release.
- `./mvnw clean verify "-Dgpg.skip=true"` succeeds on Java 8.
- No public API additions are left undocumented in Javadocs.

### Suggested Next Session Order

1. Review this v0.6.0 plan against the current code and adjust API names before implementation.
2. Add docs skeleton and fixture test structure.
3. Implement `MissingKeyPolicy` and tests.
4. Implement template introspection API and tests.
5. Revisit optional file helpers only after the first three stages pass verification.
6. Update README/README_zh/CHANGELOG and run Java 8 verification.

## Later Backlog

These items should stay out of v0.6.0 unless the release scope is explicitly changed.

### Quality and Tooling Backlog

- Improve coverage headroom above the configured JaCoCo thresholds.
- Add tests for error paths that are currently only incidentally covered.
- Consider static analysis tooling such as SpotBugs or PMD after compatibility behavior stabilizes.
- Review deprecated APIs, magic strings, and input validation boundaries.

### Performance Backlog

- Add JMH or a lightweight benchmark harness for parse, execute, field access, function calls, and range-heavy templates.
- Establish baseline numbers before adding reflection caching or AST caching.
- Add performance regression checks only after the benchmark harness is stable enough to avoid noisy failures.

### API Backlog

- Decide whether general Java method invocation with arguments is desirable, given security and compatibility tradeoffs.
- Revisit Java-friendly file helpers if caller-managed IO proves too verbose in real usage.
- Revisit broader Go API parity only after v0.6.0 missing-key policy and introspection APIs are stable.

## Maintenance Rules

- Every behavior change needs focused tests.
- Every known Go difference must be visible in this plan or user-facing documentation.
- Compatibility work takes precedence over performance work when the two conflict.
- New dependencies require a clear compatibility, security, or maintainability justification.
- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
