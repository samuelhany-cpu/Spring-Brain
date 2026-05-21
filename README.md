# Spring Brain

> Architecture intelligence for Spring Boot.

Spring Brain is an open-source CLI tool that statically scans a Spring Boot codebase and generates an architecture report showing `Route → Controller → Service → Repository → Entity` flows, broken links, and architecture diagnostics.

---

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+

### Build

```bash
git clone https://github.com/your-org/spring-brain.git
cd spring-brain
mvn package -DskipTests
```

### Run the CLI

```bash
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar --help
```

### Scan a Spring Boot project

```bash
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar scan --path ./spring-brain-samples/clean-crud-app
```

### Run Tests

```bash
mvn test
```

---

## Usage

### Basic scan

Point `--path` at any Spring Boot project root:

```bash
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar scan --path /path/to/your-spring-app
```

Example output printed to the terminal:

```text
Spring Brain — Static Analysis
================================
Project path : /path/to/your-spring-app
Output path  : /path/to/your-spring-app/.spring-brain

Scanning sources...
Building graph...
Running diagnostics...

Results
-------
  Controllers : 5
  Services    : 4
  Repositories: 4
  Entities    : 6
  Routes      : 18
  Errors      : 0
  Warnings    : 2

Output written to: /path/to/your-spring-app/.spring-brain
```

### Custom output directory

```bash
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar scan \
  --path /path/to/your-spring-app \
  --output /tmp/my-report
```

### Fail the build on errors (CI usage)

```bash
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar scan \
  --path /path/to/your-spring-app \
  --fail-on-error
```

Exits with code `2` if any `ERROR` diagnostics are found. Useful in CI pipelines to enforce architecture rules.

---

## Output

After a successful scan, Spring Brain writes to `.spring-brain/` (or `--output <dir>`):

```text
.spring-brain/
├── graph.json        ← Architecture graph (nodes + edges)
├── diagnostics.json  ← Broken links and rule violations
└── summary.md        ← Human-readable report
```

---

## Project Structure

```text
spring-brain/
├── spring-brain-core/       # Domain logic, scanner, graph, diagnostics
├── spring-brain-cli/        # Picocli CLI entry point
├── spring-brain-server/     # Planned: local HTTP API
├── spring-brain-viewer/     # Planned: React graph viewer
├── spring-brain-samples/
│   ├── clean-crud-app/                          # Clean reference app (0 diagnostics)
│   ├── broken-controller-without-service-app/   # Triggers CONTROLLER_WITHOUT_SERVICE
│   ├── broken-controller-direct-repository-app/ # Triggers CONTROLLER_DIRECT_REPOSITORY
│   ├── broken-missing-repository-app/           # Triggers MISSING_REPOSITORY_BEAN
│   ├── broken-repository-entity-mismatch-app/   # Triggers REPOSITORY_ENTITY_MISMATCH
│   └── broken-config-property-app/              # Triggers MISSING_CONFIG_PROPERTY
├── docs/
│   ├── PRD.md
│   ├── ARCHITECTURE.md
│   ├── MVP_PLAN.md
│   ├── GRAPH_SCHEMA.md
│   ├── DIAGNOSTIC_RULES.md
│   └── ROADMAP.md
└── pom.xml
```

---

## CLI Commands

| Command | Description |
|---------|-------------|
| `--help` | Show help |
| `scan --path <dir>` | Scan a Spring Boot project |
| `scan --path <dir> --output <dir>` | Custom output directory |
| `scan --path <dir> --fail-on-error` | Exit 2 on ERROR diagnostics |

---

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | Tool error (invalid path, etc.) |
| 2 | Scan succeeded but ERROR diagnostics found (with `--fail-on-error`) |

---

## Documentation

| Document | Description |
|----------|-------------|
| [docs/PRD.md](docs/PRD.md) | Product requirements |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Architecture and module design |
| [docs/MVP_PLAN.md](docs/MVP_PLAN.md) | Milestone plan |
| [docs/GRAPH_SCHEMA.md](docs/GRAPH_SCHEMA.md) | Graph JSON schema |
| [docs/DIAGNOSTIC_RULES.md](docs/DIAGNOSTIC_RULES.md) | Diagnostic rule definitions |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Future roadmap |

---

## Tech Stack

- Java 21
- Spring Boot 3.x (BOM only; CLI has no Spring context)
- Maven multi-module
- JavaParser 3.x
- Picocli 4.x
- Jackson 2.x
- JUnit 5 + AssertJ

---

## Status

**v0.1.0 — Release Candidate** ✅

| Milestone | Status |
|-----------|--------|
| 0 — Project Bootstrap | ✅ |
| 1 — Static Scanner | ✅ |
| 2 — Graph Builder | ✅ |
| 3 — Broken Link Detector | ✅ |
| 4 — Summary Report | ✅ |
| 5 — Release Candidate | ✅ |

124 tests pass. Produces `graph.json`, `diagnostics.json`, and `summary.md` from any Spring Boot source tree.

---

## License

MIT
