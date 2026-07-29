package com.example.ivopay.app.ui.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel(private val context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)

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

    var sendAble by mutableStateOf(true)
    var verCountDown by mutableStateOf(0)
    private var countDownJob: Job? = null

    var isLoading by mutableStateOf(false)
    var showLoginTipPop by mutableStateOf(false)
    var inmText by mutableStateOf("")

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
    fun startCountDown() {
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

    // Simulasi aksi saat "Selanjutnya" diklik
    fun handleNextClick(
        onGestureLogin: () -> Unit,
        onFaceLogin: () -> Unit,
        onBaseInfo: () -> Unit,
        onShowOtpInput: () -> Unit
    ) {
        isLoading = true
        viewModelScope.launch {
            delay(1000) // Simulasi Hit API _getLoginWay
            isLoading = false

            // Simulasi respon dari backend
            val resDataGesture = false
            val resDataAig = false
            val resDataVLtr = false
            val resDataWa = true

            when {
                resDataGesture -> onGestureLogin()
                resDataAig -> onFaceLogin()
                resDataVLtr -> onBaseInfo()
                else -> {
                    // Masuk ke tahap OTP
                    showLoginWay = true
                    showWaLogin = resDataWa
                    codeWayChecked = if (resDataWa) "1" else "2"
                    haveInputNumber = true
                    onShowOtpInput()
                }
            }
        }
    }

    // Handling Login / Registrasi
    fun handleLoginClick(onSuccess: (targetRoute: String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            delay(1000) // Simulasi Hit API Login
            isLoading = false

            // Tentukan rute dinamis berdasarkan role yang diset
            val activeRole = if (isLenderRole || userRole == 1) 1 else 0
            val targetRoute = if (activeRole == 1) {
                Screen.LenderMain // "l_main"
            } else {
                Screen.Main       // "main"
            }

            Log.d("LoginViewModel", "Login Success. Role: $activeRole -> Route: $targetRoute")
            val formattedPhone = when {
                userPhone.startsWith("08") -> userPhone
                userPhone.startsWith("8") -> "0$userPhone"
                else -> userPhone
            }

            // Simpan Session ke EncryptedSharedPreferences dengan role yang sesuai
            sessionManager.saveLoginSession(
                token = "sample_jwt_token",
                role = activeRole,
                hasPgsh = false,
                mobile = formattedPhone,
                fullName = ""
            )

            // Kirimkan targetRoute hasil evaluasi (bukan hardcoded)
            onSuccess(targetRoute)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownJob?.cancel()
    }
}