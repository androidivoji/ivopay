package com.example.ivopay.app.ui.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.LoginData
import com.example.ivopay.app.data.model.RoleData
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.SecurityUtils
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class GestureLoginViewModel(private val context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)

    var phoneNumber by mutableStateOf("")
    var userRole by mutableStateOf(0)
    var infoTips by mutableStateOf("")
    var isTipsError by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    
    // Popup states
    var showLoginMethodPop by mutableStateOf(false)
    var showLoginTipPop by mutableStateOf(false)
    var inmText by mutableStateOf("")
    
    private var loginData: LoginData? = null

    fun init(phone: String, role: Int = 0) {
        this.phoneNumber = phone
        this.userRole = role
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

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1 && body.data != null) {
                        val data = body.data
                        loginData = data
                        
                        // Store session sesuai logika Vue
                        sessionManager.saveLoginSession(
                            token = data.token ?: "",
                            role = data.role ?: userRole,
                            hasPgsh = data.isActive, 
                            isActive = data.isActive,
                            mobile = data.mobile ?: phoneNumber
                        )
                        sessionManager.saveMobileNumber(data.mobile ?: phoneNumber)
                        sessionManager.saveSavedPhoneNumber(data.mobile ?: phoneNumber)
                        
                        if (data.lostStatus == "3") {
                            isLoading = false
                            inmText = data.inm ?: ""
                            showLoginTipPop = true
                        } else {
                            // Hit API Role (onNext logic in Vue)
                            fetchRole(onSuccess)
                        }
                    } else {
                        isLoading = false
                        isTipsError = true
                        infoTips = body?.msg ?: "Login gagal"
                    }
                } else {
                    isLoading = false
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

    private fun fetchRole(onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("spe", "h")
                    addProperty("acs", userRole.toString())
                }
                val response = NetworkClient.apiService.getRole(requestBody)
                isLoading = false
                if (response.isSuccessful) {
                    val roleData = response.body()?.data
                    determineNextRoute(onNavigate, roleData)
                } else {
                    determineNextRoute(onNavigate)
                }
            } catch (e: Exception) {
                isLoading = false
                determineNextRoute(onNavigate)
            }
        }
    }

    fun determineNextRoute(onNavigate: (String) -> Unit, roleData: RoleData? = null) {
        val data = loginData ?: return
        
        // Match Vue logic:
        // if (this.loginData.act) route('main')
        // else if (suc.acs == '1') { set role 1; if (suc.uico) route('l_main') else route('LenderBasicInfo') }
        // else route('main')
        
        if (data.isActive) {
            onNavigate(Screen.Main)
        } else if (roleData?.acs == "1") {
            // Update role to 1 (localStorage.setItem('role', '1'))
            sessionManager.saveLoginSession(
                token = data.token ?: "",
                role = 1,
                hasPgsh = data.isActive,
                isActive = data.isActive,
                mobile = data.mobile ?: phoneNumber
            )
            
            if (roleData.uico) {
                onNavigate(Screen.LenderMain)
            } else {
                onNavigate(Screen.LenderBasicInfo)
            }
        } else {
            onNavigate(Screen.Main)
        }
    }
}
