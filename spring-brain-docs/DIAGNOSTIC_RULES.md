# Spring Brain — Diagnostic Rules

## 1. Purpose

Diagnostic rules detect broken architecture links and risky Spring Boot patterns.

Each diagnostic should answer:

```text
What is wrong?
Where is it?
Why does it matter?
How can it be fixed?
```

## 2. Diagnostic Output File

Default path:

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
      "relatedNodeIds": [
        "repository:com.example.user.UserRepository"
      ],
      "suggestedFixes": [
        "Use a JPA entity as the repository generic type.",
        "Annotate the target class with @Entity if it is intended to be persisted."
      ]
    }
  ]
}
```

## 4. Severity Levels

## 4.1 INFO

Used for low-risk observations.

Example:

```text
Service method appears unused.
```

## 4.2 WARNING

Used for architecture smells or likely issues.

Example:

```text
Controller method does not call a service.
```

## 4.3 ERROR

Used for likely broken application behavior.

Example:

```text
Repository generic type is not an entity.
```

## 5. MVP Rules

## Rule 1 — Controller Without Service

### Code

```text
SPRING_BRAIN_CONTROLLER_WITHOUT_SERVICE
```

### Severity

```text
WARNING
```

### Problem

A controller route method does not appear to call a service.

### Why It Matters

Business logic may be placed directly inside the controller, or the endpoint may be incomplete.

### Detection Logic

For each controller method with route mapping:

1. Check method body for calls to known service dependencies
2. Check injected dependencies of controller
3. If no service call is found, emit warning

### Example Bad Code

```java
@RestController
@RequestMapping("/api/users")
class UserController {
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id) {
        return "user";
    }
}
```

### Example Diagnostic

```json
{
  "severity": "WARNING",
  "code": "SPRING_BRAIN_CONTROLLER_WITHOUT_SERVICE",
  "message": "Controller method UserController.getUser does not call any service.",
  "file": "src/main/java/com/example/user/UserController.java",
  "line": 12,
  "suggestedFixes": [
    "Move business logic to a @Service class.",
    "Inject a service into the controller and delegate the operation."
  ]
}
```

## Rule 2 — Controller Directly Calls Repository

### Code

```text
SPRING_BRAIN_CONTROLLER_DIRECT_REPOSITORY_ACCESS
```

### Severity

```text
WARNING
```

### Problem

A controller directly depends on or calls a repository.

### Why It Matters

This violates the common Spring layered architecture:

```text
Controller → Service → Repository
```

### Detection Logic

For each controller:

1. Check injected dependencies
2. If any dependency is a known repository, emit warning
3. Check method calls
4. If a controller method calls a repository variable, emit warning

### Example Bad Code

```java
@RestController
class UserController {
    private final UserRepository userRepository;

    @GetMapping("/users")
    List<User> all() {
        return userRepository.findAll();
    }
}
```

### Suggested Fixes

- Create a `UserService`
- Move repository call to service layer
- Inject service into controller

## Rule 3 — Missing Repository Bean

### Code

```text
SPRING_BRAIN_MISSING_REPOSITORY_BEAN
```

### Severity

```text
ERROR
```

### Problem

A service depends on a repository type that cannot be found in scanned source files.

### Why It Matters

The application may fail to start or the flow may be incomplete.

### Detection Logic

For each service dependency:

1. If dependency name/type looks like a repository
2. Check if matching repository exists in `ProjectModel.repositories`
3. If not found, emit error

### Example Bad Code

```java
@Service
class OrderService {
    private final OrderRepository orderRepository;
}
```

But no `OrderRepository` interface exists.

### Suggested Fixes

- Create the missing repository interface
- Ensure repository is under component scan path
- Check spelling of repository class name
- Check package scanning boundaries

## Rule 4 — Repository Entity Mismatch

### Code

```text
SPRING_BRAIN_REPOSITORY_ENTITY_MISMATCH
```

### Severity

```text
ERROR
```

### Problem

A repository generic type is not annotated with `@Entity`.

### Why It Matters

Spring Data JPA repositories should manage JPA entities, not DTOs or request models.

### Example Bad Code

```java
public interface UserRepository extends JpaRepository<UserDto, Long> {
}
```

### Detection Logic

For each repository:

1. Extract first generic type argument
2. Search for matching entity class
3. If the class exists but is not annotated with `@Entity`, emit error
4. If the class does not exist in scanned source, emit warning or error depending on confidence

### Suggested Fixes

- Replace DTO type with entity type
- Add `@Entity` if the class is intended to be persisted
- Create a separate mapper between entity and DTO

## Rule 5 — Missing Config Property

### Code

```text
SPRING_BRAIN_MISSING_CONFIG_PROPERTY
```

### Severity

```text
ERROR
```

### Problem

A property is used in code but not found in configuration files.

### Example Bad Code

```java
@Value("${jwt.secret}")
private String jwtSecret;
```

But no config file contains:

```text
jwt.secret
```

### Detection Logic

1. Extract property keys from `@Value("${...}")`
2. Load keys from application properties/yml/yaml files
3. Compare used keys against defined keys
4. Emit diagnostic for missing keys

### Suggested Fixes

- Add property to `application.properties`
- Add property to the correct profile file
- Use `@ConfigurationProperties` for grouped configuration
- Check spelling of property key

## 6. Future Diagnostic Rules

## 6.1 Circular Dependency

```text
SPRING_BRAIN_CIRCULAR_DEPENDENCY
```

Detect:

```text
A → B → C → A
```

## 6.2 Fat Controller

```text
SPRING_BRAIN_FAT_CONTROLLER
```

Detect controller classes with too many methods or too much logic.

## 6.3 Fat Service

```text
SPRING_BRAIN_FAT_SERVICE
```

Detect service classes with too many dependencies or long methods.

## 6.4 Public Risky Endpoint

```text
SPRING_BRAIN_PUBLIC_RISKY_ENDPOINT
```

Detect public endpoints with dangerous HTTP methods:

```text
POST
PUT
PATCH
DELETE
```

## 6.5 Entity Exposed Directly

```text
SPRING_BRAIN_ENTITY_EXPOSED_IN_API
```

Detect controller methods returning entity classes directly.

## 6.6 Hardcoded Secret

```text
SPRING_BRAIN_HARDCODED_SECRET
```

Detect string literals that look like secrets.

## 6.7 Missing Timeout in External Client

```text
SPRING_BRAIN_EXTERNAL_CLIENT_WITHOUT_TIMEOUT
```

Detect HTTP clients without timeout configuration.

## 6.8 Unused Service Method

```text
SPRING_BRAIN_UNUSED_SERVICE_METHOD
```

Detect service methods not called from controllers, schedulers, listeners, or other services.

## 7. Rule Engine Design

Interface:

```java
public interface DiagnosticRule {
    String code();

    List<Diagnostic> analyze(ProjectModel projectModel, GraphDocument graphDocument);
}
```

Engine:

```java
public final class DiagnosticEngine {
    private final List<DiagnosticRule> rules;

    public List<Diagnostic> run(ProjectModel projectModel, GraphDocument graphDocument) {
        return rules.stream()
            .flatMap(rule -> rule.analyze(projectModel, graphDocument).stream())
            .sorted(DiagnosticOrdering.DEFAULT)
            .toList();
    }
}
```

## 8. Diagnostic Ordering

Sort diagnostics by:

```text
severity: ERROR → WARNING → INFO
file path
line number
code
message
```

## 9. Rule Testing Requirements

Each rule must have:

- Positive test
- Negative test
- Edge case test if applicable
- Stable diagnostic code
- Stable message format

## 10. Good Diagnostic Message Format

Use this style:

```text
{ClassName}.{methodName} directly uses {RepositoryName}. Controllers should delegate persistence access through a service layer.
```

Avoid vague messages like:

```text
Bad architecture.
```

## 11. Suggested Fix Quality Bar

Every diagnostic must include at least one actionable fix.

Good:

```text
Create UserService and move UserRepository.findAll() into UserService.findAll().
```

Bad:

```text
Fix this.
```
