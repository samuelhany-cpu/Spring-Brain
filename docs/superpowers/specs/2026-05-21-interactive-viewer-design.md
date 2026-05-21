# Spring Brain — Interactive Viewer Design

**Date:** 2026-05-21
**Phase:** 2 — Interactive Viewer

---

## Overview

Add a browser-based interactive graph viewer to Spring Brain. After running `scan`, the user can run `serve` (or pass `--serve` to `scan`) to open a React + React Flow visualization of their Spring Boot architecture in the browser. The viewer auto-refreshes via SSE whenever a new scan is written to `.spring-brain/`.

---

## Decisions

| Question | Decision |
|---|---|
| How is the viewer launched? | `serve` command + `--serve` flag on `scan` |
| How is the viewer distributed? | React app built by Maven, embedded in the fat JAR as classpath resources |
| HTTP server | JDK built-in `com.sun.net.httpserver.HttpServer` — no extra dependency |
| Auto-refresh | Server-Sent Events (SSE) + JDK `WatchService` |
| Layout | Toolbar (top) + React Flow graph (center) + Detail/Diagnostics panel (right) |
| Node.js at runtime? | No — `frontend-maven-plugin` installs Node during build only |

---

## Architecture

### Module structure

```
spring-brain/
├── spring-brain-viewer/     ← NEW: Vite + React + TypeScript app
│   ├── src/
│   ├── package.json
│   └── pom.xml              ← frontend-maven-plugin: npm install + npm run build
├── spring-brain-cli/        ← MODIFIED: embeds viewer dist/, adds ServeCommand
│   └── src/main/
│       ├── java/.../ServeCommand.java    ← NEW
│       ├── java/.../ViewerServer.java    ← NEW
│       └── resources/viewer/            ← copied from spring-brain-viewer/dist/
└── pom.xml                  ← adds spring-brain-viewer module (before spring-brain-cli)
```

### Dependency chain

```
spring-brain-viewer (npm build → dist/)
       ↓  maven-resources-plugin copies dist/
spring-brain-cli (embeds viewer as classpath resources, adds ServeCommand)
       ↓
spring-brain-core (unchanged)
```

---

## CLI

### New command

```bash
java -jar spring-brain-cli.jar serve --path ./my-app
java -jar spring-brain-cli.jar serve --path ./my-app --port 3000
```

Errors with exit code 1 and a helpful message if `.spring-brain/` does not exist inside `--path` (tells user to run `scan` first).

### Updated scan command

```bash
java -jar spring-brain-cli.jar scan --path ./my-app --serve
java -jar spring-brain-cli.jar scan --path ./my-app --serve --port 3000
```

After writing output files, `ScanCommand` delegates to `ServeCommand` with the same path and port.

### Exit codes

| Code | Meaning |
|---|---|
| 0 | Server started (blocks until Ctrl+C) |
| 1 | `.spring-brain/` not found at given path |

---

## Server (`ViewerServer`)

### Endpoints

| Endpoint | Handler |
|---|---|
| `GET /` | Serves `viewer/index.html` from classpath |
| `GET /*` | Serves static assets from classpath `viewer/` |
| `GET /api/graph` | Reads `.spring-brain/graph.json`, streams as `application/json` |
| `GET /api/diagnostics` | Reads `.spring-brain/diagnostics.json`, streams as `application/json` |
| `GET /api/events` | SSE endpoint (`text/event-stream`), pushes `data: refresh\n\n` on file change |

### Auto-refresh

A JDK `WatchService` watches `.spring-brain/`. When `graph.json` or `diagnostics.json` is modified, it broadcasts `data: refresh\n\n` to all open SSE connections. The React app re-fetches both API endpoints on receipt.

### Port selection

Default port: 3000. If 3000 is busy, the server tries the next available port and prints the actual URL used.

---

## React App (`spring-brain-viewer/src/`)

### File structure

```
src/
├── App.tsx                  ← root: owns state, wires data to components
├── types.ts                 ← TypeScript types mirroring graph.json schema
├── hooks/
│   └── useGraphData.ts      ← fetches /api/graph + /api/diagnostics, wires EventSource
├── components/
│   ├── Toolbar.tsx          ← filter chips, search input, stats + diagnostics badge
│   ├── GraphCanvas.tsx      ← React Flow canvas, custom nodes, node click handler
│   ├── DetailPanel.tsx      ← selected node info + diagnostics list
│   └── nodes/
│       ├── RouteNode.tsx
│       ├── ControllerNode.tsx
│       ├── ServiceNode.tsx
│       ├── RepositoryNode.tsx
│       ├── EntityNode.tsx
│       └── ConfigNode.tsx
└── main.tsx
```

### Node colour scheme

| Node type | Colour |
|---|---|
| route | Blue `#79c0ff` |
| controller | Orange `#ffa657` |
| service | Green `#7ee787` |
| repository | Purple `#d2a8ff` |
| entity | Pink `#f778ba` |
| config_property | Yellow `#e3b341` |

### Toolbar features

- Filter chips: All / Routes / Controllers / Services / Repositories / Entities
- Search input: dims nodes whose label does not match the query
- Stats badge: `{n} nodes · {n} edges`
- Diagnostics badge: `⚠ {n} warnings` / `✖ {n} errors` (hidden when 0)

### Detail panel (right)

When a node is selected:
- Node name, type badge, qualified name, file:line link
- Injects list (for controllers and services)
- Routes list (for controllers)
- Diagnostics scoped to this node's file

When nothing is selected:
- Full diagnostics list (all severities)
- `● Live — watching for changes` indicator (green when SSE connected, grey when reconnecting)

### Data flow

```
useGraphData
  └── fetch /api/graph + /api/diagnostics on mount
  └── EventSource /api/events → re-fetch on "refresh"
  └── returns { graph, diagnostics, liveStatus }

App
  └── applies filter + search → filteredNodes, filteredEdges
  └── passes to GraphCanvas
  └── selectedNodeId → DetailPanel

GraphCanvas
  └── renders React Flow with custom node components
  └── onNodeClick → sets selectedNodeId in App

DetailPanel
  └── looks up node by selectedNodeId
  └── filters diagnostics by node file
```

---

## Build Pipeline

### `spring-brain-viewer/pom.xml`

- `frontend-maven-plugin` installs Node 20 + npm 10 (pinned, downloaded to `target/`)
- Runs `npm install` then `npm run build`
- Output: `spring-brain-viewer/dist/`

### `spring-brain-cli/pom.xml`

- `maven-resources-plugin` copies `../spring-brain-viewer/dist/**` → `src/main/resources/viewer/` before compile phase
- Fat JAR includes `viewer/` under classpath root

### Root `pom.xml`

Modules ordered as:
1. `spring-brain-core`
2. `spring-brain-viewer`
3. `spring-brain-cli`
4. `spring-brain-server` (unchanged)

### Dev workflow

```bash
# Terminal 1 — Java backend
java -jar spring-brain-cli/target/spring-brain-cli-0.1.0.jar serve --path ./spring-brain-samples/clean-crud-app

# Terminal 2 — Vite dev server (proxies /api/* to :3000)
cd spring-brain-viewer && npm run dev
```

Vite's dev proxy forwards `/api/*` to the running Java server. No JAR rebuild needed while iterating on UI.

### `.gitignore` additions

```
spring-brain-viewer/node_modules/
spring-brain-viewer/dist/
.superpowers/
```

---

## Testing

### Java unit tests

| Test class | What it covers |
|---|---|
| `ServeCommandTest` | Exits 1 when `.spring-brain/` missing; starts successfully when present |
| `ViewerServerTest` | `/api/graph` and `/api/diagnostics` return JSON; `/api/events` returns `text/event-stream` |
| `ScanCommandTest` | `--serve` flag triggers server after scan completes |

### Frontend unit tests (Vitest + React Testing Library)

| Test file | What it covers |
|---|---|
| `useGraphData.test.ts` | Mocks fetch + EventSource; asserts data loads and SSE refresh triggers re-fetch |
| `Toolbar.test.tsx` | Filter chips show correct counts; search narrows visible nodes |
| `GraphCanvas.test.tsx` | Correct number of React Flow nodes rendered for a fixture graph |
| `DetailPanel.test.tsx` | Selected node metadata and matching diagnostics displayed |

### E2E tests (Playwright)

| Test file | What it covers |
|---|---|
| `viewer.spec.ts` | Serves `clean-crud-app`; asserts 12 nodes rendered; clicks `UserController` and checks detail panel; tests filter chip and search |
| `broken-app.spec.ts` | Serves `broken-controller-direct-repository-app`; asserts `⚠ 2 warnings` badge in toolbar and both violations listed in diagnostics panel |

E2E tests run in CI after the fat JAR is built (`mvn verify`).

### README test badge section

The README will be updated to show:

```
Unit: 125 tests  |  Integration: 7 tests  |  E2E: Playwright
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend framework | React 18 + TypeScript |
| Build tool | Vite 5 |
| Graph rendering | React Flow 11 |
| Styling | Tailwind CSS 3 |
| Frontend testing | Vitest + React Testing Library |
| E2E testing | Playwright |
| HTTP server | JDK `com.sun.net.httpserver.HttpServer` |
| File watching | JDK `java.nio.file.WatchService` |
| Frontend build in Maven | `frontend-maven-plugin` |
