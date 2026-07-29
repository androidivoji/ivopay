package com.example.ivopay.app.ui.splash

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashNavigationState {
    object Loading : SplashNavigationState()
    object Error : SplashNavigationState()
    object GoToLMain : SplashNavigationState()
    object GoToMain : SplashNavigationState()
    object GoToSelectRole : SplashNavigationState()
}

class SplashViewModel(context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)

    // Menggunakan StateFlow untuk memantau status UI (Pengganti ref/showError di Vue)
    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState

    fun judgeAndJump() {
        viewModelScope.launch {
            _navigationState.value = SplashNavigationState.Loading
            try {
                val isLoggedIn = sessionManager.isUserLoggedIn()

                if (isLoggedIn) {
                    val role = sessionManager.getUserRole()
                    if (role == 1) {
                        _navigationState.value = SplashNavigationState.GoToLMain
                    } else {
                        _navigationState.value = SplashNavigationState.GoToMain
                    }
                } else {
                    fetchRoleConfig()
                }
            } catch (e: Exception) {
                _navigationState.value = SplashNavigationState.Error
            }
        }
    }

    private suspend fun fetchRoleConfig() {
        // Simulasi hit API _fetchPConfig
        // Di native nanti, panggil repository/API service kamu di sini
        val isNetworkSuccess = true // Ubah ke false jika ingin tes error screen

        if (isNetworkSuccess) {
            val hasPgsh = false // Simulasi properti su.cme.pgsh dari API
            if (hasPgsh) {
                _navigationState.value = SplashNavigationState.GoToSelectRole
            } else {
                _navigationState.value = SplashNavigationState.GoToMain
            }
        } else {
            _navigationState.value = SplashNavigationState.Error
        }
    }
}