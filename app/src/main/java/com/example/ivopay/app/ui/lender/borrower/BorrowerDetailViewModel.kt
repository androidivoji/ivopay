package com.example.ivopay.app.ui.lender.borrower

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class BorrowerDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BorrowerDetailUiState())
    val uiState: StateFlow<BorrowerDetailUiState> = _uiState.asStateFlow()

    fun getBorrowDetail(ati: String) {
        if (ati.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // TODO: Ganti dengan pemanggilan API real _getBorrowerDetail(ati)
            delay(600)

            // Simulasi parsing data response (su)
            val mockSections = listOf(
                SectionInfo(
                    iconRes = R.drawable.iv_borrower_ic_name,
                    title = "Data Pribadi",
                    contentList = listOf(
                        InfoItem("Borrower Nama:", "Budi Santoso"),
                        InfoItem("Jenis Kelamin:", "Laki-laki"),
                        InfoItem("Usia:", "30 Tahun"),
                        InfoItem("Tempat Lahir:", "Jakarta")
                    )
                ),
                SectionInfo(
                    iconRes = R.drawable.iv_borrower_ic_details,
                    title = "Rincian tagihan",
                    contentList = listOf(
                        InfoItem("Nilai Pinjaman:", formatRupiah(5000000.0)),
                        InfoItem("Jatuh Tempo:", "2026-08-30"),
                        InfoItem("Estimasi pendapatan:", formatRupiah(250000.0)),
                        InfoItem("Besaran Komisi:", "2%"),
                        InfoItem("PPN Komisi:", "11%")
                    )
                ),
                SectionInfo(
                    iconRes = R.drawable.iv_borrower_ic_work,
                    title = "Data Pekerjaan",
                    contentList = listOf(
                        InfoItem("Nama Perusahaan:", "PT Teknologi Indonesia"),
                        InfoItem("Jenis Usaha:", "Teknologi Informasi"),
                        InfoItem("Jenis Industri:", "Software"),
                        InfoItem("Alamat kantor:", "Jakarta Selatan"),
                        InfoItem("Lama Bekerja:", "3 Tahun"),
                        InfoItem("Penghasilan Per Bulan:", formatRupiah(12000000.0))
                    )
                ),
                SectionInfo(
                    iconRes = R.drawable.iv_borrower_ic_score,
                    title = "Nilai Kredit",
                    contentList = listOf(
                        InfoItem("Nilai Kredit(Total 100):", "85")
                    )
                )
            )

            val mockRecords = listOf(
                BorrowRecord(
                    tma = formatRupiah(3000000.0),
                    pdi = "Lunas",
                    dun = "Ya",
                    ods = "Tidak"
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    borrowerSections = mockSections,
                    borrowRecordList = mockRecords
                )
            }
        }
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }
}