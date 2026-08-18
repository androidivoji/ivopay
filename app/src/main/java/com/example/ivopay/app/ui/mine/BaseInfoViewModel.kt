package com.example.ivopay.app.ui.mine

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.data.api.NetworkClient
import com.example.ivopay.app.data.model.*
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.SessionManager
import com.example.ivopay.app.util.SystemBridge
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

// --- Models ---
data class AddressItem(
    @SerializedName("c") val code: String? = null,
    @SerializedName("n") val name: String? = null,
    @SerializedName("pc") val postalCode: String? = null
)

data class OcrResult(
    @SerializedName("o_ats") val status: Int = 0,
    @SerializedName("o_imb") val nik: String? = null,
    @SerializedName("o_ged") val gender: Int = 0,
    @SerializedName("o_bit") val birthDate: String? = null,
    @SerializedName("o_brp") val birthPlace: String? = null,
    @SerializedName("o_fme") val fullName: String? = null
)

data class BaseInfoState(
    val inm: String = "",
    val funName: String = "",
    val gen: Int = 0,
    val genn: String = "",
    val bire: String = "",
    val bipl: String = "",
    val poco: String = "",
    val eil: String = "",
    val lvstr: String = "",
    val cstr: String = "",
    val rtid: String = "",
    val rtidn: String = "",
    val rwid: String = "",
    val rwidn: String = "",
    val lpid: String = "",
    val lpidn: String = "",
    val lcid: String = "",
    val lcidn: String = "",
    val ldid: String = "",
    val ldidn: String = "",
    val viid: String = "",
    val viidn: String = "",
    val cpid: String = "",
    val cpidn: String = "",
    val ccid: String = "",
    val ccidn: String = "",
    val cdid: String = "",
    val cdidn: String = "",
    val cviid: String = "",
    val cviidn: String = "",
    val crtid: String = "",
    val crtidn: String = "",
    val crwid: String = "",
    val crwidn: String = "",
    val ktpUrl: String = ""
)

// --- ViewModel ---
class BaseInfoViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
    private val systemBridge = SystemBridge(context)
    private val gson = Gson()

    var state by mutableStateOf(BaseInfoState())
    var isLoading by mutableStateOf(false)
    var showOCRLoading by mutableStateOf(false)
    var ocrAts by mutableStateOf(0)
    var capturedBitmap by mutableStateOf<Bitmap?>(null)
    var canUpdateIdFile by mutableStateOf(false)
    
    var addressList by mutableStateOf<List<AddressItem>>(emptyList())
    var currentAddressLevel by mutableIntStateOf(1)
    var addressPickerTitle by mutableStateOf("Provinsi")
    var commonParams by mutableStateOf<JsonObject?>(null)
    var cmeData by mutableStateOf<BorrowerCmeData?>(null)
    
    // Logic flags from Vue
    var checkAgree by mutableStateOf(false)
    var idNumConfirmed by mutableStateOf(false)
    var showConfirmInfoPop by mutableStateOf(false)
    var showHaveBillPop by mutableStateOf(false)
    var showInfoErrorPop by mutableStateOf(false)
    var errMsg by mutableStateOf("")
    var errCode by mutableIntStateOf(0)
    
    private var ocrJob: Job? = null
    private var uploadJob: Job? = null
    private var aru: String = ""

    // Statics tracking
    private val statics = mutableMapOf(
        "id_num_user_paste_count" to 0,
        "id_num_modify_count" to 0
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
                val requestBody = systemBridge.getCommonParamsJson().apply { addProperty("spe", "h") }
                val response = NetworkClient.apiService.getUserInfo(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data
                    if (data != null) {
                        val pi = data.customer?.personalInfo
                        val lotn = data.customer?.address 
                        val wiLotn = data.customer?.workInfo?.lotn
                        val ide = data.customer?.identityImages
                        
                        // Format Local Address (Domisili)
                        val localRtRw = if (lotn != null) "${lotn.rtidn ?: ""}/${lotn.rwidn ?: ""}" else ""
                        val localAddr = if (lotn != null) {
                            "$localRtRw ${lotn.lpidn ?: ""} ${lotn.lcidn ?: ""} ${lotn.ldidn ?: ""} ${lotn.viidn ?: ""}".trim()
                        } else ""

                        // Format Company Address (Kantor)
                        val companyRtRw = if (wiLotn != null) "${wiLotn.rtidn ?: ""}/${wiLotn.rwidn ?: ""}" else ""
                        val companyAddr = if (wiLotn != null) {
                            "$companyRtRw ${wiLotn.lpidn ?: ""} ${wiLotn.lcidn ?: ""} ${wiLotn.ldidn ?: ""} ${wiLotn.viidn ?: ""}".trim()
                        } else ""

                        state = state.copy(
                            inm = pi?.ktpMasked ?: "",
                            funName = pi?.fullName ?: "",
                            gen = pi?.gender ?: 0,
                            genn = pi?.genderName ?: "",
                            bire = pi?.birthDate ?: "",
                            bipl = lotn?.birthPlace ?: "", // Ambil dari CustomerAddress.bipl
                            eil = pi?.email ?: "",
                            lvstr = localAddr,
                            cstr = companyAddr,
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
                            poco = lotn?.postCode ?: "", // Pastikan poco tersimpan
                            cpid = wiLotn?.lpid ?: "",
                            cpidn = wiLotn?.lpidn ?: "",
                            ccid = wiLotn?.lcid ?: "",
                            ccidn = wiLotn?.lcidn ?: "",
                            cdid = wiLotn?.ldid ?: "",
                            cdidn = wiLotn?.ldidn ?: "",
                            cviid = wiLotn?.viid ?: "",
                            cviidn = wiLotn?.viidn ?: "",
                            crtid = wiLotn?.rtid ?: "",
                            crtidn = wiLotn?.rtidn ?: "",
                            crwid = wiLotn?.rwid ?: "",
                            crwidn = wiLotn?.rwidn ?: "",
                            ktpUrl = ide?.ktpFront ?: ""
                        )
                        
                        canUpdateIdFile = data.customer?.personalInfo?.inmt != 1
                    }
                }
            } catch (e: Exception) {
                Log.e("BaseInfoVM", "Init error", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchCommonParams() {
        viewModelScope.launch {
            try {
                val requestBody = systemBridge.getCommonParamsJson()
                val response = NetworkClient.apiService.getCommonConfig(requestBody)
                if (response.isSuccessful) {
                    commonParams = response.body()?.data
                }
            } catch (e: Exception) {
                Log.e("BaseInfoVM", "fetchCommonParams error", e)
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
            } catch (e: Exception) {}
        }
    }

    fun uploadKtp(bitmap: Bitmap) {
        if (uploadJob?.isActive == true) return
        
        capturedBitmap = bitmap
        showOCRLoading = true
        ocrAts = 0
        
        uploadJob = viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                val bytes = stream.toByteArray()
                aru = CommonUtils.generateSessionId()
                
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("aru", aru)
                    .addFormDataPart("spe", "h")
                    .addFormDataPart("oim", "ktp.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                    .build()

                val response = NetworkClient.apiService.uploadOcrPhoto(requestBody)
                if (response.isSuccessful && response.body()?.get("code")?.asInt == 1) {
                    startOcrPolling()
                } else {
                    showOCRLoading = false
                }
            } catch (e: Exception) {
                showOCRLoading = false
            }
        }
    }

    private fun startOcrPolling() {
        ocrJob?.cancel()
        ocrJob = viewModelScope.launch {
            var retryCount = 0
            while (true) {
                if (retryCount > 0) delay(2000)
                try {
                    val requestBody = systemBridge.getCommonParamsJson().apply { 
                        addProperty("aru", aru)
                        addProperty("spe", "h")
                    }
                    val response = NetworkClient.apiService.getOcrResult(requestBody)
                    if (response.isSuccessful) {
                        val body = response.body()
                        val dataElement = body?.get("data")
                        if (dataElement != null && !dataElement.isJsonNull) {
                            val ocr = gson.fromJson(dataElement, OcrResult::class.java)
                            ocrAts = ocr.status
                            if (ocr.status == 2) {
                                state = state.copy(
                                    inm = ocr.nik ?: "",
                                    funName = ocr.fullName ?: "",
                                    bire = formatOcrDate(ocr.birthDate),
                                    gen = ocr.gender,
                                    genn = if (ocr.gender == 1) "Laki-laki" else "Perempuan"
                                )
                                showOCRLoading = false
                                break
                            } else if (ocr.status == 3) {
                                showOCRLoading = false
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    showOCRLoading = false
                    break
                }
                retryCount++
                if (retryCount > 15) {
                    showOCRLoading = false
                    break
                }
            }
        }
    }

    private fun formatOcrDate(rawDate: String?): String {
        if (rawDate.isNullOrEmpty()) return ""
        return rawDate
    }

    fun loadAddresses(level: Int, parentId: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("lvl", level)
                    addProperty("pad", parentId)
                }
                val response = NetworkClient.apiService.getAddressList(requestBody)
                if (response.isSuccessful) {
                    val listType = object : com.google.gson.reflect.TypeToken<List<AddressItem>>() {}.type
                    addressList = gson.fromJson(response.body()?.get("data"), listType)
                    currentAddressLevel = level
                    addressPickerTitle = when(level) {
                        1 -> "Provinsi"
                        2 -> "Kota"
                        3 -> "Kecamatan"
                        else -> "Desa"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateField(newState: BaseInfoState) {
        state = newState
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

    private fun postEventsListBaseInfo() {
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
                    addProperty("eli", eventsArray.toString())
                    addProperty("spe", "h")
                }
                NetworkClient.apiService.postEventsListBaseInfo(body)
            } catch (e: Exception) {}
        }
    }

    fun submitInfo(onSuccess: (targetRoute: String?) -> Unit) {
        // Validation check from Vue
        if (cmeData?.wiue == true && !checkAgree) {
            errMsg = "Anda harus menyetujui persyaratan dan ketentuan"
            // Show toast or handling error
            return
        }
        
        if (!idNumConfirmed) {
            showConfirmInfoPop = true
            return
        }

        if (capturedBitmap == null && state.ktpUrl.isEmpty()) {
            errMsg = "Harap Pilih Foto Terlebih Dahulu!"
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                // Tracking stats
                if (cmeData?.wof == false) {
                    postEventsListBaseInfo()
                }

                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                
                builder.addFormDataPart("inm", if (state.inm.contains("*")) "" else state.inm)
                builder.addFormDataPart("fun", state.funName)
                builder.addFormDataPart("gen", state.gen.toString())
                builder.addFormDataPart("bire", state.bire)
                builder.addFormDataPart("bipl", state.bipl) // Gunakan state.bipl asli
                builder.addFormDataPart("poco", state.poco) // Tambahkan poco dari pc
                builder.addFormDataPart("eil", state.eil)
                builder.addFormDataPart("lvstr", state.lvstr)
                builder.addFormDataPart("cstr", state.cstr)
                builder.addFormDataPart("aru", aru)
                builder.addFormDataPart("lpid", state.lpid)
                builder.addFormDataPart("lcid", state.lcid)
                builder.addFormDataPart("ldid", state.ldid)
                builder.addFormDataPart("viid", state.viid)
                builder.addFormDataPart("rtid", state.rtid)
                builder.addFormDataPart("rwid", state.rwid)
                builder.addFormDataPart("cpid", state.cpid)
                builder.addFormDataPart("ccid", state.ccid)
                builder.addFormDataPart("cdid", state.cdid)
                builder.addFormDataPart("cviid", state.cviid)
                builder.addFormDataPart("crtid", state.crtid)
                builder.addFormDataPart("crwid", state.crwid)
                builder.addFormDataPart("cgen", "1")
                builder.addFormDataPart("cbire", "1")
                
                capturedBitmap?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream) // Kualitas 95
                    val bytes = stream.toByteArray()
                    builder.addFormDataPart("idfie", "ktp.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                }

                val response = NetworkClient.apiService.updateBaseInfo(builder.build())
                isLoading = false

                if (response.isSuccessful) {
                    val resData = response.body()
                    val code = resData?.get("code")?.asInt
                    if (code == 1) {
                        val dataObj = resData.getAsJsonObject("data")
                        
                        // Check Have Bill Pop
                        if (dataObj?.get("oivoex")?.asBoolean == true) {
                            showHaveBillPop = true
                        }
                        
                        uploadTrackingEvent("P22")

                        if (dataObj?.get("is_new")?.asBoolean == true) {
                            // lackinFlo logic
                            val lackinResp = NetworkClient.apiService.getLackinFlo(JsonObject().apply { addProperty("spe", "h") })
                            // Refresh Config & Next
                            onSuccess(null)
                        } else {
                            onSuccess(null)
                        }
                    } else {
                        handleError(code ?: 0, resData?.get("msg")?.asString ?: "Gagal simpan")
                    }
                } else {
                    handleError(response.code(), "Server Error")
                }
            } catch (e: Exception) {
                isLoading = false
                errMsg = "Terjadi kesalahan sistem"
            }
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == 8 || code == 9 || code == 104) {
            errCode = code
            errMsg = if (code == 104) "KTP ini sudah terkoneksi ke nomor HP lain, apakah perlu dikoneksikan kembali?" else message
            showInfoErrorPop = true
            uploadTrackingEvent("N30")
        } else {
            errMsg = message
        }
    }

    override fun onCleared() {
        ocrJob?.cancel()
    }
}
