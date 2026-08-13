package com.example.ivopay.app.ui.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.util.SecurityUtils
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GestureCreateViewModel(private val context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)

    var infoTips by mutableStateOf("Silahkan gambar pola kunci")
    var isTipsError by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    
    var firstPattern by mutableStateOf("")
    var isConfirmStep by mutableStateOf(false)

    fun handlePatternComplete(pattern: String, onSuccess: (String) -> Unit) {
        if (pattern.length < 5) {
            isTipsError = true
            infoTips = "Gambar setidaknya 5 titik"
            return
        }

        if (!isConfirmStep) {
            // Langkah pertama: gambar pola
            firstPattern = pattern
            isConfirmStep = true
            infoTips = "Silahkan gambar pola kunci kembali"
            isTipsError = false
        } else {
            // Langkah kedua: konfirmasi pola
            if (pattern == firstPattern) {
                requestSetGesture(pattern, onSuccess)
            } else {
                isTipsError = true
                infoTips = "Pola tidak konsisten, silahkan gambar kembali"
            }
        }
    }

    private fun requestSetGesture(pattern: String, onSuccess: (String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                // Enkripsi pola sesuai logika Vue
                val encryptedPattern = SecurityUtils.encodeGesture(pattern)
                val requestBody = JsonObject().apply {
                    addProperty("gede", encryptedPattern)
                }

                val response = NetworkClient.apiService.setGesturePwd(requestBody)
                isLoading = false

                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    // Berhasil: Upload Event N21 dan panggil callback setelah delay 1 detik
                    uploadTrackingEvent("N21")
                    onSuccess("Kata sandi gerakan berhasi")
                } else {
                    val msg = response.body()?.get("msg")?.asString ?: "Gagal mengatur pola"
                    isTipsError = true
                    infoTips = "Harap menggambar pola kunci sekali lagi"
                    // Reset langkah konfirmasi jika gagal di server agar user mengulang dari awal
                    reset()
                }
            } catch (e: Exception) {
                isLoading = false
                isTipsError = true
                infoTips = "Harap menggambar pola kunci sekali lagi"
                reset()
            }
        }
    }

    private fun uploadTrackingEvent(evme: String) {
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("evme", evme)
                    addProperty("eval", "1")
                    addProperty("spe", "h")
                }
                NetworkClient.apiService.uploadEvent(body)
            } catch (e: Exception) {}
        }
    }
    
    fun reset() {
        firstPattern = ""
        isConfirmStep = false
        // infoTips tidak di-reset di sini agar pesan error tetap terlihat saat transisi reset
    }
}
