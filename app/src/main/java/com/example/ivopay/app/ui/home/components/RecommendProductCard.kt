package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import com.example.ivopay.app.util.CommonUtils

@Composable
fun RecommendProductCard(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Rekomendasi produk pinjaman lain yang sesuai untuk Anda",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF0EB), Color.White),
                            startY = 0f,
                            endY = 300f
                        )
                    )
                    .padding(16.dp)
            ) {
                // Gold Image at Top Right
                Image(
                    painter = painterResource(id = R.drawable.iv_home_img_gold),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopEnd)
                )

                Column {
                    Text(text = "Disetujui dalam 30 menit", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.4f))
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    Text(text = "Limit Maks (Rp)", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.4f))
                    
                    Text(
                        text = CommonUtils.formatRupiah(10000000.0),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Apply Button at Bottom Right
                Button(
                    onClick = { onNavigate("OtherProductPage") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Ajukan", fontSize = 12.sp)
                }
            }
        }
    }
}
