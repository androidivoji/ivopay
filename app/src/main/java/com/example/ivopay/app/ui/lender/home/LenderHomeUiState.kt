package com.example.ivopay.app.ui.lender.home

data class BorrowerItem(
    val ati: String,
    val oen: String,        // Nama/Kode Borrower
    val tma: Double,        // Loan Amount
    val ife: Double,        // Income
    val npeo: String,       // Nilai Pinjaman / Rating
    val bcy: String,        // City
    val bpo: String,        // Purpose
    val aut: String,        // Approved Time
    var isSelect: Boolean = false
)

data class InsuranceItem(
    val ian: String,        // Insurance Name
    val ity: Int,          // Insurance Type
    val ire: Double,       // Insurance Rate
    val ima: Double        // Insurance Amount
)

data class FinanceDetail(
    val toa: Int = 0,       // Total Loan Count
    val atma: Double = 0.0, // Total Amount
    val trv: Double = 0.0,  // Income Assessment
    val iet: String = "",   // Repayment Time
    val iat: Double = 0.0,  // Total Base Payment
    val isnc: List<InsuranceItem> = emptyList()
)

data class FinanceBill(
    val bnm: String = "",   // Bank Name
    val bkn: String = "",   // Bank Code/Branch
    val pcd: String = "",   // VA Account Number
    val toa: Int = 0,       // Total Loan
    val ima: Double = 0.0,  // Insurance Amount
    val tpa: Double = 0.0   // Total Payment Amount
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