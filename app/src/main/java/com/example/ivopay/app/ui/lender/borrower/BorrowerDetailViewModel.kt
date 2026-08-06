package com.example.ivopay.app.ui.lender.borrower

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.R
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class BorrowerDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BorrowerDetailUiState())
    val uiState: StateFlow<BorrowerDetailUiState> = _uiState.asStateFlow()

    fun getBorrowDetail(ati: String) {
        if (ati.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val requestBody = JsonObject().apply {
                    addProperty("ati", ati)
                }
                val response = NetworkClient.apiService.getBorrowerDetail(requestBody)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.get("code")?.asInt == 1) {
                        val su = body.getAsJsonObject("data")
                        
                        // Mapping Sections
                        val sections = mutableListOf<SectionInfo>()
                        
                        // Data Pribadi
                        sections.add(SectionInfo(
                            iconRes = R.drawable.iv_borrower_ic_name,
                            title = "Data Pribadi",
                            contentList = listOf(
                                InfoItem("Borrower Nama:", su.get("oen")?.asString ?: "-"),
                                InfoItem("Jenis Kelamin:", su.get("gen")?.asString ?: "-"),
                                InfoItem("Usia:", su.get("bag")?.asString ?: "-"),
                                InfoItem("Tempat Lahir:", su.get("bipl")?.asString ?: "-")
                            )
                        ))
                        
                        // Rincian Tagihan
                        sections.add(SectionInfo(
                            iconRes = R.drawable.iv_borrower_ic_details,
                            title = "Rincian tagihan",
                            contentList = listOf(
                                InfoItem("Nilai Pinjaman:", formatRupiah(su.get("tma")?.asDouble ?: 0.0)),
                                InfoItem("Jatuh Tempo:", su.get("dud")?.asString ?: "-"),
                                InfoItem("Estimasi pendapatan:", formatRupiah(su.get("trv")?.asDouble ?: 0.0)),
                                InfoItem("Besaran Komisi:", su.get("bek")?.asString ?: "-"),
                                InfoItem("PPN Komisi:", su.get("pnk")?.asString ?: "-")
                            )
                        ))
                        
                        // Data Pekerjaan
                        sections.add(SectionInfo(
                            iconRes = R.drawable.iv_borrower_ic_work,
                            title = "Data Pekerjaan",
                            contentList = listOf(
                                InfoItem("Nama Perusahaan:", su.get("con")?.asString ?: "-"),
                                InfoItem("Jenis Usaha:", su.get("joiun")?.asString ?: "-"),
                                InfoItem("Jenis Industri:", su.get("iniun")?.asString ?: "-"),
                                InfoItem("Alamat kantor:", su.get("cad")?.asString ?: "-"),
                                InfoItem("Lama Bekerja:", su.get("wkdnn")?.asString ?: "-"),
                                InfoItem("Penghasilan Per Bulan:", su.get("syidn")?.asString ?: "-")
                            )
                        ))
                        
                        // Nilai Kredit
                        sections.add(SectionInfo(
                            iconRes = R.drawable.iv_borrower_ic_score,
                            title = "Nilai Kredit",
                            contentList = listOf(
                                InfoItem("Nilai Kredit(Total 100):", su.get("nk")?.asString ?: "-")
                            )
                        ))

                        // Mapping Borrow Records (ois)
                        val records = mutableListOf<BorrowRecord>()
                        su.getAsJsonArray("ois")?.forEach { element ->
                            val obj = element.asJsonObject
                            records.add(BorrowRecord(
                                tma = formatRupiah(obj.get("tma")?.asDouble ?: 0.0),
                                pdi = obj.get("pdi")?.asString ?: "-",
                                dun = obj.get("dun")?.asString ?: "-",
                                ods = obj.get("ods")?.asString ?: "-"
                            ))
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                borrowerSections = sections,
                                borrowRecordList = records
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }
}