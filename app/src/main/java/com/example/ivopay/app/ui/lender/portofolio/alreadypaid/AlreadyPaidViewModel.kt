package com.example.ivopay.app.ui.lender.portofolio.alreadypaid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                // Simulasi pemanggilan API _getBorrowerOrderList({ yto: 3 })
                // Ganti bagian ini dengan repository/API Call asli Anda
                val mockResponseList = listOf(
                    PaidOrderItem(
                        odi = "ORD_1001",
                        bnm = "Budi Santoso",
                        bkn = "Bank BCA",
                        pcd = "88308123456789",
                        toa = 2,
                        tpa = 4050000.0
                    ),
                    PaidOrderItem(
                        odi = "ORD_1002",
                        bnm = "Siti Rahma",
                        bkn = "Bank Mandiri",
                        pcd = "88308987654321",
                        toa = 1,
                        tpa = 2025000.0
                    )
                )

                _uiState.value = _uiState.value.copy(
                    contractLists = mockResponseList,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Terjadi kesalahan"
                )
            }
        }
    }
}