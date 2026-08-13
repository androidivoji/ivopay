package com.example.ivopay.app.data.api

import com.example.ivopay.app.data.model.BankListResponse
import com.example.ivopay.app.data.model.BorrowerContractsResponse
import com.example.ivopay.app.data.model.BorrowerListResponse
import com.example.ivopay.app.data.model.CommonConfigResponse
import com.example.ivopay.app.data.model.LenderUserInfoResponse
import com.example.ivopay.app.data.model.LoginResponse
import com.example.ivopay.app.data.model.LoginWayResponse
import com.example.ivopay.app.data.model.MgeaResponse
import com.example.ivopay.app.data.model.RoleResponse
import com.example.ivopay.app.data.model.UserInfoResponse
import com.example.ivopay.app.data.network.CryptoInterceptor
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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

    @POST("v1/api/lg/sc")
    suspend fun sendVerCode(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/lg/v")
    suspend fun verifyLogin(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/lg/gen")
    suspend fun gestureLogin(
        @Body requestBody: JsonObject
    ): Response<LoginResponse>

    @POST("v1/api/geup")
    suspend fun setGesturePwd(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v2/api/gast")
    suspend fun getRole(
        @Body requestBody: JsonObject
    ): Response<RoleResponse>

    @POST("v1/api/c/pa")
    suspend fun getCommonConfig(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<CommonConfigResponse>

    @POST("v2/api/c/b")
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

    @POST("v1/api/gol")
    suspend fun getBorrowerOrderList(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/bsal")
    suspend fun batchSignAllContracts(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/bnpt")
    suspend fun getPlatformContract(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<ResponseBody>

    @POST("v1/api/pgod")
    suspend fun getOrderDetail(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/glbc")
    suspend fun getBorrowerContract(
        @Body requestBody: JsonObject
    ): Response<ResponseBody>

    @POST("v1/api/sgbr")
    suspend fun signLenderAndBorrower(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/sipt")
    suspend fun signLenderAndPlatform(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/glsl")
    suspend fun getBorrowerContractList(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v1/api/aict")
    suspend fun getBorrowerLoanList(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v2/api/mgea")
    suspend fun postMgeaBorrower(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v2/api/laey")
    suspend fun getHomeCashConfig(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v2/api/c/gc")
    suspend fun getUserInfo(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<UserInfoResponse>

    @POST("v2/api/acnt")
    suspend fun getTadpoleHomeData(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v1/api/oaict")
    suspend fun getOldBillList(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v1/api/gbsc")
    suspend fun getBorrowerContracts(
        @Body requestBody: JsonObject
    ): Response<BorrowerContractsResponse>

    @POST("v1/api/gbss")
    suspend fun getBorrowerContractsStatus(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/gsbc")
    suspend fun borrowerSendCode(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v1/api/gscc")
    suspend fun borrowerCheckCode(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/gsbw")
    suspend fun borrowerSign(
        @Body requestBody: okhttp3.RequestBody
    ): Response<JsonObject>

    @POST("api/pyc")
    suspend fun getPayCodeWays(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("api/dk/pay")
    suspend fun getDynamicPayCode(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v2/api/ado")
    suspend fun lackinApply(
        @Body requestBody: JsonObject = JsonObject()
    ): Response<JsonObject>

    @POST("v1/api/lcea")
    suspend fun getAmountCashConfig(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/yatc")
    suspend fun applyLoan(
        @Body requestBody: okhttp3.RequestBody
    ): Response<JsonObject>

    // OCR & Base Info
    @POST("api/soime")
    suspend fun uploadOcrPhoto(
        @Body requestBody: okhttp3.RequestBody
    ): Response<JsonObject>

    @POST("api/sonfo")
    suspend fun getOcrResult(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/c/pcd")
    suspend fun getAddressList(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/c/up")
    suspend fun updateBaseInfo(
        @Body requestBody: okhttp3.RequestBody
    ): Response<JsonObject>

    @POST("v1/api/c/up")
    suspend fun updateUserInfo(
        @Body requestBody: JsonObject
    ): Response<JsonObject>

    @POST("v1/api/pa/e")
    suspend fun uploadEvent(
        @Body requestBody: JsonObject
    ): Response<JsonObject>
}

object NetworkClient {
    private const val BASE_URL_DEVEL = "https://devapi.ivoji.id/"
    private const val BASE_URL_1 = "https://appv.ivoji.id/index.html/"
    private const val BASE_URL = "https://backend.ivoji.id/"

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
            .baseUrl(BASE_URL_DEVEL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
