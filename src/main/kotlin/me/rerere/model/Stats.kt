package me.rerere.model

data class Stats(
    val salaryMonth: Float, // 本月总共发放了多少工资
    val users: Int, // 总共有多少用户
    val records: List<Salary> // 本月工资发放记录
)