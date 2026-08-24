package com.example.ivopay.app.ui.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import kotlinx.coroutines.launch

class UseAgreementViewModel : ViewModel() {
    var htmlText by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun getAppPrivacy() {
        isLoading = true
        viewModelScope.launch {
            try {
                // and the Vue code logic which uses a GET request.
                val response = NetworkClient.apiService.getUserAgreement()
                if (response.isSuccessful) {
                    htmlText = response.body()?.string() ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}
