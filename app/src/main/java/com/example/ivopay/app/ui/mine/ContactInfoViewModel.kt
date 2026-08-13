package com.example.ivopay.app.ui.mine

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.*
import com.example.ivopay.app.ui.components.OptionItem
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

// Model lokal untuk form
data class EmergencyContactState(
    val funName: String = "",
    val phe: String = "",
    val rel: String = "",
    val reln: String = ""
)

class ContactInfoViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var contacts by mutableStateOf(listOf(EmergencyContactState(), EmergencyContactState()))
    var isLoading by mutableStateOf(false)
    var relationOptions by mutableStateOf<List<OptionItem>>(emptyList())
    var cmeData by mutableStateOf<BorrowerCmeData?>(null)

    fun init() {
        fetchInitialUserInfo()
        fetchCommonParams()
        fetchHomeConfig()
    }

    private fun fetchInitialUserInfo() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getUserInfo(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val su = response.body()?.data
                    val serverCots = su?.contacts ?: emptyList()
                    
                    // Logic pushContactsInfo: always 2 contacts
                    val newList = mutableListOf<EmergencyContactState>()
                    for (i in 0 until 2) {
                        if (serverCots.size > i) {
                            var phone = serverCots[i].phone ?: ""
                            if (phone.startsWith("0")) phone = phone.substring(1)
                            newList.add(EmergencyContactState(
                                funName = serverCots[i].name ?: "",
                                phe = phone,
                                rel = serverCots[i].rel ?: "",
                                reln = serverCots[i].relationName ?: ""
                            ))
                        } else {
                            newList.add(EmergencyContactState())
                        }
                    }
                    contacts = newList
                }
            } catch (e: Exception) {
                Log.e("ContactInfoVM", "Init error", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchCommonParams() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getCommonConfig(JsonObject())
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    val r1 = data?.getAsJsonArray("r_1")
                    val options = mutableListOf<OptionItem>()
                    r1?.forEach { el ->
                        val obj = el.asJsonObject
                        options.add(OptionItem(obj.get("k").asString, obj.get("v").asString))
                    }
                    relationOptions = options
                }
            } catch (e: Exception) {}
        }
    }

    private fun fetchHomeConfig() {
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getHomeCashConfig(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val data = response.body()?.get("data")?.asJsonObject
                    val cme = data?.get("cme")?.asJsonObject
                    if (cme != null) {
                        cmeData = gson.fromJson(cme, BorrowerCmeData::class.java)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    fun updateContact(index: Int, newState: EmergencyContactState) {
        val updated = contacts.toMutableList()
        updated[index] = newState
        contacts = updated
    }

    fun submitInfo(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Logic isOkContacts
        val loginPhone = sessionManager.getMobileNumber() ?: ""
        val phoneList = mutableListOf<String>()

        for ((index, it) in contacts.withIndex()) {
            if (it.phe.isEmpty()) {
                onError("Nomor telepon Kontak Darurat ${index + 1} tidak boleh kosong")
                return
            }
            // Check starts with 8
            if (!it.phe.startsWith("8")) {
                onError("Masukkan nomor telepon yang dimulai dengan 8")
                return
            }
            // Length 9-12
            if (it.phe.length < 9 || it.phe.length > 12) {
                onError("Nomor telepon adalah 9-12 digit, harap verifikasi")
                return
            }
            // Check login phone
            if (loginPhone.isNotEmpty()) {
                val cleanLogin = if (loginPhone.startsWith("0")) loginPhone.substring(1) else loginPhone
                if (it.phe == cleanLogin) {
                    onError("Dilarang mengisi nomor ponsel login sebagai kontak darurat.")
                    return
                }
            }
            phoneList.add(it.phe)
        }

        // Check duplicates
        if (phoneList.distinct().size != phoneList.size) {
            onError("Jangan ulangi nomor kontak")
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                // Tracking event P8 as per Vue
                val trackingBody = JsonObject().apply {
                    addProperty("evme", "P8")
                    addProperty("eval", "1")
                    addProperty("spe", "h")
                }
                NetworkClient.apiService.uploadEvent(trackingBody)

                // Serializing cots as string JSON array
                val cotsArray = JsonArray()
                contacts.forEach {
                    val obj = JsonObject()
                    obj.addProperty("fun", it.funName)
                    obj.addProperty("phe", it.phe)
                    obj.addProperty("rel", it.rel)
                    obj.addProperty("reln", it.reln)
                    cotsArray.add(obj)
                }

                val body = JsonObject()
                body.addProperty("cots", cotsArray.toString())

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
