package com.example.ivopay.app.ui.loan

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.CashConfigData
import com.example.ivopay.app.data.model.CashConfigResponse
import com.example.ivopay.app.data.model.DayOption
import com.example.ivopay.app.data.model.LoanOption
import com.example.ivopay.app.util.SessionManager
import android.util.Base64
import com.example.ivopay.app.util.CommonUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

sealed class ApplyActionEvent {
    object StartFaceLiveDetect : ApplyActionEvent()
    object StartFaceLiveDetectType2 : ApplyActionEvent()
    object StartAliFaceVerify : ApplyActionEvent()
    object StartZuluzFaceVerify : ApplyActionEvent()
}

class ApplyLoanViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var isLoading by mutableStateOf(false)
    var cashData by mutableStateOf<CashConfigData?>(null)
    
    // States for UI
    var inputAmount by mutableStateOf("0")
    var inputWidth by mutableIntStateOf(120)
    var dayIdx by mutableIntStateOf(0)
    var amountIdx by mutableIntStateOf(0)
    
    var minAmount by mutableLongStateOf(0L)
    var maxAmount by mutableLongStateOf(0L)
    var opWithDays by mutableStateOf<List<DayOption>>(emptyList())
    
    var showInputTip by mutableStateOf(false)
    var showSignPop by mutableStateOf(false)
    var showSignFeePop by mutableStateOf(false)
    var signImage by mutableStateOf<Bitmap?>(null)

    var actionEvent by mutableStateOf<ApplyActionEvent?>(null)
    var submitSuccessNoc by mutableStateOf<String?>(null)

    // Data for submission
    private var zlzId = ""
    private var aynId = ""
    private var freId = ""
    private var capturedFaceBitmap: Bitmap? = null

    // Mock data for 'act' mode
    private val SIMPLE_PNG_DATA = "data:image/jpeg;base64,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"

    fun init() {
        fetchCashConfig(0)
    }

    fun fetchCashConfig(amt: Long) {
        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    if (amt > 0) addProperty("amt", amt)
                }
                val response = NetworkClient.apiService.getAmountCashConfig(requestBody)
                if (response.isSuccessful) {
                    val bodyString = response.body()?.toString()
                    val responseObj = gson.fromJson(bodyString, CashConfigResponse::class.java)
                    if (responseObj?.code == 1 && responseObj.data != null) {
                        val data = responseObj.data
                        cashData = data
                        opWithDays = data.tpos ?: emptyList()
                        amountIdx = data.dtma
                        dayIdx = data.dpeo
                        maxAmount = data.atma
                        minAmount = data.itma
                        
                        val selAmount = getSelAmount()
                        inputAmount = (selAmount / 1000).toString()
                        inputWidth = inputAmount.length * 40
                    }
                }
            } catch (e: Exception) {
                Log.e("ApplyLoanViewModel", "fetchCashConfig error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun getSelAmount(): Long {
        return getCurLoanOption()?.tma ?: 0L
    }

    fun getCurLoanOption(): LoanOption? {
        val curDay = getCurDayOption()
        if (curDay?.dop != null && amountIdx < curDay.dop.size) {
            return curDay.dop[amountIdx]
        }
        return null
    }

    fun getCurDayOption(): DayOption? {
        if (opWithDays.isNotEmpty() && dayIdx < opWithDays.size) {
            return opWithDays[dayIdx]
        }
        return null
    }

    fun onDayItemClick(idx: Int) {
        val option = opWithDays.getOrNull(idx)
        if (option?.aow == true) {
            dayIdx = idx
        }
    }

    fun handleAmountInput(input: String) {
        inputAmount = input
        inputWidth = input.length * 40
        
        val amt = (input.toLongOrNull() ?: 0L) * 1000L
        if (amt < minAmount || amt > maxAmount) {
            showInputTip = true
        } else {
            showInputTip = false
            viewModelScope.launch {
                delay(500)
                if (inputAmount == input) {
                    fetchCashConfig(amt)
                }
            }
        }
    }

    fun onNextClick(onSign: () -> Unit, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val actStatus = sessionManager.getActStatus()
        Log.d("APPLY_DEBUG", "Checking act mode: '$actStatus'")
        
        if (actStatus == "2") { // Vue uses act "1" or true for mock
            Log.d("APPLY_DEBUG", "Act mode ACTIVE, submitting mock data")
            submitApplyWithMock(onSuccess, onError)
        } else {
            Log.d("APPLY_DEBUG", "Act mode INACTIVE, showing sign popup")
            onSign()
        }
    }

    fun onSignatureSubmit(bitmap: Bitmap) {
        signImage = bitmap
        showSignPop = false
        startFaceLiveDetect()
    }

    private fun startFaceLiveDetect() {
        val tttp = sessionManager.getTttp()
        when (tttp) {
            1 -> actionEvent = ApplyActionEvent.StartFaceLiveDetect
            5 -> actionEvent = ApplyActionEvent.StartFaceLiveDetectType2
            // Add other cases if needed (Alibaba, Zuluz)
            else -> actionEvent = ApplyActionEvent.StartFaceLiveDetect
        }
    }

    fun handleFaceDetectResult(bitmap: Bitmap?, fre_id: String? = null, zlz_id: String? = null, ayn_id: String? = null) {
        this.capturedFaceBitmap = bitmap
        this.freId = fre_id ?: ""
        this.zlzId = zlz_id ?: ""
        this.aynId = ayn_id ?: ""
        
        submitApply()
    }

    fun submitApply() {
        val loanOp = getCurLoanOption()
        val dayOp = getCurDayOption()
        if (loanOp == null || dayOp == null) return

        isLoading = true
        viewModelScope.launch {
            try {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.addFormDataPart("tma", loanOp.tma.toString())
                builder.addFormDataPart("peo", dayOp.peo.toString())
                builder.addFormDataPart("pocy", "10")
                builder.addFormDataPart("yep", cashData?.yep ?: "")
                builder.addFormDataPart("itma", minAmount.toString())
                builder.addFormDataPart("atma", maxAmount.toString())
                builder.addFormDataPart("wof", sessionManager.getRasn().toString())
                builder.addFormDataPart("ife", loanOp.ife.toString())
                builder.addFormDataPart("sam", loanOp.sam.toString())
                builder.addFormDataPart("dua", loanOp.dua.toString())
                
                // Add Face Detect IDs
                if (zlzId.isNotEmpty()) builder.addFormDataPart("zlz_id", zlzId)
                if (aynId.isNotEmpty()) builder.addFormDataPart("ayn_id", aynId)
                if (freId.isNotEmpty()) builder.addFormDataPart("fre_id", freId)

                // Face image (aig)
                capturedFaceBitmap?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val bytes = stream.toByteArray()
                    builder.addFormDataPart("aig", "face.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                }
                
                // Note: Sign image (bsi) is typically for BorrowerSignContracts (gsbw), 
                // but if this applyLoan (yatc) needs it, we could add it.
                // Vue CashLoan apply doesn't seem to send bsi in fileInfo.
                
                val response = NetworkClient.apiService.applyLoan(builder.build())
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val noc = body.getAsJsonObject("data")?.get("noc")?.asString ?: ""
                        submitSuccessNoc = noc
                    } else if (body?.get("code")?.asInt == 101) {
                        fetchCashConfig(0)
                    }
                }
            } catch (e: Exception) {
                Log.e("ApplyLoanVM", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun submitApplyWithMock(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val loanOp = getCurLoanOption()
                val dayOp = getCurDayOption()
                
                // For mock mode, we use the Base64 string directly
                val rawBase64 = SIMPLE_PNG_DATA.substringAfter(",")
                val bytes = Base64.decode(rawBase64, Base64.DEFAULT)

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("tma", loanOp?.tma.toString())
                    .addFormDataPart("peo", dayOp?.peo.toString())
                    .addFormDataPart("pocy", "10")
                    .addFormDataPart("yep", cashData?.yep ?: "")
                    .addFormDataPart("itma", minAmount.toString())
                    .addFormDataPart("atma", maxAmount.toString())
                    .addFormDataPart("wof", sessionManager.getRasn().toString())
                    .addFormDataPart("ife", loanOp?.ife.toString())
                    .addFormDataPart("sam", loanOp?.sam.toString())
                    .addFormDataPart("dua", loanOp?.dua.toString())
                    .addFormDataPart(
                        "aig", 
                        "mock_signature.png", 
                        bytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                    )
                    .build()

                val response = NetworkClient.apiService.applyLoan(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val noc = body.getAsJsonObject("data")?.get("noc")?.asString ?: ""
                        onSuccess(noc)
                    } else {
                        val msg = body?.get("msg")?.asString ?: "Gagal mengajukan"
                        onError(msg)
                    }
                } else {
                    onError("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Terjadi kesalahan")
            } finally {
                isLoading = false
            }
        }
    }
}
