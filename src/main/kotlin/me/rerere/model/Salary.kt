package me.rerere.model

import java.util.Date

data class Salary(
    val eid: Int,
    val salary: Float,
    val time: Long,
    val base: Float,
    val perf: Float,
    val welfare: Float,
    val special: Float,
    val overtime: Float,
    val subsidy: Float,
    val debit: Float,
    val description: String
)