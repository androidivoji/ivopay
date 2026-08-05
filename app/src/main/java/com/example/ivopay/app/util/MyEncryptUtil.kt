package com.example.ivopay.app.util

import android.util.Base64
import android.util.Log
import com.blankj.utilcode.util.EncodeUtils
import org.json.JSONObject
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.Throws

object MyEncryptUtil {
    private const val CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val SECRET_KEY_LENGTH = 32

    private val CHARSET_UTF8 = StandardCharsets.UTF_8
    private const val KEY_ALGORITHM = "AES"
    private const val DEFAULT_VALUE = "0"


    @Throws(java.lang.Exception::class)
    fun encryptStr(sSrc: String): String {
        // Format JSON untuk logging
        val formattedJson = try {
            val json = JSONObject(sSrc)
            json.toString(4) // Indent dengan 4 spasi
        } catch (e: java.lang.Exception) {
            sSrc // Jika bukan JSON valid, gunakan string asli
        }
        Log.d("XBZ", "encryptStr\n$formattedJson")
        // Menggunakan salt statis sesuai permintaan user untuk menggantikan JNI
        val staticSalt = "bA7R7324zJy@loVL"
        val str = Sha256.getSHA256(staticSalt)
        val vi = str!!.substring(str.length - 16, str.length)
        val key = str.substring(0, 32)
        val raw = key.toByteArray(charset("utf-8"))
        val skeySpec = SecretKeySpec(raw, CIPHER_ALGORITHM)
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val ivParameterSpec = IvParameterSpec(vi.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec)
        val encrypted = cipher.doFinal(sSrc.toByteArray(CHARSET_UTF8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    // Versi alternatif dengan lebih banyak detail debugging
    fun decryptStr(base64Data: String?): String? {
        try {
            Log.d("XBZ", "╔═══════════════════════════════════════════════")
            Log.d("XBZ", "║ DECRYPT PROCESS STARTED")
            Log.d("XBZ", "╠═══════════════════════════════════════════════")
            Log.d("XBZ", "║ Input base64 length: ${base64Data?.length ?: 0}")

            if (base64Data.isNullOrEmpty()) {
                Log.w("XBZ", "║ WARNING: base64Data is null or empty")
                return null
            }

            // Tampilkan preview base64 (50 karakter pertama)
            val preview = if (base64Data.length > 50)
                "${base64Data.substring(0, 50)}..."
            else base64Data
            Log.d("XBZ", "║ Base64 preview: $preview")

            val data = EncodeUtils.base64Decode(base64Data)
            Log.d("XBZ", "║ Decoded data length: ${data.size} bytes")

            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            val staticSalt = "bA7R7324zJy@loVL"
            val str = Sha256.getSHA256(staticSalt)
            val vi = str!!.substring(str.length - 16, str.length)
            val key = str.substring(0, 32)

            Log.d("XBZ", "║ Derived Key (first 16 chars): ${key.substring(0, 16)}...")
            Log.d("XBZ", "║ strKey: $str")
            Log.d("XBZ", "║ IV: $vi")

            val ivParameterSpec = IvParameterSpec(vi.toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), ivParameterSpec)

            val result = cipher.doFinal(data)
            val decryptedString = String(result, CHARSET_UTF8)

            Log.d("XBZ", "║ Formatted JSON Result:")

            val dekriptStr = getFormattedJson(decryptedString)
            Log.d("XBZ", "dekriptStr\n$dekriptStr")

            return decryptedString

        } catch (e: java.lang.Exception) {
            Log.e("XBZ", "╔═══════════════════════════════════════════════")
            Log.e("XBZ", "║ DECRYPTION FAILED")
            Log.e("XBZ", "║ Error: ${e.javaClass.simpleName}")
            Log.e("XBZ", "║ Message: ${e.message}")
            Log.e("XBZ", "╚═══════════════════════════════════════════════")
            handleException(e)
        }
        return null
    }

    // Fungsi helper untuk mendapatkan formatted JSON string
    fun getFormattedJson(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            json.toString(4)
        } catch (e: java.lang.Exception) {
            // Return dengan indentasi manual jika bukan JSON
            "Not a valid JSON:\n$jsonString"
        }
    }

    private fun handleException(e: java.lang.Exception) {
        e.printStackTrace()
    }

    private fun getSecretKey(secretKey: String): SecretKeySpec {
        var secretKey = secretKey
        secretKey = toMakeKey(secretKey, SECRET_KEY_LENGTH, DEFAULT_VALUE)
        return SecretKeySpec(secretKey.toByteArray(CHARSET_UTF8), KEY_ALGORITHM)
    }

    private fun toMakeKey(secretKey: String, length: Int, text: String): String {
        var secretKey = secretKey
        val strLen = secretKey.length
        if (strLen < length) {
            val builder = StringBuilder()
            builder.append(secretKey)
            for (i in 0 until length - strLen) {
                builder.append(text)
            }
            secretKey = builder.toString()
        }
        return secretKey
    }
}