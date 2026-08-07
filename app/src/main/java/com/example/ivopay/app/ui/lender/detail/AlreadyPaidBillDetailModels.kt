package com.example.ivopay.app.ui.lender.detail

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

data class StatusInfo(
    val txt: String,
    val color: Color,
    val bgColor: Color
)

data class PaidContractItem(
    @SerializedName("mdi") val mdi: String,       // ID Kontrak
    @SerializedName("lfn") val lfn: String,       // Borrower Name
    @SerializedName("lat") val lat: Double,       // Loan Amount
    @SerializedName("tlr") val tlr: Double,       // Lender Income
    @SerializedName("let") val let: String,       // Due Date
    @SerializedName("mta") val mta: Int           // Status Code (201, 202, 203)
)

data class AlreadyPaidBillDetailUiState(
    val isLoading: Boolean = false,
    val contractLists: List<PaidContractItem> = emptyList()
)