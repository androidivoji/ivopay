package com.example.ivopay.app.ui.mine

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class ChangeBindPhoneViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    
    var phoneNumber by mutableStateOf(sessionManager.getMobileNumber() ?: "")
    var verCode by mutableStateOf("")
    var verCountDown by mutableIntStateOf(0)
    var isSendingCode by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    
    var ktpBitmap by mutableStateOf<Bitmap?>(null)
    var selfieBitmap by mutableStateOf<Bitmap?>(null)
    
    var showSuccessPop by mutableStateOf(false)
    var showHaveBillsPop by mutableStateOf(false)
    var errMsg by mutableStateOf("")
    var errCode by mutableIntStateOf(0)
    
    private var timerJob: Job? = null

    fun checkCanUpdatePhone() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.canUpdatePhone(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val body = response.body()
                    val code = body?.get("code")?.asInt ?: 0
                    if (code == 102 || code == 103) {
                        errCode = code
                        errMsg = body?.get("msg")?.asString ?: ""
                        showHaveBillsPop = true
                    } else if (code != 1) {
                        errMsg = body?.get("msg")?.asString ?: "Terjadi kesalahan"
                        // Show error toast logic can be here or in UI
                    }
                }
            } catch (e: Exception) {
                Log.e("ChangePhoneVM", "canUpdatePhone error", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun sendVerificationCode(onToast: (String) -> Unit) {
        if (phoneNumber.isEmpty()) {
            onToast("Silakan isi nomor ponsel")
            return
        }
        if (verCountDown > 0) {
            onToast("Silakan klik lagi setelah $verCountDown detik")
            return
        }
        
        isSendingCode = true
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("mob", phoneNumber)
                    addProperty("spe", "h")
                }
                val response = NetworkClient.apiService.cpSendCode(body)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    onToast("Setelah mengirimkan kode verifikasi, harap tunggu beberapa saat")
                    startTimer()
                } else {
                    val msg = response.body()?.get("msg")?.asString ?: "Gagal mengirim kode"
                    onToast(msg)
                }
            } catch (e: Exception) {
                onToast("Terjadi kesalahan sistem")
            } finally {
                isSendingCode = false
            }
        }
    }

    private fun startTimer() {
        verCountDown = 60
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (verCountDown > 0) {
                delay(1000)
                verCountDown--
            }
        }
    }

    fun submitInfo(onToast: (String) -> Unit) {
        if (ktpBitmap == null || selfieBitmap == null) {
            onToast("Harap Pilih Foto Terlebih Dahulu!")
            return
        }
        
        isLoading = true
        viewModelScope.launch {
            try {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.addFormDataPart("mob", phoneNumber)
                builder.addFormDataPart("ver", verCode)
                builder.addFormDataPart("spe", "h")
                
                // Add KTP photo
                ktpBitmap?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    builder.addFormDataPart("mae", "ktp.jpg", stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull()))
                }
                
                // Add Selfie photo
                selfieBitmap?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    builder.addFormDataPart("apo", "selfie.jpg", stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull()))
                }

                val response = NetworkClient.apiService.cpUpdate(builder.build())
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    showSuccessPop = true
                } else {
                    onToast(response.body()?.get("msg")?.asString ?: "Gagal simpan")
                }
            } catch (e: Exception) {
                onToast("Terjadi kesalahan sistem")
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
    }
}
