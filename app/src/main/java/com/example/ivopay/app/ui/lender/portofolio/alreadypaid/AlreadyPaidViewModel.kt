package com.example.ivopay.app.ui.lender.portofolio.alreadypaid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlreadyPaidViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AlreadyPaidUiState())
    val uiState: StateFlow<AlreadyPaidUiState> = _uiState.asStateFlow()

    init {
        getContracts()
    }

    fun getContracts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val requestBody = JsonObject().apply {
                    addProperty("yto", "3")
                }
                val response = NetworkClient.apiService.getBorrowerOrderList(requestBody)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val data = body.getAsJsonObject("data")
                        val oli = data.getAsJsonArray("oli")
                        
                        val orderList: List<PaidOrderItem> = Gson().fromJson(
                            oli,
                            object : TypeToken<List<PaidOrderItem>>() {}.type
                        )

                        _uiState.value = _uiState.value.copy(
                            contractLists = orderList,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = body?.get("msg")?.asString ?: "Gagal memuat data"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Terjadi kesalahan"
                )
            }
        }
    }
}
