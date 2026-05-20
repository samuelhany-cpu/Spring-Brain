# Spring Brain — Architecture Document

## 1. Architecture Overview

Spring Brain is a static analysis and architecture reporting tool for Spring Boot applications.

```text
CLI
 ↓
Scanner (Milestone 1)
 ↓
ProjectModel
 ↓
GraphBuilder (Milestone 2)
 ↓
DiagnosticEngine (Milestone 3)
 ↓
Exporters
 ↓
.spring-brain/graph.json
.spring-brain/diagnostics.json
.spring-brain/summary.md
```

## 2. Design Principles

- **Static First** — No running application required.
- **Deterministic Output** — Same input always produces same output.
- **Rule-Based Diagnostics** — Each diagnostic is an independent rule class.
- **Stable Graph Schema** — Versioned from day one.
- **Small Modules** — Avoid mixed responsibilities.

## 3. Repository Structure

```text
spring-brain/
├── spring-brain-core/       # Domain logic: scanner, model, graph, diagnostics, export
├── spring-brain-cli/        # Picocli CLI: command parsing, output
├── spring-brain-server/     # PLACEHOLDER: future local HTTP API
├── spring-brain-viewer/     # PLACEHOLDER: future React UI
├── spring-brain-samples/    # Sample Spring Boot apps for testing
│   └── clean-crud-app/
├── docs/                    # Architecture docs
├── pom.xml                  # Parent POM
└── README.md
```

## 4. Module Responsibilities

### spring-brain-core

- Java source file discovery
- JavaParser-based AST parsing
- Spring annotation detection
- Domain model construction (`ProjectModel`, etc.)
- Graph generation (`GraphDocument`)
- Diagnostic rule execution
- JSON and Markdown export

Must not depend on CLI-specific code.

### spring-brain-cli

- Parse CLI arguments via Picocli
- Validate input path
- Trigger scan via core
- Print terminal summary
- Exit with appropriate status code

### spring-brain-server *(future)*

- Serve graph data over HTTP
- Support React viewer

### spring-brain-viewer *(future)*

- Interactive graph viewer (React + React Flow)
- Diagnostic panel

## 5. Core Pipeline

```text
1. CLI parses --path and --output
2. FileDiscovery finds .java files under src/main/java
3. JavaParser parses each file into a CompilationUnit
4. SpringAnnotationScanner builds ProjectModel
5. GraphBuilder converts ProjectModel to GraphDocument
6. DiagnosticEngine runs all rules
7. Exporters write graph.json, diagnostics.json, summary.md
```

## 6. Domain Models

```java
ProjectModel          // root: contains all collected models
ControllerModel       // @RestController / @Controller class
RouteModel            // HTTP route (method + path)
ServiceModel          // @Service class
RepositoryModel       // JpaRepository interface
EntityModel           // @Entity class
ConfigPropertyUsageModel  // @Value("${...}") usage
```

## 7. Graph Schema

```java
GraphDocument         // top-level: schemaVersion, metadata, nodes, edges
GraphNode             // id, type, label, qualifiedName, file, line, metadata
GraphEdge             // id, from, to, type, metadata
```

Node types: `route`, `controller`, `service`, `repository`, `entity`, `config_property`

Edge types: `maps_to`, `calls`, `injects`, `manages`, `uses_config`

## 8. Diagnostic Rule Architecture

```java
public interface DiagnosticRule {
    String code();
    List<Diagnostic> analyze(ProjectModel model, GraphDocument graph);
}
```

MVP rules:
- `ControllerWithoutServiceRule` — WARNING
- `ControllerDirectRepositoryRule` — WARNING
- `MissingRepositoryBeanRule` — ERROR
- `RepositoryEntityMismatchRule` — ERROR
- `MissingConfigPropertyRule` — ERROR

## 9. CLI Exit Codes

| Code | Meaning |
|------|---------|
| 0    | Scan completed, no errors |
| 1    | Tool failure (bad path, parse error) |
| 2    | Scan succeeded but ERROR diagnostics found with `--fail-on-error` |

## 10. Architecture Decision Records

- **ADR-001** Static analysis only in MVP (no runtime needed)
- **ADR-002** JavaParser as initial parser (mature, well-tested)
- **ADR-003** JSON graph as stable interchange format
- **ADR-004** Diagnostics as independent rule classes
- **ADR-005** CLI-first MVP (no UI until graph is stable)

## 11. Risk Areas

- **Method resolution** — Static call tracing uses heuristics in MVP; JavaSymbolSolver added later
- **Lombok** — `@RequiredArgsConstructor` etc. hide constructors; handled heuristically
- **Multiple beans** — Multiple implementations of an interface reported as warnings
- **Config profiles** — All known `application*.properties/yml` files scanned conservatively
