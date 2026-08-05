package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginWayResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: LoginWayData? = null
)

data class LoginWayData(
    @SerializedName("v") val isValid: Boolean = false,
    @SerializedName("g") val hasGesture: Boolean = false,
    @SerializedName("w") val hasWaLogin: Boolean = false,
    @SerializedName("aig") val hasFaceLogin: Boolean = false,
    @SerializedName("v_ltr") val vLtr: Boolean = false
)
