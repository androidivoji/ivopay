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
import com.google.gson.Gson
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
    val eil: String = "",
    val lvstr: String = "",
    val cstr: String = "",
    val rtid: String = "",
    val rwid: String = "",
    val lpid: String = "",
    val lcid: String = "",
    val ldid: String = "",
    val viid: String = "",
    val cpid: String = "",
    val ccid: String = "",
    val cdid: String = "",
    val cviid: String = "",
    val crtid: String = "",
    val crwid: String = "",
    val ktpUrl: String = ""
)

// --- ViewModel ---
class BaseInfoViewModel(context: Context) : ViewModel() {
    private val sessionManager = SessionManager(context)
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
    
    private var ocrJob: Job? = null
    private var uploadJob: Job? = null
    private var aru: String = ""

    fun init() {
        fetchInitialUserInfo()
    }

    private fun fetchInitialUserInfo() {
        isLoading = true
        viewModelScope.launch {
            try {
                val response = NetworkClient.apiService.getUserInfo(JsonObject().apply { addProperty("spe", "h") })
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data
                    if (data != null) {
                        val pi = data.customer?.personalInfo
                        val lotn = data.customer?.getAddressSafe()
                        val wiLotn = data.customer?.workInfo?.lotn
                        val ide = data.customer?.identityImages
                        
                        // Format Local Address (Domisili)
                        val localRtRw = "${lotn?.rtidn ?: ""}/${lotn?.rwidn ?: ""}"
                        val localAddr = "$localRtRw ${lotn?.lpidn ?: ""} ${lotn?.lcidn ?: ""} ${lotn?.ldidn ?: ""} ${lotn?.viidn ?: ""}".trim()

                        // Format Company Address (Kantor)
                        val companyRtRw = "${wiLotn?.rtidn ?: ""}/${wiLotn?.rwidn ?: ""}"
                        val companyAddr = "$companyRtRw ${wiLotn?.lpidn ?: ""} ${wiLotn?.lcidn ?: ""} ${wiLotn?.ldidn ?: ""} ${wiLotn?.viidn ?: ""}".trim()

                        state = state.copy(
                            inm = pi?.ktpMasked ?: "",
                            funName = pi?.fullName ?: "",
                            gen = pi?.gender ?: 0,
                            genn = pi?.genderName ?: "",
                            bire = pi?.birthDate ?: "",
                            eil = pi?.email ?: "",
                            lvstr = localAddr,
                            cstr = companyAddr,
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

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun startOcrPolling() {
        ocrJob?.cancel()
        ocrJob = viewModelScope.launch {
            var retryCount = 0
            while (true) {
                if (retryCount > 0) delay(2000)
                try {
                    val requestBody = JsonObject().apply { 
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
                                    bipl = ocr.birthPlace ?: "",
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

    fun submitInfo(onSuccess: () -> Unit, onError: (String, Int) -> Unit) {
        isLoading = true
        viewModelScope.launch {
            try {
                val body = JsonObject().apply {
                    addProperty("inm", state.inm)
                    addProperty("fun", state.funName)
                    addProperty("gen", state.gen)
                    addProperty("bire", state.bire)
                    addProperty("bipl", state.bipl)
                    addProperty("eil", state.eil)
                    addProperty("lvstr", state.lvstr)
                    addProperty("cstr", state.cstr)
                    addProperty("aru", aru)
                    addProperty("lpid", state.lpid)
                    addProperty("lcid", state.lcid)
                    addProperty("ldid", state.ldid)
                    addProperty("viid", state.viid)
                    addProperty("rtid", state.rtid)
                    addProperty("rwid", state.rwid)
                }
                
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("rd", body.toString())
                    .build()

                val response = NetworkClient.apiService.updateBaseInfo(requestBody)
                if (response.isSuccessful) {
                    val code = response.body()?.get("code")?.asInt
                    if (code == 1) {
                        onSuccess()
                    } else {
                        onError(response.body()?.get("msg")?.asString ?: "Gagal simpan", code ?: 0)
                    }
                }
            } catch (e: Exception) {
                onError("Terjadi kesalahan sistem", 0)
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        ocrJob?.cancel()
    }
}
