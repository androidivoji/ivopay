package com.example.ivopay.app.ui.loan

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.SignatureCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerSignContractsScreen(
    noc: String,
    isWiue: Boolean, // Pass this from navigation
    viewModel: BorrowerSignContractsViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    LaunchedEffect(noc) {
        viewModel.init(noc, isWiue)
    }

    // Reset canSign when new HTML is loaded
    LaunchedEffect(viewModel.htmlText) {
        if (viewModel.htmlText.isNotEmpty()) {
            viewModel.canSign = false
        }
    }

    // Scroll Listener to enable signing
    LaunchedEffect(scrollState.value, scrollState.maxValue, viewModel.isLoading, viewModel.htmlText) {
        if (viewModel.isLoading || viewModel.htmlText.isEmpty()) {
            viewModel.canSign = false
            return@LaunchedEffect
        }

        if (scrollState.maxValue > 0) {
            // Content is scrollable, check if we reached the bottom
            if (scrollState.value >= scrollState.maxValue - 50) {
                if (!viewModel.canSign) {
                    android.util.Log.d("SCROLL_DEBUG", "Reached bottom: ${scrollState.value} / ${scrollState.maxValue}")
                    viewModel.canSign = true
                }
            }
        } else {
            // Potentially non-scrollable content. Wait for measurement.
            kotlinx.coroutines.delay(1000)
            if (scrollState.maxValue <= 0 && viewModel.htmlText.isNotEmpty()) {
                android.util.Log.d("SCROLL_DEBUG", "Content is definitely not scrollable")
                viewModel.canSign = true
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tandatangan Perjanjian", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = "Back", modifier = Modifier.size(24.dp))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (viewModel.htmlText.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Download Button if dpdf exists
                    if (viewModel.dpdf.startsWith("http")) {
                        Button(
                            onClick = { 
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(viewModel.dpdf))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFFE5455)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFE5455)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Download Kontrak", fontSize = 12.sp)
                        }
                    }

                    // WebView wrapped in a Scrollable Box to detect scroll to bottom
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp)
                    ) {
                        AndroidView(
                            factory = {
                                WebView(it).apply {
                                    settings.javaScriptEnabled = true
                                    // Disable WebView's internal scroll to let Compose handle it
                                    isVerticalScrollBarEnabled = false
                                    overScrollMode = android.view.View.OVER_SCROLL_NEVER
                                }
                            },
                            update = {
                                it.loadDataWithBaseURL(null, viewModel.htmlText, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    if (viewModel.showSignBtn) {
                        Button(
                            onClick = { 
                                if (viewModel.canSign) {
                                    viewModel.closeAllPopups()
                                    if (viewModel.isWiue) {
                                        viewModel.sendCode { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                    } else {
                                        viewModel.showSignPop = true
                                    }
                                } else {
                                    viewModel.closeAllPopups()
                                    viewModel.showSignTipsPop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.canSign) Color(0xFFFE5455) else Color(0xFFCCCCCC)
                            )
                        ) {
                            Text("Tandatangani", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (!viewModel.isLoading) {
                // Empty State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iv_apply_empty_state), 
                        contentDescription = null, 
                        modifier = Modifier.size(200.dp)
                    )
                    Text("Belum ada kontrak", fontSize = 16.sp, color = Color.Gray)
                }
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }
        }
    }

    // 1. Sign Tips Popup
    if (viewModel.showSignTipsPop) {
        Dialog(onDismissRequest = { viewModel.showSignTipsPop = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Mohon membaca dan memahami keseluruhan perjanjian ini, sebelum melanjutkan proses tanda tangan.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                        color = Color(0xFF262626)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.showSignTipsPop = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text("Setuju")
                    }
                }
            }
        }
    }

    // 2. OTP VIDA Popup
    if (viewModel.showVIDACodePop) {
        Dialog(onDismissRequest = { viewModel.showVIDACodePop = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Silakan masukin kode OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Telah mengirim sms Kode OTP ke nomor anda", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    
                    OutlinedTextField(
                        value = viewModel.verCode,
                        onValueChange = { if (it.length <= 4) viewModel.verCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Kode OTP") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                        Text(
                            text = if (viewModel.sendAble) "Kirim kode verifikasi" else "Kirim kode verifikasi(${viewModel.verCountDown}s)",
                            color = if (viewModel.sendAble) Color(0xFFBD0100) else Color.Gray,
                            modifier = Modifier.clickable(enabled = viewModel.sendAble) { viewModel.sendCode { } }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(
                            checked = viewModel.checkAgree,
                            onCheckedChange = { viewModel.checkAgree = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFE5455))
                        )
                        Text(
                            text = "Saya telah membaca, memahami, dan menyetujui Syarat dan Ketentuan untuk tanda tangan digital.",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Button(
                        onClick = { 
                            viewModel.verifyOTP(
                                viewModel.verCode,
                                onSuccess = { },
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text("Kirim")
                    }
                }
            }
        }
    }

    // 3. Signature Canvas Popup
    if (viewModel.showSignPop) {
        Dialog(onDismissRequest = { viewModel.showSignPop = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Harap tanda tangan di sini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = { viewModel.showSignPop = false }) {
                            Icon(painterResource(id = R.drawable.iv_popup_ic_cancel), contentDescription = "Close", modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SignatureCanvas(
                        onClear = { viewModel.signImage = null },
                        onSubmit = { bitmap ->
                            viewModel.signImage = bitmap
                            viewModel.submitSignature(
                                bitmap = bitmap,
                                onSuccess = {
                                    viewModel.closeAllPopups()
                                    Toast.makeText(context, "Tanda tangan selesai", Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                },
                                onError = { code, msg -> 
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (code == 101) {
                                        onBackClick()
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

