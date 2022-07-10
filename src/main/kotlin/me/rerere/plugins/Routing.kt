package me.rerere.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.rerere.database.DatabaseFactory
import me.rerere.model.Salary
import me.rerere.model.Stats
import me.rerere.route.salaryRoute
import me.rerere.route.userRoute
import me.rerere.util.Response
import me.rerere.util.respondRestful

fun Application.configureRouting() {
    val secret = environment.config.property("jwt.secret").getString()
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        get("/") {
            call.respond(
                Response(
                    code = 0,
                    message = "工资管理系统后端服务",
                    body = null
                )
            )
        }

        get("/stats") {
            withContext(Dispatchers.IO) {
                try {
                    DatabaseFactory.get().use { connection ->
                        // The timestamp of the start of this month
                        val startOfMonth =
                            System.currentTimeMillis() - (System.currentTimeMillis() % (1000L * 60 * 60 * 24 * 30))

                        // 查询本月发放的工资总额
                        val salaryMonth = connection.prepareStatement(
                            "SELECT SUM(salary) FROM salary WHERE time >= $startOfMonth"
                        ).executeQuery().use {
                            if(it.next()) {
                                it.getFloat("SUM(salary)")
                            } else {
                                0.0f
                            }
                        }

                        // 查询员工数量
                        val userCount = connection.prepareStatement(
                            "SELECT COUNT(*) FROM user"
                        ).executeQuery().use {
                            if(it.next()) {
                                it.getInt("COUNT(*)")
                            } else {
                                0
                            }
                        }

                        // 查询本月工资记录
                        val record = mutableListOf<Salary>()
                        connection.prepareStatement(
                            "SELECT * FROM salary WHERE time >= $startOfMonth"
                        ).executeQuery().use { resultSet ->
                            while(resultSet.next()) {
                                record.add(
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
                        }

                        call.respondRestful(
                            code = 0,
                            message = "success",
                            body = Stats(
                                salaryMonth = salaryMonth,
                                users = userCount,
                                records = record
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondRestful(
                        code = 1,
                        message = e.javaClass.name,
                        body = null
                    )
                }
            }
        }

        post("/login") {
            val param = call.receiveParameters()
            val eid = param["eid"]
            val password = param["password"]

            withContext(Dispatchers.IO) {
                DatabaseFactory.get().use { connection ->
                    connection.prepareStatement("SELECT * FROM user WHERE eid = ?").use { preparedStatement ->
                        preparedStatement.setString(1, eid)
                        preparedStatement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                val dbPassword = resultSet.getString("password")
                                if (dbPassword == password) {
                                    call.respondRestful(
                                        code = 0,
                                        message = "登录成功",
                                        body = JWT.create()
                                            .withClaim("eid", eid)
                                            .withClaim("role", resultSet.getString("role"))
                                            .withClaim("name", resultSet.getString("name"))
                                            .sign(Algorithm.HMAC256(secret))
                                    )
                                } else {
                                    call.respondRestful(
                                        code = 2,
                                        message = "密码错误",
                                        body = null
                                    )
                                }
                            } else {
                                call.respondRestful(
                                    code = 1,
                                    message = "用户不存在",
                                    body = null
                                )
                            }
                        }
                    }
                }
            }
        }

        salaryRoute()
        userRoute()
    }
}
