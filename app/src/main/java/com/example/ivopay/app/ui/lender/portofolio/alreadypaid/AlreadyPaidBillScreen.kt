package com.example.ivopay.app.ui.lender.portofolio.alreadypaid

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import java.text.NumberFormat
import java.util.Locale

// Helper formatting Rupiah
private fun Double.toRp(): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0
    return format.format(this)
}

@Composable
fun AlreadyPaidBillScreen(
    viewModel: AlreadyPaidViewModel = remember { AlreadyPaidViewModel() },
    onNavigateToDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFB12127)
            )
        } else if (uiState.contractLists.isEmpty()) {
            // State Jika Data Kosong
            EmptyStateView()
        } else {
            // List Kontrak yang sudah dibayar
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                itemsIndexed(uiState.contractLists) { index, item ->
                    PaidOrderItemCard(
                        orderIndex = index + 1,
                        item = item,
                        onItemClick = { onNavigateToDetail(item.odi) }
                    )
                }
            }
        }
    }
}

@Composable
fun PaidOrderItemCard(
    orderIndex: Int,
    item: PaidOrderItem,
    onItemClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Text Order X
        Text(
            text = "Order $orderIndex",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF262626),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick() }
        ) {
            Column {
                // Header Card dengan Gradien Merah (#EB6767 -> #B12127)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFB12127),
                                    Color(0xFFEB6767)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CardRowInfo(label = "Name", value = item.bnm)
                        CardRowInfo(label = "Bank Name", value = item.bkn)
                        CardRowInfo(label = "No.VA", value = item.pcd)
                        CardRowInfo(label = "Amount", value = item.toa.toString())
                        CardRowInfo(label = "Total", value = item.tpa.toRp())
                    }
                }

                // Bottom Card Row (Logo & Arrow)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iv_invest_logo),
                        contentDescription = "Invest Logo",
                        modifier = Modifier.width(55.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.iv_set_right_arrow),
                        contentDescription = "Next Arrow",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardRowInfo(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            color = Color.White
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.iv_apply_empty_state),
            contentDescription = "Empty State",
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Data tidak ditemukan",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}