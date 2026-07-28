package com.example.ivopay.app.ui.lender.portofolio.waitsign

import android.graphics.Bitmap

data class BorrowerSignContractsUiState(
    val isLoading: Boolean = false,
    val htmlText: String = "",
    val signImageString: String? = null, // URL atau Base64 dari backend
    val showSignPop: Boolean = false,
    val isUpdateSignature: Boolean = false,
    val toastMessage: String? = null,
    val isSignSuccess: Boolean = false
)