package com.example.ivopay.app.ui.lender.portofolio.toberecharged

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import kotlinx.coroutines.launch

class ToBeRechargedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ToBeRechargedUiState())
    val uiState: StateFlow<ToBeRechargedUiState> = _uiState.asStateFlow()

    fun getContracts(onUpdateCount: (Int) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

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
                        
                        val orderList: List<RechargeOrderItem> = Gson().fromJson(
                            oli, 
                            object : TypeToken<List<RechargeOrderItem>>() {}.type
                        )

                        _uiState.value = _uiState.value.copy(
                            contractLists = orderList,
                            isLoading = false
                        )

                        // Update badge counter pada parent tab
                        onUpdateCount(orderList.size)
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

    // Menggantikan fungsi copyPayCode(pcd)
    fun copyPayCode(context: Context, pcd: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("VA Account", pcd)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Salin Sukses", Toast.LENGTH_SHORT).show()
    }

    // Menggantikan fungsi _getStatusColorLender(sts)
    fun getStatus(ota: Int): StatusInfo {
        return when (ota) {
            2 -> StatusInfo("Tanda tangan selesai", Color(0x66000000))
            102 -> StatusInfo("Menunggu ditanda tangan", Color(0xFFFF7725))
            3 -> StatusInfo("Kadaluarsa", Color.Gray)
            else -> StatusInfo("Pending", Color.DarkGray)
        }
    }
}