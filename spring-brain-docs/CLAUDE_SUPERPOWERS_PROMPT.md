# Claude Superpowers Prompt — Spring Brain

Copy this into Claude Code with Superpowers enabled.

```text
You are working as a senior Java/Spring Boot architect and product engineer.

We are building a new open-source developer tool called Spring Brain.

Product goal:
Spring Brain scans a Spring Boot codebase and generates an interactive architecture map showing request lifecycle and application structure. It should also detect broken links and architecture problems in the application.

Inspiration:
Laravel Brain visualizes Laravel application architecture, but this project must be a clean Spring Boot implementation. Do not copy code, assets, UI, file names, or implementation details from Laravel Brain. Build a Spring-native architecture intelligence tool.

Use Superpowers methodology:
- Start with brainstorming.
- Then write a concrete implementation plan.
- Use TDD for core scanner and diagnostics.
- Use isolated branches/worktrees if available.
- Use subagent/code-review workflow if available.
- Do not implement everything at once.
- Keep changes small, reviewable, and milestone-based.
- After each milestone, create a summary of what changed, tests added, and next steps.

Tech stack:
- Java 21
- Spring Boot 3.x
- Maven multi-module project
- JavaParser
- JavaSymbolSolver
- Jackson
- Picocli for CLI
- JUnit 5
- AssertJ
- React + TypeScript + Vite + React Flow for viewer later

Repository structure target:

spring-brain/
  spring-brain-core/
  spring-brain-cli/
  spring-brain-server/
  spring-brain-viewer/
  spring-brain-samples/
  docs/

MVP scope:
1. Scan a Spring Boot project directory.
2. Detect:
   - @RestController
   - @Controller
   - @RequestMapping
   - @GetMapping
   - @PostMapping
   - @PutMapping
   - @PatchMapping
   - @DeleteMapping
   - @Service
   - @Component
   - @Repository
   - @Entity
   - JpaRepository
   - CrudRepository
   - @Value("${...}")
3. Build an architecture graph:
   Route → Controller Method → Service Method/Class → Repository → Entity
4. Export graph.json.
5. Detect these MVP diagnostics:
   - Controller method does not call any service
   - Controller directly calls repository
   - Service has injected repository but repository class not found
   - Repository generic type is not annotated with @Entity
   - @Value property is missing from application.properties or application.yml
6. Create CLI:
   - spring-brain scan --path .
   - spring-brain scan --path . --output .spring-brain
7. Generate:
   - .spring-brain/graph.json
   - .spring-brain/diagnostics.json
   - .spring-brain/summary.md

Important design rules:
- The scanner must be deterministic.
- Avoid runtime execution in MVP.
- Do not require the target Spring Boot app to start.
- Prefer static analysis first.
- Keep the graph schema stable and documented.
- All diagnostics must include:
  - severity
  - code
  - message
  - file path
  - line number when available
  - suggested fixes
- Build with tests first for every diagnostic rule.
- Add sample Spring Boot projects under spring-brain-samples for scanner validation.

Implementation plan required before coding:
Create these docs first:
1. docs/PRD.md
2. docs/ARCHITECTURE.md
3. docs/MVP_PLAN.md
4. docs/GRAPH_SCHEMA.md
5. docs/DIAGNOSTIC_RULES.md

Then implement Milestone 0 only:
Milestone 0 deliverables:
- Maven multi-module project
- spring-brain-core module
- spring-brain-cli module
- basic Picocli command
- sample Spring Boot app
- unit test setup
- GitHub Actions workflow running mvn test
- README with basic usage

After Milestone 0, stop and provide:
- files created
- architecture decisions
- how to run tests
- what remains for Milestone 1

Quality bar:
- No giant files.
- No vague TODO-only implementation.
- No hardcoded absolute paths.
- Strong package naming.
- Clear domain model.
- Tests must pass.
- Keep output readable.
- Prefer simple working scanner over over-engineered abstractions.

Start by brainstorming the project boundaries and risks. Then produce the implementation plan. Then execute only Milestone 0.
```
