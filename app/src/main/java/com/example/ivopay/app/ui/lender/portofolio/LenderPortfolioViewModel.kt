package com.example.ivopay.app.ui.lender.portofolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LenderPortfolioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LenderPortfolioUiState())
    val uiState: StateFlow<LenderPortfolioUiState> = _uiState.asStateFlow()

    init {
//        fetchRechargeBadgeCount()
    }

    fun onTabSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
    }

    // Menggantikan fungsi getContracts2() pada Vue
    fun fetchRechargeBadgeCount() {
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("yto", "2")
                }
                val response = NetworkClient.apiService.getBorrowerOrderList(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val data = body.getAsJsonObject("data")
                        val oli = data.getAsJsonArray("oli")
                        if (oli != null && oli.size() > 0) {
                            _uiState.value = _uiState.value.copy(toRechargeCount = oli.size())
                        } else {
                            _uiState.value = _uiState.value.copy(toRechargeCount = 0)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Callback pengganti @updateCount dari child tab 1
    fun updateWaitSignCount(count: Int) {
        _uiState.value = _uiState.value.copy(waitSignCount = count)
    }

    // Callback pengganti @updateCount dari child tab 2
    fun updateRechargeCount(count: Int) {
        _uiState.value = _uiState.value.copy(toRechargeCount = count)
    }
}