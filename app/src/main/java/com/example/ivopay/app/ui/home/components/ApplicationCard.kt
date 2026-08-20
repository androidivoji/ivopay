package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import com.example.ivopay.app.ui.home.BorrowerHomeViewModel
import com.example.ivopay.app.util.CommonUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationCard(
    viewModel: BorrowerHomeViewModel,
    onNavigate: (String) -> Unit
) {
    var amount by remember (viewModel.showAmount) {
        mutableFloatStateOf(viewModel.showAmount.toFloat()) }
    val cashData = viewModel.cashData

    // Tentukan range slider secara aman
    val minVal = cashData?.itma?.toFloat() ?: 0f
    val maxVal = cashData?.atma?.toFloat() ?: 5000000f

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

            val step = if (cashData?.nar != null && cashData.nar > 0) cashData.nar.toDouble() else 1.0

            Slider(
                value = amount,
                onValueChange = { 
                    // Membulatkan nilai ke kelipatan 'nar' terdekat dari server
                    amount = (Math.round(it / step) * step).toFloat()
                },
                valueRange = (minVal)..(maxVal),
                thumb = {
                    // Menggunakan icon dari drawable sebagai thumb slider
                    Image(
                        painter = painterResource(id = R.drawable.iv_choose2_sel), // Ganti dengan ID drawable Anda
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFFFE5455),
                    inactiveTrackColor = Color(0xFFE8E8E8)
                )
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
                onClick = { viewModel.checkInfo(onNavigate) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
            ) {
                Text("Permohonan")
            }
        }
    }
}
