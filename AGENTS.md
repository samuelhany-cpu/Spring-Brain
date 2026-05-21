# AGENTS.md

Guidance for AI coding agents working in this repository.

## Project Identity

Spring Brain is a Spring Boot architecture intelligence tool. It statically scans a Spring Boot project and produces architecture context for humans and AI agents:

- `.spring-brain/graph.json`
- `.spring-brain/diagnostics.json`
- `.spring-brain/summary.md`

The core product question is:

> What is connected, what is broken, what is risky, and what should an AI coding agent know before changing this Spring Boot project?

Do not position the project as only a graph visualizer. The stronger framing is architecture debugger for Spring Boot.

## Current Architecture

This is a Maven multi-module repository:

- `spring-brain-core/`: scanner, domain models, graph builder, diagnostics, exporters, summary report generation.
- `spring-brain-cli/`: Picocli command line interface, scan command, serve command, viewer server.
- `spring-brain-viewer/`: React + TypeScript + Vite + React Flow interactive viewer.
- `spring-brain-server/`: reserved/future server module.
- `spring-brain-samples/`: clean and broken Spring Boot sample apps used for validation.
- `docs/`: canonical product, architecture, schema, diagnostic, and roadmap documents.
- `spring-brain-docs/`: original planning docs pack; useful background, but prefer `docs/` when both exist.

Before making product or architecture changes, read the relevant files in `docs/`.

## Core Principles

- Static analysis first. The MVP must not start the scanned Spring Boot application.
- Deterministic output. Sort nodes, edges, diagnostics, routes, and report sections consistently.
- Stable schemas. Treat `graph.json` and `diagnostics.json` as versioned contracts.
- Small modules and focused classes. Avoid giant services or mixed scanner/graph/diagnostic responsibilities.
- Independent diagnostic rules. Each rule should be its own class implementing the shared diagnostic contract.
- Clear, actionable diagnostics. Every diagnostic should explain what is wrong, where it is, why it matters, and how to fix it.
- Keep Spring Brain Spring-native. Do not copy code, assets, UI, filenames, or implementation details from Laravel Brain or other projects.

## Technology Stack

Backend and CLI:

- Java 17 target in Maven currently, with project docs and README expecting Java 21+ for users/CI.
- Maven multi-module build.
- JavaParser for source parsing.
- Jackson for JSON export.
- Picocli for CLI commands.
- JUnit 5 and AssertJ for tests.

Viewer:

- React 18.
- TypeScript 5.
- Vite 5.
- React Flow 11.
- dagre for graph layout.
- Vitest and Playwright for tests.

## Important Commands

Run all Maven tests:

```bash
mvn test
```

Build the project:

```bash
mvn package
```

Run the CLI after packaging:

```bash
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar --help
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar scan --path ./spring-brain-samples/clean-crud-app
```

Run viewer tests from `spring-brain-viewer/`:

```bash
npm test
npm run test:e2e
```

Build the viewer from `spring-brain-viewer/`:

```bash
npm run build
```

## CLI Behavior

Supported commands include:

- `scan --path <dir>`
- `scan --path <dir> --output <dir>`
- `scan --path <dir> --fail-on-error`
- `scan --path <dir> --serve`
- `serve --path <dir>`
- `serve --path <dir> --port <n>`

Exit codes:

- `0`: success.
- `1`: tool error, such as invalid path.
- `2`: scan succeeded but `ERROR` diagnostics exist and `--fail-on-error` was enabled.

## Graph Contract

The graph output lives at `.spring-brain/graph.json`.

Supported MVP node types:

- `route`
- `controller`
- `service`
- `repository`
- `entity`
- `config_property`

Supported MVP edge types:

- `maps_to`
- `calls`
- `injects`
- `manages`
- `uses_config`

Determinism rules:

- Sort nodes by `type`, then `id`.
- Sort edges by `type`, then `from`, then `to`.
- Use relative paths in output, not machine-specific absolute paths.
- Do not include unstable object identity, memory references, or nondeterministic ordering.
- Every edge endpoint must reference an existing node ID.

## Diagnostic Contract

The diagnostic output lives at `.spring-brain/diagnostics.json`.

MVP diagnostic rules:

- `SPRING_BRAIN_CONTROLLER_WITHOUT_SERVICE`
- `SPRING_BRAIN_CONTROLLER_DIRECT_REPOSITORY_ACCESS`
- `SPRING_BRAIN_MISSING_REPOSITORY_BEAN`
- `SPRING_BRAIN_REPOSITORY_ENTITY_MISMATCH`
- `SPRING_BRAIN_MISSING_CONFIG_PROPERTY`

Severity levels:

- `ERROR`: likely broken application behavior.
- `WARNING`: architecture smell or likely issue.
- `INFO`: low-risk observation.

Sort diagnostics by:

1. Severity: `ERROR`, `WARNING`, `INFO`.
2. File path.
3. Line number.
4. Code.
5. Message.

Each diagnostic must include actionable suggested fixes. Avoid vague messages such as "Bad architecture" or "Fix this."

## Development Rules

- Follow existing package boundaries under `com.springbrain.core` and `com.springbrain.cli`.
- Keep `spring-brain-core` independent from CLI-specific behavior.
- Use JavaParser AST APIs for source analysis instead of brittle string parsing where practical.
- Prefer immutable or final domain models consistent with the current code style.
- Preserve deterministic behavior in tests and production code.
- Add or update tests with behavioral changes.
- For scanner changes, cover annotation detection, route extraction, repository generic extraction, config property extraction, and line/path behavior as relevant.
- For diagnostic rules, add positive and negative tests; include edge cases when confidence can vary.
- For output changes, use stable assertions or golden-file style tests where appropriate.
- Do not introduce runtime Spring application startup into the scanner.
- Do not add broad future roadmap features unless the user explicitly asks for that milestone.

## Viewer Rules

- The viewer should load and inspect `graph.json` and `diagnostics.json`.
- Keep the interface work-focused: graph canvas, filters, search, details, diagnostics, endpoint lifecycle.
- Avoid decorative landing pages. The useful viewer experience should be the first screen.
- Use React Flow for graph rendering and dagre for layout.
- Validate visual changes with Vitest and Playwright where behavior or layout risk is meaningful.

## Sample Apps

Use `spring-brain-samples/` to validate behavior:

- `clean-crud-app`: should scan cleanly.
- `broken-controller-without-service-app`: should trigger controller-without-service diagnostics.
- `broken-controller-direct-repository-app`: should trigger direct repository access diagnostics.
- `broken-missing-repository-app`: should trigger missing repository bean diagnostics.
- `broken-repository-entity-mismatch-app`: should trigger repository/entity mismatch diagnostics.
- `broken-config-property-app`: should trigger missing config property diagnostics.

When adding a diagnostic or scanner feature, prefer a small fixture or sample app that makes the expected behavior obvious.

## Documentation Rules

Canonical docs:

- `docs/PRD.md`
- `docs/ARCHITECTURE.md`
- `docs/MVP_PLAN.md`
- `docs/GRAPH_SCHEMA.md`
- `docs/DIAGNOSTIC_RULES.md`
- `docs/ROADMAP.md`

Keep docs, README, tests, and behavior aligned. If you change schema, diagnostics, CLI behavior, commands, or roadmap status, update the corresponding docs.

## Non-Goals Unless Explicitly Requested

Do not implement these without a clear user request or milestone direction:

- Runtime Actuator scanning.
- Security analyzer.
- GitHub Action integration.
- OpenRewrite auto-fixes.
- IDE plugins.
- Mermaid export.
- Branch diff mode.
- AI chat interface.
- Enterprise dashboards.

## Quality Bar

- Tests should pass before claiming completion.
- Generated files should be readable and deterministic.
- Error messages should tell users what failed and what to do next.
- Agent-facing output should preserve the architecture story: route to controller to service to repository to entity, plus diagnostics and suggested fixes.
- Keep changes small, reviewable, and milestone-aware.
