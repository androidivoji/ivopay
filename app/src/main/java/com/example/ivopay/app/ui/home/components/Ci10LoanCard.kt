package com.example.ivopay.app.ui.home.components

import androidx.compose.runtime.Composable
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.data.model.LoanProductConfig

@Composable
fun Ci10LoanCard(
    comData: LoanProductConfig?,
    curBill: LoanOrder? = null,
    onApply: () -> Unit,
    onNavigate: (String) -> Unit
) {
    // Berdasarkan kategori "installment" di Vue, Ci10 menggunakan style InlgLoanCard
    InlgLoanCard(
        comData = comData,
        curBill = curBill,
        onApply = onApply,
        onNavigate = onNavigate
    )
}
