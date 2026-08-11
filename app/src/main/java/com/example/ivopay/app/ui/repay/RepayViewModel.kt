package com.example.ivopay.app.ui.repay

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.data.model.PayMethod
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class RepayViewModel(context: Context) : ViewModel() {
    var repayBill by mutableStateOf<LoanOrder?>(null)
    var payList by mutableStateOf<List<PayMethod>>(emptyList())
    var loamAmount by mutableStateOf(0L)
    var curPayCode by mutableStateOf("")
    var curPay by mutableStateOf<PayMethod?>(null)
    var curPayName by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    
    // UI States
    var showPayCodePop by mutableStateOf(false)
    var showPayCodePop2 by mutableStateOf(false)
    var showRepayProgressPop by mutableStateOf(false)
    
    var prePay by mutableStateOf(false)
    var curPayFlag by mutableStateOf(false)

    fun init(billJson: String, isPrePay: Boolean, isCurPay: Boolean) {
        prePay = isPrePay
        curPayFlag = isCurPay
        val bill = Gson().fromJson(billJson, LoanOrder::class.java)
        repayBill = bill
        
        loamAmount = if (prePay) bill.uda else bill.csp
        
        if (bill.fullPaymentCode != null && bill.fullPaymentCode.isNotEmpty()) {
            payList = bill.fullPaymentCode
        } else {
            getPayCodeWays()
        }
    }

    private fun getPayCodeWays() {
        val bill = repayBill ?: return
        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("spe", if (bill.gie == "n") "h" else "")
                    addProperty("credit_type", getCashType(bill))
                    addProperty("noc", bill.noc)
                }
                val response = NetworkClient.apiService.getPayCodeWays(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.getAsJsonObject("data")
                    val vaList = data?.getAsJsonArray("full_payment_code")
                    if (vaList != null) {
                        payList = Gson().fromJson(vaList, object : com.google.gson.reflect.TypeToken<List<PayMethod>>() {}.type)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun getCashType(bill: LoanOrder): Int {
        return when (bill.yep) {
            "ciub" -> 3
            "rta2" -> 4
            else -> 1
        }
    }

    fun onRepayClick(item: PayMethod) {
        curPayCode = ""
        curPayName = item.name ?: ""
        curPay = item
        if (!item.code.isNullOrEmpty()) {
            curPayCode = item.code
            showPayCodePop = true
        } else {
            showPayCodePop2 = true
        }
    }

    fun getDKPayCode(onToast: (String) -> Unit) {
        val bill = repayBill ?: return
        if (loamAmount <= 0) {
            onToast("Masukkan jumlah pembayaran")
            return
        }
        val maxValue = if (prePay) bill.uda else bill.csp
        if (loamAmount > maxValue) {
            onToast("Jangan melebihi jumlah pembayaran maksimum")
            return
        }
        
        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("spe", if (bill.gie == "n") "h" else "")
                    addProperty("credit_no", bill.noc)
                    // Gunakan payment_method jika ada, jika tidak gunakan name sebagai fallback
                    val method = curPay?.paymentMethod ?: curPay?.name ?: ""
                    addProperty("payment_method", method)
                    addProperty("amount", loamAmount)
                }
                val response = NetworkClient.apiService.getDynamicPayCode(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.getAsJsonObject("data")
                    curPayCode = data?.get("payment_code")?.asString ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}
