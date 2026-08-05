package com.example.ivopay.app.data.api

import com.example.ivopay.app.data.model.LenderUserInfoResponse
import com.example.ivopay.app.data.model.LoginWayResponse
import com.example.ivopay.app.data.model.MgeaResponse
import com.example.ivopay.app.data.network.CryptoInterceptor
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("v2/api/mgea")
    suspend fun postMgea(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<MgeaResponse>

    @POST("v2/api/cugo")
    suspend fun getLenderUserInfo(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<LenderUserInfoResponse>

    @POST("v2/api/lg/m")
    suspend fun getLoginWay(
        @Body requestBody: JsonObject
    ): Response<LoginWayResponse>
}

object NetworkClient {
    private const val BASE_URL = "https://devapi.ivoji.id/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cryptoInterceptor = CryptoInterceptor()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(cryptoInterceptor) // Enkripsi/Dekripsi dilakukan sebelum logging agar data asli terlihat di log (atau sesuaikan urutan)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
