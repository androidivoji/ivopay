package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class OtherProductResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: List<OtherProductItem>? = null
)

data class OtherProductItem(
    @SerializedName("nik") val nik: String? = null,    // Link URL / Jump target
    @SerializedName("pam") val pam: String? = null,    // Product Name
    @SerializedName("oic") val oic: String? = null,    // Icon URL
    @SerializedName("oan") val oan: String? = null,    // Tag / Subtext
    @SerializedName("amo") val amo: Long = 0,         // Max Amount
    @SerializedName("ita") val ita: String? = null,   // Daily Interest
    @SerializedName("ipe") val ipe: Int = 0,          // Min Period
    @SerializedName("ape") val ape: Int = 0,          // Max Period
    @SerializedName("gad") val gad: Float = 0f,       // Rating (Gad)
    
    // Client-side transient field (mirrors Vue borrowerCount)
    var borrowerCount: Int = 0
)
