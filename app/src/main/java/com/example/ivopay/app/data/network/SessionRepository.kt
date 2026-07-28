package com.example.ivopay.app.data.network

import retrofit2.http.Body
import retrofit2.http.POST

data class PageStartRequest(
    val spe: String = "h",
    val tkn: String,
    val session_id: String,
    val page_name: String
)

data class PageEndRequest(
    val spe: String = "h",
    val tkn: String,
    val session_id: String,
    val page_name: String,
    val page_stay_seconds: Int
)

interface SessionApiService {
    @POST("session/pageStart") // Sesuaikan endpoint urls.session.PAGE_START
    suspend fun sendPageStart(@Body request: PageStartRequest)

    @POST("session/pageEnd")   // Sesuaikan endpoint urls.session.PAGE_END
    suspend fun sendPageEnd(@Body request: PageEndRequest)
}