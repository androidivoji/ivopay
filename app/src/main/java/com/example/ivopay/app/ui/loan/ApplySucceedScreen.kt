package com.example.ivopay.app.ui.loan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplySucceedScreen(
    ocEui: Boolean = false,              // Dari store/state cme.oc_eui
    cashType: String? = null,           // Query param: cash_type
    needConfirm: Boolean = false,        // Query param: need_confirm == 1
    mob: String = "",                    // Query param: mob
    noc: String = "",                    // Query param: noc
    onNavigateHome: () -> Unit,
    onNavigateJmo: () -> Unit,
    onNavigateAppStore: (appId: String) -> Unit,
    onNavigateBpjsDetail: () -> Unit,
    onConfirmInsurance: (noc: String) -> Unit,
    showInitialRatePopup: Boolean = true // Di-pass berdasarkan cek data lokal (SharedPreferences)
) {
    var showInsurancePop by remember { mutableStateOf(false) }
    var showRatePop by remember { mutableStateOf(showInitialRatePopup) }

    // Intercept tombol back fisik Android -> arahkan ke Beranda
    BackHandler {
        if (ocEui) onNavigateJmo() else onNavigateHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "IVOCASH Borrower",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF262626)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Gambar Status Pengajuan
            // Ganti R.drawable.iv_apply_img dengan resource gambar Anda
            Image(
                painter = painterResource(id = R.drawable.iv_apply_img),
                contentDescription = "Gambar Status Pengajuan",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .wrapContentHeight()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Judul Status Pengajuan
            Text(
                text = if (ocEui) {
                    "Pengajuan Anda berhasil, sudah masuk tahap akhir. Hubungkan akun untuk dapat limit dan potongan bunga."
                } else {
                    "Pengajuan Anda Sedang Dalam Proses"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Deskripsi / Tahapan Tinjauan
            if (!needConfirm) {
                Text(
                    text = if (cashType == "ci10") {
                        "Setelah persetujuan selesai, petugas kami akan menghubungi Anda untuk melakukan verifikasi data di tempat. Setelah dikonfirmasi, pencairan dana akan dilakukan segera."
                    } else {
                        "Mohon tunggu, saat ini pengajuan pinjaman Anda sudah kami terima dan sedang kami proses. Anda akan menerima notifikasi melalui aplikasi ini saat proses selesai."
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF8C8C8C),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else {
                // Tampilan Langkah 01 & 02
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Tahap 01
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xFFF0F0F0)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("01", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mohon tunggu, saat ini pengajuan pinjaman Anda sudah kami terima dan sedang kami proses. Anda akan menerima notifikasi melalui aplikasi ini saat proses selesai.",
                            fontSize = 13.sp,
                            color = Color(0xFF8C8C8C),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Tahap 02
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(22.dp),
                                color = Color(0xFFF0F0F0)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("02", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Setelah selesai ditinjau, kamu perlu membuka aplikasi IVOJI,",
                                fontSize = 13.sp,
                                color = Color(0xFFBD0100),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "setelah itu klik konfirmasi baru bisa melakukan pembayaran, harap perhatikan aplikasi tepat waktu untuk notifikasi SMS atau telepon.",
                                fontSize = 13.sp,
                                color = Color(0xFF8C8C8C)
                            )
                        }
                    }
                }
            }

            // Opsi Ubah Nomor Telepon (ci10)
            if (cashType == "ci10") {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Nomor Telepon: $mob", fontSize = 13.sp, color = Color(0xFF262626))
                        Text(
                            text = "Ubah",
                            fontSize = 13.sp,
                            color = Color(0xFFBD0100),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { /* Trigger dialog ubah HP */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Utama (Kembali ke Beranda / Dialihkan)
            Button(
                onClick = {
                    if (ocEui) onNavigateJmo() else onNavigateHome()
                },
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (ocEui) "Dialihkan ke link tautan" else "Kembali ke beranda",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet / Dialog BPJS TK Insurance
    if (showInsurancePop) {
        ModalBottomSheet(
            onDismissRequest = { showInsurancePop = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "IVOJI sudah bekerjasama dengan BPJSTK untuk layanan jaminan sosial bagi pekerja tidak dibayar (BPU). Apakah Anda tertarik untuk cari tahu?",
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Periksa detailnya",
                        fontSize = 14.sp,
                        color = Color(0xFFBD0100),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            showInsurancePop = false
                            onNavigateBpjsDetail()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { showInsurancePop = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Tidak", color = Color(0xFF333333))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            showInsurancePop = false
                            onConfirmInsurance(noc)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Ya", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Dialog Penilaian App Store (Rating)
    if (showRatePop) {
        AlertDialog(
            onDismissRequest = { showRatePop = false },
            title = null,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showRatePop = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(120.dp),
                        color = Color(0xFFF9F9F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("[Banner Penilaian App]", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Permohonan Anda telah diajukan untuk ditinjau. Pinjaman akan segera dicairkan setelah peninjauan selesai. Silakan tinggalkan ulasan.",
                        fontSize = 14.sp,
                        color = Color(0xFF262626),
                        textAlign = TextAlign.Left
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showRatePop = false
                            onNavigateAppStore("1519599799")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Berikan penilaian sekarang", color = Color.White)
                    }
                }
            },
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}