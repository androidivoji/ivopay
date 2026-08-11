package com.example.ivopay.app.data.network

import android.util.Log
import com.blankj.utilcode.util.Utils
import com.example.ivopay.app.util.*
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Interceptor untuk menangani enkripsi request (rd) dan dekripsi response (rf)
 * sesuai dengan logika di project Vue (axios interceptors).
 */
class CryptoInterceptor : Interceptor {
    private val gson = Gson()
    private val context = Utils.getApp()
    private val sessionManager = SessionManager(context)
    private val systemBridge = SystemBridge(context)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // 1. Request Interceptor Logic
        if (request.method == "POST") {
            val originalBody = request.body
            
            if (originalBody is MultipartBody) {
                // Penanganan Multipart (Request dengan file)
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                val combineData = mutableMapOf<String, Any>()
                val filePartsData = mutableMapOf<String, String>() // Untuk ig_i5f calculation
                
                // Pisahkan data teks (untuk rd) dan data file
                originalBody.parts.forEach { part ->
                    val header = part.headers
                    val contentDisposition = header?.get("Content-Disposition")
                    
                    if (contentDisposition != null) {
                        val nameMatch = "name=\"([^\"]+)\"".toRegex().find(contentDisposition)
                        val name = nameMatch?.groupValues?.get(1) ?: ""
                        
                        if (!contentDisposition.contains("filename=")) {
                            // Text part
                            try {
                                val buffer = okio.Buffer()
                                part.body.writeTo(buffer)
                                combineData[name] = buffer.readUtf8()
                            } catch (e: Exception) {}
                        } else {
                            // File part
                            builder.addPart(part)
                            try {
                                val buffer = okio.Buffer()
                                part.body.writeTo(buffer)
                                val bytes = buffer.readByteArray()
                                
                                // Backend Parity: Backend menghitung base64_encode(binary_content)
                                // Jadi kita ubah binary ke Base64 murni (Standard, NO_WRAP) untuk hashing
                                val base64ForHash = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                filePartsData[name] = base64ForHash
                                Log.e("IG5F_DEBUG", "File Part: $name, Base64 Length for Hash: ${base64ForHash.length}")
                            } catch (e: Exception) {
                                Log.e("IG5F_DEBUG", "Error reading file bytes", e)
                            }
                        }
                    }
                }

                // Calculate ig_i5f sesuai logic: SHA1(MD5(concat(base64Content)))
                if (filePartsData.isNotEmpty()) {
                    val sortedKeys = filePartsData.keys.sorted()
                    val baseStr = StringBuilder()
                    for (key in sortedKeys) {
                        baseStr.append(filePartsData[key])
                    }
                    
                    val contentToHash = baseStr.toString()
                    val md5 = com.example.ivopay.app.util.Sha1.getMD5(contentToHash)
                    if (md5 != null) {
                        val ig5f = com.example.ivopay.app.util.Sha1.getSHA1(md5)?.lowercase()
                        if (ig5f != null) {
                            combineData["ig_i5f"] = ig5f
                        }
                    }
                }
                
                // Tambahkan common parameters
                combineData.putAll(systemBridge.getCommonParams())
                sessionManager.getAuthToken()?.let { combineData["tkn"] = it }
                
                // Signature
                val cleanUrl = request.url.newBuilder().query(null).build().toString()
                val signature = SecurityUtils.generateSign(request.method, cleanUrl, combineData)
                if (signature != null) combineData["sign"] = signature
                
                // Enkripsi ke "rd"
                val encryptedData = SecurityUtils.encryptParams(gson.toJson(combineData))
                if (encryptedData != null) {
                    builder.addFormDataPart("rd", encryptedData)
                    request = request.newBuilder()
                        .post(builder.build())
                        .build()
                }
            } else if (originalBody == null || (originalBody.contentType()?.subtype?.contains("json") == true)) {
                
                val combineData = mutableMapOf<String, Any>()
                
                // Ambil data original jika ada
                if (originalBody != null) {
                    try {
                        val buffer = okio.Buffer()
                        originalBody.writeTo(buffer)
                        val originalJson = buffer.readUtf8()
                        if (originalJson.isNotEmpty()) {
                            val map: Map<String, Any> = gson.fromJson(originalJson, object : TypeToken<Map<String, Any>>() {}.type)
                            combineData.putAll(map)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Tambahkan common parameters
                combineData.putAll(systemBridge.getCommonParams())
                
                // Tambahkan token jika sudah login
                sessionManager.getAuthToken()?.let {
                    combineData["tkn"] = it
                }

                // Tambahkan Signature sesuai logika Vue
                val cleanUrl = request.url.newBuilder().query(null).build().toString()
                val signature = SecurityUtils.generateSign(request.method, cleanUrl, combineData)
                if (signature != null) {
                    combineData["sign"] = signature
                }

                // Enkripsi seluruh package data
                val encryptedData = SecurityUtils.encryptParams(gson.toJson(combineData))
                
                if (encryptedData != null) {
                    // Rebuild body sebagai FormData dengan field "rd" sesuai server requirement ("miss rd" fix)
                    val multipartBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("rd", encryptedData)
                        .build()
                    
                    request = request.newBuilder()
                        .post(multipartBody)
                        .build()
                }
            }
        } else if (request.method == "GET") {
            val urlBuilder = request.url.newBuilder()
            val commonParams = systemBridge.getCommonParams()
            val combineParams = mutableMapOf<String, String>()
            
            // Collect existing query params
            for (i in 0 until request.url.querySize) {
                combineParams[request.url.queryParameterName(i)] = request.url.queryParameterValue(i) ?: ""
            }
            
            // Add common params to URL and combineParams map for signing
            commonParams.forEach { (k, v) ->
                if (!combineParams.containsKey(k)) {
                    urlBuilder.addQueryParameter(k, v)
                    combineParams[k] = v
                }
            }
            
            // Add token
            sessionManager.getAuthToken()?.let {
                if (!combineParams.containsKey("tkn")) {
                    urlBuilder.addQueryParameter("tkn", it)
                    combineParams["tkn"] = it
                }
            }
            
            // Add Sign sesuai logika Vue
            val cleanUrl = request.url.newBuilder().query(null).build().toString()
            val anyParams = combineParams.mapValues { it.value as Any }.toMutableMap()
            val signature = SecurityUtils.generateSign(request.method, cleanUrl, anyParams)
            if (signature != null) {
                urlBuilder.addQueryParameter("sign", signature)
            }
            
            request = request.newBuilder().url(urlBuilder.build()).build()
        }

        // 2. Kirim Request
        Log.d("XBZ", "--> ${request.method} ${request.url}")
        val response = chain.proceed(request)

        // 3. Response Interceptor Logic (Decryption rf)
        val responseBody = response.body
        if (response.isSuccessful && responseBody != null) {
            val responseString = responseBody.string()
            
            try {
                val jsonResponse = gson.fromJson(responseString, JsonObject::class.java)
                if (jsonResponse != null && jsonResponse.has("rf")) {
                    val rf = jsonResponse.get("rf").asString
                    val decryptedData = SecurityUtils.decryptParams(rf)
                    
                    if (!decryptedData.isNullOrEmpty()) {
                        // Cek status code (misal code 5 untuk logout)
                        try {
                            val decryptedJson = gson.fromJson(decryptedData, JsonObject::class.java)
                            if (decryptedJson.has("code") && decryptedJson.get("code").asInt == 5) {
                                sessionManager.clearSession()
                                // Kirim event global untuk ditangani di UI
                                kotlinx.coroutines.GlobalScope.launch {
                                    GlobalEvent.sendEvent(GlobalEvent.Event.TokenError)
                                }
                            }
                        } catch (e: Exception) {}
                        
                        // Kembalikan response body yang sudah di-dekripsi agar bisa di-parse oleh Retrofit
                        val contentType = "application/json; charset=utf-8".toMediaTypeOrNull()
                        val newBody = decryptedData.toResponseBody(contentType)
                        return response.newBuilder()
                            .body(newBody)
                            .build()
                    }
                } else {
                    // Jika tidak ada rf, kembalikan body asli (penting karena kita sudah string() di atas)
                    val newBody = responseString.toResponseBody(responseBody.contentType())
                    return response.newBuilder().body(newBody).build()
                }
            } catch (e: Exception) {
                // Jika parsing gagal, kembalikan body asli
                val newBody = responseString.toResponseBody(responseBody.contentType())
                return response.newBuilder().body(newBody).build()
            }
        }

        return response
    }
}
