# Phase 4 Security Intelligence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add static Spring Security intelligence: annotation detection, `SecurityFilterChain` rule extraction, endpoint security matrix reporting, and risky public endpoint diagnostics.

**Architecture:** Extend `ProjectModel` with security annotation and filter-chain rule models collected by `SpringAnnotationScanner`. Add a resolver that combines route mappings, method/class security annotations, and path-based filter-chain rules into endpoint security rows. Use the resolver in the Markdown summary and a new diagnostic rule that flags public mutating endpoints.

**Tech Stack:** Java 17, JavaParser, Maven, JUnit 5, AssertJ.

---

## Files

- Create: `spring-brain-core/src/main/java/com/springbrain/core/model/SecurityAnnotationModel.java`
- Create: `spring-brain-core/src/main/java/com/springbrain/core/model/SecurityRuleModel.java`
- Create: `spring-brain-core/src/main/java/com/springbrain/core/security/EndpointSecurityModel.java`
- Create: `spring-brain-core/src/main/java/com/springbrain/core/security/EndpointSecurityResolver.java`
- Create: `spring-brain-core/src/main/java/com/springbrain/core/diagnostic/rules/PublicRiskyEndpointRule.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/model/ProjectModel.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/scanner/SpringAnnotationScanner.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/report/SummaryReportGenerator.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/diagnostic/DiagnosticEngine.java`
- Modify: `docs/DIAGNOSTIC_RULES.md`
- Modify: `docs/ROADMAP.md`
- Test: `spring-brain-core/src/test/java/com/springbrain/core/scanner/SpringAnnotationScannerTest.java`
- Test: `spring-brain-core/src/test/java/com/springbrain/core/security/EndpointSecurityResolverTest.java`
- Test: `spring-brain-core/src/test/java/com/springbrain/core/diagnostic/rules/PublicRiskyEndpointRuleTest.java`
- Test: `spring-brain-core/src/test/java/com/springbrain/core/report/SummaryReportGeneratorTest.java`
- Create fixtures under `spring-brain-core/src/test/resources/fixtures/`

---

### Task 1: Scan Security Annotations And Filter Chain Rules

- [ ] **Step 1: Write scanner tests for security annotations**

Add fixtures with `@PreAuthorize`, `@Secured`, and `@RolesAllowed` on controller class and methods. Add assertions that `ProjectModel#getSecurityAnnotations()` contains the annotation name, expression, owner class, optional method name, file, and line.

- [ ] **Step 2: Write scanner tests for `SecurityFilterChain` rules**

Add a fixture with:

```java
.requestMatchers("/", "/login").permitAll()
.requestMatchers("/admin/**").authenticated()
.requestMatchers("/client/**").hasRole("CLIENT")
```

Assert that `ProjectModel#getSecurityRules()` contains three stable rules with path patterns, access types, details, file, and line.

- [ ] **Step 3: Run scanner tests and verify RED**

Run: `mvn -pl spring-brain-core -Dtest=SpringAnnotationScannerTest test`

Expected: compile failure or assertion failure because security models and scanner support do not exist yet.

- [ ] **Step 4: Implement security models and `ProjectModel` fields**

Create immutable model classes and add `securityAnnotations` and `securityRules` lists to `ProjectModel` and its builder.

- [ ] **Step 5: Implement scanner extraction**

Update `SpringAnnotationScanner` to extract `@PreAuthorize`, `@Secured`, `@RolesAllowed`, and simple chained `requestMatchers(...).permitAll/authenticated/hasRole/hasAnyRole` rules from `SecurityFilterChain` methods.

- [ ] **Step 6: Run scanner tests and verify GREEN**

Run: `mvn -pl spring-brain-core -Dtest=SpringAnnotationScannerTest test`

Expected: tests pass.

---

### Task 2: Resolve Endpoint Security And Report Matrix

- [ ] **Step 1: Write resolver tests**

Create tests where:

- Method-level `@PreAuthorize` resolves an endpoint as `PROTECTED`.
- Class-level `@PreAuthorize` applies to all methods without method-level annotations.
- `permitAll` filter-chain rules resolve matching endpoints as `PUBLIC`.
- `authenticated` and `hasRole` rules resolve matching endpoints as `PROTECTED`.
- Unmatched endpoints resolve as `UNKNOWN`.

- [ ] **Step 2: Run resolver tests and verify RED**

Run: `mvn -pl spring-brain-core -Dtest=EndpointSecurityResolverTest test`

Expected: compile failure because resolver classes do not exist yet.

- [ ] **Step 3: Implement endpoint security resolver**

Create `EndpointSecurityResolver` with deterministic route ordering. Precedence: method annotation, class annotation, first matching filter-chain rule, then `UNKNOWN`.

- [ ] **Step 4: Update summary report tests**

Add a test that generated `summary.md` contains a `## Endpoint Security Matrix` section with method, path, access, source, and detail columns.

- [ ] **Step 5: Run summary test and verify RED**

Run: `mvn -pl spring-brain-core -Dtest=SummaryReportGeneratorTest test`

Expected: failure because the section is missing.

- [ ] **Step 6: Add security matrix to summary report**

Update `SummaryReportGenerator` to append the endpoint security matrix after the endpoint list.

- [ ] **Step 7: Run resolver and summary tests and verify GREEN**

Run: `mvn -pl spring-brain-core "-Dtest=EndpointSecurityResolverTest,SummaryReportGeneratorTest" test`

Expected: tests pass.

---

### Task 3: Flag Risky Public Endpoints

- [ ] **Step 1: Write diagnostic tests**

Create `PublicRiskyEndpointRuleTest` with:

- A positive test for `POST /signup` matched by `permitAll`.
- A negative test for `GET /login` matched by `permitAll`.
- A negative test for `POST /admin/tours` matched by `authenticated`.

Assert code `SPRING_BRAIN_PUBLIC_RISKY_ENDPOINT`, severity `WARNING`, message, related route node ID, and suggested fixes.

- [ ] **Step 2: Run diagnostic tests and verify RED**

Run: `mvn -pl spring-brain-core -Dtest=PublicRiskyEndpointRuleTest test`

Expected: compile failure because `PublicRiskyEndpointRule` does not exist.

- [ ] **Step 3: Implement diagnostic rule**

Use `EndpointSecurityResolver.resolve(model)` and flag public endpoints with HTTP methods `POST`, `PUT`, `PATCH`, or `DELETE`.

- [ ] **Step 4: Register the rule**

Add `new PublicRiskyEndpointRule()` to `DiagnosticEngine.defaultRules()`.

- [ ] **Step 5: Run diagnostic and engine tests and verify GREEN**

Run: `mvn -pl spring-brain-core "-Dtest=PublicRiskyEndpointRuleTest,DiagnosticEngineTest" test`

Expected: tests pass.

---

### Task 4: Docs, Full Verification, Real-Project Validation, Merge, Push

- [ ] **Step 1: Update docs**

Update `docs/DIAGNOSTIC_RULES.md` with `SPRING_BRAIN_PUBLIC_RISKY_ENDPOINT` and update `docs/ROADMAP.md` to mark Phase 4 as started/completed for the implemented scope.

- [ ] **Step 2: Run full verification**

Run: `mvn test`

Expected: build success with all tests passing.

- [ ] **Step 3: Package CLI**

Run: `mvn -pl spring-brain-cli -am package -DskipTests`

Expected: executable CLI jar exists at `spring-brain-cli/target/spring-brain-cli-0.1.0.jar`.

- [ ] **Step 4: Scan ToursWebsite copy**

Run:

```powershell
java -jar spring-brain-cli\target\spring-brain-cli-0.1.0.jar scan --path "F:\ToursWebsite-main\ToursWebsite-main - Copy" --output "F:\ToursWebsite-main\ToursWebsite-main - Copy\.spring-brain-phase4"
```

Expected: scan succeeds and writes graph, diagnostics, and summary.

- [ ] **Step 5: Inspect Phase 4 results**

Read `.spring-brain-phase4/summary.md` and `diagnostics.json`. Confirm that the security matrix appears and that risky public endpoint diagnostics are understandable.

- [ ] **Step 6: Commit, merge, and push**

If full verification and ToursWebsite validation look good:

```bash
git add <intentional files>
git commit -m "feat: add phase 4 security intelligence"
git -C "f:\Spring Brain" merge phase-4-security-intelligence
git -C "f:\Spring Brain" push origin main
```

Expected: merge succeeds on `main`, `mvn test` passes on `main`, and push to `origin/main` succeeds.
