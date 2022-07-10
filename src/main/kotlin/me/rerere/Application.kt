package me.rerere

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import me.rerere.database.DatabaseFactory
import me.rerere.plugins.configureRouting
import me.rerere.plugins.configureSecurity
import me.rerere.util.Response

fun main(args: Array<String>) {
    DatabaseFactory.init()
    EngineMain.main(args)
}

fun Application.modules() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(Response(
                code = -1,
                message = cause.message ?: "Unknown error",
                body = null
            ))
        }
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(Response(
                code = -2,
                message = status.description,
                body = null
            ))
        }
    }

    configureSecurity()
    configureRouting()
}
