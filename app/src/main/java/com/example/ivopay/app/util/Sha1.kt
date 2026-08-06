package com.example.ivopay.app.util

import java.security.MessageDigest
import java.util.Locale

object Sha1 {
    fun getSHA1(input: String): String? {
        return try {
            val md = MessageDigest.getInstance("SHA-1")
            val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
            val sb = StringBuilder()
            for (b in bytes) {
                sb.append(String.format("%02x", b))
            }
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }
}
