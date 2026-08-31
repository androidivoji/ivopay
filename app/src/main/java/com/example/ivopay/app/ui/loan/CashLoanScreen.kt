package com.example.ivopay.app.ui.loan

import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.SignatureCanvas
import com.example.ivopay.app.ui.navigation.Screen
import com.example.ivopay.app.util.CommonUtils
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashLoanScreen(
    viewModel: CashLoanViewModel,
    onBack: () -> Unit,
    onNavigateToFace: (String) -> Unit,
    onSuccess: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.init()
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
        containerColor = Color(0xFFF8F8F8)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
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
                            text = CommonUtils.formatRupiah(viewModel.selAmount.toDouble()),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF262626)
                        )
                        
                        val maxIdx = viewModel.maxAmountIndex.toFloat().coerceAtLeast(0f)
                        Slider(
                            value = viewModel.amountIdx.toFloat().coerceIn(0f, maxIdx),
                            onValueChange = { 
                                val item = viewModel.getLoanOptionByIndex(it.toInt())
                                if (item?.aow == true) {
                                    viewModel.amountIdx = it.toInt() 
                                } else {
                                    Toast.makeText(context, "Ajukan dan lunasi tepat waktu lebih dari 3x untuk tingkatkan limit pinjamanmu.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            valueRange = 0f..maxIdx.coerceAtLeast(0.01f),
                            steps = (viewModel.maxAmountIndex - 1).coerceAtLeast(0),
                            enabled = viewModel.maxAmountIndex > 0,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFE5455),
                                activeTrackColor = Color(0xFFFE5455)
                            ),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = CommonUtils.formatRupiah((viewModel.cashData?.itma ?: 0).toDouble()), fontSize = 12.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = CommonUtils.formatRupiah((viewModel.cashData?.atma ?: 0).toDouble()), fontSize = 12.sp, color = Color.Gray)
                                val maxOption = viewModel.getLoanOptionByIndex(viewModel.maxAmountIndex)
                                if (viewModel.cashData != null && viewModel.cashData!!.atma < (maxOption?.tma ?: 0L)) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = "${(maxOption?.tma ?: 0) / 1000000} Juta", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "Jangka Pinjaman", modifier = Modifier.fillMaxWidth(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        
                        // Days Grid
                        val tpos = viewModel.cashData?.tpos ?: emptyList()
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            tpos.chunked(3).forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowItems.forEach { timeOption ->
                                        val idx = tpos.indexOf(timeOption)
                                        val isSelected = idx == viewModel.dayIdx
                                        val isLocked = !timeOption.aow

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .background(
                                                    if (isSelected) Color(0xFFFE5455).copy(alpha = 0.05f) else Color.White,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFFFE5455) else Color(0xFFE8E8E8),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .clickable(enabled = true) { 
                                                    if (!isLocked) viewModel.dayIdx = idx 
                                                    else {
                                                        Toast.makeText(context, "Ajukan dan lunasi tepat waktu lebih dari 3x untuk akses tenor pinjaman lebih panjang.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isLocked) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.iv_borrower_ic_record), // Placeholder lock
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp),
                                                        tint = Color.Gray
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                val dayText = if (timeOption.peoGfd != null && timeOption.peoGfd != 0) {
                                                    "${timeOption.peoGfd} hari"
                                                } else {
                                                    "${timeOption.peo} hari"
                                                }
                                                Text(
                                                    text = dayText,
                                                    fontSize = 13.sp,
                                                    color = if (isLocked) Color.Gray else if (isSelected) Color(0xFFFE5455) else Color(0xFF262626)
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.iv_choose_sel),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp).align(Alignment.BottomEnd),
                                                    tint = Color.Unspecified
                                                )
                                            }
                                        }
                                    }
                                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // 2. Tips Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(id = R.drawable.iv_hone_tips_ic_horn), contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFE5455))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Jumlah pencairan dan tenor akhir mengikuti hasil verifikasi", fontSize = 12.sp, color = Color.Gray)
                }

                // 3. Loan Data List Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        viewModel.getLoanData().forEach { (key, value) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = key, color = Color.Gray, fontSize = 14.sp)
                                    if (key == "Biaya Manajemen") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn), // Placeholder tips icon
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp).clickable { /* Show admin pop */ },
                                            tint = Color.LightGray
                                        )
                                    }
                                }
                                Text(text = value, color = Color(0xFF262626), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
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
                        onClick = { viewModel.onApplyClick { onNavigateToFace(Screen.CashLoan) } },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Selanjutnya")
                    }
                }
            }
        }
    }

    // Signature Dialog
    if (viewModel.showSignPop) {
        Dialog(onDismissRequest = { viewModel.showSignPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Tanda Tangan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { viewModel.showSignPop = false }) {
                            Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = null, modifier = Modifier.size(20.dp))
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
                            onNavigateToFace(Screen.CashLoan)
                        }
                    )
                }
            }
        }
    }
}
