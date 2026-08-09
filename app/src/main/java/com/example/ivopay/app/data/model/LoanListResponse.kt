package com.example.ivopay.app.data.model

import com.google.gson.annotations.SerializedName

data class LoanListResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: LoanListData? = null
)

data class LoanListData(
    @SerializedName("ois") val orders: List<LoanOrder>? = null
)
