package com.example.ivopay.app.ui.repay

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.util.CommonUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepayScreen(
    billJson: String,
    isPrePay: Boolean,
    isCurPay: Boolean,
    viewModel: RepayViewModel,
    onBackClick: () -> Unit,
    onNavigateBcaGuide: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    LaunchedEffect(billJson) {
        viewModel.init(billJson, isPrePay, isCurPay)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pembayaran segera", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Top Banner Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(150.dp)
                    ) {
                        // Placeholder for iv_repayment_banner
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFE5455)))
                        /*Image(
                            painter = painterResource(id = R.drawable.iv_repayment_banner),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )*/
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, top = 24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            val title = if (viewModel.curPayFlag) "Jumlah bayar kini" else "Total Repayment"
                            Text(text = "$title(Rp)", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
                            Text(
                                text = CommonUtils.formatRupiah(viewModel.loamAmount.toDouble()),
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val repayBill = viewModel.repayBill
                            if (repayBill != null && repayBill.udar > 0 && !viewModel.curPayFlag) {
                                Text(
                                    text = "Pengurangan bunga sebesar RP ${CommonUtils.formatRupiah(repayBill.udar.toDouble())}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Kode pembayaran VA",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(viewModel.payList) { item ->
                    ListItem(
                        headlineContent = { Text(item.name ?: "") },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                        modifier = Modifier.clickable { viewModel.onRepayClick(item) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFEEEEEE))
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Panduan Pembayaran BCA",
                        color = Color(0xFFBD0100),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { 
                            onNavigateBcaGuide(com.google.gson.Gson().toJson(viewModel.payList)) 
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }
        }
    }

    // 1. Static Pay Code Popup
    if (viewModel.showPayCodePop) {
        Dialog(onDismissRequest = { viewModel.showPayCodePop = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment code", color = Color.Gray)
                        Text(viewModel.curPayCode, fontWeight = FontWeight.Bold)
                    }
                    
                    if (viewModel.curPayName.contains("ALFAMART")) {
                        Text(
                            text = "*Pengingat Hangat: Anda dapat memberitahu kasir nama merchant - Jokul, untuk melakukan pembayaran.",
                            color = Color(0xFF666666),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    Button(
                        onClick = { 
                            clipboardManager.setText(AnnotatedString(viewModel.curPayCode))
                            Toast.makeText(context, "Salin Sukses", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text("Copy")
                    }
                }
            }
        }
    }

    // 2. Dynamic Pay Code Popup
    if (viewModel.showPayCodePop2) {
        Dialog(onDismissRequest = { viewModel.showPayCodePop2 = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Masukkan jumlah pembayaran", fontSize = 14.sp)
                    
                    OutlinedTextField(
                        value = viewModel.loamAmount.toString(),
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            viewModel.loamAmount = filtered.toLongOrNull() ?: 0L 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            TextButton(onClick = { viewModel.getDKPayCode { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }) {
                                Text("Confirm", color = Color(0xFFFE5455))
                            }
                        }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment code", color = Color.Gray)
                        Text(viewModel.curPayCode, fontWeight = FontWeight.Bold)
                    }

                    if (viewModel.curPayCode.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .background(Color(0xFFFFFBE6), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFA8C16))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Berlaku 1 jam, minimal pelunasan Rp10.000", fontSize = 11.sp, color = Color(0xFF8C6B00))
                        }
                    }

                    Button(
                        onClick = { 
                            clipboardManager.setText(AnnotatedString(viewModel.curPayCode))
                            Toast.makeText(context, "Salin Sukses", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        enabled = viewModel.curPayCode.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text("Copy")
                    }
                    
                    if (viewModel.curPay?.name == "Alfamart") {
                        Text(
                            text = "Proses pembayaran",
                            color = Color.Gray,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 16.dp)
                                .clickable { viewModel.showRepayProgressPop = true }
                        )
                    }
                }
            }
        }
    }

    // 3. Alfamart Progress Popup
    if (viewModel.showRepayProgressPop) {
        Dialog(onDismissRequest = { viewModel.showRepayProgressPop = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Proses pembayaran", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Placeholder for iv_popup_repayment_process
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                    /*Image(
                        painter = painterResource(id = R.drawable.iv_popup_repayment_process),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )*/
                }
            }
        }
    }
}
