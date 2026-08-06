package com.example.ivopay.app.ui.lender.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LenderHomeViewModel(private val context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)

    private val _uiState = MutableStateFlow(LenderHomeUiState())
    val uiState: StateFlow<LenderHomeUiState> = _uiState.asStateFlow()

    init {
        getLenderConfig()
    }

    fun getLenderConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simulasi/Panggilan API _getLenderConfig
            val auss = 1 // lenderStatus (0: Review, 1: Pass, 2: Reject)
            val uico = true // status profile info

            _uiState.value = _uiState.value.copy(
                lenderStatus = auss,
                uico = uico,
                isLoading = false
            )

            if (auss == 1) {
                getBorrowerList()
            }
        }
    }

    private fun getBorrowerList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val response = NetworkClient.apiService.getBorrowerList(JsonObject())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1) {
                        val borrowers = body.data?.ais ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            borrowList = borrowers,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleSelectBorrower(ati: String) {
        val updatedList = _uiState.value.borrowList.map { item ->
            if (item.ati == ati) item.copy(isSelect = !item.isSelect) else item
        }
        _uiState.value = _uiState.value.copy(borrowList = updatedList)
    }

    fun onConfirmPayBack() {
        val selectedIds = _uiState.value.borrowList.filter { it.isSelect }.map { it.ati }
        if (selectedIds.isEmpty()) {
            Toast.makeText(context, "Silakan pilih peminjam", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val requestBody = JsonObject().apply {
                    addProperty("ati", Gson().toJson(selectedIds))
                }
                val response = NetworkClient.apiService.confirmPayBack(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val su = Gson().fromJson(body.get("data"), FinanceDetail::class.java)
                        
                        val insuranceList = mutableListOf<InsuranceItem>()
                        // Tambahkan "Tanpa Asuransi" di awal (parity dengan Vue)
                        insuranceList.add(InsuranceItem("Tanpa Asuransi", 0, 0.0, 0.0))
                        su.isnc?.let { insuranceList.addAll(it) }

                        _uiState.value = _uiState.value.copy(
                            financeDetail = su,
                            insuranceList = insuranceList,
                            showSelectLoanDesc = true,
                            isLoading = false
                        )
                    } else {
                        val msg = body?.get("msg")?.asString ?: "Gagal memuat data pendanaan"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onCreateOrder(selectedInsuranceIndex: Int) {
        val selectedIds = _uiState.value.borrowList.filter { it.isSelect }.map { it.ati }
        if (selectedIds.isEmpty()) {
            Toast.makeText(context, "Silakan pilih peminjam", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showSelectLoanDesc = false)

            try {
                val requestBody = JsonObject().apply {
                    addProperty("ati", Gson().toJson(selectedIds))
                    
                    val insurance = _uiState.value.insuranceList.getOrNull(selectedInsuranceIndex)
                    if (insurance != null && insurance.ity != 0) {
                        addProperty("ity", insurance.ity)
                    }
                }

                val response = NetworkClient.apiService.createOrder(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val su = Gson().fromJson(body.get("data"), FinanceBill::class.java)
                        
                        _uiState.value = _uiState.value.copy(
                            financeBill = su,
                            showConfirmPayPop = true,
                            isLoading = false
                        )
                    } else {
                        val msg = body?.get("msg")?.asString ?: "Gagal membuat pesanan"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onConfirmPayClick() {
        _uiState.value = _uiState.value.copy(
            showConfirmPayPop = false,
            showSuccessNotify = true
        )
    }

    fun copyPayCode(code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("VA Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Salin Sukses", Toast.LENGTH_SHORT).show()
    }

    fun hideSuccessNotify() {
        _uiState.value = _uiState.value.copy(showSuccessNotify = false)
    }

    fun closeSelectLoanSheet() {
        _uiState.value = _uiState.value.copy(showSelectLoanDesc = false)
    }

    fun closeConfirmPayPop() {
        _uiState.value = _uiState.value.copy(showConfirmPayPop = false)
    }
}