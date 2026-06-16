# 8. Authorization

Guard actions so only permitted users can run them. Add `@LiveAuthorize` and provide a
`LiveSecurity` bean that decides.

## Step 1 — guard an action

```kotlin
@LiveComponent("Admin")
class AdminScreen : ServerComponent {

    @LiveState var log = mutableListOf<String>()

    @LiveAction
    @LiveAuthorize("ADMIN")          // only ADMINs may run this
    fun wipe() { log.clear() }

    @LiveAction
    fun ping() { log.add("ping") }   // no annotation = open to anyone

    override fun render(): UiNode = ul(log.map { li(it) })
}
```

You can also put `@LiveAuthorize` on the **class** to guard every action in it.

When an unauthorized call arrives, the action is **skipped** and the client receives an
error message (`{ "t": "error", "message": "Not authorized for action 'wipe'" }`). State is
never touched.

## Step 2 — decide who's allowed

Provide a `LiveSecurity` bean. It receives the connected user's `Principal` and the
required roles:

```kotlin
@Configuration
class SecurityWiring {
    @Bean
    fun liveSecurity(): LiveSecurity = LiveSecurity { principal, roles ->
        if (roles.isEmpty()) return@LiveSecurity true
        val auth = principal as? Authentication ?: return@LiveSecurity false
        auth.authorities.any { a -> roles.any { "ROLE_$it" == a.authority || it == a.authority } }
    }
}
```

This bridges to Spring Security: the WebSocket `principal` is the authenticated user, and
you check their authorities against the required roles.

## Default behavior

If you don't provide a `LiveSecurity` bean, the default is:

- action with **no** roles → allowed
- action **with** roles → allowed only if there's an authenticated `principal`

So guarded actions are locked down out of the box; you opt into real role logic with your
own bean.

## How it fits

```
client calls "wipe"
        │
handler finds @LiveAuthorize("ADMIN")
        │
liveSecurity.authorize(session.principal, ["ADMIN"])  →  false
        │
action skipped, client gets { t:"error", message:"Not authorized…" }
```

## Tips

- Pair with Spring Security so `session.principal` is the real user (secure the WebSocket
  handshake/HTTP session as usual).
- Keep authorization on the **server** (here) — never trust the client to hide buttons.
- You can still hide UI for unauthorized users in `render()`; the guard is the real gate.

## TODO checklist

- [ ] Add `@LiveAuthorize("ROLE")` to a sensitive action
- [ ] Provide a `LiveSecurity` bean that checks the principal's roles
- [ ] Verify a denied call returns an error and changes nothing
- [ ] Next: [Lists & Keys](09-lists-and-keys.md)
