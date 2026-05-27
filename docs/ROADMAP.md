# Spring Brain — Roadmap

## Phase 1 — MVP CLI ✅ Complete

Static scanner + `graph.json` + `diagnostics.json` + `summary.md`

- [x] Milestone 0: Project bootstrap
- [x] Milestone 1: Static scanner (controllers, services, repos, entities, config properties)
- [x] Milestone 2: Graph builder (nodes + edges + metadata)
- [x] Milestone 3: Broken link detector (diagnostic rules engine)
- [x] Milestone 4: Summary report (`summary.md` generator)
- [x] Milestone 5: Release candidate (sample apps, integration tests)

---

## Phase 2 — Interactive Viewer ✅ Complete

React + TypeScript + Vite + React Flow graph viewer:

- [x] Node graph with dagre auto-layout
- [x] Node details panel
- [x] Diagnostic panel (errors + warnings)
- [x] Filter by node type
- [x] Search by class/endpoint
- [x] Animated brain background
- [x] Live WebSocket connection to Spring Brain server
- [x] Click-to-zoom on nodes

---

## Phase 3 — Advanced Spring Analysis ⚠️ Experimental / In Progress

Bean dependency graph and cycle detection are partially implemented.
Results may include false positives for some project structures.

- [x] Bean dependency graph (generic Spring bean nodes + `injects` edges)
- [x] Circular dependency detection (rule implemented, experimental accuracy)
- [ ] Scheduled task mapping (`@Scheduled`)
- [ ] Event publisher/listener mapping (`@EventListener`)
- [ ] Async method detection (`@Async`)
- [ ] External API client detection (RestTemplate, WebClient, Feign)

---

## Phase 4 — Security Intelligence ✅ Complete (basic patterns)

Simple static pattern matching — not a full security audit tool.

- [x] Detect `@PreAuthorize`, `@Secured`, `@RolesAllowed`
- [x] Parse simple chained `SecurityFilterChain` `requestMatchers(...)` rules
- [x] Generate endpoint security matrix
- [x] Flag risky public mutating endpoints

---

## Phase 5 — AI Context Export 📋 Planned

Generate context files for AI coding agents:
- `SPRING_BRAIN_CONTEXT.md`
- `CLAUDE.md` / `AGENTS.md`
- `.cursor/rules/spring-brain.mdc`
- `.github/copilot-instructions.md`

---

## Phase 6 — CI/CD Integration 📋 Planned

- GitHub Action (publish to Marketplace)
- PR comment summary
- Architecture diff between commits

---

## Phase 7 — Branch Diff Mode 📋 Planned

Compare architecture before and after a change:
- Added/removed endpoints
- Changed service flows
- New security risks

---

## Phase 8 — Auto-Fix Suggestions 📋 Planned

Using OpenRewrite recipes:
- Add missing service skeleton
- Move repository access out of controller
- Replace field injection with constructor injection

---

## Phase 9 — IDE Integration 📋 Planned

- IntelliJ IDEA plugin
- VS Code extension
