package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class BorrowerContractsResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: ContractData? = null
)

data class ContractData(
    @SerializedName("vhtml") val vhtml: String? = null,
    @SerializedName("dpdf") val dpdf: String? = null
)
