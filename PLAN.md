# gotemplate4j Development Plan

**Last Updated**: 2026-05-23  
**Current Version**: 0.9.1 (Released)  
**Next Version**: 0.10.0 (Java 11 upgrade planned)  
**Current Focus**: v0.10.0 - Java 11 migration, modernization, and compatibility polish

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

## Version Plans

### 📋 Upcoming: v0.10.0 Release Plan

**Positioning**: v0.10.0 is the Java 11 baseline release. It should keep API compatibility where practical while using the runtime upgrade to clean up Java 8-era code and finish a focused set of Go `text/template` compatibility improvements.

**Important Scope Note**: gotemplate4j implements Go's `text/template` package only, NOT `html/template`. Features like contextual auto-escaping, XSS prevention, and HTML-specific security mechanisms are intentionally out of scope. Users needing HTML safety should use the existing `{{html .value}}` function or pre-process data in Java code.

#### Goals

- Migrate to Java 11 minimum requirement
- Modernize codebase with low-risk Java 11 APIs and remove Java 8 compatibility workarounds
- Preserve public API compatibility unless a breaking change is required by the Java 11 baseline
- Improve Go `text/template` compatibility in small, well-tested areas
- Improve developer diagnostics for parse and execution failures
- Keep documentation synchronized across README.md and README_zh.md

#### Non-Goals

- Do not break API backward compatibility
- Do not add runtime dependencies beyond vanilla Java
- Do not implement method invocation with arguments (requires extensive security design)
- Do not start template pre-compilation (deferred to v1.0+)
- Do not migrate to Java 17+ in this release
- Do not perform a broad parser, executor, or AST rewrite unless required for a scoped compatibility fix

#### v0.10.0 Work Scope

**Required: Java 11 Baseline**
- Update Maven compiler settings and build plugins for Java 11
- Update local development, CI, and release documentation to use Java 11+
- Document that v0.9.x is the final Java 8-compatible release line
- Run full verification with `./mvnw clean verify "-Dgpg.skip=true"`

**Required: Documentation and Release Hygiene**
- Update README.md and README_zh.md together for Java 11 requirements
- Keep version-specific release notes out of README files
- Document any known Go `text/template` differences that remain after v0.10.0
- Keep production/experimental warnings consistent across translated docs

**Recommended: Compatibility Polish**
- Audit and complete common built-in function behavior where gaps are small and testable
- Add focused pipeline compatibility tests for function chaining, variable assignment, and parenthesized pipelines
- Improve parse/execution error context, especially missing function and invalid field/key diagnostics
- Clarify or tighten complex number support; if not completed, document the remaining limitation

**Recommended: Java 11 Modernization**
- Prefer targeted use of Java 11 APIs such as `String.isBlank()`, `strip()`, and immutable collection factories where they reduce code complexity
- Avoid broad stylistic rewrites such as converting large areas to `var`
- Avoid introducing Java 17-only language features such as records or pattern matching
- Preserve readable, explicit code in parser and executor hot paths

**Optional: Template Definition Compatibility**
- Review `define`, `template`, and `block` behavior against Go `text/template`
- Fix only well-bounded discrepancies with focused tests
- Defer larger template inheritance or composition redesign to a later version

---

### Next Version: v0.10.0 - Java 11 Migration and Compatibility Polish

#### Overview

Starting from version 0.10.0, gotemplate4j will require **Java 11 or higher**. This strategic decision enables:
- Access to modern Java 11 language and library features
- Better runtime behavior with modern JVM improvements
- Alignment with industry standards (Java 8 reached end of public updates in 2019)
- Simplified codebase maintenance

#### Migration Timeline

```
v0.8.0 (Released) -> Java 8, Quality improvements + Performance optimization
v0.9.1 (Current)  -> Java 8, bug fixes + Compatibility testing
v0.10.0 (Next)    -> Java 11+, Modernization + Compatibility polish
```

#### What Changes in v0.10.0?

**Breaking Changes:**
- Minimum Java version: 8 -> 11
- No API changes planned (backward compatible at API level)
- Runtime requirement only

**Code Improvements:**
- Use Java 11 APIs where they simplify existing code
- Leverage `List.of()`, `Map.of()` for small immutable collections where mutation is not required
- Use modern String methods such as `isBlank()`, `strip()`, and `repeat()`
- Remove Java 8 compatibility workarounds
- Avoid broad style-only rewrites that increase review risk

**Performance Benefits:**
- G1 Garbage Collector (default in Java 11)
- Improved JIT compilation
- Better memory management
- Potential runtime improvements depending on workload and JVM configuration

**Compatibility Improvements:**
- Focused built-in function behavior fixes
- Pipeline execution compatibility tests and small fixes
- Better parser and executor diagnostics
- Clear documentation for any remaining Go `text/template` differences

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
- Most users: Zero code changes needed
- Library consumers: Just update Java runtime
- Android developers: May need to stay on v0.9.x (last Java 8 version)
- Legacy systems: Plan Java upgrade alongside library upgrade

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

This section should stay short. Keep only work that is plausibly actionable; remove release-history checklists and stale version-specific tasks after the release is complete.

### Near-Term Candidates

#### Error Diagnostics Polish
**Priority**: Medium  
**Status**: Partially complete

Remaining useful work:
- Add compact argument type summaries for function execution failures.
- Keep parse and execution errors actionable without dumping large object values.
- Continue improving suggestions only where the fix is obvious and low-risk.

#### Documentation Cleanup
**Priority**: Medium  
**Status**: Ongoing

Remaining useful work:
- Keep README.md and README_zh.md synchronized.
- Keep Java version requirements consistent across docs.
- Remove version-specific feature tours from stable getting-started docs; keep release details in CHANGELOG.
- Document any known Go `text/template` differences in compatibility docs.

#### v0.10.0 Release Verification
**Priority**: Medium  
**Status**: Not started

Run before release:
- `./mvnw clean verify "-Dgpg.skip=true"` with JDK 11.
- Review public API compatibility.
- Review README, README_zh, CHANGELOG, and compatibility docs for Java 11 wording.

### Deferred

- **Test suite reorganization**: useful but high-churn; defer until the package layout creates real maintenance pain.
- **Method invocation with arguments**: requires security design; continue using explicit `Function` registration.
- **Template pre-compilation**: defer until real performance data justifies the complexity.
- **Enhanced template inheritance syntax**: avoid non-Go syntax unless user feedback strongly supports it.
- **Mutation/property-based/static-analysis tooling**: defer unless quality risks appear that current tests do not catch.

#### Documentation Site Deployment
**Priority**: Low
**Status**: Not started

Deploy `docs/` as a static site with bilingual (en/zh) support. Key decisions to make when planning:
- Choose static site generator (VitePress recommended for i18n support).
- Handle repo-root cross-references (`CHANGELOG`, `CONTRIBUTING.md`, `src/test/...`) — currently relative paths for local browsing; will need a pre-build script to rewrite to full GitHub URLs during deployment.
- Set up GitHub Actions to build and publish to GitHub Pages.
