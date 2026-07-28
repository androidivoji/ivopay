package com.example.ivopay.app.ui.lender.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivopay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlreadyPaidBillDetailScreen(
    odi: String,
    onBackClick: () -> Unit,
    onNavigateToChooseContracts: (mdi: String) -> Unit,
    viewModel: AlreadyPaidBillDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(odi) {
        if (odi.isEmpty()) {
            onBackClick()
        } else {
            viewModel.getOrderDetail(odi)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order already paid", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
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
            } else if (uiState.contractLists.isEmpty()) {
                // Empty State View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iv_apply_empty_state),
                        contentDescription = "Empty State",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .wrapContentHeight()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Data tidak ditemukan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF262626)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sementara tidak ada catatan",
                        fontSize = 14.sp,
                        color = Color(0xFF8C8C8C)
                    )
                }
            } else {
                // List of Paid Contracts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 70.dp)
                ) {
                    items(uiState.contractLists) { item ->
                        PaidContractCardItem(
                            item = item,
                            statusInfo = viewModel.getItemStatus(item.mta),
                            formatRupiah = { viewModel.formatRupiah(it) },
                            onCheckContractClick = { onNavigateToChooseContracts(item.mdi) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaidContractCardItem(
    item: PaidContractItem,
    statusInfo: StatusInfo,
    formatRupiah: (Double) -> String,
    onCheckContractClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
    ) {
        Column {
            // Wait Sign Top Section dengan Gradient Background Red
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFB12127), Color(0xFFEB6767))
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    RowInfoText(label = "Name:", value = item.lfn)
                    RowInfoText(label = "Loan Amount:", value = formatRupiah(item.lat))
                    RowInfoText(label = "Lender income:", value = formatRupiah(item.tlr))
                    RowInfoText(label = "Due date:", value = item.let)

                    // Check Contract Row (Clickable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { onCheckContractClick() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Check contract:",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.iv_set_right_arrow_1),
                            contentDescription = "Check Contract",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Wait Sign Bottom Section (Logo & Status Badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iv_invest_logo),
                    contentDescription = "Invest Logo",
                    modifier = Modifier.width(55.dp)
                )

                // Status Badge (Custom shape matching CSS border-radius: 11px 0px 0px 11px)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 11.dp, bottomStart = 11.dp))
                        .background(statusInfo.bgColor)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusInfo.txt,
                        color = statusInfo.color,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun RowInfoText(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}