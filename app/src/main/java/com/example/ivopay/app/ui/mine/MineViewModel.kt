package com.example.ivopay.app.ui.mine

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.UserInfoData
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class MineViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)

    var userInfo by mutableStateOf<UserInfoData?>(null)
    var isLoading by mutableStateOf(false)
    var mobile by mutableStateOf(sessionManager.getMobileNumber() ?: "")
    var userName by mutableStateOf(sessionManager.getUserFullName() ?: "Pengguna")

    init {
        if (sessionManager.isUserLoggedIn()) {
            refreshCustomerInfo()
        }
    }

    fun refreshCustomerInfo() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getUserInfo(JsonObject().apply { addProperty("spe", "h") })
                isLoading = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1 && body.data != null) {
                        userInfo = body.data
                        val phone = body.data.customer?.personalInfo?.mob
                        if (!phone.isNullOrEmpty()) {
                            mobile = phone
                            sessionManager.saveMobileNumber(phone)
                        }
                        val name = body.data.customer?.personalInfo?.fullName
                        if (!name.isNullOrEmpty()) {
                            userName = name
                            sessionManager.saveUserFullName(name)
                        }
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                e.printStackTrace()
            }
        }
    }

    val isLoggedIn: Boolean get() = sessionManager.isUserLoggedIn()
}
