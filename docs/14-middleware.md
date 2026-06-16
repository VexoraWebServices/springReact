# 14. Middleware (Action Interceptors)

A `LiveInterceptor` runs **before every `@LiveAction`** — the place for cross-cutting
concerns: logging, metrics, multi-tenancy, rate limiting, custom auth. It's like Next.js
middleware, but for your live actions.

## Write one

Just a Spring bean implementing `LiveInterceptor`:

```kotlin
@Component
class AuditLog : LiveInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun beforeAction(ctx: LiveActionContext): Boolean {
        log.info("{}#{} by {}", ctx.component, ctx.action, ctx.principal?.name ?: "anon")
        return true            // allow the action to run
    }
}
```

`ctx` tells you the component name, the action name, and the connected `Principal`.

## Block an action

Return `false` to stop it. The action never runs and the client gets an error.

```kotlin
@Component
class RateLimit(private val limiter: Limiter) : LiveInterceptor {
    override fun beforeAction(ctx: LiveActionContext): Boolean =
        limiter.tryAcquire(ctx.principal?.name ?: "anon")   // false → blocked
}
```

## Many interceptors

Register as many as you like; order them with `@Order`:

```kotlin
@Component @Order(1) class Tenant : LiveInterceptor { ... }
@Component @Order(2) class Audit  : LiveInterceptor { ... }
```

They run in order; if **any** returns `false`, the action is blocked.

## Interceptor vs `@LiveAuthorize`

| Use | For |
|---|---|
| `@LiveAuthorize("ROLE")` | declarative role checks on specific actions |
| `LiveInterceptor` | global logic that runs for *every* action (logging, tenancy, limits) |

You can use both — interceptors run first, then per-action authorization.

## TODO checklist

- [ ] A `@Component` implementing `LiveInterceptor` that logs each action
- [ ] One that returns `false` to block under some condition
- [ ] Order two interceptors with `@Order`
- [ ] Next: [How It Works](12-how-it-works.md)
