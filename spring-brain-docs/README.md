# Spring Brain Docs Pack

This folder contains planning documents for **Spring Brain**, a Spring Boot architecture intelligence tool.

## Included Files

```text
PRD.md
ARCHITECTURE.md
MVP_PLAN.md
GRAPH_SCHEMA.md
DIAGNOSTIC_RULES.md
ROADMAP.md
CLAUDE_SUPERPOWERS_PROMPT.md
MILESTONE_PROMPTS.md
```

## Recommended Start

Read in this order:

1. `PRD.md`
2. `ARCHITECTURE.md`
3. `MVP_PLAN.md`
4. `GRAPH_SCHEMA.md`
5. `DIAGNOSTIC_RULES.md`
6. `CLAUDE_SUPERPOWERS_PROMPT.md`
7. `MILESTONE_PROMPTS.md`
8. `ROADMAP.md`

## How to Use With Claude

Start with:

```text
CLAUDE_SUPERPOWERS_PROMPT.md
```

Then continue milestone by milestone using:

```text
MILESTONE_PROMPTS.md
```

## MVP Target

The MVP should produce:

```text
.spring-brain/
├── graph.json
├── diagnostics.json
└── summary.md
```

from this command:

```bash
spring-brain scan --path .
```
