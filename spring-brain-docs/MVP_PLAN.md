# Spring Brain — MVP Plan

## 1. MVP Objective

Build a working CLI tool that scans a Spring Boot project and outputs:

```text
.spring-brain/
├── graph.json
├── diagnostics.json
└── summary.md
```

The MVP should answer:

```text
Which endpoints exist?
Which controller handles each endpoint?
Which services, repositories, and entities are linked?
Which common architecture links are broken?
```

## 2. MVP Non-Goals

The MVP will not include:

- React UI
- Runtime Actuator scanning
- Security analysis
- OpenRewrite auto-fixes
- GitHub Action
- IDE plugin
- Mermaid sequence diagrams
- AI chat interface

## 3. MVP Feature List

### Feature 1 — CLI

Command:

```bash
spring-brain scan --path .
```

Optional output path:

```bash
spring-brain scan --path . --output .spring-brain
```

Optional failure mode:

```bash
spring-brain scan --path . --fail-on-error
```

### Feature 2 — Static Java Scanner

Detect Java files inside:

```text
src/main/java
```

Parse with JavaParser.

Extract:

- Package
- Class name
- Interface name
- Methods
- Fields
- Constructors
- Annotations
- Method calls
- Line numbers

### Feature 3 — Spring Component Detection

Detect:

```text
@RestController
@Controller
@Service
@Component
@Repository
@Entity
```

### Feature 4 — Route Detection

Detect:

```text
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@PatchMapping
@DeleteMapping
```

Build full route path from class-level and method-level mappings.

### Feature 5 — Repository Detection

Detect:

```text
JpaRepository<Entity, ID>
CrudRepository<Entity, ID>
```

Extract entity generic type.

### Feature 6 — Config Property Detection

Detect:

```java
@Value("${property.name}")
```

Read keys from:

```text
application.properties
application.yml
application.yaml
application-*.properties
application-*.yml
application-*.yaml
```

### Feature 7 — Graph Builder

Generate links:

```text
Route → Controller Method
Controller → Service
Service → Repository
Repository → Entity
```

### Feature 8 — Diagnostic Rules

Implement MVP rules:

1. Controller method does not call any service
2. Controller directly calls repository
3. Service has injected repository but repository class is not found
4. Repository generic type is not annotated with `@Entity`
5. `@Value` property is missing from configuration files

### Feature 9 — Exporters

Generate:

```text
graph.json
diagnostics.json
summary.md
```

## 4. Milestones

## Milestone 0 — Project Bootstrap

### Goal

Create the project skeleton.

### Deliverables

```text
Maven multi-module project
spring-brain-core module
spring-brain-cli module
sample Spring Boot app
basic Picocli command
unit test setup
GitHub Actions workflow
README.md
```

### Acceptance Criteria

```bash
mvn test
```

passes.

```bash
spring-brain --help
```

shows CLI help.

```bash
spring-brain scan --path spring-brain-samples/clean-crud-app
```

runs without crashing.

## Milestone 1 — Static Scanner

### Goal

Detect Spring Boot structure from source code.

### Deliverables

- Java file discovery
- JavaParser integration
- Controller detection
- Route detection
- Service detection
- Repository detection
- Entity detection
- Config property usage detection

### Acceptance Criteria

Scanner returns a `ProjectModel` containing:

```text
controllers
routes
services
repositories
entities
config property usages
```

Tests must cover:

- Annotation detection
- Route extraction
- Repository generic extraction
- `@Value` property extraction

## Milestone 2 — Graph Builder

### Goal

Build architecture graph from `ProjectModel`.

### Deliverables

- `GraphDocument`
- `GraphNode`
- `GraphEdge`
- JSON exporter
- Deterministic node/edge IDs

### Acceptance Criteria

For a sample CRUD app, the graph contains:

```text
GET /api/users/{id}
→ UserController.getUser
→ UserService.findById
→ UserRepository
→ User
```

Tests must verify:

- Expected nodes exist
- Expected edges exist
- No duplicate nodes
- No duplicate edges
- Stable JSON output

## Milestone 3 — Broken Link Detector

### Goal

Detect MVP architecture problems.

### Deliverables

Rules:

```text
ControllerWithoutServiceRule
ControllerDirectRepositoryRule
MissingRepositoryBeanRule
RepositoryEntityMismatchRule
MissingConfigPropertyRule
```

### Acceptance Criteria

Each rule has:

- Positive test
- Negative test
- Clear diagnostic message
- Suggested fixes

Output includes:

```text
diagnostics.json
summary.md diagnostics section
terminal summary
```

## Milestone 4 — Summary Report

### Goal

Generate readable Markdown summary.

### Deliverables

`summary.md` containing:

- Project name
- Scan date
- Counts
- Endpoints
- Graph summary
- Diagnostics summary
- Suggested next actions

### Acceptance Criteria

The report should be useful without opening JSON files.

## Milestone 5 — Release Candidate

### Goal

Package MVP for local use.

### Deliverables

- Build instructions
- Usage examples
- Sample outputs
- Known limitations
- Version number
- Release notes

### Acceptance Criteria

A developer can clone the repo, build the tool, and scan a sample Spring Boot app.

## 5. Suggested Development Order

```text
1. Create docs
2. Bootstrap Maven modules
3. Implement CLI shell
4. Implement Java source discovery
5. Implement annotation scanner
6. Implement route scanner
7. Implement repository/entity scanner
8. Implement config scanner
9. Implement graph builder
10. Implement diagnostics
11. Implement exporters
12. Polish README and examples
```

## 6. Testing Strategy

## 6.1 Unit Tests

Test individual scanner functions and diagnostic rules.

## 6.2 Fixture Tests

Use small Java files in test resources.

## 6.3 Sample App Tests

Use complete sample apps under:

```text
spring-brain-samples/
```

## 6.4 Golden File Tests

Compare generated JSON with expected JSON files.

## 7. Sample Apps Required

```text
spring-brain-samples/
├── clean-crud-app/
├── broken-controller-without-service-app/
├── broken-controller-direct-repository-app/
├── broken-missing-repository-app/
├── broken-repository-entity-mismatch-app/
└── broken-config-property-app/
```

## 8. MVP Definition of Done

The MVP is complete when:

```bash
spring-brain scan --path ./spring-brain-samples/clean-crud-app
```

generates:

```text
.spring-brain/
├── graph.json
├── diagnostics.json
└── summary.md
```

and broken sample apps correctly trigger expected diagnostics.

## 9. Example Final CLI Output

```text
Spring Brain Scan Complete

Project: clean-crud-app

Controllers: 2
Routes: 8
Services: 3
Repositories: 3
Entities: 3
Config Properties Used: 4

Diagnostics:
ERROR: 0
WARNING: 1
INFO: 2

Files written:
.spring-brain/graph.json
.spring-brain/diagnostics.json
.spring-brain/summary.md
```
