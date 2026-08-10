package com.example.ivopay.app.ui.loan

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

import java.util.Timer
import java.util.TimerTask

class BorrowerSignContractsViewModel : ViewModel() {

    var htmlText by mutableStateOf("")
    var showSignBtn by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    
    // Popups & Flow
    var showSignPop by mutableStateOf(false)
    var showSignTipsPop by mutableStateOf(false)
    var showVIDACodePop by mutableStateOf(false)
    
    // States
    var canSign by mutableStateOf(false)
    var checkAgree by mutableStateOf(false)
    var signImage by mutableStateOf<Bitmap?>(null)
    var verCode by mutableStateOf("")
    var verCountDown by mutableStateOf(0)
    var sendAble by mutableStateOf(true)
    
    var isWiue by mutableStateOf(false)
    var dpdf by mutableStateOf("")

    private var noc: String = ""
    private var timer: Timer? = null

    fun init(noc: String, isWiue: Boolean = false) {
        this.noc = noc
        this.isWiue = isWiue
        fetchContractData()
    }

    private fun fetchContractData() {
        isLoading = true
        viewModelScope.launch {
            // 1. Ambil Status dulu (gbss) untuk mendapatkan NOC terbaru/valid
            fetchContractStatus()
                // 2. Baru ambil isi kontrak (gbsc) menggunakan NOC hasil dari step 1
                if (noc.isNotEmpty()) {
                    fetchContractHtml()
                }
            
            isLoading = false
        }
    }

    private suspend fun fetchContractStatus() {
        try {
            val requestBody = JsonObject().apply {
                if (noc.isNotEmpty()) addProperty("noc", noc)
            }
            val response = NetworkClient.apiService.getBorrowerContractsStatus(requestBody)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.get("code")?.asInt == 1) {
                    val data = body.getAsJsonObject("data")
                    // Logic: res.data.data.srq && res.data.data.nsc
                    val srq = data.get("srq")?.asBoolean ?: false
                    val nsc = data.get("nsc")?.asBoolean ?: false
                    
                    showSignBtn = srq && nsc
                    
                    if (showSignBtn) {
                        showSignTipsPop = true
                    }

                    val newNoc = data.get("noc")?.asString
                    if (!newNoc.isNullOrEmpty()) {
                        this.noc = newNoc
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchContractHtml() {
        try {
            val requestBody = JsonObject().apply {
                addProperty("noc", noc)
            }
            val response = NetworkClient.apiService.getBorrowerContracts(requestBody)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.code == 1) {
                    htmlText = body.data?.vhtml ?: ""
                    dpdf = body.data?.dpdf ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendCode(onError: (String) -> Unit) {
        if (!sendAble) return
        
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.borrowerSendCode()
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    startTimer()
                    showVIDACodePop = true
                } else {
                    onError(response.body()?.get("msg")?.asString ?: "Gagal kirim kode")
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan")
            }
        }
    }

    private fun startTimer() {
        sendAble = false
        verCountDown = 60
        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (verCountDown > 0) {
                    verCountDown--
                } else {
                    sendAble = true
                    timer?.cancel()
                }
            }
        }, 0, 1000)
    }

    fun verifyOTP(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!checkAgree) {
            onError("Silakan centang persetujuan terlebih dahulu")
            return
        }
        if (code.length != 4) {
            onError("Kode OTP harus terdiri dari 4 digit angka")
            return
        }

        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("ver", code)
                }
                val response = NetworkClient.apiService.borrowerCheckCode(requestBody)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    showVIDACodePop = false
                    showSignPop = true
                    onSuccess()
                } else {
                    onError(response.body()?.get("msg")?.asString ?: "Kode OTP salah")
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan")
            }
        }
    }

    fun submitSignature(bitmap: Bitmap, onSuccess: () -> Unit, onError: (Int?, String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()

                // Ganti Content-Type ke application/octet-stream untuk bsi agar lebih aman diterima server
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("noc", noc)
                    .addFormDataPart(
                        "bsi", 
                        "signature.png", 
                        byteArray.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                    )
                    .build()

                val response = NetworkClient.apiService.borrowerSign(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    val code = body?.get("code")?.asInt
                    if (code == 1) {
                        onSuccess()
                    } else {
                        onError(code, body?.get("msg")?.asString ?: "Gagal tanda tangan")
                    }
                } else {
                    onError(response.code(), "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onError(null, e.message ?: "Terjadi kesalahan")
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
