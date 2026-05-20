# Spring Brain — Product Requirements Document

## 1. Product Summary

**Spring Brain** is an open-source developer tool that scans a Spring Boot codebase and generates an architecture intelligence report.

It helps developers understand:

- What endpoints exist
- Which controller handles each endpoint
- Which service layer is involved
- Which repository and entity are connected
- Which application links are broken
- Which architecture rules are violated
- What context should be given to AI coding agents

The tool is inspired by the idea of framework-aware architecture visualization, but this project is designed specifically for Java and Spring Boot.

## 2. Product Vision

Spring Brain should become a developer assistant for Spring Boot applications.

The goal is not only to visualize code structure, but also to answer:

```text
What is connected?
What is broken?
What is risky?
What should an AI coding agent know before changing this project?
```

## 3. Target Users

### Primary Users

- Java backend developers
- Spring Boot developers
- DevOps engineers reviewing backend services
- Technical leads
- Software architects
- Students building portfolio projects
- AI-assisted developers using Claude Code, Codex, Cursor, or Copilot

### Secondary Users

- QA engineers
- Code reviewers
- Engineering managers
- New developers onboarding into existing Spring Boot projects

## 4. Core Problem

Spring Boot applications can become difficult to understand because important flows are spread across:

- Controllers
- Services
- Repositories
- Entities
- Configuration files
- Security classes
- Event listeners
- Scheduled tasks
- External API clients

A developer often has to manually jump between many files to understand a single endpoint.

Example:

```text
POST /api/orders
→ OrderController.createOrder()
→ OrderService.create()
→ OrderRepository.save()
→ Order Entity
→ PaymentClient
→ OrderCreatedEvent
```

Spring Brain should make this flow visible and check if it is broken.

## 5. MVP Goal

The MVP should scan a Spring Boot project without starting the application.

It should generate:

```text
.spring-brain/
├── graph.json
├── diagnostics.json
└── summary.md
```

The MVP must support:

- Static scanning of Java source files
- Detection of Spring stereotypes and mappings
- Architecture graph generation
- Basic broken link detection
- CLI usage
- Markdown summary generation

## 6. MVP Features

### 6.1 Static Scanner

Detect:

- `@RestController`
- `@Controller`
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@PatchMapping`
- `@DeleteMapping`
- `@Service`
- `@Component`
- `@Repository`
- `@Entity`
- `JpaRepository`
- `CrudRepository`
- `@Value("${...}")`

### 6.2 Architecture Graph

Generate graph links:

```text
Route → Controller Method → Service → Repository → Entity
```

### 6.3 Broken Link Detector

MVP diagnostic rules:

1. Controller method does not call any service
2. Controller directly calls repository
3. Service has injected repository but repository class is not found
4. Repository generic type is not annotated with `@Entity`
5. `@Value` property is missing from configuration files

### 6.4 CLI

Required commands:

```bash
spring-brain scan --path .
spring-brain scan --path . --output .spring-brain
```

### 6.5 Output Files

Required output:

```text
graph.json
diagnostics.json
summary.md
```

## 7. Non-MVP Features

These are intentionally excluded from the first MVP:

- Runtime Spring Boot Actuator integration
- React graph viewer
- Security analyzer
- GitHub Action
- OpenRewrite auto-fixes
- IDE plugin
- AI chat interface
- Mermaid export
- Branch diff mode

## 8. Functional Requirements

### FR-001 — Project Scanning

The tool must accept a path to a Spring Boot project.

```bash
spring-brain scan --path ./my-app
```

### FR-002 — Java Source Discovery

The tool must search inside:

```text
src/main/java
```

and find all `.java` files.

### FR-003 — Annotation Detection

The scanner must detect supported Spring annotations on classes, methods, fields, and constructors.

### FR-004 — Route Detection

The scanner must detect controller routes from class-level and method-level mappings.

Example:

```java
@RestController
@RequestMapping("/api/users")
class UserController {
    @GetMapping("/{id}")
    UserResponse getUser() {}
}
```

Expected route:

```text
GET /api/users/{id}
```

### FR-005 — Repository Detection

The scanner must detect repository interfaces extending:

```java
JpaRepository<Entity, ID>
CrudRepository<Entity, ID>
```

### FR-006 — Entity Detection

The scanner must detect classes annotated with:

```java
@Entity
```

### FR-007 — Config Property Usage Detection

The scanner must detect:

```java
@Value("${jwt.secret}")
```

and compare it with:

```text
application.properties
application.yml
```

### FR-008 — Graph Generation

The tool must generate a deterministic `graph.json`.

### FR-009 — Diagnostics Generation

The tool must generate a deterministic `diagnostics.json`.

### FR-010 — Markdown Summary

The tool must generate a human-readable `summary.md`.

## 9. Non-Functional Requirements

### NFR-001 — Deterministic Output

Same input project should produce the same output order and same IDs.

### NFR-002 — No Runtime Requirement

The MVP must not require the scanned Spring Boot app to start.

### NFR-003 — Testability

Scanner and diagnostic rules must be testable without a real running application.

### NFR-004 — Extensibility

New diagnostic rules should be easy to add.

### NFR-005 — Performance

The scanner should handle small and medium projects quickly.

Initial target:

```text
Up to 1,000 Java files
Scan time under 10 seconds where possible
```

### NFR-006 — Clear Errors

Errors should explain what failed and how to fix it.

## 10. Success Criteria

The MVP is successful when:

```bash
spring-brain scan --path ./sample-clean-crud-app
```

produces:

```text
.spring-brain/graph.json
.spring-brain/diagnostics.json
.spring-brain/summary.md
```

and correctly identifies:

- Controllers
- Routes
- Services
- Repositories
- Entities
- Basic broken links

## 11. Example Terminal Output

```text
Spring Brain Scan Complete

Project: sample-clean-crud-app

Routes found: 8
Controllers found: 2
Services found: 3
Repositories found: 3
Entities found: 3

Diagnostics:
ERROR: 0
WARNING: 1
INFO: 2

Output:
.spring-brain/graph.json
.spring-brain/diagnostics.json
.spring-brain/summary.md
```

## 12. Product Positioning

Spring Brain should be described as:

> A Spring Boot architecture intelligence tool that visualizes, diagnoses, and explains backend application structure.

Avoid positioning it as only:

> A graph visualizer.

The stronger positioning is:

> Architecture debugger for Spring Boot.
