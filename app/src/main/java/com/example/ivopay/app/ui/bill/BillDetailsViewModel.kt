package com.example.ivopay.app.ui.bill

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.*
import com.example.ivopay.app.util.CommonUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class BillDetailsViewModel(context: Context) : ViewModel() {
    private val sessionManager = com.example.ivopay.app.util.SessionManager(context)
    
    var curBill by mutableStateOf<LoanOrder?>(null)
    var isLoading by mutableStateOf(false)
    var billDetailList by mutableStateOf<List<Pair<String, String>>>(emptyList())
    
    // UI States
    var showModifyBank by mutableStateOf(false)
    var showSignFeePop by mutableStateOf(false)
    var showExtensionPop by mutableStateOf(false)
    var selectedExtension by mutableStateOf<ExtensionOption?>(null)

    // Bank Info for Modify
    var bankBkm by mutableStateOf("")
    var bankBkan by mutableStateOf("")
    var bankBaut by mutableStateOf("")

    fun init(noc: String) {
        if (curBill == null || curBill?.noc != noc) {
            fetchBillDetails(noc)
        }
    }

    fun isShowRepayBtn(asu: Int): Boolean {
        return asu in listOf(301, 302, 303, 801, 802, 803, 800, 804)
    }

    fun isShowExtensionApplyBtn(asu: Int): Boolean {
        return asu in listOf(800301, 800302, 800303, 301, 302, 303)
    }

    fun isShowPayCountDown(asu: Int): Boolean {
        return asu == 801
    }

    private fun fetchBillDetails(noc: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getBorrowerLoanList(JsonObject())
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.getAsJsonObject("data")
                    val ois = data?.getAsJsonArray("ois")
                    val billElement = ois?.find { it.asJsonObject.get("noc").asString == noc }
                    
                    if (billElement != null) {
                        var bill = Gson().fromJson(billElement, LoanOrder::class.java)
                        
                        // --- DUMMY INJECTION (Hanya asu_800) ---
                        // Jika dari server asu800 kosong/null, kita suntikkan dummy agar bisa tes UI
                        if (bill.asu800.isNullOrEmpty()) {
                            bill = bill.copy(
                                asu800 = listOf(
                                    ExtensionOption(dferePeo = 7, dfereTma = 35000, dfereDud = "30/09/2026"),
                                    ExtensionOption(dferePeo = 14, dfereTma = 70000, dfereDud = "07/10/2026"),
                                    ExtensionOption(dferePeo = 21, dfereTma = 105000, dfereDud = "14/10/2026"),
                                    ExtensionOption(dferePeo = 30, dfereTma = 150000, dfereDud = "23/10/2026")
                                )
                            )
                        }
                        // ---------------------------------------

                        curBill = bill
                        
                        // Prep Bank Info
                        bankBkm = bill.dbi?.bkm ?: ""
                        bankBkan = bill.dbi?.bkan ?: ""
                        bankBaut = bill.dbi?.baut ?: ""
                        
                        parseBillData(bill)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun parseBillData(bill: LoanOrder) {
        val list = mutableListOf<Pair<String, String>>()
        
        list.add("Tenor Pinjaman" to (bill.peoGfd ?: "${bill.peo} Hari"))
        list.add("Waktu Pengajuan" to (bill.ade ?: ""))
        list.add("Bank Penerima" to (bill.dbi?.bkm ?: ""))
        
        if (bill.ife > 0L) {
            val key = if (sessionManager.getActStatus() == "0") "Biaya Layanan(3%/bulan,Apr:36.0%)" else "Biaya Layanan"
            list.add(2, key to CommonUtils.formatRupiah(bill.ife.toDouble()))
        }

        if ((bill.sam ?: 0L) > 0) {
            list.add("Biaya Admin Platform" to CommonUtils.formatRupiah(bill.sam.toDouble()))
        }
        
        val showRepay = isShowRepayBtn(bill.asu)
        if (showRepay) {
            list.add(2, "Nilai Pinjaman" to CommonUtils.formatRupiah(bill.tma.toDouble()))
            list.add(3, "Tanggal Pembayaran" to (bill.dud ?: ""))
        }
        
        // Perbaikan: Munculkan denda jika status Overdue (303) dan nilai ltf > 0
        if (bill.asu == 303 && bill.ltf > 0L) {
            list.add(2, "Denda telat bayar" to CommonUtils.formatRupiah(bill.ltf.toDouble()))
        }
        
        billDetailList = list
    }

    fun applyExtension(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val selected = selectedExtension ?: return
        isLoading = true
        viewModelScope.launch {
            try {
                val params = JsonObject().apply {
                    addProperty("noc", curBill?.noc)
                    addProperty("dfere_peo", selected.dferePeo)
                    addProperty("dfere_tma", selected.dfereTma)
                    addProperty("dfere_dud", selected.dfereDud)
                    addProperty("spe", "h")
                }
                
                val response = NetworkClient.apiService.applyExtension(params) 
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    showExtensionPop = false
                    onSuccess()
                } else {
                    onError(response.body()?.get("msg")?.asString ?: "Gagal mengajukan penundaan")
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan")
            } finally {
                isLoading = false
            }
        }
    }
}
