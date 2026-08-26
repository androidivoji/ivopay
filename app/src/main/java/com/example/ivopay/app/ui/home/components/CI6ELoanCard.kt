package com.example.ivopay.app.ui.home.components

import androidx.compose.runtime.Composable
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.data.model.LoanProductConfig

@Composable
fun CI6ELoanCard(
    comData: LoanProductConfig?,
    curBill: LoanOrder? = null,
    onApply: () -> Unit,
    onNavigate: (String) -> Unit
) {
    // CI6E menggunakan style yang sama dengan InlgLoanCard berdasarkan template Vue
    InlgLoanCard(
        comData = comData,
        curBill = curBill,
        onApply = onApply,
        onNavigate = onNavigate
    )
}
