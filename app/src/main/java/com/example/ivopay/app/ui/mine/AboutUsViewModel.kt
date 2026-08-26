package com.example.ivopay.app.ui.mine

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.BorrowerCmeData
import com.example.ivopay.app.data.model.BorrowerHomeResponse
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class AboutUsViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var cmeData by mutableStateOf<BorrowerCmeData?>(null)
    var isLoading by mutableStateOf(false)

    val email: String
        get() = if (cmeData?.usv == true) "customer@ivoji.id" else "help@ivoji.id"

    val hotline: String
        get() = "021-39506655"

    val csLink: String
        get() = if (cmeData?.usv == true) "https://vue.comm100.com/chatwindow.aspx?siteId=90004963&planId=77926765-6cc9-46f4-8c5a-c65dd3c907ef#" else "https://vue.comm100.com/chatwindow.aspx?siteId=90005039&planId=1cb69d63-7a69-4838-80d9-af5c7b661a03#"

    val showSocialTabs: Boolean
        get() = cmeData?.usv == true || !sessionManager.isUserLoggedIn() || cmeData?.uico == false

    fun init() {
        if (sessionManager.isUserLoggedIn()) {
            fetchHomeData()
        }
    }

    private fun fetchHomeData() {
        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("spe", "h")
                }
                val response = NetworkClient.apiService.postMgeaBorrower(requestBody)
                if (response.isSuccessful) {
                    val bodyString = response.body()?.toString()
                    val responseObj = gson.fromJson(bodyString, BorrowerHomeResponse::class.java)
                    if (responseObj?.code == 1) {
                        cmeData = responseObj.data?.cme
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}
