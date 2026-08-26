package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import com.example.ivopay.app.data.model.CashConfigData
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.data.model.LoanProductConfig
import com.example.ivopay.app.ui.home.BorrowerHomeViewModel
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.SessionManager

@Composable
fun CashLoanCard(
    viewModel: BorrowerHomeViewModel,
    config: LoanProductConfig?,
    cashData: CashConfigData?,
    showAmount: Long,
    isWof: Boolean,
    isWiue: Boolean,
    productType: String,
    onNavigate: (String) -> Unit,
    curBillOverride: LoanOrder? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val curBill = curBillOverride ?: config?.podi
    
    var showUnqualifiedPop2 by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (curBill != null) {
            // 1. Kartu Tagihan Aktif (Bill Card) - v-if="curBill"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { viewModel.onJumpBillDetails(onNavigate, curBill, config, productType) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color(0xFFFE5455)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Lamaran saya", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                        }
                        StatusBadge(asu = curBill.asu, hasPgsh = sessionManager.getHasPgsh())
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val showRepayBtn = curBill.asu in listOf(
                        303, 301, 302, // overdue, using_money, expired
                        801, 802, 803, // extension_* (estimasi)
                        800301, 800302, 800303
                    )

                    Text(
                        text = if (showRepayBtn) "Repayment Amount(Rp)" else "Nilai Pinjaman(Rp)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CommonUtils.formatRupiah((if (showRepayBtn) curBill.csp else curBill.tma).toDouble()),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF262626)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.iv_set_right_arrow),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.LightGray
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            val days = if (!curBill.peoGfd.isNullOrEmpty() && curBill.peoGfd != "0") {
                                curBill.peoGfd
                            } else {
                                "${curBill.peo} hari"
                            }
                            Text(text = days, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                            Text(text = "Waktupeminjaman", fontSize = 11.sp, color = Color.Gray)
                        }
                        
                        Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            val dateText = if (showRepayBtn) curBill.dud else curBill.ade
                            val label = if (showRepayBtn) "Tanggal pembayaran" else "Application Time"
                            Text(text = dateText ?: "--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                            Text(text = label, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        showRepayBtn -> {
                            Text(
                                text = "Untuk menjaga keamanan akun, harap salin kode pembayaran terbaru dari dalam tagihan untuk melakukan pembayaran",
                                fontSize = 11.sp,
                                color = Color(0xFFFE5455),
                                modifier = Modifier.padding(bottom = 12.dp),
                                lineHeight = 15.sp
                            )
                            Button(
                                onClick = { viewModel.onJumpBillDetails(onNavigate, curBill, config, productType) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Bayar Segera")
                            }
                        }
                        curBill.asu == 203 -> { // Passed wait confirm
                            Button(
                                onClick = { viewModel.onJumpBillDetails(onNavigate, curBill, config, productType) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Confirmation for withdrawal of funds")
                            }
                        }
                        curBill.asu == 601 || curBill.asu == 701 || curBill.asu == 800301 -> { // wait_borrow_sign / kfc
                             Button(
                                onClick = { viewModel.toSignContracts(onNavigate, curBill) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Proses tanda tangan")
                            }
                        }
                        else -> {
                            if (curBill.bae) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn),
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color(0xFFFE5455)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = curBill.baeTtm ?: "", fontSize = 11.sp, color = Color(0xFF8C8C8C))
                                }
                            }
                            Button(
                                onClick = { viewModel.onJumpBillDetails(onNavigate, curBill, config, productType) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Periksa detailnya")
                            }
                        }
                    }
                }
            }
        } else if (config?.psw == 1) {
            // 2. Kartu Pengajuan (Application Card) - v-else-if="comData.psw"
            if (cashData != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.iv_borrower_ic_record), // Placeholder for iv_home_cash_hand
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFFE5455)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "Produk pinjaman tunai", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(text = CommonUtils.formatRupiah(showAmount.toDouble()), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text(text = "Jumlah maksimum(Rp)", fontSize = 12.sp, color = Color.Gray)
                                }
                                
                                Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    val days = if (!cashData.peoGfd.isNullOrEmpty() && cashData.peoGfd != "0") {
                                        cashData.peoGfd
                                    } else {
                                        "${cashData.peo} hari"
                                    }
                                    Text(text = days, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text(text = "Jangka Pinjaman", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { 
                                    if (cashData.koc) showUnqualifiedPop2 = true 
                                    else viewModel.onApplyClick(onNavigate, productType) 
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Ajukan pinjaman")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUnqualifiedPop2) {
        UnqualifiedPopup(
            message = cashData?.rea,
            onDismiss = { showUnqualifiedPop2 = false }
        )
    }
}
