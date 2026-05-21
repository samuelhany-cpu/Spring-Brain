# Phase 3 Bean Dependency Analysis Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Start Phase 3 by adding a static Spring bean dependency graph and circular dependency diagnostics.

**Architecture:** Extend the scanner to collect generic Spring beans from `@Component`, `@Configuration`, `@ControllerAdvice`, and repository interfaces in addition to the existing controller/service/repository/entity models. Store those beans in `ProjectModel`, emit `bean` graph nodes and `injects` graph edges for bean-to-bean dependencies, and add an independent diagnostic rule that detects dependency cycles from the static injection graph.

**Tech Stack:** Java 17, JavaParser, Maven, JUnit 5, AssertJ.

---

## Files

- Create: `spring-brain-core/src/main/java/com/springbrain/core/model/BeanModel.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/model/ProjectModel.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/scanner/SpringAnnotationScanner.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/graph/GraphBuilder.java`
- Create: `spring-brain-core/src/main/java/com/springbrain/core/diagnostic/rules/CircularDependencyRule.java`
- Modify: `spring-brain-core/src/main/java/com/springbrain/core/diagnostic/DiagnosticEngine.java`
- Modify: `docs/GRAPH_SCHEMA.md`
- Modify: `docs/DIAGNOSTIC_RULES.md`
- Modify: `docs/ROADMAP.md`
- Test: `spring-brain-core/src/test/java/com/springbrain/core/scanner/SpringAnnotationScannerTest.java`
- Test: `spring-brain-core/src/test/java/com/springbrain/core/graph/GraphBuilderTest.java`
- Test: `spring-brain-core/src/test/java/com/springbrain/core/diagnostic/rules/CircularDependencyRuleTest.java`
- Create fixtures under `spring-brain-core/src/test/resources/fixtures/`

---

### Task 1: Model And Scan Generic Beans

- [ ] **Step 1: Write scanner tests for bean detection**

Add tests to `SpringAnnotationScannerTest` that scan fixtures for `@Component`, `@Configuration`, `@ControllerAdvice`, and a repository interface. Assert that `ProjectModel#getBeans()` contains deterministic `BeanModel` entries with class name, qualified name, bean type, file, line, and injected type names.

- [ ] **Step 2: Run scanner tests and verify RED**

Run: `mvn -pl spring-brain-core -Dtest=SpringAnnotationScannerTest test`

Expected: compile failure or test failure because `BeanModel` and `ProjectModel#getBeans()` do not exist yet.

- [ ] **Step 3: Implement `BeanModel`**

Create `BeanModel` as an immutable final class with:

```java
private final String className;
private final String qualifiedName;
private final String beanType;
private final Path file;
private final int line;
private final List<String> injectedTypeNames;
```

Add getters and copy injected type names defensively.

- [ ] **Step 4: Add beans to `ProjectModel`**

Add `List<BeanModel> beans`, a getter, a builder field, and `Builder#beans(List<BeanModel>)`.

- [ ] **Step 5: Extend scanner bean collection**

Update `SpringAnnotationScanner` to collect beans for controllers, services, repositories, and extra Spring bean annotations. Bean types should be lowercase stable strings such as `controller`, `service`, `repository`, `component`, `configuration`, and `controller_advice`.

- [ ] **Step 6: Run scanner tests and verify GREEN**

Run: `mvn -pl spring-brain-core -Dtest=SpringAnnotationScannerTest test`

Expected: tests pass.

---

### Task 2: Add Bean Nodes And Injection Edges To Graph

- [ ] **Step 1: Write graph tests for bean nodes and injects edges**

Add tests to `GraphBuilderTest` that build a small `ProjectModel` with beans `AlphaService -> BetaService -> GammaClient`. Assert the graph contains `bean:` nodes and `injects` edges, sorted deterministically.

- [ ] **Step 2: Run graph tests and verify RED**

Run: `mvn -pl spring-brain-core -Dtest=GraphBuilderTest test`

Expected: graph tests fail because bean nodes and generic injection edges are not emitted yet.

- [ ] **Step 3: Implement graph bean support**

Update `GraphBuilder` to add:

- `bean:{qualifiedName}` nodes with type `bean`.
- `injects` edges from each bean to any injected type that resolves to another bean.
- Stable lookup by simple class name and implemented service interface name where available.

- [ ] **Step 4: Run graph tests and verify GREEN**

Run: `mvn -pl spring-brain-core -Dtest=GraphBuilderTest test`

Expected: tests pass.

---

### Task 3: Add Circular Dependency Diagnostic

- [ ] **Step 1: Write circular dependency rule tests**

Create `CircularDependencyRuleTest` with:

- A positive test for `AlphaService -> BetaService -> AlphaService`.
- A three-node positive test for `AlphaService -> BetaService -> GammaService -> AlphaService`.
- A negative test for an acyclic chain.

Assert code `SPRING_BRAIN_CIRCULAR_DEPENDENCY`, severity `ERROR`, stable message, related node IDs, and suggested fixes.

- [ ] **Step 2: Run diagnostic tests and verify RED**

Run: `mvn -pl spring-brain-core -Dtest=CircularDependencyRuleTest test`

Expected: compile failure because `CircularDependencyRule` does not exist.

- [ ] **Step 3: Implement `CircularDependencyRule`**

Create the rule as an independent `DiagnosticRule`. Build an adjacency map from `ProjectModel#getBeans()`, resolve injected type names to beans, run DFS with a recursion stack, and emit one deterministic diagnostic per unique cycle.

- [ ] **Step 4: Register the rule**

Add `new CircularDependencyRule()` to `DiagnosticEngine.defaultRules()`.

- [ ] **Step 5: Run diagnostic tests and verify GREEN**

Run: `mvn -pl spring-brain-core -Dtest=CircularDependencyRuleTest,DiagnosticEngineTest test`

Expected: tests pass.

---

### Task 4: Documentation And Full Verification

- [ ] **Step 1: Update docs**

Update:

- `docs/GRAPH_SCHEMA.md` with `bean` node type and `injects` edge usage.
- `docs/DIAGNOSTIC_RULES.md` with `SPRING_BRAIN_CIRCULAR_DEPENDENCY`.
- `docs/ROADMAP.md` to mark the bean dependency graph and circular dependency detection as the first Phase 3 slice now started.

- [ ] **Step 2: Run full verification**

Run: `mvn test`

Expected: build success with all Maven tests passing.

- [ ] **Step 3: Check git status**

Run: `git status --short`

Expected: only intentional Phase 3 files are modified or created.
