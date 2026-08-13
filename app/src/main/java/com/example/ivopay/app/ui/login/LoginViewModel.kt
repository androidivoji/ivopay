package com.example.ivopay.app.ui.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.SessionManager
import com.google.gson.JsonObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel(private val context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)
    private val gson = com.google.gson.Gson()

    // Flag Role (1 = Lender, 0 = Borrower/Default)
    var isLenderRole by mutableStateOf(false)
    var userRole by mutableStateOf(0) // 1: Lender, 0: Borrower

    // State untuk form login
    var userPhone by mutableStateOf("")
    var verCode by mutableStateOf("")
    var checkAgree by mutableStateOf(true)

    var haveInputNumber by mutableStateOf(false)
    var showLoginWay by mutableStateOf(false)
    var showWaLogin by mutableStateOf(false)
    var codeWayChecked by mutableStateOf("1") // "1": WhatsApp, "2": SMS
    var currentType by mutableStateOf("1") // 1: SMS, 3: WA

    var sendAble by mutableStateOf(true)
    var verCountDown by mutableStateOf(0)
    private var countDownJob: Job? = null

    var isLoading by mutableStateOf(false)
    var showLoginTipPop by mutableStateOf(false)
    var inmText by mutableStateOf("")

    // Additional flags from Vue logic
    var pya by mutableStateOf("")
    var nway by mutableStateOf(false)
    
    // Store login data temporarily for navigation decision
    private var tempLoginData: JsonObject? = null

    // Setter untuk menentukan role saat pengguna datang dari SelectRoleScreen
    fun setRole(isLender: Boolean) {
        this.isLenderRole = isLender
        this.userRole = if (isLender) 1 else 0
    }

    // Computed: Validasi Nomor Telepon
    val isPhoneValid: Boolean
        get() {
            return if (userPhone.startsWith("08")) {
                userPhone.length in 10..13 && checkAgree
            } else if (userPhone.startsWith("8")) {
                userPhone.length in 9..12 && checkAgree
            } else {
                false
            }
        }

    // Computed: Validasi Form OTP (Memastikan verCode 4 digit di kedua kondisi)
    val isFormValid: Boolean
        get() {
            return if (userPhone.startsWith("08")) {
                userPhone.length in 10..13 && verCode.length == 4 && checkAgree
            } else if (userPhone.startsWith("8")) {
                userPhone.length in 9..12 && verCode.length == 4 && checkAgree
            } else {
                false
            }
        }

    // Hitung Mundur OTP (60 detik)
    private fun startCountDown() {
        sendAble = false
        verCountDown = 60
        countDownJob?.cancel()
        countDownJob = viewModelScope.launch {
            while (verCountDown > 0) {
                delay(1000)
                verCountDown--
            }
            sendAble = true
        }
    }

    // Fungsi untuk mengirim Kode OTP
    fun sendVerCode(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (!sendAble) {
            onError("Silakan klik lagi setelah $verCountDown detik")
            return
        }

        currentType = if (showWaLogin && codeWayChecked == "1") "3" else "1"
        val evme = if (currentType == "3") "N22" else "N5"

        isLoading = true
        viewModelScope.launch {
            try {
                uploadTrackingEvent(evme)

                val requestBody = JsonObject().apply {
                    addProperty("mob", userPhone)
                    addProperty("tye", currentType)
                }

                val response = NetworkClient.apiService.sendVerCode(requestBody)
                isLoading = false

                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    onSuccess("Setelah mengirimkan kode verifikasi, harap tunggu beberapa saat")
                    startCountDown()
                } else {
                    val errorMsg = response.body()?.get("msg")?.asString ?: "Gagal mengirim kode"
                    val code = response.body()?.get("code")?.asInt
                    if (code == 101) {
                        handleNextClick({}, { _, _ -> }, {}, {}, {}, { onError(it) })
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                isLoading = false
                onError(e.message ?: "Koneksi bermasalah")
            }
        }
    }

    // Simulasi aksi saat "Selanjutnya" diklik
    fun handleNextClick(
        onSuccessAutoLogin: (targetRoute: String) -> Unit,
        onGestureLogin: (phone: String, role: Int) -> Unit,
        onFaceLogin: () -> Unit,
        onBaseInfo: () -> Unit,
        onOtpStepReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        viewModelScope.launch {
            try {
                // 1. Cek pemulihan token (Token Recovery)
                val oldTkn = sessionManager.getOldToken()
                val savedPhone = sessionManager.getSavedPhoneNumber()

                if (!oldTkn.isNullOrEmpty() && savedPhone == userPhone) {
                    sessionManager.saveLoginSession(
                        token = oldTkn,
                        role = userRole,
                        hasPgsh = sessionManager.getHasPgsh(),
                        mobile = userPhone
                    )
                    sessionManager.removeOldToken()
                    isLoading = false
                    val targetRoute = if (userRole == 1) Screen.LenderMain else Screen.Main
                    onSuccessAutoLogin(targetRoute)
                    return@launch
                }

                // 2. Hit API getLoginWay (/api/lg/m)
                val requestBody = JsonObject().apply {
                    addProperty("mob", userPhone)
                }
                
                val response = NetworkClient.apiService.getLoginWay(requestBody)

                if (response.isSuccessful) {
                    val resData = response.body()?.data
                    if (resData != null) {
                        if (resData.hasGesture) {
                            isLoading = false
                            onGestureLogin(userPhone, userRole)
                        } else if (resData.hasFaceLogin) {
                            isLoading = false
                            onFaceLogin()
                        } else if (resData.vLtr) {
                            isLoading = false
                            onBaseInfo()
                        } else {
                            // Standard OTP Flow
                            showLoginWay = true
                            showWaLogin = resData.hasWaLogin
                            codeWayChecked = if (resData.hasWaLogin) "1" else "2"
                            haveInputNumber = true
                            isLoading = false
                            
                            sendVerCode(
                                onSuccess = { msg -> onOtpStepReady() },
                                onError = { error -> onError(error) }
                            )
                        }
                    } else {
                        isLoading = false
                        onError("Data tidak ditemukan")
                    }
                } else {
                    isLoading = false
                    onError("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                isLoading = false
                onError(e.message ?: "Koneksi bermasalah")
            }
        }
    }

    // Handling Login / Registrasi (Verify OTP)
    fun handleLoginClick(onSuccess: (targetRoute: String) -> Unit, onError: (String) -> Unit) {
        if (pya.isNotEmpty()) {
            uploadTrackingEvent("F19")
        }
        
        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("mob", userPhone)
                    addProperty("ver", verCode)
                    addProperty("acs", userRole.toString())
                    addProperty("pya", pya)
                    addProperty("tye", currentType)
                }

                val response = NetworkClient.apiService.verifyLogin(requestBody)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val data = body.getAsJsonObject("data")
                        if (data != null) {
                            tempLoginData = data
                            Log.d("LoginVM", "Verify Login Success. data: $data")
                            
                            // Conceptually clear old session as per Vue
                            sessionManager.clearSession()
                            
                            // Safe parsing from GSON JsonObject
                            val token = if (data.has("tkn") && !data.get("tkn").isJsonNull) data.get("tkn").asString else ""
                            val mobile = if (data.has("mie") && !data.get("mie").isJsonNull) data.get("mie").asString else userPhone
                            val isActive = if (data.has("act") && !data.get("act").isJsonNull) data.get("act").asBoolean else false
                            val lost = if (data.has("lost") && !data.get("lost").isJsonNull) data.get("lost").asString else null
                            val tinm = if (data.has("tinm") && !data.get("tinm").isJsonNull) data.get("tinm").asString else ""

                            // Save session initially
                            sessionManager.saveLoginSession(
                                token = token,
                                role = userRole,
                                hasPgsh = false,
                                isActive = isActive,
                                mobile = mobile
                            )
                            sessionManager.saveSavedPhoneNumber(mobile)
                            
                            // Tracking
                            if (nway) uploadTrackingEvent("N15")
                            uploadTrackingEvent("N10")
                            if (codeWayChecked == "1") uploadTrackingEvent("N23") else uploadTrackingEvent("N20")

                            // Account Recovery Popup logic (highest priority)
                            if (lost == "3") {
                                isLoading = false
                                inmText = tinm
                                showLoginTipPop = true
                            } else {
                                // MENGHUBUNGI getRole() SEBELUM NAVIGASI
                                onVerifySuccessWithRole(onSuccess, onError)
                            }
                        } else {
                            isLoading = false
                            onError("Data response kosong")
                        }
                    } else {
                        isLoading = false
                        onError(body?.get("msg")?.asString ?: "Verifikasi gagal")
                    }
                } else {
                    isLoading = false
                    onError("Verifikasi gagal: ${response.code()}")
                }
            } catch (e: Exception) {
                isLoading = false
                Log.e("LoginViewModel", "Verify OTP error", e)
                onError("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    private fun onVerifySuccessWithRole(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val roleRequestBody = JsonObject().apply {
                    addProperty("acs", userRole.toString())
                }
                val roleResponse = NetworkClient.apiService.getRole(roleRequestBody)
                isLoading = false

                if (roleResponse.isSuccessful) {
                    val roleData = roleResponse.body()?.data
                    Log.d("LoginVM", "getRole Success. roleData: $roleData")
                    
                    val isActive = if (tempLoginData?.has("act") == true && !tempLoginData!!.get("act").isJsonNull) tempLoginData!!.get("act").asBoolean else false
                    val ngup = if (tempLoginData?.has("ngup") == true && !tempLoginData!!.get("ngup").isJsonNull) tempLoginData!!.get("ngup").asBoolean else false
                    
                    Log.d("LoginVM", "isActive: $isActive, ngup: $ngup")

                    if (isActive) {
                        onSuccess(Screen.Main)
                    } else if (roleData?.acs == "1") {
                        // Lender logic
                        sessionManager.saveLoginSession(
                            token = sessionManager.getAuthToken() ?: "",
                            role = 1,
                            hasPgsh = sessionManager.getHasPgsh(),
                            isActive = true,
                            mobile = sessionManager.getMobileNumber() ?: ""
                        )
                        if (roleData.uico) {
                            onSuccess(Screen.LenderMain)
                        } else {
                            onSuccess(Screen.LenderBasicInfo)
                        }
                    } else {
                        // Borrower logic
                        if (ngup) {
                            Log.d("LoginVM", "Navigating to GestureCreate")
                            // Target to Gesture creation
                            onSuccess("${Screen.GestureCreate}?fromPage=PhoneLogin")
                        } else {
                            onSuccess(Screen.Main)
                        }
                    }
                } else {
                    Log.e("LoginVM", "getRole Failed: ${roleResponse.code()}")
                    onSuccess(if (userRole == 1) Screen.LenderMain else Screen.Main)
                }
            } catch (e: Exception) {
                isLoading = false
                Log.e("LoginVM", "getRole Exception", e)
                onSuccess(if (userRole == 1) Screen.LenderMain else Screen.Main)
            }
        }
    }

    private fun uploadTrackingEvent(evme: String) {
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("evme", evme)
                    addProperty("eval", "1")
                    addProperty("spe", "h")
                }
                NetworkClient.apiService.uploadEvent(body)
            } catch (e: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownJob?.cancel()
    }
}
