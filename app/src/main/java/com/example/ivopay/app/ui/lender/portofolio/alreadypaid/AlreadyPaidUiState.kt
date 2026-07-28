package com.example.ivopay.app.ui.lender.portofolio.alreadypaid

data class PaidOrderItem(
    val odi: String = "",    // Order ID
    val bnm: String = "",    // Name
    val bkn: String = "",    // Bank Name
    val pcd: String = "",    // Virtual Account Number
    val toa: Int = 0,        // Amount / Total Quantity
    val tpa: Double = 0.0,   // Total Payment Amount
    var isSelect: Boolean = false
)

data class AlreadyPaidUiState(
    val isLoading: Boolean = false,
    val contractLists: List<PaidOrderItem> = emptyList(),
    val errorMessage: String? = null
)