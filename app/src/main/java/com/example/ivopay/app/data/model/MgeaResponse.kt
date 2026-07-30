package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class MgeaResponse (
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: Any? = null
)