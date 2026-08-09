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

class BorrowerSignContractsViewModel : ViewModel() {

    var htmlText by mutableStateOf("")
    var showSignBtn by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var showSignPop by mutableStateOf(false)
    var signImage by mutableStateOf<Bitmap?>(null)
    
    private var noc: String = ""

    fun init(noc: String) {
        this.noc = noc
        fetchContractData()
    }

    private fun fetchContractData() {
        isLoading = true
        viewModelScope.launch {
            // Urutan Hit API sesuai permintaan Anda:
            // 1. Ambil Status dulu (gbss) untuk mendapatkan NOC terbaru
            val statusOk = fetchContractStatus()
            
            // 2. Baru ambil isi kontrak (gbsc) menggunakan NOC hasil dari step 1
            if (statusOk) {
                fetchContractHtml()
            }
            
            isLoading = false
        }
    }

    private suspend fun fetchContractStatus(): Boolean {
        return try {
            // Hit gbss
            val requestBody = JsonObject().apply {
                if (noc.isNotEmpty()) addProperty("noc", noc)
            }
            val response = NetworkClient.apiService.getBorrowerContractsStatus(requestBody)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.get("code")?.asInt == 1) {
                    val data = body.getAsJsonObject("data")
                    showSignBtn = data.get("srq")?.asBoolean ?: false
                    
                    // Ambil NOC dari response data untuk digunakan di request selanjutnya (gbsc)
                    val newNoc = data.get("noc")?.asString
                    if (!newNoc.isNullOrEmpty()) {
                        this.noc = newNoc
                        Log.d("SIGN_CONTRACT", "Updated NOC from status API: $noc")
                    }
                    true
                } else false
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun fetchContractHtml() {
        try {
            // Hit gbsc menggunakan NOC terbaru
            val requestBody = JsonObject().apply {
                addProperty("noc", noc)
            }
            val response = NetworkClient.apiService.getBorrowerContracts(requestBody)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.code == 1) {
                    htmlText = body.data?.vhtml ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun submitSignature(bitmap: Bitmap, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                // Convert Bitmap to ByteArray
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()

                // Create Multipart request body
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("noc", noc)
                    .addFormDataPart(
                        "bsi", 
                        "signature.png", 
                        byteArray.toRequestBody("image/png".toMediaTypeOrNull())
                    )
                    .build()

                val response = NetworkClient.apiService.borrowerSign(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        onSuccess()
                    } else {
                        onError(body?.get("msg")?.asString ?: "Gagal tanda tangan")
                    }
                } else {
                    onError("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan")
            } finally {
                isLoading = false
            }
        }
    }
}
