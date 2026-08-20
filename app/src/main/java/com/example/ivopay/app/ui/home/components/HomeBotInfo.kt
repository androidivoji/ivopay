package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeBotInfo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(text = "Layanan Konsumen: 021-39506655", fontSize = 12.sp, color = Color.Gray)
        Text(text = "Email: cs@ivoji.id", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Copyright © 2026 IVOJI. All rights reserved.", fontSize = 10.sp, color = Color.LightGray)
    }
}
