package me.rerere.route

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.database.DatabaseFactory
import me.rerere.model.Salary
import me.rerere.util.respondRestful

fun Routing.salaryRoute() {
    authenticate {
        get("/salary/{eid}") {
            val eid = call.parameters["eid"]?.toIntOrNull() ?: error("eid is not number")
            withContext(Dispatchers.IO){
                try {
                    DatabaseFactory.get().use { connection ->
                        connection.createStatement().use { statement ->
                            statement.executeQuery(
                                "SELECT * FROM salary WHERE eid = $eid"
                            ).use { resultSet ->
                                val result = mutableListOf<Salary>()
                                while (resultSet.next()) {
                                    result.add(
                                        Salary(
                                            eid = resultSet.getInt("eid"),
                                            salary = resultSet.getFloat("salary"),
                                            time = resultSet.getLong("time"),
                                            base = resultSet.getFloat("salary_base"),
                                            perf = resultSet.getFloat("salary_perf"),
                                            welfare = resultSet.getFloat("salary_welfare"),
                                            special = resultSet.getFloat("salary_special"),
                                            overtime = resultSet.getFloat("salary_overtime"),
                                            subsidy = resultSet.getFloat("salary_subsidy"),
                                            debit = resultSet.getFloat("salary_debit"),
                                            description = resultSet.getString("description")
                                        )
                                    )
                                }
                                call.respondRestful(
                                    code = 0,
                                    message = "success",
                                    body = result
                                )
                            }
                        }
                    }
                }catch (e: Exception){
                    call.respondRestful(0, e.message ?: "", null)
                }
            }
        }

        post("/salary/{eid}") {
            if (call.principal<JWTPrincipal>()
                    ?.getClaim("role", Int::class) == 0
            ) error("You don't have permission to do this")

            val salary = call.receive<Salary>()

            require(salary.salary >= 0)
            require(salary.base >= 0)
            require(salary.debit >= 0)

            withContext(Dispatchers.IO) {
                try {
                    DatabaseFactory.get().use { connection ->

                        // 确保此用户存在
                        connection.createStatement().use { statement ->
                            statement.executeQuery(
                                "SELECT * FROM user WHERE eid = ${salary.eid}"
                            ).use { resultSet ->
                                if (!resultSet.next()) error("Employee not found")
                            }
                        }

                        connection.prepareStatement(
                            "INSERT INTO salary (eid, salary, time, salary_base, salary_perf, salary_welfare, salary_special, salary_overtime, salary_subsidy, salary_debit, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        ).use { stmt ->
                            stmt.setInt(1, salary.eid)
                            stmt.setFloat(2, salary.salary)
                            stmt.setLong(3, salary.time)
                            stmt.setFloat(4, salary.base)
                            stmt.setFloat(5, salary.perf)
                            stmt.setFloat(6, salary.welfare)
                            stmt.setFloat(7, salary.special)
                            stmt.setFloat(8, salary.overtime)
                            stmt.setFloat(9, salary.subsidy)
                            stmt.setFloat(10, salary.debit)
                            stmt.setString(11, salary.description)
                            stmt.executeUpdate()
                        }
                        call.respondRestful(
                            code = 0,
                            message = "success",
                            body = null
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondRestful(
                        code = 1,
                        message = "Database error",
                        body = null
                    )
                }
            }
        }
    }
}