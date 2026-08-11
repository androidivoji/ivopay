package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.data.model.LoanProductConfig
import com.example.ivopay.app.util.CommonUtils

@Composable
fun InstallmentBillCard(
    bill: LoanOrder,
    config: LoanProductConfig?,
    hasPgsh: Boolean,
    isWof: Boolean,
    isWiue: Boolean,
    onNavigate: (String) -> Unit,
    isExtra: Boolean = false
) {
    val showRepayBtn = bill.asu in listOf(303, 301, 302, 304, 305, 306, 307, 308)
    val btnColor = Color(0xFFFE5455)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).clickable { onNavigate("BillDetails?bill=${bill.noc}") }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color(0xFFFE5455)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Lamaran saya", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                StatusBadge(asu = bill.asu, hasPgsh = hasPgsh)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = if (showRepayBtn) "Repayment Amount (Rp)" else "Nilai Pinjaman (Rp)", color = Color.Gray, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CommonUtils.formatRupiah((if (showRepayBtn) bill.csp else bill.tma).toDouble()),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262626)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val durationText = if (isExtra) "${bill.peo} hari" else (bill.bpioTxt ?: "--")
                    Text(text = durationText, fontWeight = FontWeight.Bold)
                    val labelText = if (isExtra) "Waktu peminjaman" else "Periode terpanjang"
                    Text(text = labelText, fontSize = 12.sp, color = Color.Gray)
                }
                
                Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val dateText = if (showRepayBtn) bill.dud else bill.ade
                    val label = if (showRepayBtn) "Tanggal pembayaran" else "Application Time"
                    Text(text = dateText ?: "--", fontWeight = FontWeight.Bold)
                    Text(text = label, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                showRepayBtn -> {
                    Text(
                        text = "Untuk menjaga keamanan akun, harap salin kode pembayaran terbaru dari dalam tagihan untuk melakukan pembayaran",
                        fontSize = 11.sp,
                        color = Color(0xFFFE5455),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val billJson = com.google.gson.Gson().toJson(bill)
                    Button(onClick = { onNavigate("RepayPage?bill=$billJson") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Bayar Segera")
                    }
                }
                bill.asu == 203 -> { // passed_wait_confirm
                    Button(onClick = { onNavigate("BillDetailsWaitConfirm") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Konfirmasi untuk penarikan dana")
                    }
                }
                bill.asu == 601 -> { // wait_borrow_sign
                    Button(
                        onClick = { 
                            onNavigate("BorrowerSignContracts?noc=${bill.noc}&wiue=$isWiue") 
                        }, 
                        modifier = Modifier.fillMaxWidth(), 
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text("Proses tanda tangan")
                    }
                }
                else -> {
                    if (bill.bae) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFE5455))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = bill.baeTtm ?: "", fontSize = 11.sp)
                        }
                    }
                    Button(onClick = { onNavigate("BillDetails?bill=${bill.noc}") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Periksa detailnya")
                    }
                }
            }
        }
    }
}
