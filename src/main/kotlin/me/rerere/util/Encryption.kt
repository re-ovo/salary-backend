package me.rerere.util

fun md5(text: String): String {
    val md = java.security.MessageDigest.getInstance("MD5")
    return String(md.digest(text.toByteArray()))
}