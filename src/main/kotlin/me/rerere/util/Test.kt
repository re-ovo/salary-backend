package me.rerere.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun main() {
    val jwt = JWT.create()
        .withClaim("user","admin")
        .sign(Algorithm.HMAC256("secret"))
    println(jwt)

    JWT.require(Algorithm.HMAC256("secretwww"))
        .withClaim("user","admin")
        .build()
        .verify(jwt)
}