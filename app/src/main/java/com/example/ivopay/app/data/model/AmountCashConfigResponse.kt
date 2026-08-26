package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class AmountCashConfigResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: AmountCashConfigData? = null
)

data class AmountCashConfigData(
    @SerializedName("tpos") val tpos: List<AmountTimeOption>? = null,
    @SerializedName("dtma") val dtma: Int = 0,
    @SerializedName("dpeo") val dpeo: Int = 0,
    @SerializedName("m_dtma") val mDtma: Int = 0,
    @SerializedName("m_dpeo") val mDpeo: Int = 0,
    @SerializedName("itma") val itma: Long = 0,
    @SerializedName("atma") val atma: Long = 0,
    @SerializedName("bio") val bio: AmountBankInfo? = null,
    @SerializedName("uoe") val uoe: Int = 0,
    @SerializedName("yep") val yep: String? = null
)

data class AmountTimeOption(
    @SerializedName("peo") val peo: Int = 0,
    @SerializedName("aow") val aow: Boolean = false,
    @SerializedName("peo_gfd") val peoGfd: String? = null,
    @SerializedName("dop") val dop: List<AmountLoanOption>? = null,
    @SerializedName("yep") val yep: String? = null
)

data class AmountLoanOption(
    @SerializedName("tma") val tma: Long = 0,
    @SerializedName("aow") val aow: Boolean = false,
    @SerializedName("itpr") val itpr: String? = null,
    @SerializedName("pdia") val pdia: Long = 0,
    @SerializedName("ife") val ife: Long = 0,
    @SerializedName("sam") val sam: Long = 0,
    @SerializedName("dam") val dam: Long = 0,
    @SerializedName("dua") val dua: Long = 0,
    @SerializedName("afm") val afm: Long = 0,
    @SerializedName("tfe") val tfe: Long = 0
)

data class AmountBankInfo(
    @SerializedName("bkm") val bkm: String? = null,
    @SerializedName("bkan") val bkan: String? = null,
    @SerializedName("baut") val baut: String? = null
)
