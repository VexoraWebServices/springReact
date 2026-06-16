# Contributing to SpringReact

Thanks for your interest in improving SpringReact! Contributions of all kinds are welcome —
bug reports, docs, examples, and code.

## Prerequisites

- **JDK 21** (a Java toolchain is configured; the wrapper handles Gradle)
- **Node 22+** (used to bundle the client runtime; the build calls it via the node-gradle plugin)

## Build & test

One command builds everything and runs both test suites (Kotlin integration tests + the
client vitest/jsdom tests):

```bash
./gradlew build
```

Useful tasks:

```bash
./gradlew test                       # Spring integration tests + client tests
./gradlew :gradle-plugin:build       # the Gradle plugin
./gradlew publishToMavenLocal        # install to ~/.m2 for local example apps
(cd client && npm test)              # client tests only
```

Run an example end-to-end:

```bash
./gradlew publishToMavenLocal
cd examples/todo && gradle bootRun   # http://localhost:8080
```

## Guidelines

- **Kotlin only** for framework and tests (no Java).
- **Every feature ships with a doc page** under `docs/` and, where it makes sense, a test.
- Keep the public API small and consistent with the existing `@Live*` / `Html` style.
- Match the surrounding code's formatting and comment density.

## Pull requests

1. Fork and create a branch.
2. Make your change with tests + docs.
3. Ensure `./gradlew build` is green.
4. Open a PR describing the change and why.

## Reporting issues

Please include: what you expected, what happened, a minimal reproduction, and your JDK/Node
versions. For UI/browser issues, the browser console output is very helpful.
