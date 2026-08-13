package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.app.data.model.LoanProductConfig
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.SessionManager

@Composable
fun ProductCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    config: LoanProductConfig?,
    isWof: Boolean,
    isWiue: Boolean,
    onNavigate: (String) -> Unit,
    onApply: (() -> Unit)? = null,
    subtitle: String? = null,
    durationUnit: String = "bulan",
    buttonText: String = "Ajukan pinjaman",
    targetRoute: String = "ApplyLoan",
    overrideDuration: Int? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val hasPgsh = sessionManager.getHasPgsh()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // 1. Header (Normal)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color(0xFFFE5455))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        if (subtitle != null) {
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Card Content
        val bill = config?.podi
        if (bill != null) {
            InstallmentBillCard(
                bill = bill, 
                config = config,
                hasPgsh = hasPgsh, 
                isWof = isWof, 
                isWiue = isWiue, 
                onNavigate = onNavigate,
                isExtra = durationUnit == "hari"
            )
        } else if (config?.psw == 1) {
            // Application Card for Installment/Extra
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Special Header if resvAtma > 0
                    if (config.resvAtma > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF7E6))
                                .padding(8.dp)
                                .clickable { onNavigate(targetRoute) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color(0xFFFAAD14))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Anda masih bisa mengajukan satu pinjaman lagi dengan limit", fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Jumlah:", fontSize = 10.sp, color = Color.Gray)
                                Text(text = CommonUtils.formatRupiah(config.resvAtma.toDouble()), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        if (config.kocByNoResvAtma) {
                            Text(text = "Limit anda sudah habis", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Segera lakukan pembayaran untuk meningkatkan limit dan dapat mengajukan pinjaman lagi.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    val amount = if (config.resvAtma > 0) config.resvAtma else config.atma
                                    Text(text = CommonUtils.formatRupiah(amount.toDouble()), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text(text = "Jumlah maksimum(Rp)", fontSize = 12.sp, color = Color.Gray)
                                }
                                
                                Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    val durationValue = overrideDuration ?: if (durationUnit == "bulan") config.bpio else config.peo
                                    Text(text = "$durationValue $durationUnit", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    val label = if (durationUnit == "bulan") "Periode terpanjang" else "Jangka Pinjaman"
                                    Text(text = label, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { 
                                if (onApply != null) onApply() else onNavigate(targetRoute) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                            enabled = config.koc == false
                        ) {
                            if (config.koc) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(buttonText)
                        }
                    }
                }
            }
        }
    }
}
