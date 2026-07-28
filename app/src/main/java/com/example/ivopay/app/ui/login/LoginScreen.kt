package com.example.ivopay.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R

val BrandRed = Color(0xFFBD0100)
val ActiveRed = Color(0xFFFE5455)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onBackClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Login", fontSize = 17.sp, fontWeight = FontWeight.Medium)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.haveInputNumber) {
                            viewModel.haveInputNumber = false
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Logo Aplikasi (iv_logo_ivoji_splash.png)
                Image(
                    painter = painterResource(id = R.drawable.iv_logo_ivoji_splash),
                    contentDescription = "Logo App",
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pesan Pilihan Metode Verifikasi (v-show="showLoginWay")
                if (viewModel.showLoginWay) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (viewModel.codeWayChecked == "1") "Silahkan periksa pesan WhatsApp" else "Silahkan periksa SMS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (viewModel.codeWayChecked == "1") "Silahkan masukkan kode verifikasi yang kami kirimkan ke akun Whatsapp anda" else "Silahkan masukkan kode verifikasi SMS yang dikirimkan",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Field Input Nomor HP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "+62 |", color = Color(0xFF262626), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = viewModel.userPhone,
                        onValueChange = { input ->
                            val cleanInput = input.filter { it.isDigit() }
                            val maxLen = if (cleanInput.startsWith("08")) 13 else 12
                            if (cleanInput.length <= maxLen) {
                                viewModel.userPhone = cleanInput
                            }
                        },
                        placeholder = { Text("8XXX XXXX XXXX", color = Color.LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Field Input Kode OTP (Muncul jika haveInputNumber == true)
                if (viewModel.haveInputNumber) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.verCode,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() }) {
                                    viewModel.verCode = input
                                }
                            },
                            placeholder = { Text("Kode OTP 4 Digit", color = Color.LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Tombol Kirim Ulang OTP
                        if (viewModel.sendAble) {
                            Text(
                                text = "Kirim kode verifikasi",
                                color = ActiveRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    viewModel.startCountDown()
                                }
                            )
                        } else {
                            Text(
                                text = "${viewModel.verCountDown}s",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Radio Group Pilihan WhatsApp / SMS
                if (viewModel.showLoginWay) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(width = 3.dp, height = 12.dp).background(BrandRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Pilih metode verifikasi", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (viewModel.showWaLogin) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.codeWayChecked = "1" }.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "WhatsApp")
                                RadioButton(
                                    selected = viewModel.codeWayChecked == "1",
                                    onClick = { viewModel.codeWayChecked = "1" },
                                    colors = RadioButtonDefaults.colors(selectedColor = ActiveRed)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.codeWayChecked = "2" }.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "SMS")
                            RadioButton(
                                selected = viewModel.codeWayChecked == "2",
                                onClick = { viewModel.codeWayChecked = "2" },
                                colors = RadioButtonDefaults.colors(selectedColor = ActiveRed)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Utama (Selanjutnya / Login & Registrasi)
                Button(
                    onClick = {
                        if (viewModel.haveInputNumber) {
                            viewModel.handleLoginClick { targetRoute ->
                                onNavigate(targetRoute)
                            }
                        } else {
                            viewModel.handleNextClick(
                                onGestureLogin = { onNavigate("GestureLogin") },
                                onFaceLogin = { onNavigate("FaceCheckWaitingPage") },
                                onBaseInfo = { onNavigate("BaseInfo") },
                                onShowOtpInput = { viewModel.startCountDown() }
                            )
                        }
                    },
                    enabled = if (viewModel.haveInputNumber) viewModel.isFormValid else viewModel.isPhoneValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActiveRed,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = if (viewModel.haveInputNumber) "Login/ Registrasi" else "Selanjutnya",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox Syarat & Ketentuan / Kebijakan Privasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = viewModel.checkAgree,
                        onCheckedChange = { viewModel.checkAgree = it },
                        colors = CheckboxDefaults.colors(checkedColor = ActiveRed)
                    )

                    val annotatedString = buildAnnotatedString {
                        append("Dengan login Anda setuju dengan ")
                        withStyle(style = SpanStyle(
                            color = ActiveRed,
                            fontWeight = FontWeight.Medium
                        )
                        ) {
                            append("syarat ketentuan")
                        }
                        append(" dan ")
                        withStyle(style = SpanStyle(color = ActiveRed, fontWeight = FontWeight.Medium)) {
                            append("kebijakan privasi")
                        }
                        append(" yang berlaku")
                    }

                    Text(
                        text = annotatedString,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // Indikator Loading
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color = ActiveRed,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // Modal Popup Pemulihan Akun (van-popup v-model:show="showLoginTipPop")
    if (viewModel.showLoginTipPop) {
        Dialog(onDismissRequest = { viewModel.showLoginTipPop = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(0.82f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saran yang baik",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191919)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Terdeteksi jika Anda telah mengajukan permohonan pembatalan akun, silahkan login ke akun Anda dan langsung pulihkan akun Anda.",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "KTP: ${viewModel.inmText}",
                        fontSize = 13.sp,
                        color = Color(0xFF191919),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.showLoginTipPop = false
                            onNavigate("main")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Konfirmasi", color = Color.White)
                    }
                }
            }
        }
    }
}