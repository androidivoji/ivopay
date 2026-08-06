package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: LoginData? = null
)

data class LoginData(
    @SerializedName("tkn") val token: String? = null,
    @SerializedName("mie") val mobile: String? = null,
    @SerializedName("act") val isActive: Boolean = false,
    @SerializedName("ina") val isIna: Boolean = false,
    @SerializedName("ngup") val isNgup: Boolean = false,
    @SerializedName("tome") val tome: String? = null,
    @SerializedName("uico") val isUserInfoCompleted: Boolean = false,
    @SerializedName("lost") val lostStatus: String? = null, // "3" for recovery popup
    @SerializedName("tinm") val inm: String? = null, // KTP/Name for popup, maps from tinm
    @SerializedName("role") val role: Int? = null
)
