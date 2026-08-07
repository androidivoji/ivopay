package com.example.ivopay.app.ui.lender.portofolio.toberecharged

import androidx.compose.ui.graphics.Color
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

data class ToBeRechargedDetailUiState(
    val isLoading: Boolean = false,
    val contractLists: List<ContractItem> = emptyList(),
    val errorMessage: String? = null
)

class ToBeRechargedDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ToBeRechargedDetailUiState())
    val uiState: StateFlow<ToBeRechargedDetailUiState> = _uiState.asStateFlow()

    fun getContracts(odi: String) {
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

    fun getStatus(mta: Int): StatusInfo {
        return when (mta) {
            105 -> StatusInfo("Tanda tangan selesai", Color(0x66000000)) // rgba(0,0,0,0.4)
            104 -> StatusInfo("Menunggu ditanda tangan", Color(0xFFFF7725))
            else -> StatusInfo("Menunggu", Color.Gray)
        }
    }
}
