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

class PlatformSignContractsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PlatformSignContractsUiState())
    val uiState: StateFlow<PlatformSignContractsUiState> = _uiState.asStateFlow()

    fun loadData(mdi: String) {
        if (mdi.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // 1. getUserInfo -> su.ide.tgim
            val mockSignImageFromUser: String? = null

            // 2. getPlatformContract (_getAPlatformContract)
            delay(500)
            val mockHtmlContent = """
                <h3>PERJANJIAN LAYANAN PLATFORM</h3>
                <p>Dokumen ini mengatur ketentuan antara Pemberi Pinjaman dan Penyelenggara Platform...</p>
                <br/><br/><br/><br/>
                <p>Silakan gulir ke bawah untuk menandatangani perjanjian platform.</p>
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

            // TODO: Panggil API _signLenderAndPlatform(mdi, lpsi = base64Image)
            delay(1000)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSignSuccess = true
                )
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}