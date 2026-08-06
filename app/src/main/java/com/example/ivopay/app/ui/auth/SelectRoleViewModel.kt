package com.example.ivopay.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.LenderUserInfoResponse
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class SelectRoleViewModel : ViewModel() {

    fun fetchLenderUserInfo(onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getLenderUserInfo(JsonObject())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1) {
                        // Jika su.pi ada dan su.pi.inm ada, berarti info sudah lengkap
                        val hasInm = body.data?.personalInfo?.idNumber != null
                        onSuccess(hasInm)
                    } else {
                        onError(body?.msg ?: "Unknown error")
                    }
                } else {
                    onError("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Connection error")
            }
        }
    }
}
