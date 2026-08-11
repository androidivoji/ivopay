package com.example.ivopay.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivopay.R
import com.example.ivopay.app.ui.home.components.*
import com.example.ivopay.app.util.SessionManager

@Composable
fun HomeScreen(
    viewModel: BorrowerHomeViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header Title
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = "IVOCASH Borrower",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Banner
        Image(
            painter = painterResource(id = R.drawable.iv_hone_default_slider),
            contentDescription = "Banner",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Tips Alert Box
        Card(
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBE6)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn),
                    contentDescription = "Notice",
                    tint = Color(0xFFFA8C16),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pemohonan dan pembayaran diselesaikan dalam APP resmi, tautan eksternal dan transfer pribadi adalah penipuan",
                    fontSize = 11.sp,
                    color = Color(0xFF8C6B00)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Dynamic Card List
        val homeConfig = viewModel.homeConfig
        val isUserInfoCompleted = homeConfig?.cme?.uico ?: false
        val isWof = homeConfig?.cme?.wof ?: false
        val isWiue = homeConfig?.cme?.wiue ?: false

        // ci10
        val ci10 = homeConfig?.ci10
        if (ci10?.psw == 1 || ci10?.podi != null) {
            ProductCard(
                title = "Pinjaman limit tinggi",
                icon = Icons.Default.Star,
                config = ci10,
                isWof = isWof,
                isWiue = isWiue,
                onNavigate = onNavigateToDetail
            )
        }

        // inlg
        val inlg = homeConfig?.inlg
        if (inlg?.psw == 1 || inlg?.podi != null) {
            ProductCard(
                title = "Produk cicilan",
                icon = Icons.Default.DateRange,
                config = inlg,
                isWof = isWof,
                isWiue = isWiue,
                onNavigate = onNavigateToDetail
            )
        }

        // fcoa
        val fcoa = homeConfig?.fcoa
        if (fcoa?.psw == 1 || fcoa?.podi != null) {
            CashLoanCard(
                config = fcoa,
                cashData = viewModel.cashData,
                showAmount = viewModel.showAmount,
                isWof = isWof,
                isWiue = isWiue,
                productType = "fcoa",
                onNavigate = onNavigateToDetail
            )
        }

        // tnpo
        val tnpo = homeConfig?.tnpo
        if (tnpo?.psw == 1 || tnpo?.podi != null) {
            CashLoanCard(
                config = tnpo,
                cashData = viewModel.cashData,
                showAmount = viewModel.showAmount,
                isWof = isWof,
                isWiue = isWiue,
                productType = "tnpo",
                onNavigate = onNavigateToDetail
            )
        }

        // wof_e
        val wofE = homeConfig?.wofE
        if (wofE?.psw == 1 || viewModel.currentBill?.yep == "wof_e") {
             if (viewModel.currentBill?.yep == "wof_e") {
                 BillCard(
                     bill = viewModel.currentBill!!, 
                     config = homeConfig?.ci6E, // Mapping wof_e to a config if exists
                     hasPgsh = sessionManager.getHasPgsh(), 
                     isWof = isWof,
                     isWiue = isWiue,
                     productType = "wof_e",
                     onNavigate = onNavigateToDetail
                 )
             } else {
                 ApplicationCard(viewModel = viewModel, onNavigate = onNavigateToDetail)
             }
        }

        // Guest / Normal Status Card
        if (!isUserInfoCompleted) {
            NormalStatusCard(
                nodp = homeConfig?.nodp,
                onApply = { onNavigateToDetail("ApplyLoan") }
            )
            
            val ci6Fe = homeConfig?.ci6Fe
            if (ci6Fe?.psw == 1 || ci6Fe?.podi != null) {
                ProductCard(
                    title = "Produk Cicilan",
                    icon = Icons.Default.DateRange,
                    config = ci6Fe,
                    isWof = isWof,
                    isWiue = isWiue,
                    onNavigate = onNavigateToDetail
                )
            }
        }

        // revolving loan (c9)
        val c9 = homeConfig?.c9
        if (c9?.psw == 1 || c9?.podi != null) {
            ProductCard(
                title = "Produk pinjaman tunai",
                icon = Icons.Default.Star,
                config = c9,
                isWof = isWof,
                isWiue = isWiue,
                onNavigate = onNavigateToDetail,
                durationUnit = "hari",
                targetRoute = "RevolvingLoan",
                overrideDuration = 14
            )
        }

        // Additional Installment Products
        listOfNotNull(
            homeConfig?.ci6,
            homeConfig?.ci6W,
            homeConfig?.ci7,
            homeConfig?.ci8
        ).forEach { config ->
            if (config.psw == 1 || config.podi != null) {
                ProductCard(
                    title = "Produk cicilan",
                    icon = Icons.Default.DateRange,
                    config = config,
                    isWof = isWof,
                    isWiue = isWiue,
                    onNavigate = onNavigateToDetail
                )
            }
        }

        // Extra Loan Products
        listOfNotNull(
            homeConfig?.rta2 to "rta2",
            homeConfig?.ciub to "ciub"
        ).forEach { (config, type) ->
            if (config?.psw == 1 || config?.podi != null) {
                ProductCard(
                    title = "Lebih banyak produk",
                    icon = Icons.Default.Info,
                    config = config,
                    isWof = isWof,
                    isWiue = isWiue,
                    onNavigate = onNavigateToDetail,
                    subtitle = "Anda dapat mengajukan permohonan produk lain jika membayar tepat waktu.",
                    durationUnit = "hari",
                    buttonText = "Permohonan",
                    targetRoute = if (type == "rta2") "CLoan16" else "CLoan15"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recommend Product Card
        RecommendProductCard(onNavigate = onNavigateToDetail)

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(text = "Layanan Konsumen: 021-39506655", fontSize = 12.sp, color = Color.Gray)
            Text(text = "Email: cs@ivoji.id", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Copyright © 2026 IVOJI. All rights reserved.", fontSize = 10.sp, color = Color.LightGray)
        }
    }
}
