package com.example.ivopay.app.util

import android.util.Log

object SecurityUtils {

    /**
     * Meniru logika addSign dari project Vue:
     * 1. srcString = METHOD + URL
     * 2. Sort keys
     * 3. Append key + trimmedValue ke srcString
     * 4. Append salt
     * 5. SHA1(srcString).toUpperCase()
     */
    fun generateSign(method: String, url: String, data: MutableMap<String, Any>): String? {
        val salt = "bA7R7324zJy@loVL"
        
        val cleanUrl = if (url.endsWith("/")) url.substring(0, url.length - 1) else url
        val keys = data.keys.toMutableList().sorted()
        
        val sb = StringBuilder()
        sb.append(method.uppercase())
        sb.append(cleanUrl)
        
        for (key in keys) {
            val value = data[key]
            if (key == "sign") continue
            
            if (value == null || value.toString().isEmpty()) {
                data.remove(key)
            } else {
                val trimmedValue = value.toString().trim()
                data[key] = trimmedValue
                sb.append(key).append(trimmedValue)
            }
        }
        
        sb.append(salt)
        val verifyStr = sb.toString()
        
        return Sha1.getSHA1(verifyStr)?.uppercase()
    }

    /**
     * Berfungsi melakukan hashing/signing parameter request menggunakan key di C++ (JNI)
     * Diupdate agar menggunakan SHA1 sesuai dengan logika project Vue
     */
    fun signBySalty(params: String, channel: String): String? {
        return try {
            val salt = "bA7R7324zJy@loVL"
            val input = params + salt
            val result = Sha1.getSHA1(input)?.uppercase()
            Log.d("SECURITY_DEBUG", "signBySalty input: $input")
            Log.d("SECURITY_DEBUG", "signBySalty result: $result")
            result
        } catch (e: Exception) {
            null
        }
    }

    fun encryptParams(params: String): String? {
        return MyEncryptUtil.encryptStr(params)
    }

    fun decryptParams(params: String): String? {
        return MyEncryptUtil.decryptStr(params)
    }

    /**
     * Meniru logika encodeGesture dari project Vue
     */
    fun encodeGesture(data: String): String {
        val salt = "bA7R7324zJy@loVL"
        return if (ChannelUtils.isTestEnv) {
            Sha1.getSHA1(data + salt)?.uppercase() ?: ""
        } else {
            // Production Mode: signBySalty(data)
            signBySalty(data, ChannelUtils.appChannel) ?: ""
        }
    }
}
