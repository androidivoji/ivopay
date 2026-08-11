package com.example.ivopay.app.ui.bill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.LoanStatusMapper
import com.example.ivopay.app.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailsScreen(
    noc: String,
    viewModel: BillDetailsViewModel,
    onBackClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scrollState = rememberScrollState()
    val curBill = viewModel.curBill

    LaunchedEffect(noc) {
        viewModel.init(noc)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rincian tagihan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = "Back", modifier = Modifier.size(24.dp))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8FA))
        ) {
            if (curBill != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 120.dp)
                ) {
                    // 1. Tips Box
                    if (curBill.yep == "tnpo") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color(0xFFFFFBE6), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFA8C16))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Lunasi tagihan saat ini, angsuran kedua dan ketiga tanpa bayar", fontSize = 12.sp, color = Color(0xFF8C6B00))
                        }
                    }

                    // 2. Main Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (curBill.buklh != null) {
                                Surface(
                                    color = Color(0xFFFFF7E6),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(text = "Cicilan telah disetujui", fontSize = 11.sp, color = Color(0xFFFAAD14), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            val showRepayBtn = curBill.asu in listOf(301, 303, 302, 800301, 800302, 800303, 802, 801)
                            Text(text = if (showRepayBtn) "Jumlah Pelunasan (Rp)" else "Nilai Pinjaman (Rp)", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = CommonUtils.formatRupiah((if (showRepayBtn) curBill.csp else curBill.tma).toDouble()),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF262626)
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Status pesanan", fontWeight = FontWeight.Medium)
                                val status = LoanStatusMapper.getStatusColor(curBill.asu, sessionManager.getHasPgsh())
                                Text(text = status.text, color = status.color, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                            viewModel.billDetailList.forEach { (key, value) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
                                    Text(text = value, fontSize = 14.sp)
                                }
                            }

                            if (curBill.dbi != null) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Nomor Rekening Bank", color = Color.Gray, fontSize = 14.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = curBill.dbi.baut ?: "", fontSize = 14.sp)
                                        if (curBill.bae) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.iv_set_right_arrow),
                                                contentDescription = "Modify",
                                                modifier = Modifier.size(16.dp).padding(start = 4.dp).clickable { viewModel.showModifyBank = true }
                                            )
                                        }
                                    }
                                }
                            }

                            if (curBill.bae) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFE5455))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = curBill.baeTtm ?: "", fontSize = 11.sp, color = Color(0xFFFE5455))
                                }
                            }
                        }
                    }

                    // 3. Installment Plan
                    if (curBill.buklh != null) {
                        Text(text = "Cicilan tagihan", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tenor cicilan")
                                    Text("Cicilan ke-${curBill.buklh.buklh?.buklhBpio ?: 0}", fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Rencana pembayaran", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                
                                // Current Active Installment
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Mengaktifkan cicilan (Rp)", fontSize = 14.sp)
                                        Text(text = curBill.buklh.buklh?.buklhAde ?: "", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Text(text = CommonUtils.formatRupiah(curBill.buklh.buklh?.buklhTma?.toDouble()), fontWeight = FontWeight.Bold)
                                }
                                
                                HorizontalDivider(color = Color(0xFFF5F5F5))

                                // Other Installments
                                curBill.buklh.buklhEwb?.forEachIndexed { index, plan ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Cicilan ke-${index + 1}", fontSize = 14.sp)
                                            Text(text = plan.rdn ?: "", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text(text = CommonUtils.formatRupiah(plan.otma.toDouble()), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }

            // Bottom Buttons
            if (curBill != null) {
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.White).padding(16.dp)) {
                    if (curBill.buklh != null) {
                        Column {
                            val billJson = com.google.gson.Gson().toJson(curBill)
                            Button(onClick = { onNavigate("RepayPage?bill=$billJson&pre_pay=1") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                                Text("Pembayaran di muka")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { onNavigate("RepayPage?bill=$billJson&cur_pay=1") }, modifier = Modifier.fillMaxWidth()) {
                                Text("Tagihan aktif periode ini")
                            }
                        }
                    } else {
                        val showRepayBtn = curBill.asu in listOf(301, 303, 302, 800301, 800302, 800303, 802, 801)
                        if (showRepayBtn) {
                            val billJson = com.google.gson.Gson().toJson(curBill)
                            Button(onClick = { onNavigate("RepayPage?bill=$billJson") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                                Text("Bayar sekarang untuk naikkan limit")
                            }
                        }
                    }
                }
            }
        }
    }

    // Popups
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
