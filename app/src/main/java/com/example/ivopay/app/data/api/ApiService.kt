package com.example.ivopay.app.data.api

import com.example.ivopay.app.data.model.BankListResponse
import com.example.ivopay.app.data.model.BorrowerListResponse
import com.example.ivopay.app.data.model.CommonConfigResponse
import com.example.ivopay.app.data.model.LenderUserInfoResponse
import com.example.ivopay.app.data.model.LoginResponse
import com.example.ivopay.app.data.model.LoginWayResponse
import com.example.ivopay.app.data.model.MgeaResponse
import com.example.ivopay.app.data.model.RoleResponse
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

    @POST("v1/api/cugo")
    suspend fun getLenderUserInfo(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<LenderUserInfoResponse>

    @POST("v2/api/lg/m")
    suspend fun getLoginWay(
        @Body requestBody: JsonObject
    ): Response<LoginWayResponse>

    @POST("v1/api/lg/gen")
    suspend fun gestureLogin(
        @Body requestBody: JsonObject
    ): Response<LoginResponse>

    @POST("v2/api/gast")
    suspend fun getRole(
        @Body requestBody: JsonObject
    ): Response<RoleResponse>

    @POST("v1/api/c/pa")
    suspend fun getCommonConfig(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<CommonConfigResponse>

    @POST("v1/api/c/b")
    suspend fun getBankList(
        @Body requestBody: JsonObject
    ): Response<BankListResponse>

    @POST("v1/api/cubd")
    suspend fun updateLenderUserInfo(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/sai")
    suspend fun getBorrowerList(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<BorrowerListResponse>

    @POST("v1/api/brde")
    suspend fun getBorrowerDetail(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/cfo")
    suspend fun confirmPayBack(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/ado")
    suspend fun createOrder(
        @Body requestBody: JsonObject
    ): Response<JsonObject>
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
