package com.example.ivopay.app.ui.mine

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import java.util.Calendar

// Data Model State untuk Form Informasi Dasar
data class BaseInfoState(
    var idNum: String = "",
    var fullName: String = "",
    var gender: String = "",
    var birthDate: String = "",
    var birthPlace: String = "",
    var email: String = "",
    var residenceAddress: String = "",
    var officeAddress: String = "",
    var ktpImgUrl: String = "",
    var isAgreed: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseInfoScreen(
    onBackClick: () -> Unit,
    onNextClick: (BaseInfoState) -> Unit,
    onSelectKtpPhoto: () -> Unit,
    onOpenTermsAndConditions: () -> Unit,
    ocrStatus: Int = 0, // 0: Normal, 2: Success, 3: Failed
    isOcrLoading: Boolean = false,
    readOnly: Boolean = false
) {
    val context = LocalContext.current
    var formState by remember { mutableStateOf(BaseInfoState()) }

    // Dialog state
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showGenderPicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Setup DatePicker untuk Tanggal Lahir
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            formState = formState.copy(birthDate = "$dayOfMonth/${month + 1}/$year")
        },
        calendar.get(Calendar.YEAR) - 20,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informasi Dasar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. KTP Photo Upload / Scan Card
            KtpPhotoCard(
                ktpImgUrl = formState.ktpImgUrl,
                isLoading = isOcrLoading,
                onClick = onSelectKtpPhoto
            )

            // 2. Banner Tips Status OCR
            if (ocrStatus != 0 || formState.ktpImgUrl.isEmpty()) {
                OcrTipsBanner(ocrStatus = ocrStatus, hasPhoto = formState.ktpImgUrl.isNotEmpty())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Form Inputs
            // Nomor KTP (NIK)
            OutlinedTextField(
                value = formState.idNum,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 16) {
                        formState = formState.copy(idNum = filtered)
                    }
                },
                label = { Text("Nomor KTP (16 Digit)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = readOnly,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = formState.idNum.isNotEmpty() && formState.idNum.length < 16
            )
            if (formState.idNum.isNotEmpty() && formState.idNum.length < 16) {
                Text("Harus dalam format 16 digit", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nama Lengkap
            OutlinedTextField(
                value = formState.fullName,
                onValueChange = { input ->
                    formState = formState.copy(fullName = input.filter { it.isLetter() || it.isWhitespace() })
                },
                label = { Text("Nama Lengkap") },
                supportingText = { Text("*Silakan isi nama Anda sesuai dengan yang tertera pada KTP Anda", color = Color.Red) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = readOnly
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Jenis Kelamin
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.gender,
                    onValueChange = {},
                    label = { Text("Jenis Kelamin (Laki-laki / Perempuan)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showGenderPicker = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tanggal Lahir
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.birthDate,
                    onValueChange = {},
                    label = { Text("Tanggal Lahir") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { datePickerDialog.show() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tempat Lahir
            OutlinedTextField(
                value = formState.birthPlace,
                onValueChange = { formState = formState.copy(birthPlace = it) },
                label = { Text("Tempat Lahir") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email Gmail
            OutlinedTextField(
                value = formState.email,
                onValueChange = { formState = formState.copy(email = it.trim()) },
                label = { Text("Gmail / Alamat Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alamat Domisili
            OutlinedTextField(
                value = formState.residenceAddress,
                onValueChange = { formState = formState.copy(residenceAddress = it) },
                label = { Text("Alamat tempat tinggal / domisili") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Alamat Kantor
            OutlinedTextField(
                value = formState.officeAddress,
                onValueChange = { formState = formState.copy(officeAddress = it) },
                label = { Text("Alamat kantor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Checkbox Persetujuan Syarat & Ketentuan VIDA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = formState.isAgreed,
                    onCheckedChange = { formState = formState.copy(isAgreed = it) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Saya telah membaca, memahami, dan menyetujui ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "Syarat dan Ketentuan untuk verifikasi identitas.",
                fontSize = 12.sp,
                color = Color(0xFFBD0100),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 48.dp)
                    .clickable { onOpenTermsAndConditions() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons (Sebelumnya & Selanjutnya)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Sebelumnya", color = Color(0xFF262626))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (!formState.isAgreed) {
                            errorMessage = "Anda harus menyetujui persyaratan dan ketentuan"
                        } else if (formState.idNum.length < 16) {
                            errorMessage = "NIK KTP harus 16 digit"
                        } else {
                            showConfirmDialog = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Selanjutnya", color = Color.White)
                }
            }
        }
    }

    // Modal Dropdown Gender
    if (showGenderPicker) {
        AlertDialog(
            onDismissRequest = { showGenderPicker = false },
            title = { Text("Pilih Jenis Kelamin") },
            text = {
                Column {
                    Text(
                        "Laki-laki",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                formState = formState.copy(gender = "Laki-laki")
                                showGenderPicker = false
                            }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider()
                    Text(
                        "Perempuan",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                formState = formState.copy(gender = "Perempuan")
                                showGenderPicker = false
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Modal Konfirmasi Data NIK & Nama
    if (showConfirmDialog) {
        Dialog(onDismissRequest = { showConfirmDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("KONFIRMASI DATA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Harap pastikan data sesuai dengan KTP, lakukan edit jika diperlukan:",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF6F7F9), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Nama: ${formState.fullName}", fontWeight = FontWeight.Medium)
                        Text("NIK: ${formState.idNum}", fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Jika terdapat ketidaksesuaian data dengan KTP maka akan mempengaruhi lamanya proses pengajuan pinjaman",
                        fontSize = 11.sp,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showConfirmDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Belum Sesuai")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                showConfirmDialog = false
                                onNextClick(formState)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sesuai")
                        }
                    }
                }
            }
        }
    }
}

// Sub-komponen Card Upload Foto KTP
@Composable
fun KtpPhotoCard(
    ktpImgUrl: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .background(Color(0xFFFAFAFA))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFBD0100))
        } else if (ktpImgUrl.isNotEmpty()) {
            // Tampilkan foto KTP jika sudah diunggah
            Image(
                painter = painterResource(id = R.drawable.iv_data_ktp_front), // ganti dengan coil/glide jika URL
                contentDescription = "KTP Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.iv_data_ktp_front),
                    contentDescription = "Contoh KTP",
                    modifier = Modifier.size(100.dp, 60.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Pindai sisi depan KTP (sisi yang ada foto)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Sub-komponen Banner Tips OCR
@Composable
fun OcrTipsBanner(ocrStatus: Int, hasPhoto: Boolean) {
    val (bgColor, txt) = when {
        ocrStatus == 2 -> Pair(Color(0xFFE6F7ED), "Pastikan data benar, kesalahan bisa memengaruhi pengajuan. Tidak bisa diubah.")
        ocrStatus == 3 -> Pair(Color(0xFFFFF0F0), "Gagal mengenali, isi data KTP atau coba lagi. Tidak bisa diubah setelah dikirim.")
        !hasPhoto -> Pair(Color(0xFFFFF0F0), "Foto di tempat terang, ponsel tegak, keempat sudut terlihat.")
        else -> Pair(Color.Transparent, "")
    }

    if (txt.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(bgColor, RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Text(text = txt, fontSize = 12.sp, color = Color(0xFF333333))
        }
    }
}