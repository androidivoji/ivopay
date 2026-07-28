package com.example.ivopay.app.ui.lender.portofolio.toberecharged

import androidx.compose.ui.graphics.Color

data class StatusInfo(
    val txt: String,
    val color: Color
)

data class RechargeOrderItem(
    val odi: String = "",    // Order ID
    val bnm: String = "",    // Name
    val bkn: String = "",    // Bank Name
    val pcd: String = "",    // Virtual Account Number
    val toa: Int = 0,        // Amount / Quantity
    val tpa: Double = 0.0,   // Total Payment Amount
    val ota: Int = 0         // Order Status Code
)

data class ToBeRechargedUiState(
    val isLoading: Boolean = false,
    val contractLists: List<RechargeOrderItem> = emptyList(),
    val errorMessage: String? = null
)