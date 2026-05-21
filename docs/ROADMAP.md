# Spring Brain — Roadmap

## Phase 1 — MVP CLI *(current)*

Static scanner + `graph.json` + `diagnostics.json` + `summary.md`

- [x] Milestone 0: Project bootstrap
- [ ] Milestone 1: Static scanner
- [ ] Milestone 2: Graph builder
- [ ] Milestone 3: Broken link detector
- [ ] Milestone 4: Summary report
- [ ] Milestone 5: Release candidate

---

## Phase 2 — Interactive Viewer

React + TypeScript + Vite + React Flow graph viewer with:
- Node details panel
- Diagnostic panel
- Endpoint lifecycle view
- Filter by node type
- Search by class/endpoint/entity

---

## Phase 3 — Advanced Spring Analysis

- Bean dependency graph - started
- Circular dependency detection - started
- Scheduled task mapping
- Event publisher/listener mapping
- Async method detection
- External API client detection

---

## Phase 4 — Security Intelligence

- [x] Detect `@PreAuthorize`, `@Secured`, `@RolesAllowed`
- [x] Parse simple chained `SecurityFilterChain` `requestMatchers(...)` rules
- [x] Generate endpoint security matrix
- [x] Flag risky public mutating endpoints

---

## Phase 5 — AI Context Export

Generate context files for AI coding agents:
- `SPRING_BRAIN_CONTEXT.md`
- `CLAUDE.md`
- `AGENTS.md`
- `.cursor/rules/spring-brain.mdc`
- `.github/copilot-instructions.md`

---

## Phase 6 — CI/CD Integration

- GitHub Action
- `--fail-on-error` exit code 2
- PR comment summary
- Architecture diff

---

## Phase 7 — Branch Diff Mode

Compare architecture before and after a change:
- Added/removed endpoints
- Changed service flows
- New security risks

---

## Phase 8 — Auto-Fix Suggestions

Using OpenRewrite recipes:
- Add missing service skeleton
- Move repository access out of controller
- Replace field injection with constructor injection

---

## Phase 9 — IDE Integration

- IntelliJ IDEA plugin
- VS Code extension

---

## Recommended Build Order

```text
1. MVP CLI
2. React viewer
3. AI context export
4. GitHub Action
5. Security analyzer
6. Diff mode
7. Auto-fixes
8. IDE plugin
```
