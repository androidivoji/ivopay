package com.example.ivopay.app.ui.lender.mycontracts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyContractsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MyContractsUiState())
    val uiState: StateFlow<MyContractsUiState> = _uiState.asStateFlow()

    init {
        getContracts()
    }

    fun getContracts() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getBorrowerContractList(JsonObject())
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val data = body.getAsJsonObject("data")
                        val ist = data.getAsJsonArray("ist")
                        
                        val orderList: List<MyContractItem> = Gson().fromJson(
                            ist,
                            object : TypeToken<List<MyContractItem>>() {}.type
                        )

                        _uiState.update {
                            it.copy(
                                contractLists = orderList,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                errorMessage = body?.get("msg")?.asString ?: "Gagal memuat kontrak"
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${response.code()}") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }
}
