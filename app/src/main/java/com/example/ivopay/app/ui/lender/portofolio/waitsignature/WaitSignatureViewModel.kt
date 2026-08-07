package com.example.ivopay.app.ui.lender.portofolio.waitsignature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
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
                val requestBody = JsonObject().apply {
                    addProperty("yto", "1")
                }
                val response = NetworkClient.apiService.getBorrowerOrderList(requestBody)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val data = body.getAsJsonObject("data")
                        val oli = data.getAsJsonArray("oli")
                        
                        val orderList: List<WaitSignOrderItem> = Gson().fromJson(
                            oli,
                            object : TypeToken<List<WaitSignOrderItem>>() {}.type
                        )

                        _uiState.update {
                            it.copy(
                                contractLists = orderList,
                                isLoading = false
                            )
                        }

                        // Emisi callback update badge count ke parent tab
                        onUpdateCount(orderList.size)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }

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
    fun executeSignItems(onUpdateCount: (Int) -> Unit = {}) {
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

        viewModelScope.launch {
            try {
                // 1. Progress Simulation 0-40%
                var currentPercent = 0f
                while (currentPercent < 0.4f) {
                    delay(400)
                    currentPercent += 0.10f
                    _uiState.update { it.copy(signProgressPercent = currentPercent) }
                }

                // 2. Real API Call
                val selectedOdis = _uiState.value.contractLists
                    .filter { it.isSelect }
                    .map { it.odi }

                val requestBody = JsonObject().apply {
                    addProperty("odis", Gson().toJson(selectedOdis))
                }

                val response = NetworkClient.apiService.batchSignAllContracts(requestBody)

                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    // 3. Progress Simulation 40-100%
                    while (currentPercent < 1.0f) {
                        delay(400)
                        currentPercent += 0.20f
                        _uiState.update { it.copy(signProgressPercent = currentPercent.coerceAtMost(1.0f)) }
                    }
                    delay(500)

                    _uiState.update {
                        it.copy(
                            showSignProgressModal = false,
                            signProgressPercent = 0f,
                            toastMessage = "Penandatanganan batch berhasil"
                        )
                    }
                    // Refresh List
                    getContracts(onUpdateCount)
                } else {
                    val msg = response.body()?.get("msg")?.asString ?: "Gagal menandatangani kontrak"
                    _uiState.update {
                        it.copy(
                            showSignProgressModal = false,
                            toastMessage = msg
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        showSignProgressModal = false,
                        toastMessage = e.localizedMessage ?: "Terjadi kesalahan"
                    )
                }
            }
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