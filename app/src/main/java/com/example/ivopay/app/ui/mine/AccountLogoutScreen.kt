package com.example.ivopay.app.ui.mine

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.FaceDetectionView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLogoutScreen(
    viewModel: AccountLogoutViewModel,
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onNavigateToProduct: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var showFaceCamera by remember { mutableStateOf(false) }
    
    val txtArr = listOf(
        "Apabila sudah ada tagihan maka rekening tidak dapat dibatalkan.",
        "Setelah membatalkan akun, Anda tidak akan dapat mengajukan permohonan. Jika Anda ingin mengajukan lagi, Anda perlu mendaftarkan akun lagi dan mengisi data yang relevan. Harap berhati-hati saat membatalkan akun Anda.",
        "Setelah mengirimkan permintaan pembatalan akun, Anda dapat memulihkan kembali untuk melanjutkan penggunaan akun Anda dalam waktu 90 hari."
    )

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    if (showFaceCamera) {
        FaceDetectionView(
            onImageCaptured = { bitmap ->
                viewModel.logoutSubmitFace(bitmap)
                showFaceCamera = false
            },
            onClose = { showFaceCamera = false }
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Keluar", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF5F5F5))) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Header Blue/Red Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFF5949), Color(0xFFED7E7A))))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Batalkan akun", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                            // Image background logic missing icon, use placeholder if needed
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        // 2. Info Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("KTP：", color = Color.Gray, fontSize = 14.sp)
                                    Text(viewModel.userInfo?.customer?.personalInfo?.ktpMasked ?: "", fontSize = 14.sp)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Nomor telepon：", color = Color.Gray, fontSize = 14.sp)
                                    Text(viewModel.userPhone, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Suggestions Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Saran yang baik", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                                txtArr.forEach { text ->
                                    Row(modifier = Modifier.padding(bottom = 12.dp)) {
                                        Icon(painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn), contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFBD0100))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = text, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Bottom Button
                Button(
                    onClick = { viewModel.checkAccount() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                ) {
                    Text("Keluar", fontWeight = FontWeight.Bold)
                }

                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFBD0100))
                    }
                }
            }
        }
    }

    // --- POPUPS ---

    // 1. Select Reason Popup
    if (viewModel.showSelectReasonPop) {
        Dialog(onDismissRequest = { viewModel.showSelectReasonPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Terima kasih sudah bersama kami", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Bantu kami berkembang dengan memilih alasan Anda pergi (hanya 2 detik, bisa pilih lebih dari satu)",
                        fontSize = 14.sp, textAlign = TextAlign.Start, modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column {
                        viewModel.selectList1.forEach { item ->
                            val isChecked = viewModel.checkedReasons.value.contains(item.k)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val current = viewModel.checkedReasons.value.toMutableSet()
                                        if (isChecked) current.remove(item.k) else current.add(item.k)
                                        viewModel.checkedReasons.value = current
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isChecked) R.drawable.iv_choose2_sel else R.drawable.iv_choose2_nor),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isChecked) Color(0xFFBD0100) else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(item.v, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.submitRetentionReasons() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                    ) {
                        Text("Kirim")
                    }
                    Text(
                        "Belum membutuhkan pinjaman",
                        modifier = Modifier.padding(top = 16.dp).clickable { viewModel.showConfirmPop = true; viewModel.showSelectReasonPop = false },
                        textDecoration = TextDecoration.Underline,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // 2. Retain Special Offer Popup
    if (viewModel.showRetainPop) {
        Dialog(onDismissRequest = { viewModel.cancelRetain() }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PENAWARAN KHUSUS", fontWeight = FontWeight.Bold, color = Color(0xFFBD0100))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Karena riwayat kredit Anda yang baik, kami telah meningkatkan produk khusus untuk Anda. Klik untuk lihat sekarang.",
                        textAlign = TextAlign.Center, fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            viewModel.showRetainPop = false
                            onNavigateToProduct(viewModel.rtinPudtyp ?: 0, viewModel.rasn)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                    ) {
                        Text("Ambil kualifikasi sekarang")
                    }
                    Text(
                        "Abaikan kualifikasi, lanjutkan penghapusan",
                        modifier = Modifier.padding(top = 16.dp).clickable { viewModel.showConfirmPop = true; viewModel.showRetainPop = false },
                        textDecoration = TextDecoration.Underline, color = Color.Gray, fontSize = 12.sp
                    )
                }
            }
        }
    }

    // 3. Confirm Deletion Popup
    if (viewModel.showConfirmPop) {
        Dialog(onDismissRequest = { viewModel.showConfirmPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Kami mohon maaf jika produk kami belum memenuhi harapan Anda. Setelah Anda mengonfirmasi penutupan akun, seluruh hak dan manfaat akan langsung dihapus dan tidak dapat dipulihkan.",
                        fontSize = 14.sp, textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.showConfirmPop = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { 
                            viewModel.showConfirmPop = false
                            if (viewModel.needAig) showFaceCamera = true else viewModel.showSMSPop = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Konfirmasi", color = Color.Gray)
                    }
                }
            }
        }
    }

    // 4. SMS Verification Popup
    if (viewModel.showSMSPop) {
        Dialog(onDismissRequest = { viewModel.showSMSPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Isi kode verifikasi", fontWeight = FontWeight.Bold)
                    
                    TextField(
                        value = viewModel.verCode,
                        onValueChange = { if (it.length <= 4) viewModel.verCode = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        placeholder = { Text("4 digit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF9F9F9), unfocusedContainerColor = Color(0xFFF9F9F9))
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                        if (viewModel.sendAble) {
                            Text(
                                "Kirim kode verifikasi",
                                color = Color(0xFFBD0100),
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable { viewModel.sendVerCode { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
                            )
                        } else {
                            Text("${viewModel.verCountDown}s", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.onLogoutClick({ Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }, onLogoutSuccess) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                    ) {
                        Text("Konfirmasi")
                    }
                }
            }
        }
    }
}
