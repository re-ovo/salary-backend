package me.rerere.route

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.database.DatabaseFactory
import me.rerere.model.User
import me.rerere.util.respondRestful

fun Routing.userRoute() {
    authenticate {
        get("/self") {
            withContext(Dispatchers.IO) {
                val eid = call.principal<JWTPrincipal>()?.getClaim("eid", Int::class)
                DatabaseFactory.get().use { connection ->
                    connection.createStatement().use { statement ->
                        statement.executeQuery("SELECT * FROM user WHERE eid=$eid").use { result ->
                            if (result.next()) {
                                call.respondRestful(
                                    code = 0,
                                    message = "success",
                                    body = User(
                                        eid = result.getInt("eid"),
                                        name = result.getString("name"),
                                        rank = result.getString("rank"),
                                        department = result.getString("department"),
                                        role = result.getInt("role")
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        get("/users") {
            withContext(Dispatchers.IO) {
                DatabaseFactory.get().use { connection ->
                    connection.createStatement().use { statement ->
                        statement.executeQuery("SELECT * FROM user").use { result ->
                            val users = mutableListOf<User>()
                            while (result.next()) {
                                users.add(
                                    User(
                                        eid = result.getInt("eid"),
                                        name = result.getString("name"),
                                        rank = result.getString("rank"),
                                        department = result.getString("department"),
                                        role = result.getInt("role")
                                    )
                                )
                            }
                            call.respondRestful(
                                code = 0,
                                message = "success",
                                body = users
                            )
                        }
                    }
                }
            }
        }

        put ("/user") {
            withContext(Dispatchers.IO) {
                if(call.principal<JWTPrincipal>()?.getClaim("role", Int::class) == 0) error("You don't have permission to do this")

                val postBody = call.receiveParameters()

                val eid = postBody["eid"]?.toInt() ?: error("eid is required")
                val name = postBody["name"] ?: error("name is required")
                val rank = postBody["rank"] ?: error("rank is required")
                val department = postBody["department"] ?: error("department is required")
                val role = postBody["role"]?.toInt() ?: error("role is required")
                val password = postBody["password"] ?: error("password is required")

                require(eid >= 0) { "eid must be greater than or equal to 0" }
                require(name.isNotEmpty()) { "name must not be empty" }
                require(rank.isNotEmpty()) { "rank must not be empty" }
                require(department.isNotEmpty()) { "department must not be empty" }
                require(role >= 0) { "role must be greater than or equal to 0" }
                require(password.isNotEmpty()) { "password must not be empty" }

                try {
                    DatabaseFactory.get().use { connection ->
                        connection.createStatement().use { statement ->
                            // 检查是否存在
                            if (statement.executeQuery("SELECT * FROM user WHERE eid=$eid").use { result ->
                                    result.next()
                                }) {
                                call.respondRestful(
                                    code = 1,
                                    message = "user already exists",
                                    body = null
                                )
                            } else {
                                statement.executeUpdate("INSERT INTO user (eid, name, `rank`, department, role, password) VALUES ($eid, '$name', '$rank', '$department', $role, '$password')")
                                call.respondRestful(
                                    code = 0,
                                    message = "success",
                                    body = null
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    call.respondRestful(
                        code = 1,
                        message = "fail",
                        body = null
                    )
                }
            }
        }

        delete("/user/{eid}") {
            if(call.principal<JWTPrincipal>()?.getClaim("role", Int::class) == 0) error("You don't have permission to do this")
            val eid = call.parameters["eid"]?.toInt()
            if(eid == 1) error("You can't delete the admin")

            withContext(Dispatchers.IO) {
                try {
                    DatabaseFactory.get().use { connection ->
                        connection.createStatement().use { statement ->
                            statement.executeUpdate("DELETE FROM user WHERE eid = $eid")
                            call.respondRestful(
                                code = 0,
                                message = "success",
                                body = null
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respondRestful(
                        code = 1,
                        message = "fail",
                        body = null
                    )
                }
            }
        }

        patch("/user/{eid}") {
            if(call.principal<JWTPrincipal>()?.getClaim("role", Int::class) == 0) error("You don't have permission to do this")
            val eid = call.parameters["eid"]?.toInt()

            withContext(Dispatchers.IO) {
                val postBody = call.receiveParameters()

                val name = postBody["name"]
                val rank = postBody["rank"]
                val department = postBody["department"]
                val role = postBody["role"]?.toInt()
                val password = postBody["password"]

                try {
                    DatabaseFactory.get().use { connection ->
                        connection.createStatement().use { statement ->
                            if(name != null) statement.executeUpdate("UPDATE user SET name = '$name' WHERE eid = $eid")
                            if(rank != null) statement.executeUpdate("UPDATE user SET `rank` = '$rank' WHERE eid = $eid")
                            if(department != null) statement.executeUpdate("UPDATE user SET department = '$department' WHERE eid = $eid")
                            if(role != null) statement.executeUpdate("UPDATE user SET role = $role WHERE eid = $eid")
                            if(password != null) statement.executeUpdate("UPDATE user SET password = '$password' WHERE eid = $eid")
                            call.respondRestful(
                                code = 0,
                                message = "success",
                                body = null
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respondRestful(
                        code = 1,
                        message = "fail",
                        body = null
                    )
                }
            }
        }
    }
}