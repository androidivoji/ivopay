package com.example.ivopay.app.ui.loan

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

class QuestionnaireViewModel(context: Context) : ViewModel() {
    var rasn by mutableStateOf("")
    var checked by mutableStateOf("")
    var checked2 by mutableStateOf("")
    
    var showFirstQuestion by mutableStateOf(true)
    var showSecondQuestion by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    val selectList1 = listOf(
        "A" to "A. Saya memiliki rumah",
        "B" to "B. Saya memiliki mobil",
        "C" to "C. Saya memiliki rumah dan mobil",
        "D" to "D. Saya belum memiliki rumah maupun mobil"
    )

    val selectList2 = listOf(
        "A" to "A. Saya tidak memiliki pinjaman apa pun",
        "B" to "B. Saya memiliki pinjaman rumah (KPR)",
        "C" to "C. Saya memiliki pinjaman mobil (KKB)"
    )

    fun init(rasnParam: String) {
        rasn = rasnParam
    }

    fun onClickSubmit(onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        if (checked.isEmpty()) return

        if (checked != "D" && checked2.isEmpty()) {
            showSecondQuestion = true
            showFirstQuestion = false
        } else {
            submitAnswer(onSuccess, onError)
        }
    }

    private fun submitAnswer(onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val params = JsonObject().apply {
                    addProperty("op1", checked)
                    addProperty("op2", checked2)
                }
                val response = NetworkClient.apiService.submitQuestionnaireAnswer(params)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val nbj = body.getAsJsonObject("data")?.get("nbj")?.asBoolean ?: false
                        onSuccess(nbj)
                    } else {
                        onError(body?.get("msg")?.asString ?: "Gagal mengirim jawaban")
                    }
                } else {
                    onError("Terjadi kesalahan jaringan")
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan")
            } finally {
                isLoading = false
            }
        }
    }
}
