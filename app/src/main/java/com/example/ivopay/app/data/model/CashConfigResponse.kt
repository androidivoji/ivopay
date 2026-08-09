package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class CashConfigResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: CashConfigData? = null
)

data class CashConfigData(
    @SerializedName("itma") val itma: Long = 0,
    @SerializedName("atma") val atma: Long = 0,
    @SerializedName("nar") val nar: Long = 0,
    @SerializedName("peo") val peo: Int = 0,
    @SerializedName("koc") val koc: Boolean = false,
    @SerializedName("rea") val rea: String? = null
)
