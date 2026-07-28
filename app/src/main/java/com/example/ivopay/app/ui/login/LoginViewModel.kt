package com.example.ivopay.app.ui.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel(private val context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)

    // State untuk form login (menggantikan ref/data di Vue)
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

    // Computed: Validasi Nomor Telepon (computed: phoneInputValid)
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

    // Computed: Validasi Form OTP (computed: inputValid)
    val isFormValid: Boolean
        get() {
            return if (userPhone.startsWith("08")) {
                userPhone.length in 10..13 && verCode.length == 4 && checkAgree
            } else if (userPhone.startsWith("8")) {
                userPhone.length in 9..12 && checkAgree
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

    // Simulasi aksi saat "Selanjutnya" diklik (onNextClick)
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

            // Contoh simulasi respon dari backend (resData)
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

    // Simulasi Login / Registrasi (loginClick)
    fun handleLoginClick(onSuccess: (targetRoute: String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            delay(1000) // Simulasi Hit API L_V_C
            isLoading = false

            // Simpan Session ke EncryptedSharedPreferences
            sessionManager.saveLoginSession(
                token = "sample_jwt_token",
                role = 0,
                hasPgsh = false
            )

            // Menentukan arah rute berikutnya
            onSuccess("main")
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownJob?.cancel()
    }
}