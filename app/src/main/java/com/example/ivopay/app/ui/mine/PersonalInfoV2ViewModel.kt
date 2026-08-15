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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

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
    var cmeData by mutableStateOf<BorrowerCmeData?>(null)

    // Statics tracking as per Vue
    private val statics = mutableMapOf(
        "live_addr_province_modify_count" to 0,
        "live_address_detail_use_paste" to 0,
        "live_type_modify_count" to 0,
        "live_duration_modify_count" to 0
    )

    fun init() {
        fetchInitialUserInfo()
        fetchBankList()
        fetchCommonParams()
//        fetchHomeConfig()
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
                        
                        // Format Alamat (Domisili) - Parity with Vue logic
                        var rtRw = ""
                        if (lotn != null && !lotn.rtidn.isNullOrEmpty() && !lotn.rwidn.isNullOrEmpty()) {
                            rtRw = "${lotn.rtidn}/${lotn.rwidn}"
                        }
                        
                        val localAddr = buildString {
                            if (rtRw.isNotEmpty()) append("$rtRw ")
                            lotn?.lpidn?.let { if (it.isNotEmpty()) append("$it ") }
                            lotn?.lcidn?.let { if (it.isNotEmpty()) append("$it ") }
                            lotn?.ldidn?.let { if (it.isNotEmpty()) append("$it ") }
                            lotn?.viidn?.let { if (it.isNotEmpty()) append(it) }
                        }.trim()

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
                            lvstr = localAddr,
                            ban = bac?.bankName ?: "",
                            bant = bac?.accountNo ?: "",
                            bante = bac?.accountHolder ?: pi?.fullName ?: "",
                            lope = pi?.loanPurpose?.toString() ?: "",
                            lopen = pi?.loanPurposeName ?: "",
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

    private fun fetchHomeConfig() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getHomeCashConfig(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val bodyString = response.body()?.toString()
                    if (bodyString != null) {
                        val jsonBody = gson.fromJson(bodyString, JsonObject::class.java)
                        val dataObj = jsonBody.getAsJsonObject("data")
                        val cmeObj = dataObj.get("cme")
                        if (cmeObj != null && !cmeObj.isJsonNull) {
                            cmeData = gson.fromJson(cmeObj, BorrowerCmeData::class.java)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PersonalInfoV2VM", "fetchHomeConfig error", e)
            }
        }
    }

    fun updateField(newState: PersonalInfoV2State) {
        state = newState
    }

    fun bankChange(bankCode: String) {
        val bank = commonBankList.find { it.name == bankCode }
        bankMaxLength = bank?.bal ?: 20
        state = state.copy(ban = bankCode)
    }

    fun uploadEvent(evme: String) {
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

    private fun postEventsList() {
        viewModelScope.launch {
            try {
                val eventsArray = JsonArray()
                statics.forEach { (key, value) ->
                    val eventObj = JsonObject()
                    eventObj.addProperty("evme", key)
                    eventObj.addProperty("eval", value.toString())
                    eventsArray.add(eventObj)
                }
                val body = JsonObject().apply {
                    addProperty("el", eventsArray.toString())
                    addProperty("spe", "h")
                }
                NetworkClient.apiService.uploadEvent(body)
            } catch (e: Exception) {}
        }
    }

    fun submitInfo(onSuccess: () -> Unit, onError: (String) -> Unit) {
        uploadEvent("P4")
        isLoading = true
        viewModelScope.launch {
            try {
                // 1. Post statistics if wof is false
                if (cmeData?.wof == false) {
                    postEventsList()
                }

                // 2. Get Loan List to check for errors as per Vue logic
                val loanListResponse = NetworkClient.apiService.getBorrowerLoanPersonalList(JsonObject().apply { addProperty("spe", "h") })
                if (loanListResponse.isSuccessful) {
                    val data = loanListResponse.body()?.get("data")?.asJsonObject
                    val errorMsg = data?.get("c_up_bae_erro")?.asString
                    if (!errorMsg.isNullOrEmpty()) {
                        onError(errorMsg)
                        isLoading = false
                        return@launch
                    }
                }

                // 3. Prepare Multipart Request (Individual Form-Data Parts)
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                
                builder.addFormDataPart("moe", state.moe)
                builder.addFormDataPart("rel", state.rel)
                builder.addFormDataPart("reln", state.reln)
                builder.addFormDataPart("edn", state.edn)
                builder.addFormDataPart("ednn", state.ednn)
                builder.addFormDataPart("lite", state.lite)
                builder.addFormDataPart("liten", state.liten)
                builder.addFormDataPart("lidn", state.lidn)
                builder.addFormDataPart("lidnn", state.lidnn)
                builder.addFormDataPart("lope", state.lope)
                builder.addFormDataPart("lvstr", state.lvstr)
                builder.addFormDataPart("del", state.del)
                builder.addFormDataPart("ban", state.ban)
                builder.addFormDataPart("bant", state.bant)
                builder.addFormDataPart("bante", state.bante)
                builder.addFormDataPart("mas", state.mas)
                builder.addFormDataPart("masn", state.masn)
                // Tambahkan key cadangan agar sesuai dengan pengecekan PHP
                builder.addFormDataPart("bank", state.ban)      // Untuk $customerBank->bank
                builder.addFormDataPart("bank_account", state.bant)  // Untuk $customerBank->account
                builder.addFormDataPart("bank_account_name", state.bante)    // Untuk $customerBank->name
                builder.addFormDataPart("detail", state.del)    // Untuk $location->detail
                builder.addFormDataPart("purpose", state.lope)  // Untuk $basicInfo->purpose

                // Tambahkan parameter bank sesuai detail objek BankItem
                val selectedBank = commonBankList.find { it.name == state.ban }
                selectedBank?.let {
                    builder.addFormDataPart("banid", it.id?.toString() ?: "")
                    builder.addFormDataPart("n", it.name ?: "")
                    builder.addFormDataPart("fn", it.fullName ?: "")
                    builder.addFormDataPart("bal", it.bal?.toString() ?: "0")
                    builder.addFormDataPart("ais", it.ais?.toString() ?: "0")
                }

                if (state.mas == "2") {
                    builder.addFormDataPart("fas", state.fas)
                    builder.addFormDataPart("fasn", state.fasn)
                    builder.addFormDataPart("spane", state.spane)
                    builder.addFormDataPart("spabire", state.spabire)
                    builder.addFormDataPart("happtyagmet", state.happtyagmet)
                    builder.addFormDataPart("happtyagmetne", state.happtyagmetne)
                } else {
                    builder.addFormDataPart("spane", "")
                    builder.addFormDataPart("happtyagmet", "")
                    builder.addFormDataPart("happtyagmetne", "")
                    builder.addFormDataPart("spabire", "")
                }

                // 4. Update User Info (Hit v1/api/c/up using individual parts)
                val response = NetworkClient.apiService.updateBaseInfo(builder.build())
                isLoading = false

                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    onSuccess()
                } else {
                    onError(response.body()?.get("msg")?.asString ?: "Gagal simpan")
                }
            } catch (e: Exception) {
                isLoading = false
                onError("Terjadi kesalahan sistem")
            }
        }
    }
}
