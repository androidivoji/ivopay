package com.example.ivopay.app.ui.loan

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.OtherProductItem
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class OtherProductViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("other_products_prefs", Context.MODE_PRIVATE)

    var productList by mutableStateOf<List<OtherProductItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    var baseBorrowerCounts: List<Int> = emptyList()

    val totalBorrowers: Int
        get() = productList.sumOf { it.borrowerCount }

    fun init() {
        getProductList()
        uploadEvent("OP5")
    }

    private fun getProductList() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getOtherProductList()
                if (response.isSuccessful && response.body()?.code == 1) {
                    val list = response.body()?.data ?: emptyList()
                    productList = list
                    initBorrowerCounts()
                    startHourlyUpdate()
                }
            } catch (e: Exception) {
                Log.e("OtherProductVM", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun initBorrowerCounts() {
        if (productList.isEmpty()) return

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("borrower_count_date", "")
        val savedCountsJson = prefs.getString("borrower_counts", "")

        if (savedDate != today || savedCountsJson.isNullOrEmpty()) {
            val newCounts = productList.mapIndexed { index, _ ->
                if (index < 2) Random.nextInt(4000, 5000)
                else Random.nextInt(3000, 4000)
            }
            prefs.edit().apply {
                putString("borrower_count_date", today)
                putString("borrower_counts", gson.toJson(newCounts))
                apply()
            }
            baseBorrowerCounts = newCounts
        } else {
            val type = object : TypeToken<List<Int>>() {}.type
            baseBorrowerCounts = gson.fromJson(savedCountsJson, type)
        }
        updateBorrowerCounts(0)
    }

    private fun updateBorrowerCounts(increase: Int) {
        val updatedList = productList.mapIndexed { index, item ->
            val base = baseBorrowerCounts.getOrElse(index) { 0 }
            item.copy(borrowerCount = base + increase)
        }
        productList = updatedList
    }

    private fun startHourlyUpdate() {
        viewModelScope.launch {
            while (true) {
                delay(60 * 60 * 1000)
                val increase = Random.nextInt(10, 50)
                updateBorrowerCounts(increase)
            }
        }
    }

    fun jumpProduct(url: String?, onOpenUrl: (String) -> Unit) {
        if (url == null) return
        uploadEvent("OP7")
        
        var targetUrl = url
        if (targetUrl.contains("{CLICK_ID}")) {
            val tkn = sessionManager.getAuthToken() ?: ""
            val prefix = if (tkn.length >= 8) tkn.substring(0, 8) else tkn
            val clickId = prefix + CommonUtils.generateRandomString(8)
            targetUrl = targetUrl.replace("{CLICK_ID}", clickId)
        }
        
        onOpenUrl(targetUrl)
    }

    private fun uploadEvent(evme: String) {
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("evme", evme)
                    addProperty("eval", "1")
                    addProperty("spe", "h")
                }
                NetworkClient.apiService.uploadEvent(body)
            } catch (e: Exception) {}
        }
    }
}
