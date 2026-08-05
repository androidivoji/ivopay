package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class MgeaResponse (
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: MgeaData? = null
)

data class MgeaData(
    @SerializedName("cme") val cme: CmeData? = null
)

data class CmeData(
    @SerializedName("pgsh") val pgsh: Boolean = false
)
