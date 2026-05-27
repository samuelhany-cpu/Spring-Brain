# Spring Brain — Graph Schema

## 1. Purpose

The graph schema defines how Spring Brain represents a Spring Boot application architecture.

The graph is:
- Deterministic
- Versioned
- Machine-readable (JSON)
- AI-friendly

## 2. Output File

```text
.spring-brain/graph.json
```

## 3. Top-Level Structure

```json
{
  "schemaVersion": "1.0.0",
  "metadata": {},
  "nodes": [],
  "edges": []
}
```

## 4. Metadata Schema

```json
{
  "tool": "spring-brain",
  "toolVersion": "0.2.0",
  "projectName": "my-app",
  "generatedAt": "2026-01-01T12:00:00Z",
  "sourceMode": "static",
  "language": "java",
  "framework": "spring-boot"
}
```

## 5. Node Schema

```json
{
  "id": "controller:com.example.UserController#getUser",
  "type": "controller",
  "label": "UserController.getUser",
  "qualifiedName": "com.example.UserController#getUser",
  "file": "src/main/java/com/example/UserController.java",
  "line": 22,
  "metadata": {}
}
```

## 6. Edge Schema

```json
{
  "id": "edge:route:GET:/api/users/{id}->controller:com.example.UserController#getUser",
  "from": "route:GET:/api/users/{id}",
  "to": "controller:com.example.UserController#getUser",
  "type": "maps_to",
  "metadata": {}
}
```

## 7. Node Types

| Type | Description |
|------|-------------|
| `route` | HTTP endpoint (method + path) |
| `controller` | Controller method handling a route |
| `service` | Spring service class |
| `repository` | Spring Data repository interface |
| `entity` | JPA entity class |
| `config_property` | Application config property key |
| `bean` | Generic Spring-managed bean (`@Component`, `@Configuration`, `@ControllerAdvice`, or any component discovered during bean dependency analysis). Overlaps with `controller`/`service`/`repository` in the graph when those nodes also carry `@Service` etc. |

## 8. Edge Types

| Type | Description |
|------|-------------|
| `maps_to` | Route → Controller method |
| `calls` | Controller → Service, Service → Repository |
| `injects` | Dependency injection relationship |
| `manages` | Repository → Entity |
| `uses_config` | Code → Config property |

## 9. Node ID Conventions

| Node Type | Format | Example |
|-----------|--------|---------|
| Route | `route:{HTTP_METHOD}:{PATH}` | `route:GET:/api/users/{id}` |
| Controller | `controller:{QUALIFIED_CLASS}#{METHOD}` | `controller:com.example.UserController#getUser` |
| Service | `service:{QUALIFIED_CLASS}` | `service:com.example.UserService` |
| Repository | `repository:{QUALIFIED_INTERFACE}` | `repository:com.example.UserRepository` |
| Entity | `entity:{QUALIFIED_CLASS}` | `entity:com.example.User` |
| Config | `config:{PROPERTY_KEY}` | `config:jwt.secret` |
| Bean | `bean:{QUALIFIED_CLASS}` | `bean:com.example.BillingComponent` |

### Route HTTP Method values

`{HTTP_METHOD}` in route node IDs is one of:

| Value | Source annotation |
|-------|-------------------|
| `GET` | `@GetMapping` or `@RequestMapping(method = RequestMethod.GET)` |
| `POST` | `@PostMapping` or `@RequestMapping(method = RequestMethod.POST)` |
| `PUT` | `@PutMapping` or `@RequestMapping(method = RequestMethod.PUT)` |
| `PATCH` | `@PatchMapping` or `@RequestMapping(method = RequestMethod.PATCH)` |
| `DELETE` | `@DeleteMapping` or `@RequestMapping(method = RequestMethod.DELETE)` |
| `HEAD` | `@RequestMapping(method = RequestMethod.HEAD)` |
| `OPTIONS` | `@RequestMapping(method = RequestMethod.OPTIONS)` |
| `ANY` | `@RequestMapping` with no `method` attribute, or with multiple methods |

`ANY` means the endpoint accepts all HTTP methods (or the specific set could not be statically determined).

## 10. Sorting Rules

1. Sort nodes by `type`, then `id`
2. Sort edges by `type`, then `from`, then `to`
3. Use relative paths, not absolute machine paths
4. Sort metadata keys alphabetically

## 11. Graph Validation Rules

A valid graph must satisfy:
- Every edge `from` ID exists in `nodes`
- Every edge `to` ID exists in `nodes`
- Node IDs are unique
- Edge IDs are unique
- Node and edge types are known
- Schema version is present

## 12. Versioning Policy

- **Additive changes** (new node/edge types): minor version bump
- **Breaking changes**: major version bump
- **Metadata-only changes**: patch version bump
