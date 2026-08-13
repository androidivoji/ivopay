package com.example.ivopay.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.app.data.model.CashConfigData
import com.example.ivopay.app.data.model.LoanProductConfig
import com.example.ivopay.app.ui.home.BorrowerHomeViewModel
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.SessionManager

@Composable
fun CashLoanCard(
    viewModel: BorrowerHomeViewModel,
    config: LoanProductConfig?,
    cashData: CashConfigData?,
    showAmount: Long,
    isWof: Boolean,
    isWiue: Boolean,
    productType: String,
    onNavigate: (String) -> Unit
) {
    val bill = config?.podi
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val hasPgsh = sessionManager.getHasPgsh()

    if (bill != null) {
        BillCard(
            bill = bill, 
            config = config,
            hasPgsh = hasPgsh, 
            isWof = isWof, 
            isWiue = isWiue, 
            productType = productType,
            onNavigate = onNavigate
        )
    } else if (config?.psw == 1) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Header: "Produk pinjaman tunai" with Hand Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star, // Use Star for Hand replacement
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFFE5455)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "Produk pinjaman tunai", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Tip Box inside card as per Vue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = CommonUtils.formatRupiah(showAmount.toDouble()), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = "Jumlah maksimum(Rp)", fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFEEEEEE))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            val term = cashData?.peoGfd ?: "${cashData?.peo ?: 0} hari"
                            Text(text = term, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = "Jangka Pinjaman", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.checkInfo(onNavigate) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        enabled = config.koc == false
                    ) {
                        Text("Ajukan pinjaman")
                    }
                }
            }
        }
    }
}
