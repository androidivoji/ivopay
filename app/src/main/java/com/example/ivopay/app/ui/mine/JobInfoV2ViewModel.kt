package com.example.ivopay.app.ui.mine

import android.content.Context
import android.graphics.Bitmap
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

data class JobInfoV2State(
    val con: String = "",       // Nama Perusahaan
    val syamt: String = "",     // Pendapatan Bulanan
    val joiun: String = "",     // Jenis Usaha (Business Type Label)
    val joi: String = "",       // Jenis Pekerjaan Key
    val join: String = "",      // Jenis Pekerjaan Label
    val iniun: String = "",     // Jenis Industri Label
    val ini: String = "",       // Sektor Ekonomi Key
    val inin: String = "",      // Sektor Ekonomi Label
    val jork: String = "",      // Jabatan Key
    val jorkn: String = "",     // Jabatan Label
    val wkdn: String = "",      // Lama Bekerja Key
    val wkdnn: String = "",     // Lama Bekerja Label
    val cstr: String = "",      // Alamat Kantor Gabungan
    val cdel: String = "",      // Detail Alamat Perusahaan
    val wkptie: String = "",    // Image/URL Bukti Kerja
    val wkptie_yep: String = "", // Tipe Dokumen Bukti Kerja
    val wkptie_bitmap: Bitmap? = null
)

class JobInfoV2ViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()

    var state by mutableStateOf(JobInfoV2State())
    var isLoading by mutableStateOf(false)
    var commonParams by mutableStateOf<JsonObject?>(null)
    var cmeData by mutableStateOf<BorrowerCmeData?>(null)
    var fcoaData by mutableStateOf<JsonObject?>(null)
    var tnpoData by mutableStateOf<JsonObject?>(null)
    var isWorkProofMandatory by mutableStateOf(false)

    // Statics tracking
    private val statics = mutableMapOf(
        "company_name_use_paste_count" to 0,
        "company_name_input_time" to 0L,
        "company_name_modify_count" to 0,
        "company_scale_modify_count" to 0,
        "industry_modify_count" to 0,
        "job_modify_count" to 0,
        "job_level_modify_count" to 0,
        "work_duration_modify_count" to 0,
        "month_income_modify_count" to 0,
        "company_addr_province_modify_count" to 0,
        "company_addr_detai_paste_count" to 0,
        "company_addr_detail_modify_count" to 0,
        "company_phone_use_paste" to 0,
        "company_phone_input_time" to 0L,
        "company_phone_input_count" to 0
    )

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
                    val data = response.body()?.data
                    if (data != null) {
                        val wi = data.customer?.workInfo
                        val lotn = wi?.lotn
                        
                        // Format Alamat Kantor
                        var rtRw = ""
                        if (lotn != null && !lotn.rtidn.isNullOrEmpty() && !lotn.rwidn.isNullOrEmpty()) {
                            rtRw = "${lotn.rtidn}/${lotn.rwidn}"
                        }
                        
                        val officeAddr = buildString {
                            if (rtRw.isNotEmpty()) append("$rtRw ")
                            lotn?.lpidn?.let { if (it.isNotEmpty()) append("$it ") }
                            lotn?.lcidn?.let { if (it.isNotEmpty()) append("$it ") }
                            lotn?.ldidn?.let { if (it.isNotEmpty()) append("$it ") }
                            lotn?.viidn?.let { if (it.isNotEmpty()) append(it) } // CompanyAddress uses viidn internally
                        }.trim()

                        state = state.copy(
                            con = wi?.companyName ?: "",
                            syamt = wi?.salary?.toString() ?: "",
                            joiun = wi?.businessTypeName ?: "",
                            joi = wi?.workTypeId?.toString() ?: "",
                            join = wi?.workTypeName ?: "",
                            iniun = wi?.industryTypeName ?: "",
                            ini = wi?.economicSectorId?.toString() ?: "",
                            inin = wi?.economicSectorName ?: "",
                            jork = wi?.jobPositionId?.toString() ?: "",
                            jorkn = wi?.jobPositionName ?: "",
                            wkdn = wi?.workDurationId?.toString() ?: "",
                            wkdnn = wi?.workDurationName ?: "",
                            cstr = officeAddr,
                            cdel = lotn?.addressDetail ?: "",
                            wkptie = data.customer?.identityImages?.selfie ?: "", // Placeholder if different field
                            wkptie_yep = ""
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("JobInfoV2VM", "Init error", e)
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
                    commonParams = response.body()?.data
                }
            } catch (e: Exception) {}
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
                        fcoaData = dataObj.getAsJsonObject("fcoa")
                        tnpoData = dataObj.getAsJsonObject("tnpo")
                    }
                }
            } catch (e: Exception) {}
        }
    }

    fun updateField(newState: JobInfoV2State) {
        state = newState
    }

    fun submitInfo(onSuccess: (BorrowerCmeData?, JsonObject?, JsonObject?) -> Unit, onError: (String) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                // Post statistics if wof is false
                if (cmeData?.wof == false) {
                    val eventsArray = JsonArray()
                    statics.forEach { (key, value) ->
                        val eventObj = JsonObject()
                        eventObj.addProperty("evme", key)
                        eventObj.addProperty("eval", value.toString())
                        eventsArray.add(eventObj)
                    }
                    val eventBody = JsonObject().apply {
                        addProperty("el", eventsArray.toString())
                        addProperty("spe", "h")
                    }
                    NetworkClient.apiService.uploadEvent(eventBody)
                }

                // Prepare file info
                val fileInfo = mutableMapOf<String, Bitmap>()
                state.wkptie_bitmap?.let { 
                    fileInfo["wkptie"] = it
                }

                // Multipart request
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                
                // Add individual fields instead of wrapping in "rd"
                builder.addFormDataPart("con", state.con)
                builder.addFormDataPart("syamt", state.syamt.replace("[.,]".toRegex(), ""))
                builder.addFormDataPart("joiun", state.joiun)
                builder.addFormDataPart("joi", state.joi)
                builder.addFormDataPart("join", state.join)
                builder.addFormDataPart("iniun", state.iniun)
                builder.addFormDataPart("ini", state.ini)
                builder.addFormDataPart("inin", state.inin)
                builder.addFormDataPart("jork", state.jork)
                builder.addFormDataPart("jorkn", state.jorkn)
                builder.addFormDataPart("wkdn", state.wkdn)
                builder.addFormDataPart("wkdnn", state.wkdnn)
                builder.addFormDataPart("cdel", state.cdel)
                builder.addFormDataPart("wkptie_yep", state.wkptie_yep)
                
                state.wkptie_bitmap?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val bytes = stream.toByteArray()
                    builder.addFormDataPart("wkptie", "wkptie.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                }

                val response = NetworkClient.apiService.updateBaseInfo(builder.build()) // Reusing updateBaseInfo for multipart
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    // Refetch config to decide navigation
                    val configResponse = NetworkClient.apiService.getHomeCashConfig(JsonObject().apply { addProperty("spe", "h") })
                    if (configResponse.isSuccessful) {
                        val bodyObj = configResponse.body()?.getAsJsonObject("data")
                        val newCme = gson.fromJson(bodyObj?.get("cme"), BorrowerCmeData::class.java)
                        onSuccess(newCme, bodyObj?.getAsJsonObject("fcoa"), bodyObj?.getAsJsonObject("tnpo"))
                    } else {
                        onSuccess(null, null, null)
                    }
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
