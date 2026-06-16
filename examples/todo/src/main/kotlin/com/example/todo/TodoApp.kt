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
    data class Todo(val id: Int, val text: String, var done: Boolean = false)

    private val items = mutableListOf<Todo>()
    private val seq = AtomicInteger(0)

    fun all(): List<Todo> = items.toList()
    fun add(text: String) { items.add(Todo(seq.incrementAndGet(), text)) }
    fun toggle(id: Int) { items.find { it.id == id }?.let { it.done = !it.done } }
    fun remove(id: Int) { items.removeIf { it.id == id } }
    fun count(): Int = items.size
}
