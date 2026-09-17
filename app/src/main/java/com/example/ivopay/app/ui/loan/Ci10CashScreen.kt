package com.example.ivopay.app.ui.loan

import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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
import com.example.ivopay.app.data.model.RiplayPoint
import com.example.ivopay.app.ui.components.SignatureCanvas
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.CommonUtils
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ci10CashScreen(
    viewModel: Ci10CashViewModel,
    rasn: String,
    onBack: () -> Unit,
    onNavigateToFace: (String) -> Unit,
    onSuccess: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(rasn) {
        viewModel.init(rasn)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Konfirmasi Pengajuan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F8FA)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp)
            ) {
                // 1. Amount Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Nilai Pinjaman(Rp)", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = CommonUtils.formatMoneyOnly(viewModel.selAmount.toDouble()),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF262626)
                        )
                        
                        val maxIdx = viewModel.maxAmountIndex.toFloat().coerceAtLeast(0.01f)
                        Slider(
                            value = viewModel.amountIdx.toFloat().coerceIn(0f, maxIdx),
                            onValueChange = { 
                                viewModel.amountIdx = kotlin.math.round(it).toInt()
                                viewModel.fetchInlgBillPre()
                            },
                            onValueChangeFinished = {
                                val options = viewModel.curDayOption?.dop ?: emptyList()
                                if (viewModel.amountIdx < options.size && !options[viewModel.amountIdx].aow) {
                                    viewModel.amountIdx = viewModel.maxAllowedAmountIndex
                                    viewModel.fetchInlgBillPre()
                                    Toast.makeText(context, "Ajukan dan lunasi tepat waktu lebih dari 3x untuk tingkatkan limit pinjamanmu.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            valueRange = 0f..maxIdx,
                            steps = (viewModel.maxAmountIndex - 1).coerceAtLeast(0),
                            enabled = viewModel.maxAmountIndex > 0,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFE5455),
                                activeTrackColor = Color(0xFFFE5455)
                            ),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${CommonUtils.formatMoneyOnly(viewModel.minAmount.toDouble() / 1000000)} JT", fontSize = 13.sp, color = Color.Gray)
                            Text(text = "${CommonUtils.formatMoneyOnly(viewModel.maxAmount.toDouble() / 1000000)} JT", fontSize = 13.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "Jangka Pinjaman", modifier = Modifier.fillMaxWidth(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        
                        // Months Grid (Manual Row for 3 columns)
                        val tpos = viewModel.cashData?.tpos ?: emptyList()
                        tpos.chunked(3).forEach { rowItems ->
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowItems.forEach { item ->
                                    val idx = tpos.indexOf(item)
                                    val isSelected = idx == viewModel.dayIdx
                                    val isAow = item.aow

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .background(
                                                color = if (!isAow) Color(0xFFF2F2F2) else if (isSelected) Color(0x0FFE5455) else Color.White,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected && isAow) Color(0xFFFE5455) else Color(0xFFEEEEEE),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable(enabled = isAow) { 
                                                viewModel.dayIdx = idx
                                                viewModel.fetchInlgBillPre()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!isAow) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.iv_home_loan_lock),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp).padding(end = 4.dp),
                                                    tint = Color.Gray
                                                )
                                            }
                                            Text(
                                                text = "${item.bpio} bulan",
                                                fontSize = 13.sp,
                                                color = if (!isAow) Color.Gray else if (isSelected) Color(0xFFFE5455) else Color(0xFF262626)
                                            )
                                        }
                                        if (isSelected && isAow) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.iv_choose_sel), // using sel icon
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp).align(Alignment.BottomEnd),
                                                tint = Color.Unspecified
                                            )
                                        }
                                    }
                                }
                                // Fill empty slots
                                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }

                // Phone Code Section
                if (viewModel.cashData?.nvmp == true) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Kode Verifikasi Ponsel", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Kode akan dikirimkan ke nomor ${viewModel.cashData?.mob ?: "--"}", fontSize = 12.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = viewModel.verCode,
                                    onValueChange = { if (it.length <= 6) viewModel.verCode = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Masukkan kode") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Button(
                                    onClick = { viewModel.sendCode { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } },
                                    enabled = viewModel.verCountDown == 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.verCountDown == 0) Color(0xFFFE5455) else Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (viewModel.verCountDown == 0) "Ambil Kode" else "${viewModel.verCountDown}s",
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Loan Data List Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        viewModel.getLoanData().forEach { (key, value) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = key, color = Color.Gray, fontSize = 14.sp)
                                Text(text = value, color = Color(0xFF262626), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // 3. Installment Preview (EWB)
                if (viewModel.ewb.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            viewModel.ewb.forEachIndexed { idx, item ->
                                Column(modifier = Modifier.padding(bottom = 12.dp, top = if (idx > 0) 12.dp else 0.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.width(3.dp).height(14.dp).background(Color(0xFFBD0100)))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Tagihan ${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF262626))
                                        }
                                        if (item.byep == 1) {
                                            Text(text = "Gratis", color = Color(0xFFBD0100), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Repayment Amount", color = Color.Gray, fontSize = 14.sp)
                                        Text(text = CommonUtils.formatRupiah(item.otma.toDouble()), color = Color(0xFF262626), fontWeight = FontWeight.Medium)
                                    }
                                    
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Repayment Time", color = Color.Gray, fontSize = 14.sp)
                                        Text(text = item.rdn ?: "--", color = Color(0xFF262626))
                                    }
                                }
                                if (idx < viewModel.ewb.size - 1) HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // 4. RIPLAY Checkbox
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
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Sebelumnya", color = Color(0xFF262626))
                    }
                    Button(
                        onClick = { 
                            if (viewModel.cashData?.nvmp == true) {
                                viewModel.checkCode(
                                    onSuccess = { viewModel.showSignPop = true },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            } else {
                                viewModel.showSignPop = true 
                            }
                        },
                        enabled = viewModel.isRiplayAgreed,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFE5455),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Selanjutnya")
                    }
                }
            }
        }
    }

    // 1. Signature Dialog
    if (viewModel.showSignPop) {
        Dialog(onDismissRequest = { viewModel.showSignPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Tanda Tangan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { viewModel.showSignPop = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(
                        text = "Harap tanda tangan di sini, dan klik tombol [Tandatangani] untuk menyimpan tanda tangan setelah tanda tangan",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    SignatureCanvas(
                        onClear = { /* No-op */ },
                        onSubmit = { bitmap ->
                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                            viewModel.signImageBase64 = base64
                            viewModel.showSignPop = false
                            onNavigateToFace("Ci10Cash") 
                        }
                    )
                }
            }
        }
    }

    // 2. RIPLAY Dialog
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
                            Ci10RiplayPointItem(
                                point = point,
                                onCheckedChange = { checked ->
                                    viewModel.toggleRiplayPoint(index, checked)
                                },
                                loanDetails = getCi10RiplayLoanDetails(viewModel),
                                ewbList = viewModel.ewb
                            )
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }

                        item {
                            Ci10RiplayFooter()
                        }
                    }

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

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFE5455))
        }
    }
}

@Composable
fun Ci10RiplayPointItem(
    point: RiplayPoint,
    onCheckedChange: (Boolean) -> Unit,
    loanDetails: Map<String, String>,
    ewbList: List<com.example.ivopay.app.data.model.TadpoleBillItem> = emptyList()
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
                Ci10FiturUtamaSection(loanDetails, ewbList)
            } else if (point.title.contains("6. Persyaratan")) {
                Ci10PersyaratanSection()
            } else if (point.title.contains("7. Biaya")) {
                Ci10BiayaSection(loanDetails)
            } else if (point.title.contains("8. Informasi Tambahan")) {
                Ci10InformasiTambahanSection()
            } else if (point.title.contains("9. Penafian")) {
                Ci10DisclaimerSection()
            } else {
                Text(point.content, fontSize = 13.sp, color = Color(0xFF666666), lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun Ci10FiturUtamaSection(
    details: Map<String, String>,
    ewbList: List<com.example.ivopay.app.data.model.TadpoleBillItem> = emptyList()
) {
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

        if (ewbList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Rincian Angsuran", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF262626))
                    Spacer(modifier = Modifier.height(12.dp))
                    ewbList.forEachIndexed { idx, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.width(3.dp).height(14.dp).background(Color(0xFFBD0100)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Tagihan ${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    if (item.byep == 1) {
                                        Text(text = "Gratis", color = Color(0xFFBD0100), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Repayment Amount", color = Color.Gray, fontSize = 12.sp)
                                    Text(text = CommonUtils.formatRupiah(item.otma.toDouble()), color = Color(0xFF262626), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Repayment Time", color = Color.Gray, fontSize = 12.sp)
                                    Text(text = item.rdn ?: "--", color = Color(0xFF262626), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Ci10PersyaratanSection() {
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
fun Ci10BiayaSection(details: Map<String, String>) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text("• Total Bunga : ${details["Total Bunga"] ?: "Rp. 0"}", fontSize = 13.sp, color = Color(0xFF666666))
        Text("• Tanda Tangan Digital : ${details["Biaya Tanda Tangan"] ?: "Rp. 0"} (dipotong diawal)", fontSize = 13.sp, color = Color(0xFF666666))
        Text("• Denda Keterlambatan : 0.3% per hari + 0.3% per hari (biaya denda + bunga %)", fontSize = 13.sp, color = Color(0xFF666666))
    }
}

@Composable
fun Ci10InformasiTambahanSection() {
    val items = listOf(
        "a. IVOJI merupakan Penyelenggara LPBBTI yang berizin dan diawasi oleh OJK.",
        "b. Pengguna wajib membaca dan memahami syarat dan ketentuan pendanaan sebelum mengajukan pendanaan.",
        "c. Pendanaan melalui LPBBTI memiliki risiko, termasuk keterlambatan pembayaran, denda, penurunan skoring kredit, and pelaporan ke SLIK OJK.",
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
fun Ci10DisclaimerSection() {
    val items = listOf(
        "a. Anda telah membaca, menerima penjelasan, and memahami produk pendanaan sesuai RIPLAY.",
        "b. Ringkasan ini hanya digunakan sebagai referensi dan bukan merupakan perjanjian mengikat.",
        "c. Informasi ini berlaku sejak tanggal cetak dokumen sampai dengan selesainya kewajiban.",
        "d. Anda harus membaca dengan teliti sebelum menyetujui and berhak bertanya kepada pegawai."
    )
    Column(modifier = Modifier.padding(top = 4.dp)) {
        items.forEach {
            Text(it, fontSize = 11.sp, color = Color(0xFF666666), lineHeight = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
fun Ci10RiplayFooter() {
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
            androidx.compose.foundation.Image(painter = painterResource(id = R.drawable.iv_invest_logo), contentDescription = "logo", modifier = Modifier.size(50.dp))
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

private fun getCi10RiplayLoanDetails(viewModel: Ci10CashViewModel): Map<String, String> {
    val loanOp = viewModel.curLoanOption
    val dayOp = viewModel.curDayOption
    return linkedMapOf(
        "Jumlah Pendanaan" to CommonUtils.formatRupiah(loanOp?.tma?.toDouble()),
        "Biaya yang dipotong saat pencairan" to CommonUtils.formatRupiah(loanOp?.sam?.toDouble()),
        "Suku Bunga*" to "0.3 % /Hari",
        "Total Biaya Bunga" to CommonUtils.formatRupiah(loanOp?.ife?.toDouble()),
        "Jangka Waktu Pendanaan/Tenor" to "${dayOp?.bpio ?: 0} bulan",
        "Pendanaan yang Diterima" to CommonUtils.formatRupiah(loanOp?.dam?.toDouble()),
        "Biaya Tanda Tangan" to CommonUtils.formatRupiah(loanOp?.sam?.toDouble()),
        "Jumlah Pengembalian" to CommonUtils.formatRupiah(loanOp?.dua?.toDouble()),
        "Cara Pembayaran" to "Virtual Account",
        "Denda Keterlambatan (apabila ada)" to "0.3% per hari + 0.3% per hari (biaya denda + bunga %)"
    )
}
