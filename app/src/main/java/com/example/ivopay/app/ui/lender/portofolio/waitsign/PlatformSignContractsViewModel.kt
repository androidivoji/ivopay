package com.example.ivopay.app.ui.lender.portofolio.waitsign

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.JsonObject
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
            try {
                // 1. getPlatformContract (_getAPlatformContract)
                val requestBody = JsonObject().apply {
                    addProperty("mdi", mdi)
                }
                val response = NetworkClient.apiService.getPlatformContract(requestBody)
                
                if (response.isSuccessful) {
                    val htmlContent = response.body()?.string() ?: ""
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            htmlText = htmlContent
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            toastMessage = "Gagal memuat kontrak platform"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        toastMessage = e.localizedMessage
                    )
                }
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
            try {
                val requestBody = JsonObject().apply {
                    addProperty("mdi", mdi)
                    
                    // Jika tanda tangan baru dibuat/diupdate, kirim dalam base64 (key: lpsi)
                    if (_uiState.value.isUpdateSignature && bitmap != null) {
                        addProperty("lpsi", convertBitmapToBase64(bitmap))
                    }
                }

                val response = NetworkClient.apiService.signLenderAndPlatform(requestBody)
                
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignSuccess = true
                        )
                    }
                } else {
                    val msg = response.body()?.get("msg")?.asString ?: "Gagal tanda tangan platform"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            toastMessage = msg
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        toastMessage = e.localizedMessage
                    )
                }
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