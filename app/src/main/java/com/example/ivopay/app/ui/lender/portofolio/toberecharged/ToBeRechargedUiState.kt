package com.example.ivopay.app.ui.lender.portofolio.toberecharged

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

data class StatusInfo(
    val txt: String,
    val color: Color
)

data class RechargeOrderItem(
    @SerializedName("odi") val odi: String = "",    // Order ID
    @SerializedName("bnm") val bnm: String = "",    // Bank Name
    @SerializedName("bkn") val bkn: String = "",    // Bank Code/Branch
    @SerializedName("pcd") val pcd: String = "",    // VA Account Number
    @SerializedName("toa") val toa: Int = 0,        // Total Amount (Count)
    @SerializedName("tpa") val tpa: Double = 0.0,   // Total Payment Amount
    @SerializedName("ota") val ota: Int = 0,        // Order Status Code
    @SerializedName("abn") val abn: List<String> = emptyList() // Names list
)

data class ToBeRechargedUiState(
    val isLoading: Boolean = false,
    val contractLists: List<RechargeOrderItem> = emptyList(),
    val errorMessage: String? = null
)
