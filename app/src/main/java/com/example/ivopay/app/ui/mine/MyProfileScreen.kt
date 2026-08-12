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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    viewModel: MyProfileViewModel,
    onBackClick: () -> Unit,
    onNavigateToStep: (routeName: String, isFinished: Boolean) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isLoggedIn = viewModel.isLoggedIn
    val isLackinA = viewModel.isLackinA
    val isLackinFlow = viewModel.isLackinFlow
    val stag = viewModel.stag ?: com.example.ivopay.app.data.model.StagLackin()
    val infoList = viewModel.infoList

    // Handil logika klik sesuai kriteria urutan pengisian Vue (itemClick)
    fun handleItemClick(item: ProfileMenuOption) {
        if (!isLoggedIn) {
            onNavigateToLogin()
            return
        }

        val isInfoCompleted = viewModel.isUico
        val finishedParam = if (isInfoCompleted) "1" else ""

        // 1. Jika item yang diklik sudah selesai, langsung buka detailnya
        if (item.isFinished) {
            onNavigateToStep("${item.name}?infoFinished=$finishedParam", true)
            return
        }

        // 2. Jika belum selesai, arahkan ke step pertama yang belum diisi (Guided Flow)
        if (!stag.s1) {
            onNavigateToStep("BaseInfo?infoFinished=$finishedParam", false)
        } else if (isLackinFlow != null && !isLackinA) {
            when {
                !stag.s3 -> onNavigateToStep("PersonalInfoV2?infoFinished=$finishedParam", false)
                !stag.s4 -> onNavigateToStep("ContactInfoPage?infoFinished=$finishedParam", false)
                !stag.s5 -> onNavigateToStep("JobInfoV2?infoFinished=$finishedParam", false)
                else -> onNavigateToStep("${item.name}?infoFinished=$finishedParam", false)
            }
        } else if (isLackinFlow != null && isLackinA) {
            when {
                !stag.s2_a -> onNavigateToStep("ContactInfoV2?infoFinished=$finishedParam", false)
                !stag.s3_a -> onNavigateToStep("BankInfo?infoFinished=$finishedParam", false)
                else -> onNavigateToStep("${item.name}?infoFinished=$finishedParam", false)
            }
        } else {
            onNavigateToStep("BaseInfo?infoFinished=$finishedParam", false)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Profile", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F7F9)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
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
            
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFE5455))
            }
        }
    }
}
