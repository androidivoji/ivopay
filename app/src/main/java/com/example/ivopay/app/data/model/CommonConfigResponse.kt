package com.example.ivopay.app.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class CommonConfigResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: JsonObject? = null
)
