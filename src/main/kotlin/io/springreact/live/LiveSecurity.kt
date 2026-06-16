package io.springreact.live

import java.security.Principal

/**
 * Guards a `@LiveAction` (or a whole `@LiveComponent`) behind one or more roles. Before the
 * action runs, the framework asks the [LiveSecurity] bean whether the connected user is
 * allowed; if not, the action is skipped and the client receives an error.
 *
 * ```
 * @LiveAction
 * @LiveAuthorize("ADMIN")
 * fun deleteEverything() { ... }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LiveAuthorize(vararg val roles: String)

/**
 * Decides whether a connected user may run a guarded action. Provide your own bean to
 * integrate with Spring Security (inspect the [Principal] / authentication) — the default
 * simply requires an authenticated principal when roles are present.
 */
fun interface LiveSecurity {
    fun authorize(principal: Principal?, roles: Array<out String>): Boolean
}
