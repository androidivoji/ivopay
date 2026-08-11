package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.app.ui.home.BorrowerHomeViewModel
import com.example.ivopay.app.util.CommonUtils

@Composable
fun ApplicationCard(
    viewModel: BorrowerHomeViewModel,
    onNavigate: (String) -> Unit
) {
    var amount by remember { mutableFloatStateOf(viewModel.showAmount.toFloat()) }
    val cashData = viewModel.cashData

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nilai Pinjaman (Rp)", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = CommonUtils.formatRupiah(amount.toDouble()),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626)
            )

            Slider(
                value = amount,
                onValueChange = { amount = it },
                valueRange = (cashData?.itma?.toFloat() ?: 0f)..(cashData?.atma?.toFloat() ?: 5000000f),
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFE5455), activeTrackColor = Color(0xFFFE5455))
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = CommonUtils.formatRupiah(cashData?.itma?.toDouble() ?: 0.0), fontSize = 12.sp, color = Color.Gray)
                Text(text = CommonUtils.formatRupiah(cashData?.atma?.toDouble() ?: 5000000.0), fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Jangka Pinjaman")
                Text("${cashData?.peo ?: 0} hari", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.onApplyClick(onNavigate) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
            ) {
                Text("Permohonan")
            }
        }
    }
}
