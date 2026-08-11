package com.example.ivopay.app.ui.bill

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.util.CommonUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class BillDetailsViewModel(context: Context) : ViewModel() {
    var curBill by mutableStateOf<LoanOrder?>(null)
    var isLoading by mutableStateOf(false)
    var billDetailList by mutableStateOf<List<Pair<String, String>>>(emptyList())
    
    // UI States
    var showModifyBank by mutableStateOf(false)
    var showSignFeePop by mutableStateOf(false)

    fun init(noc: String) {
        if (curBill == null || curBill?.noc != noc) {
            fetchBillDetails(noc)
        }
    }

    private fun fetchBillDetails(noc: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                // Fetch fresh list and find the bill
                val response = NetworkClient.apiService.getBorrowerLoanList(JsonObject())
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.getAsJsonObject("data")
                    val ois = data?.getAsJsonArray("ois")
                    val billJson = ois?.find { it.asJsonObject.get("noc").asString == noc }
                    
                    if (billJson != null) {
                        val bill = Gson().fromJson(billJson, LoanOrder::class.java)
                        curBill = bill
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
        
        if ((bill.sam ?: 0L) > 0) {
            list.add("Biaya Admin Platform" to CommonUtils.formatRupiah(bill.sam.toDouble()))
        }
        
        // Status checks for repayment time, etc.
        val isInUse = bill.asu in listOf(301, 303, 302, 800301, 800302, 800303, 802, 801)
        if (isInUse) {
            list.add(2, "Nilai Pinjaman" to CommonUtils.formatRupiah(bill.tma.toDouble()))
            list.add(3, "Tanggal Pembayaran" to (bill.dud ?: ""))
        }
        
        billDetailList = list
    }
}
