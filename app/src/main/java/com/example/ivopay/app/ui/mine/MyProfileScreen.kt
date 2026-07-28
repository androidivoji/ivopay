package com.example.ivopay.app.ui.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

// Data model item menu
data class ProfileSectionItem(
    val txt: String,
    val name: String,
    val isFinished: Boolean
)

// Data class simulasi status dari API (stag / stag_lackin)
data class CustomerStag(
    val s1: Boolean = false,   // BaseInfo
    val s3: Boolean = false,   // PersonalInfoV2
    val s4: Boolean = false,   // ContactInfoPage
    val s5: Boolean = false,   // JobInfoV2
    val s2_a: Boolean = false, // ContactInfoV2 (Lackin A)
    val s3_a: Boolean = false  // BankInfo (Lackin A)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onBackClick: () -> Unit,
    onNavigateToStep: (routeName: String, isFinished: Boolean) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoggedIn: Boolean = true,
    isLackinA: Boolean = false,
    isLackinFlow: Boolean? = true,
    stag: CustomerStag = CustomerStag() // Bisa disunting lewat ViewModel/State
) {
    // Menyusun list menu berdasarkan status flow (lackin_A / standard)
    val infoList = remember(isLackinA, isLackinFlow, stag) {
        mutableListOf<ProfileSectionItem>().apply {
            add(ProfileSectionItem("01 Informasi Dasar", "BaseInfo", stag.s1))

            if (isLackinFlow != null) {
                if (isLackinA) {
                    add(ProfileSectionItem("02 Informasi Kontak", "ContactInfoV2", stag.s2_a))
                    add(ProfileSectionItem("03 Rekening Kartu Bank", "BankInfo", stag.s3_a))
                } else {
                    add(ProfileSectionItem("02 Informasi Pribadi", "PersonalInfoV2", stag.s3))
                    add(ProfileSectionItem("03 Informasi Kontak", "ContactInfoPage", stag.s4))
                    add(ProfileSectionItem("04 Informasi Pekerjaan", "JobInfoV2", stag.s5))
                }
            }
        }
    }

    // Handil logika klik sesuai kriteria urutan pengisian Vue (itemClick)
    fun handleItemClick(item: ProfileSectionItem) {
        if (!isLoggedIn) {
            onNavigateToLogin()
            return
        }

        // 1. Jika item yang diklik sudah selesai, langsung buka detailnya
        if (item.isFinished) {
            onNavigateToStep(item.name, true)
            return
        }

        // 2. Jika belum selesai, arahkan ke step pertama yang belum diisi (Guided Flow)
        if (!stag.s1) {
            onNavigateToStep("BaseInfo", false)
        } else if (isLackinFlow != null && !isLackinA) {
            when {
                !stag.s3 -> onNavigateToStep("PersonalInfoV2", false)
                !stag.s4 -> onNavigateToStep("ContactInfoPage", false)
                !stag.s5 -> onNavigateToStep("JobInfoV2", false)
                else -> onNavigateToStep(item.name, false)
            }
        } else if (isLackinFlow != null && isLackinA) {
            when {
                !stag.s2_a -> onNavigateToStep("ContactInfoV2", false)
                !stag.s3_a -> onNavigateToStep("BankInfo", false)
                else -> onNavigateToStep(item.name, false)
            }
        } else {
            onNavigateToStep("BaseInfo", false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F7F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Container List Items
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn {
                    itemsIndexed(infoList) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable { handleItemClick(item) }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Status Icon (Selesai / Belum Selesai)
                                val iconRes = if (item.isFinished) {
                                    R.drawable.iv_data_ic_finish
                                } else {
                                    R.drawable.iv_data_ic_undone
                                }

                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .padding(end = 10.dp)
                                )

                                Text(
                                    text = item.txt,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF262626)
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF8C8C8C)
                            )
                        }

                        if (index < infoList.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = Color(0xFFF0F0F0)
                            )
                        }
                    }
                }
            }

            // Kind Tip Footer
            Text(
                text = "Semakin lengkap informasinya, semakin tinggi peluang pinjaman disetujui",
                color = Color(0xFF8C8C8C),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}