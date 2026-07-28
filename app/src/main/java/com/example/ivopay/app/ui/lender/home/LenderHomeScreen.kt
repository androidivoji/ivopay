package com.example.ivopay.app.ui.lender.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import java.text.NumberFormat
import java.util.Locale

// Custom Helper Format Rupiah
fun Double.toRp(): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0
    return format.format(this)
}

val HeaderRed = Color(0xFFFE5455)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderHomeScreen(
    viewModel: LenderHomeViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPortfolio: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedInsuranceIdx by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header App Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iv_invest_logo),
                    contentDescription = "Invest Logo",
                    modifier = Modifier.width(90.dp)
                )
            }

            // Kondisi 1: Wait Review State (lenderStatus != 1 || !uico)
            if (uiState.lenderStatus != 1 || !uiState.uico) {
                LenderReviewView(
                    lenderStatus = uiState.lenderStatus,
                    uico = uiState.uico,
                    onEditDataClick = onNavigateToProfile
                )
            } else {
                // Kondisi 2: Borrower List Available
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Top Banner
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = HeaderRed),
                                modifier = Modifier.fillMaxWidth().height(110.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("IVOCASH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Pendanaan jangka pendek untuk kebutuhan pinjaman kecil dan praktis dengan komisi yang menarik, tumbuh dan telah terverifikasi.",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Pilih peminjam", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF262626))
                        }

                        if (uiState.borrowList.isNotEmpty()) {
                            items(uiState.borrowList) { item ->
                                BorrowerCardItem(
                                    item = item,
                                    onCardClick = { onNavigateToDetail(item.ati) },
                                    onSelectToggle = { viewModel.toggleSelectBorrower(item.ati) }
                                )
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.iv_home_img_noborrower),
                                        contentDescription = "Empty",
                                        modifier = Modifier.width(200.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Sementara belum ada pilihan peminjam, harap tunggu pemohonan peminjam",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Floating Bottom Submit Button
                    if (uiState.borrowList.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.onConfirmPayBack() },
                            colors = ButtonDefaults.buttonColors(containerColor = HeaderRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(48.dp)
                        ) {
                            Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 1. BottomSheet Financing Summary (<van-popup>)
        // ==========================================
        if (uiState.showSelectLoanDesc) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeSelectLoanSheet() },
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Financing Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Loan Amount (${uiState.financeDetail.toa})", fontSize = 14.sp)
                        Text(uiState.financeDetail.atma.toRp(), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Income", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Income Assessment", fontSize = 14.sp)
                        Text(uiState.financeDetail.trv.toRp(), color = HeaderRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Repayment Time", fontSize = 14.sp)
                        Text(uiState.financeDetail.iet, fontSize = 14.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Select Insurance", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // Insurance Radio Group
                    Column {
                        uiState.insuranceList.forEachIndexed { idx, item ->
                            val isSelected = selectedInsuranceIdx == idx
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = isSelected, onClick = { selectedInsuranceIdx = idx })
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (isSelected) R.drawable.iv_choose2_sel else R.drawable.iv_choose2_nor
                                    ),
                                    contentDescription = "Radio",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val rateText = if (item.ire > 0) " (${item.ire}%)" else ""
                                Text(text = "${item.ian}$rateText", fontSize = 14.sp)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    val insuranceCost = uiState.insuranceList.getOrNull(selectedInsuranceIdx)?.ima ?: 0.0
                    val totalPay = uiState.financeDetail.iat + insuranceCost

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total payment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(totalPay.toRp(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HeaderRed)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onCreateOrder(selectedInsuranceIdx) },
                        colors = ButtonDefaults.buttonColors(containerColor = HeaderRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==========================================
        // 2. Dialog Virtual Account (<van-popup>)
        // ==========================================
        if (uiState.showConfirmPayPop) {
            Dialog(onDismissRequest = { viewModel.closeConfirmPayPop() }) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.iv_popup_img_operate),
                            contentDescription = "Header Image",
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Complete your Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Please transfer funds immediately to the account provided by us. Funds not paid within 59 minutes will be removed from your list",
                            fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        RowDetail("Account Number", uiState.financeBill.bnm)
                        RowDetail("Bank Name", uiState.financeBill.bkn)

                        // VA Copy Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("No.VA", fontSize = 13.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(uiState.financeBill.pcd, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.iv_ic_copy_black),
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(18.dp).clickable { viewModel.copyPayCode(uiState.financeBill.pcd) }
                                )
                            }
                        }

                        RowDetail("Jumlah", "${uiState.financeBill.toa}")
                        if (uiState.financeBill.ima > 0) {
                            RowDetail("Biaya Asuransi", uiState.financeBill.ima.toRp())
                        }
                        RowDetail("Total", uiState.financeBill.tpa.toRp(), isBold = true)

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.onConfirmPayClick() },
                            colors = ButtonDefaults.buttonColors(containerColor = HeaderRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Confirm", color = Color.White)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. Bottom Banner Signature Notification (<van-notify>)
        // ==========================================
        if (uiState.showSuccessNotify) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF010002)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        viewModel.hideSuccessNotify()
                        onNavigateToPortfolio()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iv_tips_img_sign),
                        contentDescription = "Sign Icon",
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tandatangani Perjanjian (${uiState.financeBill.toa})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Terdapat perjanjian pendanaan yang perlu ditandatangani.", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// Sub-komponen Tampilan Tunggu Review/Ditolak
@Composable
fun LenderReviewView(
    lenderStatus: Int,
    uico: Boolean,
    onEditDataClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.iv_home_img_review),
            contentDescription = "Review",
            modifier = Modifier.width(260.dp).padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Mohon Menunggu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        val desc = when {
            !uico -> "Pendaftaran lender silahkan mengisi data"
            lenderStatus == 0 -> "Pengajuan Anda Sebagai Pendana Sedang Di Proses. Pengajuan registrasi Anda sebagai pemberi dana sedang kami proses. Mohon tunggu pemberitahuan melalui notifikasi setelah prosesnya selesai paling lambat 1x24 jam."
            lenderStatus == 2 -> "Peninjauan data ditolak. Kirim ulang setelah mengubah data"
            else -> ""
        }

        Text(desc, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onEditDataClick,
            colors = ButtonDefaults.buttonColors(containerColor = HeaderRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ubah Data", color = Color.White)
        }
    }
}

// Sub-komponen Card Borrower
@Composable
fun BorrowerCardItem(
    item: BorrowerItem,
    onCardClick: () -> Unit,
    onSelectToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCardClick() }
    ) {
        Column {
            // Card Top Background Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderRed)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectToggle() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.oen, color = Color.White, fontWeight = FontWeight.Bold)
                        Image(
                            painter = painterResource(
                                id = if (item.isSelect) R.drawable.iv_choose_sel else R.drawable.iv_choose_nor
                            ),
                            contentDescription = "Select Icon",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loan Amount: ${item.tma.toRp()}", color = Color.White, fontSize = 13.sp)
                    Text("Estimasi pendapatan: ${item.ife.toRp()}", color = Color.White, fontSize = 13.sp)
                    Text("Nilai pinjaman: ${item.npeo}", color = Color.White, fontSize = 13.sp)
                }

                Image(
                    painter = painterResource(id = R.drawable.iv_home_img_gold),
                    contentDescription = "Gold Badge",
                    modifier = Modifier.width(110.dp).align(Alignment.BottomEnd)
                )
            }

            // Card Bottom Details
            Column(modifier = Modifier.padding(16.dp)) {
                Text("City: ${item.bcy}", fontSize = 13.sp, color = Color(0xFF262626))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Loan Purpose: ${item.bpo}", fontSize = 13.sp, color = Color(0xFF262626))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Approved Time: ${item.aut}", fontSize = 13.sp, color = Color(0xFF262626))
            }
        }
    }
}

// Sub-komponen Reusable Row Info
@Composable
fun RowDetail(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) HeaderRed else Color.Black
        )
    }
}