package com.example.ivopay.app.ui.mine

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.UserInfoData
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

data class ReasonItem(val k: String, val v: String)

class AccountLogoutViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()
    
    var userInfo by mutableStateOf<UserInfoData?>(null)
    var userPhone by mutableStateOf(sessionManager.getMobileNumber() ?: "")
    var isLoading by mutableStateOf(false)
    
    // UI Visibility States
    var showConfirmPop by mutableStateOf(false)
    var showSMSPop by mutableStateOf(false)
    var showRetainPop by mutableStateOf(false)
    var showSelectReasonPop by mutableStateOf(false)
    
    // SMS States
    var verCode by mutableStateOf("")
    var verCountDown by mutableIntStateOf(0)
    var sendAble by mutableStateOf(true)
    
    // Logic Data
    var needAig by mutableStateOf(false)
    var selectList1 by mutableStateOf<List<ReasonItem>>(emptyList())
    var checkedReasons = mutableStateOf<Set<String>>(emptySet())
    
    // Retention Data
    var rtinPudtyp by mutableStateOf<Int?>(null)
    var rtinPudtypL by mutableStateOf("")
    var rtinPudtypA by mutableStateOf("")
    var rtinPudtypP by mutableStateOf("")
    var rasn by mutableStateOf("")
    
    var errMsg by mutableStateOf("")
    var errCode by mutableIntStateOf(0)
    
    private var timerJob: Job? = null
    private var intervalJob: Job? = null

    fun init() {
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getUserInfo(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    userInfo = response.body()?.data
                }
            } catch (e: Exception) {}
        }
    }

    fun checkAccount() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.checkBeforeAccountLogout(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val data = response.body()?.getAsJsonObject("data")
                    needAig = data?.get("aig")?.asBoolean ?: false
                    
                    val rtinOpsElement = data?.get("rtin_ops")
                    if (rtinOpsElement != null && rtinOpsElement.isJsonArray) {
                        val type = object : TypeToken<List<ReasonItem>>() {}.type
                        val list: List<ReasonItem> = gson.fromJson(rtinOpsElement, type)
                        if (list.isNotEmpty()) {
                            selectList1 = list
                            showSelectReasonPop = true
                        } else {
                            showConfirmPop = true
                        }
                    } else {
                        showConfirmPop = true
                    }
                }
            } catch (e: Exception) {
                Log.e("LogoutVM", "checkAccount error", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun submitRetentionReasons() {
        isLoading = true
        showSelectReasonPop = false
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("rtin_ops", gson.toJson(checkedReasons.value))
                    addProperty("spe", "h")
                }
                val response = NetworkClient.apiService.submitLogoutRetain(body)
                if (response.isSuccessful) {
                    val res = response.body()
                    rtinPudtyp = if (res?.get("rtin_pudtyp")?.isJsonNull == true) null else res?.get("rtin_pudtyp")?.asInt
                    rtinPudtypL = res?.get("rtin_pudtyp_l")?.asString ?: ""
                    rtinPudtypA = res?.get("rtin_pudtyp_a")?.asString ?: ""
                    rtinPudtypP = res?.get("rtin_pudtyp_p")?.asString ?: ""
                    rasn = res?.get("rasn")?.asString ?: ""
                    
                    if (rtinPudtyp == null) {
                        showConfirmPop = true
                    } else {
                        showRetainPop = true
                    }
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun cancelRetain() {
        showRetainPop = false
        isLoading = true
        viewModelScope.launch {
            try {
                NetworkClient.apiService.rtinQud(JsonObject().apply { addProperty("spe", "h") })
            } catch (e: Exception) {}
            finally { isLoading = false }
        }
    }

    fun logoutSubmitFace(bitmap: Bitmap) {
        isLoading = true
        viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("aig", "logout_face.jpg", stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull()))
                    .addFormDataPart("spe", "h")
                    .build()
                
                val response = NetworkClient.apiService.logoutSubmitFace(requestBody)
                if (response.isSuccessful) {
                    val aigUk = response.body()?.getAsJsonObject("data")?.get("aig_uk")?.asString
                    if (!aigUk.isNullOrEmpty()) {
                        startFacePolling(aigUk)
                    } else {
                        isLoading = false
                        showSMSPop = true
                    }
                } else {
                    isLoading = false
                    showSMSPop = true
                }
            } catch (e: Exception) {
                isLoading = false
                showSMSPop = true
            }
        }
    }

    private fun startFacePolling(aigUk: String) {
        intervalJob?.cancel()
        intervalJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                try {
                    val body = JsonObject().apply {
                        addProperty("aig_uk", aigUk)
                        addProperty("spe", "h")
                    }
                    val response = NetworkClient.apiService.getFaceLogoutResult(body)
                    if (response.isSuccessful) {
                        val status = response.body()?.getAsJsonObject("data")?.get("aig_status")?.asInt
                        if (status == 2) {
                            onSubmitLogout { } // Handle success in Screen
                            break
                        } else if (status == 3) {
                            showSMSPop = true
                            break
                        }
                    }
                } catch (e: Exception) {
                    showSMSPop = true
                    break
                }
            }
            isLoading = false
        }
    }

    fun sendVerCode(onToast: (String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("mob", userPhone)
                    addProperty("spe", "h")
                }
                val response = NetworkClient.apiService.accountLogoutSendCode(body)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    onToast("Setelah mengirimkan kode verifikasi, harap tunggu beberapa saat")
                    startTimer()
                } else {
                    onToast(response.body()?.get("msg")?.asString ?: "Gagal kirim kode")
                }
            } catch (e: Exception) {}
            finally { isLoading = false }
        }
    }

    private fun startTimer() {
        sendAble = false
        verCountDown = 60
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (verCountDown > 0) {
                delay(1000)
                verCountDown--
            }
            sendAble = true
        }
    }

    fun onLogoutClick(onToast: (String) -> Unit, onFinished: () -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("mob", userPhone)
                    addProperty("ver", verCode)
                    addProperty("spe", "h")
                }
                val response = NetworkClient.apiService.accountLogoutVerifyCode(body)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    onSubmitLogout(onFinished)
                } else {
                    onToast(response.body()?.get("msg")?.asString ?: "Kode verifikasi salah")
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    fun onSubmitLogout(onFinished: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.accountLogout(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    sessionManager.clearSession()
                    onFinished()
                }
            } catch (e: Exception) {}
            finally { isLoading = false }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        intervalJob?.cancel()
    }
}
