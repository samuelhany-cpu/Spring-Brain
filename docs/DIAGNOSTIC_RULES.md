# Spring Brain — Diagnostic Rules

## 1. Purpose

Diagnostic rules detect broken architecture links and risky Spring Boot patterns.

Each diagnostic answers:

```text
What is wrong?
Where is it?
Why does it matter?
How can it be fixed?
```

## 2. Output File

```text
.spring-brain/diagnostics.json
```

## 3. Diagnostic Schema

```json
{
  "schemaVersion": "1.0.0",
  "diagnostics": [
    {
      "severity": "ERROR",
      "code": "SPRING_BRAIN_REPOSITORY_ENTITY_MISMATCH",
      "message": "UserRepository uses UserDto, but UserDto is not annotated with @Entity.",
      "file": "src/main/java/com/example/user/UserRepository.java",
      "line": 8,
      "relatedNodeIds": ["repository:com.example.user.UserRepository"],
      "suggestedFixes": [
        "Use a JPA entity as the repository generic type.",
        "Annotate the target class with @Entity if it is intended to be persisted."
      ]
    }
  ]
}
```

## 4. Severity Levels

| Severity | Meaning |
|----------|---------|
| `INFO` | Low-risk observation |
| `WARNING` | Architecture smell or likely issue |
| `ERROR` | Likely broken application behavior |

## 5. MVP Rules

### Rule 1 — Controller Without Service

**Code:** `SPRING_BRAIN_CONTROLLER_WITHOUT_SERVICE`
**Severity:** WARNING

A controller route method does not appear to call a service. Business logic may be placed directly in the controller, or the endpoint may be incomplete.

**Suggested fixes:**
- Move business logic to a `@Service` class
- Inject a service into the controller and delegate

---

### Rule 2 — Controller Directly Calls Repository

**Code:** `SPRING_BRAIN_CONTROLLER_DIRECT_REPOSITORY_ACCESS`
**Severity:** WARNING

A controller directly depends on or calls a repository, violating `Controller → Service → Repository`.

**Suggested fixes:**
- Create a service class
- Move repository call to service layer
- Inject service into controller

---

### Rule 3 — Missing Repository Bean

**Code:** `SPRING_BRAIN_MISSING_REPOSITORY_BEAN`
**Severity:** ERROR

A service depends on a repository type that cannot be found in scanned source files.

**Suggested fixes:**
- Create the missing repository interface
- Ensure it's under component scan path
- Check spelling of repository class name

---

### Rule 4 — Repository Entity Mismatch

**Code:** `SPRING_BRAIN_REPOSITORY_ENTITY_MISMATCH`
**Severity:** ERROR

A repository generic type is not annotated with `@Entity`. Spring Data JPA repositories should manage JPA entities, not DTOs.

**Suggested fixes:**
- Replace DTO type with entity type
- Add `@Entity` if the class is intended to be persisted
- Create a mapper between entity and DTO

---

### Rule 5 — Missing Config Property

**Code:** `SPRING_BRAIN_MISSING_CONFIG_PROPERTY`
**Severity:** ERROR

A property is used via `@Value("${key}")` but not found in any scanned configuration file.

**Suggested fixes:**
- Add property to `application.properties`
- Add property to the correct profile file
- Use `@ConfigurationProperties` for grouped configuration
- Check spelling of property key

---

## 6. Phase 3 Rules

### Rule 6 - Circular Dependency

**Code:** `SPRING_BRAIN_CIRCULAR_DEPENDENCY`
**Severity:** ERROR

Two or more Spring beans depend on each other through constructor or autowired field injection. Circular dependencies can prevent startup or make bean initialization order fragile.

**Suggested fixes:**
- Introduce an interface or mediator to break the direct bean cycle
- Move shared behavior into a third bean that both cyclic beans can depend on
- Replace one dependency with an event or callback if it is not required during construction

---

## 7. Rule Engine Interface

```java
public interface DiagnosticRule {
    String code();
    List<Diagnostic> analyze(ProjectModel model, GraphDocument graph);
}
```

## 8. Diagnostic Ordering

Sort by:
1. Severity: ERROR → WARNING → INFO
2. File path
3. Line number
4. Code
5. Message

## 9. Testing Requirements

Each rule must have:
- Positive test (rule fires)
- Negative test (rule does not fire for clean code)
- Stable diagnostic code
- At least one actionable suggested fix
