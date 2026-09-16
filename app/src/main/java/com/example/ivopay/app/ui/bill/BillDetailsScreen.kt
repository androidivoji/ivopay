package com.example.ivopay.app.ui.bill

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.LoanStatusMapper
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson

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
                        .padding(bottom = 140.dp)
                ) {
                    // 1. Tadp Tips (Vue: showTadpTips)
                    if (curBill.yep == "tnpo" && viewModel.isShowRepayBtn(curBill.asu)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color(0xFFFFFBE6), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painterResource(id = R.drawable.iv_hone_tips_ic_horn), contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFA8C16))
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
                                    color = Color(0xFFFDE3CF),
                                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                                    modifier = Modifier.padding(bottom = 12.dp).offset(x = (-16).dp, y = (-16).dp)
                                ) {
                                    Text(text = "Cicilan telah disetujui", fontSize = 11.sp, color = Color(0xFF8C5C32), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }

                            val showRepayBtn = viewModel.isShowRepayBtn(curBill.asu)
                            Text(
                                text = if (showRepayBtn) "Repayment Amount (Rp)" else "Nilai Pinjaman (Rp)", 
                                color = Color.Gray, 
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = CommonUtils.formatMoneyOnly((if (showRepayBtn) curBill.csp else curBill.tma).toDouble()),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF262626),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
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
                                                painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn), 
                                                contentDescription = null, 
                                                modifier = Modifier.size(18.dp).padding(start = 4.dp).clickable { viewModel.showSignFeePop = true },
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                    Text(text = value, fontSize = 14.sp)
                                }
                            }

                            if (curBill.dbi != null && !curBill.dbi.baut.isNullOrEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Nomor Rekening Bank", color = Color.Gray, fontSize = 14.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = curBill.dbi.baut, fontSize = 14.sp)
                                        if (curBill.bae) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.iv_set_right_arrow),
                                                contentDescription = "Modify",
                                                modifier = Modifier.size(20.dp).padding(start = 4.dp).clickable { viewModel.showModifyBank = true }
                                            )
                                        }
                                    }
                                }
                            }

                            if (curBill.bae) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).background(Color(0xFFFFFBE6), RoundedCornerShape(4.dp)).padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(painterResource(id = R.drawable.iv_hone_tips_ic_horn), contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFA8C16))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = curBill.baeTtm ?: "", fontSize = 11.sp, color = Color(0xFF8C6B00))
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
                                    Text("Tenor cicilan", color = Color.Gray)
                                    Text("Cicilan ke-${curBill.buklh.buklh?.buklhBpio ?: 0}", fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Rencana pembayaran", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                
                                // Current Active Installment
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Mengaktifkan cicilan (Rp)", fontSize = 14.sp)
                                            Icon(painterResource(id = R.drawable.iv_hone_tips_ic_horn), contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp).clickable { 
                                                Toast.makeText(context, "Cicilan akan aktif setelah pembayaran lunas atas tagihan ini.", Toast.LENGTH_SHORT).show()
                                            })
                                            if (curBill.buklh.buklh?.buklhBrps == 2) {
                                                Text("Lunas", color = Color(0xFF00B95E), fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                                            }
                                        }
                                        Text(text = curBill.buklh.buklh?.buklhAde ?: "", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Text(text = CommonUtils.formatMoneyOnly(curBill.buklh.buklh?.buklhTma?.toDouble()), fontWeight = FontWeight.Bold)
                                }
                                
                                HorizontalDivider(color = Color(0xFFF5F5F5))

                                // Other Installments
                                curBill.buklh.buklhEwb?.forEachIndexed { index, plan ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Cicilan ke-${index + 1}", fontSize = 14.sp)
                                                if (plan.brps == 2) {
                                                    Text("Lunas", color = Color(0xFF00B95E), fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                                                }
                                            }
                                            Text(text = plan.rdn ?: "", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text(text = CommonUtils.formatMoneyOnly(plan.otma.toDouble()), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 4. Extension Pending (Status 800 / Applied)
                    if (viewModel.isShowPayCountDown(curBill.asu) && curBill.asu801 != null) {
                        Button(
                            onClick = { onNavigate("ExtensionRepayPage?bill=${Gson().toJson(curBill)}") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Biaya penundaan ${CommonUtils.formatRupiah(curBill.asu801.dfereTma.toDouble())}", fontSize = 14.sp)
                                Text(text = "Waktu pembayaran tersisa ${curBill.asu801.dfereLftSec / 60} menit", fontSize = 11.sp)
                            }
                        }
                    }

                    // 5. Installment Pending (Status 901)
                    if (curBill.asu901 != null) {
                        Button(
                            onClick = { onNavigate("BillInstallmentRepayPage?bill=${Gson().toJson(curBill)}") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFE5455)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cicilan tagihan", color = Color(0xFFFE5455))
                        }
                    }
                }
            } else if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }

            // Bottom Buttons
            if (curBill != null) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (curBill.buklh != null) {
                            val billJson = Gson().toJson(curBill)
                            Button(onClick = { onNavigate("RepayPage?bill=$billJson&pre_pay=1") }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                                Text("Pembayaran di muka")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { onNavigate("RepayPage?bill=$billJson&cur_pay=1") }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                                Text("Tagihan aktif periode ini", color = Color(0xFF262626))
                            }
                        } else {
                            if (viewModel.isShowRepayBtn(curBill.asu)) {
                                Button(onClick = { onNavigate("RepayPage?bill=${Gson().toJson(curBill)}") }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                                    Text("Bayar sekarang untuk naikkan limit")
                                }
                            }
                            if (viewModel.isShowExtensionApplyBtn(curBill.asu)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.showExtensionPop = true }, 
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE54E31))
                                ) {
                                    Text("Ajukan penundaan pembayaran")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Sign Fee Info Popup
    if (viewModel.showSignFeePop) {
        Dialog(onDismissRequest = { viewModel.showSignFeePop = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Termasuk tanda tangan digital, pembayaran dan biaya lainnya", color = Color(0xFF262626), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.showSignFeePop = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                        Text("Jadi begitu")
                    }
                }
            }
        }
    }

    // 2. Apply Extension Popup (Vue: ApplyExtensionPop)
    if (viewModel.showExtensionPop) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showExtensionPop = false },
            containerColor = Color(0xFFF8F8F8)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 32.dp)) {
                Text(text = "Pilih waktu penundaan pembayaran", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Instructions Box
                Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.02f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Text(text = "[Penundaan Pembayaran] Ket :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "1. Penundaan aktif setelah biaya dibayarkan.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    Text(text = "2. Waktu jatuh tempo diperpanjang berdasarkan tanggal pembayaran biaya penundaan.", fontSize = 13.sp, color = Color.Gray)
                    Text(text = "3. Penjelasan biaya penundaan: Biaya akan bertambah sesuai jumlah hari keterlambatan, harap lakukan pembayaran tepat waktu.", fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options List
                curBill?.asu800?.forEach { option ->
                    val isSelected = viewModel.selectedExtension == option
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { viewModel.selectedExtension = option },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = if (isSelected) R.drawable.iv_choose_sel else R.drawable.iv_choose_nor),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Jumlah hari penundaan")
                                    Text("${option.dferePeo} hari", fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Biaya penundaan")
                                    Text(CommonUtils.formatRupiah(option.dfereTma.toDouble()))
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tanggal jatuh tempo baru", modifier = Modifier.weight(1f))
                                    Text(option.dfereDud ?: "")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        viewModel.applyExtension(
                            onSuccess = { onNavigate("ExtensionRepayPage?bill=${Gson().toJson(curBill)}") },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                    enabled = viewModel.selectedExtension != null
                ) {
                    Text("Lakukan pembayaran biaya penundaan")
                }
            }
        }
    }
}
