package com.example.ivopay.app.ui.lender.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivopay.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LenderHomeViewModel(private val context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)

    private val _uiState = MutableStateFlow(LenderHomeUiState())
    val uiState: StateFlow<LenderHomeUiState> = _uiState.asStateFlow()

    init {
        getLenderConfig()
    }

    fun getLenderConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simulasi/Panggilan API _getLenderConfig
            val auss = 1 // lenderStatus (0: Review, 1: Pass, 2: Reject)
            val uico = true // status profile info

            _uiState.value = _uiState.value.copy(
                lenderStatus = auss,
                uico = uico,
                isLoading = false
            )

            if (auss == 1) {
                getBorrowerList()
            }
        }
    }

    private fun getBorrowerList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Dummy Data Peminjam
            val mockList = listOf(
                BorrowerItem(
                    ati = "ID_1001",
                    oen = "BORROWER-001",
                    tma = 2000000.0,
                    ife = 150000.0,
                    npeo = "100%",
                    bcy = "Jakarta South",
                    bpo = "Modal Usaha",
                    aut = "2026-07-27 10:00"
                ),
                BorrowerItem(
                    ati = "ID_1002",
                    oen = "BORROWER-002",
                    tma = 5000000.0,
                    ife = 400000.0,
                    npeo = "100%",
                    bcy = "Bandung",
                    bpo = "Konsumtif",
                    aut = "2026-07-27 11:30"
                )
            )

            _uiState.value = _uiState.value.copy(
                borrowList = mockList,
                isLoading = false
            )
        }
    }

    fun toggleSelectBorrower(ati: String) {
        val updatedList = _uiState.value.borrowList.map { item ->
            if (item.ati == ati) item.copy(isSelect = !item.isSelect) else item
        }
        _uiState.value = _uiState.value.copy(borrowList = updatedList)
    }

    fun onConfirmPayBack() {
        val selectedIds = _uiState.value.borrowList.filter { it.isSelect }.map { it.ati }
        if (selectedIds.isEmpty()) {
            Toast.makeText(context, "Silakan pilih peminjam", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simulasi Response API _confirmPayBack
            val noInsurance = InsuranceItem("Tanpa Asuransi", 0, 0.0, 0.0)
            val mockInsurances = listOf(noInsurance, InsuranceItem("Asuransi Jiwa", 1, 2.5, 50000.0))

            val mockDetail = FinanceDetail(
                toa = selectedIds.size,
                atma = 2000000.0 * selectedIds.size,
                trv = 150000.0 * selectedIds.size,
                iet = "30 Hari",
                iat = 2000000.0 * selectedIds.size,
                isnc = mockInsurances
            )

            _uiState.value = _uiState.value.copy(
                financeDetail = mockDetail,
                insuranceList = mockInsurances,
                showSelectLoanDesc = true,
                isLoading = false
            )
        }
    }

    fun onCreateOrder(selectedInsuranceIndex: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val insurance = _uiState.value.insuranceList.getOrNull(selectedInsuranceIndex)
            val insuranceAmount = insurance?.ima ?: 0.0

            val mockBill = FinanceBill(
                bnm = "Bank BCA",
                bkn = "BCA VA",
                pcd = "88308123456789",
                toa = _uiState.value.financeDetail.toa,
                ima = insuranceAmount,
                tpa = _uiState.value.financeDetail.iat + insuranceAmount
            )

            _uiState.value = _uiState.value.copy(
                showSelectLoanDesc = false,
                showConfirmPayPop = true,
                financeBill = mockBill,
                isLoading = false
            )
        }
    }

    fun onConfirmPayClick() {
        _uiState.value = _uiState.value.copy(
            showConfirmPayPop = false,
            showSuccessNotify = true
        )
    }

    fun copyPayCode(code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("VA Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Salin Sukses", Toast.LENGTH_SHORT).show()
    }

    fun hideSuccessNotify() {
        _uiState.value = _uiState.value.copy(showSuccessNotify = false)
    }

    fun closeSelectLoanSheet() {
        _uiState.value = _uiState.value.copy(showSelectLoanDesc = false)
    }

    fun closeConfirmPayPop() {
        _uiState.value = _uiState.value.copy(showConfirmPayPop = false)
    }
}