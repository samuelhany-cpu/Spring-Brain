# Spring Brain — Follow-Up Claude Prompts

## Milestone 1 Prompt — Static Scanner

```text
Continue Spring Brain using Superpowers methodology.

Current goal:
Implement Milestone 1 — Static Scanner.

Scope:
- Find Java source files under src/main/java
- Parse Java files using JavaParser
- Detect classes and methods
- Detect annotations:
  @RestController
  @Controller
  @RequestMapping
  @GetMapping
  @PostMapping
  @PutMapping
  @PatchMapping
  @DeleteMapping
  @Service
  @Component
  @Repository
  @Entity
- Detect repository interfaces extending JpaRepository or CrudRepository
- Detect repository entity generic type
- Detect @Value("${...}") property usages
- Extract file path and line number when available

Required outputs:
- ProjectModel
- ControllerModel
- RouteModel
- ServiceModel
- RepositoryModel
- EntityModel
- ConfigPropertyUsageModel

Testing:
- Create sample classes in test resources
- Add unit tests for annotation detection
- Add unit tests for route extraction
- Add unit tests for repository generic extraction
- Add unit tests for @Value property extraction

Do not build graph yet.
Do not build React UI yet.
Do not implement diagnostics yet.

After implementation:
- Run tests
- Summarize files changed
- Explain design decisions
- List limitations
- Prepare Milestone 2 plan
```

## Milestone 2 Prompt — Graph Builder

```text
Continue Spring Brain using Superpowers methodology.

Goal:
Implement Milestone 2 — Graph Builder.

Input:
Use ProjectModel from Milestone 1.

Output:
Generate graph.json with:
- nodes
- edges
- metadata

Required graph relationships:
1. Route → Controller method
2. Controller → Service when a service dependency or method call is detected
3. Service → Repository when repository dependency or method call is detected
4. Repository → Entity based on JpaRepository<Entity, ID>

Create graph schema classes:
- GraphDocument
- GraphNode
- GraphEdge
- GraphMetadata

Node types:
- route
- controller
- service
- repository
- entity
- config_property

Edge types:
- maps_to
- calls
- injects
- manages
- uses_config

Add tests:
- clean CRUD sample should generate expected route-controller-service-repository-entity flow
- graph JSON should be stable and deterministic
- no duplicate nodes
- no duplicate edges

Do not implement diagnostics yet.
Do not implement UI yet.

After implementation:
- Run tests
- Show example graph.json
- Summarize design decisions
- Prepare Milestone 3 plan
```

## Milestone 3 Prompt — Broken Link Detector

```text
Continue Spring Brain using Superpowers methodology.

Goal:
Implement Milestone 3 — Broken Link Detector MVP.

Implement these diagnostic rules:
1. ControllerWithoutServiceRule
2. ControllerDirectRepositoryRule
3. MissingRepositoryBeanRule
4. RepositoryEntityMismatchRule
5. MissingConfigPropertyRule

Diagnostic fields:
- severity: INFO | WARNING | ERROR
- code
- message
- file
- line
- relatedNodeIds
- suggestedFixes

Output:
- .spring-brain/diagnostics.json
- terminal summary
- summary.md section for diagnostics

Testing:
Create broken sample apps or fixtures for each rule.
Each rule must have positive and negative tests.

Important:
- Rules must be independent classes.
- Rule engine should accept ProjectModel and GraphDocument.
- Keep rules deterministic.
- Do not use runtime Spring Boot startup in MVP.

After implementation:
- Run all tests
- Show sample diagnostics.json
- Explain limitations
- Prepare viewer milestone
```

## Milestone 4 Prompt — Summary Report

```text
Continue Spring Brain using Superpowers methodology.

Goal:
Implement Milestone 4 — Markdown Summary Report.

Output:
.spring-brain/summary.md

Required sections:
1. Scan Overview
2. Project Counts
3. Endpoint Summary
4. Architecture Graph Summary
5. Diagnostics Summary
6. Suggested Next Actions
7. Known Limitations

Rules:
- Keep output deterministic.
- Sort endpoints by HTTP method then path.
- Sort diagnostics by severity, file, line, code.
- Include useful links to source file paths where possible.

Testing:
- Add golden-file test for summary.md.
- Add test for empty diagnostics.
- Add test for multiple severities.

After implementation:
- Run tests
- Show sample summary.md
- List next recommended milestone
```

## Milestone 5 Prompt — React Viewer

```text
Continue Spring Brain using Superpowers methodology.

Goal:
Implement first version of Spring Brain Viewer.

Tech:
- React
- TypeScript
- Vite
- React Flow
- Tailwind CSS

Input:
- Load graph.json
- Load diagnostics.json

Required UI:
1. Graph canvas
2. Node details panel
3. Diagnostic panel
4. Filter by node type
5. Search by label/path
6. Endpoint lifecycle view

Do not build backend server yet.
For now, allow loading local JSON files from sample output.

Quality:
- Clean layout
- No visual clutter
- Works with the sample clean CRUD graph
- Diagnostics are visible and actionable

After implementation:
- Explain how to run viewer
- Show component structure
- List limitations
```

## Milestone 6 Prompt — GitHub Action

```text
Continue Spring Brain using Superpowers methodology.

Goal:
Create CI/CD integration.

Features:
- Add --fail-on-error CLI option if not already complete
- Create GitHub Action usage docs
- Add sample workflow:
  .github/workflows/spring-brain.yml
- Exit with code 2 when ERROR diagnostics exist and --fail-on-error is enabled

Testing:
- Unit test CLI behavior
- Document expected exit codes

After implementation:
- Show sample workflow
- Explain how teams can use this in pull requests
```
