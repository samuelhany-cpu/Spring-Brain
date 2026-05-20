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
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0-SNAPSHOT.jar --help
```

### Scan a Spring Boot project

```bash
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0-SNAPSHOT.jar scan --path ./spring-brain-samples/clean-crud-app
```

### Run Tests

```bash
mvn test
```

---

## Output (Milestone 1+)

After a successful scan, Spring Brain writes to `.spring-brain/`:

```text
.spring-brain/
├── graph.json        ← Architecture graph (nodes + edges)
├── diagnostics.json  ← Broken links and rule violations
└── summary.md        ← Human-readable report
```

> In Milestone 0, the scan command validates the path and prints a placeholder message. Full scanner is Milestone 1.

---

## Project Structure

```text
spring-brain/
├── spring-brain-core/       # Domain logic, scanner, graph, diagnostics
├── spring-brain-cli/        # Picocli CLI entry point
├── spring-brain-server/     # Planned: local HTTP API
├── spring-brain-viewer/     # Planned: React graph viewer
├── spring-brain-samples/
│   └── clean-crud-app/      # Sample CRUD app for scanning
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

**Milestone 0 — Project Bootstrap** ✅

Project compiles, tests pass, CLI skeleton working. Scanner begins in Milestone 1.

---

## License

MIT
