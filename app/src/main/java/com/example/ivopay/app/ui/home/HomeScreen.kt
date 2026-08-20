package com.example.ivopay.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ivopay.R
import com.example.ivopay.app.data.model.LoanProductConfig
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

    val homeConfig = viewModel.homeConfig
    val isUserInfoCompleted = homeConfig?.cme?.uico ?: false
    val isWof = homeConfig?.cme?.wof ?: false
    val isWiue = homeConfig?.cme?.wiue ?: false

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp),
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
            val bannerRes = if (homeConfig?.cme?.ocEui == true && sessionManager.isUserLoggedIn()) {
                R.drawable.iv_hone_default_slider // Seharusnya iv_home_banner_social_security
            } else {
                R.drawable.iv_hone_default_slider
            }
            
            Image(
                painter = painterResource(id = bannerRes),
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clickable { 
                         if (homeConfig?.cme?.ocEui == true) onNavigateToDetail("JMOPage")
                    },
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Tips Alert Box
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBE6)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.iv_hone_tips_ic_horn),
                        contentDescription = null,
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

            Spacer(modifier = Modifier.height(12.dp))

            // 4. ci10 Section
            homeConfig?.ci10?.let { config ->
                if (config.psw == 1 || config.podi != null) {
                    ExtraLoanTip("Pinjaman limit tinggi", R.drawable.iv_borrower_ic_score)
                    ProductCard(
                        title = "Pinjaman limit tinggi",
                        icon = Icons.Default.Star,
                        config = config,
                        isWof = isWof,
                        isWiue = isWiue,
                        onNavigate = onNavigateToDetail,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail) }
                    )
                }
            }

            // 5. inlg Section
            homeConfig?.inlg?.let { config ->
                if (config.psw == 1 || config.podi != null) {
                    ExtraLoanTip("Produk cicilan", R.drawable.iv_borrower_ic_work)
                    ProductCard(
                        title = "Produk cicilan",
                        icon = Icons.Default.DateRange,
                        config = config,
                        isWof = isWof,
                        isWiue = isWiue,
                        onNavigate = onNavigateToDetail,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail) }
                    )
                }
            }

            // 6. fcoa, tnpo, wof_e (Cash Loan Cards)
            CashLoanCard(
                viewModel = viewModel,
                config = homeConfig?.fcoa,
                cashData = viewModel.cashData,
                showAmount = viewModel.showAmount,
                isWof = isWof,
                isWiue = isWiue,
                productType = "fcoa",
                onNavigate = onNavigateToDetail
            )
            
            CashLoanCard(
                viewModel = viewModel,
                config = homeConfig?.tnpo,
                cashData = viewModel.cashData,
                showAmount = viewModel.showAmount,
                isWof = isWof,
                isWiue = isWiue,
                productType = "tnpo",
                onNavigate = onNavigateToDetail
            )

            // wof_e logic matching Vue
            if (homeConfig?.wofE?.psw == 1 || viewModel.currentBill != null) {
                if (viewModel.currentBill != null) {
                    BillCard(
                        bill = viewModel.currentBill!!,
                        config = homeConfig?.wofE?.let { LoanProductConfig(psw = it.psw) },
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

            // 7. No Info Card (Guest Mode)
            if (!isUserInfoCompleted) {
                NormalStatusCard(
                    nodp = homeConfig?.nodp,
                    onApply = { viewModel.onApplyClick(onNavigateToDetail) }
                )
                
                // ci6_fe (Installment Guest)
                homeConfig?.ci6Fe?.let { config ->
                    if (config.psw == 1 || config.podi != null) {
                        ProductCard(
                            title = "Produk Cicilan",
                            icon = Icons.Default.DateRange,
                            config = config,
                            isWof = isWof,
                            isWiue = isWiue,
                            onNavigate = onNavigateToDetail,
                            onApply = { viewModel.onApplyClick(onNavigateToDetail) }
                        )
                    }
                }
            }

            // 8. Revolving Loan (c9)
            homeConfig?.c9?.let { config ->
                if (config.psw == 1 || config.podi != null) {
                    ProductCard(
                        title = "Produk pinjaman tunai",
                        icon = Icons.Default.Star,
                        config = config,
                        isWof = isWof,
                        isWiue = isWiue,
                        onNavigate = onNavigateToDetail,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail) },
                        targetRoute = "RevolvingLoan"
                    )
                }
            }

            // 9. Extra Installments (ci6, ci6_w, ci7, ci8)
            listOfNotNull(homeConfig?.ci6, homeConfig?.ci6W, homeConfig?.ci7, homeConfig?.ci8).forEach { config ->
                if (config.psw == 1 || config.podi != null) {
                    ExtraLoanTip("Produk cicilan", R.drawable.iv_borrower_ic_work)
                    ProductCard(
                        title = "Produk cicilan",
                        icon = Icons.Default.DateRange,
                        config = config,
                        isWof = isWof,
                        isWiue = isWiue,
                        onNavigate = onNavigateToDetail,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail) }
                    )
                }
            }

            // 10. Extra Loan 15/16 (ciub, rta2)
            if (homeConfig?.ciub?.psw == 1 || homeConfig?.rta2?.psw == 1) {
                ExtraLoanTip("Lebih banyak produk", R.drawable.iv_invest_logo, "Anda dapat mengajukan permohonan produk lain jika membayar tepat waktu.")
                
                homeConfig.rta2?.let { if (it.psw == 1 || it.podi != null) {
                    ProductCard(title = "rta2", icon = Icons.Default.Info, config = it, isWof = isWof, isWiue = isWiue, onNavigate = onNavigateToDetail, targetRoute = "CLoan16")
                }}
                homeConfig.ciub?.let { if (it.psw == 1 || it.podi != null) {
                    ProductCard(title = "ciub", icon = Icons.Default.Info, config = it, isWof = isWof, isWiue = isWiue, onNavigate = onNavigateToDetail, targetRoute = "CLoan15")
                }}
            }

            Spacer(modifier = Modifier.height(16.dp))
            RecommendProductCard(onNavigate = onNavigateToDetail)
            HomeBotInfo()
        }

        // Blurry Photo Tip (Fixed at bottom)
        if (viewModel.homeConfig?.cme?.nmin?.idfie == true) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 65.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFFFF0ED))
                    .clickable { onNavigateToDetail("BaseInfo") }
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "*Karena foto KTP Anda buram, informasi identitas tidak dapat diperiksa, harap diunggah ulang.",
                        fontSize = 11.sp,
                        color = Color(0xFF262626),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(painterResource(id = R.drawable.iv_set_right_arrow), contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFE5455))
                }
            }
        }
    }

    // Modal: Pemeriksaan Lulus (showConfirmBillPop)
    if (viewModel.showConfirmBillPop) {
        Dialog(onDismissRequest = { viewModel.showConfirmBillPop = false }) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.iv_logo_ivoji_splash), contentDescription = null, modifier = Modifier.fillMaxWidth())
                    Text("Pemeriksaan lulus", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 10.dp))
                    Text(
                        text = "Halo, pinjaman Anda sudah disetujui. Harap segera konfirmasi tagihan, jika lewat jatuh tempo akan batal.",
                        fontSize = 13.sp, 
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Button(
                        onClick = { 
                            viewModel.showConfirmBillPop = false
                            onNavigateToDetail("BillDetails?bill=${viewModel.currentBill?.noc}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text("Konfirmasi Dana")
                    }
                }
            }
        }
    }
}

@Composable
fun ExtraLoanTip(title: String, icon: Int, subtitle: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(id = icon), contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.Unspecified)
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
        }
        if (subtitle != null) {
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
