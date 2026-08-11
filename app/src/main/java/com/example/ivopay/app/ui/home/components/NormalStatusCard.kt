package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.app.data.model.NodpData
import com.example.ivopay.app.util.CommonUtils

@Composable
fun NormalStatusCard(
    nodp: NodpData?,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Nilai Pinjaman (Rp)", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = CommonUtils.formatRupiah(nodp?.tma?.toDouble() ?: 5000000.0),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626)
            )
            Text(text = "Pelunasan total: ${CommonUtils.formatRupiah(nodp?.datm?.toDouble() ?: 5200000.0)}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${nodp?.peo ?: 91} hari", fontWeight = FontWeight.Bold)
                    Text(text = "Waktu peminjaman", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = nodp?.dud ?: "--", fontWeight = FontWeight.Bold)
                    Text(text = "Tanggal pembayaran", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
            ) {
                Text("Ajukan Pinjaman")
            }
        }
    }
}
