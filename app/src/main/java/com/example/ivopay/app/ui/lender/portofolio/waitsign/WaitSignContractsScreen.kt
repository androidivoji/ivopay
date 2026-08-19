package com.example.ivopay.app.ui.lender.portofolio.waitsign

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
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay

// Data Model Kontrak
data class ContractItem(
    @SerializedName("mdi") val mdi: String,
    @SerializedName("lfn") val lfn: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("tlr") val tlr: Double,
    @SerializedName("let") val let: String,
    @SerializedName("mta") val mta: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitSignContractsScreen(
    odi: String,
    onBackClick: () -> Unit,
    onNavigateToBorrowerSign: (String) -> Unit,
    onNavigateToPlatformSign: (String) -> Unit,
    viewModel: WaitSignContractsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // State
    var showSignAllItemsPop by remember { mutableStateOf(false) }
    var showSignProgressPop by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }
    var percent by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    // Fetch Data saat mounted
    LaunchedEffect(odi) {
        if (odi.isEmpty()) {
            onBackClick()
        } else {
            viewModel.getOrderDetail(odi)
        }
    }

    // Effect untuk Progress Bar Batch Sign (menggantikan setInterval)
    LaunchedEffect(showSignProgressPop) {
        if (showSignProgressPop) {
            percent = 0
            while (percent < 100) {
                delay(1000)
                percent += 10
            }
            showSignProgressPop = false
            Toast.makeText(context, "Penandatanganan batch berhasil", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    fun onJumpSignPage(item: ContractItem) {
        when (item.mta) {
            102 -> onNavigateToBorrowerSign(item.mdi)
            103 -> onNavigateToPlatformSign(item.mdi)
            104 -> Toast.makeText(context, "Kontrak telah ditandatangani, menunggu pembayaran, harap tunggu", Toast.LENGTH_SHORT).show()
            105 -> Toast.makeText(context, "Kontrak telah ditandatangani, menunggu pembayaran, harap tunggu", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tanda Tangan Kontrak", fontSize = 18.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.iv_set_left_arrow), // Ganti icon back Anda
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Sticky Bottom Button with dynamic navigation bar padding
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { showSignAllItemsPop = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Tanda Tangan Sekaligus", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada kontrak", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(uiState.contractLists) { index, item ->
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
                                .clickable { onJumpSignPage(item) }
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

                                    Icon(
                                        painter = painterResource(id = R.drawable.iv_set_right_arrow),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Popup Sign All
    if (showSignAllItemsPop) {
        Dialog(onDismissRequest = { showSignAllItemsPop = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tanda Tangan Semua Perjanjian", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.contractLists.forEachIndexed { idx, item ->
                        Text(
                            text = "${idx + 1}. ${item.lfn}",
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChecked = !isChecked }
                    ) {
                        RadioButton(
                            selected = isChecked,
                            onClick = { isChecked = !isChecked },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFE5455))
                        )
                        Text(
                            "Saya telah membaca dan memahami setiap perjanjian secara keseluruhan",
                            color = Color(0xFF8C8C8C),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (!isChecked) {
                                Toast.makeText(context, "Silahkan centang", Toast.LENGTH_SHORT).show()
                            } else {
                                showSignAllItemsPop = false
                                showSignProgressPop = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tanda Tangan Semua", color = Color.White)
                    }
                }
            }
        }
    }

    // Modal Progress Bar
    if (showSignProgressPop) {
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Progres Penandatanganan Perjanjian", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(20.dp))

                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Color(0xFFBD0100),
                        trackColor = Color(0xFFF2F2F2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Perjanjian sedang ditandatangani, mohon jangan tutup halaman",
                        color = Color(0xFF8C8C8C),
                        fontSize = 12.sp
                    )
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