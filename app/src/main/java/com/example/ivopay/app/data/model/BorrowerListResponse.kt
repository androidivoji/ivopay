package com.example.ivopay.app.data.model

import com.example.ivopay.app.ui.lender.home.BorrowerItem
import com.google.gson.annotations.SerializedName

data class BorrowerListResponse(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: BorrowerListData? = null
)

data class BorrowerListData(
    @SerializedName("ais") val ais: List<BorrowerItem>? = null
)
