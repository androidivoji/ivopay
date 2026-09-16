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

class Ci10CashViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var cashData by mutableStateOf<TadpoleCashConfigData?>(null)
    var amountIdx by mutableIntStateOf(0)
    var dayIdx by mutableIntStateOf(0)
    var ewb by mutableStateOf<List<TadpoleBillItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    
    var showSignPop by mutableStateOf(false)
    var signImageBase64 by mutableStateOf("")

    // RIPLAY Agreement State
    var isRiplayAgreed by mutableStateOf(false)
    var showRiplayDialog by mutableStateOf(false)
    var riplayPoints by mutableStateOf(
        listOf(
            RiplayPoint("1. Definisi", "Penjelasan mengenai istilah-istilah yang digunakan dalam produk ini.", true),
            RiplayPoint("2. Manfaat dan Risiko", "Keuntungan mendapatkan pendanaan cepat dan risiko jika terjadi keterlambatan pembayaran.", false),
            RiplayPoint("3. Fitur Utama", "Rincian mengenai jumlah, tenor, dan bunga pendanaan.", false),
            RiplayPoint("4. Kewajiban Pengguna", "Kewajiban untuk membayar tepat waktu dan memberikan data yang akurat.", false),
            RiplayPoint("5. Tata Cara Pelayanan", "Prosedur pengajuan dan pengaduan konsumen.", false),
            RiplayPoint("6. Persyaratan", "Kriteria penerima dana dan dokumen yang diperlukan.", false),
            RiplayPoint("7. Biaya", "Rincian biaya administrasi dan denda keterlambatan.", false),
            RiplayPoint("8. Informasi Tambahan", "IVOJI berizin dan diawasi oleh OJK.", false),
            RiplayPoint("9. Penafian", "Pernyataan bahwa pengguna telah memahami isi dokumen.", false)
        )
    )

    fun toggleRiplayPoint(index: Int, checked: Boolean) {
        val newList = riplayPoints.toMutableList()
        newList[index] = newList[index].copy(checked = checked)
        riplayPoints = newList
    }

    fun confirmRiplay() {
        if (riplayPoints.all { it.checked }) {
            isRiplayAgreed = true
            showRiplayDialog = false
        }
    }

    // Phone Code Logic
    var verCode by mutableStateOf("")
    var verCountDown by mutableIntStateOf(0)
    private var countDownJob: kotlinx.coroutines.Job? = null
    var rasn by mutableStateOf("")

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

    fun init(rasn: String) {
        this.rasn = rasn
        fetchCashConfig()
    }

    private fun fetchCashConfig() {
        isLoading = true
        viewModelScope.launch {
            try {
                val params = JsonObject().apply { addProperty("spe", "h") }
                val response = NetworkClient.apiService.getCi10CashConfig(params)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1) {
                        cashData = body.data
                        amountIdx = body.data?.dtma ?: 0
                        dayIdx = body.data?.dpeo ?: 0
                        fetchInlgBillPre()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchInlgBillPre() {
        val curDay = curDayOption ?: return
        viewModelScope.launch {
            try {
                val params = JsonObject().apply {
                    addProperty("spe", "h")
                    addProperty("tma", selAmount)
                    addProperty("bpio", curDay.bpio)
                    // Add other params if needed like fbd, ddd, itpr
                }
                val response = NetworkClient.apiService.getCi10BillPreview(params)
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
        val curDay = curDayOption
        val curLoan = curLoanOption ?: return emptyList()

        return listOf(
            "Nama" to (bio?.bkan ?: "--"),
            "Bank Penerima" to (bio?.bkm ?: "--"),
            "Nomor Rekening" to (bio?.baut ?: "--"),
            "Nilai Pinjaman" to CommonUtils.formatRupiah(selAmount.toDouble()),
            "Jangka Pinjaman" to "${curDay?.bpio ?: 0} bulan",
            "Biaya Layanan" to CommonUtils.formatRupiah(curLoan.ife.toDouble()),
            "Jumlah diterima" to CommonUtils.formatRupiah(curLoan.dam.toDouble()),
            "Repayment Amount" to CommonUtils.formatRupiah(curLoan.dua.toDouble())
        )
    }

    fun onApplyClick() {
        if (cashData?.nvmp == true) {
            // Logic handled in Screen for PhoneCode
        } else {
            showSignPop = true
        }
    }

    fun handleFaceDetectResult(bitmap: android.graphics.Bitmap, onSuccess: (String) -> Unit) {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        val faceBase64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
        
        submitApply(faceBase64) { mob ->
            onSuccess(mob)
        }
    }

    fun submitApply(faceImageBase64: String?, onComplete: (String) -> Unit) {
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
                builder.addFormDataPart("bpio", (curDayOption?.bpio ?: 0).toString())
                builder.addFormDataPart("rasn", rasn)

                val response = NetworkClient.apiService.applyCi10Loan(builder.build())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val mob = body.getAsJsonObject("data")?.getAsJsonObject("ci10")?.get("mob")?.asString ?: ""
                        onComplete(mob)
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
