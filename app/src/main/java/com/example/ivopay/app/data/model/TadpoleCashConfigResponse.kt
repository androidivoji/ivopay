package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class TadpoleCashConfigResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: TadpoleCashConfigData? = null
)

data class TadpoleCashConfigData(
    @SerializedName("tpos") val tpos: List<TadpoleTimeOption>? = null,
    @SerializedName("dtma") val dtma: Int = 0,
    @SerializedName("dpeo") val dpeo: Int = 0,
    @SerializedName("m_dtma") val mDtma: Int = 0,
    @SerializedName("m_dpeo") val mDpeo: Int = 0,
    @SerializedName("itma") val itma: Long = 0,
    @SerializedName("atma") val atma: Long = 0,
    @SerializedName("bio") val bio: TadpoleBankInfo? = null,
    @SerializedName("uoe") val uoe: Int = 0,
    @SerializedName("yep") val yep: String? = null,
    @SerializedName("mob") val mob: String? = null,
    @SerializedName("nvmp") val nvmp: Boolean = false
)

data class TadpoleTimeOption(
    @SerializedName("peo") val peo: Int = 0,
    @SerializedName("swo") val swo: String? = null,
    @SerializedName("aow") val aow: Boolean = false,
    @SerializedName("dop") val dop: List<TadpoleLoanOption>? = null,
    @SerializedName("yep") val yep: String? = null,
    @SerializedName("bpio") val bpio: Int = 0
)

data class TadpoleLoanOption(
    @SerializedName("tma") val tma: Long = 0,
    @SerializedName("aow") val aow: Boolean = false,
    @SerializedName("itpr") val itpr: String? = null,
    @SerializedName("pdia") val pdia: Long = 0,
    @SerializedName("ife") val ife: Long = 0,
    @SerializedName("sam") val sam: Long = 0,
    @SerializedName("dam") val dam: Long = 0,
    @SerializedName("dua") val dua: Long = 0,
    @SerializedName("peo") val peo: Int = 0
)

data class TadpoleBankInfo(
    @SerializedName("bkm") val bkm: String? = null,
    @SerializedName("bkan") val bkan: String? = null,
    @SerializedName("baut") val baut: String? = null
)
