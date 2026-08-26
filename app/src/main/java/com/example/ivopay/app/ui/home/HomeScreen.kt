package com.example.ivopay.app.ui.home

import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.example.ivopay.app.ui.loan.ProductItemCard
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

    val dummyConfig = LoanProductConfig(
        psw = 1,              // Wajib 1 agar kartu muncul
        atma = 5000000,       // Limit Maksimal (misal 5jt)
        bpio = 12,            // Tenor/Periode (misal 12 bulan)
        koc = false           // Tombol tidak terkunci
    )

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

            // 2. Banner (Swipeable if otherProducts exists)
            val defaultBanner = if (homeConfig?.cme?.ocEui == true && sessionManager.isUserLoggedIn()) {
                R.drawable.iv_home_banner_social_security
            } else {
                R.drawable.iv_hone_default_slider
            }
            
            val banners = remember(viewModel.otherProducts, defaultBanner) {
                mutableListOf(defaultBanner).apply {
                    if (viewModel.otherProducts.isNotEmpty()) {
                        add(R.drawable.iv_other_banner)
                    }
                }
            }

            val pagerState = rememberPagerState(pageCount = { banners.size })

            // Autoplay logic (3 seconds)
            LaunchedEffect(banners.size) {
                if (banners.size > 1) {
                    while (true) {
                        delay(3000)
                        val nextPage = (pagerState.currentPage + 1) % banners.size
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(160.dp)
            ) { page ->
                val currentBanner = banners[page]
                Image(
                    painter = painterResource(id = currentBanner),
                    contentDescription = "Banner $page",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { 
                            if (currentBanner == R.drawable.iv_other_banner) {
                                // Specific action for other product banner if needed
                                onNavigateToDetail("OtherProductPage")
                            } else if (homeConfig?.cme?.ocEui == true) {
                                onNavigateToDetail("JMOPage")
                            }
                        },
                    contentScale = ContentScale.FillBounds
                )
            }

            // Pager Indicator (Dots)
            if (banners.size > 1) {
                Row(
                    Modifier
                        .height(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(banners.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color(0xFFFE5455) else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .background(color, RoundedCornerShape(8.dp))
                                .size(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Tips Alert Box
            Log.d("XBZ", "Tampilan Tips Alert Box: Muncul")
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
                    Log.d("XBZ", "Tampilan Kartu: Pinjaman limit tinggi (ci10)")
                    ExtraLoanTip("Pinjaman limit tinggi", R.drawable.iv_borrower_ic_score)
                    Ci10LoanCard(
                        comData = config,
                        curBill = config.podi,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail, "ci10") },
                        onNavigate = onNavigateToDetail
                    )
                }
            }

            // 5. inlg Section
            homeConfig?.inlg?.let { config ->
                if (config.psw == 1 || config.podi != null) {
                    Log.d("XBZ", "Tampilan Kartu: Produk cicilan (inlg)")
                    ExtraLoanTip("Produk cicilan", R.drawable.iv_borrower_ic_record)
                    InlgLoanCard(
                        comData = config,
                        curBill = config.podi,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail, "inlg") },
                        onNavigate = onNavigateToDetail
                    )
                }
            }

            // 6. fcoa, tnpo, wof_e (Cash Loan Cards)
            if (homeConfig?.fcoa?.psw == 1 || homeConfig?.fcoa?.podi != null) {
                Log.d("XBZ", "Tampilan Kartu: CashLoanCard fcoa")
                CashLoanCard(
                    viewModel = viewModel,
                    config = homeConfig.fcoa,
                    cashData = viewModel.cashData,
                    showAmount = viewModel.showAmount,
                    isWof = isWof,
                    isWiue = isWiue,
                    productType = "fcoa",
                    onNavigate = onNavigateToDetail
                )
            }
            
            if (homeConfig?.tnpo?.psw == 1 || homeConfig?.tnpo?.podi != null) {
                CashLoanCard(
                    viewModel = viewModel,
                    config = homeConfig.tnpo,
                    cashData = viewModel.cashData,
                    showAmount = viewModel.showAmount,
                    isWof = isWof,
                    isWiue = isWiue,
                    productType = "tnpo",
                    onNavigate = onNavigateToDetail
                )
            }

            // wof_e logic matching Vue
            if (homeConfig?.wofE?.psw == 1 || viewModel.currentBill != null) {
                Log.d("XBZ", "Tampilan Kartu: CashLoanCard wof_e")
                CashLoanCard(
                    viewModel = viewModel,
                    config = homeConfig?.wofE?.let { LoanProductConfig(psw = it.psw) },
                    curBillOverride = viewModel.currentBill,
                    cashData = viewModel.cashData,
                    showAmount = viewModel.showAmount,
                    isWof = isWof,
                    isWiue = isWiue,
                    productType = "wof_e",
                    onNavigate = onNavigateToDetail
                )
            }

            // ci6_e Section
            homeConfig?.ci6E?.let { config ->
                if (config.psw == 1 || viewModel.ci6EBill != null) {
                    Log.d("XBZ", "Tampilan Kartu: CI6ELoanCard")
                    CI6ELoanCard(
                        comData = config,
                        curBill = viewModel.ci6EBill,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail, "ci6_e") },
                        onNavigate = onNavigateToDetail
                    )
                }
            }

            // 7. No Info Card (Guest Mode)
            if (!isUserInfoCompleted) {
                Log.d("XBZ", "Tampilan Kartu: NormalStatusCard (Guest Mode)")
                NormalStatusCard(
                    nodp = homeConfig?.nodp,
                    onApply = { viewModel.onApplyClick(onNavigateToDetail) }
                )
                
                // ci6_fe (Installment Guest)
                homeConfig?.ci6Fe?.let { config ->
                    if (config.psw == 1 || config.podi != null) {
                        InlgLoanCard(
                            comData = config,
                            curBill = config.podi,
                            onApply = { viewModel.onApplyClick(onNavigateToDetail, "ci6_fe") },
                            onNavigate = onNavigateToDetail
                        )
                    }
                }
            }

            // 8. Revolving Loan (c9)
            homeConfig?.c9?.let { config ->
                if (config.psw == 1 || config.podi != null) {
                    Log.d("XBZ", "Tampilan Kartu: RevolvingLoanCard")
                    RevolvingLoanCard(
                        comData = config,
                        curBill = config.podi,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail, "c9") },
                        onNavigate = onNavigateToDetail
                    )
                }
            }

            // 9. Extra Installments (ci6, ci6_w, ci7, ci8)
            listOfNotNull(
                homeConfig?.ci6?.let { it to "ci6" }, 
                homeConfig?.ci6W?.let { it to "ci6_w" }, 
                homeConfig?.ci7?.let { it to "ci7" }, 
                homeConfig?.ci8?.let { it to "ci8" }
            ).forEach { (config, tag) ->
                if (config.psw == 1 || config.podi != null) {
                    Log.d("XBZ", "Tampilan Kartu: InlgLoanCard")
                    ExtraLoanTip("Produk cicilan", R.drawable.iv_borrower_ic_record)
                    InlgLoanCard(
                        comData = config,
                        curBill = config.podi,
                        onApply = { viewModel.onApplyClick(onNavigateToDetail, tag) },
                        onNavigate = onNavigateToDetail
                    )
                }
            }

            // 10. Extra Loan 15/16 (ciub, rta2)
            if (homeConfig?.ciub?.psw == 1 || homeConfig?.rta2?.psw == 1) {
                Log.d("XBZ", "Tampilan Kartu: ExtraLoanCard")
                ExtraLoanTip("Lebih banyak produk", R.drawable.iv_invest_logo, "Anda dapat mengajukan permohonan produk lain jika membayar tepat waktu.")
                
                homeConfig.rta2?.let { if (it.psw == 1 || it.podi != null) {
                    ExtraLoanCard(comData = it, curBill = it.podi, onApply = { viewModel.onApplyClick(onNavigateToDetail, "rta2") }, onNavigate = onNavigateToDetail)
                }}
                homeConfig.ciub?.let { if (it.psw == 1 || it.podi != null) {
                    ExtraLoanCard(comData = it, curBill = it.podi, onApply = { viewModel.onApplyClick(onNavigateToDetail, "ciub") }, onNavigate = onNavigateToDetail)
                }}
            }

            Spacer(modifier = Modifier.height(16.dp))
            Log.d("XBZ", "Tampilan Kartu: RecommendProductCard")
            RecommendProductCard(onNavigate = onNavigateToDetail)

            // Menambahkan Daftar Produk Lain di bawah RecommendProductCard
            if (viewModel.otherProducts.isNotEmpty()) {
                Log.d("XBZ", "Tampilan List: ${viewModel.otherProducts.size} Produk Lain")
                viewModel.otherProducts.forEach { product ->
                    ProductItemCard(
                        item = product,
                        onApply = {
                            viewModel.onApplyClick(onNavigateToDetail)
                        }
                    )
                }
            }

            if (viewModel.showEcurEntry) {
                Log.d("XBZ", "Tampilan Kartu: RecommendProductCard")
                RecommendProductCard(onNavigate = onNavigateToDetail)
            }

            HomeBotInfo()
        }

        // Blurry Photo Tip (Fixed at bottom)
        val nmin = viewModel.homeConfig?.cme?.nmin
        val hasPhotoError = nmin?.idfie == true || nmin?.idbie == true || nmin?.idhie == true || nmin?.wkptie == true
        
        if (hasPhotoError) {
            Log.d("XBZ", "Tampilan Tip: Foto KTP Buram")
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
        Log.d("XBZ", "Tampilan Modal: Pemeriksaan Lulus")
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
