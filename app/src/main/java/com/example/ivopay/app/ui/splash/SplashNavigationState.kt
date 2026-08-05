package com.example.ivopay.app.ui.splash

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
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

    fun judgeAndJumpDummy() {
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

    fun judgeAndJump() {
        viewModelScope.launch {
            _navigationState.value = SplashNavigationState.Loading

            // 1. Hit API POST /v2/api/mgea dan dapatkan status pgsh
            val hasPgsh = hitMgeaApi()

            // 2. Tahan splash minimal 2 detik
            delay(2000)

            // 3. Tentukan arah navigasi berdasarkan status login dan pgsh
            val isLoggedIn = sessionManager.isUserLoggedIn()
            val role = sessionManager.getUserRole()

            if (isLoggedIn) {
                if (role == 1) {
                    _navigationState.value = SplashNavigationState.GoToLMain
                } else {
                    _navigationState.value = SplashNavigationState.GoToMain
                }
            } else {
                if (hasPgsh) {
                    _navigationState.value = SplashNavigationState.GoToSelectRole
                } else {
                    _navigationState.value = SplashNavigationState.GoToMain
                }
            }
        }
    }

    private suspend fun hitMgeaApi(): Boolean {
        var pgshResult = false
        try {
            Log.d("MGEA_TEST", "Memulai Hit API POST ke https://devapi.ivoji.id/v2/api/mgea ...")

            val requestBody = JsonObject().apply {
                addProperty("a", "ivoji") // Parameter wajib sesuai server log
            }

            val response = NetworkClient.apiService.postMgea(requestBody)

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("MGEA_TEST", "SUCCESS (${response.code()}): $body")
                
                // Ambil pgsh dari data response asli
                pgshResult = body?.data?.cme?.pgsh ?: false
                sessionManager.savePgshStatus(pgshResult)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("MGEA_TEST", "FAILED (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            Log.e("MGEA_TEST", "ERROR/EXCEPTION: ${e.message}", e)
        }
        return pgshResult
    }
}
