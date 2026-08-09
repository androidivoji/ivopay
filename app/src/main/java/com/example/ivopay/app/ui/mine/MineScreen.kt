package com.example.ivopay.app.ui.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R

// Model untuk item menu
data class MenuItem(
    val title: String,
    val routeName: String,
    val requiresLogin: Boolean = true
)

@Composable
fun MineScreen(
    isLoggedIn: Boolean,
    userName: String?,
    userPhone: String?,
    appVersion: String = "1.0.0",
    isUnderReview: Boolean = false,
    hasContract: Boolean = false,
    onNavigate: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var showReviewDialog by remember { mutableStateOf(false) }

    // Membangun daftar menu secara dinamis sesuai status login
    val menuItems = remember(isLoggedIn, hasContract) {
        mutableListOf(
            MenuItem("Riwayat Pengajuan", "MyBill"),
            MenuItem("Profil Saya", "MyProfile")
        ).apply {
            if (hasContract) {
                add(1, MenuItem("Kontrak saya", "BorrowerSignContracts"))
            }
            add(MenuItem("Hubungi Kami", "AboutUs", requiresLogin = false))
            add(MenuItem("Kebijakan Privasi", "PrivacyPolicy", requiresLogin = false))
            add(MenuItem("Syarat & Ketentuan", "UseAgreement", requiresLogin = false))

            if (isLoggedIn) {
                add(MenuItem("Atur Pola Kunci", "GestureCreate"))
                add(MenuItem("Akun pihak ketiga terikat", "ThirdAccountBind"))
                add(MenuItem("Ubah nomor ponsel Anda", "ChangeBindPhone"))
                add(MenuItem("Berhenti / Keluar", "LogoutAndExitPage"))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Title Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = "Pengaturan",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        // 2. Profile Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFE5455))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.iv_set_avatar),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isLoggedIn) (userName?.ifEmpty { null } ?: "Pengguna") else "Selamat Datang",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Tampilkan nomor phone jika user login dan userPhone tidak kosong
                if (isLoggedIn && !userPhone.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userPhone,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 3. Dynamic Menu List
        menuItems.forEach { item ->
            MenuItemRow(
                title = item.title,
                onClick = {
                    if (!isLoggedIn && item.requiresLogin) {
                        onNavigateToLogin()
                    } else if (isUnderReview && (item.routeName == "ChangeBindPhone" || item.routeName == "MyProfile")) {
                        showReviewDialog = true
                    } else {
                        onNavigate(item.routeName)
                    }
                }
            )
            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. App Version Footer
        Text(
            text = "IVOJI App Version v$appVersion",
            fontSize = 12.sp,
            color = Color(0xFF8C8C8C)
        )
    }

    // Modal Informasi Peninjauan Identitas
    if (showReviewDialog) {
        Dialog(onDismissRequest = { showReviewDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Verifikasi identitas sedang ditinjau. Peninjauan akan selesai dalam 10 menit. Silakan ajukan pinjaman setelah peninjauan selesai.",
                        fontSize = 14.sp,
                        color = Color(0xFF262626)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showReviewDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text(text = "OK", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF262626)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Arrow Right",
            tint = Color(0xFFBFBFBF)
        )
    }
}

// Extension utilitas sederhana
private fun String?.isNull_or_Empty(): Boolean = this == null || this.isEmpty()