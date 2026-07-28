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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R

// Data model dummy untuk representasi item tagihan
data class BillItem(
    val noc: String,
    val tma: Long,            // Loan Amount
    val csp: Long,            // Repayment Amount
    val asu: Int,             // Application Status
    val peo: String?,         // Loan Term Days
    val bpioTxt: String?,     // Installment Term Text
    val ade: String,          // Application Time
    val isCi10FaceToFace: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBillScreen(
    billList: List<BillItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (BillItem) -> Unit,
    onCancelBill: (String) -> Unit
) {
    var showWarnDialog by remember { mutableStateOf(false) }
    var selectedNoc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Riwayat Pengajuan", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
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
                onCancelBill(selectedNoc)
            }
        )
    }
}

@Composable
fun BillCardItem(
    item: BillItem,
    onClick: () -> Unit,
    onCancelClick: (String) -> Unit
) {
    // Logika penentuan status pelunasan vs pengajuan
    val isRepayment = item.asu == 1 || item.asu == 2 // Sesuaikan dengan ConstData.AS
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE6F7FF)
                ) {
                    Text(
                        text = "Dalam Proses",
                        color = Color(0xFF1890FF),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Amount Text
            Text(
                text = "Rp ${String.format("%,d", displayAmount)}",
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
                    text = item.bpioTxt ?: "${item.peo ?: "0"} Hari",
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
                Text(text = item.ade, fontSize = 13.sp, color = Color(0xFF595959))
            }

            // Opsi Tambahan untuk Verifikasi Kunjungan / Offline
            if (item.isCi10FaceToFace) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pengajuan Anda telah disetujui. Petugas verifikasi akan menghubungi Anda.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Saya tidak setuju verifikasi kunjungan, batalkan pinjaman",
                    fontSize = 12.sp,
                    color = Color(0xFFFF4D4F),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onCancelClick(item.noc) }
                )
            }
        }
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