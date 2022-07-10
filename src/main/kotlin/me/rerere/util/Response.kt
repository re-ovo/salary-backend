package me.rerere.util

import io.ktor.server.application.*
import io.ktor.server.response.*

data class Response<T>(
    val code: Int,
    val message: String,
    val body: T? = null
)

suspend inline fun <reified T : Any> ApplicationCall.respondRestful(
    code: Int,
    message: String,
    body: T? = null
) {
    this.respond(Response(code, message, body))
}