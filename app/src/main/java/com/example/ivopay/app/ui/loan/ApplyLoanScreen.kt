package com.example.ivopay.app.ui.loan

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.SignatureCanvas
import com.example.ivopay.app.util.CommonUtils

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
                    // Trigger Native Face Detection Bridge/SDK
                    // On Result: viewModel.handleFaceDetectResult(bitmap)
                    onNavigate("FaceDetection") // Placeholder for SDK call
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
                    .padding(bottom = 100.dp)
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
            }

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sebelumnya", color = Color.Gray)
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
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Selanjutnya")
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

    // 2. Admin Fee Popup
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

private fun getLoanDataList(viewModel: ApplyLoanViewModel): List<Pair<String, String>> {
    val bio = viewModel.cashData?.bio
    val loanOp = viewModel.getCurLoanOption()
    val dayOp = viewModel.getCurDayOption()
    
    return listOf(
        "Nama" to (bio?.bkan ?: ""),
        "Bank Penerima" to (bio?.bkm ?: ""),
        "Nomor Rekening" to (bio?.baut ?: ""),
        "Jumlah Pinjaman" to CommonUtils.formatRupiah(loanOp?.tma?.toDouble()),
        "Waktu Peminjaman" to "${dayOp?.peo ?: 0} hari",
        "Biaya Admin Platform" to CommonUtils.formatRupiah(loanOp?.sam?.toDouble()),
        "Bunga" to CommonUtils.formatRupiah(loanOp?.ife?.toDouble()),
        "Jumlah Pelunasan" to CommonUtils.formatRupiah(loanOp?.dua?.toDouble())
    )
}
