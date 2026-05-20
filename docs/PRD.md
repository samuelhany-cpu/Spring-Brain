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
- Technical leads and software architects
- Students building portfolio projects
- AI-assisted developers using Claude Code, Codex, Cursor, or Copilot

### Secondary Users

- QA engineers, code reviewers, engineering managers
- New developers onboarding into existing Spring Boot projects

## 4. Core Problem

Spring Boot applications can become difficult to understand because important flows are spread across controllers, services, repositories, entities, configuration files, and more.

Example:

```text
POST /api/orders
→ OrderController.createOrder()
→ OrderService.create()
→ OrderRepository.save()
→ Order Entity
```

Spring Brain makes this flow visible and checks if it is broken.

## 5. MVP Goal

Scan a Spring Boot project without starting the application and generate:

```text
.spring-brain/
├── graph.json
├── diagnostics.json
└── summary.md
```

## 6. MVP Features

### 6.1 Static Scanner

Detect: `@RestController`, `@Controller`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`, `@Service`, `@Component`, `@Repository`, `@Entity`, `JpaRepository`, `CrudRepository`, `@Value("${...}")`

### 6.2 Architecture Graph

```text
Route → Controller Method → Service → Repository → Entity
```

### 6.3 Broken Link Detector

Five MVP diagnostic rules covering controller-without-service, direct repository access, missing beans, entity mismatches, and missing config properties.

### 6.4 CLI

```bash
spring-brain scan --path .
spring-brain scan --path . --output .spring-brain
spring-brain scan --path . --fail-on-error
```

## 7. Non-MVP Features

Excluded from MVP: React UI, runtime Actuator integration, security analyzer, GitHub Action, OpenRewrite auto-fixes, IDE plugin, AI chat interface.

## 8. Non-Functional Requirements

- **NFR-001** — Deterministic output
- **NFR-002** — No runtime requirement on scanned app
- **NFR-003** — Testable scanner and diagnostic rules
- **NFR-004** — Extensible rule engine
- **NFR-005** — Handles up to 1,000 Java files in under 10 seconds

## 9. Product Positioning

> Architecture debugger for Spring Boot.
