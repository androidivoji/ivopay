package com.example.ivopay.app.ui.mine

import android.content.Context
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
}
