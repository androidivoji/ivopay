package com.example.ivopay.app.ui.lender.home

import com.google.gson.annotations.SerializedName

data class BorrowerItem(
    @SerializedName("ati") val ati: String,
    @SerializedName("oen") val oen: String,        // Nama/Kode Borrower
    @SerializedName("tma") val tma: Double,        // Loan Amount
    @SerializedName("ife") val ife: Double,        // Income
    @SerializedName("npeo") val npeo: String,       // Nilai Pinjaman / Rating
    @SerializedName("bcy") val bcy: String,        // City
    @SerializedName("bpo") val bpo: String,        // Purpose
    @SerializedName("aut") val aut: String,        // Approved Time
    var isSelect: Boolean = false
)

data class InsuranceItem(
    @SerializedName("ian") val ian: String,        // Insurance Name
    @SerializedName("ity") val ity: Int,          // Insurance Type
    @SerializedName("ire") val ire: Double,       // Insurance Rate
    @SerializedName("ima") val ima: Double        // Insurance Amount
)

data class FinanceDetail(
    @SerializedName("toa") val toa: Int = 0,       // Total Loan Count
    @SerializedName("atma") val atma: Double = 0.0, // Total Amount
    @SerializedName("trv") val trv: Double = 0.0,  // Income Assessment
    @SerializedName("iet") val iet: String = "",   // Repayment Time
    @SerializedName("iat") val iat: Double = 0.0,  // Total Base Payment
    @SerializedName("isnc") val isnc: List<InsuranceItem> = emptyList()
)

data class FinanceBill(
    @SerializedName("bnm") val bnm: String = "",   // Bank Name
    @SerializedName("bkn") val bkn: String = "",   // Bank Code/Branch
    @SerializedName("pcd") val pcd: String = "",   // VA Account Number
    @SerializedName("toa") val toa: Int = 0,       // Total Loan
    @SerializedName("ima") val ima: Double = 0.0,  // Insurance Amount
    @SerializedName("tpa") val tpa: Double = 0.0   // Total Payment Amount
)

data class LenderHomeUiState(
    val isLoading: Boolean = false,
    val lenderStatus: Int = 0,    // 0: Review, 1: Approved, 2: Rejected
    val uico: Boolean = false,    // Status kelengkapan profil
    val borrowList: List<BorrowerItem> = emptyList(),
    val financeDetail: FinanceDetail = FinanceDetail(),
    val insuranceList: List<InsuranceItem> = emptyList(),
    val financeBill: FinanceBill = FinanceBill(),

    // UI Dialog & BottomSheet Controllers
    val showSelectLoanDesc: Boolean = false,
    val showConfirmPayPop: Boolean = false,
    val showSuccessNotify: Boolean = false
)
