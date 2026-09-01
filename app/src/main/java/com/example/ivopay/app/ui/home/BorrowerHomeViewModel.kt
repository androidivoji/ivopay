package com.example.ivopay.app.ui.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.*
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.SessionManager
import com.example.ivopay.app.util.SystemBridge
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

sealed class HomeActionEvent {
    object StartLoginFaceLive : HomeActionEvent()
    object StartLackinFaceLive : HomeActionEvent()
}

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
    var ci6EBill by mutableStateOf<LoanOrder?>(null)

    // Data Produk Lain (dari OtherProductScreen)
    var otherProducts by mutableStateOf<List<OtherProductItem>>(emptyList())
    var totalBorrowers by mutableIntStateOf(0)

    // UI flags
    var showConfirmBillPop by mutableStateOf(false)
    var showPermissionTipPop by mutableStateOf(false)
    var showUnqualifiedPop2 by mutableStateOf(false)
    var showUnqualifiedPop3 by mutableStateOf(false)

    // Events for Navigation or Actions
    var actionEvent by mutableStateOf<HomeActionEvent?>(null)

    val isLogin: Boolean get() = sessionManager.isUserLoggedIn()

    var showEcurEntry by mutableStateOf(false)

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

                // 5. Get Other Products (Untuk ditampilkan di bawah RecommendCard)
                fetchOtherProducts()

                // 6. Fetch CEUR Config for RecommendProductCard visibility
                fetchCeurConfig()
            }
        }
    }

    private suspend fun fetchCeurConfig() {
        try {
            val response = NetworkClient.apiService.getCommonConfig(JsonObject())
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null && data.has("psw")) {
                    showEcurEntry = data.get("psw").asInt == 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                    Log.d("XBZ", "fetchHomeData success, uico: ${responseObj.data?.cme?.uico}")
                    homeConfig = responseObj.data
                    // Simpan status pgsh, rasn, lackinA, uico
                    responseObj.data?.cme?.let {
                        sessionManager.savePgshStatus(it.pgsh)
                        
                        // Handle rasn yang bisa berupa Boolean atau Int
                        val rasnInt = when (val r = it.rasn) {
                            is Boolean -> if (r) 1 else 0
                            is Number -> r.toInt()
                            is String -> r.toIntOrNull() ?: 0
                            else -> 0
                        }
                        sessionManager.saveRasn(rasnInt)
                        
                        sessionManager.saveLackinA(it.lackinA)
                        sessionManager.saveUico(it.uico)
                        sessionManager.saveTttp(it.tttp)
                    }
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
        // Reset tagihan aktif sebelum pengecekan baru
        currentBill = null
        
        // Tahap 1: Hit dengan { spe: 'h' } (Cash Loan Utama)
        val foundInPrimary = fetchLoanList(JsonObject().apply { addProperty("spe", "h") })
        
        if (!foundInPrimary) {
            // Tahap 2: Hit tanpa parameter (Other List / Cicilan) jika tidak ditemukan di tahap 1
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
                    val orders = responseObj.data?.orders ?: emptyList()
                    
                    var foundPrimary = false
                    for (order in orders) {
                        if (getCurrentBill(order)) {
                            if (order.yep == "ci6_e") {
                                ci6EBill = order
                            } else {
                                currentBill = order
                                foundPrimary = true
                            }
                        }
                    }
                    foundPrimary
                } else false
            } else false
        } catch (e: Exception) {
            Log.e("HomeViewModel", "fetchLoanList error: ${e.message}")
            false
        }
    }

    private fun getCurrentBill(bill: LoanOrder): Boolean {
        val asu = bill.asu
        // Sinkronisasi dengan ConstData.js di Vue
        val activeStatuses = listOf(101, 203, 201, 301, 302, 303, 202, 601, 701, 801, 802, 800301, 800302, 800303)
        
        if (bill.yep == "cash_credit" || bill.yep == "tloan" || bill.yep == "wof_e" || bill.yep == "ci6_e") {
            return asu in activeStatuses
        }
        return false
    }

    private fun checkPermission() {
        if (!systemBridge.hasLocationPermission()) {
             showPermissionTipPop = true
        }
    }

    /**
     * Logika _checkInfo dari project Vue:
     * Memastikan data nasabah lengkap sebelum mengizinkan pengajuan pinjaman.
     */
    fun checkInfo(onNavigate: (String) -> Unit) {
        if (!isLogin) {
            onNavigate(Screen.Login)
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                // 1. Refresh Customer Info (GET_USER_INFO)
                val response = NetworkClient.apiService.getUserInfo(JsonObject())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1 && body.data != null) {
                        val cu = body.data
                        
                        // rpns check (Kondisi tidak memenuhi syarat)
                        if (cu.rpns) {
                            showUnqualifiedPop2 = true
                            isLoading = false
                            return@launch
                        }

                        if (cu.stagLackin != null) {
                            val stag = cu.stagLackin
                            val cme = homeConfig?.cme

                            // Alur Pengecekan Tahapan (Staging)
                            if (!stag.s1) {
                                onNavigate(Screen.BaseInfo)
                            } else if (cu.isLackinFlow == null) {
                                onNavigate(Screen.RegisterInfoWaiting)
                            } else if (cme?.lackinA == false) {
                                // Alur Normal (Non-Lackin A)
                                if (cu.isLackinFlow == false && cme.aigEp) {
                                    actionEvent = HomeActionEvent.StartLoginFaceLive
                                } else if (!stag.s3) {
                                    onNavigate(Screen.PersonalInfoV2)
                                } else if (!stag.s4) {
                                    onNavigate(Screen.ContactInfo)
                                } else if (!stag.s5) {
                                    onNavigate(Screen.JobInfoV2)
                                } else {
                                    // Semua info lengkap, kembalikan target ApplyLoan agar onApplyClick tahu bisa lanjut
                                    onNavigate(Screen.ApplyLoan)
                                }
                            } else if (cme?.lackinA == true) {
                                // Alur Lackin A
                                if (cu.aigSce == null && cu.aigNed) {
                                    actionEvent = HomeActionEvent.StartLackinFaceLive
                                } else {
                                    if (!stag.s2_a) {
                                        onNavigate(Screen.ContactInfoV2)
                                    } else if (!stag.s3_a) {
                                        onNavigate(Screen.BankInfo)
                                    } else {
                                        if (cu.isLackinFlow == true && cu.aigSce == true) {
                                            if (cu.aigNedApl) {
                                                lackinApply(onNavigate)
                                            } else {
                                                lackinCC(onNavigate)
                                            }
                                        } else {
                                            // Semua Lackin info lengkap
                                            onNavigate(Screen.ApplyLoan)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (body?.code == 6) {
                        // Error code 6: Biasanya sesi bermasalah atau butuh BaseInfo ulang
                        onNavigate(Screen.BaseInfo)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "checkInfo error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun lackinApply(onNavigate: (String) -> Unit) {
        try {
            val response = NetworkClient.apiService.lackinApply()
            if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                onNavigate(Screen.UnderReview)
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "lackinApply error: ${e.message}")
        }
    }

    private suspend fun lackinCC(onNavigate: (String) -> Unit) {
        try {
            val response = NetworkClient.apiService.lackinCC()
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.getAsJsonObject("data")
                val asu = data?.get("lackin_flow_app_asu")?.asInt
                
                if (asu == 1) {
                    onNavigate(Screen.UnderReview)
                } else if (asu == 2) {
                    val typ = data.get("lackin_flow_typ")?.asString ?: ""
                    val config = data.get("konfigurasi")?.toString() ?: ""
                    onNavigate("${Screen.A_Apply}?lackin_flow_typ=$typ&konfigurasi=$config")
                } else {
                    onNavigate(Screen.ApplyLoan)
                }
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "lackinCC error: ${e.message}")
        }
    }

    private suspend fun fetchOtherProducts() {
        try {
            val response = NetworkClient.apiService.getOtherProductList()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.code == 1 && body.data != null) {
                    otherProducts = body.data
                    totalBorrowers = (1500..3000).random()
                }
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "fetchOtherProducts error: ${e.message}")
        }
    }

    /**
     * Fungsi utama saat tombol "Ajukan" diklik.
     * Melakukan pengecekan info terlebih dahulu.
     */
    fun onApplyClick(onNavigate: (String) -> Unit, productType: String = "wof_e") {
        if (currentBill != null) return
        
        if (cashData?.koc == true) {
            showUnqualifiedPop3 = true
            return
        }

        // Jalankan pengecekan staging (BaseInfo -> Personal -> Contact -> Job)
        checkInfo { target ->
            // Jika target navigasi dari checkInfo adalah "ApplyLoan" (artinya semua step selesai),
            // maka arahkan ke produk yang spesifik sesuai productType.
            if (target == Screen.ApplyLoan) {
                performFinalNavigation(onNavigate, productType)
            } else {
                // Jika masih ada step yang kurang (misal ke BaseInfo), arahkan ke sana.
                onNavigate(target)
            }
        }
    }

    private fun performFinalNavigation(onNavigate: (String) -> Unit, productType: String) {
        Log.d("XBZ", "productType: $productType")
        val rasn = sessionManager.getRasn().toString()
        when (productType) {
            "fcoa" -> onNavigate(Screen.CashLoan) //asli
//            "fcoa" -> onNavigate(Screen.TadpoleCash)
            "tnpo" -> onNavigate(Screen.TadpoleCash)
            "ci6" -> onNavigate("Ci6Cash?rasn=$rasn")
            "ci6_w" -> onNavigate("Ci6WCash?rasn=$rasn")
            "ci7" -> onNavigate("Ci7Cash?rasn=$rasn")
            "ci8" -> onNavigate("Ci8Cash?rasn=$rasn")
            "inlg" -> onNavigate("InlgCash?rasn=$rasn")
            "rta2" -> onNavigate("CLoan16")
            "ciub" -> onNavigate("CLoan15")
//            "wof_e" -> onNavigate(Screen.CashLoan)
            "wof_e" -> onNavigate(Screen.ApplyLoan) //asli
//            "wof_e" -> onNavigate(Screen.TadpoleCash)
            else -> onNavigate(Screen.ApplyLoan)
        }
    }

    fun onJumpBillDetails(
        onNavigate: (String) -> Unit,
        bill: LoanOrder,
        config: LoanProductConfig?,
        type: String
    ) {
        if (bill.asu == 203) { // PASSED_WAIT_CONFIRM
            val nct = config?.nct
            if (nct?.cdi == true) {
                val upgrade = nct.nctUpgdData
                if (upgrade?.nctTyp == 7) {
                    val configJson = upgrade.konfigurasi?.toString() ?: ""
                    onNavigate("A_Ci7Cash?bill=$configJson")
                } else if (upgrade?.nctTyp == 11) {
                    val configJson = upgrade.konfigurasi?.toString() ?: ""
                    onNavigate("RCBWCLoan?bill=$configJson&noc=${nct.noc}&asu=${bill.asu}")
                } else {
                    if (type == "tnpo") {
                        onNavigate("TadpBWC?bill=${gson.toJson(bill)}")
                    } else if (type == "fcoa") {
                        onNavigate("BillDetailsWaitConfirm?bill=${gson.toJson(bill)}")
                    }
                }
            } else {
                onNavigate("BillDetails?bill=${bill.noc}")
            }
        } else {
            onNavigate("BillDetails?bill=${bill.noc}")
        }
    }

    fun toSignContracts(onNavigate: (String) -> Unit, bill: LoanOrder) {
        if (bill.asu == 800301) { // wait_borrow_check_kfc (estimated code)
            // showWaitKFCPop handled in Screen
            return
        }
        onNavigate("BorrowerSignContracts?noc=${bill.noc}")
    }
}
