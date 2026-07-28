package com.example.ivopay.app.util

import androidx.compose.ui.graphics.Color

// Ganti dari: _getStatusColor
data class StatusColorConfig(
    val color: Color,
    val backgroundColor: Color,
    val textResId: String
)

object LoanStatusMapper {

    fun getStatusConfig(statusCode: Int): StatusColorConfig {
        return when (statusCode) {
            // under_review (1)
            1 -> StatusColorConfig(
                color = Color(0xFFFF7725),
                backgroundColor = Color(0x10FF7725),
                textResId = "Sedang Ditinjau"
            )
            // reject (2)
            2 -> StatusColorConfig(
                color = Color(0xFFFF4D4F),
                backgroundColor = Color(0x10FF4D4F),
                textResId = "Ditolak"
            )
            // using_money / in use (5)
            5 -> StatusColorConfig(
                color = Color(0x00B95E),
                backgroundColor = Color(0x1000B95E),
                textResId = "Sedang Digunakan"
            )
            // overdue (8)
            8 -> StatusColorConfig(
                color = Color(0xFFFF4D4F),
                backgroundColor = Color(0x10FF4D4F),
                textResId = "Terlambat"
            )
            // Default Fallback
            else -> StatusColorConfig(
                color = Color(0xFF8C8C8C),
                backgroundColor = Color(0x108C8C8C),
                textResId = "Status Tidak Diketahui"
            )
        }
    }
}