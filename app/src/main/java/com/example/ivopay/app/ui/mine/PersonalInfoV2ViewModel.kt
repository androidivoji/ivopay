package com.example.ivopay.app.ui.mine

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.*
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

// --- Model State (V2) ---
data class PersonalInfoV2State(
    val bipl: String = "",
    val moe: String = "",
    val rtid: String = "",
    val rtidn: String = "",
    val rwid: String = "",
    val rwidn: String = "",
    val addl: String = "",
    val lpid: String = "",
    val lpidn: String = "",
    val lcid: String = "",
    val lcidn: String = "",
    val ldid: String = "",
    val ldidn: String = "",
    val viid: String = "",
    val viidn: String = "",
    val del: String = "",
    val adst: String = "",
    val poco: String = "",
    val rel: String = "",
    val reln: String = "",
    val mas: String = "",
    val masn: String = "",
    val edn: String = "",
    val ednn: String = "",
    val fas: String = "",
    val fasn: String = "",
    val lite: String = "",
    val liten: String = "",
    val lidn: String = "",
    val lidnn: String = "",
    val lvstr: String = "",
    val ban: String = "",
    val banid: String = "",
    val bant: String = "",
    val bante: String = "",
    val lope: String = "",
    val lopen: String = "",
    val spane: String = "",
    val spabire: String = "",
    val happtyagmet: String = "",
    val happtyagmetne: String = ""
)

class PersonalInfoV2ViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var state by mutableStateOf(PersonalInfoV2State())
    var isLoading by mutableStateOf(false)
    var commonBankList by mutableStateOf<List<BankItem>>(emptyList())
    var bankMaxLength by mutableStateOf(20)
    var commonParams by mutableStateOf<JsonObject?>(null)

    fun init() {
        fetchInitialUserInfo()
        fetchBankList()
        fetchCommonParams()
    }

    private fun fetchInitialUserInfo() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getUserInfo(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        val pi = data.customer?.personalInfo
                        val lotn = data.customer?.address
                        val bac = data.bankAccount
                        
                        state = state.copy(
                            bipl = lotn?.birthPlace ?: "",
                            moe = pi?.motherName ?: "",
                            rtid = lotn?.rtid ?: "",
                            rtidn = lotn?.rtidn ?: "",
                            rwid = lotn?.rwid ?: "",
                            rwidn = lotn?.rwidn ?: "",
                            lpid = lotn?.lpid ?: "",
                            lpidn = lotn?.lpidn ?: "",
                            lcid = lotn?.lcid ?: "",
                            lcidn = lotn?.lcidn ?: "",
                            ldid = lotn?.ldid ?: "",
                            ldidn = lotn?.ldidn ?: "",
                            viid = lotn?.viid ?: "",
                            viidn = lotn?.viidn ?: "",
                            del = lotn?.street ?: "",
                            adst = lotn?.addressDetail ?: "",
                            poco = lotn?.postCode ?: "",
                            rel = pi?.religion?.toString() ?: "",
                            reln = pi?.religionName ?: "",
                            mas = pi?.marryStatus?.toString() ?: "",
                            masn = pi?.marryStatusName ?: "",
                            edn = pi?.education?.toString() ?: "",
                            ednn = pi?.educationName ?: "",
                            fas = pi?.familySize?.toString() ?: "",
                            fasn = pi?.familySizeName ?: "",
                            lite = pi?.houseType?.toString() ?: "",
                            liten = pi?.houseTypeName ?: "",
                            lidn = pi?.stayDuration?.toString() ?: "",
                            lidnn = pi?.stayDurationName ?: "",
                            lvstr = pi?.liveAddrStr ?: "",
                            ban = bac?.bankName ?: "",
                            bant = bac?.accountNo ?: "",
                            bante = bac?.accountHolder ?: pi?.fullName ?: "",
                            lope = pi?.loanPurpose?.toString() ?: "",
                            spane = pi?.spouseName ?: "",
                            spabire = pi?.spouseBirthDate ?: "",
                            happtyagmet = pi?.hasPropertyAgreement?.toString() ?: "",
                            happtyagmetne = pi?.hasPropertyAgreementName ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalInfoV2VM", "Init error", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchBankList() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getBankList(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 1 && body.data != null) {
                        commonBankList = body.data.bankList ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalInfoV2VM", "fetchBankList error", e)
            }
        }
    }

    private fun fetchCommonParams() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getCommonConfig(JsonObject())
                if (response.isSuccessful) {
                    commonParams = response.body()?.data
                }
            } catch (e: Exception) {
                Log.e("PersonalInfoV2VM", "fetchCommonParams error", e)
            }
        }
    }

    fun updateField(newState: PersonalInfoV2State) {
        state = newState
    }

    fun bankChange(bankName: String) {
        val bank = commonBankList.find { it.name == bankName }
        bankMaxLength = bank?.bal ?: 20
        state = state.copy(ban = bankName)
    }

    fun submitInfo(onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val body = JsonObject()
                val stateJson = gson.toJson(state)
                val map = gson.fromJson(stateJson, JsonObject::class.java)
                map.entrySet().forEach { body.add(it.key, it.value) }

                // Check mas for spouse fields
                if (state.mas != "2") {
                    body.addProperty("spane", "")
                    body.addProperty("happtyagmet", "")
                    body.addProperty("happtyagmetne", "")
                    body.addProperty("spabire", "")
                }

                val response = NetworkClient.apiService.updateUserInfo(body)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    onSuccess()
                } else {
                    onError(response.body()?.get("msg")?.asString ?: "Gagal simpan")
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan sistem")
            } finally {
                isLoading = false
            }
        }
    }
}
