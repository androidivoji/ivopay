package com.example.ivopay.app.ui.loan

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.SignatureCanvas
import com.example.ivopay.app.util.CommonUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLoanScreen(
    viewModel: ApplyLoanViewModel,
    onBackClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onSubmitSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    // Handle Action Events for Face Detection
    LaunchedEffect(viewModel.actionEvent) {
        viewModel.actionEvent?.let { event ->
            when (event) {
                ApplyActionEvent.StartFaceLiveDetect -> {
                    onNavigate("FaceDetection") 
                }
                ApplyActionEvent.StartFaceLiveDetectType2 -> {
                    onNavigate("FaceDetectionType2") 
                }
                ApplyActionEvent.StartAliFaceVerify -> {
                    onNavigate("AliFaceVerify")
                }
                ApplyActionEvent.StartZuluzFaceVerify -> {
                    onNavigate("ZuluzFaceVerify")
                }
            }
            viewModel.actionEvent = null
        }
    }

    LaunchedEffect(viewModel.submitSuccessNoc) {
        viewModel.submitSuccessNoc?.let { noc ->
            onSubmitSuccess(noc)
            viewModel.submitSuccessNoc = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Konfirmasi Pengajuan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8FA))
                    .verticalScroll(scrollState)
                    .padding(bottom = 120.dp)
            ) {
                // 1. Amount Input Section
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val rangeText = if (viewModel.minAmount == viewModel.maxAmount) "" 
                                        else " (${CommonUtils.formatRupiah(viewModel.minAmount.toDouble())}-${CommonUtils.formatRupiah(viewModel.maxAmount.toDouble())})"
                        Text(text = "Nilai Pinjaman$rangeText", fontSize = 14.sp, color = Color(0xFF333333))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            TextField(
                                value = viewModel.inputAmount,
                                onValueChange = { viewModel.handleAmountInput(it) },
                                readOnly = viewModel.minAmount == viewModel.maxAmount,
                                modifier = Modifier.width(viewModel.inputWidth.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x0A000000),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(text = ".000", fontSize = 32.sp, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (viewModel.showInputTip) {
                            Text(text = "Silahkan masukkan kisaran jumlah pinjaman", color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Jangka Pinjaman", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        
                        viewModel.opWithDays.forEachIndexed { index, item ->
                            val isSelected = index == viewModel.dayIdx
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(
                                        if (isSelected) Color(0x0FFE5455) else Color.White,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFFE5455) else Color(0xFFEEEEEE),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.onDayItemClick(index) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${item.peo} hari", color = if (isSelected) Color(0xFFFE5455) else Color(0xFF262626))
                                Icon(
                                    painter = painterResource(id = if (isSelected) R.drawable.iv_choose2_sel else R.drawable.iv_choose2_nor),
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFFE5455) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Loan Details Section
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val loanData = getLoanDataList(viewModel)
                        loanData.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = key, color = Color.Gray, fontSize = 14.sp)
                                    if (key == "Biaya Admin Platform") {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp).padding(start = 4.dp).clickable { viewModel.showSignFeePop = true },
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // 3. Agreement Checkbox
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { viewModel.showRiplayDialog = true },
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = viewModel.isRiplayAgreed,
                        onCheckedChange = { viewModel.showRiplayDialog = true },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFE5455))
                    )
                    val annotatedString = buildAnnotatedString {
                        append("Silahkan dicheck dan konfirmasi ")
                        withStyle(style = SpanStyle(color = Color(0xFFFE5455), fontWeight = FontWeight.SemiBold)) {
                            append("《Ringkasan Informasi Produk dan Layanan (RIPLAY)》")
                        }
                    }
                    Text(
                        text = annotatedString,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            // Bottom Buttons
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sebelumnya", color = Color(0xFF666666))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            viewModel.onNextClick(
                                onSign = { viewModel.showSignPop = true },
                                onSuccess = { noc ->
                                    onSubmitSuccess(noc)
                                },
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        enabled = viewModel.isRiplayAgreed,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFE5455),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Selanjutnya")
                    }
                }
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }
        }
    }

    // 1. Signature Popup
    if (viewModel.showSignPop) {
        Dialog(onDismissRequest = { viewModel.showSignPop = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tanda Tangan Digital", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.showSignPop = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                    Text(
                        text = "Harap tanda tangan di sini, dan klik tombol [OK] untuk menyimpan tanda tangan setelah tanda tangan",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    SignatureCanvas(
                        onClear = { viewModel.signImage = null },
                        onSubmit = { bitmap ->
                            viewModel.onSignatureSubmit(bitmap)
                        }
                    )
                }
            }
        }
    }

    // 2. RIPLAY Full Agreement Dialog
    if (viewModel.showRiplayDialog) {
        Dialog(onDismissRequest = { viewModel.showRiplayDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.showRiplayDialog = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Konfirmasi Perjanjian", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Ringkasan Informasi Produk dan Layanan (RIPLAY)", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                                Text("Penerima Dana", color = Color(0xFFFE5455), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "Harap baca dan konfirmasi informasi berikut sebelum melanjutkan",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFA8C16),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }

                        itemsIndexed(viewModel.riplayPoints) { index, point ->
                            RiplayPointItem(
                                point = point,
                                onCheckedChange = { checked ->
                                    viewModel.toggleRiplayPoint(index, checked)
                                },
                                loanDetails = getRiplayLoanDetails(viewModel)
                            )
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }

                        item {
                            RiplayFooter()
                        }
                    }

                    // Button
                    Button(
                        onClick = { viewModel.confirmRiplay() },
                        enabled = viewModel.riplayPoints.all { it.checked },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBD0100),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Konfirmasi & Ajukan Pinjaman", color = Color.White)
                    }
                }
            }
        }
    }

    // 3. Admin Fee Popup
    if (viewModel.showSignFeePop) {
        Dialog(onDismissRequest = { viewModel.showSignFeePop = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Termasuk tanda tangan digital, pembayaran dan biaya lainnya", color = Color(0xFF262626))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.showSignFeePop = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                        Text("Jadi begitu")
                    }
                }
            }
        }
    }
}

@Composable
fun RiplayPointItem(
    point: RiplayPoint,
    onCheckedChange: (Boolean) -> Unit,
    loanDetails: Map<String, String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = point.checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFBD0100))
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(point.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF333333))
            
            if (point.title.contains("3. Fitur Utama")) {
                FiturUtamaSection(loanDetails)
            } else if (point.title.contains("6. Persyaratan")) {
                PersyaratanSection()
            } else if (point.title.contains("7. Biaya")) {
                BiayaSection(loanDetails)
            } else if (point.title.contains("8. Informasi Tambahan")) {
                InformasiTambahanSection()
            } else if (point.title.contains("9. Penafian")) {
                DisclaimerSection()
            } else {
                Text(point.content, fontSize = 13.sp, color = Color(0xFF666666), lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun FiturUtamaSection(details: Map<String, String>) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        details.forEach { (k, v) ->
            Text("• $k : $v", fontSize = 13.sp, color = Color(0xFF666666))
        }
        Text(
            text = "*Suku bunga pendanaan IVOCASH dari Ivoji diatur sesuai dengan ketentuan OJK untuk memastikan perlindungan konsumen dan transparansi dalam proses pendanaan. Bunga pinjaman dihitung berdasarkan jangka waktu pendanaan yang dipilih pengguna, dengan bunga harian maksimal sebesar 0,3% per hari.",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            fontStyle = FontStyle.Italic,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun PersyaratanSection() {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text("Persyaratan Pengajuan Pendanaan:", fontSize = 13.sp, color = Color(0xFF333333), textDecoration = TextDecoration.Underline)
        Text("Calon penerima dana wajib berusia diatas 18 tahun, memiliki KTP terbaru, mempunyai penghasilan, serta memiliki rekening bank pribadi yang sesuai dengan data identitas.", fontSize = 12.sp, color = Color(0xFF666666), modifier = Modifier.padding(vertical = 4.dp))
        
        Text("Tata Cara Pengajuan Pendanaan:", fontSize = 13.sp, color = Color(0xFF333333), textDecoration = TextDecoration.Underline)
        Text("Pengajuan pendanaan dilakukan melalui aplikasi Ivoji dengan mengunduh aplikasi, melakukan registrasi nomor ponsel, melengkapi data pribadi, unggah KTP, verifikasi wajah dan rekening bank, memilih jumlah serta tenor pendanaan, kemudian mengajukan permohonan pendanaan untuk diproses.", fontSize = 12.sp, color = Color(0xFF666666), modifier = Modifier.padding(vertical = 4.dp))
        
        Surface(color = Color(0xFFF9F9F9), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Pengguna dapat menyampaikan pertanyaan melalui:", fontSize = 12.sp, color = Color(0xFF333333), textDecoration = TextDecoration.Underline)
                Text("• Email : customer@ivoji.id", fontSize = 11.sp, color = Color(0xFF666666))
                Text("• Telepon : 021-30208005", fontSize = 11.sp, color = Color(0xFF666666))
                Text("• Alamat : Jalan H. R. Rasuna Said Nomor B12, Lantai 16 Blok E2, Karet Kuningan, Setiabudi, Jakarta Selatan 12940", fontSize = 11.sp, color = Color(0xFF666666))
            }
        }
        Text("Jam Operasional: Senin – Sabtu, 09.00 – 17.00 WIB", fontSize = 12.sp, color = Color(0xFFFE5455), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun BiayaSection(details: Map<String, String>) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text("• Total Bunga : ${details["Total Bunga"] ?: "Rp. 0"}", fontSize = 13.sp, color = Color(0xFF666666))
        Text("• Tanda Tangan Digital : ${details["Biaya Tanda Tangan"] ?: "Rp. 0"} (dipotong diawal)", fontSize = 13.sp, color = Color(0xFF666666))
        Text("• Denda Keterlambatan : 0.3% per hari + 0.3% per hari (biaya denda + bunga %)", fontSize = 13.sp, color = Color(0xFF666666))
    }
}

@Composable
fun InformasiTambahanSection() {
    val items = listOf(
        "a. IVOJI merupakan Penyelenggara LPBBTI yang berizin dan diawasi oleh OJK.",
        "b. Pengguna wajib membaca dan memahami syarat dan ketentuan pendanaan sebelum mengajukan pendanaan.",
        "c. Pendanaan melalui LPBBTI memiliki risiko, termasuk keterlambatan pembayaran, denda, penurunan skoring kredit, dan pelaporan ke SLIK OJK.",
        "d. Data pribadi pengguna diproses dan dilindungi sesuai kebijakan privasi.",
        "e. Pengguna wajib memberikan data dan informasi yang benar dan akurat.",
        "f. IVOJI tidak memungut biaya diluar biaya yang telah diinformasikan.",
        "g. Persetujuan pendanaan dilakukan berdasarkan hasil analisis dan evaluasi risiko.",
        "h. Pengguna dihimbau menggunakan layanan pendanaan secara bijak.",
        "i. IVOJI tidak pernah meminta pembayaran ke rekening pribadi dalam proses pengajuan.",
        "j. Dengan mengajukan pendanaan, pengguna dianggap telah membaca dan menyetujui syarat.",
        "k. Dalam hal terjadi keterlambatan, akan dilakukan penagihan sesuai ketentuan."
    )
    Column(modifier = Modifier.padding(top = 4.dp)) {
        items.forEach {
            Text(it, fontSize = 11.sp, color = Color(0xFF666666), lineHeight = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
fun DisclaimerSection() {
    val items = listOf(
        "a. Anda telah membaca, menerima penjelasan, dan memahami produk pendanaan sesuai RIPLAY.",
        "b. Ringkasan ini hanya digunakan sebagai referensi dan bukan merupakan perjanjian mengikat.",
        "c. Informasi ini berlaku sejak tanggal cetak dokumen sampai dengan selesainya kewajiban.",
        "d. Anda harus membaca dengan teliti sebelum menyetujui dan berhak bertanya kepada pegawai."
    )
    Column(modifier = Modifier.padding(top = 4.dp)) {
        items.forEach {
            Text(it, fontSize = 11.sp, color = Color(0xFF666666), lineHeight = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
fun RiplayFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 20.dp)
            .border(width = 0.5.dp, color = Color(0xFFDDDDDD))
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
            Image(painter = painterResource(id = R.drawable.iv_invest_logo), contentDescription = "logo", modifier = Modifier.size(50.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "PT Finansia Aira Teknologi Berizin dan Diawasi Otoritas Jasa Keuangan",
                fontSize = 11.sp,
                color = Color(0xFF666666),
                lineHeight = 15.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Date:", fontSize = 12.sp, color = Color(0xFF666666))
            Text(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
    }
}

private fun getLoanDataList(viewModel: ApplyLoanViewModel): List<Pair<String, String>> {
    val bio = viewModel.cashData?.bio
    val loanOp = viewModel.getCurLoanOption()
    val dayOp = viewModel.getCurDayOption()
    
    return listOf(
        "Name" to (bio?.bkan ?: ""),
        "Beneficiary Bank" to (bio?.bkm ?: ""),
        "Bank Number" to (bio?.baut ?: ""),
        "Loan Amount" to CommonUtils.formatRupiah(loanOp?.tma?.toDouble()),
        "Loan Date" to "${dayOp?.peo ?: 0}days",
        "Biaya Admin Platform" to CommonUtils.formatRupiah(loanOp?.sam?.toDouble()),
        "Bunga" to CommonUtils.formatRupiah(loanOp?.ife?.toDouble()),
        "Repayment Amount" to CommonUtils.formatRupiah(loanOp?.dua?.toDouble())
    )
}

private fun getRiplayLoanDetails(viewModel: ApplyLoanViewModel): Map<String, String> {
    val loanOp = viewModel.getCurLoanOption()
    val dayOp = viewModel.getCurDayOption()
    return mapOf(
        "Jumlah Pendanaan" to CommonUtils.formatRupiah(loanOp?.tma?.toDouble()),
        "Suku Bunga*" to "${viewModel.cashData?.itrp ?: 0.3} % /Hari",
        "Jangka Waktu Pendanaan/Tenor" to "${dayOp?.peo ?: 14} Hari",
        "Pendanaan yang Diterima" to CommonUtils.formatRupiah(viewModel.cashData?.dam?.toDouble() ?: (loanOp?.tma?.minus(loanOp.sam ?: 0L))?.toDouble()),
        "Biaya Tanda Tangan" to CommonUtils.formatRupiah(loanOp?.sam?.toDouble()),
        "Jumlah Pengembalian" to CommonUtils.formatRupiah(loanOp?.dua?.toDouble()),
        "Total Bunga" to CommonUtils.formatRupiah(loanOp?.ife?.toDouble())
    )
}
