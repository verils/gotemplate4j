# Bug Records

**Last Updated**: 2026-05-22

## Critical Bugs Discovered (2026-05-21)

During development of `ComprehensiveTemplateTest`, the following engine-level bugs were discovered. These are confirmed to exist in v0.9.0 and should be addressed in v0.10.0.

### BUG-1: Lexer `{{-` at position 0 causes StringIndexOutOfBoundsException 🔴 CRITICAL
**Priority**: Highest
**Status**: Fixed and covered by lexer trim marker tests
**First Affected Version**: v0.3.2 (introduced in `c2e6385`, "Fix Lexer issues - Fix trim marker processing")
**Recommended Fix Version**: v0.3.3 (crash-level regression, should have been fixed in the next patch)

**Description**: When a left-trim marker `{{-` appears at template position 0 (or at the start of any text segment where `start == 0`), the backward whitespace scan in `Lexer.parseText()` at line 144 accesses `input.charAt(-1)`, causing `StringIndexOutOfBoundsException`.

**Root Cause**: `Lexer.java:143` — the for-loop condition `eotPos >= start` does not guard against `eotPos - 1` going negative when `start == 0` and the preceding content is all whitespace.

**Reproduction**:
```java
Template t = new Template("test");
t.parse("{{- \"hello\" -}}"); // BOOM: StringIndexOutOfBoundsException
```

**Expected Behavior**: `{{-` at position 0 should trim nothing (no preceding whitespace exists) and parse normally.

**Fix**: Change the loop condition from `eotPos >= start` to `eotPos > start`.

**Affected File**: `src/main/java/io/github/verils/gotemplate/internal/Lexer.java:143`

**Resolution**: Verified fixed by the `eotPos > start` guard and covered by `LexerTrimDelimiterTest`.

---

### BUG-2: `index` function does not support `List` / `Collection` types 🔴 CRITICAL
**Priority**: Highest
**Status**: Fixed and covered by collection function tests
**First Affected Version**: v0.4.0 (introduced in `63f5893`, initial `index` function implementation — design omission, not a regression)
**Recommended Fix Version**: v0.4.1 (critical functional gap, should have been fixed in the next patch)

**Description**: The built-in `index` function only supports `Map`, arrays (via `Class.isArray()`), and `String`. Java `List` and `Collection` types (e.g., `ArrayList`, `Arrays$ArrayList`) throw `IllegalArgumentException: index: invalid type`.

**Root Cause**: `Functions.java:220-243` — the `index` function checks for `Map`, then `isArray()`, then `String`, but has no `instanceof Collection` / `instanceof List` branch. `Arrays.asList()` returns `java.util.Arrays$ArrayList` which is not an array class.

**Reproduction**:
```java
Map<String, Object> data = new HashMap<>();
data.put("items", Arrays.asList("a", "b", "c"));
render("{{index .items 1}}", data); // ERROR: invalid type Arrays$ArrayList
```

**Expected Behavior**: `index` should accept any `List` or `Collection` and return `list.get(index)`.

**Fix**: Add `collection instanceof List` and generic `collection instanceof Collection` branches before the fallback error.

**Affected File**: `src/main/java/io/github/verils/gotemplate/Functions.java:220-243`

**Resolution**: `Functions.index()` now supports `List` and other `Collection` implementations. `FunctionsCollectionTest` covers list indexing and out-of-range list indexing.

---

### BUG-3: `=` variable reassignment has no effect 🔴 CRITICAL
**Priority**: Highest
**Status**: Verified working and covered by variable tests
**First Affected Version**: v0.4.0 (introduced in `3360dfb`, initial variable assignment implementation — `=` path never implemented, only `:=`)
**Recommended Fix Version**: v0.4.1 (critical Go compatibility gap, should have been fixed in the next patch)

**Description**: Variable reassignment using `{{$var = newValue}}` is silently ignored. Only `{{$var := value}}` (declaration) works. This is a significant deviation from Go `text/template` where `=` reassignment is standard.

**Reproduction**:
```java
Template t = new Template("test");
t.parse("{{$x := 1}}{{$x = 5}}{{$x}}");
// Expected output: "5"
// Actual output: "1"
```

**Expected Behavior**: `{{$x = 5}}` should update `$x` to 5, and subsequent `{{$x}}` should render "5".

**Note**: The `PipeNodeVariableTest` test suite only tests `:=` (declaration), never `=` (reassignment). The feature is documented as supported but has no test coverage.

**Affected File**: `src/main/java/io/github/verils/gotemplate/internal/Executor.java` (variable handling in `executePipe` / `writeAction`)

**Resolution**: Current parser/executor behavior already supports `=` through the same variable assignment path as `:=`. `PipeNodeVariableTest` now covers `{{$x = 5}}`.

---

### BUG-4: Parenthesized pipeline chaining `(pipeline).field` not supported 🟡 HIGH
**Priority**: High
**Status**: Fixed and covered by comprehensive execution tests
**First Affected Version**: v0.1.0 (introduced in `31ed6bd`, initial parenthesized pipeline parsing — Executor never supported field chaining on parenthesized results)
**Recommended Fix Version**: v0.1.1

**Description**: Cannot chain field access on a parenthesized pipeline result. `{{(index .items 0).name}}` fails with "can't evaluate command (index .items 0).name". This is a minor deviation from Go `text/template` where parenthesized field chaining is valid.

**Reproduction**:
```java
render("{{(index .items 0).name}}", data);
// ERROR: can't evaluate command (index .items 0).name
```

**Expected Behavior**: `{{(index .items 0).name}}` should first evaluate `index .items 0`, then access `.name` on the result.

**Workaround**: Use `{{with index .items 0}}{{.name}}{{end}}`.

**Affected File**: `src/main/java/io/github/verils/gotemplate/internal/Executor.java` (command evaluation)

**Resolution**: `ChainNode` now exposes its base node and fields, and `Executor` evaluates the base expression before applying the field chain. `ComprehensiveTemplateTest` covers `{{(index .items 0).name}}`.

---

### BUG-5: Raw string literals (`\`...\``) incorrectly process escape sequences 🟡 HIGH
**Priority**: High
**Status**: Fixed and covered by comprehensive execution tests
**First Affected Version**: v0.1.0 (raw string parsing added in `bb2dfda`, `StringEscapeUtils.unescape()` applied unconditionally in `printValue()` since `ec1395d` — both pre-v0.1.0)
**Recommended Fix Version**: v0.1.1

**Description**: Backtick-quoted raw string literals should treat backslashes as literal characters (per Go `text/template` spec). However, escape sequences like `\b` are being interpreted (e.g., `\b` becomes ASCII backspace 0x08 instead of literal `\` + `b`).

**Reproduction**:
```java
Template t = new Template("test");
t.parse("{{`raw\\backticks`}}");
// Expected output: raw\backticks
// Actual output: raw<0x08>ackticks (\b interpreted as backspace)
```

**Expected Behavior**: All characters between backticks should be treated literally; no escape processing should occur.

**Affected File**: `src/main/java/io/github/verils/gotemplate/internal/ast/StringNode.java` and `src/main/java/io/github/verils/gotemplate/internal/Executor.java`

**Resolution**: `StringNode` now unescapes only non-raw string literals. `Executor.printValue()` no longer unescapes every runtime `String`. `ComprehensiveTemplateTest` covers raw string output containing `\b`.

---

### BUG-6: `{{break}}` and `{{continue}}` do not work in integer `range` 🟡 HIGH
**Priority**: High
**Status**: Fixed and covered by integer range tests
**First Affected Version**: v0.7.0 (`break`/`continue` added in v0.5.0 via `957c1c0`, integer `range` added in v0.7.0 via `decd282` — break/continue handling never extended to integer range path)
**Recommended Fix Version**: v0.7.1

**Description**: `{{break}}` and `{{continue}}` appeared to be ignored inside `{{range $i := N}}` integer ranges when guarded by comparisons such as `{{if eq $i 2}}`.

**Reproduction**:
```java
Template t = new Template("test");
t.parse("{{range $i := 5}}{{if eq $i 2}}{{break}}{{end}}{{$i}}{{end}}");
// Expected output: "01"
// Previous output: "01234" (condition never matched)
```

**Expected Behavior**: `{{break}}` and `{{continue}}` should work identically in both collection-range and integer-range contexts.

**Root Cause**: Integer range values were Java `Integer` values while numeric template literals evaluate to `Long`; `eq` used `Object.equals`, so `Integer.valueOf(2)` was not equal to `Long.valueOf(2)`.

**Affected File**: `src/main/java/io/github/verils/gotemplate/Functions.java` (numeric equality)

**Resolution**: Numeric equality now compares numeric values across Java number types. `IntegerRangeTest` covers `break` and `continue` in integer ranges.

---

## Pending Analysis

- [x] **Analyze which version introduced each bug and target fix version** (completed 2026-05-22) — See individual bug entries above for per-bug trace results. Summary:

| Bug | Severity | First Affected | Root Cause | Fix Target |
|:----|:---------|:---------------|:-----------|:-----------|
| BUG-1 | 🔴 CRITICAL | v0.3.2 | `eotPos >= start` loop replaced safe `ltrimLength()` in `c2e6385` | Fixed |
| BUG-2 | 🔴 CRITICAL | v0.4.0 | `index()` never had `instanceof List/Collection` branch (design omission) | Fixed |
| BUG-3 | 🔴 CRITICAL | v0.4.0 | Missing coverage for `=` assignment path | Covered |
| BUG-4 | 🟡 HIGH | v0.1.0 | Parenthesized pipeline parsing existed from start, Executor never supported chaining | Fixed |
| BUG-5 | 🟡 HIGH | v0.1.0 | `StringEscapeUtils.unescape()` applied unconditionally to all strings including raw | Fixed |
| BUG-6 | 🟡 HIGH | v0.7.0 | Numeric equality did not compare across Java number wrapper types | Fixed |
