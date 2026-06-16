package com.example.todo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@SpringBootApplication
class TodoApp

fun main(args: Array<String>) {
    runApplication<TodoApp>(*args)
}

/** A tiny in-memory store — swap for a real repository in a real app. */
@Service
class TodoStore {
    data class Todo(
        val id: Int,
        val text: String,
        var done: Boolean = false,
        var qty: Int = 1,
    )

    private val items = mutableListOf<Todo>()
    private val seq = AtomicInteger(0)

    fun all(): List<Todo> = items.toList()

    /** Case-insensitive duplicate check (used to show the custom dialog). */
    fun exists(text: String): Boolean =
        items.any { it.text.equals(text.trim(), ignoreCase = true) }

    fun add(text: String) { items.add(Todo(seq.incrementAndGet(), text.trim())) }
    fun toggle(id: Int) { items.find { it.id == id }?.let { it.done = !it.done } }
    fun remove(id: Int) { items.removeIf { it.id == id } }
    fun setQty(id: Int, delta: Int) {
        items.find { it.id == id }?.let { it.qty = (it.qty + delta).coerceAtLeast(1) }
    }
    fun clearDone() { items.removeIf { it.done } }

    fun count(): Int = items.size
    fun activeCount(): Int = items.count { !it.done }
}
