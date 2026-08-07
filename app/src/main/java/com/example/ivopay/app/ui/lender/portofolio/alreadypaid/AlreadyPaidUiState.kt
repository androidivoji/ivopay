package com.example.ivopay.app.ui.lender.portofolio.alreadypaid

import com.google.gson.annotations.SerializedName

data class PaidOrderItem(
    @SerializedName("odi") val odi: String = "",    // Order ID
    @SerializedName("bnm") val bnm: String = "",    // Bank Name
    @SerializedName("bkn") val bkn: String = "",    // Bank Code/Branch
    @SerializedName("pcd") val pcd: String = "",    // VA Account Number
    @SerializedName("toa") val toa: Int = 0,        // Total Amount (Count)
    @SerializedName("tpa") val tpa: Double = 0.0,   // Total Payment Amount
    @SerializedName("abn") val abn: List<String> = emptyList(), // Names list
    var isSelect: Boolean = false
)

data class AlreadyPaidUiState(
    val isLoading: Boolean = false,
    val contractLists: List<PaidOrderItem> = emptyList(),
    val errorMessage: String? = null
)
