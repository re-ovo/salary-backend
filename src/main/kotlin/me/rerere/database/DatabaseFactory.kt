package me.rerere.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object DatabaseFactory {
    private lateinit var hikariDataSource: HikariDataSource

    fun init() {
        val hikariConfig = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = "jdbc:mysql://localhost:3306/salary"
            username = "root"
            password = "root"
            maximumPoolSize = 20
        }
        hikariDataSource = HikariDataSource(hikariConfig)

        this.initTables()
    }

    private fun initTables() {
        this.get().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE IF NOT EXISTS user (id INT NOT NULL AUTO_INCREMENT, eid INT NOT NULL, name VARCHAR(255) NOT NULL, rank VARCHAR(16), department VARCHAR(32), role INT(1), password varchar(255), PRIMARY KEY (id))")
                statement.execute("CREATE TABLE IF NOT EXISTS salary (id INT NOT NULL AUTO_INCREMENT, eid INT NOT NULL, salary FLOAT, time BIGINT, salary_base FLOAT, salary_perf FLOAT , salary_welfare FLOAT ,salary_special FLOAT , salary_overtime FLOAT ,salary_subsidy FLOAT ,salary_debit FLOAT , description VARCHAR (1024), PRIMARY KEY (id))")

                statement.executeQuery("SELECT * FROM user").use { resultSet ->
                    if(!resultSet.next()) {
                        statement.execute("INSERT INTO user (eid, name, rank, department, role, password) VALUES (1, 'admin', '管理员', '财务部门', 1, 'admin')")
                    }
                }
            }
        }
    }

    fun get(): Connection = hikariDataSource.connection
}