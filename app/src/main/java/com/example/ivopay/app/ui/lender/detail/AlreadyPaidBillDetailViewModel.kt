package com.example.ivopay.app.ui.lender.detail

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
import java.text.NumberFormat
import java.util.Locale

class AlreadyPaidBillDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AlreadyPaidBillDetailUiState())
    val uiState: StateFlow<AlreadyPaidBillDetailUiState> = _uiState.asStateFlow()

    fun getOrderDetail(odi: String) {
        if (odi.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

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
                        
                        val orderList: List<PaidContractItem> = Gson().fromJson(
                            odli,
                            object : TypeToken<List<PaidContractItem>>() {}.type
                        )

                        _uiState.update {
                            it.copy(
                                contractLists = orderList,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getItemStatus(mta: Int): StatusInfo {
        return when (mta) {
            201 -> StatusInfo(
                txt = "Belum dibayar",
                color = Color(0xFFFF7725),
                bgColor = Color(0x0FFF7725)
            )
            202 -> StatusInfo(
                txt = "Dibayar sebagian",
                color = Color(0xFFFF7725),
                bgColor = Color(0x0FFF7725)
            )
            203 -> StatusInfo(
                txt = "Dilunasi",
                color = Color(0xFF8C8C8C),
                bgColor = Color(0x1A8C8C8C)
            )
            else -> StatusInfo(
                txt = "Unknown",
                color = Color(0xFF8C8C8C),
                bgColor = Color(0x1A8C8C8C)
            )
        }
    }

    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }
}