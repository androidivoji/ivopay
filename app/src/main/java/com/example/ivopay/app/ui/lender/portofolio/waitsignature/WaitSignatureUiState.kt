package com.example.ivopay.app.ui.lender.portofolio.waitsignature

import com.google.gson.annotations.SerializedName

data class WaitSignOrderItem(
    @SerializedName("odi") val odi: String = "",         // Order ID
    @SerializedName("sno") val sno: String = "",         // Serial / Serial Number
    @SerializedName("toa") val toa: Int = 0,             // Amount / Quantity
    @SerializedName("tpa") val tpa: Double = 0.0,        // Total Amount
    @SerializedName("abn") val abn: List<String> = emptyList(), // Array of Borrower Names
    var isSelect: Boolean = false  // Local UI selection state
)

data class WaitSignatureUiState(
    val isLoading: Boolean = false,
    val contractLists: List<WaitSignOrderItem> = emptyList(),
    val showSignAllModal: Boolean = false,
    val showSignProgressModal: Boolean = false,
    val isAgreementChecked: Boolean = false,
    val signProgressPercent: Float = 0f,
    val toastMessage: String? = null
)
