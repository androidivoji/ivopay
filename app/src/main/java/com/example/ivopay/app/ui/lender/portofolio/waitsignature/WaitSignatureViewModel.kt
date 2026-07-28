package com.example.ivopay.app.ui.lender.portofolio.waitsignature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WaitSignatureViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WaitSignatureUiState())
    val uiState: StateFlow<WaitSignatureUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    // Ambil data kontrak menunggu tanda tangan (yto = 1)
    fun getContracts(onUpdateCount: (Int) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Simulasi pemanggilan API _getBorrowerOrderList({ yto: 1 })
                val mockList = listOf(
                    WaitSignOrderItem(
                        odi = "SIGN_001",
                        sno = "INV-2026-001",
                        toa = 5,
                        tpa = 10000000.0,
                        abn = listOf("Budi Santoso", "Siti Aminah")
                    ),
                    WaitSignOrderItem(
                        odi = "SIGN_002",
                        sno = "INV-2026-002",
                        toa = 2,
                        tpa = 4000000.0,
                        abn = listOf("PT Maju Bersama", "Rian Hidayat")
                    )
                )

                _uiState.update {
                    it.copy(
                        contractLists = mockList,
                        isLoading = false
                    )
                }

                // Emisi callback update badge count ke parent tab
                onUpdateCount(mockList.size)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Toggle pilihan item
    fun toggleSelectItem(odi: String) {
        _uiState.update { currentState ->
            val updatedList = currentState.contractLists.map { item ->
                if (item.odi == odi) item.copy(isSelect = !item.isSelect) else item
            }
            currentState.copy(contractLists = updatedList)
        }
    }

    // Validasi & Buka Modal Konfirmasi Tanda Tangan
    fun prepareBatchSign() {
        val selectedItems = _uiState.value.contractLists.filter { it.isSelect }
        if (selectedItems.isEmpty()) {
            _uiState.update { it.copy(toastMessage = "Silakan pilih pesanan") }
            return
        }

        _uiState.update {
            it.copy(
                showSignAllModal = true,
                isAgreementChecked = false
            )
        }
    }

    fun toggleAgreementCheck() {
        _uiState.update { it.copy(isAgreementChecked = !it.isAgreementChecked) }
    }

    fun dismissSignModal() {
        _uiState.update { it.copy(showSignAllModal = false) }
    }

    // Jalankan Proses Tanda Tangan Kolektif & Progress Simulation
    fun executeSignItems() {
        if (!_uiState.value.isAgreementChecked) {
            _uiState.update { it.copy(toastMessage = "Silahkan centang") }
            return
        }

        _uiState.update {
            it.copy(
                showSignAllModal = false,
                showSignProgressModal = true,
                signProgressPercent = 0f
            )
        }

        // Timer progress bar (1000ms increment)
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            var currentPercent = 0f
            while (currentPercent < 1.0f) {
                delay(1000)
                currentPercent += 0.10f
                _uiState.update { it.copy(signProgressPercent = currentPercent.coerceAtMost(1.0f)) }
            }

            // Selesai -> Reset & Reload Data
            _uiState.update {
                it.copy(
                    showSignProgressModal = false,
                    signProgressPercent = 0f
                )
            }
            getContracts()
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // Mengambil daftar nama borrower unik dari pesanan terpilih
    fun getSelectedBorrowerNames(): List<String> {
        return _uiState.value.contractLists
            .filter { it.isSelect }
            .flatMap { it.abn }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
    }
}