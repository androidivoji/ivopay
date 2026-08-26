package com.example.ivopay.app.ui.mine

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    viewModel: AboutUsViewModel,
    onBackClick: () -> Unit,
    onNavigateToCsOnline: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hubungi Kami", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.iv_set_left_arrow),
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Background Card Area (seperti home-card-bg di Vue)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp)
                ) {
                    // Email Row
                    ContactItemRow(
                        iconRes = R.drawable.iv_borrower_ic_record, // Ganti dengan icon mail jika ada
                        title = viewModel.email,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${viewModel.email}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))

                    // Hotline Row
                    ContactItemRow(
                        iconRes = R.drawable.iv_borrower_ic_work, // Ganti dengan icon tel jika ada
                        title = viewModel.hotline,
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${viewModel.hotline}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))

                    // Online Customer Service Row
                    ContactItemRow(
                        iconRes = R.drawable.iv_borrower_ic_details, // Ganti dengan icon service jika ada
                        title = "Layanan pelanggan online",
                        onClick = {
                            onNavigateToCsOnline(viewModel.csLink)
                        }
                    )

                    if (viewModel.showSocialTabs) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))

                        // Instagram Row
                        ContactItemRow(
                            iconRes = R.drawable.iv_tab_invest_nor, // Ganti dengan icon instagram jika ada
                            title = "Instagram",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/ivoji.id/"))
                                context.startActivity(intent)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))

                        // Facebook Row
                        ContactItemRow(
                            iconRes = R.drawable.iv_tab_home_nor, // Ganti dengan icon facebook jika ada
                            title = "Facebook",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/ivoji.id"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                // Style Tips Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn),
                        contentDescription = "Tips",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Untuk mencegah penipuan, hanya informasi layanan pelanggan dalam aplikasi ini yang resmi.",
                        fontSize = 12.sp,
                        color = Color(0xFF8C8C8C),
                        lineHeight = 16.sp
                    )
                }
            }

            // Hidden Version Check Area at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomCenter)
                    .clickable { 
                        // logic check version
                    }
            )
        }
    }
}

@Composable
fun ContactItemRow(
    iconRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color(0xFF262626),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFBFBFBF),
            modifier = Modifier.size(20.dp)
        )
    }
}
