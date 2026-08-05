package com.example.ivopay.app.util

import java.security.MessageDigest

object Sha1 {
    fun getSHA1(input: String): String? {
        return try {
            val md = MessageDigest.getInstance("SHA-1")
            val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }
}
