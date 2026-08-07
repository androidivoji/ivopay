package com.example.ivopay.app.ui.lender.mycontracts

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivopay.R
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyContractsScreen(
    onBackClick: () -> Unit,
    onNavigateToChooseContracts: (cno: String) -> Unit,
    viewModel: MyContractsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kontrak", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
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
                .background(Color(0xFFF8F8F8))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFE5455)
                )
            } else if (uiState.contractLists.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(uiState.contractLists) { item ->
                        MyContractCardItem(
                            item = item,
                            onItemClick = { onNavigateToChooseContracts(item.cno) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyContractCardItem(
    item: MyContractItem,
    onItemClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .clickable { onItemClick() }
    ) {
        Column {
            // Top Section with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFB12127), Color(0xFFEB6767))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    RowJustifyBetween("Jumlah pembayaran:", formatRupiah(item.tma))
                    RowJustifyBetween("Nama:", item.lfn)
                    RowJustifyBetween("Tanggal penandatanganan:", item.sgd)
                }
            }

            // Bottom Section (Logo & Arrow)
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

                Icon(
                    painter = painterResource(id = R.drawable.iv_set_right_arrow),
                    contentDescription = "Go Detail",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun RowJustifyBetween(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

private fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}
