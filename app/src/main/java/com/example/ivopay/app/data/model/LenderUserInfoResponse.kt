package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class LenderUserInfoResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: LenderUserInfoData? = null
)

data class LenderUserInfoData(
    @SerializedName("pi") val pi: PersonalInfo? = null
)

data class PersonalInfo(
    @SerializedName("inm") val inm: Boolean = false
)
