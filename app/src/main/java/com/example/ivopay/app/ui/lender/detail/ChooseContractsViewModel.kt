package com.example.ivopay.app.ui.lender.detail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChooseContractsUiState(
    val mdi: String = "",
    val cno: String = ""
)

class ChooseContractsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseContractsUiState())
    val uiState: StateFlow<ChooseContractsUiState> = _uiState.asStateFlow()

    fun initData(mdi: String?, cno: String?) {
        _uiState.update {
            it.copy(
                mdi = mdi ?: "",
                cno = cno ?: ""
            )
        }
    }
}