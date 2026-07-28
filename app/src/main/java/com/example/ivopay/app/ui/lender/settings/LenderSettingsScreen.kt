package com.example.ivopay.app.ui.lender.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R

// Warna disesuaikan dengan CSS Vue: background #FE5455
val HeaderRed = Color(0xFFFE5455)
val TextDark = Color(0xFF262626)
val TextGray = Color(0xFF8C8C8C)

@Composable
fun LenderSettingsScreen(
    viewModel: LenderSettingsViewModel,
    onNavigate: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Dialog Konfirmasi Sign Out (Pengganti showConfirmDialog Vant)
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Konfirmasi") },
            text = { Text(text = "Apakah Anda yakin ingin keluar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(text = "Confirm", color = HeaderRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Cancel", color = TextGray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Title "Pengaturan"
        Text(
            text = "Pengaturan",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        )

        // User Info Banner Header (mine-top-bg)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderRed)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Icon (iv_set_avatar)
            Image(
                painter = painterResource(id = R.drawable.iv_set_avatar),
                contentDescription = "Avatar",
                modifier = Modifier.size(66.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info (Nama & Nomor HP)
            Column {
                Text(
                    text = uiState.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.mobile,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }

        // List Item Menu Settings (Daftar Menu Vant Cell)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.menuItems) { item ->
                SettingCellItem(
                    title = item.title,
                    onClick = {
                        if (uiState.isLoggedIn) {
                            onNavigate(item.route)
                        } else {
                            onNavigateToLogin()
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
            }

            // Cell untuk "Sign Out"
            item {
                SettingCellItem(
                    title = "Sign Out",
                    onClick = { showLogoutDialog = true }
                )
                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
            }

            // Version Text Footer
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IVOJI App Version v ${uiState.appVersion}",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

// Sub-komponen Reusable Cell Item (Meniru <van-cell center is-link />)
@Composable
fun SettingCellItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = TextGray
        )
    }
}