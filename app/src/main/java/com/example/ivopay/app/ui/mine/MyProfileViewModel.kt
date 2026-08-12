package com.example.ivopay.app.ui.mine

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.StagLackin
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

data class ProfileMenuOption(
    val txt: String,
    val name: String,
    val isFinished: Boolean
)

class MyProfileViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)

    var isLoading by mutableStateOf(false)
    var stag by mutableStateOf<StagLackin?>(null)
    var isLackinFlow by mutableStateOf<Boolean?>(null)
    var infoList by mutableStateOf<List<ProfileMenuOption>>(emptyList())
    
    val isLoggedIn: Boolean get() = sessionManager.isUserLoggedIn()
    val isLackinA: Boolean get() = sessionManager.getLackinA()
    val isUico: Boolean get() = sessionManager.getUico()

    init {
        fetchCustomerInfo()
    }

    fun fetchCustomerInfo() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getUserInfo(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1 && body.data != null) {
                        val data = body.data
                        this@MyProfileViewModel.stag = data.stagLackin
                        this@MyProfileViewModel.isLackinFlow = data.isLackinFlow
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
                buildMenu()
            }
        }
    }

    private fun buildMenu() {
        val list = mutableListOf<ProfileMenuOption>()
        val currentStag = stag ?: StagLackin()
        
        list.add(ProfileMenuOption("01 Informasi Dasar", "BaseInfo", currentStag.s1))
        
        if (isLackinFlow != null) {
            if (isLackinA) {
                list.add(ProfileMenuOption("02 Informasi Kontak", "ContactInfoV2", currentStag.s2_a))
                list.add(ProfileMenuOption("03 Rekening Kartu Bank", "BankInfo", currentStag.s3_a))
            } else {
                list.add(ProfileMenuOption("02 Informasi Pribadi", "PersonalInfoV2", currentStag.s3))
                list.add(ProfileMenuOption("03 Informasi Kontak", "ContactInfoPage", currentStag.s4))
                list.add(ProfileMenuOption("04 Informasi Pekerjaan", "JobInfoV2", currentStag.s5))
            }
        }
        infoList = list
    }
}
