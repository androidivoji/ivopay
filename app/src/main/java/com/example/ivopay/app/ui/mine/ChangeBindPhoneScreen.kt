package com.example.ivopay.app.ui.mine

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.KtpCameraView
import androidx.camera.core.CameraSelector
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeBindPhoneScreen(
    viewModel: ChangeBindPhoneViewModel,
    onBackClick: () -> Unit,
    onNavigateToBill: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var showCamera by remember { mutableStateOf(false) }
    var captureMode by remember { mutableStateOf(0) } // 0: KTP, 1: Selfie

    // Launcher untuk izin kamera (Pre-check matching BaseInfoScreen)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkCanUpdatePhone()
    }

    if (showCamera) {
        KtpCameraView(
            onImageCaptured = { bitmap ->
                if (captureMode == 0) viewModel.ktpBitmap = bitmap
                else viewModel.selfieBitmap = bitmap
                showCamera = false
            },
            onClose = { showCamera = false },
            cameraSelector = if (captureMode == 1) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
            isFaceMode = captureMode == 1
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Ubah nomor ponsel Anda", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F8FA))
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // 1. Inputs Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Phone Number
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Nomor Telepon:", fontSize = 14.sp)
                                TextField(
                                    value = viewModel.phoneNumber,
                                    onValueChange = { viewModel.phoneNumber = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Masukkan nomor ponsel") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true
                                )
                            }
                            
                            HorizontalDivider(color = Color(0xFFEEEEEE))

                            // Ver Code
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Kode verifikasi:", fontSize = 14.sp)
                                TextField(
                                    value = viewModel.verCode,
                                    onValueChange = { if (it.length <= 4) viewModel.verCode = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("4 digit") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    trailingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .background(
                                                    if (viewModel.verCountDown == 0) Color(0xFFBD0100) else Color.LightGray,
                                                    RoundedCornerShape(2.dp)
                                                )
                                                .clickable(enabled = viewModel.verCountDown == 0) {
                                                    viewModel.sendVerificationCode { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (viewModel.verCountDown == 0) "Kirim" else "${viewModel.verCountDown}s",
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Photo Section
                    Text("Dokumen Identitas", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))

                    // KTP Photo
                    PhotoUploadCard(
                        title = "Ambil foto KTP",
                        bitmap = viewModel.ktpBitmap,
                        exampleRes = R.drawable.iv_data_ktp_front,
                        onClick = {
                            captureMode = 0
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selfie Photo
                    PhotoUploadCard(
                        title = "Ambil foto selfie dengan KTP",
                        bitmap = viewModel.selfieBitmap,
                        exampleRes = R.drawable.iv_data_ic_sign, // Replace with selfie example if exists
                        onClick = {
                            captureMode = 1
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Submit Button
                    val isEnabled = viewModel.phoneNumber.isNotEmpty() && viewModel.verCode.length == 4 && 
                                    viewModel.ktpBitmap != null && viewModel.selfieBitmap != null
                    
                    Button(
                        onClick = { viewModel.submitInfo { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } },
                        enabled = isEnabled,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Selanjutnya", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Popups
    if (viewModel.showSuccessPop) {
        Dialog(onDismissRequest = { viewModel.showSuccessPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Perubahan nomor ponsel Anda berhasil, akan diproses dalam waktu 24 jam. Pesan teks akan segera dikirim.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            viewModel.showSuccessPop = false
                            onNavigateHome()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                    ) {
                        Text("Tentu")
                    }
                }
            }
        }
    }

    if (viewModel.showHaveBillsPop) {
        Dialog(onDismissRequest = { viewModel.showHaveBillsPop = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = viewModel.errMsg, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            viewModel.showHaveBillsPop = false
                            if (viewModel.errCode == 102) onNavigateToBill()
                            else onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD0100))
                    ) {
                        Text(if (viewModel.errCode == 102) "Mohon diperiksa" else "Tentu")
                    }
                }
            }
        }
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFBD0100))
        }
    }
}

@Composable
fun PhotoUploadCard(
    title: String,
    bitmap: Bitmap?,
    exampleRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = exampleRes),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        alpha = 0.5f
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = title, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}
