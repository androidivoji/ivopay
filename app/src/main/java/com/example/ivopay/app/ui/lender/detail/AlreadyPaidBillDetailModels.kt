package com.example.ivopay.app.ui.lender.detail

import androidx.compose.ui.graphics.Color

data class StatusInfo(
    val txt: String,
    val color: Color,
    val bgColor: Color
)

data class PaidContractItem(
    val mdi: String,       // ID Kontrak
    val lfn: String,       // Borrower Name
    val lat: Double,       // Loan Amount
    val tlr: Double,       // Lender Income
    val let: String,       // Due Date
    val mta: Int           // Status Code (201, 202, 203)
)

data class AlreadyPaidBillDetailUiState(
    val isLoading: Boolean = false,
    val contractLists: List<PaidContractItem> = emptyList()
)