package io.springreact.live

/**
 * Validation errors for a form action. Declare it as a parameter right after the form
 * object and the framework fills it from Bean Validation before your action runs:
 *
 * ```
 * @LiveAction
 * fun save(form: TodoForm, errors: LiveErrors) {
 *     if (errors.hasErrors()) { this.error = errors["title"]; return }
 *     // ... persist form
 * }
 * ```
 */
class LiveErrors {

    private val fields = LinkedHashMap<String, String>()

    fun reject(field: String, message: String) {
        fields.putIfAbsent(field, message)
    }

    fun hasErrors(): Boolean = fields.isNotEmpty()

    operator fun get(field: String): String? = fields[field]

    fun asMap(): Map<String, String> = fields
}
