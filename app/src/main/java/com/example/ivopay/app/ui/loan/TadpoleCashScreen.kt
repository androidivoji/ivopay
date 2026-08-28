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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
fun TadpoleCashScreen(
    viewModel: TadpoleCashViewModel,
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
                        
                        val maxIdx = viewModel.maxAmountIndex.toFloat().coerceAtLeast(0.01f)
                        Slider(
                            value = viewModel.amountIdx.toFloat().coerceIn(0f, maxIdx),
                            onValueChange = { 
                                viewModel.amountIdx = kotlin.math.round(it).toInt()
                                viewModel.fetchTadpoleBillPre()
                            },
                            onValueChangeFinished = {
                                val options = viewModel.curDayOption?.dop ?: emptyList()
                                if (viewModel.amountIdx < options.size && !options[viewModel.amountIdx].aow) {
                                    viewModel.amountIdx = viewModel.maxAllowedAmountIndex
                                    viewModel.fetchTadpoleBillPre()
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
                            Text(text = CommonUtils.formatRupiah(viewModel.minAmount.toDouble()), fontSize = 12.sp, color = Color.Gray)
                            Text(text = CommonUtils.formatRupiah(viewModel.maxAmount.toDouble()), fontSize = 12.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "Jangka Pinjaman", modifier = Modifier.fillMaxWidth(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        
                        // Days List Selection (Vertical items with check icon)
                        val tpos = viewModel.cashData?.tpos ?: emptyList()
                        tpos.forEachIndexed { idx, item ->
                            val isSelected = idx == viewModel.dayIdx
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(
                                        if (isSelected) Color(0xFFFE5455).copy(alpha = 0.05f) else Color.White,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFFE5455) else Color(0xFFE8E8E8),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { 
                                        if (item.aow) {
                                            viewModel.dayIdx = idx
                                            viewModel.fetchTadpoleBillPre()
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dayText = if (!item.swo.isNullOrEmpty()) item.swo else "${item.peo}"
                                    Text(
                                        text = "$dayText hari",
                                        color = if (isSelected) Color(0xFFFE5455) else Color(0xFF262626),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Icon(
                                        painter = painterResource(id = if (isSelected) R.drawable.iv_choose_sel else R.drawable.iv_choose_nor),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                    }
                }

                // Phone Code Section (Conditional)
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                        Text(text = "Peraturan pembayaran hutang", color = Color.Gray, fontSize = 14.sp)
                                        Text(text = "${item.dtap}% (pokok dan bunga)", fontSize = 13.sp, color = Color(0xFF262626))
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
                            onNavigateToFace(Screen.TadpoleCash)
                        }
                    )
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
