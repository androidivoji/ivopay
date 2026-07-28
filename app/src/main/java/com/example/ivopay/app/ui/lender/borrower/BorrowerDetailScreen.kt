package com.example.ivopay.app.ui.lender.borrower

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivopay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerDetailScreen(
    ati: String,
    onBackClick: () -> Unit,
    viewModel: BorrowerDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(ati) {
        if (ati.isEmpty()) {
            onBackClick()
        } else {
            viewModel.getBorrowDetail(ati)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borrower Data", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.iv_set_right_arrow),
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8FA))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFE5455)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Dinamis Section (Data Pribadi, Rincian Tagihan, Data Pekerjaan, Nilai Kredit)
                    uiState.borrowerSections.forEach { section ->
                        BorrowerInfoCard(section = section)
                    }

                    // 2. Section Catatan Pinjaman Historis
                    BorrowHistoryCard(borrowRecords = uiState.borrowRecordList)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BorrowerInfoCard(section: SectionInfo) {
    Card(
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 20.dp)) {
            // Title Header dengan Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = section.iconRes),
                    contentDescription = section.title,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = section.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFE5455) // primary-color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items Content
            section.contentList.forEach { item ->
                RowInfoLine(label = item.label, value = item.value)
            }
        }
    }
}

@Composable
private fun BorrowHistoryCard(borrowRecords: List<BorrowRecord>) {
    Card(
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 20.dp)) {
            // Title Header Catatan Historis
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.iv_borrower_ic_record),
                    contentDescription = "Catatan Pinjaman Historis",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Catatan Pinjaman Historis",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFE5455)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (borrowRecords.isNotEmpty()) {
                borrowRecords.forEachIndexed { index, record ->
                    RowInfoLine(label = "Nilai Pinjaman:", value = record.tma)
                    RowInfoLine(label = "Status peminjaman:", value = record.pdi)
                    RowInfoLine(label = "Apakah untuk membayar:", value = record.dun)
                    RowInfoLine(label = "Apakah sudah lewat waktu:", value = record.ods)

                    if (index < borrowRecords.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFE8E8E8)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada riwayat pinjaman",
                        color = Color(0xFF595959),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RowInfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF595959)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF595959),
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}