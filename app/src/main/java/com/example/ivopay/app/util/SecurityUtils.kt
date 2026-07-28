package com.example.ivopay.app.util

object SecurityUtils {

    // Ganti dari: signBySalty(params)
    // Berfungsi melakukan hashing/signing parameter request menggunakan key di C++ (JNI)
    fun signBySalty(params: String, channel: String): String? {
        return try {
            JniFuncs.getSaltySign(params, channel)
        } catch (e: Exception) {
            null
        }
    }

    // Ganti dari: encryptInputParams(params)
    fun encryptParams(params: String): String? {
        return MyEncryptUtil.encryptStr(params)
    }

    // Ganti dari: decryptOutputParams(params)
    fun decryptParams(params: String): String? {
        return MyEncryptUtil.decryptStr(params)
    }
}