package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class BankListResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: BankListData? = null
)

data class BankListData(
    @SerializedName("bl") val bankList: List<BankItem>? = null
)

data class BankItem(
    @SerializedName("d") val id: Int? = null,
    @SerializedName("n") val name: String? = null,
    @SerializedName("fn") val fullName: String? = null,
    @SerializedName("bal") val bal: Int? = null,
    @SerializedName("ais") val ais: Int? = null
)
