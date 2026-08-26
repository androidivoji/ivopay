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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Label placeholder if image not found
            Text(
                text = "CASH LOAN", 
                color = Color.White, 
                modifier = Modifier
                    .background(Color(0xFFFE5455), RoundedCornerShape(bottomEnd = 12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .offset(x = (-16).dp, y = (-16).dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Nilai Pinjaman(Rp)", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = CommonUtils.formatRupiah(nodp?.tma?.toDouble() ?: 5000000.0),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262626)
                )
                Text(
                    text = "Pelunasantotal: ${CommonUtils.formatRupiah(nodp?.datm?.toDouble() ?: 5200000.0)}", 
                    fontSize = 12.sp, 
                    color = Color(0xFFBFBFBF)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "${nodp?.peo ?: 91} hari", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                        Text(text = "Waktupeminjaman", fontSize = 12.sp, color = Color.Gray)
                    }
                    
                    Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = nodp?.dud ?: "--", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF262626))
                        Text(text = "Tanggal pembayaran", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onApply,
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
