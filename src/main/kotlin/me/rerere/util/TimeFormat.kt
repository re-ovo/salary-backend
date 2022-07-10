package me.rerere.util

import java.text.SimpleDateFormat
import java.util.*

private val formatter_month = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

fun Date.format(): String = formatter.format(this)

fun Date.formatMonth(): String = formatter_month.format(this)