package com.example.ivopay.app.ui.lender.portofolio.waitsign

data class PlatformSignContractsUiState(
    val isLoading: Boolean = false,
    val htmlText: String = "",
    val signImageString: String? = null,
    val showSignPop: Boolean = false,
    val isUpdateSignature: Boolean = false,
    val isSignSuccess: Boolean = false
)