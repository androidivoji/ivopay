package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.data.model.LoanProductConfig
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.LoanStatusMapper
import com.example.ivopay.app.util.SessionManager
import com.google.gson.Gson

@Composable
fun Ci10LoanCard(
    comData: LoanProductConfig?,
    curBill: LoanOrder? = null,
    onApply: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val rasn = remember { sessionManager.getRasn().toString() }
    
    var showUnqualifiedPop2 by remember { mutableStateOf(false) }
    var showCi10ApplySuccessPop by remember { mutableStateOf(false) }

    // Logic from Vue script: Show success popup if status is under review face to face (estimated 205)
    LaunchedEffect(curBill) {
        if (curBill != null && curBill.asu == 205) { 
            showCi10ApplySuccessPop = true
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (curBill != null) {
            // --- active bill card ---
            Card(
                modifier = Modifier.fillMaxWidth().clickable { 
                    onNavigate("InlgBillDetails?bill=${Gson().toJson(curBill)}")
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
                        
                        val status = LoanStatusMapper.getStatusColor(curBill.asu)
                        Surface(
                            color = status.bgColor,
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text(
                                text = status.text,
                                color = status.color,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val showRepayBtn = curBill.asu in listOf(301, 303, 302) // using_money, overdue, expired
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
                            text = CommonUtils.formatMoneyOnly((if (showRepayBtn) curBill.csp else curBill.tma).toDouble()),
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

                    // middle words box
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
                            Text(text = "${curBill.ewb?.size ?: 0} bulan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                            Text(text = "Periode terpanjang", fontSize = 11.sp, color = Color.Gray)
                        }
                        
                        Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            val dateText = if (showRepayBtn) curBill.dud else curBill.ade
                            val label = if (showRepayBtn) "Tanggal pembayaran" else "Application Time"
                            Text(text = dateText ?: "--", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                            Text(text = label, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    if (showRepayBtn) {
                        Text(
                            text = "Untuk menjaga keamanan akun, harap salin kode pembayaran terbaru dari dalam tagihan untuk melakukan pembayaran",
                            fontSize = 11.sp,
                            color = Color(0xFFFE5455),
                            modifier = Modifier.padding(bottom = 12.dp),
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = { onNavigate("InlgBillDetails?bill=${Gson().toJson(curBill)}") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                        ) {
                            Text("Bayar Segera")
                        }
                    } else if (curBill.asu == 203) { // passed_wait_confirm
                        Button(
                            onClick = { onNavigate("InlgBillDetails?bill=${Gson().toJson(curBill)}") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                        ) {
                            Text("Confirmation for withdrawal of funds")
                        }
                    } else {
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
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(text = curBill.baeTtm ?: "", fontSize = 11.sp, color = Color(0xFF8C8C8C))
                            }
                        }
                        Button(
                            onClick = { onNavigate("InlgBillDetails?bill=${Gson().toJson(curBill)}") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                        ) {
                            Text("Periksa detailnya")
                        }
                    }
                }
            }
        } else if (comData?.psw == 1) { //asli
//        } else if (comData != null) {
            // --- apply card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Revolving / More Limit Tip
                    val resv = comData.resvAtma ?: 0L
                    if (resv > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF7E6))
                                .clickable { 
                                    handleApplyAction(comData, onApply, onNavigate, rasn) { showUnqualifiedPop2 = true }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.iv_home_img_gold),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Anda masih bisa mengajukan satu pinjaman lagi dengan limit",
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(horizontal = 4.dp)) {
                                Text(text = "Jumlah:", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    text = CommonUtils.formatRupiah(resv.toDouble()),
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
                                .background(Color.White)
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = CommonUtils.formatMoneyOnly(comData.atma.toDouble()),
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
                                handleApplyAction(comData, onApply, onNavigate, rasn) { showUnqualifiedPop2 = true }
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
    }

    // --- Popups ---
    
    if (showUnqualifiedPop2) {
        Dialog(onDismissRequest = { showUnqualifiedPop2 = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = comData?.rea ?: "", color = Color(0xFF262626), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showUnqualifiedPop2 = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }

    if (showCi10ApplySuccessPop) {
        Dialog(onDismissRequest = { showCi10ApplySuccessPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.iv_apply_img),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = "Selamat", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "Pengajuan pinjaman Anda sebesar ${(curBill?.tma ?: 0) / 1000000} juta telah disetujui. Kami akan menghubungi Anda dalam 3 hari kerja, dan petugas verifikasi akan datang untuk memeriksa data Anda. Klik untuk melihat dokumen yang perlu diverifikasi. Setelah data dinyatakan benar, dana akan segera dicairkan.",
                        fontSize = 14.sp,
                        color = Color(0xFF262626),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            showCi10ApplySuccessPop = false
                            onNavigate("ExampleImagePage")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Lihat sekarang")
                    }
                }
            }
        }
    }
}

private fun handleApplyAction(
    comData: LoanProductConfig,
    onApply: () -> Unit,
    onNavigate: (String) -> Unit,
    rasn: String,
    onKoc: () -> Unit
) {
    if (comData.koc) {
        onKoc()
    } else {
        val qocm = comData.qocm
        when {
            qocm?.nqs == true -> onNavigate("QuestionnairePage?rasn=$rasn")
            qocm?.nbj == true -> onNavigate("JMOPage?rasn=$rasn")
            else -> onApply() //asli
        }
    }
}
