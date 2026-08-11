package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.app.util.LoanStatusMapper

@Composable
fun StatusBadge(asu: Int, hasPgsh: Boolean) {
    val display = LoanStatusMapper.getStatusColor(asu, hasPgsh)

    Surface(
        color = display.bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = display.text, 
            color = display.color, 
            fontSize = 12.sp, 
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
