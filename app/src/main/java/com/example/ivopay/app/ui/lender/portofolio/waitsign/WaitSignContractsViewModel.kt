package com.example.ivopay.app.ui.lender.portofolio.waitsign

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

data class WaitSignContractsUiState(
    val isLoading: Boolean = false,
    val contractLists: List<ContractItem> = emptyList(),
    val errorMessage: String? = null
)

class WaitSignContractsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WaitSignContractsUiState())
    val uiState: StateFlow<WaitSignContractsUiState> = _uiState.asStateFlow()

    fun getOrderDetail(odi: String) {
        if (odi.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("odi", odi)
                }
                val response = NetworkClient.apiService.getOrderDetail(requestBody)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val data = body.getAsJsonObject("data")
                        val odli = data.getAsJsonArray("odli")
                        
                        val orderList: List<ContractItem> = Gson().fromJson(
                            odli,
                            object : TypeToken<List<ContractItem>>() {}.type
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
                                errorMessage = body?.get("msg")?.asString ?: "Gagal memuat detail"
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
