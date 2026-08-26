package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
fun RevolvingLoanCard(
    comData: LoanProductConfig?,
    curBill: LoanOrder? = null,
    onApply: () -> Unit,
    onNavigate: (String) -> Unit
) {
    // revolving-loan-card di Vue biasanya menampilkan limit dinamis
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFE5455), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Produk pinjaman tunai", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = CommonUtils.formatRupiah((comData?.atma ?: 0).toDouble()),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Jumlah maksimum(Rp)", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Ajukan pinjaman")
            }
        }
    }
}
