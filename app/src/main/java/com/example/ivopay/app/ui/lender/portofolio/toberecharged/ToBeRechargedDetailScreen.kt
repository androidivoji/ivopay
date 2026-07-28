package com.example.ivopay.app.ui.lender.portofolio.toberecharged

import android.widget.Toast
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R // Sesuaikan dengan package R Anda
import kotlinx.coroutines.delay

// Data Model Kontrak
data class ContractItem(
    val mdi: String,
    val lfn: String,
    val lat: Double,
    val tlr: Double,
    val let: String,
    val mta: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToBeRechargedDetailScreen(
    odi: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var contractLists by remember { mutableStateOf<List<ContractItem>>(emptyList()) }

    // Simulasi Fetch Data saat mounted (menggantikan mounted() getContracts)
    LaunchedEffect(odi) {
        if (odi.isEmpty()) {
            onBackClick()
        } else {
            // TODO: Ganti dengan pemanggilan ViewModel/API _getOrderDetail
            contractLists = listOf(
                ContractItem("MDI_01", "Perjanjian Pinjaman 1", 5000000.0, 250000.0, "2026-08-01", 102)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borrower Detail", fontSize = 18.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.iv_set_right_arrow), // Ganti icon back Anda
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(contractLists) { index, item ->
                Text(
                    text = "Pesanan ${index + 1}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                // Card Item Kontrak
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clickable { }
                ) {
                    Column {
                        // Header Merah Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFB12127), Color(0xFFEB6767))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(item.lfn, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                RowJustifyBetween("Jumlah Pinjaman:", "Rp ${item.lat.toLong()}")
                                RowJustifyBetween("Pendapatan Lender:", "Rp ${item.tlr.toLong()}")
                                RowJustifyBetween("Tanggal Jatuh Tempo:", item.let)
                            }
                        }

                        // Bottom Action Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.iv_invest_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Beri Tandatangan", color = Color(0xFF8C8C8C), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowJustifyBetween(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}