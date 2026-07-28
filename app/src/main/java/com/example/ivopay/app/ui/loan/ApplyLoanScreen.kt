package com.example.ivopay.app.ui.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// Data Model Pilihan Tenor / Hari
data class LoanDayOption(
    val peo: Int,           // Jumlah hari (contoh: 7, 14, 30)
    val aow: Boolean = true // Apakah opsi aktif
)

// Data Model Rincian Pinjaman
data class LoanDetailData(
    val bkan: String = "Nudi",                 // Nama Penerima
    val bkm: String = "BCA",                   // Bank Penerima
    val baut: String = "1234567890",           // Nomor Rekening
    val sam: Long = 50_000,                    // Biaya Admin Platform
    val ife: Long = 30_000,                    // Biaya Bunga / Layanan
    val dua: Long = 1_080_000                  // Total Pengembalian
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLoanScreen(
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    minAmount: Long = 500_000L,
    maxAmount: Long = 2_000_000L,
    initialAmount: Long = 1_000_000L,
    loanDetails: LoanDetailData = LoanDetailData(),
    dayOptions: List<LoanDayOption> = listOf(
        LoanDayOption(peo = 7),
        LoanDayOption(peo = 14),
        LoanDayOption(peo = 30)
    )
) {
    // Formatting Rupiah
    val formatRp: (Long) -> String = { amount ->
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        format.format(amount)
    }

    var inputAmountText by remember { mutableStateOf((initialAmount / 1000).toString()) }
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var showInputTip by remember { mutableStateOf(false) }
    var showSignFeePop by remember { mutableStateOf(false) }
    var showSignPop by remember { mutableStateOf(false) }
    var hasSigned by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Hitung jumlah pinjaman aktual dalam Rupiah (dikali 1.000)
    val currentInputAmount = (inputAmountText.toLongOrNull() ?: 0L) * 1000L

    // Validasi input nominal
    fun validateAmount(): Boolean {
        if (currentInputAmount < minAmount || currentInputAmount > maxAmount) {
            showInputTip = true
            return false
        }
        showInputTip = false
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konfirmasi Pengajuan", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Card 1: Input Nilai Pinjaman & Jangka Waktu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nilai Pinjaman (${formatRp(minAmount)} - ${formatRp(maxAmount)})",
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputAmountText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                inputAmountText = filtered
                                validateAmount()
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showInputTip
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x0A000000),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = ".000",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (showInputTip) {
                        Text(
                            text = "Silakan masukkan kisaran jumlah pinjaman yang valid",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Pilihan Tenor
                    Text(
                        text = "Jangka Pinjaman",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    dayOptions.forEachIndexed { index, option ->
                        val isSelected = index == selectedDayIndex
                        val cardBg = if (isSelected) Color(0x0FFE5455) else Color.White
                        val borderColor = if (isSelected) Color(0xFFFE5455) else Color(0xFFE0E0E0)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .background(cardBg, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (option.aow) selectedDayIndex = index
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${option.peo} Hari",
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFFE5455) else Color(0xFF262626)
                            )

                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFFE5455) else Color(0xFFCCCCCC)
                            )
                        }
                    }
                }
            }

            // Card 2: Rincian Pinjaman
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rincian Pinjaman",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF262626)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LoanDetailRow(label = "Nama Penerima", value = loanDetails.bkan)
                    LoanDetailRow(label = "Bank Penerima", value = loanDetails.bkm)
                    LoanDetailRow(label = "Nomor Rekening", value = loanDetails.baut)
                    LoanDetailRow(label = "Jumlah Pinjaman", value = formatRp(currentInputAmount))
                    LoanDetailRow(
                        label = "Tenor Pinjaman",
                        value = "${dayOptions.getOrNull(selectedDayIndex)?.peo ?: 0} Hari"
                    )
                    LoanDetailRow(
                        label = "Biaya Admin Platform",
                        value = formatRp(loanDetails.sam),
                        showInfoIcon = true,
                        onInfoClick = { showSignFeePop = true }
                    )
                    LoanDetailRow(label = "Biaya Bunga / Layanan", value = formatRp(loanDetails.ife))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFEEEEEE)
                    )

                    LoanDetailRow(
                        label = "Total Pengembalian",
                        value = formatRp(loanDetails.dua),
                        isBold = true
                    )
                }
            }

            // Error Message Display
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Sebelumnya", color = Color(0xFF262626))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (validateAmount()) {
                            showSignPop = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Selanjutnya", color = Color.White)
                }
            }
        }
    }

    // Modal Popup Tanda Tangan Digital
    if (showSignPop) {
        AlertDialog(
            onDismissRequest = { showSignPop = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tanda Tangan Digital", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showSignPop = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = "Harap tanda tangan pada area di bawah ini untuk menyetujui pengajuan pinjaman.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulasi Pad Tanda Tangan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
                            .clickable { hasSigned = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasSigned) {
                            Text("Tanda Tangan Tersimpan ✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        } else {
                            Text("Klik di sini untuk menyetujui tanda tangan", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hasSigned) {
                            showSignPop = false
                            onSubmitSuccess()
                        } else {
                            errorMessage = "Silakan lengkapi tanda tangan terlebih dahulu"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                ) {
                    Text("Tandatangani & Ajukan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    hasSigned = false
                }) {
                    Text("Ulangi")
                }
            },
            containerColor = Color.White
        )
    }

    // Modal Popup Info Biaya Admin Platform
    if (showSignFeePop) {
        AlertDialog(
            onDismissRequest = { showSignFeePop = false },
            title = { Text("Informasi Biaya Admin", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Termasuk biaya tanda tangan digital, proses verifikasi, pembayaran, dan biaya operasional platform lainnya.",
                    fontSize = 14.sp,
                    color = Color(0xFF333333)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSignFeePop = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mengerti")
                }
            },
            containerColor = Color.White
        )
    }
}

// Sub-component Rincian Baris Data
@Composable
private fun LoanDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    showInfoIcon: Boolean = false,
    onInfoClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (isBold) Color(0xFF262626) else Color(0xFF666666),
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
            if (showInfoIcon) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF888888),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onInfoClick() }
                )
            }
        }

        Text(
            text = value,
            fontSize = 14.sp,
            color = if (isBold) Color(0xFFBD0100) else Color(0xFF262626),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}