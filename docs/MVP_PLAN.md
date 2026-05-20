# Spring Brain — MVP Plan

## 1. MVP Objective

Build a working CLI tool that scans a Spring Boot project and outputs:

```text
.spring-brain/
├── graph.json
├── diagnostics.json
└── summary.md
```

## 2. MVP Non-Goals

- React UI
- Runtime Actuator scanning
- Security analysis
- OpenRewrite auto-fixes
- GitHub Action
- IDE plugin

## 3. Milestones

### Milestone 0 — Project Bootstrap ✅

**Goal:** Create the project skeleton.

**Deliverables:**
- Maven multi-module project
- `spring-brain-core` module
- `spring-brain-cli` module (Picocli, placeholder scan command)
- `spring-brain-server` placeholder
- `spring-brain-viewer` placeholder
- `spring-brain-samples/clean-crud-app`
- Unit test setup
- GitHub Actions CI workflow
- `README.md` and docs

**Acceptance Criteria:**
- `mvn test` passes
- `spring-brain --help` shows CLI help
- `spring-brain scan --path <dir>` runs without crashing

---

### Milestone 1 — Static Scanner

**Goal:** Detect Spring Boot structure from source code.

**Deliverables:**
- Java file discovery (`src/main/java`)
- JavaParser integration
- Controller, route, service, repository, entity, config property detection
- Populated `ProjectModel`

**Tests:** Annotation detection, route extraction, repository generic extraction, `@Value` extraction.

---

### Milestone 2 — Graph Builder

**Goal:** Build architecture graph from `ProjectModel`.

**Deliverables:**
- `GraphDocument`, `GraphNode`, `GraphEdge`
- JSON exporter
- Deterministic node/edge IDs
- `graph.json` output

---

### Milestone 3 — Broken Link Detector

**Goal:** Detect architecture problems.

**Rules:**
1. `ControllerWithoutServiceRule` — WARNING
2. `ControllerDirectRepositoryRule` — WARNING
3. `MissingRepositoryBeanRule` — ERROR
4. `RepositoryEntityMismatchRule` — ERROR
5. `MissingConfigPropertyRule` — ERROR

**Output:** `diagnostics.json`

---

### Milestone 4 — Summary Report

**Goal:** Generate human-readable `summary.md`.

**Sections:** Scan overview, counts, endpoints, graph summary, diagnostics, suggested actions.

---

### Milestone 5 — Release Candidate

**Goal:** Package and document the MVP for public use.

## 4. Sample Apps Required

```text
spring-brain-samples/
├── clean-crud-app/                         ← implemented in Milestone 0
├── broken-controller-without-service-app/  ← Milestone 3
├── broken-controller-direct-repository-app/
├── broken-missing-repository-app/
├── broken-repository-entity-mismatch-app/
└── broken-config-property-app/
```

## 5. Testing Strategy

- **Unit tests** — Individual scanner functions and rules
- **Fixture tests** — Small Java files in `src/test/resources`
- **Sample app tests** — Full scans of `spring-brain-samples/`
- **Golden file tests** — Compare generated JSON with expected output

## 6. Development Order

```text
1. Docs + Bootstrap (Milestone 0) ✅
2. Java source discovery
3. Annotation scanner
4. Route scanner
5. Repository / entity scanner
6. Config scanner
7. Graph builder
8. Diagnostics engine
9. Exporters
10. README and release
```
