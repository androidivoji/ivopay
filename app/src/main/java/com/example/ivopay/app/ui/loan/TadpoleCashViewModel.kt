package com.example.ivopay.app.ui.loan

import android.content.Context
import android.util.Log
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

class TadpoleCashViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var cashData by mutableStateOf<TadpoleCashConfigData?>(null)
    var amountIdx by mutableIntStateOf(0)
    var dayIdx by mutableIntStateOf(0)
    var ewb by mutableStateOf<List<TadpoleBillItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    
    var showSignPop by mutableStateOf(false)
    var signImageBase64 by mutableStateOf("")

    // Phone Code Logic
    var verCode by mutableStateOf("")
    var verCountDown by mutableIntStateOf(0)
    private var countDownJob: kotlinx.coroutines.Job? = null

    fun startCountDown() {
        verCountDown = 60
        countDownJob?.cancel()
        countDownJob = viewModelScope.launch {
            while (verCountDown > 0) {
                kotlinx.coroutines.delay(1000)
                verCountDown--
            }
        }
    }

    fun sendCode(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val params = JsonObject().apply { addProperty("mob", cashData?.mob ?: "") }
                val response = NetworkClient.apiService.borrowerSendCode(params)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    startCountDown()
                    onResult("Kode terkirim")
                } else {
                    onResult(response.body()?.get("msg")?.asString ?: "Gagal mengirim kode")
                }
            } catch (e: Exception) {
                onResult("Terjadi kesalahan")
            }
        }
    }

    fun checkCode(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (verCode.length < 4) {
            onError("Masukkan kode verifikasi")
            return
        }
        isLoading = true
        viewModelScope.launch {
            try {
                val params = JsonObject().apply {
                    addProperty("mob", cashData?.mob ?: "")
                    addProperty("vcd", verCode)
                }
                val response = NetworkClient.apiService.borrowerCheckCode(params)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    onSuccess()
                } else {
                    onError(response.body()?.get("msg")?.asString ?: "Kode salah")
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan")
            } finally {
                isLoading = false
            }
        }
    }

    val curDayOption: TadpoleTimeOption?
        get() = cashData?.tpos?.getOrNull(dayIdx)

    val curLoanOption: TadpoleLoanOption?
        get() = curDayOption?.dop?.getOrNull(amountIdx)

    val selAmount: Long
        get() = curLoanOption?.tma ?: 0L

    val maxAmountIndex: Int
        get() = (curDayOption?.dop?.size ?: 1) - 1

    val maxAllowedAmountIndex: Int
        get() = curDayOption?.dop?.indexOfLast { it.aow } ?: 0

    val minAmount: Long
        get() = curDayOption?.dop?.firstOrNull()?.tma ?: 0L

    val maxAmount: Long
        get() = curDayOption?.dop?.lastOrNull()?.tma ?: 0L

    fun init() {
        fetchCashConfig()
    }

    private fun fetchCashConfig() {
        isLoading = true
        viewModelScope.launch {
            try {
                val params = JsonObject().apply { addProperty("spe", "h") }
                val response = NetworkClient.apiService.getTadpoleCashConfig(params)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1) {
                        cashData = body.data
                        // Pastikan index awal tidak melebihi batas aow
                        val maxAowIdx = maxAmountIndex
                        amountIdx = (body.data?.dtma ?: 0).coerceAtMost(maxAowIdx)
                        dayIdx = body.data?.dpeo ?: 0
                        fetchTadpoleBillPre()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchTadpoleBillPre() {
        val curDay = curDayOption ?: return
        viewModelScope.launch {
            try {
                val params = JsonObject().apply {
                    addProperty("spe", "h")
                    addProperty("tma", selAmount)
                    addProperty("bpio", curDay.bpio)
                }
                val response = NetworkClient.apiService.getTadpoleBillPreview(params)
                if (response.isSuccessful && response.body()?.code == 1) {
                    ewb = response.body()?.data?.ewb ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getLoanData(): List<Pair<String, String>> {
        val bio = cashData?.bio
        val curLoan = curLoanOption ?: return emptyList()

        val list = mutableListOf(
            "Nama" to (bio?.bkan ?: "--"),
            "Bank Penerima" to (bio?.bkm ?: "--"),
            "Nomor Rekening" to (bio?.baut ?: "--"),
            "Nilai Pinjaman" to CommonUtils.formatRupiah(selAmount.toDouble()),
            "Biaya Layanan" to CommonUtils.formatRupiah(curLoan.ife.toDouble())
        )

        if (cashData?.uoe != 1) {
            list.add("Repayment Amount" to CommonUtils.formatRupiah(curLoan.dua.toDouble()))
        }

        return list
    }

    fun onApplyClick() {
        // Logika P13 Event
        showSignPop = true
    }

    fun handleFaceDetectResult(bitmap: android.graphics.Bitmap, onSuccess: (String) -> Unit) {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        val faceBase64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
        
        submitApply(faceBase64) { needConfirm ->
            onSuccess(if (needConfirm) "1" else "0")
        }
    }

    fun submitApply(faceImageBase64: String?, onComplete: (Boolean) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.addFormDataPart("spe", "h")
                builder.addFormDataPart("bsi", signImageBase64)
                if (!faceImageBase64.isNullOrEmpty()) {
                    builder.addFormDataPart("aig", faceImageBase64)
                }
                builder.addFormDataPart("tma", selAmount.toString())
                builder.addFormDataPart("peo", (curDayOption?.peo ?: 0).toString())
                builder.addFormDataPart("wof", sessionManager.getRasn().toString())

                val response = NetworkClient.apiService.applyTadpoleLoan(builder.build())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val nct = body.getAsJsonObject("data")?.getAsJsonObject("nct")
                        val needConfirm = nct?.get("cdi")?.asBoolean == true
                        onComplete(needConfirm)
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
