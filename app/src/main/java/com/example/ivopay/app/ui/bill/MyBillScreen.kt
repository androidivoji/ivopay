package com.example.ivopay.app.ui.bill

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.LoanStatusMapper
import com.example.ivopay.app.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBillScreen(
    viewModel: MyBillViewModel,
    onItemClick: (LoanOrder) -> Unit
) {
    var showWarnDialog by remember { mutableStateOf(false) }
    var selectedNoc by remember { mutableStateOf("") }
    val billList = viewModel.billList
    val isRefreshing = viewModel.isRefreshing
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Pemicu API saat layar tampil (seperti activated di Vue)
    LaunchedEffect(Unit) {
        viewModel.getLoanList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Riwayat Pengajuan", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.getLoanList() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (billList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(billList) { item ->
                        BillCardItem(
                            item = item,
                            hasPgsh = sessionManager.getHasPgsh(),
                            onClick = { onItemClick(item) },
                            onCancelClick = { noc ->
                                selectedNoc = noc
                                showWarnDialog = true
                            }
                        )
                    }

                    // Bottom Tips Info
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn),
                                contentDescription = "Tip",
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Untuk menjaga keamanan akun, harap salin kode pembayaran terbaru dari dalam tagihan untuk melakukan pembayaran.",
                                fontSize = 12.sp,
                                color = Color(0xFF8C8C8C)
                            )
                        }
                    }
                }
            } else {
                // Empty State
                EmptyBillState()
            }
        }
    }

    // Modal Konfirmasi Pembatalan
    if (showWarnDialog) {
        CancelBillDialog(
            onDismiss = { showWarnDialog = false },
            onConfirmCancel = {
                showWarnDialog = false
                viewModel.cancelBill(selectedNoc)
            }
        )
    }
}

@Composable
fun BillCardItem(
    item: LoanOrder,
    hasPgsh: Boolean,
    onClick: () -> Unit,
    onCancelClick: (String) -> Unit
) {
    // Logika penentuan status pelunasan vs pengajuan: asu in [overdue (303), useing_money (301), expired (302)]
    val isRepayment = item.asu in listOf(303, 301, 302)
    val titleText = if (isRepayment) "Jumlah Pelunasan" else "Nilai Pinjaman"
    val displayAmount = if (isRepayment) item.csp else item.tma

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Card (Title & Status Tag)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$titleText:", fontSize = 14.sp, color = Color(0xFF595959))

                // Status Badge
                StatusBadgeSmall(asu = item.asu, hasPgsh = hasPgsh)
            }

            // Amount Text
            Text(
                text = CommonUtils.formatRupiah(displayAmount.toDouble()),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(8.dp))

            // Details (Tenor & Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tenor Pinjaman:", fontSize = 13.sp, color = Color(0xFF8C8C8C))
                Text(
                    text = "${item.peo} Hari",
                    fontSize = 13.sp,
                    color = Color(0xFF595959)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Waktu Pengajuan:", fontSize = 13.sp, color = Color(0xFF8C8C8C))
                Text(text = item.ade ?: "--", fontSize = 13.sp, color = Color(0xFF595959))
            }
        }
    }
}

@Composable
fun StatusBadgeSmall(asu: Int, hasPgsh: Boolean) {
    val display = LoanStatusMapper.getStatusColor(asu, hasPgsh)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = display.bgColor
    ) {
        Text(
            text = display.text,
            color = display.color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyBillState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.iv_apply_empty_state),
            contentDescription = "Data Kosong",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Data tidak ditemukan", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(text = "Belum mempunyai riwayat pinjaman", fontSize = 13.sp, color = Color(0xFF8C8C8C))
    }
}

@Composable
fun CancelBillDialog(
    onDismiss: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iv_dialog_ic_cancel),
                    contentDescription = "Cancel Icon",
                    modifier = Modifier.size(70.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Apakah Anda yakin ingin membatalkan?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = "Lanjutkan Pengajuan")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onConfirmCancel) {
                    Text(
                        text = "Batalkan Pinjaman",
                        color = Color.Gray,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}