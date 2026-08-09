package com.example.ivopay.app.ui.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.*
import com.example.ivopay.app.util.SessionManager
import com.example.ivopay.app.util.SystemBridge
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

/**
 * ViewModel untuk Home Borrower dengan alur hit API berurutan (Sequential).
 */
class BorrowerHomeViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val systemBridge = SystemBridge(context)
    private val gson = Gson()

    var isLoading by mutableStateOf(false)
    
    // Data dari /api/mgeaborrower
    var homeConfig by mutableStateOf<BorrowerHomeData?>(null)
    
    // Data dari /api/laey atau /api/acnt
    var cashData by mutableStateOf<CashConfigData?>(null)
    var showAmount by mutableStateOf(0L)
    
    // Data dari /api/aict
    var currentBill by mutableStateOf<LoanOrder?>(null)

    // UI flags
    var showConfirmBillPop by mutableStateOf(false)
    var showPermissionTipPop by mutableStateOf(false)
    var showUnqualifiedPop3 by mutableStateOf(false)

    val isLogin: Boolean get() = sessionManager.isUserLoggedIn()

    fun init() {
        viewModelScope.launch {
            // Urutan Hit API: 
            // 1. Get Home Data (Config)
            val configSuccess = fetchHomeData()
            
            if (configSuccess && isLogin) {
                // 2. Cek Izin
                checkPermission()
                
                // 3. Handle Logic Produk (Cash Config / Tadpole) secara berurutan
                fetchBorrowerProductLogic(homeConfig)
                
                // 4. Get Loan List (Daftar Pinjaman Aktif)
                fetchLoanListWithFallback()
            }
        }
    }

    private suspend fun fetchHomeData(): Boolean {
        isLoading = true
        return try {
            val requestBody = JsonObject().apply {
                addProperty("spe", "h")
            }
            val response = NetworkClient.apiService.postMgeaBorrower(requestBody)
            isLoading = false
            
            if (response.isSuccessful) {
                val bodyString = response.body()?.toString()
                val responseObj = gson.fromJson(bodyString, BorrowerHomeResponse::class.java)
                
                if (responseObj?.code == 1) {
                    homeConfig = responseObj.data
                    // Simpan status pgsh
                    responseObj.data?.cme?.pgsh?.let { sessionManager.savePgshStatus(it) }
                    true
                } else false
            } else false
        } catch (e: Exception) {
            isLoading = false
            Log.e("HomeViewModel", "fetchHomeData error: ${e.message}")
            false
        }
    }

    private suspend fun fetchBorrowerProductLogic(data: BorrowerHomeData?) {
        val cme = data?.cme
        val fcoa = data?.fcoa
        val tnpo = data?.tnpo

        if (cme?.wof == false) {
            if (fcoa?.psw == 1) {
                fetchCashConfig(JsonObject().apply { addProperty("spe", "h") })
            } else if (tnpo?.psw == 1) {
                fetchTadPHomeData(JsonObject().apply { addProperty("spe", "h") })
            }
        } else {
            fetchCashConfig(JsonObject())
        }
    }

    private suspend fun fetchCashConfig(params: JsonObject) {
        try {
            val response = NetworkClient.apiService.getHomeCashConfig(params)
            if (response.isSuccessful) {
                val bodyString = response.body()?.toString()
                val responseObj = gson.fromJson(bodyString, CashConfigResponse::class.java)
                if (responseObj?.code == 1) {
                    cashData = responseObj.data
                    showAmount = responseObj.data?.atma ?: 0L
                }
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "fetchCashConfig error: ${e.message}")
        }
    }

    private suspend fun fetchTadPHomeData(params: JsonObject) {
        try {
            val response = NetworkClient.apiService.getTadpoleHomeData(params)
            if (response.isSuccessful) {
                val bodyString = response.body()?.toString()
                val responseObj = gson.fromJson(bodyString, CashConfigResponse::class.java)
                if (responseObj?.code == 1) {
                    cashData = responseObj.data
                    showAmount = responseObj.data?.atma ?: 0L
                }
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "fetchTadPHomeData error: ${e.message}")
        }
    }

    private suspend fun fetchLoanListWithFallback() {
        // Tahap 1: Hit dengan { spe: 'h' }
        val foundInPrimary = fetchLoanList(JsonObject().apply { addProperty("spe", "h") })
        
        if (!foundInPrimary) {
            // Tahap 2: Hit tanpa parameter (Other List) jika tidak ditemukan di tahap 1
            fetchLoanList(JsonObject())
        }
    }

    private suspend fun fetchLoanList(params: JsonObject): Boolean {
        return try {
            val response = NetworkClient.apiService.getBorrowerLoanList(params)
            if (response.isSuccessful) {
                val bodyString = response.body()?.toString()
                val responseObj = gson.fromJson(bodyString, LoanListResponse::class.java)
                if (responseObj?.code == 1) {
                    val orders = responseObj.data?.orders
                    val activeBill = orders?.find { getCurrentBill(it) }
                    if (activeBill != null) {
                        currentBill = activeBill
                        true
                    } else false
                } else false
            } else false
        } catch (e: Exception) {
            Log.e("HomeViewModel", "fetchLoanList error: ${e.message}")
            false
        }
    }

    private fun getCurrentBill(bill: LoanOrder): Boolean {
        val asu = bill.asu
        // Menambahkan wof_e dan status 601 sesuai response terbaru
        if (bill.yep == "cash_credit" || bill.yep == "tloan" || bill.yep == "wof_e") {
            return asu == 1 || asu == 2 || asu == 3 || asu == 4 || asu == 6 || asu == 5 || asu == 7 || asu == 8 || asu == 601
        }
        return false
    }

    private fun checkPermission() {
        if (!systemBridge.hasLocationPermission()) {
             showPermissionTipPop = true
        }
    }

    fun onApplyClick(onNavigate: (String) -> Unit) {
        if (currentBill != null) {
            // Toast: "Anda memiliki pesanan sedang diproses..."
            return
        }
        if (cashData?.koc == true) {
            showUnqualifiedPop3 = true
            return
        }
        
        if (homeConfig?.cme?.wof == false) {
             if (homeConfig?.fcoa?.psw == 1) {
                 onNavigate("CashLoan")
             } else if (homeConfig?.tnpo?.psw == 1) {
                 onNavigate("TadpoleCash")
             } else {
                 onNavigate("ApplyLoan")
             }
        } else {
            onNavigate("ApplyLoan")
        }
    }
}
