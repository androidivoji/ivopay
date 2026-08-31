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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Label Tag (Peach/Orange style matching Vue)
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFFDE3CF),
                        shape = RoundedCornerShape(bottomEnd = 12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Produk pinjaman tunai",
                    color = Color(0xFF8C5C32),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            ) {
                Text(text = "Nilai Pinjaman(Rp)", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = CommonUtils.formatMoneyOnly(nodp?.tma?.toDouble() ?: 10000000.0),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262626)
                )
                Text(
                    text = "Pelunasantotal: ${CommonUtils.formatRupiah(nodp?.datm?.toDouble() ?: 12730000.0)}", 
                    fontSize = 13.sp, 
                    color = Color(0xFFBFBFBF)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "${nodp?.peo ?: 91}hari", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                        Text(text = "Waktu peminjaman", fontSize = 12.sp, color = Color.Gray)
                    }
                    
                    Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = nodp?.dud ?: "29/11/2026", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                        Text(text = "Tanggal pembayaran", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE54E31)) // Match Vue Red/Orange
                ) {
                    Text("Ajukan pinjaman", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
