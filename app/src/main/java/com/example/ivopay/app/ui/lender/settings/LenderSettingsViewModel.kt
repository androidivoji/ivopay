package com.example.ivopay.app.ui.lender.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingMenuItem(
    val title: String,
    val route: String
)

data class LenderSettingsUiState(
    val name: String = "",
    val mobile: String = "",
    val appVersion: String = "1.0.0",
    val isLoggedIn: Boolean = false,
    val menuItems: List<SettingMenuItem> = emptyList()
)

class LenderSettingsViewModel(private val context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)

    private val _uiState = MutableStateFlow(LenderSettingsUiState())
    val uiState: StateFlow<LenderSettingsUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val isLoggedIn = sessionManager.isUserLoggedIn()
            val savedMob = sessionManager.getMobileNumber() ?: ""

            // Inisialisasi daftar menu sesuai jumpList di Vue
            val items = listOf(
                SettingMenuItem("My Profile", "lender_basic_info"),
                SettingMenuItem("My Contracts", "my_contracts"),
                SettingMenuItem("Contact US", "about_us"),
                SettingMenuItem("Privacy Policy", "privacy_policy"),
                SettingMenuItem("Terms & Use", "use_agreement")
            )

            _uiState.value = _uiState.value.copy(
                isLoggedIn = isLoggedIn,
                mobile = savedMob,
                menuItems = items,
                appVersion = getAppVersion()
            )

            if (isLoggedIn) {
                fetchLenderUserInfo()
            }
        }
    }

    private fun fetchLenderUserInfo() {
        viewModelScope.launch {
            try {
                // Hit API _getLenderUserInfo (M_U_I: v1/api/cugo)
                val response = NetworkClient.apiService.getLenderUserInfo(JsonObject())
                
                if (response.isSuccessful) {
                    val resData = response.body()?.data
                    val fullName = resData?.personalInfo?.fullName
                    
                    if (!fullName.isNullOrEmpty()) {
                        _uiState.update { it.copy(name = fullName) }
                        // Opsional: Simpan ke session jika diperlukan
                        sessionManager.saveUserFullName(fullName)
                    }
                }
            } catch (e: Exception) {
                // Handle error silent
            }
        }
    }

    private fun getAppVersion(): String {
        return com.example.ivopay.app.util.SystemBridge(context).getAppVersion()
    }
}