package com.example.ivopay.app.ui.loan

import android.graphics.Bitmap
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
    viewModel: BorrowerSignContractsViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    LaunchedEffect(noc) {
        viewModel.init(noc)
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
                    AndroidView(
                        factory = {
                            WebView(it).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                            }
                        },
                        update = {
                            it.loadDataWithBaseURL(null, viewModel.htmlText, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    )
                    
                    if (viewModel.showSignBtn) {
                        Button(
                            onClick = { viewModel.showSignPop = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
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

                    if (viewModel.signImage == null) {
                        SignatureCanvas(
                            onClear = { /* path clear in SignatureCanvas */ },
                            onSubmit = { bitmap ->
                                viewModel.signImage = bitmap
                            }
                        )
                    } else {
                        Image(
                            bitmap = viewModel.signImage!!.asImageBitmap(),
                            contentDescription = "Signature",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color(0xFFF8F8F8))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.signImage = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ubah")
                            }
                            Button(
                                onClick = {
                                    viewModel.submitSignature(
                                        bitmap = viewModel.signImage!!,
                                        onSuccess = {
                                            viewModel.showSignPop = false
                                            Toast.makeText(context, "Selesai tanda tangan", Toast.LENGTH_SHORT).show()
                                            onBackClick()
                                        },
                                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                            ) {
                                Text("Tandatangani")
                            }
                        }
                    }
                }
            }
        }
    }
}
