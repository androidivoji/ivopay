package com.example.ivopay.app.ui.lender.mycontracts

import com.google.gson.annotations.SerializedName

data class MyContractItem(
    @SerializedName("cno") val cno: String = "",    // Contract Number / ID
    @SerializedName("tma") val tma: Double = 0.0,   // Amount
    @SerializedName("lfn") val lfn: String = "",    // Name
    @SerializedName("sgd") val sgd: String = "",    // Signed Date
    var isSelect: Boolean = false
)

data class MyContractsUiState(
    val isLoading: Boolean = false,
    val contractLists: List<MyContractItem> = emptyList(),
    val errorMessage: String? = null
)
