package com.example.ivopay.app.ui.lender.portofolio.toberecharged

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                // Simulasi panggilan API _getBorrowerOrderList({ yto: 2 })
                val mockResponseList = listOf(
                    RechargeOrderItem(
                        odi = "RECHARGE_2001",
                        bnm = "Ahmad Rifai",
                        bkn = "Bank BCA",
                        pcd = "88308998877665",
                        toa = 3,
                        tpa = 6000000.0,
                        ota = 1
                    ),
                    RechargeOrderItem(
                        odi = "RECHARGE_2002",
                        bnm = "Dewi Lestari",
                        bkn = "Bank BRI",
                        pcd = "88308112233445",
                        toa = 1,
                        tpa = 2000000.0,
                        ota = 2
                    )
                )

                _uiState.value = _uiState.value.copy(
                    contractLists = mockResponseList,
                    isLoading = false
                )

                // Update badge counter pada parent tab
                onUpdateCount(mockResponseList.size)

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
            1 -> StatusInfo("Menunggu Pembayaran", Color(0xFFFE5455))
            2 -> StatusInfo("Proses Verifikasi", Color(0xFFFF9800))
            3 -> StatusInfo("Kadaluarsa", Color.Gray)
            else -> StatusInfo("Pending", Color.DarkGray)
        }
    }
}