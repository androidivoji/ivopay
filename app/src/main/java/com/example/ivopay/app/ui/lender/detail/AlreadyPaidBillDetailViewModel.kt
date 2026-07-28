package com.example.ivopay.app.ui.lender.detail

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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
            // TODO: Ganti dengan pemanggilan API real _getOrderDetail(odi)
            delay(600)

            // Simulasi response data dari backend su.odli
            val mockData = listOf(
                PaidContractItem(
                    mdi = "MDI_1002",
                    lfn = "Siti Aminah",
                    lat = 3000000.0,
                    tlr = 150000.0,
                    let = "2026-09-15",
                    mta = 202 // Dibayar sebagian
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    contractLists = mockData
                )
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