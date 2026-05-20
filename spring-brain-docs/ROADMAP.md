# Spring Brain — Roadmap

## Phase 1 — MVP CLI

Goal:

```text
Static scanner + graph.json + diagnostics.json + summary.md
```

Features:

- CLI scan command
- Java source discovery
- Spring annotation detection
- Route detection
- Repository/entity detection
- Config property detection
- Architecture graph generation
- Five MVP diagnostic rules
- Markdown summary report

Status target:

```text
Useful as a CLI architecture report tool.
```

## Phase 2 — Interactive Viewer

Goal:

```text
Visual architecture map
```

Features:

- React + TypeScript + Vite frontend
- React Flow graph viewer
- Node details panel
- Diagnostic panel
- Endpoint lifecycle view
- Filters by node type
- Search by class, endpoint, entity, repository
- Open source file path display

Target experience:

```text
Select endpoint → see full request lifecycle.
```

## Phase 3 — Advanced Spring Analysis

Goal:

```text
Understand more of the Spring application.
```

Features:

- Bean dependency graph
- Circular dependency detection
- Scheduled task mapping
- Event publisher/listener mapping
- Async method detection
- External API client detection
- DTO detection
- Mapper detection
- Exception handler detection

## Phase 4 — Security Intelligence

Goal:

```text
Understand endpoint access control.
```

Features:

- Detect `@PreAuthorize`
- Detect `@Secured`
- Detect `@RolesAllowed`
- Parse `SecurityFilterChain`
- Detect public endpoints
- Detect risky public methods
- Generate endpoint security matrix

Example report:

```text
DELETE /api/users/{id}
Security: Public
Risk: High
```

## Phase 5 — AI Context Export

Goal:

```text
Make Spring Boot projects easier for AI coding agents.
```

Generate:

```text
SPRING_BRAIN_CONTEXT.md
CLAUDE.md
AGENTS.md
.cursor/rules/spring-brain.mdc
.github/copilot-instructions.md
```

Content:

- Project architecture
- Module overview
- Endpoint flows
- Entities and repositories
- Diagnostics
- Coding conventions
- Safe modification rules

## Phase 6 — CI/CD Integration

Goal:

```text
Architecture health check in pull requests.
```

Features:

- GitHub Action
- `--fail-on-error`
- PR comment summary
- Architecture diff
- New diagnostics detection
- Baseline file support

Example:

```yaml
- name: Run Spring Brain
  run: spring-brain scan --path . --fail-on-error
```

## Phase 7 — Branch Diff Mode

Goal:

```text
Compare architecture before and after a change.
```

Features:

- Compare `main` vs feature branch
- Added endpoints
- Removed endpoints
- Changed service flow
- New security risks
- New broken links
- New dependencies

Example output:

```text
New endpoint added:
POST /api/payments

Risk:
Endpoint has no detected security rule.
```

## Phase 8 — Auto-Fix Suggestions

Goal:

```text
Move from detection to repair.
```

Features:

- Generate suggested patches
- Add missing service skeleton
- Add missing repository skeleton
- Move repository access out of controller
- Replace field injection with constructor injection
- Generate config property placeholder

Possible future technology:

```text
OpenRewrite recipes
```

## Phase 9 — IDE Integration

Goal:

```text
Bring Spring Brain into developer workflow.
```

Possible plugins:

- IntelliJ IDEA plugin
- VS Code extension

Features:

- View endpoint flow from editor
- Show diagnostics inline
- Jump to graph node
- Export AI context from IDE

## Phase 10 — Enterprise Edition Ideas

Goal:

```text
Team-level architecture intelligence.
```

Features:

- Multi-repository scanning
- Architecture trends over time
- Team dashboard
- Service ownership map
- Dependency risk map
- API change history
- Organization-wide architecture rules

## Recommended Build Order

```text
1. MVP CLI
2. Diagnostics
3. Summary report
4. React viewer
5. AI context export
6. GitHub Action
7. Security analyzer
8. Diff mode
9. Auto-fixes
10. IDE plugin
```

## Portfolio Strategy

For a portfolio project, the most impressive combination is:

```text
1. Static architecture graph
2. Broken link detector
3. Diagnostic report
4. React graph viewer
5. AI context export
6. GitHub Action
```

This shows:

- Java expertise
- Spring Boot architecture knowledge
- Static analysis
- CLI engineering
- Frontend graph visualization
- DevOps thinking
- AI-assisted development workflow
