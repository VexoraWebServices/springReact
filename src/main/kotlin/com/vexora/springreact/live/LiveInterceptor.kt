package com.vexora.springreact.live

import java.security.Principal

/** What's about to run: which component, which action, and who's connected. */
data class LiveActionContext(
    val component: String,
    val action: String,
    val principal: Principal?,
)

/**
 * Middleware that runs before every `@LiveAction` — for logging, metrics, multi-tenancy,
 * rate limiting, custom auth, etc. Register one or more as Spring beans (ordered with
 * `@Order`). Return `false` to block the action; the client gets an error and state is
 * untouched.
 *
 * ```
 * @Component
 * class AuditLog : LiveInterceptor {
 *     override fun beforeAction(ctx: LiveActionContext): Boolean {
 *         log.info("{}#{} by {}", ctx.component, ctx.action, ctx.principal?.name)
 *         return true
 *     }
 * }
 * ```
 */
fun interface LiveInterceptor {
    fun beforeAction(ctx: LiveActionContext): Boolean
}
