package com.example.ivopay.app.data.network

import android.util.Log
import com.blankj.utilcode.util.Utils
import com.example.ivopay.app.util.SecurityUtils
import com.example.ivopay.app.util.SessionManager
import com.example.ivopay.app.util.SystemBridge
import com.google.gson.Gson
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
            // Hanya enkripsi jika body adalah JSON (atau kosong untuk mgea)
            if (originalBody == null || (originalBody.contentType()?.subtype?.contains("json") == true)) {
                
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
                                // Logika navigasi ke logout bisa diletakkan di ViewModel atau UI layer
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
