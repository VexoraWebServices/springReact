# Example — SpringReact as the frontend for a REST API

This app has **two halves in one process**:

1. A normal Spring Boot **REST API** (`/api/todos`, returns JSON) — `Api.kt`.
2. A SpringReact **UI** that consumes that API over HTTP via `RestClient` — `TodoApiClient.kt`
   + `Screens.kt`.

It demonstrates using SpringReact purely as the **frontend tier** for an existing API.

## Run

```bash
# from the repo root, make the framework available locally first:
./gradlew publishToMavenLocal

cd examples/todo-api && gradle bootRun     # → http://localhost:8080
```

- UI: <http://localhost:8080>
- API: <http://localhost:8080/api/todos>

Add/toggle/remove in the UI and refresh `/api/todos` — the data is the same, because the UI
goes through the API.

## Point at a remote API

The API base URL is configurable:

```properties
# application.properties
todo.api.base-url=https://your-api.example.com
```

Set it to a separate API service and the UI keeps working unchanged — SpringReact is now a
pure frontend for that remote API.

See [docs/21-frontend-for-an-api.md](../../docs/21-frontend-for-an-api.md) for the full
explanation, including the simpler same-app option (inject the service directly).
