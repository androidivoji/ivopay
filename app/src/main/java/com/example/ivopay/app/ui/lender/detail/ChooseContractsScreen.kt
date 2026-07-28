package com.example.ivopay.app.ui.lender.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ivopay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseContractsScreen(
    mdi: String?,
    cno: String?,
    onBackClick: () -> Unit,
    onNavigateToViewContractsPage: (mdi: String, type: Int) -> Unit,
    onNavigateToViewContractsPage2: (cno: String, type: Int) -> Unit,
    viewModel: ChooseContractsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mdi, cno) {
        viewModel.initData(mdi, cno)
    }

    // Menggunakan logika 'if ... else if' sesuai Pilihan B
    val handleJumpContracts: (type: Int) -> Unit = { type ->
        if (uiState.mdi.isNotEmpty()) {
            onNavigateToViewContractsPage(uiState.mdi, type)
        } else if (uiState.cno.isNotEmpty()) {
            onNavigateToViewContractsPage2(uiState.cno, type)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check contract", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            ) {
                // Item Cell 1: Contract lender and borrower
                ContractCellItem(
                    title = "Contract lender and borrower",
                    onClick = { handleJumpContracts(1) }
                )

                HorizontalDivider(
                    color = Color(0xFFF5F5F5),
                    thickness = 1.dp
                )

                // Item Cell 2: Lender contracts and platforms
                ContractCellItem(
                    title = "Lender contracts and platforms",
                    onClick = { handleJumpContracts(2) }
                )
            }
        }
    }
}

@Composable
private fun ContractCellItem(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF262626)
            )
            Icon(
                painter = painterResource(id = R.drawable.iv_set_right_arrow),
                contentDescription = "Chevron Right",
                tint = Color(0xFFC8C7CC),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}