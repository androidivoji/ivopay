package com.example.ivopay.app.ui.lender.portofolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LenderPortfolioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LenderPortfolioUiState())
    val uiState: StateFlow<LenderPortfolioUiState> = _uiState.asStateFlow()

    init {
        fetchRechargeBadgeCount()
    }

    fun onTabSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
    }

    // Menggantikan fungsi getContracts2() pada Vue
    fun fetchRechargeBadgeCount() {
        viewModelScope.launch {
            // Simulasi API _getBorrowerOrderList(yto = 2)
            val mockCount = 2 // Contoh data dari API
            _uiState.value = _uiState.value.copy(toRechargeCount = mockCount)
        }
    }

    // Callback pengganti @updateCount dari child tab 1
    fun updateWaitSignCount(count: Int) {
        _uiState.value = _uiState.value.copy(waitSignCount = count)
    }

    // Callback pengganti @updateCount dari child tab 2
    fun updateRechargeCount(count: Int) {
        _uiState.value = _uiState.value.copy(toRechargeCount = count)
    }
}