package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class TadpoleBillPreviewResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: TadpoleBillPreviewData? = null
)

data class TadpoleBillPreviewData(
    @SerializedName("ewb") val ewb: List<TadpoleBillItem>? = null
)

data class TadpoleBillItem(
    @SerializedName("bpio") val bpio: Int = 0,
    @SerializedName("rdn") val rdn: String? = null,
    @SerializedName("bpeo") val bpeo: Int = 0,
    @SerializedName("btma") val btma: Long = 0,
    @SerializedName("bife") val bife: Long = 0,
    @SerializedName("bltf") val bltf: Long = 0,
    @SerializedName("dta") val dta: Long = 0,
    @SerializedName("dtag") val dtag: Long = 0,
    @SerializedName("dtap") val dtap: String? = null,
    @SerializedName("otma") val otma: Long = 0,
    @SerializedName("ptma") val ptma: Long = 0,
    @SerializedName("brps") val brps: Int = 0,
    @SerializedName("brpd") val brpd: String? = null,
    @SerializedName("byep") val byep: Int = 0
)
