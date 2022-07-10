package me.rerere.plugins

import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureSecurity() {
    val environment = environment
    authentication {
        jwt {
            val secret = environment.config.property("jwt.secret").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .build()
            )
            validate { credential ->
                JWTPrincipal(credential.payload)
            }
        }
    }
}
