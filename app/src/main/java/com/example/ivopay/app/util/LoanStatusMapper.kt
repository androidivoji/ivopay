package com.example.ivopay.app.util

import androidx.compose.ui.graphics.Color
import com.example.ivopay.app.util.SessionManager

/**
 * Utility untuk memetakan status pinjaman (asu) ke warna dan teks
 * sesuai dengan logika project Vue.
 */
object LoanStatusMapper {
    
    // Status Constants (dari AS di Vue)
    const val UNDER_REVIEW = 101
    const val REJECT = 102
    const val PAYMENT_IN_PROGRESS = 201
    const val WAIT_BORROW_SIGN = 601
    const val PAYMENT_FAILED = 202
    const val PASSED_WAIT_CONFIRM = 203
    const val USEING_MONEY = 301
    const val EXPIRED = 302
    const val REPAID = 501
    const val OVERDUE = 303

    fun getStatusColor(asu: Int, hasPgsh: Boolean = false): StatusDisplay {
        return when (asu) {
            UNDER_REVIEW -> StatusDisplay(
                text = "Approving",
                color = Color(0xFFFF7725),
                bgColor = Color(0xFFFF7725).copy(alpha = 0.06f)
            )
            REJECT -> StatusDisplay(
                text = "Reject",
                color = Color(0xFFFF4D4F),
                bgColor = Color(0xFFFF4D4F).copy(alpha = 0.06f)
            )
            PAYMENT_IN_PROGRESS -> StatusDisplay(
                text = if (hasPgsh) "Platfom sedang Pengolahan" else "Disbursing",
                color = Color(0xFFFF7725),
                bgColor = Color(0xFFFF7725).copy(alpha = 0.06f)
            )
            WAIT_BORROW_SIGN -> StatusDisplay(
                text = "Menunggu ditanda tangan",
                color = Color(0xFFFF7725),
                bgColor = Color(0xFFFF7725).copy(alpha = 0.06f)
            )
            PAYMENT_FAILED -> StatusDisplay(
                text = "Disbursement failed",
                color = Color(0xFFFF4D4F),
                bgColor = Color(0xFFFF4D4F).copy(alpha = 0.06f)
            )
            USEING_MONEY -> StatusDisplay(
                text = "In Use",
                color = Color(0xFF00B95E),
                bgColor = Color(0xFF00B95E).copy(alpha = 0.06f)
            )
            PASSED_WAIT_CONFIRM -> StatusDisplay(
                text = "Approved-Wait confirm",
                color = Color(0xFF00B95E),
                bgColor = Color(0xFF00B95E).copy(alpha = 0.06f)
            )
            EXPIRED -> StatusDisplay(
                text = "Kadaluarsa",
                color = Color(0xFFFF4D4F),
                bgColor = Color(0xFFFF4D4F).copy(alpha = 0.06f)
            )
            REPAID -> StatusDisplay(
                text = "Repaid",
                color = Color(0xFF8C8C8C),
                bgColor = Color(0xFF8C8C8C).copy(alpha = 0.1f)
            )
            OVERDUE -> StatusDisplay(
                text = "Overdue",
                color = Color(0xFFFF4D4F),
                bgColor = Color(0xFFFF4D4F).copy(alpha = 0.06f)
            )
            else -> StatusDisplay(
                text = "Status $asu",
                color = Color.Gray,
                bgColor = Color.Gray.copy(alpha = 0.06f)
            )
        }
    }
}

data class StatusDisplay(
    val text: String,
    val color: Color,
    val bgColor: Color
)
