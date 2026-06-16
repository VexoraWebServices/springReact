package com.example.todoapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicInteger

@SpringBootApplication
class TodoApiApp

fun main(args: Array<String>) {
    runApplication<TodoApiApp>(*args)
}

/** The data the API exchanges as JSON. */
data class TodoDto(val id: Int, val text: String, val done: Boolean)
data class NewTodo(val text: String = "")

/** A plain in-memory store behind the API. */
@Service
class TodoStore {
    private val items = mutableListOf<TodoDto>()
    private val seq = AtomicInteger(0)

    fun all(): List<TodoDto> = items.toList()
    fun add(text: String): TodoDto =
        TodoDto(seq.incrementAndGet(), text.trim(), false).also { items.add(it) }
    fun toggle(id: Int) {
        val i = items.indexOfFirst { it.id == id }
        if (i >= 0) items[i] = items[i].copy(done = !items[i].done)
    }
    fun remove(id: Int) { items.removeIf { it.id == id } }
}

/**
 * Your normal Spring Boot REST API — exactly what you'd build for mobile apps or third
 * parties. It knows nothing about SpringReact. Returns/accepts JSON.
 *
 *   GET    /api/todos            → list
 *   POST   /api/todos            → add        { "text": "..." }
 *   PUT    /api/todos/{id}/toggle→ toggle done
 *   DELETE /api/todos/{id}       → remove
 */
@RestController
@RequestMapping("/api/todos")
class TodoApiController(private val store: TodoStore) {

    @GetMapping
    fun list(): List<TodoDto> = store.all()

    @PostMapping
    fun add(@RequestBody body: NewTodo): TodoDto = store.add(body.text)

    @PutMapping("/{id}/toggle")
    fun toggle(@PathVariable id: Int) = store.toggle(id)

    @DeleteMapping("/{id}")
    fun remove(@PathVariable id: Int) = store.remove(id)
}
