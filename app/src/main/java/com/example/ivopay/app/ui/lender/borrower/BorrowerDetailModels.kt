package com.example.ivopay.app.ui.lender.borrower

import androidx.annotation.DrawableRes

data class InfoItem(
    val label: String,
    val value: String
)

data class SectionInfo(
    @DrawableRes val iconRes: Int,
    val title: String,
    val contentList: List<InfoItem>
)

data class BorrowRecord(
    val tma: String, // Nilai Pinjaman
    val pdi: String, // Status Peminjaman
    val dun: String, // Apakah untuk membayar
    val ods: String  // Apakah sudah lewat waktu
)

data class BorrowerDetailUiState(
    val isLoading: Boolean = false,
    val borrowerSections: List<SectionInfo> = emptyList(),
    val borrowRecordList: List<BorrowRecord> = emptyList()
)