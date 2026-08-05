package com.example.ivopay.app.ui.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.LoginData
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.SecurityUtils
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class GestureLoginViewModel(private val context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)

    var phoneNumber by mutableStateOf("")
    var infoTips by mutableStateOf("")
    var isTipsError by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    
    // Popup states
    var showLoginMethodPop by mutableStateOf(false)
    var showLoginTipPop by mutableStateOf(false)
    var inmText by mutableStateOf("")
    
    private var loginData: LoginData? = null

    fun init(phone: String) {
        this.phoneNumber = phone
    }

    fun requestGestureLogin(pattern: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (pattern.length < 5) {
            isTipsError = true
            infoTips = "Menggambar gagal, hubungkan setidaknya 5 titik"
            return
        }

        isLoading = true
        isTipsError = false
        infoTips = ""

        viewModelScope.launch {
            try {
                // Enkripsi pattern gesture sesuai logika Vue (encodeGesture)
                val encryptedPattern = SecurityUtils.encodeGesture(pattern)
                
                val requestBody = JsonObject().apply {
                    addProperty("gede", encryptedPattern)
                    addProperty("mob", phoneNumber)
                }

                val response = NetworkClient.apiService.gestureLogin(requestBody)
                isLoading = false

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1 && body.data != null) {
                        val data = body.data
                        loginData = data
                        
                        // Store session
                        sessionManager.saveLoginSession(
                            token = data.token ?: "",
                            role = data.role ?: 0,
                            hasPgsh = data.isActive, // assuming act maps to something relevant
                            mobile = data.mobile ?: phoneNumber
                        )
                        sessionManager.saveSavedPhoneNumber(data.mobile ?: phoneNumber)
                        
                        if (data.lostStatus == "3") {
                            inmText = data.inm ?: ""
                            showLoginTipPop = true
                        } else {
                            determineNextRoute(onSuccess)
                        }
                    } else {
                        isTipsError = true
                        infoTips = body?.msg ?: "Login gagal"
                    }
                } else {
                    isTipsError = true
                    infoTips = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                isLoading = false
                isTipsError = true
                infoTips = e.message ?: "Koneksi bermasalah"
            }
        }
    }

    fun determineNextRoute(onNavigate: (String) -> Unit) {
        val data = loginData ?: return
        
        // Match Vue logic: this.loginData.act ? 'main' : role == '1' ? (uico ? 'l_main' : 'LenderBasicInfo') : 'main'
        if (data.isActive) {
            onNavigate(Screen.Main)
        } else if (data.role == 1) {
            if (data.isUserInfoCompleted) {
                onNavigate(Screen.LenderMain)
            } else {
                onNavigate(Screen.LenderBasicInfo)
            }
        } else {
            onNavigate(Screen.Main)
        }
    }
}
