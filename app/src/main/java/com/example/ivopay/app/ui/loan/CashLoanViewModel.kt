package com.example.ivopay.app.ui.loan

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.*
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class CashLoanViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var cashData by mutableStateOf<AmountCashConfigData?>(null)
    var amountIdx by mutableIntStateOf(0)
    var dayIdx by mutableIntStateOf(0)
    var isLoading by mutableStateOf(false)
    
    var showSignPop by mutableStateOf(false)
    var signImageBase64 by mutableStateOf("")

    val curDayOption: AmountTimeOption?
        get() = cashData?.tpos?.getOrNull(dayIdx)

    val curLoanOption: AmountLoanOption?
        get() = curDayOption?.dop?.getOrNull(amountIdx)

    val selAmount: Long
        get() = curLoanOption?.tma ?: 0L

    val maxAmountIndex: Int
        get() = (curDayOption?.dop?.size ?: 1) - 1

    fun getLoanOptionByIndex(index: Int): AmountLoanOption? {
        return curDayOption?.dop?.getOrNull(index)
    }

    fun getLoanData(): List<Pair<String, String>> {
        val bio = cashData?.bio
        val curDay = curDayOption
        val curLoan = curLoanOption ?: return emptyList()

        val loanDate = if (curDay?.peoGfd != null && curDay.peoGfd != 0) {
            "${curDay.peoGfd} hari"
        } else {
            "${curDay?.peo ?: "--"} hari"
        }

        val list = mutableListOf(
            "Nama" to (bio?.bkan ?: "--"),
            "Bank Penerima" to (bio?.bkm ?: "--"),
            "Nomor Rekening" to (bio?.baut ?: "--"),
            "Nilai Pinjaman" to CommonUtils.formatRupiah(selAmount.toDouble()),
            "Tanggal Pinjaman" to loanDate,
            "Biaya Layanan" to CommonUtils.formatRupiah(curLoan.ife.toDouble()),
            "Repayment Amount" to CommonUtils.formatRupiah(curLoan.dua.toDouble())
        )

        if (cashData?.uoe == 1) {
            list.add(6, "Jumlah diterima" to CommonUtils.formatRupiah(curLoan.dam.toDouble()))
        }

        return list
    }

    fun init() {
        fetchCashConfig()
    }

    private fun fetchCashConfig() {
        isLoading = true
        viewModelScope.launch {
            try {
                val params = JsonObject().apply { addProperty("spe", "h") }
                val response = NetworkClient.apiService.getAmountCashLoanConfig(params)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1) {
                        cashData = body.data
                        amountIdx = body.data?.dtma ?: 0
                        dayIdx = body.data?.dpeo ?: 0
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun onApplyClick(onNext: () -> Unit) {
        if (cashData?.nvmp == true) {
            // Should show PhoneCode dialog, but for now just show sign
            showSignPop = true
        } else {
            showSignPop = true
        }
    }

    fun submitApply(faceImageBase64: String?, onSuccess: (Boolean) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val params = JsonObject().apply {
                    addProperty("spe", "h")
                    addProperty("tma", selAmount)
                    addProperty("peo", curDayOption?.peo ?: 0)
                    // Tambahkan parameter lain dari applyInfo di Vue
                }

                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.addFormDataPart("spe", "h")
                builder.addFormDataPart("bsi", signImageBase64)
                if (!faceImageBase64.isNullOrEmpty()) {
                    builder.addFormDataPart("aig", faceImageBase64)
                }
                
                // Tambahkan field dari params ke multipart
                params.entrySet().forEach { (key, value) ->
                    builder.addFormDataPart(key, value.asString)
                }

                val response = NetworkClient.apiService.applyLoan(builder.build())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val nct = body.getAsJsonObject("data")?.getAsJsonObject("nct")
                        val needConfirm = nct?.get("cdi")?.asBoolean == true
                        onSuccess(needConfirm)
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
