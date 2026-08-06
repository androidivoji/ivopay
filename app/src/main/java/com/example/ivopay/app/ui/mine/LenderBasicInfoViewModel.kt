package com.example.ivopay.app.ui.mine

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.BankItem
import com.example.ivopay.app.data.model.LenderUserInfoData
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class LenderBasicInfoViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)

    var isLoading by mutableStateOf(false)
    var bankList by mutableStateOf<List<BankItem>>(emptyList())
    var userInfo by mutableStateOf<LenderUserInfoData?>(null)
    var commonParams by mutableStateOf<JsonObject?>(null)

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            isLoading = true
            fetchCommonParamConfig()
            fetchBankList()
            fetchUserInfo()
            isLoading = false
        }
    }

    private suspend fun fetchCommonParamConfig() {
        try {
            val response = NetworkClient.apiService.getCommonConfig()
            if (response.isSuccessful) {
                commonParams = response.body()?.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchBankList() {
        try {
            val requestBody = JsonObject().apply {
                addProperty("spe", "h")
            }
            val response = NetworkClient.apiService.getBankList(requestBody)
            if (response.isSuccessful) {
                bankList = response.body()?.data?.bankList ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchUserInfo() {
        try {
            val response = NetworkClient.apiService.getLenderUserInfo()
            if (response.isSuccessful) {
                userInfo = response.body()?.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper untuk mengambil list dari commonParams
    fun getCommonList(key: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val jsonArray = commonParams?.getAsJsonArray(key)
            jsonArray?.forEach { element ->
                val obj = element.asJsonObject
                val k = obj.get("k").asString
                val v = obj.get("v").asString
                list.add(k to v)
            }
        } catch (e: Exception) {
            // Error parsing or key not found
        }
        return list
    }

    fun updateLenderUserInfo(
        bankName: String,
        accountNumber: String,
        accountOwner: String,
        fullName: String,
        docType: Int,
        idNumber: String,
        birthPlace: String,
        birthDate: String,
        email: String,
        npwpNumber: String,
        rtKey: String,
        rwKey: String,
        province: String,
        city: String,
        postalCode: String,
        addressDetail: String,
        jobKey: String,
        incomeKey: String,
        companyName: String,
        companyAddress: String,
        photoList: List<LenderPhotoItem>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val requestBody = JsonObject().apply {
                    // Bank Info
                    addProperty("ban", bankName)
                    addProperty("bant", accountNumber)
                    addProperty("bante", accountOwner)

                    // Personal Info
                    addProperty("fun", fullName)
                    addProperty("inmt", docType)
                    addProperty("inm", idNumber)
                    addProperty("bire", birthDate)
                    addProperty("npnm", npwpNumber)
                    addProperty("eil", email)

                    // Location
                    addProperty("lpidn", province)
                    addProperty("lcidn", city)
                    addProperty("rtid", rtKey)
                    addProperty("rwid", rwKey)
                    addProperty("del", addressDetail)
                    addProperty("bipl", birthPlace)
                    addProperty("poco", postalCode)

                    // Work Info
                    addProperty("posi", jobKey)
                    addProperty("anin", incomeKey)
                    addProperty("con", companyName)
                    addProperty("caddr", companyAddress)

                    // Images (if new ones are selected)
                    photoList.forEach { item ->
                        item.bitmap?.let { bitmap ->
                            addProperty(item.imgName, convertBitmapToBase64(bitmap))
                        }
                    }
                }

                val response = NetworkClient.apiService.updateLenderUserInfo(requestBody)
                val body = response.body()
                if (response.isSuccessful && body?.get("code")?.asInt == 1) {
                    onSuccess()
                } else {
                    onError(body?.get("msg")?.asString ?: "Gagal update data")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan")
            } finally {
                isLoading = false
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
