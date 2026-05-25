# gotemplate4j Development Plan

**Last Updated**: 2026-05-25  
**Current Version**: 0.10.0 (Released)  
**Next Version**: TBD  
**Current Focus**: maintenance, documentation, and backlog grooming

---

## Plan Documentation and Rules

### Project Direction

gotemplate4j should remain a small, Java-compatible implementation of Go's `text/template` semantics for Java applications.

**Important Notice**: Since v0.10.0, gotemplate4j requires **Java 11 or higher**. This decision aligns with industry standards and enables the use of modern Java features for cleaner, more maintainable code. Java 8 support ended with v0.9.x.

### Document Maintenance Rules

- Keep this plan short enough to guide work; remove completed tasks instead of accumulating release history.
- Items in "Future Backlog" should be reviewed periodically and either promoted to active development or removed.
- Completed version releases should be moved to "Current Progress" section, not kept in detailed planning sections.

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
