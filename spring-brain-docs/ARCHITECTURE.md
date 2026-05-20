# Spring Brain — Architecture Document

## 1. Architecture Overview

Spring Brain is a static analysis and architecture reporting tool for Spring Boot applications.

The MVP architecture is intentionally simple:

```text
CLI
 ↓
Scanner
 ↓
Project Model
 ↓
Graph Builder
 ↓
Diagnostic Engine
 ↓
Exporters
```

Output:

```text
.spring-brain/
├── graph.json
├── diagnostics.json
└── summary.md
```

## 2. Design Principles

### 2.1 Static First

The MVP must analyze source code without starting the target Spring Boot application.

This avoids:

- Runtime failures
- Database requirements
- External service dependencies
- Profile configuration issues
- Slow scanning

### 2.2 Deterministic Output

All nodes, edges, diagnostics, and reports must be sorted consistently.

### 2.3 Rule-Based Diagnostics

Each diagnostic should be implemented as an independent rule class.

### 2.4 Stable Graph Schema

The graph schema should be versioned from the beginning.

### 2.5 Small Modules

Avoid giant services and mixed responsibilities.

## 3. Recommended Repository Structure

```text
spring-brain/
├── spring-brain-core/
│   ├── src/main/java/com/springbrain/core/
│   │   ├── scanner/
│   │   ├── parser/
│   │   ├── model/
│   │   ├── graph/
│   │   ├── diagnostics/
│   │   ├── config/
│   │   └── export/
│   └── src/test/java/
│
├── spring-brain-cli/
│   ├── src/main/java/com/springbrain/cli/
│   └── src/test/java/
│
├── spring-brain-server/
│   └── reserved for future local API server
│
├── spring-brain-viewer/
│   └── reserved for future React UI
│
├── spring-brain-samples/
│   ├── clean-crud-app/
│   ├── broken-missing-bean-app/
│   ├── broken-controller-direct-repository-app/
│   ├── broken-repository-entity-mismatch-app/
│   └── broken-config-property-app/
│
├── docs/
│   ├── PRD.md
│   ├── ARCHITECTURE.md
│   ├── MVP_PLAN.md
│   ├── GRAPH_SCHEMA.md
│   └── DIAGNOSTIC_RULES.md
│
├── pom.xml
└── README.md
```

## 4. Module Responsibilities

## 4.1 spring-brain-core

Contains the main domain logic.

Responsibilities:

- Source file discovery
- Java parsing
- Spring annotation detection
- Project model construction
- Graph generation
- Diagnostic rule execution
- JSON and Markdown export

Should not depend on CLI-specific code.

## 4.2 spring-brain-cli

Contains the command-line interface.

Responsibilities:

- Parse CLI arguments
- Validate input path
- Trigger scan process
- Print terminal summary
- Exit with appropriate status code

## 4.3 spring-brain-server

Future module.

Responsibilities:

- Serve graph data through HTTP
- Support local viewer
- Provide analysis API

Not required in MVP.

## 4.4 spring-brain-viewer

Future module.

Responsibilities:

- Interactive graph viewer
- Diagnostic panel
- Endpoint lifecycle view
- Filtering by node type

Not required in MVP.

## 5. Core Pipeline

## 5.1 Scan Command

```bash
spring-brain scan --path ./target-app --output .spring-brain
```

## 5.2 File Discovery

Input:

```text
target-app/src/main/java
```

Output:

```text
List<JavaSourceFile>
```

Each source file should include:

- Absolute path
- Relative path
- Package name
- Raw content
- Parsed AST

## 5.3 Java Parsing

Use JavaParser to parse source files.

Output:

```text
CompilationUnit
```

The parser extracts:

- Classes
- Interfaces
- Methods
- Fields
- Constructors
- Annotations
- Method calls
- Generic types
- Imports
- Line numbers

## 5.4 Project Model Construction

The scanner converts raw AST data into domain models:

```text
ProjectModel
├── controllers
├── routes
├── services
├── repositories
├── entities
├── components
├── configPropertyUsages
└── methodCalls
```

## 5.5 Graph Builder

The graph builder receives `ProjectModel`.

It creates:

```text
GraphDocument
├── metadata
├── nodes
└── edges
```

## 5.6 Diagnostic Engine

The diagnostic engine receives:

```text
ProjectModel
GraphDocument
ConfigurationModel
```

It runs all enabled rules and produces:

```text
List<Diagnostic>
```

## 5.7 Exporters

Exporters write:

```text
graph.json
diagnostics.json
summary.md
```

## 6. Important Domain Models

## 6.1 ProjectModel

```java
public final class ProjectModel {
    private final Path rootPath;
    private final List<ControllerModel> controllers;
    private final List<ServiceModel> services;
    private final List<RepositoryModel> repositories;
    private final List<EntityModel> entities;
    private final List<ConfigPropertyUsageModel> configPropertyUsages;
    private final List<MethodCallModel> methodCalls;
}
```

## 6.2 ControllerModel

```java
public final class ControllerModel {
    private final String className;
    private final String packageName;
    private final String qualifiedName;
    private final Path file;
    private final int line;
    private final List<RouteModel> routes;
    private final List<DependencyModel> dependencies;
}
```

## 6.3 RouteModel

```java
public final class RouteModel {
    private final String httpMethod;
    private final String path;
    private final String controllerClass;
    private final String methodName;
    private final Path file;
    private final int line;
}
```

## 6.4 ServiceModel

```java
public final class ServiceModel {
    private final String className;
    private final String qualifiedName;
    private final Path file;
    private final int line;
    private final List<DependencyModel> dependencies;
    private final List<MethodModel> methods;
}
```

## 6.5 RepositoryModel

```java
public final class RepositoryModel {
    private final String interfaceName;
    private final String qualifiedName;
    private final String entityType;
    private final String idType;
    private final Path file;
    private final int line;
}
```

## 6.6 EntityModel

```java
public final class EntityModel {
    private final String className;
    private final String qualifiedName;
    private final Path file;
    private final int line;
}
```

## 6.7 Diagnostic

```java
public final class Diagnostic {
    private final Severity severity;
    private final String code;
    private final String message;
    private final Path file;
    private final Integer line;
    private final List<String> relatedNodeIds;
    private final List<String> suggestedFixes;
}
```

## 7. Diagnostic Rule Architecture

```text
DiagnosticRule
├── ControllerWithoutServiceRule
├── ControllerDirectRepositoryRule
├── MissingRepositoryBeanRule
├── RepositoryEntityMismatchRule
└── MissingConfigPropertyRule
```

Interface:

```java
public interface DiagnosticRule {
    String code();
    List<Diagnostic> analyze(ProjectModel projectModel, GraphDocument graph);
}
```

## 8. CLI Architecture

Recommended commands:

```bash
spring-brain --help

spring-brain scan --path .
spring-brain scan --path . --output .spring-brain
spring-brain scan --path . --fail-on-error
```

Exit code behavior:

```text
0 = scan completed with no errors
1 = scan failed due to tool error
2 = scan completed but diagnostics include ERROR and --fail-on-error is enabled
```

## 9. Future Architecture Extensions

## 9.1 Local Server

```text
spring-brain serve --path .
```

Runs local API:

```text
GET /api/graph
GET /api/diagnostics
GET /api/summary
```

## 9.2 React Viewer

Visualizes:

- Graph nodes
- Edges
- Endpoint lifecycle
- Diagnostics
- Source locations

## 9.3 Runtime Integration

Future versions may use Spring Boot Actuator endpoints:

- `/actuator/beans`
- `/actuator/mappings`
- `/actuator/conditions`
- `/actuator/configprops`
- `/actuator/scheduledtasks`

## 9.4 Auto-Fixes

Future versions may use OpenRewrite recipes for safe refactoring.

## 10. Risk Areas

### 10.1 Method Resolution

Static method-call tracing is difficult.

Initial MVP should use simple heuristics:

- Injected field or constructor parameter type
- Method call variable name
- Known service/repository class names

Later versions can use JavaSymbolSolver.

### 10.2 Lombok

Lombok-generated constructors may not exist in source AST.

MVP should support common annotations:

- `@RequiredArgsConstructor`
- `@AllArgsConstructor`
- `@NoArgsConstructor`

### 10.3 Multiple Beans

Spring allows multiple implementations of an interface.

MVP may report uncertain cases as warnings instead of errors.

### 10.4 Profiles

Config files may differ by profile.

MVP should scan all known application files and report missing keys conservatively.

## 11. Architecture Decision Records

Recommended ADRs:

```text
ADR-001 Static analysis first
ADR-002 JavaParser as initial parser
ADR-003 JSON graph as stable interchange format
ADR-004 Diagnostics implemented as independent rules
ADR-005 CLI-first MVP
```
