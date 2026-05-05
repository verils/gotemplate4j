# Go text/template Compatibility Matrix

**Last Updated**: 2026-05-05  
**Scope**: Phase 2 compatibility audit for gotemplate4j v0.5.0  
**Reference**: Go `text/template` package documentation: https://pkg.go.dev/text/template

This matrix tracks semantic compatibility with Go's `text/template` package. It is not a code coverage checklist. Each row should be resolved by behavior-oriented tests, documented Java-specific deviations, or explicit deferral.

## Status Legend

- **Compatible**: Implemented and covered by focused tests.
- **Mostly Compatible**: Implemented and covered, but still needs direct Go behavior comparison or known edge-case review.
- **Known Gap**: Go feature is not implemented or behavior is observably different.
- **Java Deviation**: Behavior intentionally differs because of Java type system or public API constraints.
- **Not Applicable**: Go feature has no meaningful Java equivalent.
- **Needs Audit**: Implementation likely exists, but compatibility has not been reviewed closely enough.

## Actions and Control Flow

| Go behavior | gotemplate4j status | Evidence | Next action |
| --- | --- | --- | --- |
| Plain text and action output `{{pipeline}}` | Compatible | `TemplateTest`, `ParserCanonicalTest`, `ExecutorEdgeCaseTest` | Add one canonical Go-output fixture set for final audit. |
| Comments `{{/* ... */}}` | Compatible | `CustomDelimiterTest`, lexer/parser tests | Verify trim interaction around comments. |
| `if`, `else`, `else if` | Mostly Compatible | `ExecutorEdgeCaseTest`, `ParserBranchScopeTest`, `ParserCanonicalTest` | Compare truthiness and `else if` parse tree against Go cases. |
| `range` over arrays and Java collections | Mostly Compatible | `RangeIndexTest`, `ExecutorEdgeCaseTest` | Add canonical empty `range ... else` tests for list, array, map. |
| `range` over maps | Mostly Compatible | `RangeIndexTest` | Verify key/value binding and define map ordering policy. Go sorts basic ordered keys; Java map iteration currently follows the map implementation. |
| `range` over integer, channel, `iter.Seq`, `iter.Seq2` | Known Gap | No parser/executor support | Decide whether to defer as non-v0.5 Java compatibility work. |
| `range` `{{break}}` and `{{continue}}` | Compatible | `BreakContinueTest`, `LexerTextAndActionTest` | Keep nested range and outside-range parse errors as regression coverage. |
| `with`, `else`, `else with` | Mostly Compatible | `ExecutorEdgeCaseTest`, `NestedTemplateContextTest` | Add direct `else with` canonical tests. |
| `template "name"` with nil data | Needs Audit | `ParserTemplateDefinitionTest`, `TemplateTest` | Add execution tests for omitted pipeline behavior. |
| `template "name" pipeline` context passing | Compatible | `NestedTemplateContextTest`, `TemplateInheritanceTest` | Keep as regression suite. |
| `block "name" pipeline` as define + template shorthand | Compatible | `BlockOverrideTest`, `TemplateInheritanceTest`, `ParserCanonicalTest` | Keep as Phase 2 completion evidence. |
| Nested template definitions | Needs Audit | `ParserTemplateDefinitionTest` | Add cases for definitions inside definitions and override order. |

## Pipelines and Variables

| Go behavior | gotemplate4j status | Evidence | Next action |
| --- | --- | --- | --- |
| Function command execution | Compatible | `Functions*Test`, `FormattingFunctionTest` | Verify wrong-arity errors are surfaced consistently. |
| Chained pipeline passes previous value as final argument | Compatible | `PipeNodeVariableTest`, `ParserPipelineCommandTest`, `FormattingFunctionTest` | Keep as regression suite. |
| Parenthesized pipeline arguments | Mostly Compatible | `ParserPipelineCommandTest`, executor support for nested `PipeNode` | Add execution tests with nested functions and template invocation. |
| Variable declaration `$x := pipeline` | Compatible | `PipeNodeVariableTest`, `ParserBranchScopeTest` | Keep as regression suite. |
| Variable assignment `$x = pipeline` | Needs Audit | Parser accepts `ASSIGN`; executor stores final value | Add tests distinguishing declaration from reassignment and scope behavior. |
| `range $v :=` binds element, not index | Compatible | `RangeIndexTest` | Keep as regression suite. |
| `range $k, $v :=` binds index/key and element | Compatible | `RangeIndexTest` | Keep as regression suite. |
| Variable scope ends at matching `end` | Mostly Compatible | `RangeIndexTest`, `ParserBranchScopeTest` | Strengthen execution tests for `if`, `with`, `range`, and root scope. |
| Template invocation does not inherit caller variables | Compatible | `RangeIndexTest` | Add one explicit non-range variable case. |
| `$` is initial execution data | Compatible | `RootVariableCompatibilityTest` | Keep root stability and template invocation root rebinding as regression coverage. |

## Data Access and Type Semantics

| Go behavior | gotemplate4j status | Evidence | Next action |
| --- | --- | --- | --- |
| Dot `.` evaluates to current data | Compatible | Broad test coverage | Add canonical dot-switching fixtures. |
| Struct/public field access | Java Deviation | `PublicFieldTest` | Java supports getters and public fields; document mapping to Go exported fields. |
| Method invocation with no args in chains | Needs Audit | Some enum method tests | Review JavaBean getters vs arbitrary public methods. |
| Method invocation with arguments in pipelines | Known Gap | No general method-call executor path | Decide whether to implement Java method calls or document function-only model. |
| Map key access via `.Key` | Mostly Compatible | `ExecutorEdgeCaseTest`, `OptionalSupportTest` | Add missing-key behavior tests. |
| Missing map key option `missingkey=default/zero/error` | Known Gap | No `Option` API | Decide whether to add `Template.option(...)` equivalent or document default behavior. |
| Nil/null behavior | Mostly Compatible | `NullSafetyTest`, `ExecutorEdgeCaseTest` | Compare printed null/missing values against Go `<no value>` behavior. |
| Java `Optional` unwrapping | Java Deviation | `OptionalSupportTest`, `NullSafetyTest` | Keep documented as Java-specific convenience. |
| Enum rendering and enum methods | Java Deviation | `EnumHandlingTest` | Keep documented as Java-specific behavior. |
| Complex number constants | Mostly Compatible | `ParserNumberTest`, `ComplexTest` | Compare formatting and overflow behavior with Go fixtures. |

## Built-in Functions

| Go behavior | gotemplate4j status | Evidence | Next action |
| --- | --- | --- | --- |
| `and`, `or`, `not` | Mostly Compatible | `FunctionsLogicalTest` | Confirm short-circuit behavior, not just returned value. |
| `eq`, `ne`, `lt`, `le`, `gt`, `ge` | Mostly Compatible | `FunctionsComparisonTest` | Audit Go comparison rules for signed/unsigned/float mixed numeric cases. |
| `len` | Compatible | `FunctionsCollectionTest` | Add map/string Unicode cases if needed. |
| `index` | Mostly Compatible | `FunctionsCollectionTest` | Audit multi-level indexing and missing-key behavior. |
| `slice` | Mostly Compatible | `FunctionsCollectionTest` | Audit one-arg, two-arg, and three-index slice forms. |
| `call` | Known Gap | `FunctionsFormattingAndCallTest` | Current implementation calls `Function`; Go supports function-valued fields/map entries with error returns. |
| `html`, `js`, `urlquery` | Mostly Compatible | `FunctionsEscapingTest` | Compare exact escaping output with Go fixtures. |
| `print`, `printf`, `println` | Mostly Compatible | `FunctionsFormattingAndCallTest`, broad usage | Compare exact spacing and newline behavior. |
| `deepEqual`, `typeof`, `kindOf` | Java Deviation | `FunctionsIntrospectionTest` | Not Go predefined functions; keep documented as Java/library extensions or consider removing from compatibility claims. |
| `default` | Java Deviation | `NullSafetyTest` | Not Go predefined function; keep documented as extension or consider moving out of Go compatibility checklist. |

## Template Set and API Behavior

| Go behavior | gotemplate4j status | Evidence | Next action |
| --- | --- | --- | --- |
| Parse multiple associated templates | Compatible | `TemplateTest`, `ParserTemplateDefinitionTest`, `TemplateCloningTest` | Keep as regression suite. |
| Empty definitions do not replace existing non-empty definitions | Compatible | `Template.isEmpty`, `TemplateCloningTest` | Add direct parse-order test if not already covered. |
| `Clone` isolates future parses | Compatible | `TemplateCloningTest` | Keep as Phase 2 completion evidence. |
| Custom delimiters apply to subsequent parse calls | Compatible | `CustomDelimiterTest`, `LexerTrimDelimiterTest` | Add parse-after-parse delimiter inheritance case if needed. |
| `Funcs` before parse | Java Deviation | Constructor-based custom functions | Document Java API difference from Go's fluent `Funcs`. |
| `Delims` fluent API | Java Deviation | Constructor-based delimiters | Constructor support exists; Go-style fluent API not implemented. |
| `Option("missingkey=...")` | Known Gap | No API | Decide whether v0.5 requires this for compatibility. |
| `ParseFiles`, `ParseGlob`, `ParseFS` | Known Gap | Reader/InputStream parsing exists | Decide whether file/glob helpers are in scope. |
| `Lookup`, `DefinedTemplates`, `Templates`, `Name`, associated `New` | Known Gap | `executeTemplate` exists; no introspection API | Decide whether public API parity is in scope for v0.5 or later. |
| Parallel execution safety | Mostly Compatible | `TemplateCloningTest` | Add direct parallel execution on same parsed template with separate writers. |

## Error Behavior

| Go behavior | gotemplate4j status | Evidence | Next action |
| --- | --- | --- | --- |
| Parse errors include useful location/context | Java Deviation | `ParserErrorContextTest`, `ParserCoverageErrorTest` | Keep enhanced Java diagnostics; compare parse failure categories, not exact strings. |
| Execution stops on function errors | Needs Audit | Function tests cover wrong arity | Add tests for partial output and wrapped `TemplateExecutionException`. |
| Missing template execution errors | Compatible | `ExecutorEdgeCaseTest`, `TemplateCloningTest` | Keep as regression suite. |
| Writer errors propagate | Needs Audit | No focused tests found | Add failing `Writer` test. |

## Phase 2 Completion Gate

Phase 2 should only be marked **Completed** after:

- [ ] All `Known Gap` rows are either implemented, explicitly deferred, or documented as Java-specific deviations.
- [ ] All `Needs Audit` rows have focused tests or a documented decision.
- [ ] Java-specific extensions are separated from Go compatibility claims.
- [ ] A small canonical fixture set compares high-risk behavior against Go `text/template` output.
