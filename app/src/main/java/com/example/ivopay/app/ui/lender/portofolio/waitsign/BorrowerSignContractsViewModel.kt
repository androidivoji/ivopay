package com.example.ivopay.app.ui.lender.portofolio.waitsign

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class BorrowerSignContractsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BorrowerSignContractsUiState())
    val uiState: StateFlow<BorrowerSignContractsUiState> = _uiState.asStateFlow()

    fun loadData(mdi: String) {
        if (mdi.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Simulasi getLenderUserInfo
            val mockSignImageFromUser = null // misal: su.ide.tgim jika ada

            // Simulasi getSignContracts (HTML text)
            delay(500)
            val mockHtmlContent = """
                <h3>PERJANJIAN PINJAMAN MEMINJAM UANG</h3>
                <p>Pada hari ini, telah disepakati perjanjian antara Pihak Peminjam dan Pihak Pemberi Pinjaman...</p>
                <br/><br/><br/><br/><br/>
                <p>Silakan gulir ke bawah untuk menandatangani dokumen ini.</p>
            """.trimIndent()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    htmlText = mockHtmlContent,
                    signImageString = mockSignImageFromUser
                )
            }
        }
    }

    fun setShowSignPop(show: Boolean) {
        _uiState.update { it.copy(showSignPop = show) }
    }

    fun clearSignature() {
        _uiState.update {
            it.copy(
                signImageString = null,
                isUpdateSignature = true
            )
        }
    }

    fun submitSignature(mdi: String, bitmap: Bitmap?) {
        _uiState.update { it.copy(isLoading = true, showSignPop = false) }

        viewModelScope.launch {
            val base64Image = bitmap?.let { convertBitmapToBase64(it) }

            // TODO: Panggil API _signLenderAndBorrower(mdi, lsi)
            delay(1000)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSignSuccess = true
                )
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}