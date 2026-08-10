package com.example.ivopay.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowRight
import com.example.ivopay.R
import com.example.ivopay.app.data.model.LoanOrder
import com.example.ivopay.app.util.CommonUtils
import com.example.ivopay.app.util.LoanStatusMapper
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

        // 4. Dynamic Card List (Replaces the single if-else block)
        val homeConfig = viewModel.homeConfig
        val isUserInfoCompleted = homeConfig?.cme?.uico ?: false

        // Card rendering order based on Vue TabHome
        
        // ci10
        val ci10 = homeConfig?.ci10
        if (ci10?.psw == 1 || ci10?.podi != null) {
            ProductCard(
                title = "Pinjaman limit tinggi",
                icon = Icons.Default.Star,
                config = ci10,
                isWof = homeConfig?.cme?.wof ?: false,
                isWiue = homeConfig?.cme?.wiue ?: false,
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
                isWof = homeConfig?.cme?.wof ?: false,
                isWiue = homeConfig?.cme?.wiue ?: false,
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
                isWof = homeConfig?.cme?.wof ?: false,
                isWiue = homeConfig?.cme?.wiue ?: false,
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
                isWof = homeConfig?.cme?.wof ?: false,
                isWiue = homeConfig?.cme?.wiue ?: false,
                onNavigate = onNavigateToDetail
            )
        }

        // wof_e
        val wofE = homeConfig?.wofE
        if (wofE?.psw == 1 || viewModel.currentBill?.yep == "wof_e") {
             // Handling wof_e using generic bill/apply logic
             if (viewModel.currentBill?.yep == "wof_e") {
                 BillCard(
                     bill = viewModel.currentBill!!, 
                     hasPgsh = sessionManager.getHasPgsh(), 
                     isWof = homeConfig?.cme?.wof ?: false,
                     isWiue = homeConfig?.cme?.wiue ?: false,
                     onNavigate = onNavigateToDetail
                 )
             } else {
                 ApplicationCard(viewModel = viewModel, onNavigate = onNavigateToDetail)
             }
        }

        // Guest / Normal Status Card (Only show if NOT uico)
        if (!isUserInfoCompleted) {
            NormalStatusCard(
                nodp = homeConfig?.nodp,
                onApply = { onNavigateToDetail("ApplyLoan") }
            )
            
                // ci6_fe
            val ci6Fe = homeConfig?.ci6Fe
            if (ci6Fe?.psw == 1 || ci6Fe?.podi != null) {
                ProductCard(
                    title = "Produk Cicilan",
                    icon = Icons.Default.DateRange,
                    config = ci6Fe,
                    isWof = homeConfig?.cme?.wof ?: false,
                    isWiue = homeConfig?.cme?.wiue ?: false,
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
                isWof = homeConfig?.cme?.wof ?: false,
                isWiue = homeConfig?.cme?.wiue ?: false,
                onNavigate = onNavigateToDetail,
                durationUnit = "hari",
                targetRoute = "RevolvingLoan",
                overrideDuration = 14
            )
        }

        // Additional Installment Products
        listOfNotNull(
            homeConfig?.ci6 to "ci6",
            homeConfig?.ci6W to "ci6_w",
            homeConfig?.ci7 to "ci7",
            homeConfig?.ci8 to "ci8"
        ).forEach { (config, type) ->
            if (config?.psw == 1 || config?.podi != null) {
                ProductCard(
                    title = "Produk cicilan",
                    icon = Icons.Default.DateRange,
                    config = config,
                    isWof = homeConfig?.cme?.wof ?: false,
                    isWiue = homeConfig?.cme?.wiue ?: false,
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
                    isWof = homeConfig?.cme?.wof ?: false,
                    isWiue = homeConfig?.cme?.wiue ?: false,
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

        // Bottom Info / Footer Placeholder
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(text = "Layanan Konsumen: 021-39506655", fontSize = 12.sp, color = Color.Gray)
            Text(text = "Email: cs@ivoji.id", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Copyright © 2026 IVOJI. All rights reserved.", fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun RecommendProductCard(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Rekomendasi produk pinjaman lain yang sesuai untuk Anda",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF0EB), Color.White),
                            startY = 0f,
                            endY = 300f // Approximate 40% height
                        )
                    )
                    .padding(16.dp)
            ) {
                // Gold Image at Top Right
                Image(
                    painter = painterResource(id = R.drawable.iv_home_img_gold),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopEnd)
                )

                Column {
                    Text(text = "Disetujui dalam 30 menit", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.4f))
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    Text(text = "Limit Maks (Rp)", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.4f))
                    
                    Text(
                        text = CommonUtils.formatRupiah(10000000.0),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Apply Button at Bottom Right
                Button(
                    onClick = { onNavigate("OtherProductPage") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Ajukan", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    config: com.example.ivopay.app.data.model.LoanProductConfig?,
    isWof: Boolean,
    isWiue: Boolean,
    onNavigate: (String) -> Unit,
    subtitle: String? = null,
    durationUnit: String = "bulan",
    buttonText: String = "Ajukan pinjaman",
    targetRoute: String = "ApplyLoan",
    overrideDuration: Int? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val hasPgsh = sessionManager.getHasPgsh()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // 1. Header (Normal)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color(0xFFFE5455))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        if (subtitle != null) {
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Card Content
        val bill = config?.podi
        if (bill != null) {
            InstallmentBillCard(
                bill = bill, 
                hasPgsh = hasPgsh, 
                isWof = isWof, 
                onNavigate = onNavigate,
                isExtra = durationUnit == "hari",
                isWiue = isWiue
            )
        } else if (config?.psw == 1) {
            // Application Card for Installment/Extra
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Special Header if resvAtma > 0
                    if (config.resvAtma > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF7E6)) // Simulated gold/yellow bg
                                .padding(8.dp)
                                .clickable { onNavigate(targetRoute) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color(0xFFFAAD14))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Anda masih bisa mengajukan satu pinjaman lagi dengan limit", fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Jumlah:", fontSize = 10.sp, color = Color.Gray)
                                Text(text = CommonUtils.formatRupiah(config.resvAtma.toDouble()), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        if (config.kocByNoResvAtma) {
                            Text(text = "Limit anda sudah habis", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Segera lakukan pembayaran untuk meningkatkan limit dan dapat mengajukan pinjaman lagi.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    val amount = if (config.resvAtma > 0) config.resvAtma else config.atma
                                    Text(text = CommonUtils.formatRupiah(amount.toDouble()), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Text(text = "Jumlah maksimum(Rp)", fontSize = 12.sp, color = Color.Gray)
                                }
                                
                                Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    val durationValue = overrideDuration ?: if (durationUnit == "bulan") config.bpio else config.peo
                                    Text(text = "$durationValue $durationUnit", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    val label = if (durationUnit == "bulan") "Periode terpanjang" else "Jangka Pinjaman"
                                    Text(text = label, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onNavigate(targetRoute) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                            enabled = config.koc == false
                        ) {
                            if (config.koc) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(buttonText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentBillCard(
    bill: com.example.ivopay.app.data.model.LoanOrder,
    hasPgsh: Boolean,
    isWof: Boolean,
    isWiue: Boolean,
    onNavigate: (String) -> Unit,
    isExtra: Boolean = false
) {
    val showRepayBtn = bill.asu in listOf(303, 301, 302, 304, 305, 306, 307, 308) // Mapping Vue AS states
    val btnColor = Color(0xFFFE5455)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).clickable { onNavigate("BillDetails") }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color(0xFFFE5455)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Lamaran saya", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                StatusBadge(asu = bill.asu, hasPgsh = hasPgsh)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = if (showRepayBtn) "Repayment Amount (Rp)" else "Nilai Pinjaman (Rp)", color = Color.Gray, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CommonUtils.formatRupiah((if (showRepayBtn) bill.csp else bill.tma).toDouble()),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262626)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val durationText = if (isExtra) "${bill.peo} hari" else (bill.bpioTxt ?: "--")
                    Text(text = durationText, fontWeight = FontWeight.Bold)
                    val labelText = if (isExtra) "Waktu peminjaman" else "Periode terpanjang"
                    Text(text = labelText, fontSize = 12.sp, color = Color.Gray)
                }
                
                Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val dateText = if (showRepayBtn) bill.dud else bill.ade
                    val label = if (showRepayBtn) "Tanggal pembayaran" else "Application Time"
                    Text(text = dateText ?: "--", fontWeight = FontWeight.Bold)
                    Text(text = label, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                showRepayBtn -> {
                    Text(
                        text = "Untuk menjaga keamanan akun, harap salin kode pembayaran terbaru dari dalam tagihan untuk melakukan pembayaran",
                        fontSize = 11.sp,
                        color = Color(0xFFFE5455),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(onClick = { onNavigate("RepayPage") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Bayar Segera")
                    }
                }
                bill.asu == 203 -> { // passed_wait_confirm
                    Button(onClick = { onNavigate("BillDetailsWaitConfirm") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Konfirmasi untuk penarikan dana")
                    }
                }
                bill.asu == 601 -> { // wait_borrow_sign
                    Button(
                        onClick = { 
                            onNavigate("BorrowerSignContracts?noc=${bill.noc}&wiue=$isWiue") 
                        }, 
                        modifier = Modifier.fillMaxWidth(), 
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
                    ) {
                        Text("Proses tanda tangan")
                    }
                }
                else -> {
                    if (bill.bae) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFE5455))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = bill.baeTtm ?: "", fontSize = 11.sp)
                        }
                    }
                    Button(onClick = { onNavigate("BillDetails") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Periksa detailnya")
                    }
                }
            }
        }
    }
}

@Composable
fun CashLoanCard(
    config: com.example.ivopay.app.data.model.LoanProductConfig?,
    cashData: com.example.ivopay.app.data.model.CashConfigData?,
    showAmount: Long,
    isWof: Boolean,
    isWiue: Boolean,
    onNavigate: (String) -> Unit
) {
    val bill = config?.podi
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val hasPgsh = sessionManager.getHasPgsh()

    if (bill != null) {
        BillCard(bill = bill, hasPgsh = hasPgsh, isWof = isWof, isWiue = isWiue, onNavigate = onNavigate)
    } else if (config?.psw == 1) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Header: "Produk pinjaman tunai" with Hand Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star, // Use Star for Hand replacement
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFFE5455)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "Produk pinjaman tunai", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Tip Box inside card as per Vue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = CommonUtils.formatRupiah(showAmount.toDouble()), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = "Jumlah maksimum(Rp)", fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFEEEEEE))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            val term = cashData?.peoGfd ?: "${cashData?.peo ?: 0} hari"
                            Text(text = term, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = "Jangka Pinjaman", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigate("CashLoan") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455)),
                        enabled = config.koc == false
                    ) {
                        Text("Ajukan pinjaman")
                    }
                }
            }
        }
    }
}

@Composable
fun BillCard(
    bill: com.example.ivopay.app.data.model.LoanOrder,
    hasPgsh: Boolean,
    isWof: Boolean,
    isWiue: Boolean,
    onNavigate: (String) -> Unit
) {
    // Logic for showRepayBtn: asu in overdue, using_money, expired, etc.
    val showRepayBtn = bill.asu in listOf(303, 301, 302, 304, 305, 306, 307, 308) // Mapping Vue AS states
    val btnColor = Color(0xFFFE5455)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).clickable { onNavigate("BillDetails") }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title with border left (Simulated)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color(0xFFFE5455)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Lamaran saya", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                StatusBadge(asu = bill.asu, hasPgsh = hasPgsh)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = if (showRepayBtn) "Repayment Amount (Rp)" else "Nilai Pinjaman (Rp)", color = Color.Gray, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CommonUtils.formatRupiah((if (showRepayBtn) bill.csp else bill.tma).toDouble()),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262626)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val days = bill.peoGfd ?: "${bill.peo} hari"
                    Text(text = days, fontWeight = FontWeight.Bold)
                    Text(text = "Waktu peminjaman", fontSize = 12.sp, color = Color.Gray)
                }
                
                Box(modifier = Modifier.height(30.dp).width(1.dp).background(Color(0xFFEEEEEE)))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val dateText = if (showRepayBtn) bill.dud else bill.ade
                    val label = if (showRepayBtn) "Tanggal pembayaran" else "Application Time"
                    Text(text = dateText ?: "--", fontWeight = FontWeight.Bold)
                    Text(text = label, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons based on status
            when {
                showRepayBtn -> {
                    Text(
                        text = "Untuk menjaga keamanan akun, harap salin kode pembayaran terbaru dari dalam tagihan untuk melakukan pembayaran",
                        fontSize = 11.sp,
                        color = Color(0xFFFE5455),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(onClick = { onNavigate("RepayPage") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Bayar Segera")
                    }
                }
                bill.asu == 203 -> { // passed_wait_confirm
                    Button(onClick = { onNavigate("BillDetailsWaitConfirm") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Konfirmasi untuk penarikan dana")
                    }
                }
                bill.asu == 601 -> { // wait_borrow_sign
                    Button(onClick = { onNavigate("BorrowerSignContracts") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))) {
                        Text("Proses tanda tangan")
                    }
                }
                else -> {
                    if (bill.bae) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFE5455))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = bill.baeTtm ?: "", fontSize = 11.sp)
                        }
                    }
                    Button(onClick = { onNavigate("BillDetails") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
                        Text("Periksa detailnya")
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    viewModel: BorrowerHomeViewModel,
    onNavigate: (String) -> Unit
) {
    var amount by remember { mutableFloatStateOf(viewModel.showAmount.toFloat()) }
    val cashData = viewModel.cashData

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nilai Pinjaman (Rp)", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = CommonUtils.formatRupiah(amount.toDouble()),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626)
            )

            Slider(
                value = amount,
                onValueChange = { amount = it },
                valueRange = (cashData?.itma?.toFloat() ?: 0f)..(cashData?.atma?.toFloat() ?: 5000000f),
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFE5455), activeTrackColor = Color(0xFFFE5455))
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = CommonUtils.formatRupiah(cashData?.itma?.toDouble() ?: 0.0), fontSize = 12.sp, color = Color.Gray)
                Text(text = CommonUtils.formatRupiah(cashData?.atma?.toDouble() ?: 5000000.0), fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Jangka Pinjaman")
                Text("${cashData?.peo ?: 0} hari", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.onApplyClick(onNavigate) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
            ) {
                Text("Permohonan")
            }
        }
    }
}

@Composable
fun StatusBadge(asu: Int, hasPgsh: Boolean) {
    val display = LoanStatusMapper.getStatusColor(asu, hasPgsh)

    Surface(
        color = display.bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = display.text, color = display.color, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
fun NormalStatusCard(
    nodp: com.example.ivopay.app.data.model.NodpData?,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Nilai Pinjaman (Rp)", fontSize = 14.sp, color = Color.Gray)
            Text(
                text = CommonUtils.formatRupiah(nodp?.tma?.toDouble() ?: 5000000.0),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF262626)
            )
            Text(text = "Pelunasan total: ${CommonUtils.formatRupiah(nodp?.datm?.toDouble() ?: 5200000.0)}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFBFBFB), RoundedCornerShape(4.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${nodp?.peo ?: 91} hari", fontWeight = FontWeight.Bold)
                    Text(text = "Waktu peminjaman", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = nodp?.dud ?: "--", fontWeight = FontWeight.Bold)
                    Text(text = "Tanggal pembayaran", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE5455))
            ) {
                Text("Ajukan Pinjaman")
            }
        }
    }
}
