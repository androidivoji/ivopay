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
    @SerializedName("rea") val rea: String? = null,
    @SerializedName("peo_gfd") val peoGfd: String? = null,
    @SerializedName("tpos") val tpos: List<DayOption>? = null,
    @SerializedName("dtma") val dtma: Int = 0,
    @SerializedName("dpeo") val dpeo: Int = 0,
    @SerializedName("yep") val yep: String? = null,
    @SerializedName("bio") val bio: BankInfo? = null,
    @SerializedName("itrp") val itrp: Double? = null,
    @SerializedName("dam") val dam: Long? = null
)

data class DayOption(
    @SerializedName("peo") val peo: Int = 0,
    @SerializedName("aow") val aow: Boolean = true,
    @SerializedName("dop") val dop: List<LoanOption>? = null
)

data class LoanOption(
    @SerializedName("tma") val tma: Long = 0,
    @SerializedName("sam") val sam: Long = 0,
    @SerializedName("ife") val ife: Long = 0,
    @SerializedName("dua") val dua: Long = 0
)

data class BankInfo(
    @SerializedName("bkan") val bkan: String? = null,
    @SerializedName("bkm") val bkm: String? = null,
    @SerializedName("baut") val baut: String? = null
)
