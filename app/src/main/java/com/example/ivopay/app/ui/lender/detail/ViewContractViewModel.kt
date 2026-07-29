package com.example.ivopay.app.ui.lender.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewContractViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ViewContractUiState())
    val uiState: StateFlow<ViewContractUiState> = _uiState.asStateFlow()

    fun getUserInfo() {
        viewModelScope.launch {

        }
    }

    fun getSignContracts(mdi: String, type: Int) {
        if (mdi.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            delay(600)

            val mockHtml = if (type == 1) {
                """
                <html>
                <head><style>body { font-family: sans-serif; padding: 10px; color: #333; line-height: 1.6; }</style></head>
                <body>
                    <h2>SURAT PERJANJIAN PINJAM MEMINJAM</h2>
                    <p>Antara <b>Lender</b> dan <b>Borrower</b> dengan nomor kontrak: <b>$mdi</b>.</p>
                    <p>Pasal 1: Ketentuan Umum...</p>
                    <p>Pasal 2: Hak dan Kewajiban...</p>
                </body>
                </html>
                """.trimIndent()
            } else {
                """
                <html>
                <head><style>body { font-family: sans-serif; padding: 10px; color: #333; line-height: 1.6; }</style></head>
                <body>
                    <h2>PERJANJIAN LAYANAN PLATFORM</h2>
                    <p>Antara <b>Lender</b> dan <b>Platform</b> dengan nomor kontrak: <b>$mdi</b>.</p>
                    <p>Pasal 1: Layanan Platform...</p>
                    <p>Pasal 2: Komisi dan Biaya...</p>
                </body>
                </html>
                """.trimIndent()
            }

            _uiState.update { it.copy(isLoading = false, htmlText = mockHtml) }
        }
    }

}