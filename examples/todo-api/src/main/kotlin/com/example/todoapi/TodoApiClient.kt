package com.example.todoapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Talks to the REST API over HTTP, the way a separate frontend service would. The base URL
 * is configurable (`todo.api.base-url`) — point it at a remote API and nothing else changes.
 * Here it defaults to this same app so the example runs as one process.
 */
@Component
class TodoApiClient(
    @Value("\${todo.api.base-url:http://localhost:8080}") baseUrl: String,
) {
    private val rest: RestClient = RestClient.create(baseUrl)
    private val listType = object : ParameterizedTypeReference<List<TodoDto>>() {}

    fun list(): List<TodoDto> =
        rest.get().uri("/api/todos").retrieve().body(listType) ?: emptyList()

    fun add(text: String) {
        rest.post().uri("/api/todos").body(NewTodo(text)).retrieve().toBodilessEntity()
    }

    fun toggle(id: Int) {
        rest.put().uri("/api/todos/{id}/toggle", id).retrieve().toBodilessEntity()
    }

    fun remove(id: Int) {
        rest.delete().uri("/api/todos/{id}", id).retrieve().toBodilessEntity()
    }
}
