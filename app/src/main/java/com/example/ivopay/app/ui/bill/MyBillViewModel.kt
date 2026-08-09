package com.example.ivopay.app.ui.bill

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.LoanListResponse
import com.example.ivopay.app.data.model.LoanOrder
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MyBillViewModel : ViewModel() {
    private val gson = Gson()

    var billList by mutableStateOf<List<LoanOrder>>(emptyList())
    var isRefreshing by mutableStateOf(false)

    init {
        getLoanList()
    }

    fun getLoanList() {
        isRefreshing = true
        viewModelScope.launch {
            try {
                // Meniru logika Vue: hit 3 API (spe:'h', empty params, dan old bill)
                val primaryReq = async { fetchBills(JsonObject().apply { addProperty("spe", "h") }) }
                val secondaryReq = async { fetchBills(JsonObject()) }
                val oldBillsReq = async { fetchOldBills() }

                val results = awaitAll(primaryReq, secondaryReq, oldBillsReq)
                
                // Gabungkan semua hasil ois (orders) tanpa duplikat berdasarkan noc
                val allOrders = results.flatten().distinctBy { it.noc }
                billList = allOrders
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    private suspend fun fetchBills(params: JsonObject): List<LoanOrder> {
        return try {
            val response = NetworkClient.apiService.getBorrowerLoanList(params)
            if (response.isSuccessful) {
                val bodyString = response.body()?.toString()
                val responseObj = gson.fromJson(bodyString, LoanListResponse::class.java)
                responseObj?.data?.orders ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchOldBills(): List<LoanOrder> {
        return try {
            val response = NetworkClient.apiService.getOldBillList(JsonObject())
            if (response.isSuccessful) {
                val bodyString = response.body()?.toString()
                val responseObj = gson.fromJson(bodyString, LoanListResponse::class.java)
                responseObj?.data?.orders ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun cancelBill(noc: String) {
        // Implementasi pembatalan bill jika diperlukan
    }
}
