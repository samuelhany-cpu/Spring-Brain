# spring-brain-viewer

Interactive React Flow graph viewer for Spring Brain. Embedded in the CLI fat JAR and served by a JDK `HttpServer` on `spring-brain serve`.

## Tech Stack

| Layer | Library |
|-------|---------|
| Framework | React 18 + TypeScript 5 |
| Bundler | Vite 5 |
| Graph | React Flow 11 + @dagrejs/dagre |
| Styling | Tailwind CSS 3 |
| Unit tests | Vitest + React Testing Library |
| E2E tests | Playwright |

## Features

- Auto-layout architecture graph (Route → Controller → Service → Repository → Entity)
- Filter chips by node type (Controllers, Services, Repos, Entities, Configs)
- Live search — dims unmatched nodes
- Detail panel — shows qualifiedName, file:line, injected beans, routes, diagnostics scoped to file
- Live status indicator (SSE auto-refresh when output files change)
- Diagnostics badge (⚠ warnings / ✖ errors) in the toolbar

## Development

```bash
cd spring-brain-viewer
npm install
npm run dev          # Vite dev server on :5173, proxies /api → localhost:3000
```

Start a `serve` process in another terminal to provide the API:

```bash
java -jar ../spring-brain-cli/target/spring-brain-cli-0.1.0.jar serve \
  --path ../spring-brain-samples/clean-crud-app --port 3000
```

## Tests

```bash
npm test             # Vitest unit tests (27 tests)
npx playwright test  # E2E tests (9 tests — requires JAR built)
```

## Build

The viewer is built automatically by Maven via `frontend-maven-plugin` in the `spring-brain-viewer` module, then copied into the CLI JAR by `maven-resources-plugin`.

```bash
# From repo root:
mvn package          # builds viewer, copies dist/ into JAR
```
