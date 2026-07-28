package com.example.ivopay.app.ui.lender.portofolio.waitsignature

data class WaitSignOrderItem(
    val odi: String = "",         // Order ID
    val sno: String = "",         // Serial / Serial Number
    val toa: Int = 0,             // Amount / Quantity
    val tpa: Double = 0.0,        // Total Amount
    val abn: List<String> = emptyList(), // Array of Borrower Names
    val isSelect: Boolean = false  // Local UI selection state
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