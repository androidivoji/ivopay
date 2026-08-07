package com.example.ivopay.app.ui.lender.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewContractViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ViewContractUiState())
    val uiState: StateFlow<ViewContractUiState> = _uiState.asStateFlow()

    fun getUserInfo() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getLenderUserInfo(JsonObject())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1) {
                        val tgim = body.data?.images?.signatureUrl ?: ""
                        _uiState.update { it.copy(signImage = tgim) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getSignContracts(mdi: String, type: Int) {
        if (mdi.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("mdi", mdi)
                }
                
                val response = if (type == 1) {
                    NetworkClient.apiService.getBorrowerContract(requestBody)
                } else {
                    NetworkClient.apiService.getPlatformContract(requestBody)
                }

                if (response.isSuccessful) {
                    val htmlContent = response.body()?.string() ?: ""
                    _uiState.update { it.copy(isLoading = false, htmlText = htmlContent) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

}