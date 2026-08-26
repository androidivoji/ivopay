package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.data.model.LoanProductConfig
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.LoanStatusMapper
import com.google.gson.Gson

@Composable
fun InlgLoanCard(
    comData: LoanProductConfig?,
    curBill: LoanOrder? = null,
    onApply: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var showUnqualifiedPop by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // 1. Kartu Pengajuan (Limit Card)
        if (comData?.psw == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Revolving / More Limit Tip
                    if (comData.resvAtma > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF7E6))
                                .clickable { onApply() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = Color(0xFFFAAD14)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Anda masih bisa mengajukan satu pinjaman lagi dengan limit",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFF262626)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(horizontal = 4.dp)) {
                                Text(text = "Jumlah:", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = CommonUtils.formatRupiah(comData.resvAtma.toDouble()),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFE5455)
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.iv_set_right_arrow),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = CommonUtils.formatRupiah(comData.atma.toDouble()),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF262626)
                                )
                                Text(text = "Jumlah maksimum(Rp)", fontSize = 12.sp, color = Color.Gray)
                            }
                            
                            Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${comData.bpio} bulan",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF262626)
                                )
                                Text(text = "Periode terpanjang", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Button(
                            onClick = {
                                if (comData.koc) showUnqualifiedPop = true else onApply()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (comData.koc) Color(0xFFD9D9D9) else Color(0xFFFE5455)
                            )
                        ) {
                            if (comData.koc) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(text = "Ajukan pinjaman", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // 2. Kartu Tagihan Aktif (Bill Card)
        if (curBill != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clickable { 
                    val billJson = Gson().toJson(curBill)
                    onNavigate("BillDetails?bill=$billJson") 
                },
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
                        StatusBadge(asu = curBill.asu, hasPgsh = false)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val showRepayBtn = curBill.asu in listOf(303, 301, 302) // overdue, using_money, expired
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
                            Text(text = curBill.bpioTxt ?: "--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                            Text(text = "Periode terpanjang", fontSize = 11.sp, color = Color.Gray)
                        }
                        
                        Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            val dateText = if (showRepayBtn) curBill.dud else curBill.ade
                            val label = if (showRepayBtn) "Tanggal pembayaran" else "Waktu Aplikasi"
                            Text(text = dateText ?: "--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                            Text(text = label, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons/Tips for Bill
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
                                onClick = { 
                                    val billJson = Gson().toJson(curBill)
                                    onNavigate("BillDetails?bill=$billJson") 
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Bayar Segera")
                            }
                        }
                        curBill.asu == 203 -> { // Passed wait confirm
                            Button(
                                onClick = { 
                                    val billJson = Gson().toJson(curBill)
                                    onNavigate("BillDetails?bill=$billJson") 
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Konfirmasi untuk penarikan dana")
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
                                onClick = { 
                                    val billJson = Gson().toJson(curBill)
                                    onNavigate("BillDetails?bill=$billJson") 
                                },
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
        }
    }

    if (showUnqualifiedPop) {
        UnqualifiedPopup(
            message = comData?.rea,
            onDismiss = { showUnqualifiedPop = false }
        )
    }
}
