package com.example.ivopay.app.ui.lender.portofolio.waitsign

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ivopay.R
import com.example.ivopay.app.ui.components.SignatureCanvas
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowerSignContractsScreen(
    mdi: String,
    onBackClick: () -> Unit,
    onNavigateToPlatformSign: (String) -> Unit,
    viewModel: BorrowerSignContractsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(mdi) {
        if (mdi.isEmpty()) {
            onBackClick()
        } else {
            viewModel.loadData(mdi)
        }
    }

    LaunchedEffect(uiState.isSignSuccess) {
        if (uiState.isSignSuccess) {
            // Pengganti _routeReplace('PlatformSignContractsPage', { mdi })
            onNavigateToPlatformSign(mdi)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tandatangan Perjanjian", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            // Auto scroll ke bawah lalu tampilkan popup (menggantikan _scrollViewToShow)
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                                viewModel.setShowSignPop(true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Tandatangani", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Render HTML Perjanjian (v-html="htmlText")
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, uiState.htmlText, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Signature Dialog (<van-popup v-model:show="showSignPop">)
    if (uiState.showSignPop) {
        Dialog(onDismissRequest = { viewModel.setShowSignPop(false) }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Popup + Icon Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { viewModel.setShowSignPop(false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.iv_popup_ic_cancel),
                                contentDescription = "Close",
                                tint = Color.Unspecified
                            )
                        }
                    }

                    Text(
                        text = "Harap tanda tangan di sini, dan klik tombol [OK] untuk menyimpan tanda tangan setelah tanda tangan",
                        color = Color(0xFF262626),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Jika Gambar Tanda Tangan Sudah Ada dari Backend
                    if (!uiState.signImageString.isNullOrEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = uiState.signImageString,
                                contentDescription = "Signature Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(Color(0xFFF8F8F8))
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.clearSignature() },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ubah", color = Color(0xFF262626))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = { viewModel.submitSignature(mdi, null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Submit", color = Color.White)
                                }
                            }
                        }
                    } else {
                        // Tampilkan Canvas untuk Tanda Tangan Manual
                        SignatureCanvas(
                            onClear = { viewModel.clearSignature() },
                            onSubmit = { bitmap ->
                                viewModel.submitSignature(mdi, bitmap)
                            }
                        )
                    }
                }
            }
        }
    }
}