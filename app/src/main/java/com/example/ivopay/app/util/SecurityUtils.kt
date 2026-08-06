package com.example.ivopay.app.util

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
        
        // Pastikan URL tidak berakhir dengan slash untuk konsistensi dengan verifyStr server
        val cleanUrl = if (url.endsWith("/")) url.substring(0, url.length - 1) else url
        
        val keys = data.keys.toMutableList().sorted()
        
        val sb = StringBuilder()
        sb.append(method.uppercase())
        sb.append(cleanUrl)
        
        for (key in keys) {
            val value = data[key]
            if (key == "sign") continue // Jangan masukkan diri sendiri
            
            if (value == null || value.toString().isEmpty()) {
                data.remove(key) // Hapus dari data agar tidak dikirim ke server (sesuai Vue)
            } else {
                val trimmedValue = value.toString().trim()
                data[key] = trimmedValue
                sb.append(key).append(trimmedValue)
            }
        }
        
        sb.append(salt)
        val verifyStr = sb.toString()
        android.util.Log.d("SIGN_TEST", "verifyStr: $verifyStr")
        
        val sign = Sha1.getSHA1(verifyStr)?.uppercase()
        android.util.Log.d("SIGN_TEST", "generated sign: $sign")
        return sign
    }

    // Ganti dari: signBySalty(params)
    // Berfungsi melakukan hashing/signing parameter request menggunakan key di C++ (JNI)
    // Diupdate agar menggunakan SHA1 sesuai dengan logika project Vue
    fun signBySalty(params: String, channel: String): String? {
        return try {
            // Logika signing manual: SHA1(params + salt)
            // Sesuai dengan Vue: sha1(data + mock.TEST_st).toUpperCase()
            val salt = "bA7R7324zJy@loVL"
            Sha1.getSHA1(params + salt)?.uppercase()
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

    /**
     * Meniru logika encodeGesture dari project Vue
     */
    fun encodeGesture(data: String): String {
        return if (ChannelUtils.isTestEnv) {
            // Test Mode: sha1(data + salt).toUpperCase()
            val salt = "bA7R7324zJy@loVL"
            Sha1.getSHA1(data + salt)?.uppercase() ?: ""
        } else {
            // Production Mode: signBySalty(data)
            signBySalty(data, ChannelUtils.appChannel) ?: ""
        }
    }
}
