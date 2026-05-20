# Spring Brain — Graph Schema

## 1. Purpose

The graph schema defines how Spring Brain represents a Spring Boot application architecture.

The graph should be:

- Deterministic
- Versioned
- Easy to visualize
- Easy to process by AI tools
- Easy to diff in CI/CD

## 2. Output File

Default path:

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
  "schemaVersion": "1.0.0",
  "metadata": {
    "tool": "spring-brain",
    "toolVersion": "0.1.0",
    "projectName": "sample-app",
    "projectRoot": "/path/to/project",
    "generatedAt": "2026-01-01T12:00:00Z",
    "sourceMode": "static",
    "language": "java",
    "framework": "spring-boot"
  },
  "nodes": [],
  "edges": []
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
  "id": "edge:route:get:/api/users/{id}->controller:com.example.UserController#getUser",
  "from": "route:GET:/api/users/{id}",
  "to": "controller:com.example.UserController#getUser",
  "type": "maps_to",
  "metadata": {}
}
```

## 7. Node Types

Supported MVP node types:

```text
route
controller
service
repository
entity
config_property
```

Future node types:

```text
component
scheduler
event
event_listener
security_rule
external_client
database_table
dto
mapper
exception_handler
configuration_class
bean
```

## 8. Edge Types

Supported MVP edge types:

```text
maps_to
calls
injects
manages
uses_config
```

Future edge types:

```text
publishes
listens_to
scheduled_calls
secured_by
returns
accepts
maps_dto
calls_external
reads_from
writes_to
```

## 9. Node ID Rules

IDs must be deterministic.

## 9.1 Route Node ID

Format:

```text
route:{HTTP_METHOD}:{PATH}
```

Example:

```text
route:GET:/api/users/{id}
```

## 9.2 Controller Method Node ID

Format:

```text
controller:{QUALIFIED_CLASS_NAME}#{METHOD_NAME}
```

Example:

```text
controller:com.example.user.UserController#getUser
```

## 9.3 Service Class Node ID

Format:

```text
service:{QUALIFIED_CLASS_NAME}
```

Example:

```text
service:com.example.user.UserService
```

## 9.4 Repository Node ID

Format:

```text
repository:{QUALIFIED_INTERFACE_NAME}
```

Example:

```text
repository:com.example.user.UserRepository
```

## 9.5 Entity Node ID

Format:

```text
entity:{QUALIFIED_CLASS_NAME}
```

Example:

```text
entity:com.example.user.User
```

## 9.6 Config Property Node ID

Format:

```text
config:{PROPERTY_KEY}
```

Example:

```text
config:jwt.secret
```

## 10. Example Graph

```json
{
  "schemaVersion": "1.0.0",
  "metadata": {
    "tool": "spring-brain",
    "toolVersion": "0.1.0",
    "projectName": "clean-crud-app",
    "sourceMode": "static",
    "language": "java",
    "framework": "spring-boot"
  },
  "nodes": [
    {
      "id": "route:GET:/api/users/{id}",
      "type": "route",
      "label": "GET /api/users/{id}",
      "qualifiedName": "GET /api/users/{id}",
      "file": "src/main/java/com/example/user/UserController.java",
      "line": 18,
      "metadata": {
        "httpMethod": "GET",
        "path": "/api/users/{id}"
      }
    },
    {
      "id": "controller:com.example.user.UserController#getUser",
      "type": "controller",
      "label": "UserController.getUser",
      "qualifiedName": "com.example.user.UserController#getUser",
      "file": "src/main/java/com/example/user/UserController.java",
      "line": 19,
      "metadata": {
        "className": "UserController",
        "methodName": "getUser"
      }
    },
    {
      "id": "service:com.example.user.UserService",
      "type": "service",
      "label": "UserService",
      "qualifiedName": "com.example.user.UserService",
      "file": "src/main/java/com/example/user/UserService.java",
      "line": 12,
      "metadata": {}
    },
    {
      "id": "repository:com.example.user.UserRepository",
      "type": "repository",
      "label": "UserRepository",
      "qualifiedName": "com.example.user.UserRepository",
      "file": "src/main/java/com/example/user/UserRepository.java",
      "line": 8,
      "metadata": {
        "entityType": "User",
        "idType": "Long"
      }
    },
    {
      "id": "entity:com.example.user.User",
      "type": "entity",
      "label": "User",
      "qualifiedName": "com.example.user.User",
      "file": "src/main/java/com/example/user/User.java",
      "line": 10,
      "metadata": {}
    }
  ],
  "edges": [
    {
      "id": "edge:route:GET:/api/users/{id}->controller:com.example.user.UserController#getUser",
      "from": "route:GET:/api/users/{id}",
      "to": "controller:com.example.user.UserController#getUser",
      "type": "maps_to",
      "metadata": {}
    },
    {
      "id": "edge:controller:com.example.user.UserController#getUser->service:com.example.user.UserService",
      "from": "controller:com.example.user.UserController#getUser",
      "to": "service:com.example.user.UserService",
      "type": "calls",
      "metadata": {}
    },
    {
      "id": "edge:service:com.example.user.UserService->repository:com.example.user.UserRepository",
      "from": "service:com.example.user.UserService",
      "to": "repository:com.example.user.UserRepository",
      "type": "calls",
      "metadata": {}
    },
    {
      "id": "edge:repository:com.example.user.UserRepository->entity:com.example.user.User",
      "from": "repository:com.example.user.UserRepository",
      "to": "entity:com.example.user.User",
      "type": "manages",
      "metadata": {}
    }
  ]
}
```

## 11. Sorting Rules

To keep output deterministic:

1. Sort nodes by `type`, then `id`
2. Sort edges by `type`, then `from`, then `to`
3. Sort metadata keys alphabetically where possible
4. Do not include unstable object memory references
5. Use relative paths, not machine-specific absolute paths

## 12. Graph Validation Rules

A valid graph must satisfy:

- Every edge `from` ID exists in `nodes`
- Every edge `to` ID exists in `nodes`
- Node IDs are unique
- Edge IDs are unique
- Node type is known
- Edge type is known
- Schema version exists

## 13. Future Compatibility

The schema should allow new node and edge types without breaking existing consumers.

Recommended policy:

- Additive changes: minor version
- Breaking changes: major version
- Metadata-only changes: patch version
