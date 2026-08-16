package com.example.ivopay.app.ui.mine

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ivopay.app.data.api.NetworkClient
import com.google.gson.JsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutAndExitScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAccountLogout: () -> Unit = {},
    onLogoutConfirmed: () -> Unit = {}
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var userKtpName by remember { mutableStateOf<String?>(null) }

    // Fetch user info untuk mengecek pi.inm (KTP)
    LaunchedEffect(Unit) {
        try {
            val response = NetworkClient.apiService.getUserInfo(JsonObject().apply { addProperty("spe", "h") })
            if (response.isSuccessful) {
                userKtpName = response.body()?.data?.customer?.personalInfo?.ktpMasked
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Berhenti/Keluar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Opsi "Keluar" (Logout)
            LogoutSettingItem(
                title = "Keluar",
                iconResId = R.drawable.iv_set_ic_quit, // Sesuaikan dengan drawable icon quit kamu
                onClick = {
                    showLogoutDialog = true
                }
            )

            // 2. Opsi "Hapus akun" (Account Logout / Delete)
            LogoutSettingItem(
                title = "Hapus akun",
                iconResId = R.drawable.iv_set_ic_logout,
                onClick = {
                    // Logika pengecekan userInfo.customer.pi.inm
                    if (!userKtpName.isNullOrEmpty()) {
                        onNavigateToAccountLogout()
                    } else {
                        Toast.makeText(
                            context,
                            "KTP belum diisi dan rekening tidak dapat dibatalkan saat ini.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    // Dialog Konfirmasi Keluar (Pengganti showConfirmDialog Vant)
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Konfirmasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin keluar?",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    }
                ) {
                    Text("Konfirmasi", color = Color(0xFFFE5455), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun LogoutSettingItem(
    title: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Kiri
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Panah Kanan (is-link)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF999999)
            )
        }
    }
}