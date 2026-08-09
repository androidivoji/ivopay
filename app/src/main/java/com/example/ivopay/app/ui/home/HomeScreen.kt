package com.example.ivopay.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.LoanStatusMapper
import com.example.ivopay.app.util.SessionManager

@Composable
fun HomeScreen(
    viewModel: BorrowerHomeViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Title
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = "IVOCASH Borrower",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Banner
        Image(
            painter = painterResource(id = R.drawable.iv_hone_default_slider),
            contentDescription = "Banner",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Tips Alert Box
        Card(
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBE6)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn),
                    contentDescription = "Notice",
                    tint = Color(0xFFFA8C16),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pemohonan dan pembayaran diselesaikan dalam APP resmi, tautan eksternal dan transfer pribadi adalah penipuan",
                    fontSize = 11.sp,
                    color = Color(0xFF8C6B00)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Dynamic Card Logic
        val currentBill = viewModel.currentBill
        val homeConfig = viewModel.homeConfig
        val isUserInfoCompleted = homeConfig?.cme?.uico ?: false

        if (currentBill != null) {
            // Bill Card
            BillCard(
                bill = currentBill,
                hasPgsh = sessionManager.getHasPgsh(),
                onNavigate = onNavigateToDetail
            )
        } else if (isUserInfoCompleted) {
            // Application Card (Slider)
            ApplicationCard(
                viewModel = viewModel,
                onNavigate = onNavigateToDetail
            )
        } else {
            // Guest / Normal Status Card
            NormalStatusCard(
                nodp = homeConfig?.nodp,
                onApply = { onNavigateToDetail("ApplyLoan") }
            )
        }
    }
}

@Composable
fun BillCard(
    bill: LoanOrder,
    hasPgsh: Boolean,
    onNavigate: (String) -> Unit
) {
    // Logic for showRepayBtn: asu in [overdue, useing_money, expired]
    val showRepayBtn = bill.asu in listOf(303, 301, 302) 

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Lamaran saya", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StatusBadge(asu = bill.asu, hasPgsh = hasPgsh)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = if (showRepayBtn) "Repayment Amount (Rp)" else "Nilai Pinjaman (Rp)", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = CommonUtils.formatRupiah((if (showRepayBtn) bill.csp else bill.tma).toDouble()),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626)
            )

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${bill.peo} days", fontWeight = FontWeight.Bold)
                    Text(text = "Waktu peminjaman", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = bill.ade ?: "--", fontWeight = FontWeight.Bold)
                    Text(text = "Tanggal pembayaran", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showRepayBtn) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onNavigate("BillDetails") }, modifier = Modifier.weight(1f)) {
                        Text("Periksa detailnya")
                    }
                    Button(onClick = { onNavigate("RepayPage") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                        Text("Bayar Segera")
                    }
                }
            } else if (bill.asu == 601) { // wait_borrow_sign
                Button(onClick = { onNavigate("BorrowerSignContracts") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                    Text("Proses tanda tangan")
                }
            } else {
                Button(onClick = { onNavigate("BillDetails") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                    Text("Periksa detailnya")
                }
            }
        }
    }
}

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

@Composable
fun StatusBadge(asu: Int, hasPgsh: Boolean) {
    val display = LoanStatusMapper.getStatusColor(asu, hasPgsh)

    Surface(
        color = display.bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = display.text, color = display.color, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
fun NormalStatusCard(
    nodp: com.example.ivopay.app.data.model.NodpData?,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Nilai Pinjaman (Rp)", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = CommonUtils.formatRupiah(nodp?.tma?.toDouble() ?: 5000000.0),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626)
            )
            Text(text = "Pelunasan total: ${CommonUtils.formatRupiah(nodp?.datm?.toDouble() ?: 5200000.0)}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${nodp?.peo ?: 91} hari", fontWeight = FontWeight.Bold)
                    Text(text = "Waktu peminjaman", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = nodp?.dud ?: "--", fontWeight = FontWeight.Bold)
                    Text(text = "Tanggal pembayaran", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
            ) {
                Text("Ajukan Pinjaman")
            }
        }
    }
}
